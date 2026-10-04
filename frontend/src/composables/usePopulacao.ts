import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError, get, post } from '../api/http'
import { marcarPopulacaoConfirmada } from '../router/guardaVila'
import {
  CARACTERISTICAS,
  LIMITES,
  MINIMOS,
  PROFISSOES,
  ROTULOS_PROFISSAO,
  bonusLider,
  distribuirPopulacao,
  principal,
  soma,
  trocarPrincipal as trocarPrincipalDominio,
  type Caracteristica,
  type MapaCaracteristicas,
  type MapaProfissoes,
  type Papel,
  type Plano,
  type Profissao,
} from '../domain/populacao'

// Reexports usados por CaracteristicasTab, ProfissoesTab, PainelPredio e PainelCidadao.
export { CARACTERISTICAS, PROFISSOES, ROTULOS_PROFISSAO, soma, bonusLider }

export const TOTAL_POPULACAO = 16

export interface CidadaoPop {
  cidadaoId: number
  nome: string
  sexo: 'M' | 'F'
  idadeAnos: number
  papel: Papel
  caracteristicas: MapaCaracteristicas
  profissoes: MapaProfissoes
}

export interface FamiliaPop {
  familiaId: number
  sobrenome: string
  cidadaos: CidadaoPop[]
}

export interface PopulacaoResposta {
  populacaoConfirmada: boolean
  plano: Plano
  minimos: Plano
  limites: { caracteristicasTotal: number; profissoesTotal: number }
  familiaLiderSugeridaId: number | null
  familiaLiderId: number | null
  familias: FamiliaPop[]
}

export interface ItemChecklist {
  chave: string
  rotulo: string
  ok: boolean
  /** Itens obrigatórios bloqueiam a confirmação; os demais são só avisos. */
  obrigatorio: boolean
}

export interface PendenciasFamilia {
  familiaId: number
  sobrenome: string
  carPendentes: number
  profPendentes: number
  excedeu: boolean
}

const clonar = <T>(v: T): T => JSON.parse(JSON.stringify(v)) as T

export function usePopulacao() {
  const router = useRouter()

  const populacao = ref<PopulacaoResposta | null>(null)
  const familias = ref<FamiliaPop[]>([])
  const familiaLiderId = ref<number | null>(null)
  const plano = ref<Plano>({})
  const minimos = ref<Plano>({ ...MINIMOS })
  const limites = ref({ ...LIMITES })
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)

  const cidadaos = computed(() => familias.value.flatMap((f) => f.cidadaos))

  function achar(cidadaoId: number): CidadaoPop | undefined {
    return cidadaos.value.find((c) => c.cidadaoId === cidadaoId)
  }

  async function carregar() {
    carregando.value = true
    erro.value = null
    try {
      const p = await get<PopulacaoResposta>('/api/jogo/vila/populacao')
      populacao.value = p
      familias.value = clonar(p.familias)
      plano.value = { ...p.plano }
      minimos.value = { ...(p.minimos ?? MINIMOS) }
      limites.value = { ...(p.limites ?? LIMITES) }
      familiaLiderId.value = p.familiaLiderId ?? p.familiaLiderSugeridaId ?? null
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar a população'
    } finally {
      carregando.value = false
    }
  }

  /* ---------- Derivados ---------- */

  const pendentesPorCidadao = computed(() => {
    const r: Record<number, { car: number; prof: number }> = {}
    for (const c of cidadaos.value) {
      r[c.cidadaoId] = {
        car: limites.value.caracteristicasTotal - soma(c.caracteristicas),
        prof: limites.value.profissoesTotal - soma(c.profissoes),
      }
    }
    return r
  })

  const contagemPrincipais = computed(() => {
    const r = Object.fromEntries(PROFISSOES.map((p) => [p, 0])) as Record<Profissao, number>
    for (const c of cidadaos.value) {
      const p = principal(c)
      if (p) r[p]++
    }
    return r
  })

  const pendentesPorFamilia = computed<PendenciasFamilia[]>(() =>
    familias.value.map((f) => {
      let car = 0
      let prof = 0
      let excedeu = false
      for (const c of f.cidadaos) {
        const p = pendentesPorCidadao.value[c.cidadaoId]
        if (p.car < 0 || p.prof < 0) excedeu = true
        car += Math.max(0, p.car)
        prof += Math.max(0, p.prof)
      }
      return { familiaId: f.familiaId, sobrenome: f.sobrenome, carPendentes: car, profPendentes: prof, excedeu }
    }),
  )

  const totalPlano = computed(() => soma(plano.value as Record<string, number>))

  const checklist = computed<ItemChecklist[]>(() => {
    const itens: ItemChecklist[] = [
      {
        chave: 'POPULACAO_COMPLETA',
        rotulo: `${TOTAL_POPULACAO} cidadãos`,
        ok: cidadaos.value.length === TOTAL_POPULACAO,
        obrigatorio: true,
      },
    ]
    for (const [p, min] of Object.entries(minimos.value) as [Profissao, number][]) {
      itens.push({
        chave: `MINIMO_${p}`,
        rotulo: `${ROTULOS_PROFISSAO[p]}es principais: ${contagemPrincipais.value[p]} / ${min}`,
        ok: contagemPrincipais.value[p] >= min,
        obrigatorio: true,
      })
    }
    itens.push({
      chave: 'LIMITES',
      rotulo: 'Pontos dentro dos limites',
      ok: pendentesPorFamilia.value.every((f) => !f.excedeu),
      obrigatorio: true,
    })
    itens.push({
      chave: 'FAMILIA_LIDER',
      rotulo: 'Família líder escolhida',
      ok: familiaLiderId.value !== null && familias.value.some((f) => f.familiaId === familiaLiderId.value),
      obrigatorio: true,
    })
    itens.push({
      chave: 'PENDENTES',
      rotulo: 'Sem pontos pendentes',
      ok: pendentesPorFamilia.value.every((f) => f.carPendentes === 0 && f.profPendentes === 0),
      obrigatorio: false,
    })
    return itens
  })

  const valido = computed(() => checklist.value.filter((i) => i.obrigatorio).every((i) => i.ok))

  /* ---------- Ações (locais, sem servidor) ---------- */

  /** Ajusta uma característica ou profissão, respeitando só os totais (sem máximo por atributo). */
  function editar(cidadaoId: number, chave: string, valor: number) {
    const c = achar(cidadaoId)
    if (!c) return
    const ehCar = (CARACTERISTICAS as readonly string[]).includes(chave)
    const ehProf = (PROFISSOES as readonly string[]).includes(chave)
    if (!ehCar && !ehProf) return
    const mapa = (ehCar ? c.caracteristicas : c.profissoes) as Record<string, number>
    const total = ehCar ? limites.value.caracteristicasTotal : limites.value.profissoesTotal
    const outros = soma(mapa) - (mapa[chave] || 0)
    const v = Number.isFinite(valor) ? Math.floor(valor) : 0
    mapa[chave] = Math.max(0, Math.min(v, total - outros))
  }

  function trocarPrincipal(cidadaoId: number, novaProfissao: Profissao) {
    const c = achar(cidadaoId)
    if (!c) return
    const r = trocarPrincipalDominio(c, novaProfissao, plano.value)
    c.caracteristicas = r.cidadao.caracteristicas
    c.profissoes = r.cidadao.profissoes
    plano.value = r.plano
  }

  /** Não desce abaixo do mínimo nem faz o total passar de 16. */
  function alterarPlano(profissao: Profissao, quantidade: number) {
    const atual = plano.value[profissao] || 0
    const outros = totalPlano.value - atual
    const min = minimos.value[profissao] || 0
    const q = Number.isFinite(quantidade) ? Math.floor(quantidade) : atual
    plano.value = { ...plano.value, [profissao]: Math.max(min, Math.min(q, TOTAL_POPULACAO - outros)) }
  }

  function redistribuir() {
    if (totalPlano.value !== TOTAL_POPULACAO) return
    const base = familias.value.map((f) => ({
      ...f,
      cidadaos: f.cidadaos.map((c) => ({ ...c, papel: c.papel })),
    }))
    const r = distribuirPopulacao(base, plano.value)
    familias.value = r.map((f) => ({
      familiaId: f.familiaId,
      sobrenome: f.sobrenome,
      cidadaos: f.cidadaos.map((c) => ({ ...c })),
    }))
  }

  /** Zera características e profissões de todos, sem mudar plano nem família líder. */
  function zerar() {
    for (const c of cidadaos.value) {
      for (const k of CARACTERISTICAS) c.caracteristicas[k as Caracteristica] = 0
      for (const k of PROFISSOES) c.profissoes[k] = 0
    }
  }

  function escolherLider(familiaId: number) {
    familiaLiderId.value = familiaId
  }

  async function confirmar(): Promise<boolean> {
    if (!valido.value) {
      const falha = checklist.value.find((i) => i.obrigatorio && !i.ok)
      erro.value = falha ? falha.rotulo : 'Corrija a distribuição'
      return false
    }
    enviando.value = true
    erro.value = null
    try {
      await post('/api/jogo/vila/populacao', {
        familiaLiderId: familiaLiderId.value,
        familias: familias.value.map((f) => ({
          familiaId: f.familiaId,
          cidadaos: f.cidadaos.map((c) => ({
            cidadaoId: c.cidadaoId,
            caracteristicas: { ...c.caracteristicas },
            profissoes: { ...c.profissoes },
          })),
        })),
      })
      marcarPopulacaoConfirmada()
      await router.push('/jogo/mapa')
      return true
    } catch (e) {
      if (e instanceof ApiError && e.status === 409 && e.codigo === 'POPULACAO_JA_CONFIRMADA') {
        marcarPopulacaoConfirmada()
        await router.push('/jogo/mapa')
        return true
      }
      erro.value = e instanceof Error ? e.message : 'Erro ao confirmar'
      return false
    } finally {
      enviando.value = false
    }
  }

  return {
    populacao, familias, familiaLiderId, plano, minimos, limites, carregando, enviando, erro,
    cidadaos, pendentesPorCidadao, contagemPrincipais, pendentesPorFamilia, totalPlano, checklist, valido,
    carregar, editar, trocarPrincipal, alterarPlano, redistribuir, zerar, escolherLider, confirmar,
  }
}

