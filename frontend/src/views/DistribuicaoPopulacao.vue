<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import AvisoToast from '../components/vilarejo/AvisoToast.vue'
import CidadaoCard from '../components/populacao/CidadaoCard.vue'
import ConfirmacaoPainel from '../components/populacao/ConfirmacaoPainel.vue'
import FamiliaLiderSelector, { type OpcaoLider } from '../components/populacao/FamiliaLiderSelector.vue'
import FamiliaTabs from '../components/populacao/FamiliaTabs.vue'
import PlanoInicialPainel from '../components/populacao/PlanoInicialPainel.vue'
import { liderDaFamilia } from '../domain/populacao'
import { TOTAL_POPULACAO, usePopulacao } from '../composables/usePopulacao'

const {
  familias, familiaLiderId, plano, minimos, limites, carregando, enviando, erro,
  contagemPrincipais, pendentesPorFamilia, totalPlano, checklist,
  carregar, editar, trocarPrincipal, alterarPlano, redistribuir, zerar, escolherLider, confirmar,
} = usePopulacao()

const familiaAtivaId = ref<number | null>(null)
onMounted(carregar)
watch(familias, (fs) => {
  if (familiaAtivaId.value === null && fs.length) familiaAtivaId.value = fs[0]!.familiaId
}, { immediate: true })

const familiaAtiva = computed(() => familias.value.find((f) => f.familiaId === familiaAtivaId.value))
const planoOk = computed(() => totalPlano.value === TOTAL_POPULACAO)
const pendentesTotal = computed(() =>
  pendentesPorFamilia.value.reduce((a, f) => a + f.carPendentes + f.profPendentes, 0),
)
const outrosOk = computed(() =>
  checklist.value.filter((i) => i.obrigatorio && !i.chave.startsWith('MINIMO_') && i.chave !== 'FAMILIA_LIDER')
    .every((i) => i.ok),
)
const opcoesLider = computed<OpcaoLider[]>(() =>
  familias.value.map((f) => {
    const l = liderDaFamilia(f)
    return { familiaId: f.familiaId, sobrenome: f.sobrenome, lider: l?.nome ?? '—', car: l?.caracteristicas.CAR ?? 0 }
  }),
)
const liderEscolhido = computed(
  () => familiaLiderId.value !== null && familias.value.some((f) => f.familiaId === familiaLiderId.value),
)
</script>

<template>
  <section class="dp-tela">
    <div class="dp-topo">
      <div>
        <p class="dp-etapas"><span>01 Regiões ✓</span> — <span class="atual" aria-current="step">02 População</span></p>
        <h1 class="dp-h1">Distribuir a população</h1>
        <p class="dp-intro">
          Os 16 cidadãos já vêm distribuídos segundo o plano inicial. Ajuste o que quiser: cada pessoa tem até
          20 pontos de característica e 10 de profissão. Pontos não usados ficam pendentes.
        </p>
      </div>
      <div class="dp-acoes">
        <button
          type="button"
          class="dp-pilula"
          data-testid="redistribuir"
          :disabled="!planoOk"
          :title="planoOk ? undefined : 'O plano precisa somar 16'"
          @click="redistribuir"
        >
          ↻ Redistribuir pelo plano
        </button>
        <button type="button" class="dp-pilula" data-testid="zerar" @click="zerar">Zerar tudo</button>
      </div>
    </div>

    <p v-if="carregando" role="status">Carregando...</p>
    <AvisoToast v-if="erro" tipo="erro" :mensagem="erro" data-testid="erro" />

    <div v-if="familiaAtiva" class="dp-corpo">
      <div class="dp-conteudo">
        <FamiliaTabs
          v-model:ativa="familiaAtivaId"
          :familias="pendentesPorFamilia"
          :lider-id="familiaLiderId"
        />
        <div class="dp-cards">
          <CidadaoCard
            v-for="c in familiaAtiva.cidadaos"
            :key="c.cidadaoId"
            :cidadao="c"
            :limite-caracteristicas="limites.caracteristicasTotal"
            :limite-profissoes="limites.profissoesTotal"
            @editar="editar"
            @trocar-principal="trocarPrincipal"
          />
        </div>
      </div>
      <aside class="dp-painel">
        <PlanoInicialPainel
          :plano="plano"
          :minimos="minimos"
          :atual="contagemPrincipais"
          :total="totalPlano"
          :total-esperado="TOTAL_POPULACAO"
          @alterar="alterarPlano"
        />
        <FamiliaLiderSelector :opcoes="opcoesLider" :model-value="familiaLiderId" @update:model-value="escolherLider" />
        <ConfirmacaoPainel
          :construtores="contagemPrincipais.CONSTRUTOR"
          :min-construtores="minimos.CONSTRUTOR ?? 2"
          :carregadores="contagemPrincipais.CARREGADOR"
          :min-carregadores="minimos.CARREGADOR ?? 2"
          :pendentes="pendentesTotal"
          :lider-escolhido="liderEscolhido"
          :outros-ok="outrosOk"
          :enviando="enviando"
          @confirmar="confirmar"
        />
      </aside>
    </div>
  </section>
</template>

<style scoped>
.dp-tela {
  max-width: 1320px;
  margin: 0 auto;
  padding: 36px 28px 48px;
  display: flex;
  flex-direction: column;
  gap: 28px;
  color: var(--vl-text);
}
.dp-topo {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 16px;
}
.dp-etapas {
  margin: 0 0 8px;
  font-family: var(--vl-font-mono);
  font-size: 12px;
  color: var(--vl-text-2);
}
.dp-etapas .atual {
  color: var(--vl-accent);
}
.dp-h1 {
  margin: 0 0 8px;
  font-family: var(--vl-font-display);
  font-size: 32px;
}
.dp-intro {
  margin: 0;
  max-width: 620px;
  color: var(--vl-text-2);
}
.dp-acoes {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  flex-wrap: wrap;
}
.dp-pilula {
  padding: 8px 16px;
  border: 1px solid var(--vl-border);
  border-radius: var(--vl-radius-pill);
  background: var(--vl-surface-2);
  color: var(--vl-text);
  cursor: pointer;
}
.dp-pilula:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.dp-corpo {
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  align-items: flex-start;
}
.dp-conteudo {
  flex: 1 1 640px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.dp-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}
.dp-painel {
  flex: 1 1 320px;
  max-width: 380px;
  position: sticky;
  top: 16px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 20px;
  border-radius: var(--vl-radius-panel);
  background: var(--vl-surface-1);
}
</style>
