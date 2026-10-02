<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ recompensas: Record<string, unknown> | null }>()
const entradas = computed(() => Object.entries(props.recompensas ?? {}))

function texto(v: unknown): string {
  return typeof v === 'object' && v !== null ? JSON.stringify(v) : String(v)
}
</script>

<template>
  <div data-testid="RecompensasBatalha">
    <p v-if="!recompensas || !entradas.length" data-testid="sem-recompensas">Sem recompensas</p>
    <ul v-else>
      <li v-for="[k, v] in entradas" :key="k" :data-testid="`recompensa-${k}`">{{ k }}: {{ texto(v) }}</li>
    </ul>
  </div>
</template>
