import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { get, post } from '../api/http'
import { resetarGuardaVila } from '../router/guardaVila'
import { adjacentes, useCriacaoVila } from './useVila'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

describe('useVila', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    resetarGuardaVila()
  })

  it('calcula adjacência ortogonal', () => {
    expect([2, 5, 7, 10].every((n) => adjacentes(6, n))).toBe(true)
    expect(adjacentes(4, 5)).toBe(false)
    expect(adjacentes(6, 11)).toBe(false)
  })

  it('recusa região não adjacente e mantém a seleção', () => {
    const c = useCriacaoVila()
    c.alternar(1)
    c.alternar(3)
    expect(c.selecao.value).toEqual([1])
    expect(c.avisoSelecao.value).toBeTruthy()
  })

  it('habilita criar com 3 regiões e ao menos 1 urbana', () => {
    const c = useCriacaoVila()
    c.alternar(1); c.alternar(2); c.alternar(6)
    expect(c.completo.value).toBe(false)
    c.definirTipo(1, 'URBANA')
    expect(c.completo.value).toBe(true)
  })

  it('envia a semente da prévia no POST', async () => {
    getMock.mockResolvedValue({ semente: 42, regioes: [] })
    postMock.mockResolvedValue({})
    const c = useCriacaoVila()
    await c.carregarPrevia()
    c.alternar(1); c.alternar(2); c.alternar(6)
    c.definirTipo(1, 'URBANA')
    expect(await c.criar()).toBe(true)
    expect(postMock).toHaveBeenCalledWith('/api/jogo/vila', {
      regioesEscolhidas: [1, 2, 6],
      tipos: { '1': 'URBANA', '2': 'RURAL', '6': 'RURAL' },
      semente: 42,
    })
  })

  it('expõe erro do backend', async () => {
    postMock.mockRejectedValue(new Error('Ao menos 1 região Urbana é obrigatória'))
    const c = useCriacaoVila()
    c.alternar(1); c.alternar(2); c.alternar(6)
    c.definirTipo(1, 'URBANA')
    expect(await c.criar()).toBe(false)
    expect(c.erro.value).toContain('Urbana')
  })
})
