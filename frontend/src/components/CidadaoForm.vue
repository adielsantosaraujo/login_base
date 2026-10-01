<script setup lang="ts">
import { computed } from 'vue'
import InputNumber from 'primevue/inputnumber'
import {
  CARACTERISTICAS, MAX_POR_CARACTERISTICA, MAX_POR_PROFISSAO, MAX_TOTAL_CARACTERISTICAS,
  MAX_TOTAL_PROFISSOES, PROFISSOES, ROTULOS_PROFISSAO, soma, validarDistribuicao,
  type CidadaoPop, type Distribuicao,
} from '../composables/usePopulacao'

const props = defineProps<{ cidadao: CidadaoPop; modelValue: Distribuicao }>()
const emit = defineEmits<{ 'update:modelValue': [Distribuicao] }>()

const totalCar = computed(() => soma(props.modelValue.caracteristicas))
const totalProf = computed(() => soma(props.modelValue.profissoes))
const erros = computed(() => validarDistribuicao(props.modelValue))

function mudar(grupo: 'caracteristicas' | 'profissoes', chave: string, valor: number | null) {
  emit('update:modelValue', {
    ...props.modelValue,
    [grupo]: { ...props.modelValue[grupo], [chave]: valor ?? 0 },
  })
}
</script>

<template>
  <fieldset class="cidadao-form" :data-testid="`cidadao-${cidadao.id}`">
    <legend>{{ cidadao.nome }} ({{ cidadao.idadeAnos }} anos)</legend>

    <div class="grupo">
      <h4>
        Características
        <span :class="{ excedido: totalCar > MAX_TOTAL_CARACTERISTICAS }" data-testid="contador-car">
          {{ totalCar }} / {{ MAX_TOTAL_CARACTERISTICAS }}
        </span>
      </h4>
      <label v-for="c in CARACTERISTICAS" :key="c" class="campo">
        {{ c }}
        <InputNumber
          :model-value="modelValue.caracteristicas[c] ?? 0"
          :min="0"
          :max="MAX_POR_CARACTERISTICA"
          :input-id="`car-${cidadao.id}-${c}`"
          :input-props="{ 'data-testid': `car-${c}` } as any"
          show-buttons
          @update:model-value="(v: number | null) => mudar('caracteristicas', c, v)"
        />
      </label>
    </div>

    <div class="grupo">
      <h4>
        Profissões
        <span :class="{ excedido: totalProf > MAX_TOTAL_PROFISSOES }" data-testid="contador-prof">
          {{ totalProf }} / {{ MAX_TOTAL_PROFISSOES }}
        </span>
      </h4>
      <label v-for="p in PROFISSOES" :key="p" class="campo">
        {{ ROTULOS_PROFISSAO[p] }}
        <InputNumber
          :model-value="modelValue.profissoes[p] ?? 0"
          :min="0"
          :max="MAX_POR_PROFISSAO"
          :input-props="{ 'data-testid': `prof-${p}` } as any"
          show-buttons
          @update:model-value="(v: number | null) => mudar('profissoes', p, v)"
        />
      </label>
    </div>

    <p v-for="e in erros" :key="e" role="alert" class="erro" data-testid="erro-cidadao">{{ e }}</p>
  </fieldset>
</template>

<style scoped>
.cidadao-form { border: 1px solid #d1d5db; border-radius: 6px; padding: 0.75rem; margin-bottom: 1rem; }
.grupo { display: grid; grid-template-columns: repeat(auto-fill, minmax(12rem, 1fr)); gap: 0.5rem; margin-bottom: 0.5rem; }
.grupo h4 { grid-column: 1 / -1; margin: 0; }
.campo { display: flex; flex-direction: column; font-size: 0.85rem; gap: 0.15rem; }
.excedido, .erro { color: #b91c1c; }
</style>
