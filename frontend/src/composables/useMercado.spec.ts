import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { get, post } from '../api/http'
import { useMercado } from './useMercado'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

describe('useMercado', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    getMock.mockImplementation(async (url: string) =>
      url.endsWith('/precos') ? { mercadoAtivo: true, precos: [] } : [],
    )
  })

  it('negocia e recarrega preços e histórico', async () => {
    postMock.mockResolvedValue({ sucesso: true, tipo: 'VENDA', recurso: 'MADEIRA', quantidade: 2, ouroTotal: 2 })
    const m = useMercado()
    expect(await m.negociar('MADEIRA', 'VENDA', 2)).toBe(true)
    expect(postMock).toHaveBeenCalledWith('/api/jogo/mercado/ordens', { recurso: 'MADEIRA', tipo: 'VENDA', quantidade: 2 })
    expect(getMock).toHaveBeenCalledTimes(2)
    expect(m.sucesso.value).toContain('Venda realizada')
  })

  it('expõe o erro da API', async () => {
    postMock.mockRejectedValue(new Error('Volume diário excedido'))
    const m = useMercado()
    expect(await m.negociar('MADEIRA', 'COMPRA', 99)).toBe(false)
    expect(m.erro.value).toBe('Volume diário excedido')
  })
})
