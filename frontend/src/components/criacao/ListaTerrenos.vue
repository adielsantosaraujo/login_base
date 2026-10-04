<script setup lang="ts">
import BarraValor from '../vilarejo/BarraValor.vue'
import { COR_TERRENO, ROTULO_TERRENO, SIGLA_TERRENO, type TipoTerreno } from '../../domain/terrenos'

export interface ItemTerreno {
  terreno: TipoTerreno
  valor: number
}

withDefaults(
  defineProps<{ itens: ItemTerreno[]; maximo: number; sufixo?: string; espessura?: 'fina' | 'normal' }>(),
  { sufixo: '', espessura: 'fina' },
)
</script>

<template>
  <ul class="lt-lista" :class="espessura" data-testid="terrenos-lista">
    <li v-for="item in itens" :key="item.terreno" class="lt-item" :data-testid="`terreno-${item.terreno}`">
      <span class="lt-nome">
        <span class="lt-chip" :style="{ background: COR_TERRENO[item.terreno] }">{{ SIGLA_TERRENO[item.terreno] }}</span>
        {{ ROTULO_TERRENO[item.terreno] }}
      </span>
      <BarraValor
        :valor="item.valor"
        :maximo="maximo"
        :cor="COR_TERRENO[item.terreno]"
        :rotulo="ROTULO_TERRENO[item.terreno]"
      />
      <span class="lt-valor">{{ item.valor }}{{ sufixo }}</span>
    </li>
  </ul>
</template>

<style scoped>
.lt-lista {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.lt-item {
  display: grid;
  grid-template-columns: minmax(80px, 1fr) 2fr 40px;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--vl-text-2);
}
.lt-nome {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.lt-chip {
  padding: 1px 5px;
  border-radius: var(--vl-radius-chip);
  font-size: 10px;
  font-weight: 600;
  color: var(--vl-accent-ink);
}
.lt-valor {
  font-family: var(--vl-font-mono);
  text-align: right;
  color: var(--vl-text);
}
.normal .lt-item :deep(.vl-barra) {
  height: 6px;
}
.fina .lt-item :deep(.vl-barra) {
  height: 3px;
}
</style>
