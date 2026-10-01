import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn() }))

import { get } from '../api/http'
import { guardaVila, marcarPopulacaoConfirmada, resetarGuardaVila } from './guardaVila'

const getMock = vi.mocked(get)

function ir(path: string) {
  return (guardaVila as unknown as (to: { path: string }) => Promise<unknown>)({ path })
}

describe('guardaVila', () => {
  beforeEach(() => {
    getMock.mockReset()
    resetarGuardaVila()
  })

  it('deixa passar rotas fora de /jogo sem consultar', async () => {
    expect(await ir('/')).toBe(true)
    expect(getMock).not.toHaveBeenCalled()
  })

  it('sem vila redireciona rotas do jogo para criar-vila', async () => {
    getMock.mockRejectedValue(new Error('Vila não encontrada'))
    expect(await ir('/jogo/mapa')).toBe('/jogo/criar-vila')
  })

  it('sem vila permite criar-vila', async () => {
    getMock.mockRejectedValue(new Error('Vila não encontrada'))
    expect(await ir('/jogo/criar-vila')).toBe(true)
  })

  it('com vila permite o mapa e redireciona criar-vila para o mapa', async () => {
    getMock.mockResolvedValue({ vilaId: 1 })
    expect(await ir('/jogo/mapa')).toBe(true)
    expect(await ir('/jogo/criar-vila')).toBe('/jogo/mapa')
  })

  it('população não confirmada leva qualquer rota do jogo para /jogo/populacao', async () => {
    getMock.mockResolvedValue({ vilaId: 1, populacaoConfirmada: false })
    expect(await ir('/jogo/mapa')).toBe('/jogo/populacao')
    expect(await ir('/jogo/criar-vila')).toBe('/jogo/populacao')
    expect(await ir('/jogo/populacao')).toBe(true)
  })

  it('população confirmada libera o mapa e após confirmar o cache é atualizado', async () => {
    getMock.mockResolvedValue({ vilaId: 1, populacaoConfirmada: false })
    expect(await ir('/jogo/mapa')).toBe('/jogo/populacao')
    marcarPopulacaoConfirmada()
    expect(await ir('/jogo/mapa')).toBe(true)
  })
})
