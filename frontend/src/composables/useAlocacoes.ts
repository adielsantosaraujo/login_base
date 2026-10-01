import { computed, ref } from 'vue'
import { del, get, post } from '../api/http'
import type { CatalogoConstrucao, ConstrucaoCriada } from './useConstrucoes'

export interface Alocacao {
  cidadaoId: number
  nome: string
  idadeAnos: number
  profissao: string
  eficiencia: number
}

export interface CidadaoElegivel {
  id: number
  nome: string
  sexo: string
  idadeAnos: number
  familiaId: number | null
  construcaoId: number | null
  profissaoTrabalho: string | null
  faminto: boolean
}

const VAGAS_ATIVA: Record<string, number> = { N1: 2, N2: 5, N3: 10 }
const VAGAS_OBRA: Record<string, number> = { N1: 2, N2: 4, N3: 6 }
export const PROFISSOES_OBRA = ['CONSTRUTOR', 'CARREGADOR']

export function emObra(estado: string | undefined): boolean {
  return estado === 'EM_OBRA' || estado === 'EM_UPGRADE'
}

export function limiteVagas(estado: string, nivel: string): number {
  return (emObra(estado) ? VAGAS_OBRA : VAGAS_ATIVA)[nivel] ?? 0
}

export function useAlocacoes() {
  const predio = ref<ConstrucaoCriada | null>(null)
  const catalogo = ref<CatalogoConstrucao[]>([])
  const alocados = ref<Alocacao[]>([])
  const elegiveis = ref<CidadaoElegivel[]>([])
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)

  const limite = computed(() => (predio.value ? limiteVagas(predio.value.estado, predio.value.nivel) : 0))
  const vagasLivres = computed(() => Math.max(0, limite.value - alocados.value.length))
  const profissoesPermitidas = computed<string[]>(() => {
    const p = predio.value
    if (!p) return []
    if (emObra(p.estado)) return PROFISSOES_OBRA
    const item = catalogo.value.find((c) => c.tipo === p.tipo)
    return item?.profissoes ?? []
  })
  /** Elegíveis ainda sem alocação. */
  const disponiveis = computed(() => elegiveis.value.filter((c) => c.construcaoId == null))

  async function carregarAlocacoes(id: number) {
    const [aloc, eleg] = await Promise.all([
      get<Alocacao[]>(`/api/jogo/construcoes/${id}/alocacoes`),
      get<CidadaoElegivel[]>('/api/jogo/cidadaos?elegiveisTrabalho=true'),
    ])
    alocados.value = aloc
    elegiveis.value = eleg
  }

  async function carregar(id: number) {
    carregando.value = true
    erro.value = null
    try {
      const [p, cat] = await Promise.all([
        get<ConstrucaoCriada>(`/api/jogo/construcoes/${id}`),
        get<CatalogoConstrucao[]>('/api/jogo/construcoes/catalogo'),
      ])
      predio.value = p
      catalogo.value = cat
      await carregarAlocacoes(id)
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar o prédio'
    } finally {
      carregando.value = false
    }
  }

  /** Retorna true se alocou. */
  async function alocar(id: number, cidadaoId: number, profissao?: string | null): Promise<boolean> {
    enviando.value = true
    erro.value = null
    try {
      await post(`/api/jogo/construcoes/${id}/alocacoes`, profissao ? { cidadaoId, profissao } : { cidadaoId })
      await carregarAlocacoes(id)
      return true
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao alocar o cidadão'
      return false
    } finally {
      enviando.value = false
    }
  }

  async function desalocar(id: number, cidadaoId: number): Promise<boolean> {
    enviando.value = true
    erro.value = null
    try {
      await del(`/api/jogo/construcoes/${id}/alocacoes/${cidadaoId}`)
      await carregarAlocacoes(id)
      return true
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao desalocar o cidadão'
      return false
    } finally {
      enviando.value = false
    }
  }

  return {
    predio, catalogo, alocados, elegiveis, carregando, enviando, erro,
    limite, vagasLivres, profissoesPermitidas, disponiveis, carregar, alocar, desalocar,
  }
}
