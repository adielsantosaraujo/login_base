import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import BarraTurno from './BarraTurno.vue'

describe('BarraTurno', () => {
  it('exibe número e contagem e emite abertura do relatório', async () => {
    const w = mount(BarraTurno, {
      props: { numero: 12, segundosRestantes: 65 },
      global: { plugins: [PrimeVue] },
    })
    expect(w.get('[data-testid="turno-numero"]').text()).toBe('12')
    expect(w.get('[data-testid="turno-contagem"]').text()).toBe('01:05')
    await w.get('[data-testid="abrir-relatorio"]').trigger('click')
    expect(w.emitted('relatorio')).toHaveLength(1)
  })
})
