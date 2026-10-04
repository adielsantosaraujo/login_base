import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', async (orig) => ({
  ...(await orig<typeof import('../api/http')>()),
  get: vi.fn(),
  post: vi.fn(),
}))
const push = vi.fn()
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))

import { ApiError, get, post } from '../api/http'
import { guardaVila, resetarGuardaVila } from '../router/guardaVila'
import type { PreviaMapa } from '../domain/regioes'
import { useCriacaoVila } from './useVila'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

function previa(id = 'p1', rodada = 1): PreviaMapa {
  const tipos = ['FLORESTA', 'PLANICIE', 'URBANA', 'LITORAL'] as const
  return {
    previaId: id,
    rodada,
    regioes: Array.from({ length: 16 }, (_, k) => ({
      indice: k + 1,
      tipo: tipos[k % 4],
      bonus: [{ bonus: 'FLORESTA' as const, posicao: 1, valor: 10 }],
    })),
  }
}

describe('useCriacaoVila', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    push.mockReset()
    resetarGuardaVila()
  })

  it('carrega a prévia por GET', async () => {
    getMock.mockResolvedValue(previa())
    const c = useCriacaoVila()
    await c.carregarPrevia()
    expect(getMock).toHaveBeenCalledWith('/api/jogo/vila/previa')
    expect(postMock).not.toHaveBeenCalled()
    expect(c.previa.value?.previaId).toBe('p1')
    expect(c.gerando.value).toBe(false)
  })

  it('GET 404 gera a prévia por POST', async () => {
    getMock.mockRejectedValue(new ApiError(404, 'sem prévia'))
    postMock.mockResolvedValue(previa('nova'))
    const c = useCriacaoVila()
    await c.carregarPrevia()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/vila/previa')
    expect(c.previa.value?.previaId).toBe('nova')
    expect(c.erro.value).toBeNull()
  })

  it('expõe erro ao carregar a prévia', async () => {
    getMock.mockRejectedValue(new ApiError(500, 'Erro 500'))
    const c = useCriacaoVila()
    await c.carregarPrevia()
    expect(c.erro.value).toBe('Erro 500')
  })

  it('alterna a seleção respeitando vizinhança e limite de 3', async () => {
    getMock.mockResolvedValue(previa())
    const c = useCriacaoVila()
    await c.carregarPrevia()
    c.alternar(1)
    c.alternar(3)
    expect(c.selecionadas.value).toEqual([1])
    c.alternar(2)
    c.alternar(6)
    c.alternar(7)
    expect(c.selecionadas.value).toEqual([1, 2, 6])
    expect(c.podeSelecionar(7)).toBe(false)
    c.alternar(2)
    expect(c.selecionadas.value).toEqual([1, 6])
  })

  it('deriva conectado, totais, dica e foco', async () => {
    getMock.mockResolvedValue(previa())
    const c = useCriacaoVila()
    await c.carregarPrevia()
    expect(c.dicaSelecao.value).toBe('Clique em uma região para começar.')
    c.alternar(1)
    c.alternar(2)
    expect(c.conectado.value).toBe(true)
    expect(c.totaisBonus.value.FLORESTA).toBe(20)
    expect(c.emFoco.value).toBe(2)
    c.setHover(9)
    expect(c.emFoco.value).toBe(9)
    c.limparHover()
    expect(c.hover.value).toBeNull()
    expect(c.emFoco.value).toBe(2)
  })

  it('gerar() troca a prévia e limpa a seleção', async () => {
    getMock.mockResolvedValue(previa())
    postMock.mockResolvedValue(previa('p2', 2))
    const c = useCriacaoVila()
    await c.carregarPrevia()
    c.alternar(1)
    await c.gerar()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/vila/previa')
    expect(c.previa.value?.previaId).toBe('p2')
    expect(c.selecionadas.value).toEqual([])
  })

  it('criar com sucesso marca a guarda e navega para a população', async () => {
    getMock.mockResolvedValue(previa())
    postMock.mockResolvedValue({})
    const c = useCriacaoVila()
    await c.carregarPrevia()
    c.alternar(2); c.alternar(3); c.alternar(7)
    expect(await c.criar()).toBe(true)
    expect(postMock).toHaveBeenCalledWith('/api/jogo/vila', { previaId: 'p1', indices: [2, 3, 7] })
    expect(push).toHaveBeenCalledWith('/jogo/distribuir-populacao')
    // guarda em "pendente": cache sem nova consulta
    getMock.mockClear()
    expect(await guardaVila({ path: '/jogo/mapa' } as never, {} as never, () => {})).toBe('/jogo/distribuir-populacao')
    expect(getMock).not.toHaveBeenCalled()
    expect(c.enviando.value).toBe(false)
  })

  it('PREVIA_EXPIRADA recarrega a prévia e limpa a seleção', async () => {
    getMock.mockResolvedValueOnce(previa('velha')).mockResolvedValueOnce(previa('atual'))
    postMock.mockRejectedValue(new ApiError(409, 'O mapa mudou. Escolha as regiões novamente', 'PREVIA_EXPIRADA'))
    const c = useCriacaoVila()
    await c.carregarPrevia()
    c.alternar(2); c.alternar(3); c.alternar(7)
    expect(await c.criar()).toBe(false)
    expect(c.selecionadas.value).toEqual([])
    expect(c.previa.value?.previaId).toBe('atual')
    expect(c.erro.value).toBe('O mapa mudou. Escolha as regiões novamente')
    expect(push).not.toHaveBeenCalled()
  })

  it('VILA_JA_EXISTE reseta a guarda e vai para o mapa', async () => {
    getMock.mockResolvedValue(previa())
    postMock.mockRejectedValue(new ApiError(409, 'Vila já existe', 'VILA_JA_EXISTE'))
    const c = useCriacaoVila()
    await c.carregarPrevia()
    c.alternar(2)
    expect(await c.criar()).toBe(false)
    expect(push).toHaveBeenCalledWith('/jogo/mapa')
  })

  it('outros erros mostram a mensagem', async () => {
    getMock.mockResolvedValue(previa())
    postMock.mockRejectedValue(new ApiError(400, 'Seleção inválida', 'SELECAO_INVALIDA'))
    const c = useCriacaoVila()
    await c.carregarPrevia()
    c.alternar(2)
    expect(await c.criar()).toBe(false)
    expect(c.erro.value).toBe('Seleção inválida')
    expect(push).not.toHaveBeenCalled()
  })
})
