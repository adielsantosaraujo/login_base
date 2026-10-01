import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive } from 'vue'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))
const rota = reactive({ params: { id: '1' } })
vi.mock('vue-router', () => ({ useRoute: () => rota }))

import { get, post } from '../api/http'
import PainelCidadao from './PainelCidadao.vue'

vi.stubGlobal('ResizeObserver', class { observe() {} unobserve() {} disconnect() {} })

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

const base = {
  id: 1, nome: 'Ana Silva', sexo: 'F', idadeAnos: 30, vivo: true, estado: 'SAUDAVEL', faminto: true,
  familiaId: 2, familiaNome: 'Silva', conjuge: { id: 9, nome: 'Rui' },
  caracteristicas: { VIT: 5, FOR: 4, VEL: 3, INT: 2, CAR: 1 },
  pontosCarPendentes: 3, pontosProfPendentes: 0,
  profissoes: [{ profissao: 'CONSTRUTOR', peBase: 2, peEfetivo: 3, eficiencia: 1.2 }],
  construcaoId: 5, profissaoTrabalho: 'CONSTRUTOR', equipamento: {},
}

function montar() {
  return mount(PainelCidadao, {
    global: { plugins: [PrimeVue], stubs: { 'router-link': { props: ['to'], template: '<a :href="to"><slot /></a>' } } },
  })
}

describe('PainelCidadao', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    rota.params.id = '1'
  })

  it('mostra cabeçalho com links', async () => {
    getMock.mockResolvedValue(base as never)
    const w = montar()
    await flushPromises()
    expect(getMock).toHaveBeenCalledWith('/api/jogo/cidadao/1')
    expect(w.get('[data-testid="cabecalho"]').text()).toContain('Ana Silva')
    expect(w.find('[data-testid="faminto"]').exists()).toBe(true)
    expect(w.get('[data-testid="familia"] a').attributes('href')).toBe('/jogo/familias')
    expect(w.get('[data-testid="conjuge"] a').attributes('href')).toBe('/jogo/cidadao/9')
    expect(w.get('[data-testid="alocacao"]').text()).toContain('#5')
  })

  it('distribui pontos de característica e atualiza', async () => {
    getMock.mockResolvedValue(base as never)
    postMock.mockResolvedValue({ ...base, pontosCarPendentes: 1, caracteristicas: { ...base.caracteristicas, VIT: 7 } } as never)
    const w = montar()
    await flushPromises()
    expect(w.get('[data-testid="confirmar-car"]').attributes('disabled')).toBeDefined()
    const input = w.get('[data-testid="inc-VIT"]')
    await input.setValue('2')
    await input.trigger('blur')
    await flushPromises()
    expect(w.get('[data-testid="contador-car"]').text()).toContain('2 de 3')
    await w.get('[data-testid="confirmar-car"]').trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/cidadao/1/distribuir-pontos', { caracteristicas: { VIT: 2 } })
    expect(w.get('[data-testid="car-VIT"] [data-testid="valor"]').text()).toBe('7')
  })

  it('mostra erro do backend', async () => {
    getMock.mockResolvedValue(base as never)
    postMock.mockRejectedValue(new Error('Pontos inválidos'))
    const w = montar()
    await flushPromises()
    const input = w.get('[data-testid="inc-VIT"]')
    await input.setValue('1')
    await input.trigger('blur')
    await flushPromises()
    await w.get('[data-testid="confirmar-car"]').trigger('click')
    await flushPromises()
    expect(w.get('[data-testid="erro"]').text()).toBe('Pontos inválidos')
  })

  it('recarrega ao mudar o id da rota', async () => {
    getMock.mockResolvedValue(base as never)
    montar()
    await flushPromises()
    rota.params.id = '9'
    await flushPromises()
    expect(getMock).toHaveBeenLastCalledWith('/api/jogo/cidadao/9')
  })
})
