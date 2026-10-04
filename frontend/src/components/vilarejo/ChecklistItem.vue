<script setup lang="ts">
defineProps<{ estado: 'ok' | 'aviso' | 'pendente'; texto: string }>()

const rotulos = { ok: 'Cumprido', aviso: 'Aviso', pendente: 'Pendente' }
const marcas = { ok: '✓', aviso: '!', pendente: '' }
</script>

<template>
  <li class="vl-check" :class="estado">
    <span class="vl-circulo" aria-hidden="true">{{ marcas[estado] }}</span>
    <span class="vl-sr">{{ rotulos[estado] }}:</span>
    <span class="vl-texto">{{ texto }}</span>
  </li>
</template>

<style scoped>
.vl-check {
  display: flex;
  align-items: center;
  gap: 8px;
  list-style: none;
  font-size: 13px;
  color: var(--vl-text-2);
}
.vl-circulo {
  width: 18px;
  height: 18px;
  border-radius: var(--vl-radius-pill);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  background: var(--vl-surface-4);
  color: var(--vl-accent-ink);
  flex-shrink: 0;
}
.ok .vl-circulo {
  background: var(--vl-accent);
}
.aviso .vl-circulo {
  background: var(--vl-warn);
}
.ok .vl-texto {
  color: var(--vl-text);
}
.vl-sr {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip-path: inset(50%);
}
</style>
