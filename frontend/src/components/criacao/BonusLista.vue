<script setup lang="ts">
import BarraValor from '../vilarejo/BarraValor.vue'
import { COR_BONUS, ROTULO_BONUS, type BonusRegiao } from '../../domain/regioes'

export interface ItemBonus {
  bonus: BonusRegiao
  valor: number
}

withDefaults(defineProps<{ itens: ItemBonus[]; maximo: number; espessura?: 'fina' | 'normal' }>(), {
  espessura: 'fina',
})
</script>

<template>
  <ul class="bl-lista" :class="espessura" data-testid="bonus-lista">
    <li
      v-for="item in itens"
      :key="item.bonus"
      class="bl-item"
      :class="{ esmaecido: item.valor === 0 }"
      :data-testid="`bonus-${item.bonus}`"
    >
      <span class="bl-nome">{{ ROTULO_BONUS[item.bonus] }}</span>
      <BarraValor :valor="item.valor" :maximo="maximo" :cor="COR_BONUS[item.bonus]" :rotulo="ROTULO_BONUS[item.bonus]" />
      <span class="bl-valor">{{ item.valor }}</span>
    </li>
  </ul>
</template>

<style scoped>
.bl-lista {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.bl-item {
  display: grid;
  grid-template-columns: minmax(80px, 1fr) 2fr 36px;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--vl-text-2);
}
.bl-item.esmaecido {
  opacity: 0.4;
}
.bl-valor {
  font-family: var(--vl-font-mono);
  text-align: right;
  color: var(--vl-text);
}
.normal .bl-item :deep(.vl-barra) {
  height: 6px;
}
.fina .bl-item :deep(.vl-barra) {
  height: 3px;
}
</style>
