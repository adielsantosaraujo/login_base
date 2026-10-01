import { computed, ref } from 'vue'
import { del, get, post } from '../api/http'
import type { Ladrilho, RegiaoDetalhe } from './useMapa'

export interface Marcacao {
  id: number
  construcaoId: number
  x: number
  y: number
}

export interface PredioMarcacao {
  id: number
  tipo: string
  nivel: string
  regiaoIndice: number
  x: number
  y: number
  tamanho: number
  estado: string
}

/** Jazida exigida por cada prédio de coleta. */
export const JAZIDA_POR_TIPO: Record<string, string> = {
  ACAMPAMENTO_LENHADORES: 'FLORESTA',
  CABANA_CACA: 'FLORESTA',
  PEDREIRA: 'ROCHA',
  BARREIRO: 'BARREIRO',
  MINA_FERRO: 'VEIO_DE_FERRO',
  MINA_CARVAO: 'VEIO_DE_CARVAO',
  SALINA: 'SALINA',
  MINA_ENXOFRE: 'ENXOFRE',
}

export function limiteMarcacoes(nivel: string | undefined): number {
  return nivel === 'N3' ? 20 : nivel === 'N2' ? 10 : 4
}

const chave = (x: number, y: number) => `${x},${y}`

export function useMarcacoes() {
  const predio = ref<PredioMarcacao | null>(null)
  const marcacoes = ref<Marcacao[]>([])
  const ladrilhos = ref<Ladrilho[]>([])
  const carregando = ref(false)
  const salvando = ref(false)
  const erro = ref<string | null>(null)

  const maximo = computed(() => limiteMarcacoes(predio.value?.nivel))
  const usado = computed(() => marcacoes.value.length)
  const jazida = computed(() => (predio.value ? (JAZIDA_POR_TIPO[predio.value.tipo] ?? null) : null))

  function ocupadoPorPredio(x: number, y: number): boolean {
    const p = predio.value
    return !!p && x >= p.x && x < p.x + p.tamanho && y >= p.y && y < p.y + p.tamanho
  }

  /** Ladrilhos com a jazida do prédio, livres e ortogonalmente adjacentes ao prédio/marcados. */
  const candidatos = computed(() => {
    const p = predio.value
    if (!p || !jazida.value) return []
    const conectados = new Set<string>()
    for (let dx = 0; dx < p.tamanho; dx++)
      for (let dy = 0; dy < p.tamanho; dy++) conectados.add(chave(p.x + dx, p.y + dy))
    for (const m of marcacoes.value) conectados.add(chave(m.x, m.y))
    const marcados = new Set(marcacoes.value.map((m) => chave(m.x, m.y)))
    return ladrilhos.value
      .filter(
        (l) =>
          l.jazida === jazida.value &&
          !l.construcao &&
          !marcados.has(chave(l.x, l.y)) &&
          !ocupadoPorPredio(l.x, l.y) &&
          [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([dx, dy]) => conectados.has(chave(l.x + dx, l.y + dy))),
      )
      .map((l) => ({ x: l.x, y: l.y }))
  })

  function mensagem(e: unknown, padrao: string): string {
    return e instanceof Error ? e.message : padrao
  }

  async function carregar(construcaoId: number) {
    carregando.value = true
    erro.value = null
    try {
      predio.value = await get<PredioMarcacao>(`/api/jogo/construcoes/${construcaoId}`)
      marcacoes.value = await get<Marcacao[]>(`/api/jogo/construcoes/${construcaoId}/marcacoes`)
      const det = await get<RegiaoDetalhe>(`/api/jogo/regioes/${predio.value.regiaoIndice}`)
      ladrilhos.value = det.ladrilhos
    } catch (e) {
      erro.value = mensagem(e, 'Erro ao carregar as marcações')
    } finally {
      carregando.value = false
    }
  }

  async function marcar(construcaoId: number, x: number, y: number): Promise<boolean> {
    salvando.value = true
    erro.value = null
    try {
      const m = await post<Marcacao>(`/api/jogo/construcoes/${construcaoId}/marcacoes`, { x, y })
      marcacoes.value = [...marcacoes.value, m]
      return true
    } catch (e) {
      erro.value = mensagem(e, 'Erro ao marcar o ladrilho')
      return false
    } finally {
      salvando.value = false
    }
  }

  async function desmarcar(construcaoId: number, x: number, y: number): Promise<boolean> {
    salvando.value = true
    erro.value = null
    try {
      await del<void>(`/api/jogo/construcoes/${construcaoId}/marcacoes/${x}/${y}`)
      // Desmarcar pode remover também ladrilhos que perdem conexão: recarrega a lista.
      marcacoes.value = await get<Marcacao[]>(`/api/jogo/construcoes/${construcaoId}/marcacoes`)
      return true
    } catch (e) {
      erro.value = mensagem(e, 'Erro ao desmarcar o ladrilho')
      return false
    } finally {
      salvando.value = false
    }
  }

  return { predio, marcacoes, ladrilhos, carregando, salvando, erro, maximo, usado, jazida, candidatos, carregar, marcar, desmarcar }
}
