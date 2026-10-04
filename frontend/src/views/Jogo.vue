<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import Drawer from 'primevue/drawer'
import CabecalhoJogo from '../components/vilarejo/CabecalhoJogo.vue'
import RelatorioTurno from '../components/jogo/RelatorioTurno.vue'
import { useTurno } from '../composables/useTurno'

const route = useRoute()
const t = useTurno()
const relatorioAberto = ref(false)

const abaAtiva = computed(() => (route.meta.abaAtiva as string | undefined) ?? '')
const abasInativas = computed(() => !!route.meta.etapaInicial)
const mostrarRelatorio = computed(() => !route.meta.etapaInicial)

onMounted(() => t.iniciar())

function abrirRelatorio() {
  relatorioAberto.value = true
  void t.carregarEventos()
}
</script>

<template>
  <div class="jogo">
    <CabecalhoJogo
      :aba-ativa="abaAtiva"
      :abas-inativas="abasInativas"
      :turno="t.turno.value?.numero ?? 0"
      :segundos-restantes="t.segundosRestantes.value"
      :mostrar-relatorio="mostrarRelatorio"
      @relatorio="abrirRelatorio"
    />
    <p v-if="t.erro.value" class="jogo-erro" role="alert">{{ t.erro.value }}</p>
    <Drawer v-model:visible="relatorioAberto" position="right" header="Relatório do turno" class="jogo-relatorio">
      <RelatorioTurno
        :turno="t.turnoExibido.value"
        :eventos="t.eventos.value"
        :carregando="t.carregando.value"
        :erro="t.erroEventos.value"
        @anteriores="t.carregarTurnoAnterior()"
      />
    </Drawer>
    <RouterView />
  </div>
</template>

<style scoped>
.jogo {
  min-height: 100vh;
  background: var(--vl-bg);
  color: var(--vl-text);
  font-family: var(--vl-font-sans);
}
.jogo-erro {
  margin: 0;
  padding: 8px 24px;
  color: var(--vl-error);
  font-size: 13px;
}
</style>
<style>
.jogo-relatorio {
  width: min(32rem, 100vw);
}
</style>
