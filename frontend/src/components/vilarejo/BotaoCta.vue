<script setup lang="ts">
const props = withDefaults(
  defineProps<{ ativo?: boolean; carregando?: boolean; rotulo?: string }>(),
  { ativo: true, carregando: false, rotulo: '' },
)
const emit = defineEmits<{ click: [e: MouseEvent] }>()

function aoClicar(e: MouseEvent) {
  if (props.ativo && !props.carregando) emit('click', e)
}
</script>

<template>
  <button
    type="button"
    class="vl-cta"
    :class="{ inativo: !ativo, carregando }"
    :aria-disabled="!ativo || carregando ? 'true' : undefined"
    :aria-busy="carregando ? 'true' : undefined"
    @click="aoClicar"
  >
    <i v-if="carregando" class="pi pi-spin pi-spinner" aria-hidden="true"></i>
    <slot>{{ rotulo }}</slot>
  </button>
</template>

<style scoped>
.vl-cta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 48px;
  padding: 0 24px;
  border: none;
  border-radius: var(--vl-radius-tile);
  background: var(--vl-accent);
  color: var(--vl-accent-ink);
  font-family: var(--vl-font-sans);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}
.vl-cta.inativo {
  background: var(--vl-surface-4);
  color: var(--vl-text-3);
  cursor: not-allowed;
}
.vl-cta.carregando {
  cursor: progress;
}
</style>
