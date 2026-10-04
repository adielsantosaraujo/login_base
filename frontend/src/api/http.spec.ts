import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError, get, post } from './http'

function responder(status: number, corpo?: unknown) {
  const texto = corpo === undefined ? '' : JSON.stringify(corpo)
  return {
    status,
    ok: status >= 200 && status < 300,
    text: () => Promise.resolve(texto),
  } as Response
}

async function capturar(p: Promise<unknown>): Promise<unknown> {
  try {
    await p
  } catch (e) {
    return e
  }
  throw new Error('não rejeitou')
}

describe('http ApiError', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('404 sem corpo gera ApiError status 404', async () => {
    vi.mocked(fetch).mockResolvedValue(responder(404))
    const e = (await capturar(get('/x'))) as ApiError
    expect(e).toBeInstanceOf(ApiError)
    expect(e).toBeInstanceOf(Error)
    expect(e.status).toBe(404)
    expect(e.message).toBe('Erro 404')
    expect(e.codigo).toBeUndefined()
  })

  it('extrai erro e codigo do corpo', async () => {
    vi.mocked(fetch).mockResolvedValue(responder(409, { erro: 'Conflito', codigo: 'NOME_EM_USO' }))
    const e = (await capturar(post('/x', {}))) as ApiError
    expect(e.message).toBe('Conflito')
    expect(e.codigo).toBe('NOME_EM_USO')
    expect(e.status).toBe(409)
  })

  it('aceita mensagem como fallback', async () => {
    vi.mocked(fetch).mockResolvedValue(responder(400, { mensagem: 'Inválido' }))
    const e = (await capturar(get('/x'))) as ApiError
    expect(e.message).toBe('Inválido')
  })

  it('sem corpo 500 gera "Erro 500"', async () => {
    vi.mocked(fetch).mockResolvedValue(responder(500))
    const e = (await capturar(get('/x'))) as ApiError
    expect(e.message).toBe('Erro 500')
  })

  it('401 redireciona para /login e não é ApiError', async () => {
    const loc = { href: '' }
    vi.stubGlobal('location', loc)
    vi.mocked(fetch).mockResolvedValue(responder(401))
    const e = await capturar(get('/x'))
    expect(loc.href).toBe('/login')
    expect(e).toBeInstanceOf(Error)
    expect(e).not.toBeInstanceOf(ApiError)
  })

  it('sucesso devolve o corpo', async () => {
    vi.mocked(fetch).mockResolvedValue(responder(200, { a: 1 }))
    expect(await get('/x')).toEqual({ a: 1 })
  })
})
