import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn(), del: vi.fn() }))

import { del, get, post } from '../api/http'
import { formatarXp, useQuartel } from './useQuartel'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)
const delMock = vi.mocked(del)

const quartel = {
  id: 4, nivel: 'N1', estado: 'ATIVA', instrutores: 1, vagasInstrutor: 1, capacidade: 5, membrosAtuais: 0, maxTropas: 1, tropas: [],
}
const disp = [{ id: 7, nome: 'Ana', idadeAnos: 20, peGuerreiro: 1, arma: 'Espada', estado: 'SAUDAVEL', elegivel: true, motivo: null }]

describe('useQuartel', () => {
  beforeEach(() => {
    getMock.mockReset(); postMock.mockReset(); delMock.mockReset()
    getMock.mockImplementation(async (url: string) => (url.endsWith('/guerreiros-disponiveis') ? disp : quartel))
  })

  it('formata XP', () => {
    expect(formatarXp(1.5)).toBe('1,50')
  })

  it('carrega quartel e guerreiros disponíveis', async () => {
    const q = useQuartel()
    await q.carregar(4)
    expect(getMock).toHaveBeenCalledWith('/api/jogo/quarteis/4')
    expect(getMock).toHaveBeenCalledWith('/api/jogo/quarteis/4/guerreiros-disponiveis')
    expect(q.quartel.value?.capacidade).toBe(5)
    expect(q.disponiveis.value).toHaveLength(1)
  })

  it('expõe o erro da API ao carregar', async () => {
    getMock.mockRejectedValue(new Error('Quartel não encontrado'))
    const q = useQuartel()
    await q.carregar(4)
    expect(q.erro.value).toBe('Quartel não encontrado')
  })

  it('forma tropa, adiciona e remove membro, desfaz', async () => {
    postMock.mockResolvedValue({ id: 9 })
    delMock.mockResolvedValue({ id: 9 })
    const q = useQuartel()
    expect(await q.formarTropa(4, { nome: 'A', membros: [{ cidadaoId: 7, posicao: 'FRENTE' }] })).toEqual({ id: 9 })
    expect(postMock).toHaveBeenCalledWith('/api/jogo/quarteis/4/tropas', { nome: 'A', membros: [{ cidadaoId: 7, posicao: 'FRENTE' }] })
    await q.adicionarMembro(4, 9, { cidadaoId: 8, posicao: 'RETAGUARDA' })
    expect(postMock).toHaveBeenCalledWith('/api/jogo/tropas/9/membros', { cidadaoId: 8, posicao: 'RETAGUARDA' })
    await q.removerMembro(4, 9, 8)
    expect(delMock).toHaveBeenCalledWith('/api/jogo/tropas/9/membros/8')
    expect(await q.desfazerTropa(4, 9)).toBe(true)
    expect(delMock).toHaveBeenCalledWith('/api/jogo/tropas/9')
    expect(q.quartel.value).not.toBeNull()
  })

  it('retorna null/false e guarda o erro quando a API recusa', async () => {
    postMock.mockRejectedValue(new Error('Capacidade do quartel excedida'))
    delMock.mockRejectedValue(new Error('Tropa em expedição não pode ser alterada'))
    const q = useQuartel()
    expect(await q.formarTropa(4, { nome: 'A', membros: [] })).toBeNull()
    expect(q.erro.value).toBe('Capacidade do quartel excedida')
    expect(await q.desfazerTropa(4, 9)).toBe(false)
    expect(q.erro.value).toBe('Tropa em expedição não pode ser alterada')
  })

  it('busca destinos e envia expedição', async () => {
    const destinos = [{ masmorraId: 10, regiao: 3, nivel: 1, turnosViagem: 2, comidaNecessaria: 8, comidaDisponivel: 20 }]
    getMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/destinos')) return destinos
      return url.endsWith('/guerreiros-disponiveis') ? disp : quartel
    })
    postMock.mockResolvedValue({ id: 9, estado: 'EM_VIAGEM_IDA' })
    const q = useQuartel()
    expect(await q.buscarDestinos(9)).toEqual(destinos)
    expect(getMock).toHaveBeenCalledWith('/api/jogo/tropas/9/destinos')
    expect(await q.enviarExpedicao(4, 9, 10)).toEqual({ id: 9, estado: 'EM_VIAGEM_IDA' })
    expect(postMock).toHaveBeenCalledWith('/api/jogo/tropas/9/expedicao', { masmorraId: 10 })
    expect(getMock).toHaveBeenCalledWith('/api/jogo/quarteis/4')
  })

  it('guarda o erro da API em destinos e expedição', async () => {
    getMock.mockRejectedValue(new Error('Tropa não encontrada'))
    const q = useQuartel()
    expect(await q.buscarDestinos(9)).toBeNull()
    expect(q.erro.value).toBe('Tropa não encontrada')
    postMock.mockRejectedValue(new Error('Comida insuficiente para a expedição'))
    expect(await q.enviarExpedicao(4, 9, 10)).toBeNull()
    expect(q.erro.value).toBe('Comida insuficiente para a expedição')
  })
})
