<template>
  <div class="fazenda-view">
    <h1>Fazenda (nível {{ nivelFazenda }})</h1>

    <h2>Canteiros</h2>
    <DataTable :value="vila?.canteiros ?? []" dataKey="posicao">
      <Column field="posicao" header="Posição" />
      <Column field="cultivo" header="Cultivo atual" />
      <Column field="producaoComidaPorHora" header="Comida/h" />
      <Column header="Novo cultivo">
        <template #body="{ data }">
          <Select
            v-model="culturaEscolhida[data.posicao]"
            :options="cultivosDisponiveis"
            optionLabel="label"
            optionValue="value"
          />
        </template>
      </Column>
      <Column header="Ação">
        <template #body="{ data }">
          <Button
            icon="pi pi-check"
            label="Plantar"
            :disabled="naoPodePlantar(data.posicao)"
            @click="plantar(data.posicao)"
          />
        </template>
      </Column>
    </DataTable>

    <h2>Estoque de sementes</h2>
    <div class="estoque">
      <div v-for="[cultivo, quantidade] in Object.entries(vila?.sementes ?? {})" :key="cultivo">
        <span>{{ cultivo }}: {{ quantidade }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Select from 'primevue/select'
import Button from 'primevue/button'
import { useToast } from 'primevue/usetoast'
import { useVila } from '../composables/useVila'
import { jogoAPI } from '../api/jogo'
import { ErroApi } from '../api/http'
import type { Cultivo } from '../api/tipos'

// Conjunto fixo de cultivos reconhecidos pelo jogo (ver spec game-farming).
// TRIGO nunca exige semente; os demais exigem 1 semente do próprio tipo.
const CULTIVOS: Cultivo[] = ['TRIGO', 'MILHO', 'BATATA', 'ABOBORA_DOURADA']

const { vila, carregar, iniciarPoll } = useVila()
const toast = useToast()

const culturaEscolhida = ref<Record<number, Cultivo>>({})

const nivelFazenda = computed(() => {
  return vila.value?.predios.find((p) => p.tipo === 'FAZENDA')?.nivel ?? 0
})

const cultivosDisponiveis = computed(() => {
  return CULTIVOS.map((cultivo) => ({ label: cultivo, value: cultivo }))
})

// Preenche a seleção de cada canteiro com o cultivo atual assim que ele
// aparece pela primeira vez, sem sobrescrever uma escolha já feita pelo
// jogador em uma sincronização seguinte.
watch(
  () => vila.value?.canteiros,
  (canteiros) => {
    for (const canteiro of canteiros ?? []) {
      if (!(canteiro.posicao in culturaEscolhida.value)) {
        culturaEscolhida.value[canteiro.posicao] = canteiro.cultivo
      }
    }
  },
  { immediate: true },
)

function naoPodePlantar(posicao: number): boolean {
  const cultivo = culturaEscolhida.value[posicao]
  if (!cultivo || cultivo === 'TRIGO') return false
  const sementes = vila.value?.sementes[cultivo] ?? 0
  return sementes <= 0
}

async function plantar(posicao: number): Promise<void> {
  const cultivo = culturaEscolhida.value[posicao]
  if (!cultivo) return

  try {
    await jogoAPI.plantar(posicao, cultivo)
    await carregar()
  } catch (e) {
    const mensagem = e instanceof ErroApi ? e.message : 'Erro ao plantar cultivo.'
    toast.add({ severity: 'error', summary: 'Erro ao plantar', detail: mensagem, life: 3000 })
  }
}

onMounted(() => {
  carregar()
  iniciarPoll()
})
</script>

<style scoped>
.fazenda-view {
  padding: 16px;
}

.estoque {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 8px;
  padding: 16px;
  background: var(--p-content-background);
}
</style>
