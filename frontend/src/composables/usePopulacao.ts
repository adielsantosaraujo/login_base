import { computed, ref } from 'vue'
import { get, post } from '../api/http'
import { marcarPopulacaoConfirmada } from '../router/guardaVila'

export const CARACTERISTICAS = ['VIT', 'FOR', 'VEL', 'INT', 'CAR'] as const
export const PROFISSOES = [
  'CONSTRUTOR', 'CARREGADOR', 'AGRICULTOR', 'FAZENDEIRO', 'MINEIRO', 'MADEIREIRO',
  'FERREIRO', 'COZINHEIRO', 'COSTUREIRO', 'CACADOR', 'GUERREIRO', 'COMERCIANTE',
] as const

export const ROTULOS_PROFISSAO: Record<string, string> = {
  CONSTRUTOR: 'Construtor', CARREGADOR: 'Carregador', AGRICULTOR: 'Agricultor', FAZENDEIRO: 'Fazendeiro',
  MINEIRO: 'Mineiro', MADEIREIRO: 'Madeireiro', FERREIRO: 'Ferreiro', COZINHEIRO: 'Cozinheiro',
  COSTUREIRO: 'Costureiro', CACADOR: 'Caçador', GUERREIRO: 'Guerreiro', COMERCIANTE: 'Comerciante',
}

export const MAX_POR_CARACTERISTICA = 10
export const MAX_TOTAL_CARACTERISTICAS = 20
export const MAX_POR_PROFISSAO = 5
export const MAX_TOTAL_PROFISSOES = 10
export const MAX_BONUS_LIDER = 10

export interface CidadaoPop {
  id: number
  nome: string
  sexo: 'M' | 'F'
  idadeAnos: number
  caracteristicas: Record<string, number>
  pontosCarPendentes: number
  pontosProfPendentes: number
  profissoes: Record<string, number>
}

export interface FamiliaPop {
  id: number
  sobrenome: string
  cidadaos: CidadaoPop[]
}

export interface Populacao {
  populacaoConfirmada: boolean
  familiaLiderId: number | null
  familias: FamiliaPop[]
}

export interface Distribuicao {
  caracteristicas: Record<string, number>
  profissoes: Record<string, number>
}

export const soma = (m: Record<string, number>) => Object.values(m).reduce((a, b) => a + (b || 0), 0)

/** Erros de limites de uma distribuição (vazio = válida). */
export function validarDistribuicao(d: Distribuicao): string[] {
  const erros: string[] = []
  if (Object.values(d.caracteristicas).some((v) => v > MAX_POR_CARACTERISTICA))
    erros.push(`Máximo ${MAX_POR_CARACTERISTICA} por característica`)
  if (soma(d.caracteristicas) > MAX_TOTAL_CARACTERISTICAS)
    erros.push(`Máximo ${MAX_TOTAL_CARACTERISTICAS} pontos de característica`)
  if (Object.values(d.profissoes).some((v) => v > MAX_POR_PROFISSAO))
    erros.push(`Máximo ${MAX_POR_PROFISSAO} por profissão`)
  if (soma(d.profissoes) > MAX_TOTAL_PROFISSOES) erros.push(`Máximo ${MAX_TOTAL_PROFISSOES} pontos de profissão`)
  return erros
}

/** Bônus do líder: +1% a cada 2 pontos de CAR, limitado a 10%. */
export function bonusLider(car: number): number {
  return Math.min(MAX_BONUS_LIDER, Math.floor(car / 2))
}

export function usePopulacao() {
  const populacao = ref<Populacao | null>(null)
  const distribuicoes = ref<Record<number, Distribuicao>>({})
  const familiaLiderId = ref<number | null>(null)
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)

  function dist(id: number): Distribuicao {
    if (!distribuicoes.value[id]) distribuicoes.value[id] = { caracteristicas: {}, profissoes: {} }
    return distribuicoes.value[id]
  }

  async function carregar() {
    carregando.value = true
    erro.value = null
    try {
      const p = await get<Populacao>('/api/jogo/vila/populacao')
      populacao.value = p
      familiaLiderId.value = p.familiaLiderId
      for (const f of p.familias) for (const c of f.cidadaos) dist(c.id)
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar a população'
    } finally {
      carregando.value = false
    }
  }

  const errosPorCidadao = computed(() => {
    const r: Record<number, string[]> = {}
    for (const [id, d] of Object.entries(distribuicoes.value)) r[Number(id)] = validarDistribuicao(d)
    return r
  })

  const valido = computed(
    () => familiaLiderId.value !== null && Object.values(errosPorCidadao.value).every((e) => e.length === 0),
  )

  async function confirmar(): Promise<boolean> {
    if (!valido.value) {
      erro.value = familiaLiderId.value === null ? 'Escolha uma família líder' : 'Corrija os limites de pontos'
      return false
    }
    enviando.value = true
    erro.value = null
    try {
      await post('/api/jogo/vila/populacao', {
        familiaLiderId: familiaLiderId.value,
        cidadaos: Object.entries(distribuicoes.value).map(([id, d]) => ({
          cidadaoId: Number(id),
          caracteristicas: d.caracteristicas,
          profissoes: d.profissoes,
        })),
      })
      marcarPopulacaoConfirmada()
      return true
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao confirmar'
      return false
    } finally {
      enviando.value = false
    }
  }

  return {
    populacao, distribuicoes, familiaLiderId, carregando, enviando, erro, errosPorCidadao, valido,
    dist, carregar, confirmar,
  }
}
