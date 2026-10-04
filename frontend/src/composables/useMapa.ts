import { ref } from 'vue'
import { get } from '../api/http'
import { ROTULO_TIPO, type TipoRegiao } from '../domain/regioes'
import type { TerrenoDaRegiao, TipoTerreno } from '../domain/terrenos'

export type { TipoRegiao }

export interface RegiaoResumo {
  indice: number
  tipo: TipoRegiao | null
  terrenos: TerrenoDaRegiao[]
  possuida: boolean
  masmorraAtiva: boolean
  nivelMasmorra: number | null
  masmorraId: number | null
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
  estado?: 'EM_OBRA' | 'ATIVA' | 'EM_UPGRADE'
  poAtual?: number
  poTotal?: number
}

export interface Ladrilho {
  x: number
  y: number
  terreno: TipoTerreno
  bonusBase: number
  bonusAdjacente: number
  bonusTotal: number
  construcao: Construcao | null
}

export interface RegiaoDetalhe {
  regiao: { id: number | null; indice: number; tipo: TipoRegiao | null; possuida: boolean; terrenos?: TerrenoDaRegiao[] }
  ladrilhos: Ladrilho[]
}

export const ROTULOS_CONSTRUCAO: Record<string, string> = {
  CASA: 'Casa',
  ARMAZEM: 'Armazém',
  SERRARIA: 'Serraria',
  OLARIA: 'Olaria',
  FUNDICAO: 'Fundição',
  TECELAGEM: 'Tecelagem',
  CURTUME: 'Curtume',
  COZINHA: 'Cozinha',
  FERRARIA: 'Ferraria',
  ALFAIATARIA: 'Alfaiataria',
  CARPINTARIA: 'Carpintaria',
  MERCADO: 'Mercado',
  ESTALAGEM: 'Estalagem',
  QUARTEL: 'Quartel',
  FAZENDA_PLANTIO: 'Fazenda de plantio',
  FAZENDA_CRIACAO: 'Fazenda de criação',
  ACAMPAMENTO_LENHADORES: 'Acampamento de lenhadores',
  PEDREIRA: 'Pedreira',
  BARREIRO: 'Barreiro',
  MINA_FERRO: 'Mina de ferro',
  MINA_CARVAO: 'Mina de carvão',
  SALINA: 'Salina',
  MINA_ENXOFRE: 'Mina de enxofre',
  CABANA_CACA: 'Cabana de caça',
}

/** Rótulos dos tipos de região (domínio) mesclados com os de construção (usados por PainelPredio/UpgradeModal). */
export const ROTULOS_TIPO: Record<string, string> = { ...ROTULO_TIPO, ...ROTULOS_CONSTRUCAO }

export const ROTULOS_ESTADO: Record<string, string> = {
  EM_OBRA: 'Em obra',
  ATIVA: 'Ativa',
  EM_UPGRADE: 'Em upgrade',
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
