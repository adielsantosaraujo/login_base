import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ConfirmacaoPainel from './ConfirmacaoPainel.vue'

const base = {
  construtores: 2, minConstrutores: 2, carregadores: 2, minCarregadores: 2,
  pendentes: 0, liderEscolhido: true, enviando: false,
}

describe('ConfirmacaoPainel', () => {
  it('válido: habilita e emite confirmar', async () => {
    const w = mount(ConfirmacaoPainel, { props: base })
    expect(w.text()).toContain('Todos os pontos usados')
    const cta = w.get('[data-testid="confirmar"]')
    expect(cta.text()).toBe('Confirmar população')
    await cta.trigger('click')
    expect(w.emitted('confirmar')).toHaveLength(1)
  })

  it('pendentes são aviso e não bloqueiam', () => {
    const w = mount(ConfirmacaoPainel, { props: { ...base, pendentes: 5 } })
    expect(w.text()).toContain('Pontos pendentes 5')
    expect(w.find('li.aviso').exists()).toBe(true)
    expect(w.get('[data-testid="confirmar"]').text()).toBe('Confirmar população')
  })

  it('mínimo ou líder faltando: Ajuste os mínimos, sem emitir', async () => {
    const w = mount(ConfirmacaoPainel, { props: { ...base, carregadores: 1 } })
    const cta = w.get('[data-testid="confirmar"]')
    expect(cta.text()).toBe('Ajuste os mínimos')
    await cta.trigger('click')
    expect(w.emitted('confirmar')).toBeUndefined()
    const w2 = mount(ConfirmacaoPainel, { props: { ...base, liderEscolhido: false } })
    expect(w2.get('[data-testid="confirmar"]').text()).toBe('Ajuste os mínimos')
  })
})
