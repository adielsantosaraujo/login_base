import { computed, ref } from 'vue'
import { get, post } from '../api/http'
import type { Estoque } from './useEstoque'
import type { TipoRegiao } from './useMapa'

export interface CustoAnexacao {
  ouro: number
  madeira: number
  pedra: number
}

export interface ResultadoAnexacao {
  regiao: { indice: number; tipo: TipoRegiao; possuida: boolean }
  estoque: Record<string, number>
  custo: CustoAnexacao
}

const LADO_GRADE = 4

/** Vizinhas ortogonais de uma região (índice = linha×4+coluna+1). */
export function vizinhas(indice: number): number[] {
  const linha = Math.floor((indice - 1) / LADO_GRADE)
  const coluna = (indice - 1) % LADO_GRADE
  const res: number[] = []
  if (linha > 0) res.push(indice - LADO_GRADE)
  if (linha < LADO_GRADE - 1) res.push(indice + LADO_GRADE)
  if (coluna > 0) res.push(indice - 1)
  if (coluna < LADO_GRADE - 1) res.push(indice + 1)
  return res
}

export function ehAnexavel(
  indice: number,
  regioes: { indice: number; possuida: boolean; masmorraAtiva?: boolean }[],
): boolean {
  const alvo = regioes.find((r) => r.indice === indice)
  if (!alvo || alvo.possuida || alvo.masmorraAtiva) return false
  const possuidas = new Set(regioes.filter((r) => r.possuida).map((r) => r.indice))
  return vizinhas(indice).some((v) => possuidas.has(v))
}

export function useAnexacao() {
  const custo = ref<CustoAnexacao | null>(null)
  const disponivel = ref<{ ouro: number; madeira: number; pedra: number }>({ ouro: 0, madeira: 0, pedra: 0 })
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)

  const suficiente = computed(
    () =>
      custo.value !== null &&
      disponivel.value.ouro >= custo.value.ouro &&
      disponivel.value.madeira >= custo.value.madeira &&
      disponivel.value.pedra >= custo.value.pedra,
  )

  async function carregar(indice: number) {
    carregando.value = true
    erro.value = null
    custo.value = null
    try {
      const [c, e] = await Promise.all([
        get<CustoAnexacao>(`/api/jogo/regioes/${indice}/custo-anexacao`),
        get<Estoque>('/api/jogo/estoque'),
      ])
      custo.value = c
      const qtd = (r: string) => e.recursos.find((l) => l.recurso === r)?.quantidade ?? 0
      disponivel.value = { ouro: qtd('OURO'), madeira: qtd('MADEIRA'), pedra: qtd('PEDRA') }
    } catch (ex) {
      erro.value = ex instanceof Error ? ex.message : 'Erro ao carregar o custo'
    } finally {
      carregando.value = false
    }
  }

  async function anexarRegiao(indice: number): Promise<ResultadoAnexacao | null> {
    enviando.value = true
    erro.value = null
    try {
      return await post<ResultadoAnexacao>(`/api/jogo/regioes/${indice}/anexar`)
    } catch (ex) {
      erro.value = ex instanceof Error ? ex.message : 'Erro ao anexar a região'
      return null
    } finally {
      enviando.value = false
    }
  }

  return { custo, disponivel, suficiente, carregando, enviando, erro, carregar, anexarRegiao }
}
