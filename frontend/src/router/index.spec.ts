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

  it('/jogo/populacao redireciona para /jogo/distribuir-populacao', () => {
    const rota = router.resolve('/jogo/populacao')
    expect(rota.matched[rota.matched.length - 1].redirect).toBe('/jogo/distribuir-populacao')
    expect(router.resolve('/jogo/distribuir-populacao').name).toBe('distribuir-populacao')
  })

  it('meta etapaInicial e abaAtiva nas rotas do jogo', () => {
    expect(router.resolve('/jogo/criar-vila').meta).toMatchObject({ etapaInicial: true, abaAtiva: 'mapa' })
    expect(router.resolve('/jogo/distribuir-populacao').meta).toMatchObject({
      etapaInicial: true,
      abaAtiva: 'familias',
    })
    const filhas = router.getRoutes().filter((r) => r.path.startsWith('/jogo/') && r.name)
    for (const r of filhas) expect(r.meta.abaAtiva, String(r.name)).toBeTruthy()
    expect(router.resolve('/jogo/cidadao/1').meta.abaAtiva).toBe('familias')
    expect(router.resolve('/jogo/oficina/1').meta.abaAtiva).toBe('inventario')
  })
})
