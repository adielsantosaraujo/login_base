import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createRouter, createMemoryHistory } from 'vue-router'
import CabecalhoJogo from './CabecalhoJogo.vue'

function montar(props = {}) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/:p(.*)*', component: { template: '<div/>' } }],
  })
  return mount(CabecalhoJogo, { props, global: { plugins: [router] } })
}

describe('CabecalhoJogo', () => {
  it('renderiza logo, seis abas e pílulas', () => {
    const w = montar({ turno: 7, segundosRestantes: 75 })
    expect(w.text()).toContain('Vilarejo')
    expect(w.findAll('a.vl-aba').map((a) => a.text())).toEqual([
      'Mapa', 'Estoque', 'Famílias', 'Mercado', 'Inventário', 'Batalhas',
    ])
    expect(w.get('[data-testid="pilula-turno"]').text()).toContain('7')
    expect(w.get('[data-testid="pilula-proximo"]').text()).toContain('01:15')
  })

  it('marca a aba ativa', () => {
    const w = montar({ abaAtiva: 'estoque' })
    expect(w.get('[data-testid="aba-estoque"]').classes()).toContain('ativa')
    expect(w.get('[data-testid="aba-estoque"]').attributes('aria-current')).toBe('page')
    expect(w.get('[data-testid="aba-mapa"]').classes()).not.toContain('ativa')
  })

  it('abas inativas têm aria-disabled e não navegam', async () => {
    const w = montar({ abasInativas: true })
    const aba = w.get('[data-testid="aba-mercado"]')
    expect(aba.attributes('aria-disabled')).toBe('true')
    expect(aba.attributes('tabindex')).toBe('-1')
  })

  it('abas ativas não têm aria-disabled', () => {
    const w = montar()
    expect(w.get('[data-testid="aba-mapa"]').attributes('aria-disabled')).toBeUndefined()
  })

  it('botão Relatório emite e some com mostrarRelatorio=false', async () => {
    const w = montar()
    await w.get('button').trigger('click')
    expect(w.emitted('relatorio')).toHaveLength(1)
    await w.setProps({ mostrarRelatorio: false })
    expect(w.find('button').exists()).toBe(false)
  })
})
