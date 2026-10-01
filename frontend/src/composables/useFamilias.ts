import { ref } from 'vue'
import { get, post } from '../api/http'

export interface MembroFamilia {
  id: number
  nome: string
  sexo: 'M' | 'F'
  idadeAnos: number
  estadoCivil: 'SOLTEIRO' | 'CASADO'
  conjugeId: number | null
}

export interface Familia {
  id: number
  vilaId: number
  sobrenome: string
  casaId: number | null
  membros: MembroFamilia[]
}

export interface CasaOcupacao {
  casaId: number
  nivel: number
  regiaoIndice: number
  x: number
  y: number
  nucleosTotal: number
  nucleosOcupados: number
  nucleosLivres: number
  vagasTotal: number
  vagasOcupadas: number
  vagasLivres: number
  familiaIds: number[]
}

export interface CasamentoRequest {
  cidadao1Id: number
  cidadao2Id: number
  casaId: number
  sobrenomeEscolhido: string
}

export const IDADE_MINIMA_CASAMENTO = 18

export function elegivelParaCasamento(m: MembroFamilia): boolean {
  return m.estadoCivil === 'SOLTEIRO' && m.idadeAnos >= IDADE_MINIMA_CASAMENTO
}

export function useFamilias() {
  const familias = ref<Familia[]>([])
  const casas = ref<CasaOcupacao[]>([])
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)

  async function carregar() {
    carregando.value = true
    erro.value = null
    try {
      const [f, c] = await Promise.all([
        get<Familia[]>('/api/jogo/familias'),
        get<CasaOcupacao[]>('/api/jogo/casas'),
      ])
      familias.value = f
      casas.value = c
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar famílias'
    } finally {
      carregando.value = false
    }
  }

  /** Retorna a nova família ou null em caso de erro (mensagem em `erro`). */
  async function casar(req: CasamentoRequest): Promise<Familia | null> {
    enviando.value = true
    erro.value = null
    try {
      return await post<Familia>('/api/jogo/casamento', req)
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao casar'
      return null
    } finally {
      enviando.value = false
    }
  }

  return { familias, casas, carregando, enviando, erro, carregar, casar }
}
