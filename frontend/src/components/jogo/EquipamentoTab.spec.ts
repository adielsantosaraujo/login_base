import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../../api/http', () => ({ get: vi.fn(), put: vi.fn(), del: vi.fn() }))
import { del, get, put } from '../../api/http'
import type { ItemDTO } from '../../composables/useItens'
import EquipamentoTab from './EquipamentoTab.vue'

const espada = {
  id: 10, categoria: 'ARMA', subtipo: 'ESPADA', nome: 'Espada', nivel: 3, qualidade: 'EXCELENTE',
  bonus: [{ codigo: 'FOR', valor: 2 }], atributoEscolhido: null, slot: 'ARMA', cidadaoId: 1,
  emAprimoramento: false, atributoPrincipal: { tipo: 'ATAQUE', valor: 12 },
} as ItemDTO
const montar = (props = {}) => mount(EquipamentoTab, {
  props: { cidadaoId: 1, equipamento: { ARMA: espada }, ...props }, global: { plugins: [PrimeVue] },
})

describe('EquipamentoTab', () => {
  beforeEach(() => { vi.mocked(get).mockReset(); vi.mocked(put).mockReset(); vi.mocked(del).mockReset() })

  it('renderiza 11 slots com item e vazios', () => {
    const w = montar()
    expect(w.findAll('[data-testid^="slot-"]')).toHaveLength(11)
    const s = w.get('[data-testid="slot-ARMA"]')
    expect(s.text()).toContain('Espada')
    expect(s.text()).toContain('ATAQUE 12')
    expect(s.text()).toContain('Força +2')
    expect(s.get('[data-testid="item-qualidade"]').text()).toBe('Excelente')
    expect(w.get('[data-testid="slot-COLAR"]').text()).toContain('Vazio')
    expect(w.find('[data-testid="remover-COLAR"]').exists()).toBe(false)
  })

  it('remover chama DELETE e emite atualizado', async () => {
    vi.mocked(del).mockResolvedValue({})
    const w = montar()
    await w.get('[data-testid="remover-ARMA"]').trigger('click')
    await flushPromises()
    expect(del).toHaveBeenCalledWith('/api/jogo/cidadao/1/equipamento/ARMA')
    expect(w.emitted('atualizado')?.[0]).toEqual([{}])
  })

  it('equipar via seletor chama PUT; erro fica inline', async () => {
    vi.mocked(get).mockResolvedValue({ itens: [{ ...espada, id: 11, slot: null, cidadaoId: null }], total: 1, page: 0, pageSize: 100 })
    vi.mocked(put).mockRejectedValueOnce(new Error('Idade insuficiente'))
    const w = montar({ equipamento: {} })
    await w.get('[data-testid="equipar-ARMA"]').trigger('click')
    await flushPromises()
    await w.get('[data-testid="selecionar-11"]').trigger('click')
    await flushPromises()
    expect(put).toHaveBeenCalledWith('/api/jogo/cidadao/1/equipamento/ARMA', { itemId: 11 })
    expect(w.get('[data-testid="erro-equipar"]').text()).toBe('Idade insuficiente')
    vi.mocked(put).mockResolvedValueOnce({ ARMA: espada })
    await w.get('[data-testid="selecionar-11"]').trigger('click')
    await flushPromises()
    expect(w.emitted('atualizado')).toBeTruthy()
    expect(w.find('[data-testid="seletor-inventario"]').exists()).toBe(false)
  })

  it('desabilita botões em expedição', () => {
    const w = montar({ emExpedicao: true })
    expect(w.get('[data-testid="equipar-ARMA"]').attributes('disabled')).toBeDefined()
    expect(w.get('[data-testid="remover-ARMA"]').attributes('disabled')).toBeDefined()
    expect(w.find('[data-testid="aviso-expedicao"]').exists()).toBe(true)
  })
})
