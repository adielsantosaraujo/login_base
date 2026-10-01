import { beforeEach, describe, expect, it, vi } from 'vitest'

let falhar = false
vi.mock('../api/http', () => {
  const mock = vi.fn()
  return {
    get: (...args: unknown[]) => (falhar ? Promise.reject(new Error('falhou')) : mock(...args)),
    __mock: mock,
  }
})

import * as http from '../api/http'
import { proximoDoLimite, useEstoque } from './useEstoque'

const getMock = (http as unknown as { __mock: ReturnType<typeof vi.fn> }).__mock

describe('useEstoque', () => {
  beforeEach(() => {
    falhar = false
    getMock.mockReset()
  })

  it('carrega o estoque', async () => {
    getMock.mockResolvedValue({ recursos: [{ recurso: 'MADEIRA', nome: 'Madeira', quantidade: 1, capacidade: 500, percentualUsado: 0.2 }] })
    const e = useEstoque()
    await e.carregar()
    expect(getMock).toHaveBeenCalledWith('/api/jogo/estoque')
    expect(e.recursos.value).toHaveLength(1)
    expect(e.erro.value).toBeNull()
  })

  it('registra erro', async () => {
    falhar = true
    const e = useEstoque()
    await e.carregar()
    expect(e.erro.value).toBe('falhou')
  })

  it('detecta proximidade do limite', () => {
    const base = { recurso: 'X', nome: 'X', percentualUsado: 0 }
    expect(proximoDoLimite({ ...base, quantidade: 451, capacidade: 500 })).toBe(true)
    expect(proximoDoLimite({ ...base, quantidade: 450, capacidade: 500 })).toBe(false)
    expect(proximoDoLimite({ ...base, quantidade: 99999, capacidade: null })).toBe(false)
  })
})
