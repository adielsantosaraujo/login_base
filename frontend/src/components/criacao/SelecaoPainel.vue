<script setup lang="ts">
import { computed } from 'vue'
import BotaoCta from '../vilarejo/BotaoCta.vue'
import ChecklistItem from '../vilarejo/ChecklistItem.vue'
import ListaTerrenos from './ListaTerrenos.vue'
import RegiaoFoco from './RegiaoFoco.vue'
import {
  ROTULO_TIPO,
  TAMANHO_SELECAO,
  COR_TIPO,
  temUrbana,
  type RegiaoPrevia,
} from '../../domain/regioes'
import { TERRENOS, type TipoTerreno } from '../../domain/terrenos'

const props = defineProps<{
  selecionadas: number[]
  regioes: RegiaoPrevia[]
  totais: Record<TipoTerreno, number>
  conectado: boolean
  valida: boolean
  enviando: boolean
  emFoco: number | null
}>()
const emit = defineEmits<{ limpar: []; criar: [] }>()

const slots = computed(() =>
  Array.from({ length: TAMANHO_SELECAO }, (_, k) => {
    const indice = props.selecionadas[k]
    const regiao = props.regioes.find((r) => r.indice === indice)
    return regiao ?? null
  }),
)
const itens = computed(() =>
  TERRENOS.map((t) => ({ terreno: t, valor: props.totais[t] ?? 0 }))
    .filter((i) => i.valor > 0)
    .sort((a, b) => b.valor - a.valor),
)
const maximo = computed(() => Math.max(100, ...itens.value.map((i) => i.valor)))
const regiaoFoco = computed(() => props.regioes.find((r) => r.indice === props.emFoco) ?? null)
const completa = computed(() => props.selecionadas.length === TAMANHO_SELECAO)
const urbana = computed(() => temUrbana(props.selecionadas, props.regioes))
</script>

<template>
  <aside class="sp-painel" aria-label="Sua seleção">
    <header class="sp-cabecalho">
      <h2 class="sp-titulo">Sua seleção</h2>
      <button type="button" class="sp-limpar" data-testid="limpar" @click="emit('limpar')">Limpar</button>
    </header>

    <ol class="sp-slots">
      <li v-for="(r, k) in slots" :key="k" class="sp-slot" :class="{ vazio: !r }" data-testid="slot">
        <template v-if="r">
          <span class="sp-slot-num" :style="{ color: COR_TIPO[r.tipo] }">{{ String(r.indice).padStart(2, '0') }}</span>
          <span class="sp-slot-tipo" :style="{ color: COR_TIPO[r.tipo] }">{{ ROTULO_TIPO[r.tipo] }}</span>
        </template>
        <template v-else>
          <span class="sp-slot-num">—</span>
          <span class="sp-slot-tipo">VAZIO</span>
        </template>
      </li>
    </ol>

    <ul class="sp-checklist" data-testid="checklist">
      <ChecklistItem :estado="completa ? 'ok' : 'pendente'" :texto="`3 regiões ${selecionadas.length}/3`" />
      <ChecklistItem :estado="completa && conectado ? 'ok' : 'pendente'" texto="Vizinhas entre si" />
      <ChecklistItem :estado="urbana ? 'ok' : 'pendente'" texto="Ao menos 1 Urbana" />
    </ul>

    <section aria-label="Ladrilhos por terreno">
      <h3 class="sp-subtitulo">Ladrilhos por terreno <small>soma das regiões escolhidas</small></h3>
      <ListaTerrenos v-if="itens.length > 0" :itens="itens" :maximo="maximo" espessura="normal" />
      <p v-else class="sp-vazio" data-testid="ladrilhos-vazio">Selecione regiões para ver os ladrilhos</p>
    </section>

    <BotaoCta
      :ativo="valida"
      :carregando="enviando"
      :rotulo="valida ? 'Criar vila' : 'Selecione 3 regiões válidas'"
      data-testid="criar"
      @click="emit('criar')"
    />

    <RegiaoFoco :regiao="regiaoFoco" />
  </aside>
</template>

<style scoped>
.sp-painel {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 20px;
  border-radius: var(--vl-radius-panel);
  background: var(--vl-surface-2);
  border: 1px solid var(--vl-border);
}
.sp-cabecalho {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.sp-titulo {
  margin: 0;
  font-family: var(--vl-font-display);
  font-size: 18px;
}
.sp-limpar {
  background: none;
  border: none;
  color: var(--vl-text-2);
  font-size: 13px;
  cursor: pointer;
}
.sp-limpar:hover,
.sp-limpar:focus-visible {
  color: var(--vl-accent);
}
.sp-slots {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.sp-slot {
  height: 64px;
  border-radius: var(--vl-radius-slot);
  background: var(--vl-surface-3);
  border: 1px solid var(--vl-border);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}
.sp-slot.vazio {
  border-style: dashed;
  color: var(--vl-text-3);
}
.sp-slot-num {
  font-family: var(--vl-font-mono);
  font-size: 16px;
}
.sp-slot-tipo {
  font-size: 10px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}
.sp-checklist {
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.sp-subtitulo {
  margin: 0 0 10px;
  font-size: 13px;
}
.sp-vazio {
  margin: 0;
  font-size: 13px;
  color: var(--vl-text-3);
}
.sp-subtitulo small {
  color: var(--vl-text-3);
  font-weight: 400;
}
</style>
