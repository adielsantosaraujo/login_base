<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Button from 'primevue/button'
import GradeRegiao from '../components/GradeRegiao.vue'
import SeletorConstrucao from '../components/jogo/SeletorConstrucao.vue'
import UpgradeModal from '../components/jogo/UpgradeModal.vue'
import PainelMarcacao from '../components/jogo/PainelMarcacao.vue'
import PainelPredio from '../components/jogo/PainelPredio.vue'
import { useConstrucoes } from '../composables/useConstrucoes'
import { useEstoque } from '../composables/useEstoque'
import {
  ROTULOS_CONSTRUCAO,
  ROTULOS_ESTADO,
  ROTULOS_TIPO,
  rotulo,
  useRegiaoDetalhes,
  type Construcao,
} from '../composables/useMapa'

const route = useRoute()
const indice = computed(() => Number(route.params.indice))

const detalhe = useRegiaoDetalhes()
const construcoes = useConstrucoes()
const estoque = useEstoque()

const posicao = ref<{ x: number; y: number } | null>(null)
const predio = ref<Construcao | null>(null)
const painel = ref<'upgrade' | 'marcacao' | 'predio' | null>(null)
const mensagem = ref<string | null>(null)

const tipoRegiao = computed(() => detalhe.regiaoSelecionada.value?.regiao.tipo ?? null)
const possuida = computed(() => detalhe.regiaoSelecionada.value?.regiao.possuida ?? false)
const ladrilhos = computed(() => detalhe.regiaoSelecionada.value?.ladrilhos ?? [])

const predios = computed(() => {
  const mapa = new Map<number, Construcao>()
  for (const l of ladrilhos.value) if (l.construcao) mapa.set(l.construcao.id, l.construcao)
  return [...mapa.values()]
})

const estoqueMapa = computed(() =>
  Object.fromEntries(estoque.recursos.value.map((r) => [r.recurso, r.quantidade])),
)

async function recarregar() {
  await Promise.all([detalhe.carregar(indice.value), estoque.carregar()])
}

onMounted(async () => {
  construcoes.carregarCatalogo()
  await recarregar()
})
watch(indice, recarregar)

function aoClicar(p: { x: number; y: number; ladrilho: { construcao: Construcao | null } | null }) {
  mensagem.value = null
  if (p.ladrilho?.construcao) {
    posicao.value = null
    predio.value = p.ladrilho.construcao
    painel.value = null
    return
  }
  predio.value = null
  painel.value = null
  construcoes.erro.value = null
  posicao.value = { x: p.x, y: p.y }
}

async function construir(tipo: string) {
  if (!posicao.value) return
  const criada = await construcoes.criar({
    tipo,
    regiaoIndice: indice.value,
    x: posicao.value.x,
    y: posicao.value.y,
  })
  if (criada) {
    posicao.value = null
    mensagem.value = 'Construção iniciada'
    await recarregar()
  }
}

function fecharPainel() {
  painel.value = null
}

async function atualizado() {
  await recarregar()
}

function porcentagem(c: Construcao): string {
  return c.poTotal ? `${c.poAtual ?? 0}/${c.poTotal}` : ''
}
</script>

<template>
  <section class="regiao-vila">
    <h1>
      Região {{ indice }}
      <small v-if="tipoRegiao">({{ rotulo(ROTULOS_TIPO, tipoRegiao) }})</small>
    </h1>
    <router-link to="/jogo/mapa">Voltar ao mapa</router-link>

    <p v-if="detalhe.erro.value" role="alert" class="erro">{{ detalhe.erro.value }}</p>
    <p v-else-if="detalhe.carregando.value && !detalhe.regiaoSelecionada.value">Carregando...</p>
    <p v-if="mensagem" role="status" class="sucesso" data-testid="mensagem-construcao">{{ mensagem }}</p>

    <p v-if="detalhe.regiaoSelecionada.value && !possuida" data-testid="regiao-nao-possuida">
      Região não possuída.
    </p>

    <div v-else-if="detalhe.regiaoSelecionada.value" class="conteudo">
      <div>
        <GradeRegiao
          :ladrilhos="ladrilhos"
          :destaques="posicao ? [posicao] : []"
          selecionaveis
          @clique-ladrilho="aoClicar"
        />
        <p><small>Clique em um ladrilho vazio para construir ou em um prédio para ver as ações.</small></p>
      </div>

      <aside class="lateral">
        <div v-if="posicao" data-testid="area-seletor">
          <h2>Construir em ({{ posicao.x }}, {{ posicao.y }})</h2>
          <SeletorConstrucao
            :catalogo="construcoes.catalogo.value"
            :tipo-regiao="tipoRegiao"
            :estoque="estoque.recursos.value.length ? estoqueMapa : undefined"
            :criando="construcoes.criando.value"
            :erro="construcoes.erro.value"
            @construir="construir"
            @cancelar="posicao = null"
          />
        </div>

        <div v-if="predio" class="menu-predio" data-testid="menu-predio">
          <h2>{{ rotulo(ROTULOS_CONSTRUCAO, predio.tipo) }} {{ predio.nivel }}</h2>
          <div class="botoes">
            <Button label="Detalhes" size="small" data-testid="acao-predio" @click="painel = 'predio'" />
            <Button label="Upgrade" size="small" data-testid="acao-upgrade" @click="painel = 'upgrade'" />
            <Button label="Marcação" size="small" data-testid="acao-marcacao" @click="painel = 'marcacao'" />
            <Button label="Fechar" severity="secondary" size="small" @click="predio = null; painel = null" />
          </div>
          <PainelPredio v-if="painel === 'predio'" :construcao-id="predio.id" @fechar="fecharPainel" @atualizado="atualizado" />
          <UpgradeModal v-if="painel === 'upgrade'" :construcao-id="predio.id" @fechar="fecharPainel" @atualizado="atualizado" />
          <PainelMarcacao v-if="painel === 'marcacao'" :construcao-id="predio.id" @fechar="fecharPainel" @atualizado="atualizado" />
        </div>

        <h2>Construções</h2>
        <p v-if="!predios.length" data-testid="sem-construcoes">Nenhuma construção nesta região.</p>
        <ul v-else class="lista-predios" data-testid="lista-construcoes">
          <li v-for="c in predios" :key="c.id" :data-testid="`predio-${c.id}`">
            {{ rotulo(ROTULOS_CONSTRUCAO, c.tipo) }} {{ c.nivel }}
            - {{ rotulo(ROTULOS_ESTADO, c.estado) }}
            <span v-if="c.estado && c.estado !== 'ATIVA'" data-testid="po">(PO {{ porcentagem(c) }})</span>
          </li>
        </ul>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.conteudo { display: flex; flex-wrap: wrap; gap: 1.5rem; align-items: flex-start; margin-top: 1rem; }
.lateral { flex: 1; min-width: 280px; }
.botoes { display: flex; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 0.5rem; }
.erro { color: var(--vl-error); }
.sucesso { color: var(--vl-tipo-floresta); }
h1, h2 { font-family: var(--vl-font-display); }
.lista-predios { padding-left: 1.2rem; }
</style>
