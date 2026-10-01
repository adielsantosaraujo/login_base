import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import HomeView from './HomeView.vue'

describe('HomeView', () => {
  it('renderiza "Seja bem-vindo"', () => {
    const wrapper = mount(HomeView, { global: { plugins: [PrimeVue] } })
    expect(wrapper.text()).toContain('Seja bem-vindo')
  })
})
