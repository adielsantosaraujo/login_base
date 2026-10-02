import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import RodadaReplay from './RodadaReplay.vue'

const g = { id: 1, lado: 'TROPA' as const, nome: 'Guerreiro' }
const o = { id: 1, lado: 'INIMIGO' as const, nome: 'Goblin' }
const rodadas = [
  { numero: 1, acoes: [
    { atacante: g, alvo: o, dano: 23, critico: true, pvAntes: 40, pvDepois: 17, abatido: false },
    { atacante: o, alvo: g, dano: 11, critico: false, pvAntes: 79, pvDepois: 68, abatido: false },
  ] },
  { numero: 2, acoes: [{ atacante: g, alvo: o, dano: 17, critico: false, pvAntes: 17, pvDepois: 0, abatido: true }] },
]

describe('RodadaReplay', () => {
  it('lista ações por rodada com PV, crítico e abatido', () => {
    const w = mount(RodadaReplay, { props: { rodadas } })
    expect(w.find('[data-testid="rodada-1"]').exists()).toBe(true)
    expect(w.find('[data-testid="acao-1-0"]').text()).toContain('Guerreiro → Goblin: 23')
    expect(w.find('[data-testid="acao-1-0"]').text()).toContain('crítico!')
    expect(w.find('[data-testid="acao-1-0"]').text()).toContain('PV 40 → 17')
    expect(w.find('[data-testid="acao-1-1"]').text()).not.toContain('crítico')
    expect(w.find('[data-testid="acao-2-0"]').text()).toContain('abatido')
    expect(w.find('[data-testid="acao-1-1"]').text()).not.toContain('abatido')
  })

  it('mostra mensagem sem rodadas', () => {
    const w = mount(RodadaReplay, { props: { rodadas: [] } })
    expect(w.find('[data-testid="sem-rodadas"]').exists()).toBe(true)
  })
})
