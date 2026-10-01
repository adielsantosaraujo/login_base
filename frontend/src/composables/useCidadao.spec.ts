import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { get, post } from '../api/http'
import { somaPontos, useCidadao } from './useCidadao'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

describe('useCidadao', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
  })

  it('carrega o cidadão', async () => {
    getMock.mockResolvedValue({ id: 3, nome: 'Ana' } as never)
    const c = useCidadao()
    await c.carregar(3)
    expect(getMock).toHaveBeenCalledWith('/api/jogo/cidadao/3')
    expect(c.cidadao.value?.nome).toBe('Ana')
  })

  it('expõe erro ao carregar', async () => {
    getMock.mockRejectedValue(new Error('Cidadão não encontrado'))
    const c = useCidadao()
    await c.carregar(9)
    expect(c.erro.value).toBe('Cidadão não encontrado')
    expect(c.cidadao.value).toBeNull()
  })

  it('distribui pontos e atualiza com a resposta', async () => {
    postMock.mockResolvedValue({ id: 3, pontosCarPendentes: 1 } as never)
    const c = useCidadao()
    expect(await c.distribuir(3, { caracteristicas: { VIT: 2 } })).toBe(true)
    expect(postMock).toHaveBeenCalledWith('/api/jogo/cidadao/3/distribuir-pontos', { caracteristicas: { VIT: 2 } })
    expect(c.cidadao.value?.pontosCarPendentes).toBe(1)
  })

  it('expõe {erro} ao distribuir', async () => {
    postMock.mockRejectedValue(new Error('Pontos acima do pendente'))
    const c = useCidadao()
    expect(await c.distribuir(3, {})).toBe(false)
    expect(c.erro.value).toContain('pendente')
  })

  it('soma pontos', () => {
    expect(somaPontos({ A: 1, B: 2 })).toBe(3)
  })
})
