<script setup lang="ts">
import { ref, watch } from 'vue'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import Select from 'primevue/select'
import { useAnexacao } from '../composables/useAnexacao'
import { ROTULOS_TIPO, type TipoRegiao } from '../composables/useMapa'

const props = defineProps<{ visivel: boolean; indice: number | null }>()
const emit = defineEmits<{
  (e: 'update:visivel', v: boolean): void
  (e: 'anexada', indice: number): void
}>()

const { custo, disponivel, suficiente, carregando, enviando, erro, carregar, anexarRegiao } = useAnexacao()

const tipo = ref<TipoRegiao>('RURAL')
const opcoes = (Object.keys(ROTULOS_TIPO) as TipoRegiao[]).map((v) => ({ valor: v, nome: ROTULOS_TIPO[v] }))

watch(
  () => [props.visivel, props.indice] as const,
  ([v, i]) => {
    if (v && i !== null) {
      tipo.value = 'RURAL'
      carregar(i)
    }
  },
  { immediate: true },
)

function fechar() {
  emit('update:visivel', false)
}

async function confirmar() {
  if (props.indice === null) return
  const r = await anexarRegiao(props.indice, tipo.value)
  if (r) {
    emit('anexada', props.indice)
    fechar()
  }
}
</script>

<template>
  <Dialog
    :visible="visivel"
    modal
    :header="`Anexar região ${indice ?? ''}`"
    :style="{ width: '26rem' }"
    @update:visible="(v: boolean) => emit('update:visivel', v)"
  >
    <p v-if="carregando">Carregando...</p>
    <div v-else-if="custo" class="anexacao" data-testid="dialogo-anexacao">
      <p class="adjacente" data-testid="adjacente">Região adjacente a uma região possuída.</p>
      <table class="custos" data-testid="custos">
        <thead>
          <tr><th>Recurso</th><th>Custo</th><th>Disponível</th></tr>
        </thead>
        <tbody>
          <tr :class="{ falta: disponivel.ouro < custo.ouro }" data-testid="custo-ouro">
            <td>Ouro</td><td>{{ custo.ouro }}</td><td>{{ disponivel.ouro }}</td>
          </tr>
          <tr :class="{ falta: disponivel.madeira < custo.madeira }" data-testid="custo-madeira">
            <td>Madeira</td><td>{{ custo.madeira }}</td><td>{{ disponivel.madeira }}</td>
          </tr>
          <tr :class="{ falta: disponivel.pedra < custo.pedra }" data-testid="custo-pedra">
            <td>Pedra</td><td>{{ custo.pedra }}</td><td>{{ disponivel.pedra }}</td>
          </tr>
        </tbody>
      </table>
      <p v-if="!suficiente" class="erro" data-testid="recursos-insuficientes">Recursos insuficientes</p>
      <label for="tipo-anexacao">Tipo da região</label>
      <Select
        v-model="tipo"
        input-id="tipo-anexacao"
        :options="opcoes"
        option-label="nome"
        option-value="valor"
        data-testid="tipo-anexacao"
        fluid
      />
    </div>
    <p v-if="erro" role="alert" class="erro" data-testid="erro-anexacao">{{ erro }}</p>

    <template #footer>
      <Button label="Cancelar" severity="secondary" data-testid="cancelar-anexacao" @click="fechar" />
      <Button
        label="Anexar"
        data-testid="confirmar-anexacao"
        :loading="enviando"
        :disabled="!custo || !suficiente || enviando"
        @click="confirmar"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.anexacao { display: flex; flex-direction: column; gap: 0.5rem; }
.adjacente { color: #15803d; margin: 0; }
.custos { width: 100%; border-collapse: collapse; text-align: left; }
.custos th, .custos td { padding: 0.25rem 0.5rem; }
.falta { color: #b91c1c; font-weight: 700; }
.erro { color: #b91c1c; }
</style>
