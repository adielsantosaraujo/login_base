import type { NavigationGuard } from 'vue-router'
import { get } from '../api/http'

type Estado = 'desconhecido' | 'sem-vila' | 'pendente' | 'pronta'

let estado: Estado = 'desconhecido'

/** Chamado após criar a vila (população ainda não confirmada). */
export function marcarVilaCriada() {
  estado = 'pendente'
}

/** Chamado após confirmar a distribuição da população. */
export function marcarPopulacaoConfirmada() {
  estado = 'pronta'
}

/** Limpa o cache (útil em testes). */
export function resetarGuardaVila() {
  estado = 'desconhecido'
}

async function carregarEstado(): Promise<Estado | 'desconhecido'> {
  if (estado === 'pendente' || estado === 'pronta') return estado
  try {
    const vila = await get<{ populacaoConfirmada?: boolean }>('/api/jogo/vila')
    estado = vila.populacaoConfirmada === false ? 'pendente' : 'pronta'
  } catch (e) {
    if ((e as { status?: number }).status === 404) {
      estado = 'sem-vila'
    } else {
      // rede/5xx: não fica em cache; deixa navegar e reconsulta na próxima navegação
      estado = 'desconhecido'
      return 'desconhecido'
    }
  }
  return estado
}

export const guardaVila: NavigationGuard = async (to) => {
  if (!to.path.startsWith('/jogo')) return true
  const criar = to.path === '/jogo/criar-vila'
  const populacao = to.path === '/jogo/distribuir-populacao'
  const e = await carregarEstado()
  if (e === 'desconhecido') return true
  if (e === 'sem-vila') return criar ? true : '/jogo/criar-vila'
  if (e === 'pendente') return populacao ? true : '/jogo/distribuir-populacao'
  if (criar) return '/jogo/mapa'
  return true
}
