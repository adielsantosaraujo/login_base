<script setup lang="ts">
import { computed, onMounted } from 'vue'
import Button from 'primevue/button'
import Message from 'primevue/message'
import GradeRegiao from '../GradeRegiao.vue'
import type { Ladrilho } from '../../composables/useMapa'
import { ROTULOS_CONSTRUCAO, ROTULOS_JAZIDA, rotulo } from '../../composables/useMapa'
import { useMarcacoes } from '../../composables/useMarcacoes'

const props = defineProps<{ construcaoId: number }>()
const emit = defineEmits<{ (e: 'fechar'): void; (e: 'atualizado'): void }>()

const { predio, marcacoes, ladrilhos, carregando, salvando, erro, maximo, usado, jazida, candidatos, carregar, marcar, desmarcar } =
  useMarcacoes()

onMounted(() => carregar(props.construcaoId))

const destaques = computed(() => marcacoes.value.map((m) => ({ x: m.x, y: m.y })))

async function aoClicar(p: { x: number; y: number; ladrilho: Ladrilho | null }) {
  if (salvando.value || carregando.value) return
  const marcada = marcacoes.value.some((m) => m.x === p.x && m.y === p.y)
  const ok = marcada ? await desmarcar(props.construcaoId, p.x, p.y) : await marcar(props.construcaoId, p.x, p.y)
  if (ok) emit('atualizado')
}
</script>

<template>
  <div class="painel-marcacao" data-testid="PainelMarcacao">
    <h3>
      Marcação
      <span v-if="predio">- {{ rotulo(ROTULOS_CONSTRUCAO, predio.tipo) }}</span>
    </h3>
    <p v-if="carregando" data-testid="marcacao-carregando">Carregando...</p>
    <template v-else-if="predio">
      <p data-testid="marcacao-contador">{{ usado }}/{{ maximo }} marcados</p>
      <p v-if="jazida" class="dica" data-testid="marcacao-dica">
        Jazida: {{ rotulo(ROTULOS_JAZIDA, jazida) }} - {{ candidatos.length }} ladrilho(s) disponível(is). Clique para marcar ou desmarcar.
      </p>
      <GradeRegiao :ladrilhos="ladrilhos" :destaques="destaques" :selecionaveis="true" @clique-ladrilho="aoClicar" />
    </template>
    <Message v-if="erro" severity="error" data-testid="marcacao-erro">{{ erro }}</Message>
    <Button label="Fechar" severity="secondary" size="small" data-testid="fechar-PainelMarcacao" @click="emit('fechar')" />
  </div>
</template>

<style scoped>
.painel-marcacao { display: flex; flex-direction: column; gap: 0.5rem; }
h3 { font-family: var(--vl-font-display); }
.dica { font-size: 0.85rem; color: var(--vl-text-3); margin: 0; }
</style>
