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
import { rotulo, ROTULOS_CONSTRUCAO, ROTULOS_JAZIDA, ROTULOS_TIPO, useMapaVila, useRegiaoDetalhes } from './useMapa'

const getMock = (http as unknown as { __mock: ReturnType<typeof vi.fn> }).__mock

describe('useMapa', () => {
  beforeEach(() => {
    falhar = false
    getMock.mockReset()
  })

  it('carrega o mapa da vila', async () => {
    getMock.mockResolvedValue({ vila: { id: 1, nome: 'V', bonusRegiao: { ROCHA: 40 } }, regioes: [{ indice: 1, tipo: 'MONTANHA', bonus: [{ bonus: 'ROCHA', posicao: 1, valor: 40 }] }] })
    const m = useMapaVila()
    await m.carregar()
    expect(getMock).toHaveBeenCalledWith('/api/jogo/vila/mapa')
    expect(m.regioes.value).toHaveLength(1)
    expect(m.regioes.value[0].bonus[0].bonus).toBe('ROCHA')
    expect(m.mapa.value?.vila.bonusRegiao.ROCHA).toBe(40)
  })

  it('expõe masmorraId nas regiões', async () => {
    getMock.mockResolvedValue({
      vila: { id: 1, nome: 'V' },
      regioes: [{ indice: 3, tipo: 'LITORAL', bonus: [], possuida: false, masmorraAtiva: true, nivelMasmorra: 4, masmorraId: 7 }],
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
    getMock.mockResolvedValue({ regiao: { id: 1, indice: 6, tipo: 'URBANA', possuida: true, bonus: [] }, ladrilhos: [] })
    const d = useRegiaoDetalhes()
    await d.carregar(6)
    expect(getMock).toHaveBeenCalledWith('/api/jogo/regioes/6')
    expect(d.regiaoSelecionada.value?.regiao.indice).toBe(6)
    d.limpar()
    expect(d.regiaoSelecionada.value).toBeNull()
  })

  it('ROTULOS_TIPO mescla tipos v2 e construções', () => {
    expect(ROTULOS_TIPO.PLANICIE).toBe('Planície')
    expect(ROTULOS_TIPO.MONTANHA).toBe('Montanha')
    expect(ROTULOS_TIPO.SERRARIA).toBe(ROTULOS_CONSTRUCAO.SERRARIA)
  })

  it('mapeia rótulos', () => {
    expect(rotulo(ROTULOS_JAZIDA, 'VEIO_DE_FERRO')).toBe('Veio de ferro')
    expect(rotulo(ROTULOS_JAZIDA, 'X')).toBe('X')
  })
})
