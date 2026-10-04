<script setup lang="ts">
import { computed, ref } from 'vue'
import Button from 'primevue/button'
import Select from 'primevue/select'
import { corQualidade, rotuloBonus, rotuloQualidade } from '../../composables/useItens'
import {
  custoEngaste, rotuloMagnitude, type ItemCompativelDTO, type PedraDTO,
} from '../../composables/usePedras'

const props = defineProps<{
  pedra: PedraDTO
  itensCompativeis: ItemCompativelDTO[]
  ouroDisponivel: number
  enviando?: boolean
  erro?: string | null
}>()
const emit = defineEmits<{
  (e: 'engastar', req: { pedraId: number; itemId: number }): void
  (e: 'fechar'): void
}>()

const itemSel = ref<number | null>(null)
const custo = computed(() => props.pedra.custoEngaste ?? custoEngaste(props.pedra.qualidade))
const faltam = computed(() => Math.max(0, custo.value - props.ouroDisponivel))
const opcoes = computed(() =>
  props.itensCompativeis.map((c) => ({
    label: `${c.item.nome} L${c.item.nivel} (${rotuloQualidade(c.item.qualidade)}) - slots ${c.slotsLivres}/${c.slotsTotais}`,
    value: c.item.id,
  })),
)
const motivoBloqueio = computed<string | null>(() => {
  if (!props.itensCompativeis.length) return 'Sem itens com slots disponíveis'
  if (faltam.value > 0) return `Ouro insuficiente (faltam ${faltam.value} Ouro)`
  if (itemSel.value == null) return 'Escolha o item'
  return null
})

function confirmar() {
  if (motivoBloqueio.value || itemSel.value == null) return
  emit('engastar', { pedraId: props.pedra.id, itemId: itemSel.value })
}
</script>

<template>
  <div class="modal-engaste" data-testid="ModalEngaste" role="dialog"
    :aria-label="`Engastar pedra ${rotuloQualidade(pedra.qualidade)}`">
    <h3>Engastar pedra <span :style="{ color: corQualidade(pedra.qualidade) }">{{ rotuloQualidade(pedra.qualidade) }}</span></h3>
    <ul data-testid="bonus-pedra">
      <li v-for="b in pedra.bonus" :key="b.codigo">
        {{ rotuloBonus(b.codigo) }}: +{{ b.valor }} ({{ rotuloMagnitude(b.magnitude) }})
      </li>
    </ul>
    <p data-testid="custo">Custo: {{ custo }} Ouro</p>
    <p data-testid="ouro">Ouro disponível: {{ ouroDisponivel }}</p>
    <label>Item
      <Select v-model="itemSel" :options="opcoes" option-label="label" option-value="value" placeholder="Item"
        :disabled="!itensCompativeis.length" data-testid="select-item" />
    </label>
    <p v-if="motivoBloqueio && (!itensCompativeis.length || faltam > 0)" class="falta" data-testid="bloqueio">{{ motivoBloqueio }}</p>
    <p v-if="erro" role="alert" class="falta" data-testid="erro-acao">{{ erro }}</p>
    <div class="acoes">
      <Button label="Engastar" data-testid="confirmar-engaste" :disabled="!!motivoBloqueio || enviando" @click="confirmar" />
      <Button label="Cancelar" severity="secondary" data-testid="fechar-ModalEngaste" @click="emit('fechar')" />
    </div>
  </div>
</template>

<style scoped>
.acoes { display: flex; gap: 0.5rem; margin-top: 0.5rem; }
.falta { color: var(--vl-error); }
label { display: flex; flex-direction: column; gap: 0.25rem; max-width: 24rem; }
</style>
