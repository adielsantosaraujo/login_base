import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { post } from '../../api/http'
import SlotsPedras from './SlotsPedras.vue'

const postMock = vi.mocked(post)
const pedra = { id: 5, qualidade: 'BOA', custoEngaste: 25, bonus: [{ codigo: 'FOR', magnitude: 'BAIXA', valor: 2 }] }

function montar(over: Record<string, unknown> = {}) {
  return mount(SlotsPedras, {
    props: { item: { id: 9, qualidade: 'EXCELENTE' }, pedras: [pedra], editavel: true, ...over } as never,
    global: { plugins: [PrimeVue] },
  })
}

describe('SlotsPedras', () => {
  beforeEach(() => { postMock.mockReset() })

  it('mostra slots ocupados e vazios', () => {
    const w = montar()
    expect(w.get('[data-testid="bonus-pedra-5"]').text()).toContain('Força +2')
    expect(w.findAll('[data-testid="slot-vazio"]')).toHaveLength(2)
  })

  it('item sem slots', () => {
    const w = montar({ item: { id: 1, qualidade: 'SIMPLES' }, pedras: [] })
    expect(w.find('[data-testid="sem-slots"]').exists()).toBe(true)
  })

  it('não mostra remover quando não editável', () => {
    expect(montar({ editavel: false }).find('[data-testid="remover-5"]').exists()).toBe(false)
  })

  it('remove com confirmação inline', async () => {
    postMock.mockResolvedValue({ itemId: 9, pedraRemovidaId: 5, slotsLivresApos: 3, pedraDestruida: true })
    const w = montar()
    await w.get('[data-testid="remover-5"]').trigger('click')
    expect(w.get('[data-testid="confirmacao-5"]').text()).toContain('destruirá permanentemente')
    expect(postMock).not.toHaveBeenCalled()
    await w.get('[data-testid="confirmar-remocao-5"]').trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/ferraria/remover-pedra', { itemId: 9, pedraId: 5 })
    expect(w.emitted('removida')?.[0]).toEqual([5])
    expect(w.find('[role="status"]').exists()).toBe(true)
  })

  it('cancelar não chama a API', async () => {
    const w = montar()
    await w.get('[data-testid="remover-5"]').trigger('click')
    await w.get('[data-testid="cancelar-remocao-5"]').trigger('click')
    expect(w.find('[data-testid="confirmacao-5"]').exists()).toBe(false)
    expect(postMock).not.toHaveBeenCalled()
  })

  it('erro do backend aparece em alert', async () => {
    postMock.mockRejectedValue(new Error('Sem Ferraria ativa'))
    const w = montar()
    await w.get('[data-testid="remover-5"]').trigger('click')
    await w.get('[data-testid="confirmar-remocao-5"]').trigger('click')
    await flushPromises()
    expect(w.get('[role="alert"]').text()).toBe('Sem Ferraria ativa')
    expect(w.emitted('removida')).toBeUndefined()
  })
})
