import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { get, post } from '../api/http'
import Mapa from './Mapa.vue'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

let estoqueOuro = 500

const TIPOS_MOCK = ['FLORESTA', 'PLANICIE', 'URBANA', 'LITORAL', 'MONTANHA']
const BONUS_MOCK: Record<string, string[]> = {
  FLORESTA: ['FLORESTA', 'BARREIRO', 'PLANTACOES'],
  PLANICIE: ['PLANTACOES', 'CRIACOES', 'FLORESTA'],
  URBANA: ['INDUSTRIA', 'COMERCIO', 'DESENVOLVIMENTO'],
  LITORAL: ['SALINAS', 'ENXOFRE', 'MILITAR'],
  MONTANHA: ['ROCHA', 'FERRO', 'CARVAO'],
}
const tipoDe = (n: number) => (n === 6 ? 'URBANA' : n === 7 ? 'FLORESTA' : TIPOS_MOCK[n % 5])

const regioes = Array.from({ length: 16 }, (_, i) => ({
  indice: i + 1,
  tipo: tipoDe(i + 1),
  bonus: BONUS_MOCK[tipoDe(i + 1)].map((b, k) => ({ bonus: b, posicao: k + 1, valor: [40, 20, 10][k] })),
  possuida: i + 1 === 6 || i + 1 === 7,
  masmorraAtiva: i + 1 === 3,
  nivelMasmorra: i + 1 === 3 ? 2 : null,
  masmorraId: i + 1 === 3 ? 9 : null,
}))

function montar() {
  return mount(Mapa, { global: { plugins: [PrimeVue], stubs: { teleport: true } } })
}

describe('Mapa', () => {
  vi.stubGlobal('matchMedia', () => ({
    matches: false, addEventListener: vi.fn(), removeEventListener: vi.fn(), addListener: vi.fn(), removeListener: vi.fn(),
  }))
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    estoqueOuro = 500
    getMock.mockImplementation(async (url: string) => {
      if (url === '/api/jogo/vila/mapa') return { vila: { id: 1, nome: 'V', bonusRegiao: { FLORESTA: 50, COMERCIO: 20 } }, regioes }
      if (url === '/api/jogo/regioes/6') {
        return {
          regiao: { id: 6, indice: 6, tipo: 'URBANA', possuida: true, bonus: regioes[5].bonus },
          ladrilhos: [0, 2, 4, 6].map((x) => ({
            x, y: 0, jazida: null, construcao: { id: x, tipo: 'CASA', nivel: 'N1', tamanho: 2 },
          })),
        }
      }
      if (url === '/api/jogo/regioes/3') return { regiao: { id: null, indice: 3, tipo: null, possuida: false }, ladrilhos: [] }
      if (url === '/api/jogo/regioes/2/custo-anexacao') return { ouro: 100, madeira: 50, pedra: 20 }
      if (url === '/api/jogo/estoque') {
        return {
          recursos: [
            { recurso: 'OURO', nome: 'Ouro', quantidade: estoqueOuro, capacidade: null, percentualUsado: null },
            { recurso: 'MADEIRA', nome: 'Madeira', quantidade: 100, capacidade: 500, percentualUsado: 20 },
            { recurso: 'PEDRA', nome: 'Pedra', quantidade: 100, capacidade: 500, percentualUsado: 20 },
          ],
        }
      }
      return { regiao: { id: null, indice: 1, tipo: null, possuida: false }, ladrilhos: [] }
    })
  })

  it('renderiza 16 células, 2 possuídas e masmorra', async () => {
    const w = montar()
    await flushPromises()
    expect(w.text()).toContain('Mapa da vila')
    expect(w.findAll('.celula')).toHaveLength(16)
    expect(w.findAll('.nao-possuida')).toHaveLength(14)
    expect(w.find('.tipo-urbana').exists()).toBe(true)
    expect(w.find('[data-testid="masmorra"]').text()).toContain('N2')
    expect(w.find('[data-testid="masmorra"]').attributes('aria-label')).toBe('Masmorra nível 2')
    expect(w.find('[data-testid="regiao-3"]').classes()).toContain('com-masmorra')
  })

  it('exibe tipo v2 e os 3 bônus de cada região, inclusive não possuída', async () => {
    const w = montar()
    await flushPromises()
    const r6 = w.find('[data-testid="regiao-6"]')
    expect(r6.classes()).toContain('tipo-urbana')
    expect(r6.text()).toContain('Urbana')
    expect(r6.findAll('li')).toHaveLength(3)
    expect(r6.text()).toContain('Indústria 40%')
    const r1 = w.find('[data-testid="regiao-1"]')
    expect(r1.classes()).toContain('nao-possuida')
    expect(r1.classes()).toContain('tipo-planicie')
    expect(r1.findAll('li')).toHaveLength(3)
    expect(r1.attributes('style')).toContain('var(--vl-tipo-planicie)')
  })

  it('mostra o bônus total da vila', async () => {
    const w = montar()
    await flushPromises()
    const painel = w.find('[data-testid="bonus-vila"]')
    expect(painel.text()).toContain('Floresta +50%')
    expect(painel.text()).toContain('Comércio +20%')
    expect(painel.text()).not.toContain('Ferro')
  })

  it('abre a região possuída com 100 ladrilhos e volta', async () => {
    const w = montar()
    await flushPromises()
    await w.find('[data-testid="regiao-6"]').trigger('click')
    await flushPromises()
    expect(w.findAll('[data-testid="ladrilho"]')).toHaveLength(100)
    expect(w.findAll('.ladrilho-construcao')).toHaveLength(4)
    await w.find('[data-testid="voltar"]').trigger('click')
    expect(w.find('[data-testid="painel-regiao"]').exists()).toBe(false)
    expect(w.findAll('.celula')).toHaveLength(16)
  })

  it('mostra mensagem para região não possuída', async () => {
    const w = montar()
    await flushPromises()
    await w.find('[data-testid="regiao-1"]').trigger('click')
    await flushPromises()
    expect(w.find('[data-testid="regiao-nao-possuida"]').text()).toContain('Região não possuída')
    expect(w.find('[data-testid="grade-regiao"]').exists()).toBe(false)
  })

  it('destaca regiões anexáveis (adjacentes às possuídas)', async () => {
    const w = montar()
    await flushPromises()
    expect(w.find('[data-testid="regiao-2"]').classes()).toContain('anexavel')
    expect(w.find('[data-testid="regiao-1"]').classes()).not.toContain('anexavel')
    expect(w.find('[data-testid="regiao-6"]').classes()).not.toContain('anexavel')
  })

  it('abre o diálogo em região adjacente, mostra custo e anexa', async () => {
    postMock.mockResolvedValue({ regiao: { indice: 2, tipo: 'FLORESTA', possuida: true }, estoque: {}, custo: { ouro: 100, madeira: 50, pedra: 20 } })
    const w = montar()
    await flushPromises()
    await w.find('[data-testid="regiao-2"]').trigger('click')
    await flushPromises()
    expect(w.find('[data-testid="custo-ouro"]').text()).toContain('100')
    const botao = w.find('[data-testid="confirmar-anexacao"]')
    expect(botao.attributes('disabled')).toBeUndefined()
    await botao.trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/regioes/2/anexar')
    expect(w.find('[data-testid="mensagem-anexacao"]').text()).toContain('anexada')
  })

  it('desabilita Anexar com recursos insuficientes', async () => {
    estoqueOuro = 10
    const w = montar()
    await flushPromises()
    await w.find('[data-testid="regiao-2"]').trigger('click')
    await flushPromises()
    expect(w.find('[data-testid="recursos-insuficientes"]').text()).toContain('Recursos insuficientes')
    expect(w.find('[data-testid="confirmar-anexacao"]').attributes('disabled')).toBeDefined()
  })

  it('não abre o diálogo em região não adjacente', async () => {
    const w = montar()
    await flushPromises()
    await w.find('[data-testid="regiao-1"]').trigger('click')
    await flushPromises()
    expect(w.find('[data-testid="dialogo-anexacao"]').exists()).toBe(false)
  })

  it('região com masmorra não é anexável: mostra aviso e não abre o diálogo', async () => {
    const w = montar()
    await flushPromises()
    expect(w.find('[data-testid="regiao-3"]').classes()).not.toContain('anexavel')
    await w.find('[data-testid="regiao-3"]').trigger('click')
    await flushPromises()
    expect(w.find('[data-testid="regiao-masmorra"]').text()).toContain('Masmorra nível 2 — não pode ser anexada')
    expect(w.find('[data-testid="regiao-nao-possuida"]').exists()).toBe(false)
    expect(w.find('[data-testid="dialogo-anexacao"]').exists()).toBe(false)
  })
})
