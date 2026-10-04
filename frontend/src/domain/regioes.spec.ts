import { describe, expect, it } from 'vitest'
import {
  adjacentes,
  conectado,
  dicaSelecao,
  podeSelecionar,
  selecaoValida,
  temUrbana,
  type RegiaoPrevia,
} from './regioes'

const regioes: RegiaoPrevia[] = [
  {
    indice: 6,
    tipo: 'URBANA',
    terrenos: [
      { terreno: 'INDUSTRIA', posicao: 1, percentual: 45 },
      { terreno: 'COMERCIO', posicao: 2, percentual: 30 },
      { terreno: 'DESENVOLVIMENTO', posicao: 3, percentual: 25 },
    ],
  },
  {
    indice: 7,
    tipo: 'LITORAL',
    terrenos: [
      { terreno: 'SALINAS', posicao: 1, percentual: 40 },
      { terreno: 'MILITAR', posicao: 2, percentual: 35 },
      { terreno: 'ENXOFRE', posicao: 3, percentual: 25 },
    ],
  },
  {
    indice: 10,
    tipo: 'PLANICIE',
    terrenos: [
      { terreno: 'CRIACOES', posicao: 1, percentual: 40 },
      { terreno: 'FLORESTA', posicao: 2, percentual: 35 },
      { terreno: 'PLANTACOES', posicao: 3, percentual: 25 },
    ],
  },
  { indice: 11, tipo: 'MONTANHA', terrenos: [] },
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

  it('dicaSelecao cobre os 5 casos', () => {
    expect(dicaSelecao([], regioes)).toBe('Clique em uma região para começar.')
    expect(dicaSelecao([6], regioes)).toBe('Regiões destacadas são vizinhas da sua seleção.')
    expect(dicaSelecao([6, 7, 10], regioes)).toBe('Tudo certo — crie sua vila.')
    expect(dicaSelecao([7, 11, 10], regioes)).toBe('Inclua ao menos uma região Urbana.')
    expect(dicaSelecao([6, 7, 12], regioes)).toBe('As regiões precisam ser vizinhas.')
  })
})
