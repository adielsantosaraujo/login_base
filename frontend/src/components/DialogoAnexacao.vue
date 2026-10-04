<script setup lang="ts">
import { watch } from 'vue'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import { useAnexacao } from '../composables/useAnexacao'
import type { RegiaoResumo } from '../composables/useMapa'
import { COR_TIPO, ROTULO_TIPO } from '../domain/regioes'
import { COR_TERRENO, ROTULO_TERRENO, ordenarTerrenos } from '../domain/terrenos'

const props = defineProps<{ visivel: boolean; indice: number | null; regiao?: RegiaoResumo | null }>()
const emit = defineEmits<{
  (e: 'update:visivel', v: boolean): void
  (e: 'anexada', indice: number): void
}>()

const { custo, disponivel, suficiente, carregando, enviando, erro, carregar, anexarRegiao } = useAnexacao()

watch(
  () => [props.visivel, props.indice] as const,
  ([v, i]) => {
    if (v && i !== null) {
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
  const r = await anexarRegiao(props.indice)
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
      <div v-if="regiao && regiao.tipo" class="regiao-info" data-testid="regiao-info">
        <span class="tipo" data-testid="regiao-tipo" :style="{ color: COR_TIPO[regiao.tipo] }">{{ ROTULO_TIPO[regiao.tipo] }}</span>
        <ul class="terrenos" data-testid="regiao-terrenos">
          <li v-for="t in ordenarTerrenos(regiao.terrenos ?? [])" :key="t.terreno" :data-testid="`terreno-${t.terreno}`">
            <span class="ponto" :style="{ background: COR_TERRENO[t.terreno] }" aria-hidden="true"></span>
            {{ ROTULO_TERRENO[t.terreno] }} {{ t.percentual }}%
          </li>
        </ul>
      </div>
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
.adjacente { color: var(--vl-accent); margin: 0; }
.regiao-info { display: flex; flex-direction: column; gap: 0.25rem; }
.tipo { font-weight: 700; }
.terrenos { list-style: none; margin: 0; padding: 0; display: flex; flex-wrap: wrap; gap: 0.25rem 0.75rem; }
.ponto { display: inline-block; width: 0.5rem; height: 0.5rem; border-radius: 50%; margin-right: 0.25rem; }
.custos { width: 100%; border-collapse: collapse; text-align: left; }
.custos th, .custos td { padding: 0.25rem 0.5rem; }
.falta { color: var(--vl-error); font-weight: 700; }
.erro { color: var(--vl-error); }
</style>
