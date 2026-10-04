<script setup lang="ts">
import { computed } from 'vue'
import PontoStepper from '../vilarejo/PontoStepper.vue'
import { ORDEM_PLANO, ROTULOS_PROFISSAO, type Plano, type Profissao } from '../../domain/populacao'

const props = defineProps<{
  plano: Plano
  minimos: Plano
  atual: Record<Profissao, number>
  total: number
  totalEsperado: number
}>()
const emit = defineEmits<{ alterar: [profissao: Profissao, quantidade: number] }>()

const completo = computed(() => props.total >= props.totalEsperado)

function classeAtual(p: Profissao): string {
  const a = props.atual[p] || 0
  if (a < (props.minimos[p] || 0)) return 'erro'
  if (a !== (props.plano[p] || 0)) return 'aviso'
  return ''
}
</script>

<template>
  <section class="pl-painel" aria-labelledby="pl-titulo">
    <div class="pl-topo">
      <h2 id="pl-titulo" class="pl-titulo">Plano inicial</h2>
      <span class="pl-total" :class="{ aviso: props.total !== totalEsperado }" data-testid="plano-total">
        {{ total }} / {{ totalEsperado }}
      </span>
    </div>
    <div class="pl-linha cab" aria-hidden="true">
      <span>PROFISSÃO</span><span>PLANO</span><span>ATUAL</span>
    </div>
    <div v-for="p in ORDEM_PLANO" :key="p" class="pl-linha" :data-testid="`plano-${p}`">
      <span class="pl-nome">
        {{ ROTULOS_PROFISSAO[p] }}
        <span v-if="minimos[p]" class="pl-chip">mín {{ minimos[p] }}</span>
      </span>
      <PontoStepper
        :model-value="plano[p] || 0"
        :min="minimos[p] || 0"
        :pode-aumentar="!completo"
        :rotulo="`plano de ${ROTULOS_PROFISSAO[p]}`"
        @update:model-value="(v: number) => emit('alterar', p, v)"
      />
      <span class="pl-atual" :class="classeAtual(p)" :data-testid="`atual-${p}`">{{ atual[p] || 0 }}</span>
    </div>
    <p class="pl-nota">
      “Atual” conta quem tem a profissão como principal (maior PE). Ajuste o plano e clique em Redistribuir.
    </p>
  </section>
</template>

<style scoped>
.pl-painel {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.pl-topo {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.pl-titulo {
  margin: 0;
  font-family: var(--vl-font-display);
  font-size: 17px;
  color: var(--vl-text);
}
.pl-total {
  font-family: var(--vl-font-mono);
  color: var(--vl-text);
}
.pl-total.aviso {
  color: var(--vl-warn);
}
.pl-linha {
  display: grid;
  grid-template-columns: 1fr auto 40px;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--vl-text);
}
.pl-linha.cab {
  font-size: 10px;
  color: var(--vl-text-3);
}
.pl-chip {
  margin-left: 4px;
  padding: 1px 5px;
  border-radius: var(--vl-radius-chip);
  background: var(--vl-surface-4);
  font-size: 10px;
  color: var(--vl-text-2);
}
.pl-atual {
  font-family: var(--vl-font-mono);
  text-align: right;
}
.pl-atual.erro {
  color: var(--vl-error);
}
.pl-atual.aviso {
  color: var(--vl-warn);
}
.pl-nota {
  margin: 4px 0 0;
  font-size: 11px;
  color: var(--vl-text-3);
}
</style>
