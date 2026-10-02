<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, RouterView } from 'vue-router'
import Drawer from 'primevue/drawer'
import BarraTurno from '../components/jogo/BarraTurno.vue'
import RelatorioTurno from '../components/jogo/RelatorioTurno.vue'
import { useTurno } from '../composables/useTurno'

const t = useTurno()
const relatorioAberto = ref(false)

onMounted(() => t.iniciar())

function abrirRelatorio() {
  relatorioAberto.value = true
  void t.carregarEventos()
}
</script>

<template>
  <div class="jogo">
    <div class="jogo-topo">
      <BarraTurno
        :numero="t.turno.value?.numero ?? 0"
        :segundos-restantes="t.segundosRestantes.value"
        :erro="t.erro.value"
        @relatorio="abrirRelatorio"
      />
    </div>
    <Drawer v-model:visible="relatorioAberto" position="right" header="Relatório do turno" class="jogo-relatorio">
      <RelatorioTurno
        :turno="t.turnoExibido.value"
        :eventos="t.eventos.value"
        :carregando="t.carregando.value"
        :erro="t.erroEventos.value"
        @anteriores="t.carregarTurnoAnterior()"
      />
    </Drawer>
    <nav class="jogo-menu">
      <RouterLink to="/jogo/mapa">Mapa</RouterLink>
      <RouterLink to="/jogo/estoque">Estoque</RouterLink>
      <RouterLink to="/jogo/familias">Famílias</RouterLink>
      <RouterLink to="/jogo/mercado">Mercado</RouterLink>
      <RouterLink to="/jogo/inventario">Inventário</RouterLink>
    </nav>
    <RouterView />
  </div>
</template>

<style scoped>
.jogo-menu {
  display: flex;
  gap: 1rem;
  padding: 0.5rem 1rem;
}
</style>
<style>
.jogo-relatorio {
  width: min(32rem, 100vw);
}
</style>
