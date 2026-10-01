import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import EstoqueTable from './EstoqueTable.vue'
import type { LinhaEstoque } from '../composables/useEstoque'

const recursos: LinhaEstoque[] = [
  { recurso: 'MADEIRA', nome: 'Madeira', quantidade: 100.9, capacidade: 500, percentualUsado: 20.18 },
  { recurso: 'PEDRA', nome: 'Pedra', quantidade: 480, capacidade: 500, percentualUsado: 96 },
  { recurso: 'OURO', nome: 'Ouro', quantidade: 1234, capacidade: null, percentualUsado: null },
]

describe('EstoqueTable', () => {
  const w = mount(EstoqueTable, { props: { recursos }, global: { plugins: [PrimeVue] } })

  it('renderiza uma linha por recurso com quantidade inteira', () => {
    const linhas = w.findAll('tbody tr')
    expect(linhas).toHaveLength(3)
    expect(linhas[0]!.text()).toContain('Madeira')
    expect(linhas[0]!.find('[data-testid="quantidade"]').text()).toBe('100')
  })

  it('destaca recurso acima de 90% da capacidade', () => {
    const linhas = w.findAll('tbody tr')
    expect(linhas[1]!.classes()).toContain('linha-alerta')
    expect(linhas[0]!.classes()).not.toContain('linha-alerta')
    expect(w.findAll('[data-testid="status-alerta"]')).toHaveLength(1)
  })

  it('mostra capacidade ilimitada para o Ouro', () => {
    const linhas = w.findAll('tbody tr')
    expect(linhas[2]!.find('[data-testid="capacidade"]').text()).toBe('Ilimitada')
  })
})
