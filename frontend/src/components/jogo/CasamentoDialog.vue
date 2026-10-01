<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import Select from 'primevue/select'
import {
  elegivelParaCasamento,
  type CasaOcupacao,
  type CasamentoRequest,
  type Familia,
  type MembroFamilia,
} from '../../composables/useFamilias'

const props = defineProps<{
  visivel: boolean
  primeiro: MembroFamilia | null
  familias: Familia[]
  casas: CasaOcupacao[]
  erro?: string | null
  enviando?: boolean
}>()
const emit = defineEmits<{
  (e: 'update:visivel', v: boolean): void
  (e: 'confirmar', req: CasamentoRequest): void
}>()

const segundoId = ref<number | null>(null)
const casaId = ref<number | null>(null)
const sobrenome = ref<string | null>(null)

function familiaDe(membroId: number): Familia | undefined {
  return props.familias.find((f) => f.membros.some((m) => m.id === membroId))
}

const candidatos = computed(() => {
  if (!props.primeiro) return []
  const p = props.primeiro
  const lista: { id: number; rotulo: string; sobrenome: string }[] = []
  for (const f of props.familias) {
    for (const m of f.membros) {
      if (m.id === p.id || !elegivelParaCasamento(m)) continue
      lista.push({ id: m.id, rotulo: `${m.nome} (${m.idadeAnos} anos)`, sobrenome: f.sobrenome })
    }
  }
  return lista
})

const casasLivres = computed(() =>
  props.casas
    .filter((c) => c.nucleosLivres > 0)
    .map((c) => ({ id: c.casaId, rotulo: `Casa #${c.casaId} (região ${c.regiaoIndice}, ${c.nucleosLivres} núcleo(s) livre(s))` })),
)

const sobrenomes = computed(() => {
  const s = new Set<string>()
  if (props.primeiro) {
    const f = familiaDe(props.primeiro.id)
    if (f) s.add(f.sobrenome)
  }
  const c = candidatos.value.find((x) => x.id === segundoId.value)
  if (c) s.add(c.sobrenome)
  return [...s]
})

const motivo = computed(() => {
  if (!props.primeiro) return 'Selecione um cidadão'
  if (!elegivelParaCasamento(props.primeiro)) return 'O cidadão precisa ser solteiro e ter 18 anos ou mais'
  if (segundoId.value === null) return 'Escolha o segundo cidadão'
  if (casaId.value === null) return 'Escolha a casa de destino'
  if (!sobrenome.value) return 'Escolha o sobrenome'
  return null
})

watch(
  () => [props.visivel, props.primeiro?.id] as const,
  () => {
    segundoId.value = null
    casaId.value = null
    sobrenome.value = null
  },
)

watch(segundoId, () => {
  if (sobrenome.value && !sobrenomes.value.includes(sobrenome.value)) sobrenome.value = null
})

function fechar() {
  emit('update:visivel', false)
}

function confirmar() {
  if (motivo.value || !props.primeiro) return
  emit('confirmar', {
    cidadao1Id: props.primeiro.id,
    cidadao2Id: segundoId.value as number,
    casaId: casaId.value as number,
    sobrenomeEscolhido: sobrenome.value as string,
  })
}
</script>

<template>
  <Dialog
    :visible="visivel"
    modal
    header="Casamento"
    :style="{ width: '28rem' }"
    @update:visible="(v: boolean) => emit('update:visivel', v)"
  >
    <div class="campos" data-testid="casamento-dialog">
      <p v-if="primeiro">
        Casar <strong>{{ primeiro.nome }}</strong> com:
      </p>
      <Select
        v-model="segundoId"
        data-testid="select-segundo"
        :options="candidatos"
        option-label="rotulo"
        option-value="id"
        placeholder="Segundo cidadão"
        fluid
      />
      <p v-if="candidatos.length === 0" data-testid="sem-candidatos">Nenhum candidato elegível (solteiro, 18+ anos).</p>
      <Select
        v-model="casaId"
        data-testid="select-casa"
        :options="casasLivres"
        option-label="rotulo"
        option-value="id"
        placeholder="Casa de destino"
        fluid
      />
      <p v-if="casasLivres.length === 0" data-testid="sem-casas">Nenhuma casa com núcleo livre.</p>
      <Select
        v-model="sobrenome"
        data-testid="select-sobrenome"
        :options="sobrenomes"
        placeholder="Sobrenome da nova família"
        fluid
      />
      <Message v-if="erro" severity="error" data-testid="erro-casamento">{{ erro }}</Message>
      <small v-else-if="motivo" data-testid="motivo">{{ motivo }}</small>
    </div>
    <template #footer>
      <Button label="Cancelar" text data-testid="cancelar" @click="fechar" />
      <Button
        label="Casar"
        data-testid="confirmar-casamento"
        :disabled="!!motivo || enviando"
        :loading="enviando"
        @click="confirmar"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.campos {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
</style>
