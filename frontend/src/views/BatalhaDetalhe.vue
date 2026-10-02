<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import Tab from 'primevue/tab'
import TabList from 'primevue/tablist'
import TabPanel from 'primevue/tabpanel'
import TabPanels from 'primevue/tabpanels'
import Tabs from 'primevue/tabs'
import RodadaReplay from '../components/jogo/RodadaReplay.vue'
import RecompensasBatalha from '../components/jogo/RecompensasBatalha.vue'
import { ROTULOS_RESULTADO, useBatalhas } from '../composables/useBatalhas'

const route = useRoute()
const id = computed(() => Number(route.params.id))
const b = useBatalhas()
const tropa = computed(() => b.batalha.value?.participantes.filter((p) => p.lado === 'TROPA') ?? [])
const inimigos = computed(() => b.batalha.value?.participantes.filter((p) => p.lado === 'INIMIGO') ?? [])

onMounted(() => b.buscar(id.value))
watch(id, (v) => b.buscar(v))
</script>

<template>
  <section data-testid="BatalhaDetalhe">
    <p><router-link to="/jogo/batalhas">← Batalhas</router-link></p>
    <p v-if="b.carregando.value" data-testid="carregando">Carregando...</p>
    <p v-else-if="b.erro.value" class="erro" role="alert" data-testid="erro-batalha">
      {{ /n[ãa]o encontrad/i.test(b.erro.value) ? 'Batalha não encontrada' : b.erro.value }}
    </p>
    <template v-else-if="b.batalha.value">
      <header data-testid="cabecalho">
        <h2>Batalha #{{ b.batalha.value.id }}</h2>
        <p>
          <strong data-testid="resultado" :class="b.batalha.value.resultado === 'VITORIA' ? 'vitoria' : 'derrota'">
            {{ ROTULOS_RESULTADO[b.batalha.value.resultado] ?? b.batalha.value.resultado }}
          </strong>
          · Turno {{ b.batalha.value.turno }} · {{ b.batalha.value.tropaNome }} ·
          Masmorra nível {{ b.batalha.value.masmorraNivel }} (região {{ b.batalha.value.regiaoIndice }}) ·
          <span data-testid="total-rodadas">{{ b.batalha.value.totalRodadas }} rodada(s)</span>
        </p>
      </header>
      <div class="lados">
        <div data-testid="lado-tropa">
          <h3>Tropa</h3>
          <ul>
            <li v-for="p in tropa" :key="`T${p.id}`" :data-testid="`part-TROPA-${p.id}`">
              {{ p.nome }}: {{ p.pvFinal }}/{{ p.pvMax }} PV
            </li>
          </ul>
        </div>
        <div data-testid="lado-inimigo">
          <h3>Inimigos</h3>
          <ul>
            <li v-for="p in inimigos" :key="`I${p.id}`" :data-testid="`part-INIMIGO-${p.id}`">
              {{ p.nome }}: {{ p.pvFinal }}/{{ p.pvMax }} PV
            </li>
          </ul>
        </div>
      </div>
      <Tabs value="rodadas">
        <TabList>
          <Tab value="rodadas">Rodada a rodada</Tab>
          <Tab value="recompensas">Recompensas</Tab>
        </TabList>
        <TabPanels>
          <TabPanel value="rodadas"><RodadaReplay :rodadas="b.batalha.value.rodadas" /></TabPanel>
          <TabPanel value="recompensas"><RecompensasBatalha :recompensas="b.batalha.value.recompensas" /></TabPanel>
        </TabPanels>
      </Tabs>
    </template>
  </section>
</template>

<style scoped>
.lados { display: flex; gap: 2rem; flex-wrap: wrap; }
.vitoria { color: var(--p-green-600, #16a34a); }
.derrota, .erro { color: var(--p-red-500, #dc2626); }
</style>
