import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import FamiliaLiderSelector from './FamiliaLiderSelector.vue'
import type { CidadaoPop, FamiliaPop } from '../composables/usePopulacao'

const cid = (id: number, nome: string, idadeAnos: number): CidadaoPop => ({
  id, nome, sexo: 'M', idadeAnos, caracteristicas: { CAR: 0 }, pontosCarPendentes: 20,
  pontosProfPendentes: 10, profissoes: {},
})
const familias: FamiliaPop[] = [
  { id: 1, sobrenome: 'Silva', cidadaos: [cid(1, 'Joao', 40), cid(2, 'Pedro', 10)] },
  { id: 2, sobrenome: 'Souza', cidadaos: [cid(3, 'Maria', 35)] },
]

describe('FamiliaLiderSelector', () => {
  it('calcula o bônus do líder em tempo real a partir do CAR distribuído', () => {
    const w = mount(FamiliaLiderSelector, {
      props: {
        familias, modelValue: null,
        distribuicoes: { 1: { caracteristicas: { CAR: 10 }, profissoes: {} }, 3: { caracteristicas: { CAR: 5 }, profissoes: {} } },
      },
      global: { plugins: [PrimeVue] },
    })
    expect(w.get('[data-testid="bonus-1"]').text()).toBe('+5%')
    expect(w.get('[data-testid="bonus-2"]').text()).toBe('+2%')
    expect(w.text()).toContain('líder Joao')
  })

  it('emite a família escolhida', async () => {
    const w = mount(FamiliaLiderSelector, {
      props: { familias, modelValue: null, distribuicoes: {} },
      global: { plugins: [PrimeVue] },
    })
    await w.get('[data-testid="lider-2"] input').setValue(true)
    expect(w.emitted('update:modelValue')?.[0]).toEqual([2])
  })
})
