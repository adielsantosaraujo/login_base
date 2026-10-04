import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import SelecaoPainel from './SelecaoPainel.vue'
import { totaisBonus, type RegiaoPrevia } from '../../domain/regioes'

const regioes: RegiaoPrevia[] = [
  { indice: 6, tipo: 'URBANA', bonus: [
    { bonus: 'COMERCIO', posicao: 1, valor: 47 },
    { bonus: 'INDUSTRIA', posicao: 2, valor: 30 },
    { bonus: 'DESENVOLVIMENTO', posicao: 3, valor: 12 },
  ] },
  { indice: 7, tipo: 'LITORAL', bonus: [
    { bonus: 'SALINAS', posicao: 1, valor: 38 },
    { bonus: 'MILITAR', posicao: 2, valor: 20 },
    { bonus: 'ENXOFRE', posicao: 3, valor: 6 },
  ] },
  { indice: 10, tipo: 'PLANICIE', bonus: [
    { bonus: 'CRIACOES', posicao: 1, valor: 41 },
    { bonus: 'FLORESTA', posicao: 2, valor: 25 },
    { bonus: 'PLANTACOES', posicao: 3, valor: 14 },
  ] },
]

function montar(selecionadas: number[], extra: Record<string, unknown> = {}) {
  return mount(SelecaoPainel, {
    props: {
      selecionadas,
      regioes,
      totais: totaisBonus(selecionadas, regioes),
      conectado: true,
      valida: selecionadas.length === 3,
      enviando: false,
      emFoco: null,
      ...extra,
    },
  })
}

describe('SelecaoPainel', () => {
  it('mostra slots vazios e preenchidos', () => {
    const w = montar([6])
    const slots = w.findAll('[data-testid="slot"]')
    expect(slots).toHaveLength(3)
    expect(slots[0].text()).toContain('06')
    expect(slots[0].text()).toContain('Urbana')
    expect(slots[1].text()).toContain('VAZIO')
  })

  it('soma os bônus e esmaece os zerados', () => {
    const w = montar([6, 7, 10])
    expect(w.get('[data-testid="bonus-COMERCIO"]').text()).toContain('47')
    expect(w.get('[data-testid="bonus-CRIACOES"]').text()).toContain('41')
    expect(w.get('[data-testid="bonus-ROCHA"]').classes()).toContain('esmaecido')
    expect(w.get('[data-testid="bonus-COMERCIO"]').classes()).not.toContain('esmaecido')
    const nomes = w.findAll('[data-testid^="bonus-"] .bl-nome').map((n) => n.text())
    expect(nomes.slice(0, 3)).toEqual(['Comércio', 'Criações', 'Salinas'])
  })

  it('checklist reflete a seleção sem Urbana', () => {
    const w = montar([7, 10], { valida: false })
    const itens = w.findAll('[data-testid="checklist"] li')
    expect(itens[0].text()).toContain('3 regiões 2/3')
    expect(itens[0].classes()).toContain('pendente')
    expect(itens[2].text()).toContain('Ao menos 1 Urbana')
    expect(itens[2].classes()).toContain('pendente')
  })

  it('botão inválido mostra texto e não emite criar', async () => {
    const w = montar([7, 10], { valida: false })
    const botao = w.get('[data-testid="criar"]')
    expect(botao.text()).toBe('Selecione 3 regiões válidas')
    expect(botao.attributes('aria-disabled')).toBe('true')
    await botao.trigger('click')
    expect(w.emitted('criar')).toBeUndefined()
  })

  it('botão válido emite criar e Limpar emite limpar', async () => {
    const w = montar([6, 7, 10])
    expect(w.get('[data-testid="criar"]').text()).toBe('Criar vila')
    await w.get('[data-testid="criar"]').trigger('click')
    await w.get('[data-testid="limpar"]').trigger('click')
    expect(w.emitted('criar')).toHaveLength(1)
    expect(w.emitted('limpar')).toHaveLength(1)
  })

  it('mostra a região em foco', () => {
    const w = montar([6], { emFoco: 7 })
    expect(w.get('[data-testid="foco-titulo"]').text()).toBe('Região 07 · Litoral')
  })
})
