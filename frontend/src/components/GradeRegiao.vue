<script setup lang="ts">
import { computed } from 'vue'
import {
  ROTULOS_CONSTRUCAO,
  ROTULOS_ESTADO,
  ROTULOS_JAZIDA,
  rotulo,
  type Ladrilho,
} from '../composables/useMapa'

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
  const partes = [`(${x}, ${y})`]
  if (l?.construcao) {
    const c = l.construcao
    partes.push(`${rotulo(ROTULOS_CONSTRUCAO, c.tipo)} ${c.nivel.replace('N', 'nível ')}`)
    if (c.estado) partes.push(rotulo(ROTULOS_ESTADO, c.estado))
    if (c.estado === 'EM_OBRA' && c.poTotal) partes.push(`PO ${c.poAtual ?? 0}/${c.poTotal}`)
  }
  if (l?.jazida) partes.push(`Jazida: ${rotulo(ROTULOS_JAZIDA, l.jazida)}`)
  if (partes.length === 1) partes.push('Vazio')
  return partes.join(' - ')
}

function classe(l: Ladrilho | null): string[] {
  if (l?.construcao) return ['ladrilho', 'ladrilho-construcao']
  if (l?.jazida) return ['ladrilho', `ladrilho-jazida-${l.jazida.toLowerCase().replace(/_/g, '-')}`]
  return ['ladrilho']
}

function simbolo(l: Ladrilho | null): string {
  if (l?.construcao) return rotulo(ROTULOS_CONSTRUCAO, l.construcao.tipo).charAt(0)
  if (l?.jazida) return rotulo(ROTULOS_JAZIDA, l.jazida).charAt(0)
  return ''
}
</script>

<template>
  <div class="grade-regiao" data-testid="grade-regiao">
    <div
      v-for="c in celulas"
      :key="`${c.x}-${c.y}`"
      :class="[...classe(c.ladrilho), { destaque: destacado(c.x, c.y), selecionavel: selecionaveis && !c.ladrilho?.construcao }]"
      :title="descricao(c.x, c.y, c.ladrilho)"
      :aria-label="descricao(c.x, c.y, c.ladrilho)"
      data-testid="ladrilho"
      @click="emit('clique-ladrilho', { x: c.x, y: c.y, ladrilho: c.ladrilho })"
      :data-x="c.x"
      :data-y="c.y"
    >
      {{ simbolo(c.ladrilho) }}
    </div>
  </div>
</template>

<style scoped>
.grade-regiao {
  display: grid;
  grid-template-columns: repeat(10, minmax(0, 1fr));
  gap: 2px;
  max-width: 480px;
}
.ladrilho {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  background: var(--p-surface-100, #f1f5f9);
  border: 1px solid var(--p-surface-200, #e2e8f0);
  color: #fff;
}
.ladrilho.selecionavel { cursor: pointer; }
.ladrilho.destaque { outline: 3px solid #f59e0b; outline-offset: -3px; }
.ladrilho-construcao { cursor: pointer; background: #b45309; }
.ladrilho-jazida-floresta { background: #15803d; }
.ladrilho-jazida-rocha { background: #64748b; }
.ladrilho-jazida-barreiro { background: #a16207; }
.ladrilho-jazida-veio-de-ferro { background: #475569; }
.ladrilho-jazida-veio-de-carvao { background: #1f2937; }
.ladrilho-jazida-salina { background: #0891b2; }
.ladrilho-jazida-enxofre { background: #ca8a04; }
.ladrilho-jazida-campo { background: #65a30d; }
</style>
