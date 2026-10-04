import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import PontoStepper from './PontoStepper.vue'

describe('PontoStepper', () => {
  it('aumenta e diminui via v-model', async () => {
    const w = mount(PontoStepper, { props: { modelValue: 3, rotulo: 'Força' } })
    await w.get('[aria-label="Aumentar Força"]').trigger('click')
    await w.setProps({ modelValue: 4 })
    await w.get('[aria-label="Diminuir Força"]').trigger('click')
    expect(w.emitted('update:modelValue')).toEqual([[4], [3]])
  })

  it('desativa menos no mínimo', async () => {
    const w = mount(PontoStepper, { props: { modelValue: 1, min: 1 } })
    const menos = w.get('[aria-label="Diminuir pontos"]')
    expect(menos.attributes('disabled')).toBeDefined()
    await menos.trigger('click')
    expect(w.emitted('update:modelValue')).toBeUndefined()
  })

  it('desativa mais quando não pode aumentar', () => {
    const w = mount(PontoStepper, { props: { modelValue: 1, podeAumentar: false } })
    expect(w.get('[aria-label="Aumentar pontos"]').attributes('disabled')).toBeDefined()
  })

  it('exibe o valor', () => {
    const w = mount(PontoStepper, { props: { modelValue: 5 } })
    expect(w.get('[data-testid="stepper-valor"]').text()).toBe('5')
  })
})
