import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import FamiliasList from './FamiliasList.vue'

const familias = [
  {
    id: 1,
    vilaId: 1,
    sobrenome: 'Silva',
    casaId: 7,
    membros: [
      { id: 10, nome: 'João', sexo: 'M', idadeAnos: 30, estadoCivil: 'SOLTEIRO', conjugeId: null },
      { id: 11, nome: 'Rui', sexo: 'M', idadeAnos: 12, estadoCivil: 'SOLTEIRO', conjugeId: null },
    ],
  },
]

function montar() {
  return mount(FamiliasList, {
    props: { familias } as never,
    global: { plugins: [PrimeVue], stubs: { 'router-link': { props: ['to'], template: '<a :href="to"><slot /></a>' } } },
  })
}

describe('FamiliasList', () => {
  it('lista famílias com contagem e expande membros', async () => {
    const w = montar()
    expect(w.get('[data-testid="familia-1"]').text()).toContain('Silva')
    expect(w.find('[data-testid="membros-1"]').exists()).toBe(false)
    await w.get('[data-testid="expandir-1"]').trigger('click')
    expect(w.get('[data-testid="membros-1"]').text()).toContain('João')
    expect(w.get('[data-testid="membro-10"] a').attributes('href')).toBe('/jogo/cidadao/10')
  })

  it('só habilita Casar para solteiro 18+ e emite o membro', async () => {
    const w = montar()
    await w.get('[data-testid="expandir-1"]').trigger('click')
    expect(w.get('[data-testid="casar-11"]').attributes('disabled')).toBeDefined()
    expect(w.get('[data-testid="casar-10"]').attributes('disabled')).toBeUndefined()
    await w.get('[data-testid="casar-10"]').trigger('click')
    expect(w.emitted('casar')?.[0][0]).toMatchObject({ id: 10 })
  })
})
