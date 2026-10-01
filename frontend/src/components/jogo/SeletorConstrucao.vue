<script setup lang="ts">
import { computed, ref } from 'vue'
import Button from 'primevue/button'
import { permitidoNaRegiao, type CatalogoConstrucao } from '../../composables/useConstrucoes'
import { ROTULOS_TIPO, rotulo } from '../../composables/useMapa'

const props = defineProps<{
  catalogo: CatalogoConstrucao[]
  tipoRegiao: string | null
  /** Quantidade disponível por recurso (chave = recurso). Se omitido, não valida. */
  estoque?: Record<string, number>
  criando?: boolean
  erro?: string | null
}>()

const emit = defineEmits<{
  (e: 'construir', tipo: string): void
  (e: 'cancelar'): void
}>()

const ABAS = ['TODOS', 'URBANA', 'RURAL', 'COLETA'] as const
const aba = ref<(typeof ABAS)[number]>('TODOS')
const escolhido = ref<string | null>(null)

const lista = computed(() =>
  props.catalogo.filter((c) => aba.value === 'TODOS' || c.regiao === aba.value),
)

const item = computed(() => props.catalogo.find((c) => c.tipo === escolhido.value) ?? null)

function faltantes(c: CatalogoConstrucao): string[] {
  if (!props.estoque) return []
  return Object.entries(c.custoN1)
    .filter(([r, q]) => (props.estoque![r] ?? 0) < q)
    .map(([r]) => r)
}

const motivoBloqueio = computed(() => {
  const c = item.value
  if (!c) return 'Selecione um prédio'
  if (!permitidoNaRegiao(c, props.tipoRegiao)) return 'Não permitido nesta região'
  const f = faltantes(c)
  if (f.length) return `Recursos insuficientes: ${f.join(', ')}`
  return null
})

function custo(c: CatalogoConstrucao): string {
  return Object.entries(c.custoN1).map(([r, q]) => `${q} ${r}`).join(', ') || '-'
}
</script>

<template>
  <div class="seletor" data-testid="seletor-construcao">
    <div class="abas" role="tablist">
      <button
        v-for="a in ABAS"
        :key="a"
        type="button"
        role="tab"
        :aria-selected="aba === a"
        :class="{ ativa: aba === a }"
        :data-testid="`aba-${a.toLowerCase()}`"
        @click="aba = a"
      >
        {{ a === 'TODOS' ? 'Todos' : rotulo(ROTULOS_TIPO, a) }}
      </button>
    </div>

    <ul class="itens">
      <li v-for="c in lista" :key="c.tipo">
        <button
          type="button"
          class="item"
          :class="{ escolhido: escolhido === c.tipo }"
          :disabled="!permitidoNaRegiao(c, tipoRegiao)"
          :title="permitidoNaRegiao(c, tipoRegiao) ? '' : 'Não permitido nesta região'"
          :data-testid="`item-${c.tipo}`"
          @click="escolhido = c.tipo"
        >
          <strong>{{ c.nome }}</strong>
          <span v-if="!permitidoNaRegiao(c, tipoRegiao)" aria-hidden="true"> ✕</span>
          <small>{{ rotulo(ROTULOS_TIPO, c.regiao) }} · {{ c.tamanho }}x{{ c.tamanho }} · PO {{ c.poN1 }}</small>
          <small>Custo: {{ custo(c) }}</small>
          <small v-if="c.profissoes?.length">Profissões: {{ c.profissoes?.join(', ') }}</small>
        </button>
      </li>
    </ul>

    <p v-if="erro" role="alert" class="erro" data-testid="erro-construcao">{{ erro }}</p>

    <div class="acoes">
      <span v-if="item" data-testid="preview">{{ item.nome }} - {{ custo(item) }}</span>
      <span :title="motivoBloqueio ?? ''">
        <Button
          label="Construir"
          size="small"
          :disabled="!!motivoBloqueio || criando"
          :loading="criando"
          data-testid="construir"
          @click="item && emit('construir', item.tipo)"
        />
      </span>
      <Button label="Cancelar" severity="secondary" size="small" data-testid="cancelar" @click="emit('cancelar')" />
    </div>
    <small v-if="motivoBloqueio && item" data-testid="motivo">{{ motivoBloqueio }}</small>
  </div>
</template>

<style scoped>
.abas { display: flex; gap: 0.25rem; margin-bottom: 0.5rem; }
.abas button { padding: 0.25rem 0.6rem; border: 1px solid #cbd5e1; background: transparent; cursor: pointer; border-radius: 4px; color: inherit; }
.abas button.ativa { background: #2563eb; color: #fff; }
.itens { list-style: none; padding: 0; margin: 0; display: grid; gap: 0.4rem; max-height: 320px; overflow: auto; }
.item { width: 100%; text-align: left; display: flex; flex-direction: column; padding: 0.4rem 0.6rem; border: 1px solid #cbd5e1; border-radius: 4px; background: transparent; cursor: pointer; color: inherit; }
.item.escolhido { border-color: #2563eb; border-width: 2px; }
.item:disabled { opacity: 0.5; cursor: not-allowed; }
.acoes { display: flex; gap: 0.5rem; align-items: center; margin-top: 0.5rem; flex-wrap: wrap; }
.erro { color: #b91c1c; }
</style>
