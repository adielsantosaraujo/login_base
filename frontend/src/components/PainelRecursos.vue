<script setup lang="ts">
import { computed } from 'vue'
import Chip from 'primevue/chip'
import { useVila } from '../composables/useVila'
import type { TipoRecurso } from '../api/tipos'

const { vila } = useVila()

const ORDEM_RECURSOS: TipoRecurso[] = ['COMIDA', 'MADEIRA', 'PEDRA', 'FERRO']

// Só lista os recursos que a vila já retornou (evita chip vazio antes do
// primeiro carregamento ou se o backend não enviar todos os tipos).
const recursos = computed(() =>
  ORDEM_RECURSOS.filter((tipo) => vila.value?.recursos?.[tipo] !== undefined)
)
</script>

<template>
  <div v-if="vila" class="painel-recursos">
    <div v-for="tipo in recursos" :key="tipo" class="recurso">
      <Chip :label="`${tipo}: ${vila.recursos[tipo]} / ${vila.capacidade[tipo]}`" />
      <small class="producao">+{{ vila.producaoPorHora[tipo] ?? 0 }}/h</small>
    </div>
  </div>
</template>

<style scoped>
.painel-recursos {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--p-content-border-color, #dee2e6);
}

.recurso {
  display: flex;
  align-items: center;
  gap: 6px;
}

.producao {
  color: var(--p-text-muted-color, #6c757d);
}
</style>
