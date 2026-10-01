<script setup lang="ts">
import { computed } from 'vue'
import Button from 'primevue/button'
import { formatarContagem } from '../../composables/useTurno'

const props = defineProps<{
  numero: number
  segundosRestantes: number
  erro?: string | null
}>()

defineEmits<{ relatorio: [] }>()

const contagem = computed(() => formatarContagem(props.segundosRestantes))
</script>

<template>
  <div class="barra-turno" data-testid="barra-turno">
    <div class="barra-turno__info">
      <span class="barra-turno__rotulo">Turno</span>
      <span class="barra-turno__numero" data-testid="turno-numero">{{ numero }}</span>
    </div>
    <div class="barra-turno__info">
      <span class="barra-turno__rotulo">Próximo turno em</span>
      <span class="barra-turno__contagem" data-testid="turno-contagem">{{ contagem }}</span>
    </div>
    <span v-if="erro" class="barra-turno__erro" role="alert">{{ erro }}</span>
    <Button
      class="barra-turno__botao"
      label="Relatório"
      icon="pi pi-list"
      size="small"
      severity="contrast"
      data-testid="abrir-relatorio"
      @click="$emit('relatorio')"
    />
  </div>
</template>

<style scoped>
.barra-turno {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem 1.5rem;
  padding: 0.5rem 1rem;
  background: #1e3a5f;
  color: #fff;
}
.barra-turno__info {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
}
.barra-turno__rotulo {
  font-size: 0.85rem;
  opacity: 0.8;
}
.barra-turno__numero {
  font-size: 1.75rem;
  font-weight: 700;
}
.barra-turno__contagem {
  font-size: 1.25rem;
  font-variant-numeric: tabular-nums;
  font-weight: 600;
}
.barra-turno__erro {
  font-size: 0.85rem;
  color: #fecaca;
}
.barra-turno__botao {
  margin-left: auto;
}
</style>
