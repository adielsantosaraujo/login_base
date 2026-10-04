<script setup lang="ts">
import { computed } from 'vue'
import ListaTerrenos from './ListaTerrenos.vue'
import { COR_TIPO, ROTULO_TIPO, type RegiaoPrevia } from '../../domain/regioes'
import { composicaoTexto, ordenarTerrenos } from '../../domain/terrenos'

const props = defineProps<{ regiao: RegiaoPrevia; ordem: number; disponivel: boolean }>()
const emit = defineEmits<{ alternar: [indice: number]; foco: [indice: number]; desfoco: [] }>()

const selecionado = computed(() => props.ordem > 0)
const numero = computed(() => String(props.regiao.indice).padStart(2, '0'))
const itens = computed(() =>
  ordenarTerrenos(props.regiao.terrenos).map((t) => ({ terreno: t.terreno, valor: t.percentual })),
)
const indisponivel = computed(() => !selecionado.value && !props.disponivel)
const rotuloAria = computed(
  () => `Região ${numero.value} · ${ROTULO_TIPO[props.regiao.tipo]} · ${composicaoTexto(props.regiao.terrenos)}`,
)

function aoClicar() {
  if (!indisponivel.value) emit('alternar', props.regiao.indice)
}
</script>

<template>
  <button
    type="button"
    class="rt-tile"
    :class="{ selecionado, indisponivel }"
    :aria-pressed="selecionado"
    :aria-disabled="indisponivel ? 'true' : undefined"
    :aria-label="rotuloAria"
    :data-testid="`regiao-${regiao.indice}`"
    @click="aoClicar"
    @mouseenter="emit('foco', regiao.indice)"
    @focus="emit('foco', regiao.indice)"
    @mouseleave="emit('desfoco')"
    @blur="emit('desfoco')"
  >
    <span class="rt-topo">
      <span class="rt-numero">{{ numero }}</span>
      <span class="rt-chip" :style="{ background: COR_TIPO[regiao.tipo] }">{{ ROTULO_TIPO[regiao.tipo] }}</span>
    </span>
    <span v-if="selecionado" class="rt-selo" data-testid="selo-ordem" aria-hidden="true">{{ ordem }}</span>
    <ListaTerrenos :itens="itens" :maximo="100" sufixo="%" />
  </button>
</template>

<style scoped>
.rt-tile {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 12px;
  min-height: 150px;
  padding: 12px;
  text-align: left;
  font-family: var(--vl-font-sans);
  color: var(--vl-text);
  background: var(--vl-surface-3);
  border: 1px solid var(--vl-border);
  border-radius: var(--vl-radius-tile);
  cursor: pointer;
  transition: transform 0.15s;
}
.rt-tile:hover,
.rt-tile:focus-visible {
  transform: translateY(-2px);
}
.rt-tile:focus-visible {
  outline: 2px solid var(--vl-accent);
  outline-offset: 2px;
}
.rt-tile.selecionado {
  border: 2px solid var(--vl-accent);
  background: var(--vl-accent-bg);
}
.rt-tile.indisponivel {
  opacity: 0.4;
  cursor: not-allowed;
}
.rt-topo {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.rt-numero {
  font-family: var(--vl-font-mono);
  font-size: 13px;
}
.rt-chip {
  padding: 2px 8px;
  border-radius: var(--vl-radius-chip);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--vl-accent-ink);
}
.rt-selo {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  border-radius: var(--vl-radius-pill);
  background: var(--vl-accent);
  color: var(--vl-accent-ink);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}
</style>
