<template>
  <div class="quartel-view">
    <h1>Quartel (nível {{ nivelQuartel }})</h1>

    <Card class="formulario">
      <template #title>Treinar unidade</template>
      <template #content>
        <div class="grid">
          <div class="campo">
            <label for="quartel-tipo">Tipo</label>
            <Select
              id="quartel-tipo"
              v-model="tipo"
              :options="tiposDisponiveis"
              optionLabel="nome"
              optionValue="id"
            />
          </div>
          <div class="campo">
            <label for="quartel-arma">Arma ({{ armaExigida ?? '—' }})</label>
            <Select
              id="quartel-arma"
              v-model="armaId"
              :options="armasDisponiveis"
              optionLabel="descricao"
              optionValue="id"
              placeholder="Selecione a arma"
            />
          </div>
          <div class="campo">
            <label for="quartel-armadura">Armadura</label>
            <Select
              id="quartel-armadura"
              v-model="armaduraId"
              :options="armadurasDisponiveis"
              optionLabel="descricao"
              optionValue="id"
              placeholder="Selecione a armadura"
            />
          </div>
        </div>

        <Button label="Treinar" :disabled="!podeTreinar || temOrdemEmAndamento" @click="treinar" />

        <div v-if="temOrdemEmAndamento" class="ordem-em-andamento">
          <ProgressBar :value="percentualOrdem" />
          <p>Treino em andamento</p>
        </div>
      </template>
    </Card>

    <div class="capacidade">
      <h2>Capacidade do exército: {{ unidadesAtivas }}/{{ capacidadeTotal }}</h2>
    </div>

    <h2>Unidades</h2>
    <DataTable :value="vila?.unidades ?? []" dataKey="id" paginator :rows="10">
      <Column field="tipo" header="Tipo" />
      <Column field="hp" header="HP" />
      <Column field="ataque" header="Ataque" />
      <Column field="defesa" header="Defesa" />
      <Column field="alcance" header="Alcance" />
      <Column field="movimento" header="Movimento" />
      <Column field="status" header="Status" />
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import Card from 'primevue/card'
import Select from 'primevue/select'
import Button from 'primevue/button'
import ProgressBar from 'primevue/progressbar'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { useToast } from 'primevue/usetoast'
import { useVila } from '../composables/useVila'
import { jogoAPI } from '../api/jogo'
import { ErroApi } from '../api/http'
import type { ModeloItem, TipoTropa } from '../api/tipos'

// Modelos de armadura reconhecidos (ver spec game-army — Validação dos
// itens): qualquer modelo de armadura serve para qualquer tipo de tropa,
// diferente da arma, que deve ser exatamente a exigida pelo tipo.
const MODELOS_ARMADURA: ModeloItem[] = ['ARMADURA_COURO', 'ARMADURA_FERRO']

const NOMES_TROPA: Record<TipoTropa, string> = {
  SOLDADO: 'Soldado',
  ARQUEIRO: 'Arqueiro',
  LANCEIRO: 'Lanceiro',
}

const TIPOS_TROPA: TipoTropa[] = ['SOLDADO', 'ARQUEIRO', 'LANCEIRO']

const { vila, catalogo, carregar, iniciarPoll, deslocamentoRelogio } = useVila()
const toast = useToast()

const tipo = ref<TipoTropa>('SOLDADO')
const armaId = ref<number | null>(null)
const armaduraId = ref<number | null>(null)

const nivelQuartel = computed(() => {
  return vila.value?.predios.find((p) => p.tipo === 'QUARTEL')?.nivel ?? 0
})

function catalogoDoTipo(t: TipoTropa) {
  return catalogo.value?.tropas[t]
}

// Só lista tipos cujo requisito de nível de quartel já foi atendido (ver
// spec game-army — Tropas liberadas pelo nível do quartel). Enquanto o
// catálogo não chegou, nenhum tipo é oferecido para evitar uma seleção que
// a API ainda não validou.
const tiposDisponiveis = computed(() => {
  return TIPOS_TROPA.filter((t) => {
    const nivelMinimo = catalogoDoTipo(t)?.nivelMinimoQuartel
    if (nivelMinimo === undefined) return false
    return nivelMinimo <= nivelQuartel.value
  }).map((t) => ({ id: t, nome: NOMES_TROPA[t] }))
})

const armaExigida = computed(() => catalogoDoTipo(tipo.value)?.armaExigida)

const armasDisponiveis = computed(() => {
  const modelo = armaExigida.value
  if (!modelo) return []
  return (vila.value?.itens ?? [])
    .filter((item) => item.status === 'DISPONIVEL' && item.modelo === modelo)
    .map((item) => ({ id: item.id, descricao: `${item.modelo} N${item.nivel}` }))
})

const armadurasDisponiveis = computed(() => {
  return (vila.value?.itens ?? [])
    .filter((item) => item.status === 'DISPONIVEL' && MODELOS_ARMADURA.includes(item.modelo))
    .map((item) => ({ id: item.id, descricao: `${item.modelo} N${item.nivel}` }))
})

const temOrdemEmAndamento = computed(() => {
  return vila.value?.ordens.some((o) => o.categoria === 'TREINO') ?? false
})

const unidadesAtivas = computed(() => {
  const unidades = vila.value?.unidades.length ?? 0
  return unidades + (temOrdemEmAndamento.value ? 1 : 0)
})

const capacidadeTotal = computed(() => vila.value?.capacidadeExercito ?? 0)

const podeTreinar = computed(() => {
  if (nivelQuartel.value === 0) return false
  if (!armaId.value || !armaduraId.value) return false
  if (capacidadeTotal.value > 0 && unidadesAtivas.value >= capacidadeTotal.value) return false

  const custoComida = catalogoDoTipo(tipo.value)?.comida ?? 0
  const comidaDisponivel = vila.value?.recursos.COMIDA ?? 0
  if (custoComida > comidaDisponivel) return false

  return true
})

const percentualOrdem = computed(() => {
  const ordem = vila.value?.ordens.find((o) => o.categoria === 'TREINO')
  if (!ordem) return 0
  const inicio = new Date(ordem.iniciadaEm).getTime()
  const conclusao = new Date(ordem.concluiEm).getTime()
  if (conclusao <= inicio) return 100
  const agora = Date.now() + deslocamentoRelogio.value
  return Math.min(100, Math.max(0, ((agora - inicio) / (conclusao - inicio)) * 100))
})

// Ao trocar o tipo de tropa a arma exigida muda, então uma arma já
// selecionada para o tipo anterior deixa de ser válida.
watch(tipo, () => {
  armaId.value = null
})

async function treinar(): Promise<void> {
  if (!armaId.value || !armaduraId.value) return

  try {
    await jogoAPI.treinar(tipo.value, armaId.value, armaduraId.value)
    await carregar()
    armaId.value = null
    armaduraId.value = null
  } catch (e) {
    const mensagem = e instanceof ErroApi ? e.message : 'Erro ao treinar unidade.'
    toast.add({ severity: 'error', summary: 'Erro ao treinar', detail: mensagem, life: 3000 })
  }
}

onMounted(() => {
  carregar()
  iniciarPoll()
})
</script>

<style scoped>
.quartel-view {
  padding: 16px;
}

.formulario {
  margin-bottom: 24px;
  max-width: 700px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ordem-em-andamento {
  margin-top: 16px;
}

.capacidade {
  background: var(--p-content-background);
  padding: 16px;
  margin: 16px 0;
  border-radius: 4px;
}
</style>
