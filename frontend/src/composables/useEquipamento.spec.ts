import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), put: vi.fn(), del: vi.fn() }))
import { del, get, put } from '../api/http'
import { categoriaDoSlot, subtipoDoSlot, useEquipamento } from './useEquipamento'

describe('useEquipamento', () => {
  beforeEach(() => { vi.mocked(get).mockReset(); vi.mocked(put).mockReset(); vi.mocked(del).mockReset() })

  it('mapeia slot para categoria e subtipo', () => {
    expect(categoriaDoSlot('ARMA')).toBe('ARMA')
    expect(categoriaDoSlot('CAPACETE')).toBe('ARMADURA')
    expect(categoriaDoSlot('ANEL_2')).toBe('JOIA')
    expect(subtipoDoSlot('ANEL_1')).toBe('ANEL')
    expect(subtipoDoSlot('LUVAS')).toBe('LUVAS')
    expect(subtipoDoSlot('ARMA')).toBeNull()
  })

  it('carrega inventário por categoria', async () => {
    vi.mocked(get).mockResolvedValue({ itens: [{ id: 1 }], total: 1, page: 0, pageSize: 100 })
    const e = useEquipamento()
    await e.carregarInventario('ARMA')
    expect(get).toHaveBeenCalledWith('/api/jogo/inventario?categoria=ARMA&page=0&size=100')
    expect(e.itens.value).toHaveLength(1)
  })

  it('equipa e remove', async () => {
    vi.mocked(put).mockResolvedValue({ ARMA: { id: 3 } })
    vi.mocked(del).mockResolvedValue({})
    const e = useEquipamento()
    expect(await e.equipar(7, 'ARMA', 3)).toEqual({ ARMA: { id: 3 } })
    expect(put).toHaveBeenCalledWith('/api/jogo/cidadao/7/equipamento/ARMA', { itemId: 3 })
    expect(await e.remover(7, 'ARMA')).toEqual({})
    expect(del).toHaveBeenCalledWith('/api/jogo/cidadao/7/equipamento/ARMA')
  })

  it('expõe erro do backend ao equipar', async () => {
    vi.mocked(put).mockRejectedValue(new Error('Máximo 2 anéis por pessoa'))
    const e = useEquipamento()
    expect(await e.equipar(7, 'ANEL', 3)).toBeNull()
    expect(e.erro.value).toBe('Máximo 2 anéis por pessoa')
  })
})
