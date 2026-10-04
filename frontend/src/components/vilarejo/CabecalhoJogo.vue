<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    abaAtiva?: string
    abasInativas?: boolean
    turno?: number
    segundosRestantes?: number
    mostrarRelatorio?: boolean
  }>(),
  { abaAtiva: '', abasInativas: false, turno: 0, segundosRestantes: 0, mostrarRelatorio: true },
)

const emit = defineEmits<{ relatorio: [] }>()

const abas = [
  { id: 'mapa', rotulo: 'Mapa', to: '/jogo/mapa' },
  { id: 'estoque', rotulo: 'Estoque', to: '/jogo/estoque' },
  { id: 'familias', rotulo: 'Famílias', to: '/jogo/familias' },
  { id: 'mercado', rotulo: 'Mercado', to: '/jogo/mercado' },
  { id: 'inventario', rotulo: 'Inventário', to: '/jogo/inventario' },
  { id: 'batalhas', rotulo: 'Batalhas', to: '/jogo/batalhas' },
]

const contagem = computed(() => {
  const total = Math.max(0, Math.floor(props.segundosRestantes))
  const mm = String(Math.floor(total / 60)).padStart(2, '0')
  const ss = String(total % 60).padStart(2, '0')
  return `${mm}:${ss}`
})

function aoClicar(e: Event) {
  if (props.abasInativas) e.preventDefault()
}
</script>

<template>
  <header class="vl-cabecalho">
    <span class="vl-logo">Vilarejo</span>
    <nav class="vl-abas" aria-label="Navegação do jogo">
      <RouterLink
        v-for="aba in abas"
        :key="aba.id"
        :to="aba.to"
        class="vl-aba"
        :class="{ ativa: aba.id === abaAtiva, inativa: abasInativas }"
        :aria-disabled="abasInativas ? 'true' : undefined"
        :aria-current="aba.id === abaAtiva ? 'page' : undefined"
        :tabindex="abasInativas ? -1 : undefined"
        :data-testid="`aba-${aba.id}`"
        @click="aoClicar"
      >
        {{ aba.rotulo }}
      </RouterLink>
    </nav>
    <div class="vl-direita">
      <span class="vl-pilula" data-testid="pilula-turno">TURNO <b class="vl-mono">{{ turno }}</b></span>
      <span class="vl-pilula" data-testid="pilula-proximo">
        PRÓXIMO TURNO <b class="vl-mono vl-destaque">{{ contagem }}</b>
      </span>
      <button v-if="mostrarRelatorio" type="button" class="vl-botao-relatorio" @click="emit('relatorio')">
        Relatório
      </button>
    </div>
  </header>
</template>

<style scoped>
.vl-cabecalho {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 12px 24px;
  background: var(--vl-surface-1);
  border-bottom: 1px solid var(--vl-border);
  color: var(--vl-text);
  flex-wrap: wrap;
}
.vl-logo {
  font-family: var(--vl-font-display);
  font-weight: 700;
  font-size: 20px;
  color: var(--vl-accent);
}
.vl-abas {
  display: flex;
  gap: 4px;
  flex: 1;
  flex-wrap: wrap;
}
.vl-aba {
  font-family: var(--vl-font-display);
  font-weight: 700;
  font-size: 17px;
  padding: 6px 14px;
  border-radius: var(--vl-radius-tab);
  color: var(--vl-text-2);
  text-decoration: none;
}
.vl-aba.ativa {
  background: var(--vl-surface-4);
  color: var(--vl-text);
}
.vl-aba.inativa {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
}
.vl-direita {
  display: flex;
  align-items: center;
  gap: 8px;
}
.vl-pilula {
  background: var(--vl-surface-4);
  border-radius: var(--vl-radius-pill);
  padding: 4px 12px;
  font-size: 11px;
  letter-spacing: 0.08em;
  color: var(--vl-text-2);
}
.vl-mono {
  font-family: var(--vl-font-mono);
  font-weight: 500;
  font-size: 13px;
  letter-spacing: 0;
}
.vl-destaque {
  color: var(--vl-accent);
}
.vl-botao-relatorio {
  background: var(--vl-surface-4);
  color: var(--vl-text);
  border: 1px solid var(--vl-border);
  border-radius: var(--vl-radius-pill);
  padding: 6px 16px;
  font-family: var(--vl-font-sans);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
</style>
