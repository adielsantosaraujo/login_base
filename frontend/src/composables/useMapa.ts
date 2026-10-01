import { ref } from 'vue'
import { get } from '../api/http'

export type TipoRegiao = 'RURAL' | 'URBANA' | 'COLETA'

export interface RegiaoResumo {
  indice: number
  tipo: TipoRegiao | null
  possuida: boolean
  masmorraAtiva: boolean
  nivelMasmorra: number | null
}

export interface MapaVila {
  vila: { id: number; nome: string }
  regioes: RegiaoResumo[]
}

export interface Construcao {
  id: number
  tipo: string
  nivel: string
  tamanho: number
}

export interface Ladrilho {
  x: number
  y: number
  jazida: string | null
  construcao: Construcao | null
}

export interface RegiaoDetalhe {
  regiao: { id: number | null; indice: number; tipo: TipoRegiao | null; possuida: boolean }
  ladrilhos: Ladrilho[]
}

export const ROTULOS_TIPO: Record<string, string> = {
  RURAL: 'Rural',
  URBANA: 'Urbana',
  COLETA: 'Coleta',
}

export const ROTULOS_JAZIDA: Record<string, string> = {
  FLORESTA: 'Floresta',
  ROCHA: 'Rocha',
  BARREIRO: 'Barreiro',
  VEIO_DE_FERRO: 'Veio de ferro',
  VEIO_DE_CARVAO: 'Veio de carvão',
  SALINA: 'Salina',
  ENXOFRE: 'Enxofre',
  CAMPO: 'Campo',
}

export const ROTULOS_CONSTRUCAO: Record<string, string> = {
  CASA: 'Casa',
}

export function rotulo(mapa: Record<string, string>, chave: string | null | undefined): string {
  if (!chave) return ''
  return mapa[chave] ?? chave
}

export function useMapaVila() {
  const mapa = ref<MapaVila | null>(null)
  const regioes = ref<RegiaoResumo[]>([])
  const carregando = ref(false)
  const erro = ref<string | null>(null)

  async function carregar() {
    carregando.value = true
    erro.value = null
    try {
      mapa.value = await get<MapaVila>('/api/jogo/vila/mapa')
      regioes.value = mapa.value.regioes
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar o mapa'
    } finally {
      carregando.value = false
    }
  }

  return { mapa, regioes, carregando, erro, carregar }
}

export function useRegiaoDetalhes() {
  const regiaoSelecionada = ref<RegiaoDetalhe | null>(null)
  const carregando = ref(false)
  const erro = ref<string | null>(null)

  async function carregar(indice: number) {
    carregando.value = true
    erro.value = null
    try {
      regiaoSelecionada.value = await get<RegiaoDetalhe>(`/api/jogo/regioes/${indice}`)
    } catch (e) {
      regiaoSelecionada.value = null
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar a região'
    } finally {
      carregando.value = false
    }
  }

  function limpar() {
    regiaoSelecionada.value = null
    erro.value = null
  }

  return { regiaoSelecionada, carregando, erro, carregar, limpar }
}
