import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError, get, post } from '../api/http'
import { marcarVilaCriada, resetarGuardaVila } from '../router/guardaVila'
import * as regioes from '../domain/regioes'
import type { PreviaMapa } from '../domain/regioes'
import { totaisLadrilhos as somarLadrilhos } from '../domain/terrenos'

export function useCriacaoVila() {
  const router = useRouter()

  const previa = ref<PreviaMapa | null>(null)
  const selecionadas = ref<number[]>([])
  const hover = ref<number | null>(null)
  const gerando = ref(false)
  const enviando = ref(false)
  const erro = ref<string | null>(null)

  const listaRegioes = () => previa.value?.regioes ?? []

  const conectado = computed(() => regioes.conectado(selecionadas.value))
  const totaisLadrilhos = computed(() => somarLadrilhos(selecionadas.value, listaRegioes()))
  const dicaSelecao = computed(() => regioes.dicaSelecao(selecionadas.value, listaRegioes()))
  const valida = computed(() => regioes.selecaoValida(selecionadas.value, listaRegioes()))
  /** Região em foco: hover ou, na ausência, a última selecionada. */
  const emFoco = computed<number | null>(
    () => hover.value ?? selecionadas.value[selecionadas.value.length - 1] ?? null,
  )

  function podeSelecionar(indice: number): boolean {
    return regioes.podeSelecionar(selecionadas.value, indice)
  }

  function mensagem(e: unknown, padrao: string): string {
    return e instanceof Error ? e.message : padrao
  }

  async function carregarPrevia() {
    gerando.value = true
    erro.value = null
    try {
      try {
        previa.value = await get<PreviaMapa>('/api/jogo/vila/previa')
      } catch (e) {
        if (e instanceof ApiError && e.status === 404) {
          previa.value = await post<PreviaMapa>('/api/jogo/vila/previa')
        } else {
          throw e
        }
      }
    } catch (e) {
      erro.value = mensagem(e, 'Erro ao carregar a prévia')
    } finally {
      gerando.value = false
    }
  }

  function alternar(indice: number) {
    if (selecionadas.value.includes(indice)) {
      selecionadas.value = selecionadas.value.filter((i) => i !== indice)
    } else if (podeSelecionar(indice)) {
      selecionadas.value = [...selecionadas.value, indice]
    }
  }

  async function gerar() {
    gerando.value = true
    erro.value = null
    try {
      previa.value = await post<PreviaMapa>('/api/jogo/vila/previa')
      selecionadas.value = []
      hover.value = null
    } catch (e) {
      erro.value = mensagem(e, 'Erro ao gerar novo mapa')
    } finally {
      gerando.value = false
    }
  }

  async function criar(): Promise<boolean> {
    if (!previa.value || enviando.value) return false
    enviando.value = true
    erro.value = null
    try {
      await post('/api/jogo/vila', { previaId: previa.value.previaId, indices: [...selecionadas.value] })
      marcarVilaCriada()
      await router.push('/jogo/distribuir-populacao')
      return true
    } catch (e) {
      if (e instanceof ApiError && e.codigo === 'VILA_JA_EXISTE') {
        resetarGuardaVila()
        await router.push('/jogo/mapa')
      } else if (e instanceof ApiError && e.codigo === 'PREVIA_EXPIRADA') {
        const aviso = e.message
        selecionadas.value = []
        hover.value = null
        await carregarPrevia()
        erro.value = aviso
      } else {
        erro.value = mensagem(e, 'Erro ao criar a vila')
      }
      return false
    } finally {
      enviando.value = false
    }
  }

  function setHover(indice: number) {
    hover.value = indice
  }

  function limparHover() {
    hover.value = null
  }

  return {
    previa, selecionadas, hover, gerando, enviando, erro,
    conectado, totaisLadrilhos, dicaSelecao, valida, emFoco,
    podeSelecionar, carregarPrevia, alternar, gerar, criar, setHover, limparHover,
  }
}
