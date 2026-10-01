import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import GradeRegiao from './GradeRegiao.vue'

describe('GradeRegiao', () => {
  it('renderiza 100 ladrilhos com casas e jazidas', () => {
    const ladrilhos = [0, 2, 4, 6].map((x) => ({
      x, y: 0, jazida: null, construcao: { id: x + 1, tipo: 'CASA', nivel: 'N1', tamanho: 2 },
    }))
    ladrilhos.push({ x: 5, y: 5, jazida: 'VEIO_DE_FERRO', construcao: null } as never)
    const w = mount(GradeRegiao, { props: { ladrilhos } })
    const cel = w.findAll('[data-testid="ladrilho"]')
    expect(cel).toHaveLength(100)
    expect(w.findAll('.ladrilho-construcao')).toHaveLength(4)
    expect(cel[0]!.attributes('title')).toContain('Casa')
    expect(w.find('[data-x="5"][data-y="5"]').attributes('title')).toContain('Veio de ferro')
  })

  it('emite clique, destaca e marca selecionáveis', async () => {
    const w = mount(GradeRegiao, { props: { ladrilhos: [], destaques: [{ x: 1, y: 1 }], selecionaveis: true } })
    expect(w.findAll('.destaque')).toHaveLength(1)
    expect(w.findAll('.selecionavel')).toHaveLength(100)
    await w.get('[data-x="2"][data-y="3"]').trigger('click')
    expect(w.emitted('clique-ladrilho')![0]).toEqual([{ x: 2, y: 3, ladrilho: null }])
  })
})
