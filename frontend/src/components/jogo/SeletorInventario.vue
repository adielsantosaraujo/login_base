<script setup lang="ts">
import { computed, onMounted } from 'vue'
import Button from 'primevue/button'
import { categoriaDoSlot, subtipoDoSlot, useEquipamento } from '../../composables/useEquipamento'
import { corQualidade, rotuloBonus, rotuloQualidade, rotuloSlot, type ItemDTO } from '../../composables/useItens'

const props = defineProps<{ slot: string; erro?: string | null; enviando?: boolean }>()
const emit = defineEmits<{ (e: 'selecionar', item: ItemDTO): void; (e: 'cancelar'): void }>()

const { itens, carregando, erro: erroInventario, carregarInventario } = useEquipamento()

const disponiveis = computed(() => {
  const sub = subtipoDoSlot(props.slot)
  return itens.value.filter((i) => !i.emAprimoramento && !i.slot && (sub == null || i.subtipo === sub))
})

onMounted(() => carregarInventario(categoriaDoSlot(props.slot)))
</script>

<template>
  <div class="seletor" data-testid="seletor-inventario">
    <h3>Escolher item para {{ rotuloSlot(slot) }}</h3>
    <p v-if="erro" role="alert" class="erro" data-testid="erro-equipar">{{ erro }}</p>
    <p v-if="erroInventario" role="alert" class="erro" data-testid="erro-inventario">{{ erroInventario }}</p>
    <p v-if="carregando">Carregando...</p>
    <p v-else-if="!disponiveis.length" data-testid="sem-itens">Nenhum item compatível no inventário.</p>
    <ul v-else>
      <li v-for="i in disponiveis" :key="i.id" :data-testid="`opcao-${i.id}`">
        <span>
          {{ i.nome }} (nível {{ i.nivel }}) -
          <strong :style="{ color: corQualidade(i.qualidade) }">{{ rotuloQualidade(i.qualidade) }}</strong>
          <template v-if="i.atributoPrincipal"> - {{ i.atributoPrincipal.tipo }} {{ i.atributoPrincipal.valor }}</template>
          <template v-for="b in i.bonus" :key="b.codigo"> / {{ rotuloBonus(b.codigo) }} +{{ b.valor }}</template>
        </span>
        <Button label="Selecionar" size="small" :disabled="enviando" :data-testid="`selecionar-${i.id}`" @click="emit('selecionar', i)" />
      </li>
    </ul>
    <Button label="Cancelar" severity="secondary" size="small" data-testid="cancelar-selecao" @click="emit('cancelar')" />
  </div>
</template>

<style scoped>
.seletor { border: 1px solid var(--vl-border); background: var(--vl-surface-2); border-radius: var(--vl-radius-card); padding: 0.75rem; margin-top: 1rem; }
ul { list-style: none; padding: 0; margin: 0 0 0.5rem; }
li { display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; padding: 0.25rem 0; }
.erro { color: var(--vl-error); }
h3 { font-family: var(--vl-font-display); color: var(--vl-text); }
</style>
