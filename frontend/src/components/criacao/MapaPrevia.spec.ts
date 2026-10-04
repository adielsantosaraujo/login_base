import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import MapaPrevia from './MapaPrevia.vue'
import type { RegiaoPrevia } from '../../domain/regioes'

const regioes: RegiaoPrevia[] = Array.from({ length: 16 }, (_, k) => ({
  indice: k + 1,
  tipo: 'URBANA' as const,
  terrenos: [{ terreno: 'COMERCIO' as const, posicao: 1, percentual: 100 }],
}))

describe('MapaPrevia', () => {
  it('renderiza 16 tiles e a legenda com 5 tipos', () => {
    const w = mount(MapaPrevia, { props: { regioes, selecionadas: [] } })
    expect(w.findAll('button.rt-tile')).toHaveLength(16)
    expect(w.findAll('.mp-legenda li')).toHaveLength(5)
  })

  it('com a região 01 selecionada, 02 e 05 estão disponíveis e 03 esmaecida', async () => {
    const w = mount(MapaPrevia, { props: { regioes, selecionadas: [1] } })
    const tile = (i: number) => w.get(`[data-testid="regiao-${i}"]`)
    expect(tile(2).attributes('aria-disabled')).toBeUndefined()
    expect(tile(5).attributes('aria-disabled')).toBeUndefined()
    expect(tile(3).attributes('aria-disabled')).toBe('true')
    await tile(3).trigger('click')
    expect(w.emitted('alternar')).toBeUndefined()
    await tile(2).trigger('click')
    expect(w.emitted('alternar')).toEqual([[2]])
  })
})
