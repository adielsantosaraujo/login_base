<script setup lang="ts">
import type { PendenciasFamilia } from '../../composables/usePopulacao'

defineProps<{
  familias: PendenciasFamilia[]
  ativa: number | null
  liderId: number | null
}>()
const emit = defineEmits<{ 'update:ativa': [familiaId: number] }>()

function status(f: PendenciasFamilia): string {
  const n = f.carPendentes + f.profPendentes
  return n > 0 ? `${n} pontos pendentes` : 'Todos os pontos usados'
}
</script>

<template>
  <div class="ft-abas" role="tablist" aria-label="Famílias">
    <button
      v-for="f in familias"
      :key="f.familiaId"
      type="button"
      role="tab"
      class="ft-aba"
      :class="{ ativa: f.familiaId === ativa }"
      :aria-selected="f.familiaId === ativa"
      :data-testid="`aba-${f.familiaId}`"
      @click="emit('update:ativa', f.familiaId)"
    >
      <span class="ft-nome">
        {{ f.sobrenome }}
        <span v-if="f.familiaId === liderId" class="ft-chip">LÍDER</span>
      </span>
      <span class="ft-status" :class="{ pendente: f.carPendentes + f.profPendentes > 0 }">{{ status(f) }}</span>
    </button>
  </div>
</template>

<style scoped>
.ft-abas {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 8px;
}
.ft-aba {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-start;
  padding: 12px 14px;
  border: 2px solid var(--vl-border);
  border-radius: var(--vl-radius-tile);
  background: var(--vl-surface-2);
  color: var(--vl-text);
  cursor: pointer;
  text-align: left;
}
.ft-aba.ativa {
  border-color: var(--vl-accent);
  background: var(--vl-accent-bg);
}
.ft-nome {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--vl-font-display);
  font-size: 17px;
}
.ft-chip {
  padding: 1px 6px;
  border-radius: var(--vl-radius-chip);
  background: var(--vl-accent);
  color: var(--vl-accent-ink);
  font-family: var(--vl-font-sans);
  font-size: 10px;
  font-weight: 600;
}
.ft-status {
  font-size: 12px;
  color: var(--vl-text-2);
}
.ft-status.pendente {
  color: var(--vl-warn);
}
</style>
