<script setup lang="ts">
import { computed } from 'vue'
import ProgressBar from 'primevue/progressbar'
import type { TropaDTO } from '../../composables/useQuartel'

const props = defineProps<{ tropa: Pick<TropaDTO, 'id' | 'estado' | 'regiaoDestino' | 'turnosViagem' | 'turnosRestantes'> }>()

const fase = computed(() => (props.tropa.estado === 'EM_VIAGEM_VOLTA' ? 'Volta' : 'Ida'))
const total = computed(() => props.tropa.turnosViagem ?? 0)
const restantes = computed(() => props.tropa.turnosRestantes ?? 0)
const percentual = computed(() =>
  total.value > 0 ? Math.min(100, Math.max(0, Math.round(((total.value - restantes.value) / total.value) * 100))) : 0)
</script>

<template>
  <div v-if="tropa.estado !== 'AQUARTELADA'" class="expedicao-status" :data-testid="`expedicao-status-${tropa.id}`">
    <p>
      <strong :data-testid="`fase-${tropa.id}`">{{ fase }}</strong>
      <template v-if="tropa.regiaoDestino != null"> · Destino: região <span :data-testid="`regiao-${tropa.id}`">{{ tropa.regiaoDestino }}</span></template>
    </p>
    <ProgressBar :value="percentual" :show-value="false" :aria-label="`Progresso da viagem (${fase})`"
      :data-testid="`progresso-${tropa.id}`" />
    <p :data-testid="`turnos-${tropa.id}`">{{ restantes }}/{{ total }} turno(s) restante(s)</p>
  </div>
</template>
