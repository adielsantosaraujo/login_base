import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import FamiliaLiderSelector from './FamiliaLiderSelector.vue'

const opcoes = [
  { familiaId: 1, sobrenome: 'Silva', lider: 'Ana', car: 13 },
  { familiaId: 2, sobrenome: 'Souza', lider: 'Bia', car: 30 },
]

describe('FamiliaLiderSelector', () => {
  it('mostra radiogroup com líder, CAR e bônus', () => {
    const w = mount(FamiliaLiderSelector, { props: { opcoes, modelValue: 1 } })
    expect(w.find('[role="radiogroup"]').exists()).toBe(true)
    expect(w.text()).toContain('Líder Ana · CAR 13')
    expect(w.text()).toContain('+6%')
    expect(w.text()).toContain('+10%')
    expect((w.get('[data-testid="lider-1"]').element as HTMLInputElement).checked).toBe(true)
  })

  it('emite a escolha', async () => {
    const w = mount(FamiliaLiderSelector, { props: { opcoes, modelValue: 1 } })
    await w.get('[data-testid="lider-2"]').setValue(true)
    expect(w.emitted('update:modelValue')![0]).toEqual([2])
  })
})
