import { ref } from 'vue'
import { get, post } from '../api/http'

export interface ProfissaoCidadao {
  profissao: string
  peBase: number
  peEfetivo: number
  eficiencia: number
}

export interface CidadaoDetalhe {
  id: number
  nome: string
  sexo: 'M' | 'F'
  idadeAnos: number
  vivo: boolean
  estado: 'SAUDAVEL' | 'FERIDO'
  faminto: boolean
  familiaId: number | null
  familiaNome: string | null
  conjuge: { id: number; nome: string } | null
  caracteristicas: Record<string, number>
  pontosCarPendentes: number
  pontosProfPendentes: number
  profissoes: ProfissaoCidadao[]
  construcaoId: number | null
  profissaoTrabalho: string | null
  equipamento: Record<string, unknown>
}

export interface DistribuicaoPontos {
  caracteristicas?: Record<string, number>
  profissoes?: Record<string, number>
}

export function somaPontos(m: Record<string, number>): number {
  return Object.values(m).reduce((a, b) => a + (b || 0), 0)
}

export function useCidadao() {
  const cidadao = ref<CidadaoDetalhe | null>(null)
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)

  async function carregar(id: number | string) {
    carregando.value = true
    erro.value = null
    try {
      cidadao.value = await get<CidadaoDetalhe>(`/api/jogo/cidadao/${id}`)
    } catch (e) {
      cidadao.value = null
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar cidadão'
    } finally {
      carregando.value = false
    }
  }

  /** Retorna true em sucesso (cidadão atualizado com a resposta); em erro, mensagem em `erro`. */
  async function distribuir(id: number | string, dist: DistribuicaoPontos): Promise<boolean> {
    enviando.value = true
    erro.value = null
    try {
      cidadao.value = await post<CidadaoDetalhe>(`/api/jogo/cidadao/${id}/distribuir-pontos`, dist)
      return true
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao distribuir pontos'
      return false
    } finally {
      enviando.value = false
    }
  }

  return { cidadao, carregando, enviando, erro, carregar, distribuir }
}
