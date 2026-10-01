<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import GradeRegiao from '../components/GradeRegiao.vue'
import DialogoAnexacao from '../components/DialogoAnexacao.vue'
import { ehAnexavel } from '../composables/useAnexacao'
import { ROTULOS_TIPO, rotulo, useMapaVila, useRegiaoDetalhes } from '../composables/useMapa'

const { regioes, carregando, erro, carregar } = useMapaVila()
const detalhe = useRegiaoDetalhes()

onMounted(carregar)

function classe(tipo: string | null, possuida: boolean): string {
  return possuida && tipo ? `tipo-${tipo.toLowerCase()}` : 'tipo-vazio'
}

const dialogoVisivel = ref(false)
const indiceAnexar = ref<number | null>(null)
const mensagem = ref<string | null>(null)

function anexavel(indice: number): boolean {
  return ehAnexavel(indice, regioes.value)
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
          :class="[classe(r.tipo, r.possuida), { anexavel: anexavel(r.indice), selecionada: detalhe.regiaoSelecionada.value?.regiao.indice === r.indice }]"
          :data-testid="`regiao-${r.indice}`"
          :aria-label="`Região ${r.indice}${r.possuida ? ' - ' + rotulo(ROTULOS_TIPO, r.tipo) : ' - não possuída'}`"
          @click="abrir(r.indice)"
        >
          <span class="numero">{{ r.indice }}</span>
          <span class="tipo">{{ r.possuida ? rotulo(ROTULOS_TIPO, r.tipo) : 'Vazio' }}</span>
          <span v-if="r.masmorraAtiva" class="masmorra" data-testid="masmorra">
            <i class="pi pi-bolt" aria-hidden="true"></i> Nv {{ r.nivelMasmorra }}
          </span>
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
          <Button label="Voltar" severity="secondary" size="small" data-testid="voltar" @click="detalhe.limpar()" />
        </div>
        <p v-if="detalhe.erro.value" role="alert" class="erro">{{ detalhe.erro.value }}</p>
        <p v-else-if="detalhe.carregando.value">Carregando...</p>
        <template v-else-if="detalhe.regiaoSelecionada.value">
          <p v-if="!detalhe.regiaoSelecionada.value.regiao.possuida" data-testid="regiao-nao-possuida">
            Região não possuída, clicar para anexar
          </p>
          <GradeRegiao v-else :ladrilhos="detalhe.regiaoSelecionada.value.ladrilhos" />
        </template>
      </aside>
    </div>

    <DialogoAnexacao v-model:visivel="dialogoVisivel" :indice="indiceAnexar" @anexada="aoAnexar" />
  </section>
</template>

<style scoped>
.mapa-conteudo { display: flex; flex-wrap: wrap; gap: 1.5rem; align-items: flex-start; }
.grade-mapa {
  display: grid;
  grid-template-columns: repeat(4, 6rem);
  gap: 0.5rem;
}
.celula {
  height: 6rem;
  border: 2px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.15rem;
}
.celula.selecionada { border-color: #000; }
.celula.anexavel { border-color: #f59e0b; border-style: dashed; }
.sucesso { color: #15803d; }
.numero { font-size: 1.25rem; font-weight: 700; }
.tipo { font-size: 0.75rem; }
.masmorra { font-size: 0.7rem; }
.tipo-rural { background: #16a34a; }
.tipo-urbana { background: #2563eb; }
.tipo-coleta { background: #92400e; }
.tipo-vazio { background: #9ca3af; }
.painel-regiao { flex: 1; min-width: 280px; }
.painel-topo { display: flex; justify-content: space-between; align-items: center; gap: 1rem; }
.erro { color: #b91c1c; }
</style>
