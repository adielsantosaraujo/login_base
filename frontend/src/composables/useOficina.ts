import { computed, ref } from 'vue'
import { get, patch, post } from '../api/http'

export interface ArtesaoDTO {
  cidadaoId: number
  nome: string
  peEfetivo: number
  eficiencia: number
  ocupado: boolean
}

export interface OficinaDTO {
  id: number
  tipo: 'FERRARIA' | 'ALFAIATARIA' | 'CARPINTARIA' | string
  nivel: string
  estado: string
  nivelMaximoItem: number
  artesaos: ArtesaoDTO[]
}

export interface FabricacaoDTO {
  id: number
  subtipo: string
  nivel: number
  artesaoId: number
  artesaoNome: string
  pfAtual: number
  pfTotal: number
  estado: 'EM_ANDAMENTO' | 'PAUSADA' | string
  turnosEstimados: number | null
  itemId: number | null
}

export interface ReceitaDTO {
  subtipo: string
  nome?: string
  categoria?: string
  receitaBase: Record<string, number>
  custoPorNivel: Record<string, Record<string, number>>
  peMinimoPorNivel: Record<string, number>
  exigeAtributo: boolean
}

export interface NovaFabricacao {
  subtipo: string
  nivel: number
  artesaoId: number
  atributoEscolhido?: string
}

export const TIPOS_OFICINA = ['FERRARIA', 'ALFAIATARIA', 'CARPINTARIA']

export function ehOficina(tipo: string | undefined | null): boolean {
  return !!tipo && TIPOS_OFICINA.includes(tipo)
}

export function percentualPf(f: Pick<FabricacaoDTO, 'pfAtual' | 'pfTotal'>): number {
  if (!f.pfTotal) return 0
  return Math.min(100, Math.max(0, Math.round((Number(f.pfAtual) / f.pfTotal) * 100)))
}

export function peMinimoDoNivel(r: ReceitaDTO | null | undefined, nivel: number): number {
  return r?.peMinimoPorNivel?.[String(nivel)] ?? 0
}

export function custoDoNivelItem(r: ReceitaDTO | null | undefined, nivel: number): Record<string, number> {
  if (!r) return {}
  return r.custoPorNivel?.[String(nivel)] ?? r.receitaBase ?? {}
}

/** Motivo pelo qual o artesão não pode ser escolhido, ou null se pode. */
export function motivoArtesaoIndisponivel(a: ArtesaoDTO, peMinimo: number): string | null {
  if (a.ocupado) return 'ocupado'
  if (a.peEfetivo < peMinimo) return `PE insuficiente (mín. ${peMinimo})`
  return null
}

export function useOficina() {
  const oficina = ref<OficinaDTO | null>(null)
  const fila = ref<FabricacaoDTO[]>([])
  const receitas = ref<ReceitaDTO[]>([])
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)

  const artesaosLivres = computed(() => (oficina.value?.artesaos ?? []).filter((a) => !a.ocupado))

  async function carregarFila(id: number) {
    fila.value = await get<FabricacaoDTO[]>(`/api/jogo/oficinas/${id}/fila`)
  }

  async function carregar(id: number) {
    carregando.value = true
    erro.value = null
    try {
      const [o, f, r] = await Promise.all([
        get<OficinaDTO>(`/api/jogo/oficinas/${id}`),
        get<FabricacaoDTO[]>(`/api/jogo/oficinas/${id}/fila`),
        get<ReceitaDTO[]>(`/api/jogo/oficinas/${id}/receitas`),
      ])
      oficina.value = o
      fila.value = f
      receitas.value = r
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar a oficina'
    } finally {
      carregando.value = false
    }
  }

  async function recarregar(id: number) {
    const [o, f] = await Promise.all([
      get<OficinaDTO>(`/api/jogo/oficinas/${id}`),
      get<FabricacaoDTO[]>(`/api/jogo/oficinas/${id}/fila`),
    ])
    oficina.value = o
    fila.value = f
  }

  /** Retorna a fabricação criada ou null em caso de erro (mensagem em `erro`). */
  async function fabricar(id: number, req: NovaFabricacao): Promise<FabricacaoDTO | null> {
    enviando.value = true
    erro.value = null
    try {
      const criada = await post<FabricacaoDTO>(`/api/jogo/oficinas/${id}/fabricacoes`, req)
      await recarregar(id)
      return criada
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao iniciar a fabricação'
      return null
    } finally {
      enviando.value = false
    }
  }

  async function reatribuir(id: number, fabricacaoId: number, artesaoId: number): Promise<boolean> {
    enviando.value = true
    erro.value = null
    try {
      await patch(`/api/jogo/fabricacoes/${fabricacaoId}`, { artesaoId })
      await recarregar(id)
      return true
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao reatribuir o artesão'
      return false
    } finally {
      enviando.value = false
    }
  }

  return { oficina, fila, receitas, carregando, enviando, erro, artesaosLivres, carregar, carregarFila, recarregar, fabricar, reatribuir }
}
