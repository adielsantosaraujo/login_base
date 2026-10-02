import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn() }))

import { get } from '../api/http'
import { useBatalhas } from './useBatalhas'

const getMock = vi.mocked(get)

describe('useBatalhas', () => {
  beforeEach(() => { getMock.mockReset() })

  it('lista batalhas', async () => {
    getMock.mockResolvedValue([{ id: 1 }, { id: 2 }])
    const b = useBatalhas()
    await b.listar()
    expect(getMock).toHaveBeenCalledWith('/api/jogo/batalhas')
    expect(b.batalhas.value).toHaveLength(2)
    expect(b.erro.value).toBeNull()
  })

  it('busca o detalhe', async () => {
    getMock.mockResolvedValue({ id: 5, totalRodadas: 2, rodadas: [] })
    const b = useBatalhas()
    await b.buscar(5)
    expect(getMock).toHaveBeenCalledWith('/api/jogo/batalhas/5')
    expect(b.batalha.value?.totalRodadas).toBe(2)
  })

  it('guarda o erro da API', async () => {
    getMock.mockRejectedValue(new Error('Batalha não encontrada'))
    const b = useBatalhas()
    await b.buscar(9)
    expect(b.erro.value).toBe('Batalha não encontrada')
    expect(b.batalha.value).toBeNull()
    await b.listar()
    expect(b.erro.value).toBe('Batalha não encontrada')
  })
})
