import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn(), del: vi.fn() }))

import { del, get, post } from '../api/http'
import { limiteVagas, useAlocacoes } from './useAlocacoes'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)
const delMock = vi.mocked(del)

describe('limiteVagas', () => {
  it('ativa 2/5/10 e obra 2/4/6', () => {
    expect(['N1', 'N2', 'N3'].map((n) => limiteVagas('ATIVA', n))).toEqual([2, 5, 10])
    expect(['N1', 'N2', 'N3'].map((n) => limiteVagas('EM_OBRA', n))).toEqual([2, 4, 6])
    expect(limiteVagas('EM_UPGRADE', 'N3')).toBe(6)
  })
})

describe('useAlocacoes', () => {
  beforeEach(() => {
    getMock.mockReset(); postMock.mockReset(); delMock.mockReset()
    getMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/catalogo')) return [{ tipo: 'ESTALAGEM', profissoes: ['COZINHEIRO', 'COMERCIANTE'] }]
      if (url.endsWith('/alocacoes')) return [{ cidadaoId: 1, nome: 'A', idadeAnos: 20, profissao: 'COZINHEIRO', eficiencia: 1 }]
      if (url.includes('/cidadaos')) return [{ id: 2, nome: 'B', idadeAnos: 30, construcaoId: null }, { id: 1, nome: 'A', idadeAnos: 20, construcaoId: 9 }]
      return { id: 9, tipo: 'ESTALAGEM', nivel: 'N1', estado: 'ATIVA', x: 0, y: 0, tamanho: 1, regiaoIndice: 1, poTotal: 1, poAtual: 1 }
    })
  })

  it('carrega prédio, vagas, profissões e disponíveis', async () => {
    const a = useAlocacoes()
    await a.carregar(9)
    expect(a.limite.value).toBe(2)
    expect(a.vagasLivres.value).toBe(1)
    expect(a.profissoesPermitidas.value).toEqual(['COZINHEIRO', 'COMERCIANTE'])
    expect(a.disponiveis.value.map((c) => c.id)).toEqual([2])
  })

  it('alocar envia POST e erro fica em erro', async () => {
    const a = useAlocacoes()
    postMock.mockResolvedValueOnce({})
    expect(await a.alocar(9, 2, 'COZINHEIRO')).toBe(true)
    expect(postMock).toHaveBeenCalledWith('/api/jogo/construcoes/9/alocacoes', { cidadaoId: 2, profissao: 'COZINHEIRO' })
    postMock.mockRejectedValueOnce(new Error('Sem vagas'))
    expect(await a.alocar(9, 2)).toBe(false)
    expect(a.erro.value).toBe('Sem vagas')
  })

  it('desalocar envia DELETE', async () => {
    const a = useAlocacoes()
    delMock.mockResolvedValueOnce(undefined)
    expect(await a.desalocar(9, 1)).toBe(true)
    expect(delMock).toHaveBeenCalledWith('/api/jogo/construcoes/9/alocacoes/1')
  })
})
