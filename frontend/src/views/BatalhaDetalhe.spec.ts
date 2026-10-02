import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn() }))
vi.mock('vue-router', () => ({ useRoute: () => ({ params: { id: '7' } }) }))

import { get } from '../api/http'
import BatalhaDetalhe from './BatalhaDetalhe.vue'

vi.stubGlobal('ResizeObserver', class { observe() {} unobserve() {} disconnect() {} })

const getMock = vi.mocked(get)
const g = { id: 1, lado: 'TROPA', nome: 'Guerreiro' }
const o = { id: 1, lado: 'INIMIGO', nome: 'Goblin' }
const detalhe = {
  id: 7, turno: 12, tropaId: 1, tropaNome: 'Alfa', masmorraId: 3, masmorraNivel: 2, regiaoIndice: 5,
  resultado: 'VITORIA', totalRodadas: 1, criadoEm: '2026-01-01T00:00:00Z',
  participantes: [
    { id: 1, lado: 'TROPA', nome: 'Guerreiro', pvMax: 79, pvFinal: 68 },
    { id: 1, lado: 'INIMIGO', nome: 'Goblin', pvMax: 40, pvFinal: 0 },
  ],
  rodadas: [{ numero: 1, acoes: [{ atacante: g, alvo: o, dano: 40, critico: false, pvAntes: 40, pvDepois: 0, abatido: true }] }],
  recompensas: null,
}
const montar = async () => {
  const w = mount(BatalhaDetalhe, { global: { plugins: [PrimeVue], stubs: { 'router-link': { template: '<a><slot /></a>', props: ['to'] } } } })
  await flushPromises()
  return w
}

describe('BatalhaDetalhe', () => {
  beforeEach(() => { getMock.mockReset() })

  it('mostra cabeçalho, participantes por lado e replay', async () => {
    getMock.mockResolvedValue(detalhe)
    const w = await montar()
    expect(getMock).toHaveBeenCalledWith('/api/jogo/batalhas/7')
    expect(w.find('[data-testid="resultado"]').text()).toBe('VITÓRIA')
    expect(w.find('[data-testid="total-rodadas"]').text()).toContain('1')
    expect(w.find('[data-testid="part-TROPA-1"]').text()).toContain('68/79')
    expect(w.find('[data-testid="part-INIMIGO-1"]').text()).toContain('0/40')
    expect(w.find('[data-testid="acao-1-0"]').text()).toContain('Guerreiro → Goblin: 40')
    expect(w.find('[data-testid="abatido"]').exists()).toBe(true)
  })

  it('recompensas null mostra "Sem recompensas"', async () => {
    getMock.mockResolvedValue(detalhe)
    const w = await montar()
    const abas = w.findAll('[role="tab"]')
    expect(abas.map((a) => a.text())).toEqual(['Rodada a rodada', 'Recompensas'])
    await abas[1]!.trigger('click')
    await flushPromises()
    expect(w.find('[data-testid="sem-recompensas"]').text()).toBe('Sem recompensas')
  })

  it('404 mostra mensagem de não encontrada com role=alert', async () => {
    getMock.mockRejectedValue(new Error('Batalha não encontrada'))
    const w = await montar()
    const e = w.find('[data-testid="erro-batalha"]')
    expect(e.attributes('role')).toBe('alert')
    expect(e.text()).toBe('Batalha não encontrada')
  })
})
