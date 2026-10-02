import { describe, expect, it } from 'vitest'
import { ehAnexavel } from './useAnexacao'

const base = [
  { indice: 1, possuida: true },
  { indice: 2, possuida: false },
  { indice: 3, possuida: false, masmorraAtiva: true },
  { indice: 5, possuida: false },
]

describe('ehAnexavel', () => {
  it('aceita região adjacente a possuída', () => {
    expect(ehAnexavel(2, base)).toBe(true)
  })
  it('exclui região com masmorra ativa', () => {
    const r = base.map((x) => (x.indice === 2 ? { ...x, masmorraAtiva: true } : x))
    expect(ehAnexavel(2, r)).toBe(false)
  })
  it('exclui região não adjacente e já possuída', () => {
    expect(ehAnexavel(3, base)).toBe(false)
    expect(ehAnexavel(1, base)).toBe(false)
  })
})
