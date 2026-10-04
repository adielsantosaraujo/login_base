<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import GradeRegiao from '../components/GradeRegiao.vue'
import DialogoAnexacao from '../components/DialogoAnexacao.vue'
import MasmorraIndicador from '../components/jogo/MasmorraIndicador.vue'
import { ehAnexavel } from '../composables/useAnexacao'
import { ROTULOS_TIPO, rotulo, useMapaVila, useRegiaoDetalhes } from '../composables/useMapa'
import { BONUS, COR_BONUS, COR_TIPO, ROTULO_BONUS, bonusOrdenados, type BonusRegiao, type TipoRegiao } from '../domain/regioes'

const { mapa, regioes, carregando, erro, carregar } = useMapaVila()
const detalhe = useRegiaoDetalhes()
const router = useRouter()

function abrirRegiao(indice: number) {
  router.push(`/jogo/regiao/${indice}`)
}

onMounted(carregar)

function classe(tipo: string | null, possuida: boolean): string[] {
  const c = [tipo ? `tipo-${tipo.toLowerCase()}` : 'tipo-vazio']
  if (!possuida) c.push('nao-possuida')
  return c
}

function corTipo(tipo: TipoRegiao | null): string | undefined {
  return tipo ? COR_TIPO[tipo] : undefined
}

const bonusVila = computed(() => {
  const total = mapa.value?.vila.bonusRegiao ?? {}
  return BONUS.filter((b) => (total[b] ?? 0) > 0).map((b) => ({ bonus: b as BonusRegiao, valor: total[b] as number }))
})

const dialogoVisivel = ref(false)
const indiceAnexar = ref<number | null>(null)
const mensagem = ref<string | null>(null)

function anexavel(indice: number): boolean {
  return ehAnexavel(indice, regioes.value)
}

function masmorraDa(indice: number) {
  const r = regioes.value.find((x) => x.indice === indice)
  return r && !r.possuida && r.masmorraAtiva ? r : null
}

function abrir(indice: number) {
  detalhe.carregar(indice)
  mensagem.value = null
  if (anexavel(indice)) {
    indiceAnexar.value = indice
    dialogoVisivel.value = true
  }
}

async function aoAnexar(indice: number) {
  mensagem.value = `Região ${indice} anexada com sucesso.`
  await carregar()
  await detalhe.carregar(indice)
}
</script>

<template>
  <section class="mapa">
    <h1>Mapa da vila</h1>

    <p v-if="erro" role="alert" class="erro">{{ erro }}</p>
    <p v-else-if="carregando">Carregando...</p>

    <p v-if="mensagem" role="status" class="sucesso" data-testid="mensagem-anexacao">{{ mensagem }}</p>

    <section v-if="mapa" class="bonus-vila" data-testid="bonus-vila" aria-label="Bônus total da vila">
      <h2>Bônus da vila</h2>
      <ul v-if="bonusVila.length > 0" class="lista-bonus">
        <li v-for="b in bonusVila" :key="b.bonus" class="chip-bonus" :data-testid="`bonus-vila-${b.bonus}`">
          <span class="ponto" :style="{ background: COR_BONUS[b.bonus] }" aria-hidden="true"></span>
          {{ ROTULO_BONUS[b.bonus] }} +{{ b.valor }}%
        </li>
      </ul>
      <p v-else class="vazio">Sem bônus de região.</p>
    </section>

    <div class="mapa-conteudo">
      <div class="grade-mapa" data-testid="grade-mapa">
        <button
          v-for="r in regioes"
          :key="r.indice"
          type="button"
          class="celula"
          :class="[...classe(r.tipo, r.possuida), { 'com-masmorra': r.masmorraAtiva, anexavel: anexavel(r.indice), selecionada: detalhe.regiaoSelecionada.value?.regiao.indice === r.indice }]"
          :style="{ '--cor-tipo': corTipo(r.tipo) }"
          :data-testid="`regiao-${r.indice}`"
          :aria-label="`Região ${r.indice}${r.possuida ? ' - ' + rotulo(ROTULOS_TIPO, r.tipo) : ' - não possuída' + (r.tipo ? ' - ' + rotulo(ROTULOS_TIPO, r.tipo) : '')}`"
          @click="abrir(r.indice)"
        >
          <span class="numero">{{ r.indice }}</span>
          <span class="tipo" data-testid="tipo-regiao">{{ r.tipo ? rotulo(ROTULOS_TIPO, r.tipo) : 'Vazio' }}</span>
          <ul class="bonus-celula" :data-testid="`bonus-regiao-${r.indice}`">
            <li v-for="b in bonusOrdenados(r.bonus ?? [])" :key="b.bonus">
              <span class="ponto" :style="{ background: COR_BONUS[b.bonus] }" aria-hidden="true"></span>
              {{ ROTULO_BONUS[b.bonus] }} {{ b.valor }}%
            </li>
          </ul>
          <MasmorraIndicador v-if="r.masmorraAtiva && r.nivelMasmorra != null" :nivel="r.nivelMasmorra" />
        </button>
      </div>

      <aside
        v-if="detalhe.regiaoSelecionada.value || detalhe.erro.value || detalhe.carregando.value"
        class="painel-regiao"
        data-testid="painel-regiao"
      >
        <div class="painel-topo">
          <h2 v-if="detalhe.regiaoSelecionada.value">
            Região {{ detalhe.regiaoSelecionada.value.regiao.indice }}
            <small v-if="detalhe.regiaoSelecionada.value.regiao.possuida">
              ({{ rotulo(ROTULOS_TIPO, detalhe.regiaoSelecionada.value.regiao.tipo) }})
            </small>
          </h2>
          <Button
            v-if="detalhe.regiaoSelecionada.value?.regiao.possuida"
            label="Abrir região"
            size="small"
            data-testid="abrir-regiao"
            @click="abrirRegiao(detalhe.regiaoSelecionada.value.regiao.indice)"
          />
          <Button label="Voltar" severity="secondary" size="small" data-testid="voltar" @click="detalhe.limpar()" />
        </div>
        <p v-if="detalhe.erro.value" role="alert" class="erro">{{ detalhe.erro.value }}</p>
        <p v-else-if="detalhe.carregando.value">Carregando...</p>
        <template v-else-if="detalhe.regiaoSelecionada.value">
          <ul v-if="detalhe.regiaoSelecionada.value.regiao.bonus?.length" class="lista-bonus" data-testid="bonus-detalhe">
            <li v-for="b in bonusOrdenados(detalhe.regiaoSelecionada.value.regiao.bonus)" :key="b.bonus" class="chip-bonus">
              <span class="ponto" :style="{ background: COR_BONUS[b.bonus] }" aria-hidden="true"></span>
              {{ ROTULO_BONUS[b.bonus] }} {{ b.valor }}%
            </li>
          </ul>
          <p
            v-if="!detalhe.regiaoSelecionada.value.regiao.possuida && masmorraDa(detalhe.regiaoSelecionada.value.regiao.indice)"
            data-testid="regiao-masmorra"
          >
            Masmorra nível {{ masmorraDa(detalhe.regiaoSelecionada.value.regiao.indice)?.nivelMasmorra }} — não pode ser anexada
          </p>
          <p v-else-if="!detalhe.regiaoSelecionada.value.regiao.possuida" data-testid="regiao-nao-possuida">
            Região não possuída, clicar para anexar
          </p>
          <GradeRegiao v-else :ladrilhos="detalhe.regiaoSelecionada.value.ladrilhos" />
        </template>
      </aside>
    </div>

    <DialogoAnexacao v-model:visivel="dialogoVisivel" :indice="indiceAnexar" :regiao="regioes.find((x) => x.indice === indiceAnexar) ?? null" @anexada="aoAnexar" />
  </section>
</template>

<style scoped>
.bonus-vila { margin-bottom: 1rem; }
.lista-bonus { display: flex; flex-wrap: wrap; gap: 0.4rem; list-style: none; margin: 0.5rem 0; padding: 0; }
.chip-bonus {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.15rem 0.6rem;
  border-radius: var(--vl-radius-pill);
  background: var(--vl-surface-3);
  border: 1px solid var(--vl-border);
  color: var(--vl-text);
  font-size: 0.8rem;
}
.ponto { display: inline-block; width: 0.5rem; height: 0.5rem; border-radius: var(--vl-radius-pill); flex: none; }
.mapa-conteudo { display: flex; flex-wrap: wrap; gap: 1.5rem; align-items: flex-start; }
.grade-mapa {
  display: grid;
  grid-template-columns: repeat(4, 9rem);
  gap: 0.5rem;
}
.celula {
  min-height: 8rem;
  border: 2px solid transparent;
  border-radius: var(--vl-radius-tile);
  cursor: pointer;
  background: var(--cor-tipo, var(--vl-surface-3));
  color: var(--vl-accent-ink);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.15rem;
  padding: 0.35rem;
}
.celula.nao-possuida {
  background: var(--vl-surface-2);
  color: var(--vl-text-2);
  border-color: var(--cor-tipo, var(--vl-border));
}
.celula.selecionada { border-color: var(--vl-text); }
.celula.anexavel { border-color: var(--vl-warn); border-style: dashed; }
.celula.com-masmorra { border-color: var(--vl-error); }
.sucesso { color: var(--vl-accent); }
.numero { font-size: 1.25rem; font-weight: 700; font-family: var(--vl-font-display); }
.tipo { font-size: 0.75rem; }
.bonus-celula { list-style: none; margin: 0; padding: 0; font-size: 0.65rem; text-align: left; }
.bonus-celula li { display: flex; align-items: center; gap: 0.25rem; }
.painel-regiao { flex: 1; min-width: 280px; }
.painel-topo { display: flex; justify-content: space-between; align-items: center; gap: 1rem; }
.erro { color: var(--vl-error); }
.vazio { color: var(--vl-text-3); }
</style>
