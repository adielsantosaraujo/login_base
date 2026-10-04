<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Message from 'primevue/message'
import FamiliasList from '../components/jogo/FamiliasList.vue'
import CasamentoDialog from '../components/jogo/CasamentoDialog.vue'
import { useFamilias, type CasamentoRequest, type MembroFamilia } from '../composables/useFamilias'

const { familias, casas, carregando, enviando, erro, carregar, casar } = useFamilias()

const dialogoVisivel = ref(false)
const primeiro = ref<MembroFamilia | null>(null)
const sucesso = ref<string | null>(null)
const erroLista = ref<string | null>(null)

onMounted(async () => {
  await carregar()
  erroLista.value = erro.value
})

function abrir(m: MembroFamilia) {
  primeiro.value = m
  erro.value = null
  sucesso.value = null
  dialogoVisivel.value = true
}

async function confirmar(req: CasamentoRequest) {
  const nova = await casar(req)
  if (nova) {
    dialogoVisivel.value = false
    sucesso.value = `Casamento realizado: família ${nova.sobrenome}`
    await carregar()
  }
}
</script>

<template>
  <section>
    <h1>Famílias</h1>
    <Message v-if="erroLista" severity="error" data-testid="erro-lista">{{ erroLista }}</Message>
    <Message v-if="sucesso" severity="success" data-testid="sucesso">{{ sucesso }}</Message>
    <p v-if="carregando">Carregando...</p>
    <FamiliasList v-else :familias="familias" @casar="abrir" />
    <CasamentoDialog
      v-model:visivel="dialogoVisivel"
      :primeiro="primeiro"
      :familias="familias"
      :casas="casas"
      :erro="erro"
      :enviando="enviando"
      @confirmar="confirmar"
    />
  </section>
</template>

<style scoped>
h1 { font-family: var(--vl-font-display); color: var(--vl-text); }
</style>
