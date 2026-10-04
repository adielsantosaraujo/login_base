import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import BotaoCta from './BotaoCta.vue'

describe('BotaoCta', () => {
  it('ativo emite click e mostra rótulo', async () => {
    const w = mount(BotaoCta, { props: { rotulo: 'Fundar' } })
    expect(w.text()).toBe('Fundar')
    expect(w.attributes('aria-disabled')).toBeUndefined()
    await w.trigger('click')
    expect(w.emitted('click')).toHaveLength(1)
  })

  it('inativo não emite e tem aria-disabled', async () => {
    const w = mount(BotaoCta, { props: { rotulo: 'Fundar', ativo: false } })
    expect(w.classes()).toContain('inativo')
    expect(w.attributes('aria-disabled')).toBe('true')
    await w.trigger('click')
    expect(w.emitted('click')).toBeUndefined()
  })

  it('carregando mostra spinner, aria-busy e não emite', async () => {
    const w = mount(BotaoCta, { props: { rotulo: 'Fundar', carregando: true } })
    expect(w.find('i.pi.pi-spin.pi-spinner').exists()).toBe(true)
    expect(w.attributes('aria-busy')).toBe('true')
    await w.trigger('click')
    expect(w.emitted('click')).toBeUndefined()
  })

  it('aceita slot', () => {
    const w = mount(BotaoCta, { slots: { default: 'Slot' } })
    expect(w.text()).toBe('Slot')
  })
})
