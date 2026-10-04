import { describe, expect, it, vi } from 'vitest'
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

describe('anexarRegiao', () => {
  it('faz POST sem corpo', async () => {
    const http = await import('../api/http')
    const spy = vi.spyOn(http, 'post').mockResolvedValue({ regiao: { indice: 2, tipo: 'FLORESTA', possuida: true }, estoque: {}, custo: { ouro: 0, madeira: 0, pedra: 0 } })
    const { useAnexacao } = await import('./useAnexacao')
    const r = await useAnexacao().anexarRegiao(2)
    expect(spy).toHaveBeenCalledWith('/api/jogo/regioes/2/anexar')
    expect(r).not.toBeNull()
    spy.mockRestore()
  })
})
