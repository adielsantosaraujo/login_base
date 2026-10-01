<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import { CARACTERISTICAS } from '../../composables/usePopulacao'
import { somaPontos } from '../../composables/useCidadao'

const props = defineProps<{
  caracteristicas: Record<string, number>
  pontosPendentes: number
  enviando?: boolean
}>()
const emit = defineEmits<{ (e: 'distribuir', pontos: Record<string, number>): void }>()

const ROTULOS: Record<string, string> = {
  VIT: 'Vitalidade', FOR: 'Força', VEL: 'Velocidade', INT: 'Inteligência', CAR: 'Carisma',
}

const incrementos = ref<Record<string, number>>({})
watch(() => [props.pontosPendentes, props.caracteristicas], () => (incrementos.value = {}))

const usados = computed(() => somaPontos(incrementos.value))
const excedeu = computed(() => usados.value > props.pontosPendentes)
const podeConfirmar = computed(() => usados.value > 0 && !excedeu.value && !props.enviando)

function maximo(c: string): number {
  return props.pontosPendentes - (usados.value - (incrementos.value[c] || 0))
}

function definir(c: string, v: number | null) {
  incrementos.value = { ...incrementos.value, [c]: v ?? 0 }
}

function confirmar() {
  const pontos: Record<string, number> = {}
  for (const [k, v] of Object.entries(incrementos.value)) if (v > 0) pontos[k] = v
  emit('distribuir', pontos)
}
</script>

<template>
  <div data-testid="aba-caracteristicas">
    <table class="tabela">
      <thead>
        <tr><th>Característica</th><th>Valor</th><th v-if="pontosPendentes > 0">Adicionar</th></tr>
      </thead>
      <tbody>
        <tr v-for="c in CARACTERISTICAS" :key="c" :data-testid="`car-${c}`">
          <td>{{ ROTULOS[c] }} ({{ c }})</td>
          <td data-testid="valor">{{ caracteristicas[c] ?? 0 }}</td>
          <td v-if="pontosPendentes > 0">
            <InputNumber
              :model-value="incrementos[c] ?? 0"
              :min="0"
              :max="Math.max(0, maximo(c))"
              show-buttons
              :pt="{ pcInputText: { root: { 'data-testid': `inc-${c}`, 'aria-label': `Pontos em ${ROTULOS[c]}` } } }"
              @update:model-value="(v: number | null) => definir(c, v)"
            />
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="pontosPendentes > 0" class="distribuicao" data-testid="distribuicao-car">
      <span data-testid="contador-car" :class="{ excedido: excedeu }">
        {{ usados }} de {{ pontosPendentes }} pontos pendentes usados
      </span>
      <Button
        data-testid="confirmar-car"
        label="Distribuir pontos de característica"
        :disabled="!podeConfirmar"
        :loading="enviando"
        @click="confirmar"
      />
    </div>
    <p v-else data-testid="sem-pendentes-car">Sem pontos de característica pendentes.</p>
  </div>
</template>

<style scoped>
.tabela { border-collapse: collapse; width: 100%; max-width: 32rem; }
.tabela th, .tabela td { text-align: left; padding: 0.35rem 0.5rem; border-bottom: 1px solid var(--p-content-border-color, #ddd); }
.distribuicao { display: flex; flex-wrap: wrap; gap: 1rem; align-items: center; margin-top: 1rem; }
.excedido { color: var(--p-red-500, #c00); }
</style>
