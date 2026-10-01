import { computed, ref } from 'vue'
import { get, post } from '../api/http'
import type { CatalogoConstrucao, ConstrucaoCriada } from './useConstrucoes'
import type { Estoque } from './useEstoque'

export type Nivel = 'N1' | 'N2' | 'N3'
export const LADO_REGIAO = 10
const NIVEIS: Nivel[] = ['N1', 'N2', 'N3']

export function proximoNivel(nivel: string): Nivel | null {
  const i = NIVEIS.indexOf(nivel as Nivel)
  return i >= 0 && i < NIVEIS.length - 1 ? NIVEIS[i + 1] : null
}

export function tamanhoDoNivel(nivel: Nivel): number {
  return NIVEIS.indexOf(nivel) + 1
}

/** Custo para chegar ao nível, conforme ConstrucaoCatalogo: N2 = 2,5x (teto), N3 = 5x. */
export function custoDoNivel(custoN1: Record<string, number>, nivel: Nivel): Record<string, number> {
  const r: Record<string, number> = {}
  for (const [rec, q] of Object.entries(custoN1)) {
    r[rec] = nivel === 'N1' ? q : nivel === 'N2' ? Math.ceil(q * 2.5) : q * 5
  }
  return r
}

export function poDoNivel(poN1: number, nivel: Nivel): number {
  return nivel === 'N1' ? poN1 : nivel === 'N2' ? Math.ceil(poN1 * 2.5) : poN1 * 5
}

export interface UpgradeRequest {
  novoNivel?: Nivel
  novaX?: number
  novaY?: number
}

export function useUpgrade() {
  const predio = ref<ConstrucaoCriada | null>(null)
  const catalogo = ref<CatalogoConstrucao[]>([])
  const estoque = ref<Record<string, number>>({})
  const carregando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)

  const item = computed(() => catalogo.value.find((c) => c.tipo === predio.value?.tipo) ?? null)
  const nivelNovo = computed(() => (predio.value ? proximoNivel(predio.value.nivel) : null))
  const custo = computed(() => (item.value && nivelNovo.value ? custoDoNivel(item.value.custoN1, nivelNovo.value) : {}))
  const poNovo = computed(() => (item.value && nivelNovo.value ? poDoNivel(item.value.poN1, nivelNovo.value) : 0))
  const tamanhoNovo = computed(() => (nivelNovo.value ? tamanhoDoNivel(nivelNovo.value) : 0))
  const faltantes = computed(() =>
    Object.entries(custo.value)
      .filter(([r, q]) => (estoque.value[r] ?? 0) < q)
      .map(([r]) => r),
  )

  async function carregar(id: number) {
    carregando.value = true
    erro.value = null
    try {
      const [p, cat, est] = await Promise.all([
        get<ConstrucaoCriada>(`/api/jogo/construcoes/${id}`),
        get<CatalogoConstrucao[]>('/api/jogo/construcoes/catalogo'),
        get<Estoque>('/api/jogo/estoque'),
      ])
      predio.value = p
      catalogo.value = cat
      estoque.value = Object.fromEntries(est.recursos.map((l) => [l.recurso, l.quantidade]))
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao carregar o prédio'
    } finally {
      carregando.value = false
    }
  }

  /** Confirma o upgrade; retorna o prédio atualizado ou null (com `erro` preenchido em 400/409). */
  async function confirmar(id: number, req: UpgradeRequest = {}): Promise<ConstrucaoCriada | null> {
    enviando.value = true
    erro.value = null
    try {
      return await post<ConstrucaoCriada>(`/api/jogo/construcoes/${id}/upgrade`, req)
    } catch (e) {
      erro.value = e instanceof Error ? e.message : 'Erro ao melhorar o prédio'
      return null
    } finally {
      enviando.value = false
    }
  }

  return { predio, catalogo, estoque, carregando, enviando, erro, item, nivelNovo, custo, poNovo, tamanhoNovo, faltantes, carregar, confirmar }
}
