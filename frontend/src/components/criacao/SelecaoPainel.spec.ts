import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import SelecaoPainel from './SelecaoPainel.vue'
import type { RegiaoPrevia } from '../../domain/regioes'
import { totaisLadrilhos } from '../../domain/terrenos'

const regioes: RegiaoPrevia[] = [
  { indice: 6, tipo: 'URBANA', terrenos: [
    { terreno: 'INDUSTRIA', posicao: 1, percentual: 45 },
    { terreno: 'COMERCIO', posicao: 2, percentual: 30 },
    { terreno: 'DESENVOLVIMENTO', posicao: 3, percentual: 25 },
  ] },
  { indice: 7, tipo: 'LITORAL', terrenos: [
    { terreno: 'SALINAS', posicao: 1, percentual: 40 },
    { terreno: 'MILITAR', posicao: 2, percentual: 35 },
    { terreno: 'ENXOFRE', posicao: 3, percentual: 25 },
  ] },
  { indice: 10, tipo: 'PLANICIE', terrenos: [
    { terreno: 'CRIACOES', posicao: 1, percentual: 40 },
    { terreno: 'FLORESTA', posicao: 2, percentual: 35 },
    { terreno: 'PLANTACOES', posicao: 3, percentual: 25 },
  ] },
]

function montar(selecionadas: number[], extra: Record<string, unknown> = {}) {
  return mount(SelecaoPainel, {
    props: {
      selecionadas,
      regioes,
      totais: totaisLadrilhos(selecionadas, regioes),
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

  it('lista ladrilhos por terreno em ordem decrescente, só com total > 0', () => {
    const w = montar([6, 7, 10])
    expect(w.get('[data-testid="terreno-INDUSTRIA"]').text()).toContain('45')
    expect(w.get('[data-testid="terreno-CRIACOES"]').text()).toContain('40')
    expect(w.find('[data-testid="terreno-ROCHA"]').exists()).toBe(false)
    expect(w.find('[data-testid="ladrilhos-vazio"]').exists()).toBe(false)
    const nomes = w.findAll('[data-testid^="terreno-"] .lt-nome').map((n) => n.text())
    expect(nomes.slice(0, 3)).toEqual(['In Indústria', 'Cr Criações', 'Sa Salinas'])
  })

  it('sem seleção mostra a mensagem de painel vazio', () => {
    const w = montar([])
    expect(w.get('[data-testid="ladrilhos-vazio"]').text()).toBe('Selecione regiões para ver os ladrilhos')
    expect(w.find('[data-testid^="terreno-"]').exists()).toBe(false)
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
