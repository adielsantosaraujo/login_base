<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{ valor: number; maximo: number; cor?: string; rotulo?: string }>(),
  { cor: 'var(--vl-accent)', rotulo: '' },
)

const percentual = computed(() => {
  if (props.maximo <= 0) return 0
  return Math.min(100, Math.max(0, (props.valor / props.maximo) * 100))
})
</script>

<template>
  <div
    class="vl-barra"
    role="progressbar"
    :aria-valuenow="valor"
    aria-valuemin="0"
    :aria-valuemax="maximo"
    :aria-label="rotulo || undefined"
  >
    <div class="vl-preenchimento" :style="{ width: `${percentual}%`, background: cor }"></div>
  </div>
</template>

<style scoped>
.vl-barra {
  height: 4px;
  background: var(--vl-surface-4);
  border-radius: var(--vl-radius-pill);
  overflow: hidden;
}
.vl-preenchimento {
  height: 100%;
  transition: width 0.2s;
}
</style>
