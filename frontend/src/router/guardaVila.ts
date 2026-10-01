import type { NavigationGuard } from 'vue-router'
import { get } from '../api/http'

let temVila = false

/** Chamado após criar a vila, para evitar nova consulta ao backend. */
export function marcarVilaCriada() {
  temVila = true
}

/** Limpa o cache (útil em testes). */
export function resetarGuardaVila() {
  temVila = false
}

async function usuarioTemVila(): Promise<boolean> {
  if (temVila) return true
  try {
    await get('/api/jogo/vila')
    temVila = true
  } catch {
    // 404 (sem vila) ou falha: tratado como sem vila
    temVila = false
  }
  return temVila
}

export const guardaVila: NavigationGuard = async (to) => {
  if (!to.path.startsWith('/jogo')) return true
  const criar = to.path === '/jogo/criar-vila'
  const possui = await usuarioTemVila()
  if (!possui && !criar) return '/jogo/criar-vila'
  if (possui && criar) return '/jogo/mapa'
  return true
}
