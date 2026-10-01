import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn() }))

import { get } from '../api/http'
import { guardaVila, resetarGuardaVila } from './guardaVila'

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
})
