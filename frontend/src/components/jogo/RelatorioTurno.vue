<script setup lang="ts">
import { computed, ref } from 'vue'
import Button from 'primevue/button'
import Select from 'primevue/select'
import {
  type EventoTurno,
  ROTULOS_EVENTO,
  iconeEvento,
  rotuloEvento,
} from '../../composables/useTurno'

const props = defineProps<{
  turno: number
  eventos: EventoTurno[]
  carregando?: boolean
  erro?: string | null
}>()

defineEmits<{ anteriores: [] }>()

const filtro = ref<string | null>(null)

const opcoes = [
  { valor: null, rotulo: 'Todos' },
  ...Object.entries(ROTULOS_EVENTO).map(([valor, rotulo]) => ({ valor, rotulo })),
]

const filtrados = computed(() =>
  filtro.value ? props.eventos.filter((e) => e.tipo === filtro.value) : props.eventos,
)

function hora(timestamp: string): string {
  const d = new Date(timestamp)
  return Number.isNaN(d.getTime()) ? timestamp : d.toLocaleString('pt-BR')
}
</script>

<template>
  <section class="relatorio" data-testid="relatorio-turno">
    <header class="relatorio__cabecalho">
      <h3 class="relatorio__titulo">Relatório do turno {{ turno }}</h3>
      <Select
        v-model="filtro"
        :options="opcoes"
        option-label="rotulo"
        option-value="valor"
        aria-label="Filtrar por tipo de evento"
        data-testid="filtro-tipo"
      />
    </header>

    <p v-if="erro" class="relatorio__erro" role="alert">{{ erro }}</p>
    <p v-else-if="carregando" class="relatorio__vazio">Carregando...</p>
    <p v-else-if="filtrados.length === 0" class="relatorio__vazio" data-testid="sem-eventos">
      Nenhum evento neste turno.
    </p>
    <ul v-else class="relatorio__lista">
      <li v-for="e in filtrados" :key="e.id" class="relatorio__item" data-testid="evento">
        <i :class="iconeEvento(e.tipo)" :title="rotuloEvento(e.tipo)" aria-hidden="true"></i>
        <div class="relatorio__texto">
          <span class="relatorio__tipo">{{ rotuloEvento(e.tipo) }}</span>
          <span>{{ e.mensagem }}</span>
        </div>
        <time class="relatorio__hora" :datetime="e.timestamp">{{ hora(e.timestamp) }}</time>
      </li>
    </ul>

    <Button
      v-if="turno > 1"
      label="Carregar anteriores"
      icon="pi pi-history"
      size="small"
      severity="secondary"
      :disabled="carregando"
      data-testid="carregar-anteriores"
      @click="$emit('anteriores')"
    />
  </section>
</template>

<style scoped>
.relatorio__cabecalho {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}
.relatorio__titulo {
  margin: 0;
}
.relatorio__lista {
  list-style: none;
  margin: 1rem 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.relatorio__item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.5rem;
  border: 1px solid var(--p-content-border-color, #ddd);
  border-radius: 6px;
}
.relatorio__texto {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}
.relatorio__tipo {
  font-weight: 600;
}
.relatorio__hora {
  font-size: 0.8rem;
  opacity: 0.7;
}
.relatorio__vazio,
.relatorio__erro {
  margin: 1rem 0;
}
.relatorio__erro {
  color: #b91c1c;
}
@media (max-width: 600px) {
  .relatorio__item {
    flex-wrap: wrap;
  }
}
</style>
