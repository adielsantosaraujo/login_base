<script setup lang="ts">
import { computed } from 'vue'
import Button from 'primevue/button'
import {
  corQualidade, rotuloBonus, rotuloCategoria, rotuloQualidade, rotuloSlot, type ItemDTO,
} from '../../composables/useItens'
import type { PedraDTO } from '../../composables/usePedras'
import SlotsPedras from './SlotsPedras.vue'

const props = defineProps<{ item: ItemDTO; pedras?: PedraDTO[] }>()
const emit = defineEmits<{ (e: 'fechar'): void; (e: 'pedra-removida', pedraId: number): void }>()

const editavel = computed(() => props.item.cidadaoId == null)
</script>

<template>
  <div class="detalhes-item" data-testid="DetalhesItemModal" role="dialog" :aria-label="`Detalhes de ${item.nome}`">
    <h3>{{ item.nome }} L{{ item.nivel }}</h3>
    <p>
      {{ rotuloCategoria(item.categoria) }} ·
      <span :style="{ color: corQualidade(item.qualidade) }" data-testid="detalhe-qualidade">{{ rotuloQualidade(item.qualidade) }}</span>
      <template v-if="item.slot"> · slot {{ rotuloSlot(item.slot) }}</template>
    </p>
    <p v-if="item.atributoPrincipal" data-testid="atributo-principal">
      Atributo principal: {{ rotuloBonus(item.atributoPrincipal.tipo) }} {{ item.atributoPrincipal.valor }}
    </p>
    <h4>Bônus</h4>
    <p v-if="!item.bonus.length" data-testid="sem-bonus">Sem bônus.</p>
    <ul v-else data-testid="bonus">
      <li v-for="b in item.bonus" :key="b.codigo">{{ rotuloBonus(b.codigo) }}: +{{ b.valor }}</li>
    </ul>
    <h4>Pedras</h4>
    <SlotsPedras :item="item" :pedras="pedras" :editavel="editavel" @removida="emit('pedra-removida', $event)" />
    <div class="acoes">
      <Button label="Fechar" severity="secondary" data-testid="fechar-DetalhesItemModal" @click="emit('fechar')" />
    </div>
  </div>
</template>

<style scoped>
.acoes { margin-top: 0.5rem; }
</style>
