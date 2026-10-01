import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn() }))

import { get } from '../api/http'
import { formatarContagem, iconeEvento, useTurno } from './useTurno'

const getMock = vi.mocked(get)

function responder(segundos = 3) {
  getMock.mockImplementation(async (url: string) => {
    if (url.startsWith('/api/jogo/turno/eventos')) return { turno: 5, eventos: [] }
    return { numero: 5, iniciadoEm: 'a', proximoEm: 'b', segundosRestantes: segundos }
  })
}

describe('useTurno', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    getMock.mockReset()
  })
  afterEach(() => vi.useRealTimers())

  it('formata a contagem', () => {
    expect(formatarContagem(65)).toBe('01:05')
    expect(formatarContagem(3725)).toBe('01:02:05')
    expect(formatarContagem(-3)).toBe('00:00')
    expect(iconeEvento('XYZ')).toBe('pi pi-info-circle')
  })

  it('carrega o turno e faz contagem regressiva local', async () => {
    responder(30)
    const t = useTurno()
    t.iniciar()
    await vi.advanceTimersByTimeAsync(0)
    expect(t.turno.value?.numero).toBe(5)
    await vi.advanceTimersByTimeAsync(3000)
    expect(t.segundosRestantes.value).toBe(27)
    t.parar()
  })

  it('recarrega turno e eventos quando a contagem zera', async () => {
    responder(2)
    const t = useTurno()
    t.iniciar()
    await vi.advanceTimersByTimeAsync(0)
    getMock.mockClear()
    await vi.advanceTimersByTimeAsync(2000)
    const urls = getMock.mock.calls.map((c) => c[0])
    expect(urls).toContain('/api/jogo/turno')
    expect(urls).toContain('/api/jogo/turno/eventos')
    t.parar()
  })

  it('refaz a consulta do turno a cada 10 segundos', async () => {
    responder(1000)
    const t = useTurno()
    t.iniciar()
    await vi.advanceTimersByTimeAsync(0)
    getMock.mockClear()
    await vi.advanceTimersByTimeAsync(10_000)
    expect(getMock.mock.calls.filter((c) => c[0] === '/api/jogo/turno')).toHaveLength(1)
    t.parar()
  })

  it('carrega turno anterior', async () => {
    responder(100)
    const t = useTurno()
    await t.carregarTurno()
    await t.carregarEventos()
    getMock.mockClear()
    await t.carregarTurnoAnterior()
    expect(getMock).toHaveBeenCalledWith('/api/jogo/turno/eventos?turno=4')
  })

  it('registra erro de eventos', async () => {
    getMock.mockRejectedValue(new Error('x'))
    const t = useTurno()
    await t.carregarEventos()
    expect(t.erroEventos.value).toBe('x')
  })
})
