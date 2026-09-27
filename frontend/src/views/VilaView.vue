<script setup lang="ts">
import { onMounted } from 'vue'
import { useVila } from '../composables/useVila'
import CartaoPredio from '../components/CartaoPredio.vue'

const { vila, carregar, iniciarPoll } = useVila()

onMounted(() => {
  carregar()
  iniciarPoll()
})
</script>

<template>
  <div class="vila-view">
    <h1>{{ vila?.nome ?? 'Vila' }}</h1>

    <h2>Prédios</h2>
    <div class="grid-predios">
      <CartaoPredio v-for="predio in vila?.predios" :key="predio.tipo" :predio="predio" />
    </div>

    <h2>Ordens</h2>
    <div v-if="vila?.ordens.length">
      <div v-for="ordem in vila.ordens" :key="ordem.id" class="ordem">
        <p>{{ ordem.categoria }} {{ ordem.alvo }}<template v-if="ordem.nivel"> — nível {{ ordem.nivel }}</template></p>
        <p>Conclusão: {{ new Date(ordem.concluiEm).toLocaleString() }}</p>
      </div>
    </div>
    <p v-else>Nenhuma ordem em andamento</p>
  </div>
</template>

<style scoped>
.grid-predios {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  padding: 16px 0;
}

.ordem {
  padding: 8px;
  border: 1px solid var(--p-content-border-color, #dee2e6);
  border-radius: 6px;
  margin: 8px 0;
}
</style>
