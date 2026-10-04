import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import FamiliaTabs from './FamiliaTabs.vue'

const familias = [
  { familiaId: 1, sobrenome: 'Silva', carPendentes: 2, profPendentes: 1, excedeu: false },
  { familiaId: 2, sobrenome: 'Souza', carPendentes: 0, profPendentes: 0, excedeu: false },
]

describe('FamiliaTabs', () => {
  it('mostra pendentes, chip de líder e aba ativa', () => {
    const w = mount(FamiliaTabs, { props: { familias, ativa: 1, liderId: 2 } })
    expect(w.get('[data-testid="aba-1"]').text()).toContain('3 pontos pendentes')
    expect(w.get('[data-testid="aba-2"]').text()).toContain('Todos os pontos usados')
    expect(w.get('[data-testid="aba-2"]').text()).toContain('LÍDER')
    expect(w.get('[data-testid="aba-1"]').attributes('aria-selected')).toBe('true')
  })

  it('emite a troca de aba', async () => {
    const w = mount(FamiliaTabs, { props: { familias, ativa: 1, liderId: 1 } })
    await w.get('[data-testid="aba-2"]').trigger('click')
    expect(w.emitted('update:ativa')![0]).toEqual([2])
  })
})
