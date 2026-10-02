import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn(), patch: vi.fn() }))

import { get, patch, post } from '../api/http'
import { custoDoNivelItem, ehOficina, motivoArtesaoIndisponivel, percentualPf, peMinimoDoNivel, useOficina } from './useOficina'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)
const patchMock = vi.mocked(patch)

const receita = {
  subtipo: 'ESPADA', receitaBase: { FERRO: 5 },
  custoPorNivel: { '1': { FERRO: 5 }, '2': { FERRO: 8 } }, peMinimoPorNivel: { '1': 0, '2': 2 }, exigeAtributo: false,
}

describe('useOficina', () => {
  beforeEach(() => { getMock.mockReset(); postMock.mockReset(); patchMock.mockReset() })

  it('helpers', () => {
    expect(ehOficina('FERRARIA')).toBe(true)
    expect(ehOficina('CASA')).toBe(false)
    expect(percentualPf({ pfAtual: 5, pfTotal: 20 })).toBe(25)
    expect(percentualPf({ pfAtual: 0, pfTotal: 0 })).toBe(0)
    expect(peMinimoDoNivel(receita, 2)).toBe(2)
    expect(custoDoNivelItem(receita, 2)).toEqual({ FERRO: 8 })
  })

  it('desabilita artesão ocupado ou com PE insuficiente', () => {
    const a = { cidadaoId: 1, nome: 'A', peEfetivo: 3, eficiencia: 1, ocupado: false }
    expect(motivoArtesaoIndisponivel(a, 2)).toBeNull()
    expect(motivoArtesaoIndisponivel({ ...a, ocupado: true }, 0)).toBe('ocupado')
    expect(motivoArtesaoIndisponivel({ ...a, peEfetivo: 1 }, 2)).toContain('PE insuficiente')
  })

  it('carrega e fabrica', async () => {
    getMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/fila')) return []
      if (url.endsWith('/receitas')) return [receita]
      return { id: 4, tipo: 'FERRARIA', nivel: 'N1', estado: 'ATIVA', nivelMaximoItem: 3, artesaos: [] }
    })
    postMock.mockResolvedValue({ id: 1 })
    const o = useOficina()
    await o.carregar(4)
    expect(o.oficina.value?.nivelMaximoItem).toBe(3)
    expect(o.receitas.value).toHaveLength(1)
    expect(await o.fabricar(4, { subtipo: 'ESPADA', nivel: 1, artesaoId: 2 })).toEqual({ id: 1 })
    expect(postMock).toHaveBeenCalledWith('/api/jogo/oficinas/4/fabricacoes', { subtipo: 'ESPADA', nivel: 1, artesaoId: 2 })
  })

  it('expõe erro da API e reatribui', async () => {
    postMock.mockRejectedValue(new Error('Recursos insuficientes'))
    const o = useOficina()
    expect(await o.fabricar(4, { subtipo: 'ESPADA', nivel: 1, artesaoId: 2 })).toBeNull()
    expect(o.erro.value).toBe('Recursos insuficientes')
    getMock.mockResolvedValue([])
    patchMock.mockResolvedValue({})
    expect(await o.reatribuir(4, 9, 3)).toBe(true)
    expect(patchMock).toHaveBeenCalledWith('/api/jogo/fabricacoes/9', { artesaoId: 3 })
  })
})
