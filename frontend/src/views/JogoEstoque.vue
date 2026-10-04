<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import Button from 'primevue/button'
import EstoqueTable from '../components/EstoqueTable.vue'
import { useEstoque } from '../composables/useEstoque'

const { recursos, carregando, erro, carregar } = useEstoque()
let timer: ReturnType<typeof setInterval> | undefined

onMounted(() => {
  carregar()
  timer = setInterval(carregar, 5000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <section class="estoque">
    <div class="estoque-topo">
      <h1>Estoque</h1>
      <Button label="Atualizar" icon="pi pi-refresh" size="small" :loading="carregando" data-testid="atualizar" @click="carregar" />
    </div>
    <p v-if="erro" role="alert" class="erro">{{ erro }}</p>
    <EstoqueTable :recursos="recursos" />
  </section>
</template>

<style scoped>
.estoque-topo { display: flex; justify-content: space-between; align-items: center; gap: 1rem; }
.estoque-topo h1 { font-family: var(--vl-font-display); color: var(--vl-text); }
.erro { color: var(--vl-error); }
</style>
