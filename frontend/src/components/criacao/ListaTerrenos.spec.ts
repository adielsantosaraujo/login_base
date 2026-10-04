import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ListaTerrenos from './ListaTerrenos.vue'

describe('ListaTerrenos', () => {
  it('mostra sigla, nome, valor com sufixo e barra proporcional ao máximo', () => {
    const w = mount(ListaTerrenos, {
      props: { itens: [{ terreno: 'COMERCIO', valor: 25 }], maximo: 50, sufixo: '%' },
    })
    expect(w.get('[data-testid="terreno-COMERCIO"]').text()).toContain('Co')
    expect(w.text()).toContain('Comércio')
    expect(w.text()).toContain('25%')
    expect(w.get('[role="progressbar"]').attributes('aria-valuemax')).toBe('50')
    expect(w.get('.vl-preenchimento').attributes('style')).toContain('width: 50%')
  })

  it('sem sufixo mostra só o número', () => {
    const w = mount(ListaTerrenos, { props: { itens: [{ terreno: 'MILITAR', valor: 5 }], maximo: 100 } })
    expect(w.get('.lt-valor').text()).toBe('5')
  })
})
