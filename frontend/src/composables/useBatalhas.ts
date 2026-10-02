import { ref } from 'vue'
import { get } from '../api/http'

export type ResultadoBatalha = 'VITORIA' | 'DERROTA'
export type LadoBatalha = 'TROPA' | 'INIMIGO'

export interface BatalhaResumoDTO {
  id: number
  turno: number
  tropaId: number | null
  tropaNome: string
  masmorraId: number
  masmorraNivel: number
  regiaoIndice: number
  resultado: ResultadoBatalha
  rodadas: number
  criadoEm: string
}

export interface ParticipanteDTO {
  id: number
  lado: LadoBatalha
  nome: string
  pvMax: number
  pvFinal: number
}

export interface ReferenciaDTO {
  id: number
  lado: LadoBatalha
  nome: string
}

export interface AcaoDTO {
  atacante: ReferenciaDTO
  alvo: ReferenciaDTO
  dano: number
  critico: boolean
  pvAntes: number
  pvDepois: number
  abatido: boolean
}

export interface RodadaDTO {
  numero: number
  acoes: AcaoDTO[]
}

export interface BonusRecompensaDTO {
  codigo: string
  valor: number
}

export interface ItemRecompensaDTO {
  itemId: number
  subtipo: string
  categoria: string
  nivel: number
  qualidade: string
  bonus: BonusRecompensaDTO[]
  atributoEscolhido: string | null
}

export interface PedraRecompensaDTO {
  pedraId: number
  qualidade: string
  bonus: { codigo: string; magnitude: string; valor: number }[]
}

export interface RecompensasBatalhaDTO {
  ouro: number
  recursos: Record<string, number>
  item: ItemRecompensaDTO | null
  xpPorGuerreiro: number
  guerreirosXp: number[]
  pedras: PedraRecompensaDTO[]
}

export interface BatalhaDetalheDTO {
  id: number
  turno: number
  tropaId: number | null
  tropaNome: string
  masmorraId: number
  masmorraNivel: number
  regiaoIndice: number
  resultado: ResultadoBatalha
  totalRodadas: number
  criadoEm: string
  participantes: ParticipanteDTO[]
  rodadas: RodadaDTO[]
  recompensas: RecompensasBatalhaDTO | null
}

export const ROTULOS_RESULTADO: Record<string, string> = { VITORIA: 'VITÓRIA', DERROTA: 'DERROTA' }

export function useBatalhas() {
  const batalhas = ref<BatalhaResumoDTO[]>([])
  const batalha = ref<BatalhaDetalheDTO | null>(null)
  const carregando = ref(false)
  const erro = ref<string | null>(null)

  async function listar() {
    carregando.value = true
    erro.value = null
    try {
      batalhas.value = await get<BatalhaResumoDTO[]>('/api/jogo/batalhas')
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar as batalhas'
    } finally {
      carregando.value = false
    }
  }

  async function buscar(id: number) {
    carregando.value = true
    erro.value = null
    batalha.value = null
    try {
      batalha.value = await get<BatalhaDetalheDTO>(`/api/jogo/batalhas/${id}`)
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar a batalha'
    } finally {
      carregando.value = false
    }
  }

  return { batalhas, batalha, carregando, erro, listar, buscar }
}
