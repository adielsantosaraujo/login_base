import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ChecklistItem from './ChecklistItem.vue'

describe('ChecklistItem', () => {
  it('ok mostra ✓', () => {
    const w = mount(ChecklistItem, { props: { estado: 'ok', texto: 'Tudo certo' } })
    expect(w.classes()).toContain('ok')
    expect(w.get('.vl-circulo').text()).toBe('✓')
    expect(w.text()).toContain('Cumprido')
    expect(w.text()).toContain('Tudo certo')
  })

  it('aviso mostra !', () => {
    const w = mount(ChecklistItem, { props: { estado: 'aviso', texto: 'Atenção' } })
    expect(w.classes()).toContain('aviso')
    expect(w.get('.vl-circulo').text()).toBe('!')
    expect(w.text()).toContain('Aviso')
  })

  it('pendente fica vazio', () => {
    const w = mount(ChecklistItem, { props: { estado: 'pendente', texto: 'Falta' } })
    expect(w.classes()).toContain('pendente')
    expect(w.get('.vl-circulo').text()).toBe('')
    expect(w.get('.vl-circulo').attributes('aria-hidden')).toBe('true')
    expect(w.text()).toContain('Pendente')
  })
})
