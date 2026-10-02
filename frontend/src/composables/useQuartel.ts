import { ref } from 'vue'
import { del, get, post } from '../api/http'

export type PosicaoTropa = 'FRENTE' | 'RETAGUARDA'
export type EstadoTropa = 'AQUARTELADA' | 'EM_VIAGEM_IDA' | 'EM_VIAGEM_VOLTA'

export interface MembroTropaDTO {
  cidadaoId: number
  nome: string
  posicao: PosicaoTropa
  idadeAnos: number
  estado: 'SAUDAVEL' | 'FERIDO' | string
  xpGuerreiro: number
  peGuerreiro: number
}

export interface TropaDTO {
  id: number
  nome: string
  estado: EstadoTropa
  quartelId: number
  masmorraId: number | null
  regiaoDestino: number | null
  turnosViagem: number | null
  turnosRestantes: number | null
  totalMembros: number
  membros: MembroTropaDTO[]
}

export interface QuartelDTO {
  id: number
  nivel: string
  estado: string
  instrutores: number
  vagasInstrutor: number
  capacidade: number
  membrosAtuais: number
  maxTropas: number
  tropas: TropaDTO[]
}

export interface GuerreiroDisponivelDTO {
  id: number
  nome: string
  idadeAnos: number
  peGuerreiro: number
  arma: string | null
  estado: string
  elegivel: boolean
  motivo: string | null
}

export interface MembroNovo {
  cidadaoId: number
  posicao: PosicaoTropa
}

export interface NovaTropa {
  nome: string
  membros: MembroNovo[]
}

export interface DestinoDTO {
  masmorraId: number
  regiao: number
  nivel: number
  turnosViagem: number
  comidaNecessaria: number
  comidaDisponivel: number
}

export const ROTULOS_ESTADO_TROPA: Record<string, string> = {
  AQUARTELADA: 'Aquartelada',
  EM_VIAGEM_IDA: 'Em viagem (ida)',
  EM_VIAGEM_VOLTA: 'Em viagem (volta)',
}

export const ROTULOS_POSICAO: Record<PosicaoTropa, string> = { FRENTE: 'Frente', RETAGUARDA: 'Retaguarda' }

export function formatarXp(v: number): string {
  return Number(v).toFixed(2).replace('.', ',')
}

export function useQuartel() {
  const quartel = ref<QuartelDTO | null>(null)
  const disponiveis = ref<GuerreiroDisponivelDTO[]>([])
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)

  function msg(e: unknown, padrao: string) {
    return e instanceof Error ? e.message : padrao
  }

  async function recarregar(id: number) {
    const [q, d] = await Promise.all([
      get<QuartelDTO>(`/api/jogo/quarteis/${id}`),
      get<GuerreiroDisponivelDTO[]>(`/api/jogo/quarteis/${id}/guerreiros-disponiveis`),
    ])
    quartel.value = q
    disponiveis.value = d
  }

  async function carregar(id: number) {
    carregando.value = true
    erro.value = null
    try {
      await recarregar(id)
    } catch (e) {
      erro.value = msg(e, 'Erro ao carregar o quartel')
    } finally {
      carregando.value = false
    }
  }

  async function executar<T>(quartelId: number, padrao: string, acao: () => Promise<T>): Promise<T | null> {
    enviando.value = true
    erro.value = null
    try {
      const r = await acao()
      await recarregar(quartelId)
      return r
    } catch (e) {
      erro.value = msg(e, padrao)
      return null
    } finally {
      enviando.value = false
    }
  }

  /** Retorna a tropa criada ou null em caso de erro (mensagem em `erro`). */
  function formarTropa(quartelId: number, req: NovaTropa): Promise<TropaDTO | null> {
    return executar(quartelId, 'Erro ao formar a tropa', () =>
      post<TropaDTO>(`/api/jogo/quarteis/${quartelId}/tropas`, req))
  }

  function buscarTropa(tropaId: number): Promise<TropaDTO> {
    return get<TropaDTO>(`/api/jogo/tropas/${tropaId}`)
  }

  function adicionarMembro(quartelId: number, tropaId: number, membro: MembroNovo): Promise<TropaDTO | null> {
    return executar(quartelId, 'Erro ao adicionar o membro', () =>
      post<TropaDTO>(`/api/jogo/tropas/${tropaId}/membros`, membro))
  }

  function removerMembro(quartelId: number, tropaId: number, cidadaoId: number): Promise<TropaDTO | null> {
    return executar(quartelId, 'Erro ao remover o membro', () =>
      del<TropaDTO>(`/api/jogo/tropas/${tropaId}/membros/${cidadaoId}`))
  }

  /** Retorna true se a tropa foi desfeita. */
  async function desfazerTropa(quartelId: number, tropaId: number): Promise<boolean> {
    const r = await executar(quartelId, 'Erro ao desfazer a tropa', async () => {
      await del(`/api/jogo/tropas/${tropaId}`)
      return true
    })
    return r === true
  }

  /** Lista os destinos da tropa; retorna null e preenche `erro` se a API recusar. */
  async function buscarDestinos(tropaId: number): Promise<DestinoDTO[] | null> {
    erro.value = null
    try {
      return await get<DestinoDTO[]>(`/api/jogo/tropas/${tropaId}/destinos`)
    } catch (e) {
      erro.value = msg(e, 'Erro ao carregar os destinos')
      return null
    }
  }

  /** Retorna a tropa em viagem ou null em caso de erro (mensagem em `erro`). */
  function enviarExpedicao(quartelId: number, tropaId: number, masmorraId: number): Promise<TropaDTO | null> {
    return executar(quartelId, 'Erro ao enviar a expedição', () =>
      post<TropaDTO>(`/api/jogo/tropas/${tropaId}/expedicao`, { masmorraId }))
  }

  return {
    buscarDestinos, enviarExpedicao,
    quartel, disponiveis, carregando, enviando, erro,
    carregar, recarregar, formarTropa, buscarTropa, adicionarMembro, removerMembro, desfazerTropa,
  }
}
