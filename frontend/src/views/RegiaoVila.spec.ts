import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))
vi.mock('vue-router', () => ({ useRoute: () => ({ params: { indice: '6' } }) }))

import { get, post } from '../api/http'
import RegiaoVila from './RegiaoVila.vue'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

const catalogo = [
  { tipo: 'CASA', nome: 'Casa', regioes: ['URBANA'], terreno: null, custoN1: { MADEIRA: 10 }, tamanho: 2, poN1: 5, profissoes: [] },
]

function montar() {
  return mount(RegiaoVila, {
    global: { plugins: [PrimeVue], stubs: { 'router-link': true } },
  })
}

describe('RegiaoVila', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    getMock.mockImplementation(async (url: string) => {
      if (url === '/api/jogo/regioes/6') {
        return {
          regiao: { id: 6, indice: 6, tipo: 'URBANA', possuida: true },
          ladrilhos: [{ x: 0, y: 0, terreno: 'DESENVOLVIMENTO', bonusBase: 10, bonusAdjacente: 0, bonusTotal: 10, construcao: { id: 3, tipo: 'CASA', nivel: 'N1', tamanho: 2, estado: 'EM_OBRA', poAtual: 2, poTotal: 5 } }],
        }
      }
      if (url === '/api/jogo/construcoes/catalogo') return catalogo
      if (url === '/api/jogo/estoque') return { recursos: [{ recurso: 'MADEIRA', nome: 'Madeira', quantidade: 100, capacidade: 500, percentualUsado: 20 }] }
      throw new Error('url inesperada ' + url)
    })
  })

  it('lista construções com estado e PO', async () => {
    const w = montar()
    await flushPromises()
    expect(w.get('[data-testid="predio-3"]').text()).toContain('Em obra')
    expect(w.get('[data-testid="po"]').text()).toContain('2/5')
  })

  it('cria construção ao escolher ladrilho vazio e tipo', async () => {
    postMock.mockResolvedValue({ id: 10 })
    const w = montar()
    await flushPromises()
    await w.get('[data-x="5"][data-y="5"]').trigger('click')
    await w.get('[data-testid="item-CASA"]').trigger('click')
    await w.get('[data-testid="construir"]').trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/construcoes', { tipo: 'CASA', regiaoIndice: 6, x: 5, y: 5 })
    expect(w.get('[data-testid="mensagem-construcao"]').text()).toBe('Construção iniciada')
  })

  it('mostra erro 409 do backend', async () => {
    postMock.mockRejectedValue(new Error('Recursos insuficientes'))
    const w = montar()
    await flushPromises()
    await w.get('[data-x="5"][data-y="5"]').trigger('click')
    await w.get('[data-testid="item-CASA"]').trigger('click')
    await w.get('[data-testid="construir"]').trigger('click')
    await flushPromises()
    expect(w.get('[data-testid="erro-construcao"]').text()).toBe('Recursos insuficientes')
  })

  it('clique em prédio abre menu e painéis', async () => {
    const w = montar()
    await flushPromises()
    await w.get('[data-x="0"][data-y="0"]').trigger('click')
    expect(w.find('[data-testid="menu-predio"]').exists()).toBe(true)
    await w.get('[data-testid="acao-upgrade"]').trigger('click')
    expect(w.find('[data-testid="UpgradeModal"]').exists()).toBe(true)
    await w.get('[data-testid="acao-marcacao"]').trigger('click')
    expect(w.find('[data-testid="PainelMarcacao"]').exists()).toBe(true)
  })
})
