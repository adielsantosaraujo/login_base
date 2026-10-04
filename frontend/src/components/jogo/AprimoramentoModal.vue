<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import Button from 'primevue/button'
import Select from 'primevue/select'
import { rotuloQualidade, rotuloRecurso, type ItemDTO } from '../../composables/useItens'
import type { OficinaDTO, ReceitaDTO } from '../../composables/useOficina'
import type { Construcao } from '../../composables/useMapa'
import {
  NIVEL_MAXIMO_ITEM, custoAprimoramento, motivoArtesaoAprimoramento, peMinimoAprimoramento,
} from '../../composables/useInventario'

const props = defineProps<{
  item: ItemDTO
  oficinas: Construcao[]
  oficina: OficinaDTO | null
  receitas: ReceitaDTO[]
  estoque?: Record<string, number>
  enviando?: boolean
  erro?: string | null
}>()
const emit = defineEmits<{
  (e: 'selecionar-oficina', id: number): void
  (e: 'aprimorar', req: { oficinaId: number; artesaoId: number }): void
  (e: 'fechar'): void
}>()

const oficinaSel = ref<number | null>(null)
const artesaoSel = ref<number | null>(null)

const nivelMaximo = computed(() => props.item.nivel >= NIVEL_MAXIMO_ITEM)
const novoNivel = computed(() => props.item.nivel + 1)
const peMinimo = computed(() => peMinimoAprimoramento(props.item.nivel))
const receita = computed(() => props.receitas.find((r) => r.subtipo === props.item.subtipo) ?? null)
const custo = computed(() => custoAprimoramento(receita.value, props.item.nivel))
const faltantes = computed<string[]>(() => {
  if (!props.estoque) return []
  return Object.entries(custo.value)
    .filter(([r, q]) => (props.estoque?.[r] ?? 0) < q)
    .map(([r]) => r)
})

const opcoesOficina = computed(() =>
  props.oficinas.map((o) => ({ label: `Oficina #${o.id} (${o.nivel})`, value: o.id })),
)
const opcoesArtesao = computed(() =>
  (props.oficina?.artesaos ?? []).map((a) => {
    const motivo = motivoArtesaoAprimoramento(a, props.item.nivel)
    return {
      label: `${a.nome} (PE ${a.peEfetivo})${motivo ? ` - ${motivo}` : ''}`,
      value: a.cidadaoId,
      disabled: !!motivo,
    }
  }),
)

watch(oficinaSel, (id) => {
  artesaoSel.value = null
  if (id != null) emit('selecionar-oficina', id)
})

const motivoBloqueio = computed<string | null>(() => {
  if (nivelMaximo.value) return 'Nível máximo L10 atingido'
  if (props.item.emAprimoramento) return 'Item já em aprimoramento'
  if (oficinaSel.value == null) return 'Escolha a oficina'
  if (artesaoSel.value == null) return 'Escolha o artesão'
  const a = props.oficina?.artesaos.find((x) => x.cidadaoId === artesaoSel.value)
  if (!a || motivoArtesaoAprimoramento(a, props.item.nivel)) return `PE mínimo ${peMinimo.value} necessário`
  if (faltantes.value.length) return 'Recursos insuficientes'
  return null
})

function aprimorar() {
  if (motivoBloqueio.value || oficinaSel.value == null || artesaoSel.value == null) return
  emit('aprimorar', { oficinaId: oficinaSel.value, artesaoId: artesaoSel.value })
}
</script>

<template>
  <div class="aprimoramento-modal" data-testid="AprimoramentoModal" role="dialog" :aria-label="`Aprimorar ${item.nome}`">
    <h3>Aprimorar {{ item.nome }}</h3>
    <p data-testid="de-para">
      L{{ item.nivel }} <template v-if="!nivelMaximo">&rarr; L{{ novoNivel }}</template>
      · {{ rotuloQualidade(item.qualidade) }}
    </p>
    <p v-if="nivelMaximo" class="falta" data-testid="nivel-maximo">Nível máximo L10 atingido; não é possível aprimorar.</p>
    <template v-else>
      <p v-if="!oficinas.length" class="falta" data-testid="sem-oficinas">
        Nenhuma oficina ativa com nível suficiente para L{{ novoNivel }}.
      </p>
      <div class="campos">
        <label>Oficina
          <Select v-model="oficinaSel" :options="opcoesOficina" option-label="label" option-value="value"
            placeholder="Oficina" data-testid="select-oficina" />
        </label>
        <label>Artesão
          <Select v-model="artesaoSel" :options="opcoesArtesao" option-label="label" option-value="value"
            option-disabled="disabled" placeholder="Artesão" :disabled="!oficina" data-testid="select-artesao" />
        </label>
      </div>
      <p data-testid="pe-minimo">PE mínimo do artesão: {{ peMinimo }}</p>
      <template v-if="oficina && receita">
        <h4>Custo (50% do custo de L{{ novoNivel }})</h4>
        <ul data-testid="custos">
          <li v-for="(q, r) in custo" :key="r" :data-testid="`custo-${r}`" :class="{ falta: faltantes.includes(String(r)) }">
            {{ rotuloRecurso(String(r)) }}: {{ q }}<template v-if="estoque"> (disponível {{ estoque[r] ?? 0 }})</template>
          </li>
        </ul>
      </template>
    </template>
    <p v-if="motivoBloqueio" class="dica" data-testid="motivo">{{ motivoBloqueio }}</p>
    <p v-if="erro" class="erro" role="alert" data-testid="erro-aprimoramento">{{ erro }}</p>
    <div class="acoes">
      <Button label="Aprimorar" data-testid="confirmar-aprimoramento" :disabled="!!motivoBloqueio || enviando"
        :loading="enviando" @click="aprimorar" />
      <Button label="Fechar" severity="secondary" data-testid="fechar-AprimoramentoModal" @click="emit('fechar')" />
    </div>
  </div>
</template>

<style scoped>
h3, h4 { font-family: var(--vl-font-display); color: var(--vl-text); }
li { font-family: var(--vl-font-mono); }
.campos { display: flex; gap: 1rem; flex-wrap: wrap; }
.campos label { display: flex; flex-direction: column; gap: 0.25rem; }
.acoes { display: flex; gap: 0.75rem; margin-top: 0.5rem; flex-wrap: wrap; }
.falta, .erro { color: var(--vl-error); }
.dica { font-size: 0.9em; color: var(--vl-text-2); }
</style>
