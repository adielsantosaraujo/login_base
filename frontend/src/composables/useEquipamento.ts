import { ref } from 'vue'
import { del, get, put } from '../api/http'
import type { Categoria, ItemDTO } from './useItens'

export type MapaEquipamento = Record<string, ItemDTO | null | undefined>

export interface InventarioPagina {
  itens: ItemDTO[]
  total: number
  page: number
  pageSize: number
}

export const SLOTS_EQUIPAMENTO = [
  'ARMA', 'FERRAMENTA', 'PEITORAL', 'CAPACETE', 'OMBREIRAS', 'LUVAS', 'CALCAS', 'SAPATO', 'COLAR', 'ANEL_1', 'ANEL_2',
] as const

/** Categoria de item compatível com o slot. */
export function categoriaDoSlot(slot: string): Categoria {
  if (slot === 'ARMA') return 'ARMA'
  if (slot === 'FERRAMENTA') return 'FERRAMENTA'
  if (slot === 'COLAR' || slot.startsWith('ANEL')) return 'JOIA'
  return 'ARMADURA'
}

/** Subtipo exigido pelo slot (peça de armadura, colar, anel); null para arma/ferramenta. */
export function subtipoDoSlot(slot: string): string | null {
  if (slot === 'ARMA' || slot === 'FERRAMENTA') return null
  return slot.startsWith('ANEL') ? 'ANEL' : slot
}

export function useEquipamento() {
  const itens = ref<ItemDTO[]>([])
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)

  async function carregarInventario(categoria: Categoria, size = 100): Promise<void> {
    carregando.value = true
    erro.value = null
    try {
      const r = await get<InventarioPagina>(`/api/jogo/inventario?categoria=${categoria}&page=0&size=${size}`)
      itens.value = r.itens ?? []
    } catch (e) {
      itens.value = []
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar inventário'
    } finally {
      carregando.value = false
    }
  }

  /** Retorna o mapa atualizado de equipamento, ou null em erro (mensagem em `erro`). */
  async function equipar(cidadaoId: number | string, slot: string, itemId: number): Promise<MapaEquipamento | null> {
    enviando.value = true
    erro.value = null
    try {
      return await put<MapaEquipamento>(`/api/jogo/cidadao/${cidadaoId}/equipamento/${slot}`, { itemId })
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao equipar'
      return null
    } finally {
      enviando.value = false
    }
  }

  async function remover(cidadaoId: number | string, slot: string): Promise<MapaEquipamento | null> {
    enviando.value = true
    erro.value = null
    try {
      return await del<MapaEquipamento>(`/api/jogo/cidadao/${cidadaoId}/equipamento/${slot}`)
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao remover'
      return null
    } finally {
      enviando.value = false
    }
  }

  return { itens, carregando, enviando, erro, carregarInventario, equipar, remover }
}
