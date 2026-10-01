import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { get, post } from '../../api/http'
import MercadoPanel from './MercadoPanel.vue'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

function preparar(mercadoAtivo: boolean) {
  getMock.mockImplementation(async (url: string) => {
    if (url.endsWith('/precos'))
      return {
        mercadoAtivo,
        peMelhorComerciante: 10,
        volumeMaximo: 20,
        volumeUsado: 5,
        volumeRestante: 15,
        precos: [{ recurso: 'MADEIRA', nome: 'Madeira', precoBase: 1, precoVenda: 0.9, precoCompra: 1.1 }],
      }
    if (url.endsWith('/ordens')) return []
    return { recursos: [{ recurso: 'MADEIRA', quantidade: 7 }, { recurso: 'OURO', quantidade: 50 }] }
  })
}

async function montar() {
  const w = mount(MercadoPanel, { global: { plugins: [PrimeVue] } })
  await flushPromises()
  return w
}

describe('MercadoPanel', () => {
  beforeEach(() => {
    vi.stubGlobal('matchMedia', () => ({
      matches: false,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      addListener: vi.fn(),
      removeListener: vi.fn(),
    }))
    getMock.mockReset()
    postMock.mockReset()
  })

  it('avisa quando não há Mercado ativo', async () => {
    preparar(false)
    const w = await montar()
    expect(w.find('[data-testid="sem-mercado"]').exists()).toBe(true)
  })

  it('mostra preços, estoque e volume', async () => {
    preparar(true)
    const w = await montar()
    expect(w.find('[data-testid="sem-mercado"]').exists()).toBe(false)
    expect(w.get('[data-testid="volume"]').text()).toContain('5 / 20')
    const tabela = w.get('[data-testid="tabela-precos"]').text()
    expect(tabela).toContain('Madeira')
    expect(tabela).toContain('7')
    expect(tabela).toContain('0.9')
    expect(w.get('[data-testid="enviar-ordem"]').attributes('disabled')).toBeDefined()
  })
})
