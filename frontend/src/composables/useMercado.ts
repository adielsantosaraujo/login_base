import { ref } from 'vue'
import { get, post } from '../api/http'

export interface LinhaPreco {
  recurso: string
  nome: string
  precoBase: number
  precoVenda: number
  precoCompra: number
}

export interface PrecosMercado {
  mercadoAtivo: boolean
  peMelhorComerciante: number
  volumeMaximo: number
  volumeUsado: number
  volumeRestante: number
  precos: LinhaPreco[]
}

export interface OrdemComercio {
  id: number
  turno: number
  tipo: 'VENDA' | 'COMPRA'
  recurso: string
  quantidade: number
  precoUnitario: number
  ouroTotal: number
  criadoEm: string
}

export interface OrdemResultado {
  sucesso: boolean
  tipo: string
  recurso: string
  quantidade: number
  precoUnitario: number
  ouroTotal: number
}

export function useMercado() {
  const precos = ref<PrecosMercado | null>(null)
  const ordens = ref<OrdemComercio[]>([])
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)
  const sucesso = ref<string | null>(null)

  async function carregar() {
    carregando.value = true
    erro.value = null
    try {
      const [p, o] = await Promise.all([
        get<PrecosMercado>('/api/jogo/mercado/precos'),
        get<OrdemComercio[]>('/api/jogo/mercado/ordens'),
      ])
      precos.value = p
      ordens.value = o
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar o mercado'
    } finally {
      carregando.value = false
    }
  }

  /** Envia a ordem; retorna true em caso de sucesso (o chamador recarrega estoque). */
  async function negociar(recurso: string, tipo: 'VENDA' | 'COMPRA', quantidade: number): Promise<boolean> {
    enviando.value = true
    erro.value = null
    sucesso.value = null
    try {
      const r = await post<OrdemResultado>('/api/jogo/mercado/ordens', { recurso, tipo, quantidade })
      sucesso.value = `${tipo === 'VENDA' ? 'Venda' : 'Compra'} realizada: ${r.quantidade} x ${r.recurso} por ${r.ouroTotal} de Ouro`
      await carregar()
      return true
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao executar a ordem'
      return false
    } finally {
      enviando.value = false
    }
  }

  return { precos, ordens, carregando, enviando, erro, sucesso, carregar, negociar }
}
