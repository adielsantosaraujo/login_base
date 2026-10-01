import { computed, getCurrentInstance, onBeforeUnmount, ref } from 'vue'
import { get } from '../api/http'

export interface Turno {
  numero: number
  iniciadoEm: string | null
  proximoEm: string | null
  segundosRestantes: number
}

export type TipoEventoTurno =
  | 'PRODUCAO'
  | 'MORTE'
  | 'NASCIMENTO'
  | 'MASMORRA'
  | 'FERIDO'
  | 'RECUPERADO'
  | 'ESTOQUE_PERDIDO'
  | 'REGIAO_ANEXADA'
  | 'MERCADO_COMPRA'
  | 'MERCADO_VENDA'
  | 'IMPOSTO_COBRADO'
  | 'ESTALAGEM_RECEITA'
  | 'FALHA_PROCESSAMENTO'

export interface EventoTurno {
  id: number
  tipo: TipoEventoTurno
  mensagem: string
  dados: Record<string, unknown> | null
  timestamp: string
}

export interface RelatorioTurno {
  turno: number
  eventos: EventoTurno[]
}

export const ROTULOS_EVENTO: Record<string, string> = {
  PRODUCAO: 'Produção',
  MORTE: 'Morte',
  NASCIMENTO: 'Nascimento',
  MASMORRA: 'Masmorra',
  FERIDO: 'Ferido',
  RECUPERADO: 'Recuperado',
  ESTOQUE_PERDIDO: 'Estoque perdido',
  REGIAO_ANEXADA: 'Região anexada',
  MERCADO_COMPRA: 'Compra no mercado',
  MERCADO_VENDA: 'Venda no mercado',
  IMPOSTO_COBRADO: 'Imposto cobrado',
  ESTALAGEM_RECEITA: 'Receita da estalagem',
  FALHA_PROCESSAMENTO: 'Falha no processamento',
}

export const ICONES_EVENTO: Record<string, string> = {
  PRODUCAO: 'pi pi-box',
  MORTE: 'pi pi-times-circle',
  NASCIMENTO: 'pi pi-user-plus',
  MASMORRA: 'pi pi-bolt',
  FERIDO: 'pi pi-exclamation-triangle',
  RECUPERADO: 'pi pi-heart',
  ESTOQUE_PERDIDO: 'pi pi-trash',
  REGIAO_ANEXADA: 'pi pi-map',
  MERCADO_COMPRA: 'pi pi-shopping-cart',
  MERCADO_VENDA: 'pi pi-dollar',
  IMPOSTO_COBRADO: 'pi pi-wallet',
  ESTALAGEM_RECEITA: 'pi pi-home',
  FALHA_PROCESSAMENTO: 'pi pi-ban',
}

export function iconeEvento(tipo: string): string {
  return ICONES_EVENTO[tipo] ?? 'pi pi-info-circle'
}

export function rotuloEvento(tipo: string): string {
  return ROTULOS_EVENTO[tipo] ?? tipo
}

/** Formata segundos como HH:MM:SS (ou MM:SS quando < 1h). */
export function formatarContagem(segundos: number): string {
  const total = Math.max(0, Math.floor(segundos))
  const h = Math.floor(total / 3600)
  const m = Math.floor((total % 3600) / 60)
  const s = total % 60
  const p = (n: number) => String(n).padStart(2, '0')
  return h > 0 ? `${p(h)}:${p(m)}:${p(s)}` : `${p(m)}:${p(s)}`
}

export const INTERVALO_POLLING_MS = 10_000

export function useTurno(intervaloPollingMs = INTERVALO_POLLING_MS) {
  const turno = ref<Turno | null>(null)
  const segundosRestantes = ref(0)
  const relatorio = ref<RelatorioTurno | null>(null)
  const carregando = ref(false)
  const erro = ref<string | null>(null)
  const erroEventos = ref<string | null>(null)
  let tickTimer: ReturnType<typeof setInterval> | null = null
  let pollTimer: ReturnType<typeof setInterval> | null = null

  const eventos = computed(() => relatorio.value?.eventos ?? [])
  const turnoExibido = computed(() => relatorio.value?.turno ?? turno.value?.numero ?? 0)

  async function carregarTurno() {
    try {
      turno.value = await get<Turno>('/api/jogo/turno')
      segundosRestantes.value = turno.value.segundosRestantes
      erro.value = null
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar o turno'
    }
  }

  async function carregarEventos(numeroTurno?: number) {
    carregando.value = true
    erroEventos.value = null
    try {
      const url =
        numeroTurno === undefined ? '/api/jogo/turno/eventos' : `/api/jogo/turno/eventos?turno=${numeroTurno}`
      relatorio.value = await get<RelatorioTurno>(url)
    } catch (e) {
      erroEventos.value = e instanceof Error ? e.message : 'Erro ao carregar o relatório'
    } finally {
      carregando.value = false
    }
  }

  async function carregarTurnoAnterior() {
    const atual = relatorio.value?.turno ?? turno.value?.numero ?? 0
    if (atual <= 1) return
    await carregarEventos(atual - 1)
  }

  async function aoVirarTurno() {
    await carregarTurno()
    await carregarEventos()
  }

  function tick() {
    if (segundosRestantes.value > 0) {
      segundosRestantes.value -= 1
      if (segundosRestantes.value === 0) void aoVirarTurno()
    }
  }

  function iniciar() {
    parar()
    void carregarTurno()
    void carregarEventos()
    tickTimer = setInterval(tick, 1000)
    pollTimer = setInterval(() => void carregarTurno(), intervaloPollingMs)
  }

  function parar() {
    if (tickTimer) clearInterval(tickTimer)
    if (pollTimer) clearInterval(pollTimer)
    tickTimer = null
    pollTimer = null
  }

  if (getCurrentInstance()) onBeforeUnmount(parar)

  return {
    turno,
    segundosRestantes,
    relatorio,
    eventos,
    turnoExibido,
    carregando,
    erro,
    erroEventos,
    carregarTurno,
    carregarEventos,
    carregarTurnoAnterior,
    iniciar,
    parar,
  }
}
