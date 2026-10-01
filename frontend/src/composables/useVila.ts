import { computed, ref } from 'vue'
import { get, post } from '../api/http'
import { marcarVilaCriada } from '../router/guardaVila'
import type { TipoRegiao } from './useMapa'

export interface RegiaoPrevia {
  indice: number
  jazidas: Record<string, number>
}

export interface PreviaVila {
  semente: number
  regioes: RegiaoPrevia[]
}

export const LARGURA_GRADE = 4
export const MAX_REGIOES = 3

/** Duas regiões são adjacentes na grade 4x4 se diferem em 1 linha ou 1 coluna (sem diagonal). */
export function adjacentes(a: number, b: number): boolean {
  const la = Math.floor((a - 1) / LARGURA_GRADE)
  const ca = (a - 1) % LARGURA_GRADE
  const lb = Math.floor((b - 1) / LARGURA_GRADE)
  const cb = (b - 1) % LARGURA_GRADE
  return Math.abs(la - lb) + Math.abs(ca - cb) === 1
}

export function useCriacaoVila() {
  const previa = ref<PreviaVila | null>(null)
  const selecao = ref<number[]>([])
  const tipos = ref<Record<number, TipoRegiao>>({})
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)
  const avisoSelecao = ref<string | null>(null)

  const completo = computed(
    () =>
      selecao.value.length === MAX_REGIOES &&
      selecao.value.every((i) => !!tipos.value[i]) &&
      selecao.value.some((i) => tipos.value[i] === 'URBANA'),
  )

  async function carregarPrevia(semente?: number) {
    carregando.value = true
    erro.value = null
    try {
      const url = semente === undefined ? '/api/jogo/vila/preview' : `/api/jogo/vila/preview?semente=${semente}`
      previa.value = await get<PreviaVila>(url)
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar a prévia'
    } finally {
      carregando.value = false
    }
  }

  function permitida(indice: number): boolean {
    const atual = selecao.value
    if (atual.includes(indice) || atual.length === 0) return true
    if (atual.length >= MAX_REGIOES) return false
    return atual.some((i) => adjacentes(i, indice))
  }

  /** Alterna a seleção; recusa (com aviso) regiões não adjacentes ou além do limite. */
  function alternar(indice: number) {
    avisoSelecao.value = null
    const atual = selecao.value
    if (atual.includes(indice)) {
      selecao.value = atual.filter((i) => i !== indice)
      const { [indice]: _removido, ...resto } = tipos.value
      tipos.value = resto
      return
    }
    if (atual.length >= MAX_REGIOES) {
      avisoSelecao.value = 'Você já escolheu 3 regiões. Desmarque uma para trocar.'
      return
    }
    if (!permitida(indice)) {
      avisoSelecao.value = `A região ${indice} não é adjacente às regiões já escolhidas.`
      return
    }
    selecao.value = [...atual, indice]
    tipos.value = { ...tipos.value, [indice]: 'RURAL' }
  }

  function definirTipo(indice: number, tipo: TipoRegiao) {
    tipos.value = { ...tipos.value, [indice]: tipo }
  }

  async function criar(): Promise<boolean> {
    if (!completo.value) return false
    enviando.value = true
    erro.value = null
    try {
      const corpo: { regioesEscolhidas: number[]; tipos: Record<string, TipoRegiao>; semente?: number } = {
        regioesEscolhidas: [...selecao.value],
        tipos: Object.fromEntries(selecao.value.map((i) => [String(i), tipos.value[i]])),
      }
      if (previa.value) corpo.semente = previa.value.semente
      await post('/api/jogo/vila', corpo)
      marcarVilaCriada()
      return true
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao criar a vila'
      return false
    } finally {
      enviando.value = false
    }
  }

  return {
    previa, selecao, tipos, carregando, enviando, erro, avisoSelecao, completo,
    carregarPrevia, alternar, definirTipo, permitida, criar,
  }
}
