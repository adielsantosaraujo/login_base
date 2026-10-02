import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import ProfissoesTab from './ProfissoesTab.vue'
import EquipamentoTab from './EquipamentoTab.vue'

const profissoes = [{ profissao: 'CONSTRUTOR', peBase: 2, peEfetivo: 3, eficiencia: 1.5 }]

describe('ProfissoesTab', () => {
  it('exibe PE base, efetivo e eficiência sem inputs quando não há pendentes', () => {
    const w = mount(ProfissoesTab, { props: { profissoes, pontosPendentes: 0 }, global: { plugins: [PrimeVue] } })
    expect(w.get('[data-testid="pe-base"]').text()).toBe('2')
    expect(w.get('[data-testid="pe-efetivo"]').text()).toBe('3')
    expect(w.get('[data-testid="eficiencia"]').text()).toBe('150%')
    expect(w.find('[data-testid="distribuicao-prof"]').exists()).toBe(false)
  })

  it('emite distribuição', async () => {
    const w = mount(ProfissoesTab, { props: { profissoes, pontosPendentes: 2 }, global: { plugins: [PrimeVue] } })
    const input = w.get('[data-testid="inc-CONSTRUTOR"]')
    await input.setValue('2')
    await input.trigger('blur')
    await w.get('[data-testid="confirmar-prof"]').trigger('click')
    expect(w.emitted('distribuir')?.[0]).toEqual([{ CONSTRUTOR: 2 }])
  })
})

describe('EquipamentoTab', () => {
  it('mostra slots vazios', () => {
    expect(mount(EquipamentoTab, { props: { cidadaoId: 1 }, global: { plugins: [PrimeVue] } }).text()).toContain('Vazio')
  })
})
