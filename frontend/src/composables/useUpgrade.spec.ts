import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { get, post } from '../api/http'
import { custoDoNivel, poDoNivel, proximoNivel, useUpgrade } from './useUpgrade'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

describe('useUpgrade', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
  })

  it('calcula custos e PO por nível', () => {
    expect(custoDoNivel({ MADEIRA: 5, PEDRA: 3 }, 'N2')).toEqual({ MADEIRA: 13, PEDRA: 8 })
    expect(custoDoNivel({ MADEIRA: 5 }, 'N3')).toEqual({ MADEIRA: 25 })
    expect(poDoNivel(5, 'N2')).toBe(13)
    expect(proximoNivel('N3')).toBeNull()
    expect(proximoNivel('N1')).toBe('N2')
  })

  it('carrega prédio, catálogo e estoque e detecta falta de recurso', async () => {
    getMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/catalogo')) return [{ tipo: 'CASA', custoN1: { MADEIRA: 10 }, poN1: 4 }]
      if (url.endsWith('/estoque')) return { recursos: [{ recurso: 'MADEIRA', quantidade: 20 }] }
      return { id: 3, tipo: 'CASA', nivel: 'N1', x: 1, y: 1, tamanho: 1, estado: 'ATIVA' }
    })
    const u = useUpgrade()
    await u.carregar(3)
    expect(u.nivelNovo.value).toBe('N2')
    expect(u.custo.value).toEqual({ MADEIRA: 25 })
    expect(u.tamanhoNovo.value).toBe(2)
    expect(u.poNovo.value).toBe(10)
    expect(u.faltantes.value).toEqual(['MADEIRA'])
  })

  it('confirma com POST e expõe erro', async () => {
    postMock.mockResolvedValueOnce({ id: 3 })
    const u = useUpgrade()
    expect(await u.confirmar(3, { novoNivel: 'N2' })).toEqual({ id: 3 })
    expect(postMock).toHaveBeenCalledWith('/api/jogo/construcoes/3/upgrade', { novoNivel: 'N2' })
    postMock.mockRejectedValueOnce(new Error('Recursos insuficientes'))
    expect(await u.confirmar(3)).toBeNull()
    expect(u.erro.value).toBe('Recursos insuficientes')
  })
})
