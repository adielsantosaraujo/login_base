import { ref } from 'vue'
import { get, post } from '../api/http'
import type { BonusRegiao, TipoRegiao } from '../domain/regioes'

export interface CatalogoConstrucao {
  tipo: string
  nome: string
  /** Tipos de região em que a construção é permitida. */
  regioes: TipoRegiao[]
  /** Bônus de região associado à construção, se houver. */
  bonusRegiao: BonusRegiao | null
  custoN1: Record<string, number>
  tamanho: number
  poN1: number
  profissoes?: string[]
}

export interface ConstrucaoCriada {
  id: number
  tipo: string
  nivel: string
  regiaoIndice: number
  x: number
  y: number
  tamanho: number
  estado: string
  poTotal: number
  poAtual: number
}

export interface CriarConstrucao {
  tipo: string
  regiaoIndice: number
  x: number
  y: number
}

export function permitidoNaRegiao(item: CatalogoConstrucao, tipoRegiao: string | null | undefined): boolean {
  return !!tipoRegiao && item.regioes.includes(tipoRegiao as TipoRegiao)
}

export function useConstrucoes() {
  const catalogo = ref<CatalogoConstrucao[]>([])
  const carregando = ref(false)
  const criando = ref(false)
  const erro = ref<string | null>(null)

  async function carregarCatalogo() {
    carregando.value = true
    erro.value = null
    try {
      catalogo.value = await get<CatalogoConstrucao[]>('/api/jogo/construcoes/catalogo')
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar o catálogo'
    } finally {
      carregando.value = false
    }
  }

  /** Cria a construção; retorna null e preenche `erro` (mensagem do backend) em 400/409. */
  async function criar(req: CriarConstrucao): Promise<ConstrucaoCriada | null> {
    criando.value = true
    erro.value = null
    try {
      return await post<ConstrucaoCriada>('/api/jogo/construcoes', req)
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao criar a construção'
      return null
    } finally {
      criando.value = false
    }
  }

  return { catalogo, carregando, criando, erro, carregarCatalogo, criar }
}
