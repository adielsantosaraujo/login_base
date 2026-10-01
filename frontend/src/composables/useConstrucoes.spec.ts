import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { get, post } from '../api/http'
import { permitidoNaRegiao, useConstrucoes } from './useConstrucoes'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

describe('useConstrucoes', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
  })

  it('carrega o catálogo', async () => {
    getMock.mockResolvedValue([{ tipo: 'CASA' }])
    const c = useConstrucoes()
    await c.carregarCatalogo()
    expect(getMock).toHaveBeenCalledWith('/api/jogo/construcoes/catalogo')
    expect(c.catalogo.value).toHaveLength(1)
  })

  it('cria construção com POST', async () => {
    postMock.mockResolvedValue({ id: 9 })
    const c = useConstrucoes()
    const r = await c.criar({ tipo: 'CASA', regiaoIndice: 6, x: 1, y: 2 })
    expect(postMock).toHaveBeenCalledWith('/api/jogo/construcoes', { tipo: 'CASA', regiaoIndice: 6, x: 1, y: 2 })
    expect(r).toEqual({ id: 9 })
    expect(c.erro.value).toBeNull()
  })

  it('expõe mensagem de erro em 400/409', async () => {
    postMock.mockRejectedValue(new Error('Recursos insuficientes'))
    const c = useConstrucoes()
    const r = await c.criar({ tipo: 'CASA', regiaoIndice: 6, x: 1, y: 2 })
    expect(r).toBeNull()
    expect(c.erro.value).toBe('Recursos insuficientes')
    expect(c.criando.value).toBe(false)
  })

  it('verifica região permitida', () => {
    const item = { regiao: 'URBANA' } as never
    expect(permitidoNaRegiao(item, 'URBANA')).toBe(true)
    expect(permitidoNaRegiao(item, 'RURAL')).toBe(false)
  })
})
