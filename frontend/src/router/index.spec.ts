import { describe, expect, it } from 'vitest'
import router from './index'

describe('router', () => {
  it.each(['/', '/index'])('%s resolve para a home', (caminho) => {
    expect(router.resolve(caminho).name).toBe('home')
  })

  it('/jogo resolve para o layout do jogo e redireciona para o mapa', () => {
    const rota = router.resolve('/jogo')
    expect(rota.matched.length).toBeGreaterThan(0)
    expect(rota.matched[0].redirect).toBe('/jogo/mapa')
    expect(router.resolve('/jogo/mapa').name).toBe('mapa')
  })
})
