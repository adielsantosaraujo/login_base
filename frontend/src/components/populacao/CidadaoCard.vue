<script setup lang="ts">
import { computed } from 'vue'
import BarraValor from '../vilarejo/BarraValor.vue'
import PontoStepper from '../vilarejo/PontoStepper.vue'
import {
  CARACTERISTICAS,
  PROFISSOES,
  ROTULOS_PROFISSAO,
  bonusR3,
  explicacaoBonusR3,
  principal,
  soma,
  type Profissao,
} from '../../domain/populacao'
import type { CidadaoPop } from '../../composables/usePopulacao'

const props = defineProps<{
  cidadao: CidadaoPop
  limiteCaracteristicas: number
  limiteProfissoes: number
}>()
const emit = defineEmits<{
  editar: [cidadaoId: number, chave: string, valor: number]
  'trocar-principal': [cidadaoId: number, profissao: Profissao]
}>()

const ROTULO_PAPEL = { PAI: 'Pai', MAE: 'Mãe', FILHO: 'Filho', FILHA: 'Filha' } as const
const NOMES_CARACTERISTICA: Record<string, string> = {
  VIT: 'Vitalidade', FOR: 'Força', VEL: 'Velocidade', INT: 'Inteligência', CAR: 'Carisma',
}

const totalCar = computed(() => soma(props.cidadao.caracteristicas))
const totalProf = computed(() => soma(props.cidadao.profissoes))
const principalAtual = computed(() => principal(props.cidadao))

function aoTrocar(e: Event) {
  const v = (e.target as HTMLSelectElement).value as Profissao
  if (v) emit('trocar-principal', props.cidadao.cidadaoId, v)
}
</script>

<template>
  <article class="cc-card" :data-testid="`cidadao-${cidadao.cidadaoId}`" :aria-label="cidadao.nome">
    <header class="cc-topo">
      <div>
        <h3 class="cc-nome">{{ cidadao.nome }}</h3>
        <span class="cc-sub">{{ cidadao.idadeAnos }} anos · {{ ROTULO_PAPEL[cidadao.papel] }}</span>
      </div>
      <select
        class="cc-principal"
        :aria-label="`Profissão principal de ${cidadao.nome}`"
        :value="principalAtual ?? ''"
        data-testid="principal"
        @change="aoTrocar"
      >
        <option v-if="!principalAtual" value="" disabled>Sem profissão</option>
        <option v-for="p in PROFISSOES" :key="p" :value="p">{{ ROTULOS_PROFISSAO[p] }}</option>
      </select>
    </header>

    <section>
      <div class="cc-titulo">
        <span>Características</span>
        <span class="cc-contador" :class="{ cheio: totalCar >= limiteCaracteristicas }" data-testid="contador-car">
          {{ totalCar }} / {{ limiteCaracteristicas }}
        </span>
      </div>
      <div v-for="k in CARACTERISTICAS" :key="k" class="cc-linha">
        <abbr class="cc-sigla" :title="NOMES_CARACTERISTICA[k]">{{ k }}</abbr>
        <BarraValor :valor="cidadao.caracteristicas[k]" :maximo="20" :rotulo="NOMES_CARACTERISTICA[k]" />
        <PontoStepper
          :model-value="cidadao.caracteristicas[k]"
          :pode-aumentar="totalCar < limiteCaracteristicas"
          :rotulo="`${NOMES_CARACTERISTICA[k]} de ${cidadao.nome}`"
          :data-testid="`car-${k}`"
          @update:model-value="(v: number) => emit('editar', cidadao.cidadaoId, k, v)"
        />
      </div>
    </section>

    <hr class="cc-div" />

    <section>
      <div class="cc-titulo">
        <span>Profissões</span>
        <span class="cc-contador" :class="{ cheio: totalProf >= limiteProfissoes }" data-testid="contador-prof">
          {{ totalProf }} / {{ limiteProfissoes }}
        </span>
      </div>
      <div class="cc-profs">
        <div v-for="p in PROFISSOES" :key="p" class="cc-prof" :class="{ zerada: cidadao.profissoes[p] === 0 }">
          <span class="cc-prof-nome">
            {{ ROTULOS_PROFISSAO[p] }}
            <span
              class="cc-bonus"
              :class="{ ativo: bonusR3(cidadao.caracteristicas, p) > 0 }"
              :title="explicacaoBonusR3(cidadao.caracteristicas, p)"
              :data-testid="`bonus-${p}`"
            >+{{ bonusR3(cidadao.caracteristicas, p) }}</span>
          </span>
          <PontoStepper
            :model-value="cidadao.profissoes[p]"
            :pode-aumentar="totalProf < limiteProfissoes"
            :rotulo="`${ROTULOS_PROFISSAO[p]} de ${cidadao.nome}`"
            :data-testid="`prof-${p}`"
            @update:model-value="(v: number) => emit('editar', cidadao.cidadaoId, p, v)"
          />
        </div>
      </div>
    </section>
  </article>
</template>

<style scoped>
.cc-card {
  padding: 16px;
  border-radius: var(--vl-radius-card);
  background: var(--vl-surface-2);
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.cc-topo {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: flex-start;
}
.cc-nome {
  margin: 0;
  font-family: var(--vl-font-display);
  font-size: 18px;
  color: var(--vl-text);
}
.cc-sub {
  font-size: 12px;
  color: var(--vl-text-2);
}
.cc-principal {
  padding: 4px 8px;
  border-radius: var(--vl-radius-chip);
  border: 1px solid color-mix(in oklch, var(--vl-accent) 35%, transparent);
  background: var(--vl-accent-bg);
  color: var(--vl-accent);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
}
.cc-titulo {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--vl-text-2);
}
.cc-contador {
  font-family: var(--vl-font-mono);
}
.cc-contador.cheio {
  color: var(--vl-accent);
  font-weight: 600;
}
.cc-linha {
  display: grid;
  grid-template-columns: 34px 1fr 84px;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.cc-sigla {
  font-family: var(--vl-font-mono);
  font-size: 12px;
  color: var(--vl-text-2);
  text-decoration: none;
}
.cc-div {
  border: none;
  border-top: 1px solid var(--vl-border);
  width: 100%;
  margin: 0;
}
.cc-profs {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px 12px;
}
.cc-prof {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--vl-text);
}
.cc-prof.zerada .cc-prof-nome {
  color: var(--vl-text-3);
}
.cc-bonus {
  margin-left: 4px;
  font-family: var(--vl-font-mono);
  font-size: 11px;
  color: var(--vl-text-3);
}
.cc-bonus.ativo {
  color: var(--vl-accent);
}
.cc-prof :deep(.vl-passo) {
  width: 20px;
  height: 20px;
}
.cc-prof :deep(.vl-stepper) {
  gap: 4px;
}
</style>
