import { describe, expect, it } from 'vitest'
import {
  adjacentes,
  bonusOrdenados,
  conectado,
  dicaSelecao,
  podeSelecionar,
  selecaoValida,
  temUrbana,
  totaisBonus,
  type RegiaoPrevia,
} from './regioes'

const regioes: RegiaoPrevia[] = [
  {
    indice: 6,
    tipo: 'URBANA',
    bonus: [
      { bonus: 'INDUSTRIA', posicao: 2, valor: 30 },
      { bonus: 'COMERCIO', posicao: 1, valor: 47 },
      { bonus: 'DESENVOLVIMENTO', posicao: 3, valor: 12 },
    ],
  },
  {
    indice: 7,
    tipo: 'LITORAL',
    bonus: [
      { bonus: 'SALINAS', posicao: 1, valor: 38 },
      { bonus: 'MILITAR', posicao: 2, valor: 20 },
      { bonus: 'ENXOFRE', posicao: 3, valor: 6 },
    ],
  },
  {
    indice: 10,
    tipo: 'PLANICIE',
    bonus: [
      { bonus: 'CRIACOES', posicao: 1, valor: 41 },
      { bonus: 'FLORESTA', posicao: 2, valor: 25 },
      { bonus: 'PLANTACOES', posicao: 3, valor: 14 },
    ],
  },
  { indice: 11, tipo: 'MONTANHA', bonus: [] },
]

describe('regioes', () => {
  it('adjacentes é ortogonal', () => {
    expect(adjacentes(1, 2)).toBe(true)
    expect(adjacentes(4, 5)).toBe(false)
    expect(adjacentes(1, 6)).toBe(false)
    expect(adjacentes(2, 6)).toBe(true)
  })

  it('podeSelecionar', () => {
    expect(podeSelecionar([], 9)).toBe(true)
    expect(podeSelecionar([1], 3)).toBe(false)
    expect(podeSelecionar([1], 2)).toBe(true)
    expect(podeSelecionar([1, 2, 3], 4)).toBe(false)
  })

  it('conectado', () => {
    expect(conectado([1, 3])).toBe(false)
    expect(conectado([1, 3, 2])).toBe(true)
  })

  it('temUrbana e selecaoValida', () => {
    expect(temUrbana([6, 7, 10], regioes)).toBe(true)
    expect(temUrbana([7, 11], regioes)).toBe(false)
    expect(selecaoValida([6, 7, 10], regioes)).toBe(true)
    expect(selecaoValida([6, 7], regioes)).toBe(false)
    expect(selecaoValida([7, 10, 11], regioes)).toBe(false)
  })

  it('totaisBonus soma e zera ausentes', () => {
    const t = totaisBonus([6, 7, 10], regioes)
    expect(t.COMERCIO).toBe(47)
    expect(t.INDUSTRIA).toBe(30)
    expect(t.DESENVOLVIMENTO).toBe(12)
    expect(t.SALINAS).toBe(38)
    expect(t.MILITAR).toBe(20)
    expect(t.ENXOFRE).toBe(6)
    expect(t.CRIACOES).toBe(41)
    expect(t.FLORESTA).toBe(25)
    expect(t.PLANTACOES).toBe(14)
    for (const b of ['BARREIRO', 'ROCHA', 'FERRO', 'CARVAO'] as const) expect(t[b]).toBe(0)
    expect(Object.keys(t)).toHaveLength(13)
  })

  it('bonusOrdenados em ordem decrescente', () => {
    expect(bonusOrdenados(regioes[0].bonus).map((b) => b.valor)).toEqual([47, 30, 12])
  })

  it('dicaSelecao cobre os 5 casos', () => {
    expect(dicaSelecao([], regioes)).toBe('Clique em uma região para começar.')
    expect(dicaSelecao([6], regioes)).toBe('Regiões destacadas são vizinhas da sua seleção.')
    expect(dicaSelecao([6, 7, 10], regioes)).toBe('Tudo certo — crie sua vila.')
    expect(dicaSelecao([7, 11, 10], regioes)).toBe('Inclua ao menos uma região Urbana.')
    expect(dicaSelecao([6, 7, 12], regioes)).toBe('As regiões precisam ser vizinhas.')
  })
})
