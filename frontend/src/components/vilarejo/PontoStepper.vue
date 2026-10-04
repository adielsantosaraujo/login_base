<script setup lang="ts">
const modelo = defineModel<number>({ required: true })

const props = withDefaults(
  defineProps<{ min?: number; podeAumentar?: boolean; rotulo?: string }>(),
  { min: 0, podeAumentar: true, rotulo: 'pontos' },
)

function diminuir() {
  if (modelo.value > props.min) modelo.value -= 1
}
function aumentar() {
  if (props.podeAumentar) modelo.value += 1
}
</script>

<template>
  <div class="vl-stepper">
    <button
      type="button"
      class="vl-passo"
      :aria-label="`Diminuir ${rotulo}`"
      :disabled="modelo <= min"
      @click="diminuir"
    >
      −
    </button>
    <span class="vl-valor" data-testid="stepper-valor" aria-live="polite">{{ modelo }}</span>
    <button
      type="button"
      class="vl-passo"
      :aria-label="`Aumentar ${rotulo}`"
      :disabled="!podeAumentar"
      @click="aumentar"
    >
      +
    </button>
  </div>
</template>

<style scoped>
.vl-stepper {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.vl-passo {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: var(--vl-radius-chip);
  background: var(--vl-surface-4);
  color: var(--vl-text);
  cursor: pointer;
  line-height: 1;
}
.vl-passo:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}
.vl-valor {
  min-width: 24px;
  text-align: center;
  font-family: var(--vl-font-mono);
  font-size: 14px;
  font-weight: 500;
  color: var(--vl-text);
}
</style>
