import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import GradeRegiao from './GradeRegiao.vue'

describe('GradeRegiao', () => {
  it('renderiza 100 ladrilhos com casas e terrenos', () => {
    const ladrilhos = [0, 2, 4, 6].map((x) => ({
      x, y: 0, terreno: 'DESENVOLVIMENTO', bonusBase: 10, bonusAdjacente: 0, bonusTotal: 10, construcao: { id: x + 1, tipo: 'CASA', nivel: 'N1', tamanho: 2 },
    }))
    ladrilhos.push({ x: 5, y: 5, terreno: 'FERRO', bonusBase: 30, bonusAdjacente: 50, bonusTotal: 80, construcao: null } as never)
    const w = mount(GradeRegiao, { props: { ladrilhos: ladrilhos as never } })
    const cel = w.findAll('[data-testid="ladrilho"]')
    expect(cel).toHaveLength(100)
    expect(w.findAll('.ladrilho-construcao')).toHaveLength(4)
    expect(cel[0]!.attributes('title')).toContain('Casa')
    expect(w.find('[data-x="5"][data-y="5"]').attributes('title')).toContain('Ferro (Fe)')
  })

  it('mostra endereço acima da sigla', () => {
    const ladrilhos = [
      { x: 0, y: 0, terreno: 'FLORESTA', bonusBase: 1, bonusAdjacente: 0, bonusTotal: 1, construcao: null },
      { x: 9, y: 9, terreno: 'DESENVOLVIMENTO', bonusBase: 1, bonusAdjacente: 0, bonusTotal: 1, construcao: null },
    ]
    const w = mount(GradeRegiao, { props: { ladrilhos: ladrilhos as never } })
    const a = w.get('[data-x="0"][data-y="0"]')
    expect(a.get('.lad-endereco').text()).toBe('(A,1)')
    expect(a.get('.lad-sigla').text()).toBe('Fl')
    const j = w.get('[data-x="9"][data-y="9"]')
    expect(j.get('.lad-endereco').text()).toBe('(J,10)')
    expect(j.get('.lad-sigla').text()).toBe('De')
  })

  it('tooltip conforme o spec', () => {
    const ladrilhos = [{ x: 2, y: 4, terreno: 'FLORESTA', bonusBase: 30, bonusAdjacente: 50, bonusTotal: 80, construcao: null }]
    const w = mount(GradeRegiao, { props: { ladrilhos: ladrilhos as never } })
    expect(w.get('[data-x="2"][data-y="4"]').attributes('title')).toBe('(C,5) - Floresta (Fl) - base 30 - adjacente +50 - total 80')
    expect(w.get('[data-x="0"][data-y="0"]').attributes('title')).toBe('(A,1) - Vazio')
  })

  it('célula com casa mostra endereço, sigla e indicador do prédio', () => {
    const ladrilhos = [
      { x: 3, y: 0, terreno: 'DESENVOLVIMENTO', bonusBase: 10, bonusAdjacente: 0, bonusTotal: 10, construcao: { id: 1, tipo: 'CASA', nivel: 'N1', tamanho: 2 } },
    ]
    const w = mount(GradeRegiao, { props: { ladrilhos: ladrilhos as never } })
    const c = w.get('[data-x="3"][data-y="0"]')
    expect(c.get('.lad-endereco').text()).toBe('(D,1)')
    expect(c.get('.lad-sigla').text()).toBe('De')
    expect(c.get('.lad-predio').text()).toBe('C')
  })

  it('emite clique, destaca e marca selecionáveis', async () => {
    const w = mount(GradeRegiao, { props: { ladrilhos: [], destaques: [{ x: 1, y: 1 }], selecionaveis: true } })
    expect(w.findAll('.destaque')).toHaveLength(1)
    expect(w.findAll('.selecionavel')).toHaveLength(100)
    await w.get('[data-x="2"][data-y="3"]').trigger('click')
    expect(w.emitted('clique-ladrilho')![0]).toEqual([{ x: 2, y: 3, ladrilho: null }])
  })
})
