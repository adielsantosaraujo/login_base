import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import RecompensasBatalha from './RecompensasBatalha.vue'

const completo = {
  ouro: 287,
  recursos: { MADEIRA: 60, ACO: 120 },
  item: { itemId: 55, subtipo: 'ESPADA', categoria: 'ARMA', nivel: 6, qualidade: 'BOA', bonus: [{ codigo: 'FOR', valor: 2 }], atributoEscolhido: null },
  xpPorGuerreiro: 6,
  guerreirosXp: [12, 15],
  pedras: [{ pedraId: 9, qualidade: 'BOA', bonus: [{ codigo: 'VIT', magnitude: 'BAIXA', valor: 1 }] }],
}
const montar = (r: unknown) => mount(RecompensasBatalha, { props: { recompensas: r as never } })

describe('RecompensasBatalha', () => {
  it('renderiza o JSON completo', () => {
    const w = montar(completo)
    expect(w.find('[data-testid="recompensa-ouro"]').text()).toBe('Ouro: 287')
    expect(w.find('[data-testid="recurso-MADEIRA"]').text()).toBe('Madeira x60')
    expect(w.find('[data-testid="recurso-ACO"]').text()).toBe('Aço x120')
    const item = w.find('[data-testid="recompensa-item"]').text()
    expect(item).toContain('Espada')
    expect(item).toContain('Boa')
    expect(item).toContain('nível 6')
    expect(item).toContain('Força +2')
    expect(w.find('[data-testid="recompensa-xp"]').text()).toBe('XP: 6 por guerreiro (2 guerreiros)')
    const pedra = w.find('[data-testid="recompensa-pedra"]').text()
    expect(pedra).toContain('Boa')
    expect(pedra).toContain('Vitalidade +1 (baixa)')
  })

  it('sem item não mostra item', () => {
    const w = montar({ ...completo, item: null })
    expect(w.find('[data-testid="recompensa-item"]').exists()).toBe(false)
    expect(w.find('[data-testid="recompensa-ouro"]').exists()).toBe(true)
  })

  it('sem pedras não mostra pedras', () => {
    const w = montar({ ...completo, pedras: [] })
    expect(w.find('[data-testid="recompensa-pedra"]').exists()).toBe(false)
  })

  it('null mostra "Sem recompensas"', () => {
    const w = montar(null)
    expect(w.find('[data-testid="sem-recompensas"]').text()).toBe('Sem recompensas')
  })
})
