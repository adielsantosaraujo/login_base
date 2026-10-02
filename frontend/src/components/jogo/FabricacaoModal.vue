<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import Button from 'primevue/button'
import Select from 'primevue/select'
import {
  custoDoNivelItem, motivoArtesaoIndisponivel, peMinimoDoNivel,
  type OficinaDTO, type ReceitaDTO,
} from '../../composables/useOficina'
import { ATRIBUTOS_ESCOLHA, rotuloBonus, rotuloRecurso, rotuloSubtipo } from '../../composables/useItens'

const props = defineProps<{
  oficina: OficinaDTO
  receitas: ReceitaDTO[]
  /** Estoque disponível por recurso (opcional; habilita o aviso de recursos insuficientes). */
  estoque?: Record<string, number>
  enviando?: boolean
  erro?: string | null
}>()
const emit = defineEmits<{
  (e: 'fabricar', req: { subtipo: string; nivel: number; artesaoId: number; atributoEscolhido?: string }): void
  (e: 'fechar'): void
}>()

const subtipoSel = ref<string | null>(null)
const nivelSel = ref<number | null>(null)
const artesaoSel = ref<number | null>(null)
const atributoSel = ref<string | null>(null)

const receita = computed(() => props.receitas.find((r) => r.subtipo === subtipoSel.value) ?? null)
const opcoesItem = computed(() =>
  props.receitas.map((r) => ({ label: r.nome ?? rotuloSubtipo(r.subtipo), value: r.subtipo })),
)
const opcoesNivel = computed(() =>
  Array.from({ length: Math.max(0, props.oficina.nivelMaximoItem) }, (_, i) => ({ label: `L${i + 1}`, value: i + 1 })),
)
const peMinimo = computed(() => (nivelSel.value ? peMinimoDoNivel(receita.value, nivelSel.value) : 0))
const opcoesArtesao = computed(() =>
  props.oficina.artesaos.map((a) => {
    const motivo = motivoArtesaoIndisponivel(a, peMinimo.value)
    return {
      label: `${a.nome} (PE ${a.peEfetivo})${motivo ? ` - ${motivo}` : ''}`,
      value: a.cidadaoId,
      disabled: !!motivo,
    }
  }),
)
const opcoesAtributo = ATRIBUTOS_ESCOLHA.map((c) => ({ label: rotuloBonus(c), value: c }))
const custo = computed(() => (nivelSel.value ? custoDoNivelItem(receita.value, nivelSel.value) : {}))
const faltantes = computed<string[]>(() => {
  if (!props.estoque) return []
  return Object.entries(custo.value)
    .filter(([r, q]) => (props.estoque?.[r] ?? 0) < q)
    .map(([r]) => r)
})

watch(subtipoSel, () => {
  atributoSel.value = null
  artesaoSel.value = null
})
watch(nivelSel, () => {
  artesaoSel.value = null
})

const motivoBloqueio = computed<string | null>(() => {
  if (!subtipoSel.value) return 'Escolha o item'
  if (!nivelSel.value) return 'Escolha o nível'
  if (artesaoSel.value == null) return 'Escolha o artesão'
  const a = props.oficina.artesaos.find((x) => x.cidadaoId === artesaoSel.value)
  if (!a || motivoArtesaoIndisponivel(a, peMinimo.value)) return 'Artesão indisponível'
  if (receita.value?.exigeAtributo && !atributoSel.value) return 'Escolha o atributo'
  return null
})

function fabricar() {
  if (motivoBloqueio.value || !subtipoSel.value || !nivelSel.value || artesaoSel.value == null) return
  emit('fabricar', {
    subtipo: subtipoSel.value,
    nivel: nivelSel.value,
    artesaoId: artesaoSel.value,
    ...(receita.value?.exigeAtributo && atributoSel.value ? { atributoEscolhido: atributoSel.value } : {}),
  })
}
</script>

<template>
  <div class="fabricacao-modal" data-testid="FabricacaoModal" role="dialog" aria-label="Nova fabricação">
    <h3>Nova fabricação</h3>
    <div class="campos">
      <label>Item
        <Select v-model="subtipoSel" :options="opcoesItem" option-label="label" option-value="value"
          placeholder="Item" data-testid="select-item" />
      </label>
      <label>Nível
        <Select v-model="nivelSel" :options="opcoesNivel" option-label="label" option-value="value"
          placeholder="Nível" data-testid="select-nivel" />
      </label>
      <label>Artesão
        <Select v-model="artesaoSel" :options="opcoesArtesao" option-label="label" option-value="value"
          option-disabled="disabled" placeholder="Artesão" data-testid="select-artesao" />
      </label>
      <label v-if="receita?.exigeAtributo">Atributo
        <Select v-model="atributoSel" :options="opcoesAtributo" option-label="label" option-value="value"
          placeholder="Atributo" data-testid="select-atributo" />
      </label>
    </div>

    <template v-if="nivelSel && receita">
      <p data-testid="pe-minimo">PE mínimo do artesão: {{ peMinimo }}</p>
      <h4>Custo</h4>
      <ul data-testid="custos">
        <li v-for="(q, r) in custo" :key="r" :data-testid="`custo-${r}`" :class="{ falta: faltantes.includes(String(r)) }">
          {{ rotuloRecurso(String(r)) }}: {{ q }}<template v-if="estoque"> (disponível {{ estoque[r] ?? 0 }})</template>
        </li>
      </ul>
      <p v-if="faltantes.length" class="falta" data-testid="aviso-recursos">
        Recursos insuficientes: {{ faltantes.map(rotuloRecurso).join(', ') }}
      </p>
    </template>

    <p v-if="motivoBloqueio" class="dica" data-testid="motivo">{{ motivoBloqueio }}</p>
    <p v-if="erro" class="erro" role="alert" data-testid="erro-fabricacao">{{ erro }}</p>
    <div class="acoes">
      <Button label="Fabricar" data-testid="confirmar-fabricacao" :disabled="!!motivoBloqueio || enviando"
        :loading="enviando" @click="fabricar" />
      <Button label="Fechar" severity="secondary" data-testid="fechar-FabricacaoModal" @click="emit('fechar')" />
    </div>
  </div>
</template>

<style scoped>
.campos { display: flex; gap: 1rem; flex-wrap: wrap; }
.campos label { display: flex; flex-direction: column; gap: 0.25rem; }
.acoes { display: flex; gap: 0.75rem; margin-top: 0.5rem; flex-wrap: wrap; }
.falta, .erro { color: var(--p-red-500, #dc2626); }
.dica { font-size: 0.9em; opacity: 0.8; }
</style>
