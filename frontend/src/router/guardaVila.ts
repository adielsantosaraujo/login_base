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

async function carregarEstado(): Promise<Estado> {
  if (estado === 'pendente' || estado === 'pronta') return estado
  try {
    const vila = await get<{ populacaoConfirmada?: boolean }>('/api/jogo/vila')
    estado = vila.populacaoConfirmada === false ? 'pendente' : 'pronta'
  } catch {
    // 404 (sem vila) ou falha: tratado como sem vila
    estado = 'sem-vila'
  }
  return estado
}

export const guardaVila: NavigationGuard = async (to) => {
  if (!to.path.startsWith('/jogo')) return true
  const criar = to.path === '/jogo/criar-vila'
  const populacao = to.path === '/jogo/populacao'
  const e = await carregarEstado()
  if (e === 'sem-vila') return criar ? true : '/jogo/criar-vila'
  if (e === 'pendente') return populacao ? true : '/jogo/populacao'
  if (criar) return '/jogo/mapa'
  return true
}
