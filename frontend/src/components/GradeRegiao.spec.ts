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
})
