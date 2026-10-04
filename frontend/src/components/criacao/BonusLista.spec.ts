import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import BonusLista from './BonusLista.vue'

describe('BonusLista', () => {
  it('mostra nome, valor e barra proporcional ao máximo', () => {
    const w = mount(BonusLista, {
      props: { itens: [{ bonus: 'COMERCIO', valor: 25 }], maximo: 50 },
    })
    expect(w.text()).toContain('Comércio')
    expect(w.text()).toContain('25')
    expect(w.get('[role="progressbar"]').attributes('aria-valuemax')).toBe('50')
    expect(w.get('.vl-preenchimento').attributes('style')).toContain('width: 50%')
  })

  it('esmaece itens com valor 0', () => {
    const w = mount(BonusLista, {
      props: { itens: [{ bonus: 'COMERCIO', valor: 0 }, { bonus: 'MILITAR', valor: 5 }], maximo: 150 },
    })
    expect(w.get('[data-testid="bonus-COMERCIO"]').classes()).toContain('esmaecido')
    expect(w.get('[data-testid="bonus-MILITAR"]').classes()).not.toContain('esmaecido')
  })
})
