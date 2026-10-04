<script setup lang="ts">
import { computed } from 'vue'
import ListaTerrenos from './ListaTerrenos.vue'
import { ROTULO_TIPO, type RegiaoPrevia } from '../../domain/regioes'
import { ordenarTerrenos } from '../../domain/terrenos'

const props = defineProps<{ regiao: RegiaoPrevia | null }>()
const titulo = computed(() =>
  props.regiao
    ? `Região ${String(props.regiao.indice).padStart(2, '0')} · ${ROTULO_TIPO[props.regiao.tipo]}`
    : '',
)
const itens = computed(() =>
  props.regiao
    ? ordenarTerrenos(props.regiao.terrenos).map((t) => ({ terreno: t.terreno, valor: t.percentual }))
    : [],
)
</script>

<template>
  <section class="rf-card" aria-label="Em foco" data-testid="regiao-foco">
    <h3 class="rf-titulo">Em foco</h3>
    <template v-if="regiao">
      <p class="rf-regiao" data-testid="foco-titulo">{{ titulo }}</p>
      <ListaTerrenos :itens="itens" :maximo="100" sufixo="%" />
    </template>
    <p v-else class="rf-vazio">Passe o mouse numa região</p>
  </section>
</template>

<style scoped>
.rf-card {
  padding: 14px;
  border-radius: var(--vl-radius-card);
  background: var(--vl-surface-2);
  border: 1px solid var(--vl-border);
}
.rf-titulo {
  margin: 0 0 8px;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--vl-text-3);
}
.rf-regiao {
  margin: 0 0 8px;
  font-weight: 600;
}
.rf-vazio {
  margin: 0;
  font-size: 13px;
  color: var(--vl-text-3);
}
</style>
