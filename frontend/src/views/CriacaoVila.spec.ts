import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', async (orig) => ({
  ...(await orig<typeof import('../api/http')>()),
  get: vi.fn(),
  post: vi.fn(),
}))
const push = vi.fn()
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))

import { ApiError, get, post } from '../api/http'
import { resetarGuardaVila } from '../router/guardaVila'
import type { PreviaMapa, RegiaoPrevia } from '../domain/regioes'
import CriacaoVila from './CriacaoVila.vue'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

const ESPECIAIS: Record<number, RegiaoPrevia> = {
  6: { indice: 6, tipo: 'URBANA', bonus: [
    { bonus: 'COMERCIO', posicao: 1, valor: 47 },
    { bonus: 'INDUSTRIA', posicao: 2, valor: 30 },
    { bonus: 'DESENVOLVIMENTO', posicao: 3, valor: 12 },
  ] },
  7: { indice: 7, tipo: 'LITORAL', bonus: [
    { bonus: 'SALINAS', posicao: 1, valor: 38 },
    { bonus: 'MILITAR', posicao: 2, valor: 20 },
    { bonus: 'ENXOFRE', posicao: 3, valor: 6 },
  ] },
  10: { indice: 10, tipo: 'PLANICIE', bonus: [
    { bonus: 'CRIACOES', posicao: 1, valor: 41 },
    { bonus: 'FLORESTA', posicao: 2, valor: 25 },
    { bonus: 'PLANTACOES', posicao: 3, valor: 14 },
  ] },
}

function previa(id = 'p1', rodada = 1): PreviaMapa {
  return {
    previaId: id,
    rodada,
    regioes: Array.from({ length: 16 }, (_, k) =>
      ESPECIAIS[k + 1] ?? {
        indice: k + 1,
        tipo: 'FLORESTA' as const,
        bonus: [{ bonus: 'FLORESTA' as const, posicao: 1, valor: 10 }],
      },
    ),
  }
}

async function montar() {
  const w = mount(CriacaoVila)
  await flushPromises()
  return w
}

const clicar = (w: Awaited<ReturnType<typeof montar>>, i: number) =>
  w.get(`[data-testid="regiao-${i}"]`).trigger('click')

describe('CriacaoVila', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    push.mockReset()
    resetarGuardaVila()
    getMock.mockResolvedValue(previa())
  })

  it('abre sem prévia: gera uma e mostra 16 regiões e Mapa nº 1', async () => {
    getMock.mockRejectedValue(new ApiError(404, 'sem prévia'))
    postMock.mockResolvedValue(previa('nova'))
    const w = await montar()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/vila/previa')
    expect(w.findAll('button.rt-tile')).toHaveLength(16)
    expect(w.get('[data-testid="rodada"]').text()).toBe('Mapa nº 1')
    expect(w.get('h1').text()).toBe('Criar minha vila')
  })

  it('região não vizinha fica esmaecida e ignora o clique', async () => {
    const w = await montar()
    await clicar(w, 1)
    expect(w.get('[data-testid="regiao-2"]').attributes('aria-disabled')).toBeUndefined()
    expect(w.get('[data-testid="regiao-5"]').attributes('aria-disabled')).toBeUndefined()
    expect(w.get('[data-testid="regiao-3"]').attributes('aria-disabled')).toBe('true')
    await clicar(w, 3)
    expect(w.get('[data-testid="regiao-3"]').attributes('aria-pressed')).toBe('false')
  })

  it('gerar novo mapa limpa a seleção e mostra a rodada seguinte', async () => {
    postMock.mockResolvedValue(previa('p2', 2))
    const w = await montar()
    await clicar(w, 1)
    await clicar(w, 2)
    await w.get('[data-testid="gerar"]').trigger('click')
    await flushPromises()
    expect(w.findAll('[aria-pressed="true"]')).toHaveLength(0)
    expect(w.get('[data-testid="rodada"]').text()).toBe('Mapa nº 2')
  })

  it('soma dos bônus no painel', async () => {
    const w = await montar()
    for (const i of [6, 7, 10]) await clicar(w, i)
    const valor = (b: string) => w.get(`[data-testid="bonus-${b}"]`).text()
    expect(valor('COMERCIO')).toContain('47')
    expect(valor('ENXOFRE')).toContain('6')
    expect(w.get('[data-testid="bonus-ROCHA"]').classes()).toContain('esmaecido')
  })

  it('seleção sem Urbana mostra dica e botão inválido', async () => {
    const w = await montar()
    for (const i of [7, 11, 10]) await clicar(w, i)
    expect(w.get('[data-testid="dica"]').text()).toBe('Inclua ao menos uma região Urbana.')
    const itens = w.findAll('[data-testid="checklist"] li')
    expect(itens[2].classes()).toContain('pendente')
    const botao = w.get('[data-testid="criar"]')
    expect(botao.text()).toBe('Selecione 3 regiões válidas')
    expect(botao.attributes('aria-disabled')).toBe('true')
  })

  it('criação com sucesso navega para a distribuição de população', async () => {
    postMock.mockResolvedValue({})
    const w = await montar()
    for (const i of [6, 7, 10]) await clicar(w, i)
    expect(w.get('[data-testid="criar"]').text()).toBe('Criar vila')
    await w.get('[data-testid="criar"]').trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/vila', { previaId: 'p1', indices: [6, 7, 10] })
    expect(push).toHaveBeenCalledWith('/jogo/distribuir-populacao')
  })

  it('prévia expirada mostra aviso, recarrega e limpa a seleção', async () => {
    getMock.mockResolvedValueOnce(previa('velha')).mockResolvedValueOnce(previa('atual', 3))
    postMock.mockRejectedValue(new ApiError(409, 'O mapa mudou. Escolha as regiões novamente', 'PREVIA_EXPIRADA'))
    const w = await montar()
    for (const i of [6, 7, 10]) await clicar(w, i)
    await w.get('[data-testid="criar"]').trigger('click')
    await flushPromises()
    expect(w.get('[data-testid="erro"]').text()).toContain('O mapa mudou')
    expect(w.findAll('[aria-pressed="true"]')).toHaveLength(0)
    expect(w.get('[data-testid="rodada"]').text()).toBe('Mapa nº 3')
    expect(push).not.toHaveBeenCalled()
  })

  it('hover mostra a região em foco e mouseleave limpa', async () => {
    const w = await montar()
    expect(w.text()).toContain('Passe o mouse numa região')
    await w.get('[data-testid="regiao-6"]').trigger('mouseenter')
    expect(w.get('[data-testid="foco-titulo"]').text()).toBe('Região 06 · Urbana')
    await w.get('[data-testid="regiao-6"]').trigger('mouseleave')
    expect(w.text()).toContain('Passe o mouse numa região')
  })
})
