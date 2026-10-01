import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import CidadaoForm from './CidadaoForm.vue'
import type { CidadaoPop } from '../composables/usePopulacao'

const cidadao: CidadaoPop = {
  id: 1, nome: 'Ana', sexo: 'F', idadeAnos: 30,
  caracteristicas: { VIT: 0, FOR: 0, VEL: 0, INT: 0, CAR: 0 },
  pontosCarPendentes: 20, pontosProfPendentes: 10, profissoes: {},
}

function montar(modelValue: { caracteristicas: Record<string, number>; profissoes: Record<string, number> }) {
  return mount(CidadaoForm, { props: { cidadao, modelValue }, global: { plugins: [PrimeVue] } })
}

describe('CidadaoForm', () => {
  it('mostra contadores de pontos usados', () => {
    const w = montar({ caracteristicas: { VIT: 10, CAR: 5 }, profissoes: { MINEIRO: 3 } })
    expect(w.get('[data-testid="contador-car"]').text()).toBe('15 / 20')
    expect(w.get('[data-testid="contador-prof"]').text()).toBe('3 / 10')
    expect(w.find('[data-testid="erro-cidadao"]').exists()).toBe(false)
  })

  it('avisa quando os limites são excedidos', () => {
    const w = montar({ caracteristicas: { VIT: 11, FOR: 10 }, profissoes: { MINEIRO: 6 } })
    const msgs = w.findAll('[data-testid="erro-cidadao"]').map((e) => e.text())
    expect(msgs).toContain('Máximo 10 por característica')
    expect(msgs).toContain('Máximo 20 pontos de característica')
    expect(msgs).toContain('Máximo 5 por profissão')
  })
})
