<script setup lang="ts">
import { computed, ref } from 'vue'
import Card from 'primevue/card'
import Button from 'primevue/button'
import ProgressBar from 'primevue/progressbar'
import { useToast } from 'primevue/usetoast'
import { useVila } from '../composables/useVila'
import { jogoAPI } from '../api/jogo'
import { ErroApi } from '../api/http'
import type { PredioDto } from '../api/tipos'

const props = defineProps<{ predio: PredioDto }>()

const { vila, carregar } = useVila()
const toast = useToast()
const melhorando = ref(false)

// A fila de construção é única por vila (spec game-buildings): enquanto
// qualquer prédio tem uma ordem CONSTRUCAO em andamento, nenhum outro pode
// iniciar uma melhoria.
const filaOcupada = computed(() =>
  vila.value?.predios.some((p) => p.ordemEmAndamento !== null) ?? false
)

const ordemPropria = computed(() => props.predio.ordemEmAndamento)

const percentualConclusao = computed(() => {
  const ordem = ordemPropria.value
  const tempoTotal = props.predio.proximoNivel?.tempoSegundos
  if (!ordem || !tempoTotal) return 0
  const percentual = 100 - (ordem.tempoRestanteSegundos / tempoTotal) * 100
  return Math.min(100, Math.max(0, percentual))
})

const podeMelhorar = computed(
  () => !!props.predio.proximoNivel && !filaOcupada.value && !melhorando.value
)

const severidade = computed(() => (filaOcupada.value ? 'secondary' : 'primary'))

async function melhorar(): Promise<void> {
  melhorando.value = true
  try {
    await jogoAPI.melhorarPredio(props.predio.tipo)
    await carregar()
  } catch (e) {
    const mensagem = e instanceof ErroApi ? e.message : 'Não foi possível melhorar o prédio.'
    toast.add({ severity: 'error', summary: 'Erro', detail: mensagem, life: 3000 })
  } finally {
    melhorando.value = false
  }
}
</script>

<template>
  <Card class="cartao-predio">
    <template #title>{{ predio.tipo }}</template>
    <template #subtitle>Nível {{ predio.nivel }}</template>
    <template #content>
      <p v-if="predio.nivel === predio.nivelMaximo">Nível máximo</p>
      <div v-else-if="predio.proximoNivel">
        <p>Próximo: nível {{ predio.proximoNivel.nivel }}</p>
        <p class="custo-titulo">Custo:</p>
        <ul class="custo-lista">
          <li v-for="item in predio.proximoNivel.custo" :key="item.recurso">
            {{ item.recurso }}: {{ item.quantidade }}
          </li>
        </ul>
        <p>Tempo: {{ predio.proximoNivel.tempoSegundos }}s</p>

        <ProgressBar v-if="ordemPropria" :value="percentualConclusao" />

        <Button
          label="Melhorar"
          :disabled="!podeMelhorar"
          :severity="severidade"
          @click="melhorar"
        />

        <small v-if="filaOcupada && !ordemPropria" class="aviso-bloqueio">
          Fila de construção ocupada
        </small>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.cartao-predio {
  min-width: 250px;
}

.custo-titulo {
  margin-bottom: 0;
}

.custo-lista {
  margin: 0 0 8px 0;
  padding-left: 1.2em;
}

.aviso-bloqueio {
  display: block;
  color: var(--p-red-500, #ef4444);
  margin-top: 4px;
}
</style>
