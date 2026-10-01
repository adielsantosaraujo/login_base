import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { get, post } from '../api/http'
import { elegivelParaCasamento, useFamilias } from './useFamilias'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

describe('useFamilias', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
  })

  it('carrega famílias e casas', async () => {
    getMock.mockImplementation(async (url: string) => (url.endsWith('familias') ? [{ id: 1 }] : [{ casaId: 2 }]) as never)
    const f = useFamilias()
    await f.carregar()
    expect(getMock).toHaveBeenCalledWith('/api/jogo/familias')
    expect(getMock).toHaveBeenCalledWith('/api/jogo/casas')
    expect(f.familias.value).toHaveLength(1)
    expect(f.casas.value).toHaveLength(1)
  })

  it('expõe erro ao falhar o carregamento', async () => {
    getMock.mockRejectedValue(new Error('falhou'))
    const f = useFamilias()
    await f.carregar()
    expect(f.erro.value).toBe('falhou')
    expect(f.carregando.value).toBe(false)
  })

  it('casa com POST', async () => {
    postMock.mockResolvedValue({ id: 5, sobrenome: 'Silva' })
    const f = useFamilias()
    const req = { cidadao1Id: 1, cidadao2Id: 2, casaId: 3, sobrenomeEscolhido: 'Silva' }
    const r = await f.casar(req)
    expect(postMock).toHaveBeenCalledWith('/api/jogo/casamento', req)
    expect(r?.id).toBe(5)
    expect(f.erro.value).toBeNull()
  })

  it('expõe {erro} do backend ao casar', async () => {
    postMock.mockRejectedValue(new Error('A casa escolhida não tem núcleo familiar livre'))
    const f = useFamilias()
    const r = await f.casar({ cidadao1Id: 1, cidadao2Id: 2, casaId: 3, sobrenomeEscolhido: 'X' })
    expect(r).toBeNull()
    expect(f.erro.value).toContain('núcleo')
    expect(f.enviando.value).toBe(false)
  })

  it('elegível: solteiro e 18+', () => {
    const base = { id: 1, nome: 'A', sexo: 'M' as const, conjugeId: null }
    expect(elegivelParaCasamento({ ...base, idadeAnos: 18, estadoCivil: 'SOLTEIRO' })).toBe(true)
    expect(elegivelParaCasamento({ ...base, idadeAnos: 17, estadoCivil: 'SOLTEIRO' })).toBe(false)
    expect(elegivelParaCasamento({ ...base, idadeAnos: 30, estadoCivil: 'CASADO' })).toBe(false)
  })
})
