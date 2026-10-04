import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn() }))

import { get } from '../api/http'
import { guardaVila, marcarPopulacaoConfirmada, resetarGuardaVila } from './guardaVila'

const getMock = vi.mocked(get)
const erro404 = Object.assign(new Error('Vila não encontrada'), { status: 404 })

function ir(path: string) {
  return (guardaVila as unknown as (to: { path: string }) => Promise<unknown>)({ path })
}

describe('guardaVila', () => {
  beforeEach(() => {
    getMock.mockReset()
    resetarGuardaVila()
  })

  it('deixa passar rotas fora de /jogo sem consultar', async () => {
    expect(await ir('/')).toBe(true)
    expect(getMock).not.toHaveBeenCalled()
  })

  it('sem vila redireciona rotas do jogo para criar-vila', async () => {
    getMock.mockRejectedValue(erro404)
    expect(await ir('/jogo/mapa')).toBe('/jogo/criar-vila')
  })

  it('sem vila permite criar-vila', async () => {
    getMock.mockRejectedValue(erro404)
    expect(await ir('/jogo/criar-vila')).toBe(true)
  })

  it('com vila permite o mapa e redireciona criar-vila para o mapa', async () => {
    getMock.mockResolvedValue({ vilaId: 1 })
    expect(await ir('/jogo/mapa')).toBe(true)
    expect(await ir('/jogo/criar-vila')).toBe('/jogo/mapa')
  })

  it('população não confirmada leva qualquer rota do jogo para /jogo/distribuir-populacao', async () => {
    getMock.mockResolvedValue({ vilaId: 1, populacaoConfirmada: false })
    expect(await ir('/jogo/mapa')).toBe('/jogo/distribuir-populacao')
    expect(await ir('/jogo/criar-vila')).toBe('/jogo/distribuir-populacao')
    expect(await ir('/jogo/distribuir-populacao')).toBe(true)
  })

  it('população confirmada libera o mapa e após confirmar o cache é atualizado', async () => {
    getMock.mockResolvedValue({ vilaId: 1, populacaoConfirmada: false })
    expect(await ir('/jogo/mapa')).toBe('/jogo/distribuir-populacao')
    marcarPopulacaoConfirmada()
    expect(await ir('/jogo/mapa')).toBe(true)
  })

  it('erro 500/rede não é tratado como sem vila nem fica em cache', async () => {
    getMock.mockRejectedValueOnce(Object.assign(new Error('falha'), { status: 500 }))
    expect(await ir('/jogo/mapa')).toBe(true)
    getMock.mockRejectedValueOnce(new Error('rede'))
    expect(await ir('/jogo/mapa')).toBe(true)
    getMock.mockRejectedValueOnce(erro404)
    expect(await ir('/jogo/mapa')).toBe('/jogo/criar-vila')
    expect(getMock).toHaveBeenCalledTimes(3)
  })
})
