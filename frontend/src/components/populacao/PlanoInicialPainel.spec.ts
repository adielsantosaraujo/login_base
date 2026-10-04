import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import PlanoInicialPainel from './PlanoInicialPainel.vue'
import { PLANO_PADRAO, PROFISSOES, type Profissao } from '../../domain/populacao'

function atual(): Record<Profissao, number> {
  return Object.fromEntries(PROFISSOES.map((p) => [p, PLANO_PADRAO[p] || 0])) as Record<Profissao, number>
}
const base = { plano: PLANO_PADRAO, minimos: { CONSTRUTOR: 2, CARREGADOR: 2 }, total: 16, totalEsperado: 16 }

describe('PlanoInicialPainel', () => {
  it('lista 12 profissões com chip de mínimo', () => {
    const w = mount(PlanoInicialPainel, { props: { ...base, atual: atual() } })
    expect(w.findAll('[data-testid^="plano-"]').length).toBe(13) // 12 + total
    expect(w.get('[data-testid="plano-CONSTRUTOR"]').text()).toContain('mín 2')
  })

  it('destaca atual abaixo do mínimo e diferente do plano', () => {
    const a = atual()
    a.CONSTRUTOR = 1
    a.MINEIRO = 3
    const w = mount(PlanoInicialPainel, { props: { ...base, atual: a } })
    expect(w.get('[data-testid="atual-CONSTRUTOR"]').classes()).toContain('erro')
    expect(w.get('[data-testid="atual-MINEIRO"]').classes()).toContain('aviso')
  })

  it('total 15 em aviso; não desce abaixo do mínimo; emite alterar', async () => {
    const w = mount(PlanoInicialPainel, { props: { ...base, total: 15, atual: atual() } })
    expect(w.get('[data-testid="plano-total"]').text()).toBe('15 / 16')
    expect(w.get('[data-testid="plano-total"]').classes()).toContain('aviso')
    const menos = w.get('[data-testid="plano-CONSTRUTOR"] button[aria-label^="Diminuir"]')
    expect(menos.attributes('disabled')).toBeDefined()
    await w.get('[data-testid="plano-MINEIRO"] button[aria-label^="Aumentar"]').trigger('click')
    expect(w.emitted('alterar')![0]).toEqual(['MINEIRO', 3])
  })
})
