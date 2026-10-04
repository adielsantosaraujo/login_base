import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import SeletorConstrucao from './SeletorConstrucao.vue'
import type { CatalogoConstrucao } from '../../composables/useConstrucoes'

const catalogo: CatalogoConstrucao[] = [
  { tipo: 'CASA', nome: 'Casa', regioes: ['URBANA', 'PLANICIE'], bonusRegiao: null, custoN1: { MADEIRA: 10 }, tamanho: 2, poN1: 5, profissoes: [] },
  { tipo: 'PEDREIRA', nome: 'Pedreira', regioes: ['MONTANHA'], bonusRegiao: 'ROCHA', custoN1: { MADEIRA: 5 }, tamanho: 2, poN1: 5, profissoes: ['PEDREIRO'] },
]

function montar(props: Record<string, unknown> = {}) {
  return mount(SeletorConstrucao, {
    props: { catalogo, tipoRegiao: 'URBANA', estoque: { MADEIRA: 100 }, ...props },
    global: { plugins: [PrimeVue] },
  })
}

describe('SeletorConstrucao', () => {
  it('desabilita prédio de outra região', () => {
    const w = montar()
    expect(w.get('[data-testid="item-PEDREIRA"]').attributes('disabled')).toBeDefined()
    expect(w.get('[data-testid="item-CASA"]').attributes('disabled')).toBeUndefined()
  })

  it('mostra Todos + 5 abas na ordem do enum', () => {
    const w = montar()
    expect(w.findAll('[role="tab"]').map((a) => a.text())).toEqual(['Todos', 'Floresta', 'Planície', 'Urbana', 'Litoral', 'Montanha'])
    expect(w.find('[data-testid="item-CASA"]').exists()).toBe(true)
    expect(w.find('[data-testid="item-PEDREIRA"]').exists()).toBe(true)
  })

  it('filtra por aba', async () => {
    const w = montar()
    await w.get('[data-testid="aba-montanha"]').trigger('click')
    expect(w.find('[data-testid="item-CASA"]').exists()).toBe(false)
    expect(w.find('[data-testid="item-PEDREIRA"]').exists()).toBe(true)
  })

  it('desabilita construir com recursos insuficientes', async () => {
    const w = montar({ estoque: { MADEIRA: 1 } })
    await w.get('[data-testid="item-CASA"]').trigger('click')
    expect(w.get('[data-testid="construir"]').attributes('disabled')).toBeDefined()
    expect(w.get('[data-testid="motivo"]').text()).toContain('Recursos insuficientes')
  })

  it('emite construir com o tipo escolhido', async () => {
    const w = montar()
    await w.get('[data-testid="item-CASA"]').trigger('click')
    await w.get('[data-testid="construir"]').trigger('click')
    expect(w.emitted('construir')![0]).toEqual(['CASA'])
  })

  it('mostra erro do backend', () => {
    const w = montar({ erro: 'Ladrilho ocupado' })
    expect(w.get('[data-testid="erro-construcao"]').text()).toBe('Ladrilho ocupado')
  })
})
