<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import Tab from 'primevue/tab'
import TabList from 'primevue/tablist'
import TabPanel from 'primevue/tabpanel'
import TabPanels from 'primevue/tabpanels'
import Tabs from 'primevue/tabs'
import CidadaoForm from '../components/CidadaoForm.vue'
import FamiliaLiderSelector from '../components/FamiliaLiderSelector.vue'
import { usePopulacao, type Distribuicao } from '../composables/usePopulacao'

const router = useRouter()
const { populacao, distribuicoes, familiaLiderId, carregando, enviando, erro, valido, carregar, confirmar } =
  usePopulacao()

onMounted(carregar)

function atualizar(id: number, d: Distribuicao) {
  distribuicoes.value = { ...distribuicoes.value, [id]: d }
}

async function enviar() {
  if (await confirmar()) router.push('/jogo/mapa')
}
</script>

<template>
  <section class="distribuicao-populacao">
    <h1>Distribuição da população</h1>
    <p>
      Distribua até 20 pontos de característica (máx. 10 cada) e 10 de profissão (máx. 5 cada) por cidadão
      e escolha a família líder. Pontos não usados ficam pendentes.
    </p>

    <p v-if="erro" role="alert" class="erro" data-testid="erro">{{ erro }}</p>
    <p v-if="carregando">Carregando...</p>

    <template v-if="populacao">
      <Tabs :value="String(populacao.familias[0]?.id)">
        <TabList>
          <Tab v-for="f in populacao.familias" :key="f.id" :value="String(f.id)">{{ f.sobrenome }}</Tab>
        </TabList>
        <TabPanels>
          <TabPanel v-for="f in populacao.familias" :key="f.id" :value="String(f.id)">
            <CidadaoForm
              v-for="c in f.cidadaos"
              :key="c.id"
              :cidadao="c"
              :model-value="distribuicoes[c.id] ?? { caracteristicas: {}, profissoes: {} }"
              @update:model-value="(d) => atualizar(c.id, d)"
            />
          </TabPanel>
        </TabPanels>
      </Tabs>

      <FamiliaLiderSelector v-model="familiaLiderId" :familias="populacao.familias" :distribuicoes="distribuicoes" />

      <Button
        label="Confirmar"
        data-testid="confirmar"
        :disabled="!valido || enviando"
        :loading="enviando"
        @click="enviar"
      />
    </template>
  </section>
</template>

<style scoped>
.erro { color: #b91c1c; }
</style>
