<script setup lang="ts">
import { bonusLider } from '../../domain/populacao'

export interface OpcaoLider {
  familiaId: number
  sobrenome: string
  lider: string
  car: number
}

defineProps<{ opcoes: OpcaoLider[]; modelValue: number | null }>()
const emit = defineEmits<{ 'update:modelValue': [familiaId: number] }>()
</script>

<template>
  <section aria-labelledby="fl-titulo" class="fl-painel">
    <div class="fl-topo">
      <h2 id="fl-titulo" class="fl-titulo">Família líder</h2>
      <span class="fl-dica">+1% a cada 2 CAR</span>
    </div>
    <div role="radiogroup" aria-labelledby="fl-titulo" class="fl-lista">
      <label
        v-for="o in opcoes"
        :key="o.familiaId"
        class="fl-opcao"
        :class="{ sel: o.familiaId === modelValue }"
      >
        <input
          type="radio"
          class="fl-radio"
          name="familia-lider"
          :value="o.familiaId"
          :checked="o.familiaId === modelValue"
          :data-testid="`lider-${o.familiaId}`"
          @change="emit('update:modelValue', o.familiaId)"
        />
        <span class="fl-info">
          <span class="fl-nome">Família {{ o.sobrenome }}</span>
          <span class="fl-sub">Líder {{ o.lider }} · CAR {{ o.car }}</span>
        </span>
        <span class="fl-bonus">+{{ bonusLider(o.car) }}%</span>
      </label>
    </div>
  </section>
</template>

<style scoped>
.fl-painel {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.fl-topo {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.fl-titulo {
  margin: 0;
  font-family: var(--vl-font-display);
  font-size: 17px;
  color: var(--vl-text);
}
.fl-dica {
  font-size: 11px;
  color: var(--vl-text-3);
}
.fl-lista {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.fl-opcao {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid var(--vl-border);
  border-radius: var(--vl-radius-tile);
  background: var(--vl-surface-2);
  cursor: pointer;
}
.fl-opcao.sel {
  border-color: var(--vl-accent);
  background: var(--vl-accent-bg);
}
.fl-opcao:focus-within {
  outline: 2px solid var(--vl-accent);
}
.fl-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}
.fl-nome {
  font-size: 14px;
  color: var(--vl-text);
}
.fl-sub {
  font-size: 12px;
  color: var(--vl-text-2);
}
.fl-bonus {
  font-family: var(--vl-font-mono);
  color: var(--vl-accent);
}
</style>
