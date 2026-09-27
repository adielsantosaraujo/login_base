<template>
  <div class="forja-view">
    <h1>Forja (nível {{ nivelForja }})</h1>

    <Card class="formulario">
      <template #title>Nova ordem</template>
      <template #content>
        <div class="campos">
          <div class="campo">
            <label for="forja-modelo">Modelo</label>
            <Select
              id="forja-modelo"
              v-model="modelo"
              :options="modelosDisponiveis"
              optionLabel="label"
              optionValue="value"
            />
          </div>
          <div class="campo">
            <label for="forja-nivel">Nível</label>
            <InputNumber id="forja-nivel" v-model="nivel" :min="1" :max="5" />
          </div>
          <div class="campo">
            <label for="forja-quantidade">Quantidade</label>
            <InputNumber id="forja-quantidade" v-model="quantidade" :min="1" :max="5" />
          </div>
        </div>

        <div class="custo">
          <p class="custo-titulo">Custo total:</p>
          <ul class="custo-lista">
            <li v-for="[recurso, valor] in custoCalculado" :key="recurso">{{ recurso }}: {{ valor }}</li>
          </ul>
        </div>

        <Button label="Forjar" :disabled="!podeForjar" @click="forjar" />

        <div v-if="temOrdemEmAndamento" class="ordem-em-andamento">
          <ProgressBar :value="percentualOrdem" />
          <p>Ordem em andamento</p>
        </div>
        <small v-else-if="nivelForja === 0" class="aviso-bloqueio">
          Forja em nível 0: melhore-a para poder forjar.
        </small>
        <small v-else-if="nivel > nivelForja" class="aviso-bloqueio">
          Nível solicitado acima do nível da forja.
        </small>
      </template>
    </Card>

    <h2>Inventário</h2>
    <DataTable
      v-model:filters="filtros"
      :value="vila?.itens ?? []"
      dataKey="id"
      paginator
      :rows="10"
      filterDisplay="row"
      responsiveLayout="scroll"
    >
      <Column field="modelo" header="Modelo" :showFilterMenu="false">
        <template #filter="{ filterModel, filterCallback }">
          <Select
            v-model="filterModel.value"
            :options="modelosDisponiveis"
            optionLabel="label"
            optionValue="value"
            placeholder="Todos"
            showClear
            @update:modelValue="filterCallback()"
          />
        </template>
      </Column>
      <Column field="nivel" header="Nível" />
      <Column field="ataque" header="Ataque" />
      <Column field="defesa" header="Defesa" />
      <Column field="alcance" header="Alcance" />
      <Column field="status" header="Status" :showFilterMenu="false">
        <template #filter="{ filterModel, filterCallback }">
          <Select
            v-model="filterModel.value"
            :options="statusDisponiveis"
            optionLabel="label"
            optionValue="value"
            placeholder="Todos"
            showClear
            @update:modelValue="filterCallback()"
          />
        </template>
      </Column>
      <Column field="origem" header="Origem" />
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import Card from 'primevue/card'
import Select from 'primevue/select'
import InputNumber from 'primevue/inputnumber'
import Button from 'primevue/button'
import ProgressBar from 'primevue/progressbar'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { FilterMatchMode } from '@primevue/core/api'
import { useToast } from 'primevue/usetoast'
import { useVila } from '../composables/useVila'
import { jogoAPI } from '../api/jogo'
import { ErroApi } from '../api/http'
import type { ModeloItem, StatusItem, TipoRecurso } from '../api/tipos'

// Modelos forjáveis e status de item reconhecidos pelo jogo (ver spec
// game-forge). Lista fixa no frontend, igual ao padrão usado em
// FazendaView.vue para os cultivos.
const MODELOS: ModeloItem[] = ['ESPADA', 'LANCA', 'ARCO', 'ARMADURA_COURO', 'ARMADURA_FERRO']
const STATUS: StatusItem[] = ['DISPONIVEL', 'RESERVADO', 'EQUIPADO']

const { vila, catalogo, carregar, iniciarPoll } = useVila()
const toast = useToast()

const modelo = ref<ModeloItem>('ESPADA')
const nivel = ref<number>(1)
const quantidade = ref<number>(1)
const forjando = ref(false)

const modelosDisponiveis = computed(() => MODELOS.map((m) => ({ label: m, value: m })))
const statusDisponiveis = computed(() => STATUS.map((s) => ({ label: s, value: s })))

const nivelForja = computed(() => {
  return vila.value?.predios.find((p) => p.tipo === 'FORJA')?.nivel ?? 0
})

// Custo total = custoBase (por unidade no nível 1, lista `[{recurso,
// quantidade}]`) × nível × quantidade (spec game-forge — Receitas e ordem de
// forja).
const custoCalculado = computed<Array<[TipoRecurso, number]>>(() => {
  const base = catalogo.value?.modelosItem[modelo.value]?.custoBase
  if (!base) return []
  return base.map((item) => [item.recurso, item.quantidade * nivel.value * quantidade.value])
})

const podeForjar = computed(() => {
  if (forjando.value) return false
  if (nivelForja.value === 0) return false
  if (nivel.value > nivelForja.value) return false

  const recursos = vila.value?.recursos
  if (!recursos) return false
  return custoCalculado.value.every(([recurso, custo]) => (recursos[recurso] ?? 0) >= custo)
})

const ordemForja = computed(() => vila.value?.ordens.find((o) => o.categoria === 'FORJA') ?? null)

const temOrdemEmAndamento = computed(() => ordemForja.value !== null)

const percentualOrdem = computed(() => {
  const ordem = ordemForja.value
  if (!ordem || !vila.value) return 0
  const inicio = new Date(ordem.iniciadaEm).getTime()
  const conclusao = new Date(ordem.concluiEm).getTime()
  const agora = new Date(vila.value.agora).getTime()
  if (conclusao <= inicio) return 100
  return Math.min(100, Math.max(0, ((agora - inicio) / (conclusao - inicio)) * 100))
})

const filtros = ref({
  modelo: { value: null, matchMode: FilterMatchMode.EQUALS },
  status: { value: null, matchMode: FilterMatchMode.EQUALS },
})

async function forjar(): Promise<void> {
  forjando.value = true
  try {
    await jogoAPI.forjar(modelo.value, nivel.value, quantidade.value)
    await carregar()
    toast.add({ severity: 'success', summary: 'Ordem criada', detail: 'Ordem de forja iniciada.', life: 3000 })
  } catch (e) {
    const mensagem = e instanceof ErroApi ? e.message : 'Não foi possível criar a ordem de forja.'
    toast.add({ severity: 'error', summary: 'Erro ao forjar', detail: mensagem, life: 3000 })
  } finally {
    forjando.value = false
  }
}

onMounted(() => {
  carregar()
  iniciarPoll()
})
</script>

<style scoped>
.forja-view {
  padding: 16px;
}

.formulario {
  margin-bottom: 24px;
  max-width: 700px;
}

.campos {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.custo {
  background: var(--p-content-background);
  padding: 16px;
  margin: 16px 0;
  border-radius: 4px;
}

.custo-titulo {
  margin: 0 0 4px 0;
}

.custo-lista {
  margin: 0;
  padding-left: 1.2em;
}

.ordem-em-andamento {
  margin-top: 16px;
}

.aviso-bloqueio {
  display: block;
  color: var(--p-red-500, #ef4444);
  margin-top: 8px;
}
</style>
