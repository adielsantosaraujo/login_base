<script setup lang="ts">
import RegiaoTile from './RegiaoTile.vue'
import { COR_TIPO, ROTULO_TIPO, TIPOS, podeSelecionar, type RegiaoPrevia } from '../../domain/regioes'

defineProps<{ regioes: RegiaoPrevia[]; selecionadas: number[] }>()
const emit = defineEmits<{ alternar: [indice: number]; foco: [indice: number]; desfoco: [] }>()
</script>

<template>
  <div class="mp-mapa">
    <ul class="mp-legenda" aria-label="Legenda dos tipos">
      <li v-for="t in TIPOS" :key="t">
        <span class="mp-amostra" :style="{ background: COR_TIPO[t] }" aria-hidden="true"></span>
        {{ ROTULO_TIPO[t] }}
      </li>
    </ul>
    <div class="mp-grade" data-testid="grade-criacao">
      <RegiaoTile
        v-for="r in regioes"
        :key="r.indice"
        :regiao="r"
        :ordem="selecionadas.indexOf(r.indice) + 1"
        :disponivel="podeSelecionar(selecionadas, r.indice)"
        @alternar="emit('alternar', $event)"
        @foco="emit('foco', $event)"
        @desfoco="emit('desfoco')"
      />
    </div>
  </div>
</template>

<style scoped>
.mp-legenda {
  list-style: none;
  margin: 0 0 12px;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 12px;
  color: var(--vl-text-2);
}
.mp-legenda li {
  display: flex;
  align-items: center;
  gap: 6px;
}
.mp-amostra {
  width: 8px;
  height: 8px;
  display: inline-block;
}
.mp-grade {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  padding: 10px;
  border-radius: var(--vl-radius-panel);
  background: var(--vl-surface-1);
}
@media (max-width: 640px) {
  .mp-grade {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
