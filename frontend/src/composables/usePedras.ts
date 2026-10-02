import { get, post } from '../api/http'
import type { BonusItem, ItemDTO } from './useItens'

export type QualidadePedra = 'SIMPLES' | 'BOA' | 'EXCELENTE' | 'DIVINA'
export type MagnitudeBonus = 'BAIXA' | 'MEDIA' | 'ALTA'

export interface BonusPedra {
  codigo: string
  magnitude: MagnitudeBonus
  valor: number
}

export interface PedraDTO {
  id: number
  qualidade: QualidadePedra
  bonus: BonusPedra[]
  custoEngaste: number
}

export interface ItemCompativelDTO {
  item: ItemDTO
  slotsTotais: number
  slotsLivres: number
}

export interface EngasteResultadoDTO {
  pedraId: number
  itemId: number
  custoOuro: number
  qualidadePedra: QualidadePedra
  bonus: BonusItem[] | BonusPedra[]
  slotsLivresApos: number
}

export interface RemocaoResultadoDTO {
  itemId: number
  pedraRemovidaId: number
  slotsLivresApos: number
  pedraDestruida: boolean
}

const CUSTO_ENGASTE: Record<string, number> = { SIMPLES: 10, BOA: 25, EXCELENTE: 60, DIVINA: 150 }
const ROTULOS_MAGNITUDE: Record<string, string> = { BAIXA: 'Baixa', MEDIA: 'Média', ALTA: 'Alta' }

export function custoEngaste(qualidade: string): number {
  return CUSTO_ENGASTE[qualidade] ?? 0
}

export function rotuloMagnitude(magnitude?: string | null): string {
  if (!magnitude) return '-'
  return ROTULOS_MAGNITUDE[magnitude] ?? magnitude
}

export function usePedras() {
  const listarPedras = () => get<PedraDTO[]>('/api/jogo/pedras')
  const itensCompativeis = () => get<ItemCompativelDTO[]>('/api/jogo/ferraria/itens-compativeis')
  const engastar = (pedraId: number, itemId: number) =>
    post<EngasteResultadoDTO>('/api/jogo/ferraria/engaste', { pedraId, itemId })
  const removerPedra = (itemId: number, pedraId: number) =>
    post<RemocaoResultadoDTO>('/api/jogo/ferraria/remover-pedra', { itemId, pedraId })
  return { listarPedras, itensCompativeis, engastar, removerPedra }
}
