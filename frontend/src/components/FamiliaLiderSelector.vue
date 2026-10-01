<script setup lang="ts">
import RadioButton from 'primevue/radiobutton'
import { bonusLider, type Distribuicao, type FamiliaPop } from '../composables/usePopulacao'

const props = defineProps<{
  familias: FamiliaPop[]
  modelValue: number | null
  distribuicoes: Record<number, Distribuicao>
}>()
const emit = defineEmits<{ 'update:modelValue': [number] }>()

/** Líder = adulto (18+) mais velho da família; desempate pelo menor id. */
function lider(f: FamiliaPop) {
  const adultos = f.cidadaos.filter((c) => c.idadeAnos >= 18)
  return adultos.reduce<(typeof adultos)[number] | null>(
    (m, c) => (m === null || c.idadeAnos > m.idadeAnos ? c : m),
    null,
  )
}

function carDe(f: FamiliaPop): number {
  const l = lider(f)
  if (!l) return 0
  return (l.caracteristicas.CAR ?? 0) + (props.distribuicoes[l.id]?.caracteristicas.CAR ?? 0)
}
</script>

<template>
  <section class="lider-selector" data-testid="lider-selector">
    <h2>Família líder</h2>
    <div v-for="f in familias" :key="f.id" class="opcao">
      <RadioButton
        :model-value="modelValue"
        :value="f.id"
        :input-id="`lider-${f.id}`"
        name="familia-lider"
        :data-testid="`lider-${f.id}`"
        @update:model-value="emit('update:modelValue', f.id)"
      />
      <label :for="`lider-${f.id}`">
        Família {{ f.sobrenome }} —
        <template v-if="lider(f)">
          líder {{ lider(f)!.nome }} (CAR {{ carDe(f) }}):
          <strong :data-testid="`bonus-${f.id}`">+{{ bonusLider(carDe(f)) }}%</strong> de eficiência
        </template>
        <template v-else>sem adulto para liderar</template>
      </label>
    </div>
  </section>
</template>

<style scoped>
.opcao { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.4rem; }
</style>
