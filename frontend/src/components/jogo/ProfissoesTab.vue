<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import { ROTULOS_PROFISSAO } from '../../composables/usePopulacao'
import { somaPontos, type ProfissaoCidadao } from '../../composables/useCidadao'

const props = defineProps<{
  profissoes: ProfissaoCidadao[]
  pontosPendentes: number
  enviando?: boolean
}>()
const emit = defineEmits<{ (e: 'distribuir', pontos: Record<string, number>): void }>()

const incrementos = ref<Record<string, number>>({})
watch(() => [props.pontosPendentes, props.profissoes], () => (incrementos.value = {}))

const usados = computed(() => somaPontos(incrementos.value))
const excedeu = computed(() => usados.value > props.pontosPendentes)
const podeConfirmar = computed(() => usados.value > 0 && !excedeu.value && !props.enviando)

function maximo(p: string): number {
  return props.pontosPendentes - (usados.value - (incrementos.value[p] || 0))
}

function definir(p: string, v: number | null) {
  incrementos.value = { ...incrementos.value, [p]: v ?? 0 }
}

function confirmar() {
  const pontos: Record<string, number> = {}
  for (const [k, v] of Object.entries(incrementos.value)) if (v > 0) pontos[k] = v
  emit('distribuir', pontos)
}

const eficienciaPct = (e: number) => `${(e * 100).toFixed(0)}%`
</script>

<template>
  <div data-testid="aba-profissoes">
    <div class="rolagem">
      <table class="tabela">
        <thead>
          <tr>
            <th>Profissão</th><th>PE base</th><th>PE efetivo</th><th>Eficiência</th>
            <th v-if="pontosPendentes > 0">Adicionar</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in profissoes" :key="p.profissao" :data-testid="`prof-${p.profissao}`">
            <td>{{ ROTULOS_PROFISSAO[p.profissao] ?? p.profissao }}</td>
            <td data-testid="pe-base">{{ p.peBase }}</td>
            <td data-testid="pe-efetivo">{{ p.peEfetivo }}</td>
            <td data-testid="eficiencia">{{ eficienciaPct(p.eficiencia) }}</td>
            <td v-if="pontosPendentes > 0">
              <InputNumber
                :model-value="incrementos[p.profissao] ?? 0"
                :min="0"
                :max="Math.max(0, maximo(p.profissao))"
                show-buttons
                :pt="{ pcInputText: { root: { 'data-testid': `inc-${p.profissao}`, 'aria-label': `Pontos em ${ROTULOS_PROFISSAO[p.profissao] ?? p.profissao}` } } }"
                @update:model-value="(v: number | null) => definir(p.profissao, v)"
              />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-if="pontosPendentes > 0" class="distribuicao" data-testid="distribuicao-prof">
      <span data-testid="contador-prof" :class="{ excedido: excedeu }">
        {{ usados }} de {{ pontosPendentes }} pontos pendentes usados
      </span>
      <Button
        data-testid="confirmar-prof"
        label="Distribuir pontos de profissão"
        :disabled="!podeConfirmar"
        :loading="enviando"
        @click="confirmar"
      />
    </div>
    <p v-else data-testid="sem-pendentes-prof">Sem pontos de profissão pendentes.</p>
  </div>
</template>

<style scoped>
.rolagem { overflow-x: auto; }
.tabela { border-collapse: collapse; width: 100%; max-width: 40rem; }
.tabela th, .tabela td { text-align: left; padding: 0.35rem 0.5rem; border-bottom: 1px solid var(--p-content-border-color, #ddd); }
.distribuicao { display: flex; flex-wrap: wrap; gap: 1rem; align-items: center; margin-top: 1rem; }
.excedido { color: var(--p-red-500, #c00); }
</style>
