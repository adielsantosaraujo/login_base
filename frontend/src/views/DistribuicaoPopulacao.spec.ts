import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({
  get: vi.fn(),
  post: vi.fn(),
  ApiError: class ApiError extends Error {},
}))
vi.mock('../router/guardaVila', () => ({ marcarPopulacaoConfirmada: vi.fn() }))
const push = vi.fn()
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))

import { get, post } from '../api/http'
import { PLANO_PADRAO, distribuirPopulacao } from '../domain/populacao'
import DistribuicaoPopulacao from './DistribuicaoPopulacao.vue'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

const PAPEIS = ['PAI', 'MAE', 'FILHO', 'FILHA'] as const
const NOMES = ['Silva', 'Souza', 'Lima', 'Costa']

function resposta() {
  let id = 1
  const base = NOMES.map((s, i) => ({
    familiaId: i + 1,
    sobrenome: s,
    cidadaos: PAPEIS.map((papel) => ({
      cidadaoId: id++,
      nome: `${papel}-${s}`,
      sexo: 'M' as const,
      idadeAnos: papel === 'PAI' ? 40 : papel === 'MAE' ? 38 : 10,
      papel,
    })),
  }))
  return {
    populacaoConfirmada: false,
    plano: { ...PLANO_PADRAO },
    minimos: { CONSTRUTOR: 2, CARREGADOR: 2 },
    limites: { caracteristicasTotal: 20, profissoesTotal: 10 },
    familiaLiderSugeridaId: 1,
    familiaLiderId: null,
    familias: distribuirPopulacao(base, PLANO_PADRAO),
  }
}

async function montar(r = resposta()) {
  getMock.mockResolvedValue(r)
  const w = mount(DistribuicaoPopulacao)
  await flushPromises()
  return w
}

describe('DistribuicaoPopulacao', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    push.mockReset()
  })

  it('carrega e mostra famílias, cidadãos e líder sugerido', async () => {
    const w = await montar()
    expect(w.find('h1').text()).toBe('Distribuir a população')
    expect(w.findAll('[role="tab"]')).toHaveLength(4)
    expect(w.findAll('article')).toHaveLength(4)
    expect((w.get('[data-testid="lider-1"]').element as HTMLInputElement).checked).toBe(true)
    expect(w.get('[data-testid="aba-1"]').text()).toContain('LÍDER')
    expect(w.get('[data-testid="aba-1"]').text()).toContain('Todos os pontos usados')
  })

  it('não chama o servidor durante os ajustes', async () => {
    const w = await montar()
    await w.get('[data-testid="car-VIT"] button[aria-label^="Diminuir"]').trigger('click')
    expect(w.get('[data-testid="contador-car"]').text()).toBe('19 / 20')
    expect(getMock).toHaveBeenCalledTimes(1)
    expect(postMock).not.toHaveBeenCalled()
  })

  it('limite total: com 20 pontos os + ficam desabilitados', async () => {
    const w = await montar()
    expect(w.get('[data-testid="contador-car"]').text()).toBe('20 / 20')
    const mais = w.findAll('article')[0]!.findAll('[data-testid^="car-"] button[aria-label^="Aumentar"]')
    expect(mais).toHaveLength(5)
    mais.forEach((b) => expect(b.attributes('disabled')).toBeDefined())
  })

  it('atributo acima de 10 é aceito', async () => {
    const r = resposta()
    const c = r.familias[0]!.cidadaos[0]!
    c.caracteristicas = { VIT: 10, FOR: 0, VEL: 0, INT: 0, CAR: 2 }
    const w = await montar(r)
    await w.get('[data-testid="car-CAR"] button[aria-label^="Diminuir"]').trigger('click')
    await w.get('[data-testid="car-VIT"] button[aria-label^="Aumentar"]').trigger('click')
    expect(w.get('[data-testid="car-VIT"] [data-testid="stepper-valor"]').text()).toBe('11')
  })

  it('bônus R3 com explicação no title', async () => {
    const r = resposta()
    r.familias[0]!.cidadaos[0]!.caracteristicas = { VIT: 0, FOR: 8, VEL: 7, INT: 0, CAR: 5 }
    const w = await montar(r)
    const b = w.get('[data-testid="bonus-CARREGADOR"]')
    expect(b.text()).toBe('+2')
    expect(b.attributes('title')).toBe('Bônus R3: FOR 8÷5 + VEL 7÷5')
  })

  it('troca de principal ajusta o plano e a contagem', async () => {
    const w = await montar()
    // 1º cidadão do plano padrão é Comerciante; troca para Construtor
    expect(w.get('[data-testid="atual-CONSTRUTOR"]').text()).toBe('2')
    await w.get('[data-testid="principal"]').setValue('CONSTRUTOR')
    expect(w.get('[data-testid="atual-CONSTRUTOR"]').text()).toBe('3')
    expect(w.get('[data-testid="atual-COMERCIANTE"]').text()).toBe('0')
    expect(w.get('[data-testid="plano-total"]').text()).toBe('16 / 16')
  })

  it('plano diferente de 16 desabilita Redistribuir', async () => {
    const w = await montar()
    await w.get('[data-testid="plano-MINEIRO"] button[aria-label^="Diminuir"]').trigger('click')
    expect(w.get('[data-testid="plano-total"]').text()).toBe('15 / 16')
    const bt = w.get('[data-testid="redistribuir"]')
    expect(bt.attributes('disabled')).toBeDefined()
    expect(bt.attributes('title')).toBe('O plano precisa somar 16')
  })

  it('mínimos não atendidos: botão mostra "Ajuste os mínimos" e não envia', async () => {
    const w = await montar()
    // troca um Carregador para Mineiro -> 1 Carregador
    const selects = w.findAll('[data-testid="principal"]')
    const idx = selects.findIndex((s) => (s.element as HTMLSelectElement).value === 'CARREGADOR')
    await selects[idx]!.setValue('MINEIRO')
    const cta = w.get('[data-testid="confirmar"]')
    expect(cta.text()).toBe('Ajuste os mínimos')
    expect(cta.attributes('aria-disabled')).toBe('true')
    await cta.trigger('click')
    expect(postMock).not.toHaveBeenCalled()
  })

  it('confirma com pendências e navega para o mapa', async () => {
    postMock.mockResolvedValue({})
    const w = await montar()
    for (let i = 0; i < 5; i++)
      await w.get('[data-testid="car-CAR"] button[aria-label^="Diminuir"]').trigger('click')
    expect(w.text()).toContain('Pontos pendentes 5')
    const cta = w.get('[data-testid="confirmar"]')
    expect(cta.text()).toBe('Confirmar população')
    await cta.trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledTimes(1)
    expect(postMock.mock.calls[0]![1]).toMatchObject({ familiaLiderId: 1 })
    expect(push).toHaveBeenCalledWith('/jogo/mapa')
  })

  it('zerar tudo mantém plano e líder', async () => {
    const w = await montar()
    await w.get('[data-testid="zerar"]').trigger('click')
    expect(w.get('[data-testid="contador-car"]').text()).toBe('0 / 20')
    expect(w.get('[data-testid="plano-total"]').text()).toBe('16 / 16')
    expect((w.get('[data-testid="lider-1"]').element as HTMLInputElement).checked).toBe(true)
  })
})
