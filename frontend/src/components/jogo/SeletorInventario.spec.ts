import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../../api/http', () => ({ get: vi.fn(), put: vi.fn(), del: vi.fn() }))
import { get } from '../../api/http'
import SeletorInventario from './SeletorInventario.vue'

const item = (id: number, subtipo: string, extra = {}) => ({
  id, categoria: 'ARMADURA', subtipo, nome: subtipo, nivel: 2, qualidade: 'BOA', bonus: [{ codigo: 'FOR', valor: 1 }],
  atributoEscolhido: null, slot: null, cidadaoId: null, emAprimoramento: false, atributoPrincipal: { tipo: 'DEFESA', valor: 3 }, ...extra,
})

describe('SeletorInventario', () => {
  beforeEach(() => vi.mocked(get).mockReset())

  it('filtra por categoria e subtipo do slot', async () => {
    vi.mocked(get).mockResolvedValue({ itens: [item(1, 'CAPACETE'), item(2, 'LUVAS'), item(3, 'CAPACETE', { slot: 'CAPACETE' })], total: 3, page: 0, pageSize: 100 })
    const w = mount(SeletorInventario, { props: { slot: 'CAPACETE' }, global: { plugins: [PrimeVue] } })
    await flushPromises()
    expect(get).toHaveBeenCalledWith(expect.stringContaining('categoria=ARMADURA'))
    expect(w.find('[data-testid="opcao-1"]').exists()).toBe(true)
    expect(w.find('[data-testid="opcao-2"]').exists()).toBe(false)
    expect(w.find('[data-testid="opcao-3"]').exists()).toBe(false)
  })

  it('emite seleção e mostra erro recebido', async () => {
    vi.mocked(get).mockResolvedValue({ itens: [item(1, 'CAPACETE')], total: 1, page: 0, pageSize: 100 })
    const w = mount(SeletorInventario, { props: { slot: 'CAPACETE', erro: 'PE insuficiente' }, global: { plugins: [PrimeVue] } })
    await flushPromises()
    await w.get('[data-testid="selecionar-1"]').trigger('click')
    expect(w.emitted('selecionar')?.[0][0]).toMatchObject({ id: 1 })
    expect(w.get('[data-testid="erro-equipar"]').text()).toBe('PE insuficiente')
  })
})
