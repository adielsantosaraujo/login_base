import { beforeEach, describe, expect, it, vi } from 'vitest'

let falhar = false
vi.mock('../api/http', () => {
  const mock = vi.fn()
  return {
    get: (...args: unknown[]) => (falhar ? Promise.reject(new Error('falhou')) : mock(...args)),
    __mock: mock,
  }
})

import * as http from '../api/http'
import { rotulo, ROTULOS_JAZIDA, useMapaVila, useRegiaoDetalhes } from './useMapa'

const getMock = (http as unknown as { __mock: ReturnType<typeof vi.fn> }).__mock

describe('useMapa', () => {
  beforeEach(() => {
    falhar = false
    getMock.mockReset()
  })

  it('carrega o mapa da vila', async () => {
    getMock.mockResolvedValue({ vila: { id: 1, nome: 'V' }, regioes: [{ indice: 1 }] })
    const m = useMapaVila()
    await m.carregar()
    expect(getMock).toHaveBeenCalledWith('/api/jogo/vila/mapa')
    expect(m.regioes.value).toHaveLength(1)
  })

  it('expõe masmorraId nas regiões', async () => {
    getMock.mockResolvedValue({
      vila: { id: 1, nome: 'V' },
      regioes: [{ indice: 3, tipo: null, possuida: false, masmorraAtiva: true, nivelMasmorra: 4, masmorraId: 7 }],
    })
    const m = useMapaVila()
    await m.carregar()
    expect(m.regioes.value[0].masmorraId).toBe(7)
  })

  it('registra erro ao falhar', async () => {
    falhar = true
    const m = useMapaVila()
    await m.carregar()
    expect(m.erro.value).toBe('falhou')
  })

  it('carrega detalhes da região', async () => {
    getMock.mockResolvedValue({ regiao: { id: 1, indice: 6, tipo: 'URBANA', possuida: true }, ladrilhos: [] })
    const d = useRegiaoDetalhes()
    await d.carregar(6)
    expect(getMock).toHaveBeenCalledWith('/api/jogo/regioes/6')
    expect(d.regiaoSelecionada.value?.regiao.indice).toBe(6)
    d.limpar()
    expect(d.regiaoSelecionada.value).toBeNull()
  })

  it('mapeia rótulos', () => {
    expect(rotulo(ROTULOS_JAZIDA, 'VEIO_DE_FERRO')).toBe('Veio de ferro')
    expect(rotulo(ROTULOS_JAZIDA, 'X')).toBe('X')
  })
})
