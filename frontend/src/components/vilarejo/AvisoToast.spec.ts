import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import AvisoToast from './AvisoToast.vue'

describe('AvisoToast', () => {
  it('info usa role status e aria-live polite', () => {
    const w = mount(AvisoToast, { props: { mensagem: 'Salvo' } })
    expect(w.text()).toBe('Salvo')
    expect(w.attributes('role')).toBe('status')
    expect(w.attributes('aria-live')).toBe('polite')
  })

  it('erro usa role alert', () => {
    const w = mount(AvisoToast, { props: { mensagem: 'Falhou', tipo: 'erro' } })
    expect(w.attributes('role')).toBe('alert')
    expect(w.classes()).toContain('erro')
  })
})
