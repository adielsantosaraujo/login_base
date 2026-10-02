<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'
import { useCidadao } from '../composables/useCidadao'
import { ROTULOS_PROFISSAO } from '../composables/usePopulacao'
import { rotulo } from '../composables/useMapa'
import CaracteristicasTab from '../components/jogo/CaracteristicasTab.vue'
import ProfissoesTab from '../components/jogo/ProfissoesTab.vue'
import EquipamentoTab from '../components/jogo/EquipamentoTab.vue'

const route = useRoute()
const { cidadao, carregando, enviando, erro, carregar, distribuir } = useCidadao()

const CARS = ['VIT', 'FOR', 'VEL', 'INT', 'CAR']
function totalCar(c: string): string {
  const base = cidadao.value?.caracteristicas?.[c] ?? 0
  const total = cidadao.value?.caracteristicasTotais?.[c] ?? base
  const bonus = total - base
  return bonus ? `${c} ${total} (${bonus > 0 ? '+' : ''}${bonus})` : `${c} ${total}`
}

watch(() => route.params.id, (id) => { if (id) carregar(String(id)) }, { immediate: true })

const ESTADOS: Record<string, string> = { SAUDAVEL: 'Saudável', FERIDO: 'Ferido' }

const alocacao = computed(() => {
  const c = cidadao.value
  if (!c || c.construcaoId == null) return 'Sem alocação'
  const prof = c.profissaoTrabalho ? ` como ${rotulo(ROTULOS_PROFISSAO, c.profissaoTrabalho)}` : ''
  return `Prédio #${c.construcaoId}${prof}`
})

async function enviar(tipo: 'caracteristicas' | 'profissoes', pontos: Record<string, number>) {
  if (!cidadao.value) return
  await distribuir(cidadao.value.id, { [tipo]: pontos })
}
</script>

<template>
  <section class="painel">
    <p v-if="erro" role="alert" class="erro" data-testid="erro">{{ erro }}</p>
    <p v-if="carregando && !cidadao">Carregando...</p>

    <template v-if="cidadao">
      <header data-testid="cabecalho">
        <h1>{{ cidadao.nome }}</h1>
        <dl class="dados">
          <div><dt>Sexo</dt><dd data-testid="sexo">{{ cidadao.sexo === 'M' ? 'Masculino' : 'Feminino' }}</dd></div>
          <div><dt>Idade</dt><dd data-testid="idade">{{ cidadao.idadeAnos }} anos</dd></div>
          <div><dt>Estado</dt><dd data-testid="estado">{{ ESTADOS[cidadao.estado] ?? cidadao.estado }}</dd></div>
          <div v-if="cidadao.faminto"><dt>Fome</dt><dd data-testid="faminto">Faminto</dd></div>
          <div>
            <dt>Família</dt>
            <dd data-testid="familia">
              <router-link v-if="cidadao.familiaId != null" to="/jogo/familias">{{ cidadao.familiaNome }}</router-link>
              <template v-else>-</template>
            </dd>
          </div>
          <div v-if="cidadao.conjuge">
            <dt>Cônjuge</dt>
            <dd data-testid="conjuge">
              <router-link :to="`/jogo/cidadao/${cidadao.conjuge.id}`">{{ cidadao.conjuge.nome }}</router-link>
            </dd>
          </div>
          <div v-if="cidadao.vidaMaxima != null"><dt>Vida máxima</dt><dd data-testid="vida-maxima">{{ cidadao.vidaMaxima }}</dd></div>
          <div v-if="cidadao.caracteristicasTotais">
            <dt>Características totais</dt>
            <dd data-testid="car-totais">{{ CARS.map(totalCar).join(' · ') }}</dd>
          </div>
          <div><dt>Alocação</dt><dd data-testid="alocacao">{{ alocacao }}</dd></div>
        </dl>
      </header>

      <Tabs value="caracteristicas">
        <TabList>
          <Tab value="caracteristicas">Características</Tab>
          <Tab value="profissoes">Profissões</Tab>
          <Tab value="equipamento">Equipamento</Tab>
        </TabList>
        <TabPanels>
          <TabPanel value="caracteristicas">
            <CaracteristicasTab
              :caracteristicas="cidadao.caracteristicas"
              :pontos-pendentes="cidadao.pontosCarPendentes"
              :enviando="enviando"
              @distribuir="(p) => enviar('caracteristicas', p)"
            />
          </TabPanel>
          <TabPanel value="profissoes">
            <ProfissoesTab
              :profissoes="cidadao.profissoes"
              :pontos-pendentes="cidadao.pontosProfPendentes"
              :enviando="enviando"
              @distribuir="(p) => enviar('profissoes', p)"
            />
          </TabPanel>
          <TabPanel value="equipamento"><EquipamentoTab
              :cidadao-id="cidadao.id"
              :equipamento="cidadao.equipamento"
              :em-expedicao="cidadao.emExpedicao"
              @atualizado="() => carregar(cidadao!.id)"
            /></TabPanel>
        </TabPanels>
      </Tabs>
    </template>
  </section>
</template>

<style scoped>
.painel { padding: 1rem; }
.dados { display: grid; grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr)); gap: 0.5rem 1rem; margin: 0 0 1rem; }
.dados dt { font-size: 0.8rem; opacity: 0.7; }
.dados dd { margin: 0; }
.erro { color: var(--p-red-500, #c00); }
</style>
