<script setup lang="ts">
import { ref } from 'vue'
import Button from 'primevue/button'
import { SLOTS_EQUIPAMENTO, useEquipamento, type MapaEquipamento } from '../../composables/useEquipamento'
import { corQualidade, rotuloBonus, rotuloQualidade, rotuloSlot, type ItemDTO } from '../../composables/useItens'
import SeletorInventario from './SeletorInventario.vue'

const props = defineProps<{
  cidadaoId: number
  equipamento?: MapaEquipamento
  emExpedicao?: boolean
}>()
const emit = defineEmits<{ (e: 'atualizado', mapa: MapaEquipamento): void }>()

const { enviando, erro, equipar, remover } = useEquipamento()
const slotAberto = ref<string | null>(null)

function itemDo(slot: string): ItemDTO | null {
  return (props.equipamento?.[slot] as ItemDTO | null | undefined) ?? null
}

function abrir(slot: string) {
  erro.value = null
  slotAberto.value = slot
}

async function selecionar(item: ItemDTO) {
  if (!slotAberto.value) return
  const mapa = await equipar(props.cidadaoId, slotAberto.value, item.id)
  if (mapa) {
    slotAberto.value = null
    emit('atualizado', mapa)
  }
}

async function tirar(slot: string) {
  const mapa = await remover(props.cidadaoId, slot)
  if (mapa) emit('atualizado', mapa)
}
</script>

<template>
  <div data-testid="aba-equipamento">
    <p v-if="emExpedicao" data-testid="aviso-expedicao">Pessoa em expedição: equipamento bloqueado.</p>
    <p v-if="erro && !slotAberto" role="alert" class="erro" data-testid="erro-equipamento">{{ erro }}</p>
    <div class="grid">
      <div v-for="slot in SLOTS_EQUIPAMENTO" :key="slot" class="slot" :data-testid="`slot-${slot}`">
        <h4>{{ rotuloSlot(slot) }}</h4>
        <template v-if="itemDo(slot)">
          <p class="nome" data-testid="item-nome">{{ itemDo(slot)!.nome }} (nível {{ itemDo(slot)!.nivel }})</p>
          <p>
            <strong :style="{ color: corQualidade(itemDo(slot)!.qualidade) }" data-testid="item-qualidade">{{ rotuloQualidade(itemDo(slot)!.qualidade) }}</strong>
          </p>
          <p v-if="itemDo(slot)!.atributoPrincipal" data-testid="item-principal">
            {{ itemDo(slot)!.atributoPrincipal!.tipo }} {{ itemDo(slot)!.atributoPrincipal!.valor }}
          </p>
          <ul v-if="itemDo(slot)!.bonus.length" class="bonus">
            <li v-for="b in itemDo(slot)!.bonus" :key="b.codigo">{{ rotuloBonus(b.codigo) }} +{{ b.valor }}</li>
          </ul>
        </template>
        <p v-else class="vazio">Vazio</p>
        <div class="acoes">
          <Button :label="itemDo(slot) ? 'Trocar' : 'Equipar'" size="small" :disabled="emExpedicao || enviando" :data-testid="`equipar-${slot}`" @click="abrir(slot)" />
          <Button v-if="itemDo(slot)" label="Remover" severity="secondary" size="small" :disabled="emExpedicao || enviando" :data-testid="`remover-${slot}`" @click="tirar(slot)" />
        </div>
      </div>
    </div>
    <SeletorInventario
      v-if="slotAberto"
      :key="slotAberto"
      :slot="slotAberto"
      :erro="erro"
      :enviando="enviando"
      @selecionar="selecionar"
      @cancelar="slotAberto = null"
    />
  </div>
</template>

<style scoped>
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(12rem, 1fr)); gap: 0.75rem; }
.slot { border: 1px solid var(--vl-border); border-radius: var(--vl-radius-slot); background: var(--vl-surface-2); color: var(--vl-text); padding: 0.5rem; }
.slot h4, .slot p { margin: 0 0 0.25rem; }
.slot h4 { font-family: var(--vl-font-display); }
.bonus { margin: 0 0 0.25rem; padding-left: 1rem; font-size: 0.85rem; }
.vazio { color: var(--vl-text-3); }
.acoes { display: flex; gap: 0.5rem; margin-top: 0.5rem; }
.erro { color: var(--vl-error); }
</style>
