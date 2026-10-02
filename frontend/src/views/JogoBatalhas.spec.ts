import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn() }))

import { get } from '../api/http'
import JogoBatalhas from './JogoBatalhas.vue'

const getMock = vi.mocked(get)
const stubs = { 'router-link': { template: '<a><slot /></a>', props: ['to'] } }
const b = (id: number, resultado: string) => ({
  id, turno: 10 + id, tropaId: 1, tropaNome: `T${id}`, masmorraId: 3, masmorraNivel: 2, regiaoIndice: 5,
  resultado, rodadas: 4, criadoEm: '2026-01-01T00:00:00Z',
})

describe('JogoBatalhas', () => {
  beforeEach(() => { getMock.mockReset() })

  it('lista batalhas com destaque do resultado', async () => {
    getMock.mockResolvedValue([b(2, 'VITORIA'), b(1, 'DERROTA')])
    const w = mount(JogoBatalhas, { global: { stubs } })
    await flushPromises()
    expect(w.findAll('tbody tr')).toHaveLength(2)
    expect(w.find('[data-testid="resultado-2"]').text()).toBe('VITÓRIA')
    expect(w.find('[data-testid="resultado-2"]').classes()).toContain('vitoria')
    expect(w.find('[data-testid="resultado-1"]').text()).toBe('DERROTA')
    expect(w.find('[data-testid="resultado-1"]').classes()).toContain('derrota')
    expect(w.find('[data-testid="batalha-2"]').text()).toContain('T2')
  })

  it('vazio mostra mensagem', async () => {
    getMock.mockResolvedValue([])
    const w = mount(JogoBatalhas, { global: { stubs } })
    await flushPromises()
    expect(w.find('[data-testid="sem-batalhas"]').text()).toBe('Nenhuma batalha ainda')
  })

  it('erro com role=alert', async () => {
    getMock.mockRejectedValue(new Error('Falha'))
    const w = mount(JogoBatalhas, { global: { stubs } })
    await flushPromises()
    expect(w.find('[data-testid="erro-batalhas"]').attributes('role')).toBe('alert')
  })
})
