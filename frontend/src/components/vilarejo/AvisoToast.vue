<script setup lang="ts">
withDefaults(defineProps<{ mensagem: string; tipo?: 'info' | 'erro' }>(), { tipo: 'info' })
</script>

<template>
  <div
    class="vl-toast"
    :class="tipo"
    :role="tipo === 'erro' ? 'alert' : 'status'"
    :aria-live="tipo === 'erro' ? 'assertive' : 'polite'"
  >
    {{ mensagem }}
  </div>
</template>

<style scoped>
.vl-toast {
  position: fixed;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 20px;
  border-radius: var(--vl-radius-tile);
  background: var(--vl-accent);
  color: var(--vl-accent-ink);
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 10px 30px color-mix(in oklch, black 40%, transparent);
  z-index: 1000;
}
.vl-toast.erro {
  background: var(--vl-error);
}
</style>
