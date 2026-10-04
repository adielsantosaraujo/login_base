<script setup lang="ts">
import { computed } from 'vue'
import {
  ROTULOS_CONSTRUCAO,
  ROTULOS_ESTADO,
  rotulo,
  type Ladrilho,
} from '../composables/useMapa'
import { COR_TERRENO, ROTULO_TERRENO, SIGLA_TERRENO, enderecoLadrilho } from '../domain/terrenos'

const props = defineProps<{
  ladrilhos: Ladrilho[]
  /** Ladrilhos realçados (ex.: posição escolhida para construir). */
  destaques?: { x: number; y: number }[]
  /** Quando true, ladrilhos sem construção são marcados como clicáveis. */
  selecionaveis?: boolean
}>()

const emit = defineEmits<{
  (e: 'clique-ladrilho', payload: { x: number; y: number; ladrilho: Ladrilho | null }): void
}>()

function destacado(x: number, y: number): boolean {
  return !!props.destaques?.some((d) => d.x === x && d.y === y)
}

const TAMANHO = 10

const celulas = computed(() => {
  const porPosicao = new Map<string, Ladrilho>()
  for (const l of props.ladrilhos) porPosicao.set(`${l.x},${l.y}`, l)
  const lista: { x: number; y: number; ladrilho: Ladrilho | null }[] = []
  for (let y = 0; y < TAMANHO; y++) {
    for (let x = 0; x < TAMANHO; x++) {
      lista.push({ x, y, ladrilho: porPosicao.get(`${x},${y}`) ?? null })
    }
  }
  return lista
})

function descricao(x: number, y: number, l: Ladrilho | null): string {
  const partes = [enderecoLadrilho(x, y)]
  if (l?.construcao) {
    const c = l.construcao
    partes.push(`${rotulo(ROTULOS_CONSTRUCAO, c.tipo)} ${c.nivel.replace('N', 'nível ')}`)
    if (c.estado) partes.push(rotulo(ROTULOS_ESTADO, c.estado))
    if (c.estado === 'EM_OBRA' && c.poTotal) partes.push(`PO ${c.poAtual ?? 0}/${c.poTotal}`)
  }
  if (l) {
    partes.push(`${ROTULO_TERRENO[l.terreno]} (${SIGLA_TERRENO[l.terreno]})`)
    partes.push(`base ${l.bonusBase}`, `adjacente +${l.bonusAdjacente}`, `total ${l.bonusTotal}`)
  }
  if (partes.length === 1) partes.push('Vazio')
  return partes.join(' - ')
}

function classe(l: Ladrilho | null): string[] {
  return l?.construcao ? ['ladrilho', 'ladrilho-construcao'] : ['ladrilho']
}

function estilo(l: Ladrilho | null): Record<string, string> | undefined {
  return l ? { background: COR_TERRENO[l.terreno] } : undefined
}

function inicialPredio(l: Ladrilho | null): string {
  return l?.construcao ? rotulo(ROTULOS_CONSTRUCAO, l.construcao.tipo).charAt(0) : ''
}
</script>

<template>
  <div class="grade-regiao" data-testid="grade-regiao">
    <div
      v-for="c in celulas"
      :key="`${c.x}-${c.y}`"
      :class="[...classe(c.ladrilho), { destaque: destacado(c.x, c.y), selecionavel: selecionaveis && !c.ladrilho?.construcao }]"
      :style="estilo(c.ladrilho)"
      :title="descricao(c.x, c.y, c.ladrilho)"
      :aria-label="descricao(c.x, c.y, c.ladrilho)"
      data-testid="ladrilho"
      @click="emit('clique-ladrilho', { x: c.x, y: c.y, ladrilho: c.ladrilho })"
      :data-x="c.x"
      :data-y="c.y"
    >
      <span class="lad-endereco">{{ enderecoLadrilho(c.x, c.y) }}</span>
      <span v-if="c.ladrilho" class="lad-sigla">{{ SIGLA_TERRENO[c.ladrilho.terreno] }}</span>
      <span v-if="c.ladrilho?.construcao" class="lad-predio">{{ inicialPredio(c.ladrilho) }}</span>
    </div>
  </div>
</template>

<style scoped>
.grade-regiao {
  display: grid;
  grid-template-columns: repeat(10, minmax(0, 1fr));
  gap: 2px;
  max-width: 560px;
}
.ladrilho {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  font-size: 0.75rem;
  font-weight: 600;
  font-family: var(--vl-font-mono);
  background: var(--vl-surface-2);
  border: 1px solid var(--vl-border);
  color: var(--vl-accent-ink);
}
.ladrilho.selecionavel { cursor: pointer; }
.ladrilho.destaque { outline: 3px solid var(--vl-accent); outline-offset: -3px; }
.ladrilho-construcao { cursor: pointer; box-shadow: inset 0 0 0 2px var(--vl-accent); }
.lad-endereco { font-size: 0.55rem; font-weight: 400; line-height: 1; }
.lad-sigla { line-height: 1.2; }
.lad-predio { position: absolute; top: 1px; right: 3px; font-size: 0.6rem; line-height: 1; }
</style>
