import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Select from 'primevue/select'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../../api/http', () => ({ get: vi.fn(), post: vi.fn(), del: vi.fn() }))

import { del, get, post } from '../../api/http'
import PainelPredio from './PainelPredio.vue'

if (!window.matchMedia) {
  window.matchMedia = ((q: string) => ({ matches: false, media: q, addEventListener() {}, removeEventListener() {}, addListener() {}, removeListener() {} })) as never
}

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)
const delMock = vi.mocked(del)

function preparar(estado = 'ATIVA', tipo = 'ESTALAGEM') {
  getMock.mockImplementation(async (url: string) => {
    if (url.endsWith('/catalogo')) return [{ tipo, profissoes: ['COZINHEIRO', 'COMERCIANTE'] }]
    if (url.endsWith('/alocacoes'))
      return [{ cidadaoId: 1, nome: 'Ana', idadeAnos: 20, profissao: 'COZINHEIRO', eficiencia: 1.25 }]
    if (url.includes('/cidadaos')) return [{ id: 2, nome: 'Bia', idadeAnos: 30, construcaoId: null }]
    return { id: 9, tipo, nivel: 'N1', estado, x: 1, y: 1, tamanho: 1, regiaoIndice: 1, poTotal: 10, poAtual: 4 }
  })
}

async function montar() {
  const w = mount(PainelPredio, {
    props: { construcaoId: 9 },
    global: { plugins: [PrimeVue], stubs: { 'router-link': { template: '<a><slot /></a>', props: ['to'] } } },
  })
  await flushPromises()
  return w
}

describe('PainelPredio', () => {
  beforeEach(() => {
    getMock.mockReset(); postMock.mockReset(); delMock.mockReset()
  })

  it('mostra vagas e trabalhadores com eficiência', async () => {
    preparar()
    const w = await montar()
    expect(w.get('[data-testid="vagas"]').text()).toContain('1/2')
    expect(w.get('[data-testid="alocado-1"]').text()).toContain('1,25')
  })

  it('em obra mostra progresso e limite 2', async () => {
    preparar('EM_OBRA')
    const w = await montar()
    expect(w.get('[data-testid="progresso"]').text()).toContain('4/10')
    expect(w.find('[data-testid="aviso-obra"]').exists()).toBe(true)
  })

  it('desalocar chama DELETE e emite atualizado', async () => {
    preparar()
    delMock.mockResolvedValue(undefined)
    const w = await montar()
    await w.get('[data-testid="desalocar-1"]').trigger('click')
    await flushPromises()
    expect(delMock).toHaveBeenCalledWith('/api/jogo/construcoes/9/alocacoes/1')
    expect(w.emitted('atualizado')).toBeTruthy()
  })

  it('aloca exigindo profissão quando há mais de uma', async () => {
    preparar()
    postMock.mockResolvedValue({})
    const w = await montar()
    const selects = w.findAllComponents(Select)
    expect(selects).toHaveLength(2)
    expect(w.get('[data-testid="alocar"]').attributes('disabled')).toBeDefined()
    selects[0].vm.$emit('update:modelValue', 2)
    await flushPromises()
    expect(w.get('[data-testid="alocar"]').attributes('disabled')).toBeDefined()
    selects[1].vm.$emit('update:modelValue', 'COMERCIANTE')
    await flushPromises()
    await w.get('[data-testid="alocar"]').trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/construcoes/9/alocacoes', { cidadaoId: 2, profissao: 'COMERCIANTE' })
    expect(w.emitted('atualizado')).toBeTruthy()
  })

  it('exibe erro do backend', async () => {
    preparar()
    postMock.mockRejectedValue(new Error('Cidadão já alocado'))
    const w = await montar()
    const selects = w.findAllComponents(Select)
    selects[0].vm.$emit('update:modelValue', 2)
    selects[1].vm.$emit('update:modelValue', 'COZINHEIRO')
    await flushPromises()
    await w.get('[data-testid="alocar"]').trigger('click')
    await flushPromises()
    expect(w.get('[data-testid="erro-alocacao"]').text()).toBe('Cidadão já alocado')
  })
})
