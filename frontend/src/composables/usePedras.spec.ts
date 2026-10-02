import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { get, post } from '../api/http'
import { custoEngaste, rotuloMagnitude, usePedras } from './usePedras'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

describe('usePedras', () => {
  beforeEach(() => { getMock.mockReset(); postMock.mockReset() })

  it('helpers de custo e magnitude', () => {
    expect(custoEngaste('SIMPLES')).toBe(10)
    expect(custoEngaste('BOA')).toBe(25)
    expect(custoEngaste('EXCELENTE')).toBe(60)
    expect(custoEngaste('DIVINA')).toBe(150)
    expect(custoEngaste('X')).toBe(0)
    expect(rotuloMagnitude('MEDIA')).toBe('Média')
    expect(rotuloMagnitude('ALTA')).toBe('Alta')
    expect(rotuloMagnitude(null)).toBe('-')
  })

  it('chama os endpoints esperados', async () => {
    getMock.mockResolvedValue([])
    postMock.mockResolvedValue({})
    const p = usePedras()
    await p.listarPedras()
    await p.itensCompativeis()
    await p.engastar(3, 7)
    await p.removerPedra(7, 3)
    expect(getMock).toHaveBeenNthCalledWith(1, '/api/jogo/pedras')
    expect(getMock).toHaveBeenNthCalledWith(2, '/api/jogo/ferraria/itens-compativeis')
    expect(postMock).toHaveBeenNthCalledWith(1, '/api/jogo/ferraria/engaste', { pedraId: 3, itemId: 7 })
    expect(postMock).toHaveBeenNthCalledWith(2, '/api/jogo/ferraria/remover-pedra', { itemId: 7, pedraId: 3 })
  })

  it('propaga o erro do backend', async () => {
    postMock.mockRejectedValue(new Error('Ouro insuficiente'))
    await expect(usePedras().engastar(1, 2)).rejects.toThrow('Ouro insuficiente')
  })
})
