// Estado reativo singleton da vila do jogador. Compartilhado entre todos os
// componentes que chamam `useVila()` (sem Pinia, ver design.md seção 19).

import { computed, ref } from 'vue'
import { jogoAPI } from '../api/jogo'
import type { CatalogoDto, VilaDto } from '../api/tipos'

const vila = ref<VilaDto | null>(null)
const catalogo = ref<CatalogoDto | null>(null)
const carregando = ref(false)
const erro = ref<unknown>(null)

let pollInterval: number | null = null

export function useVila() {
  async function carregar(): Promise<void> {
    carregando.value = true
    erro.value = null
    try {
      vila.value = await jogoAPI.consultarVila()
      catalogo.value ??= await jogoAPI.catalogo()
    } catch (e) {
      erro.value = e
      console.error('Erro ao carregar vila:', e)
    } finally {
      carregando.value = false
    }
  }

  function iniciarPoll(): void {
    if (pollInterval !== null) return
    pollInterval = window.setInterval(carregar, 5000)
  }

  function pararPoll(): void {
    if (pollInterval !== null) {
      window.clearInterval(pollInterval)
      pollInterval = null
    }
  }

  // Diferença entre o relógio do servidor (`vila.agora`) e o do navegador,
  // usada para contagens regressivas (ordens/construções) sem depender do
  // relógio local estar correto.
  const deslocamentoRelogio = computed(() => {
    if (!vila.value) return 0
    return new Date(vila.value.agora).getTime() - Date.now()
  })

  return { vila, catalogo, carregando, erro, carregar, iniciarPoll, pararPoll, deslocamentoRelogio }
}
