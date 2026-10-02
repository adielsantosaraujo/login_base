import { ref } from 'vue'
import { get, post } from '../api/http'
import type { ItemDTO } from './useItens'
import type { ArtesaoDTO, FabricacaoDTO, OficinaDTO, ReceitaDTO } from './useOficina'
import { custoDoNivelItem } from './useOficina'
import type { Construcao, Ladrilho, MapaVila, RegiaoDetalhe } from './useMapa'

export interface InventarioDTO {
  itens: ItemDTO[]
  total: number
  page: number
  pageSize: number
}

export interface ItemDetalheDTO {
  item: ItemDTO
  pedras: unknown[]
}

export const NIVEL_MAXIMO_ITEM = 10

const NIVEL_MAX_OFICINA: Record<string, number> = { N1: 3, N2: 6, N3: 10 }

/** Oficina que fabrica/aprimora cada subtipo (espelha os catálogos do backend). */
const OFICINA_DO_SUBTIPO: Record<string, string> = {
  ESPADA: 'FERRARIA', LANCA: 'FERRARIA', ARCO: 'CARPINTARIA', BESTA: 'CARPINTARIA',
  MARTELO: 'FERRARIA', CARRINHO_DE_MAO: 'CARPINTARIA', ENXADA: 'FERRARIA', FORCADO: 'FERRARIA',
  PICARETA: 'FERRARIA', MACHADO: 'FERRARIA', MALHO: 'FERRARIA', CUTELO: 'FERRARIA',
  KIT_DE_COSTURA: 'ALFAIATARIA', FACA_DE_CACA: 'FERRARIA', BALANCA: 'CARPINTARIA',
  PEITORAL: 'FERRARIA', CAPACETE: 'FERRARIA', OMBREIRAS: 'FERRARIA',
  LUVAS: 'ALFAIATARIA', CALCAS: 'ALFAIATARIA', SAPATO: 'ALFAIATARIA',
  COLAR: 'FERRARIA', ANEL: 'FERRARIA',
}

const SLOTS_PEDRA: Record<string, number> = { SIMPLES: 0, BOA: 1, EXCELENTE: 3, DIVINA: 5 }

export function oficinaDoSubtipo(subtipo: string): string | null {
  return OFICINA_DO_SUBTIPO[subtipo] ?? null
}

export function slotsPedra(qualidade: string): number {
  return SLOTS_PEDRA[qualidade] ?? 0
}

export function peMinimoAprimoramento(nivelAtual: number): number {
  return 2 * (nivelAtual + 1) - 2
}

export function podeAprimorar(item: Pick<ItemDTO, 'nivel' | 'emAprimoramento' | 'cidadaoId'>): boolean {
  return item.nivel < NIVEL_MAXIMO_ITEM && !item.emAprimoramento && item.cidadaoId == null
}

export function nivelMaximoDaOficina(nivel: string): number {
  return NIVEL_MAX_OFICINA[nivel] ?? 0
}

/** Custo de aprimorar L -> L+1: 50% (arredondado para cima) do custo de fabricar L+1. */
export function custoAprimoramento(receita: ReceitaDTO | null | undefined, nivelAtual: number): Record<string, number> {
  const cheio = custoDoNivelItem(receita, nivelAtual + 1)
  return Object.fromEntries(Object.entries(cheio).map(([r, q]) => [r, Math.ceil(q / 2)]))
}

export function motivoArtesaoAprimoramento(a: ArtesaoDTO, nivelAtual: number): string | null {
  if (a.ocupado) return 'ocupado'
  const min = peMinimoAprimoramento(nivelAtual)
  if (a.peEfetivo < min) return `PE insuficiente (mín. ${min})`
  return null
}

export function useInventario() {
  const itens = ref<ItemDTO[]>([])
  const total = ref(0)
  const page = ref(0)
  const pageSize = ref(10)
  const categoria = ref<string | null>(null)
  const carregando = ref(false)
  const erro = ref<string | null>(null)

  const detalhe = ref<ItemDetalheDTO | null>(null)
  const oficinas = ref<Construcao[]>([])
  const oficina = ref<OficinaDTO | null>(null)
  const receitas = ref<ReceitaDTO[]>([])
  const enviando = ref(false)
  const erroAcao = ref<string | null>(null)

  async function carregar() {
    carregando.value = true
    erro.value = null
    try {
      const q = new URLSearchParams({ page: String(page.value), size: String(pageSize.value) })
      if (categoria.value) q.set('categoria', categoria.value)
      const r = await get<InventarioDTO>(`/api/jogo/inventario?${q.toString()}`)
      itens.value = r.itens
      total.value = r.total
      page.value = r.page
      pageSize.value = r.pageSize
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar o inventário'
    } finally {
      carregando.value = false
    }
  }

  async function filtrar(cat: string | null) {
    categoria.value = cat
    page.value = 0
    await carregar()
  }

  async function irParaPagina(p: number, tamanho?: number) {
    page.value = p
    if (tamanho) pageSize.value = tamanho
    await carregar()
  }

  async function carregarDetalhe(itemId: number): Promise<ItemDetalheDTO | null> {
    erroAcao.value = null
    try {
      detalhe.value = await get<ItemDetalheDTO>(`/api/jogo/inventario/${itemId}`)
      return detalhe.value
    } catch (e) {
      detalhe.value = null
      erroAcao.value = e instanceof Error ? e.message : 'Erro ao carregar o item'
      return null
    }
  }

  /**
   * Não há endpoint de listagem de oficinas: percorre as regiões possuídas do mapa
   * e coleta construções ATIVAS do tipo exigido, com nível que permita L+1.
   */
  async function carregarOficinasElegiveis(item: ItemDTO): Promise<Construcao[]> {
    erroAcao.value = null
    oficinas.value = []
    oficina.value = null
    receitas.value = []
    const tipo = oficinaDoSubtipo(item.subtipo)
    try {
      const mapa = await get<MapaVila>('/api/jogo/vila/mapa')
      const indices = mapa.regioes.filter((r) => r.possuida).map((r) => r.indice)
      const regioes = await Promise.all(indices.map((i) => get<RegiaoDetalhe>(`/api/jogo/regioes/${i}`)))
      const vistos = new Map<number, Construcao>()
      regioes.forEach((r) =>
        r.ladrilhos.forEach((l: Ladrilho) => {
          const c = l.construcao
          if (c && c.tipo === tipo && c.estado === 'ATIVA' && nivelMaximoDaOficina(c.nivel) >= item.nivel + 1) {
            vistos.set(c.id, c)
          }
        }),
      )
      oficinas.value = [...vistos.values()]
    } catch (e) {
      erroAcao.value = e instanceof Error ? e.message : 'Erro ao carregar as oficinas'
    }
    return oficinas.value
  }

  /** Artesãos (e receitas, para o custo) da oficina escolhida. */
  async function carregarOficina(id: number) {
    erroAcao.value = null
    try {
      const [o, r] = await Promise.all([
        get<OficinaDTO>(`/api/jogo/oficinas/${id}`),
        get<ReceitaDTO[]>(`/api/jogo/oficinas/${id}/receitas`),
      ])
      oficina.value = o
      receitas.value = r
    } catch (e) {
      oficina.value = null
      erroAcao.value = e instanceof Error ? e.message : 'Erro ao carregar a oficina'
    }
  }

  /** Retorna a fabricação criada ou null (mensagem do backend em `erroAcao`). */
  async function aprimorar(itemId: number, oficinaId: number, artesaoId: number): Promise<FabricacaoDTO | null> {
    enviando.value = true
    erroAcao.value = null
    try {
      const f = await post<FabricacaoDTO>(`/api/jogo/inventario/${itemId}/aprimorar`, { oficinaId, artesaoId })
      await carregar()
      return f
    } catch (e) {
      erroAcao.value = e instanceof Error ? e.message : 'Erro ao aprimorar o item'
      return null
    } finally {
      enviando.value = false
    }
  }

  return {
    itens, total, page, pageSize, categoria, carregando, erro,
    detalhe, oficinas, oficina, receitas, enviando, erroAcao,
    carregar, filtrar, irParaPagina, carregarDetalhe, carregarOficinasElegiveis, carregarOficina, aprimorar,
  }
}
