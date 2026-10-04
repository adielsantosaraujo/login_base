<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import GradeRegiao from '../components/GradeRegiao.vue'
import DialogoAnexacao from '../components/DialogoAnexacao.vue'
import MasmorraIndicador from '../components/jogo/MasmorraIndicador.vue'
import { ehAnexavel } from '../composables/useAnexacao'
import { ROTULOS_TIPO, rotulo, useMapaVila, useRegiaoDetalhes } from '../composables/useMapa'
import { COR_TIPO, type TipoRegiao } from '../domain/regioes'
import { COR_TERRENO, ROTULO_TERRENO, SIGLA_TERRENO, composicaoTexto, ordenarTerrenos } from '../domain/terrenos'

const { regioes, carregando, erro, carregar } = useMapaVila()
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
          :aria-label="`Região ${r.indice}${r.possuida ? ' - ' + rotulo(ROTULOS_TIPO, r.tipo) : ' - não possuída' + (r.tipo ? ' - ' + rotulo(ROTULOS_TIPO, r.tipo) : '')}${r.terrenos?.length ? ' - ' + composicaoTexto(r.terrenos) : ''}`"
          @click="abrir(r.indice)"
        >
          <span class="numero">{{ r.indice }}</span>
          <span class="tipo" data-testid="tipo-regiao">{{ r.tipo ? rotulo(ROTULOS_TIPO, r.tipo) : 'Vazio' }}</span>
          <ul class="terrenos-celula" :data-testid="`terrenos-regiao-${r.indice}`">
            <li v-for="t in ordenarTerrenos(r.terrenos ?? [])" :key="t.terreno">
              <span class="ponto" :style="{ background: COR_TERRENO[t.terreno] }" aria-hidden="true"></span>
              {{ SIGLA_TERRENO[t.terreno] }} {{ t.percentual }}%
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
          <ul v-if="detalhe.regiaoSelecionada.value.regiao.terrenos?.length" class="lista-terrenos" data-testid="terrenos-detalhe">
            <li v-for="t in ordenarTerrenos(detalhe.regiaoSelecionada.value.regiao.terrenos)" :key="t.terreno" class="chip-terreno">
              <span class="ponto" :style="{ background: COR_TERRENO[t.terreno] }" aria-hidden="true"></span>
              {{ ROTULO_TERRENO[t.terreno] }} {{ t.percentual }}%
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
.lista-terrenos { display: flex; flex-wrap: wrap; gap: 0.4rem; list-style: none; margin: 0.5rem 0; padding: 0; }
.chip-terreno {
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
.terrenos-celula { list-style: none; margin: 0; padding: 0; font-size: 0.65rem; text-align: left; }
.terrenos-celula li { display: flex; align-items: center; gap: 0.25rem; }
.painel-regiao { flex: 1; min-width: 280px; }
.painel-topo { display: flex; justify-content: space-between; align-items: center; gap: 1rem; }
.erro { color: var(--vl-error); }
.vazio { color: var(--vl-text-3); }
</style>
