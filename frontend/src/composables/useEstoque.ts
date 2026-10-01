import { ref } from 'vue'
import { get } from '../api/http'

export interface LinhaEstoque {
  recurso: string
  nome: string
  quantidade: number
  /** null = ilimitada (Ouro) */
  capacidade: number | null
  percentualUsado: number | null
}

export interface Estoque {
  recursos: LinhaEstoque[]
}

/** Quantidade excedente a partir da qual a linha é destacada (90% da capacidade). */
export const LIMITE_ALERTA = 0.9

export function proximoDoLimite(linha: LinhaEstoque): boolean {
  return linha.capacidade !== null && linha.quantidade > LIMITE_ALERTA * linha.capacidade
}

export function useEstoque() {
  const recursos = ref<LinhaEstoque[]>([])
  const carregando = ref(false)
  const erro = ref<string | null>(null)

  async function carregar() {
    carregando.value = true
    erro.value = null
    try {
      const estoque = await get<Estoque>('/api/jogo/estoque')
      recursos.value = estoque.recursos
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar o estoque'
    } finally {
      carregando.value = false
    }
  }

  return { recursos, carregando, erro, carregar }
}
