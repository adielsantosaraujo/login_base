<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Paginator from 'primevue/paginator'
import Select from 'primevue/select'
import Tab from 'primevue/tab'
import TabList from 'primevue/tablist'
import TabPanel from 'primevue/tabpanel'
import TabPanels from 'primevue/tabpanels'
import Tabs from 'primevue/tabs'
import ModalEngaste from '../components/jogo/ModalEngaste.vue'
import AprimoramentoModal from '../components/jogo/AprimoramentoModal.vue'
import DetalhesItemModal from '../components/jogo/DetalhesItemModal.vue'
import { podeAprimorar, useInventario } from '../composables/useInventario'
import { useEstoque } from '../composables/useEstoque'
import { usePedras, type ItemCompativelDTO, type PedraDTO } from '../composables/usePedras'
import {
  corQualidade, ROTULOS_CATEGORIA, rotuloBonus, rotuloCategoria, rotuloQualidade, type ItemDTO,
} from '../composables/useItens'

const inv = useInventario()
const est = useEstoque()
const detalheItem = ref<ItemDTO | null>(null)
const aprimorando = ref<ItemDTO | null>(null)
const mensagem = ref<string | null>(null)

const pedrasApi = usePedras()
const pedras = ref<PedraDTO[]>([])
const erroPedras = ref<string | null>(null)
const mensagemPedras = ref<string | null>(null)
const engastando = ref<PedraDTO | null>(null)
const compativeis = ref<ItemCompativelDTO[]>([])
const erroEngaste = ref<string | null>(null)
const enviandoEngaste = ref(false)
const ouro = computed(() => estoque.value.OURO ?? 0)

const opcoesCategoria = [
  { label: 'Todos', value: null },
  ...Object.keys(ROTULOS_CATEGORIA).map((c) => ({ label: rotuloCategoria(c), value: c })),
]
const estoque = computed<Record<string, number>>(() =>
  Object.fromEntries(est.recursos.value.map((l) => [l.recurso, l.quantidade])),
)

onMounted(() => {
  void inv.carregar()
  void carregarPedras()
  void est.carregar()
})

async function carregarPedras() {
  erroPedras.value = null
  try {
    pedras.value = await pedrasApi.listarPedras()
  } catch (e) {
    erroPedras.value = e instanceof Error ? e.message : 'Erro ao carregar as pedras'
  }
}

function resumoPedra(p: PedraDTO) {
  return p.bonus.map((b) => `${rotuloBonus(b.codigo)} +${b.valor}`).join(', ') || '-'
}

async function abrirEngaste(p: PedraDTO) {
  mensagemPedras.value = null
  erroEngaste.value = null
  engastando.value = p
  compativeis.value = []
  try {
    const [c] = await Promise.all([pedrasApi.itensCompativeis(), est.carregar()])
    compativeis.value = c
  } catch (e) {
    erroEngaste.value = e instanceof Error ? e.message : 'Erro ao carregar os itens compatíveis'
  }
}

async function engastar(req: { pedraId: number; itemId: number }) {
  enviandoEngaste.value = true
  erroEngaste.value = null
  try {
    await pedrasApi.engastar(req.pedraId, req.itemId)
    engastando.value = null
    mensagemPedras.value = 'Pedra engastada com sucesso.'
    await Promise.all([carregarPedras(), inv.carregar(), est.carregar()])
  } catch (e) {
    erroEngaste.value = e instanceof Error ? e.message : 'Erro ao engastar a pedra'
  } finally {
    enviandoEngaste.value = false
  }
}

async function pedraRemovida() {
  if (detalheItem.value) {
    const d = await inv.carregarDetalhe(detalheItem.value.id)
    detalheItem.value = d?.item ?? detalheItem.value
  }
  await inv.carregar()
}

function resumoBonus(i: ItemDTO) {
  const partes = i.bonus.map((b) => `${rotuloBonus(b.codigo)} +${b.valor}`)
  return partes.length ? partes.join(', ') : '-'
}

async function abrirDetalhes(i: ItemDTO) {
  const d = await inv.carregarDetalhe(i.id)
  detalheItem.value = d?.item ?? null
}

async function abrirAprimoramento(i: ItemDTO) {
  mensagem.value = null
  aprimorando.value = i
  await Promise.all([inv.carregarOficinasElegiveis(i), est.carregar()])
}

async function aprimorar(req: { oficinaId: number; artesaoId: number }) {
  const i = aprimorando.value
  if (!i) return
  const f = await inv.aprimorar(i.id, req.oficinaId, req.artesaoId)
  if (f) {
    aprimorando.value = null
    mensagem.value = `${i.nome} L${i.nivel} enviado para aprimoramento (L${f.nivel}${f.turnosEstimados != null ? `, ~${f.turnosEstimados} turnos` : ''}).`
  }
}
</script>

<template>
  <section class="inventario" data-testid="JogoInventario">
    <h1>Inventário</h1>
    <p data-testid="total">{{ inv.total.value }} {{ inv.total.value === 1 ? 'item' : 'itens' }}</p>
    <label class="filtro">Categoria
      <Select :model-value="inv.categoria.value" :options="opcoesCategoria" option-label="label" option-value="value"
        data-testid="filtro-categoria" @update:model-value="inv.filtrar($event)" />
    </label>

    <Tabs value="itens">
      <TabList>
        <Tab value="itens">Itens</Tab>
        <Tab value="pedras" data-testid="aba-pedras">Pedras</Tab>
      </TabList>
      <TabPanels>
        <TabPanel value="itens">
        <p v-if="inv.erro.value" role="alert" class="erro" data-testid="erro">{{ inv.erro.value }}</p>
        <p v-if="mensagem" role="status" data-testid="sucesso">{{ mensagem }}</p>
        <p v-if="inv.carregando.value" data-testid="carregando">Carregando...</p>
        <p v-else-if="!inv.itens.value.length" data-testid="vazio">Nenhum item no inventário.</p>
        <table v-else class="tabela" data-testid="itens">
          <caption class="sr-only">Itens do inventário</caption>
          <thead>
            <tr><th>Item</th><th>Categoria</th><th>Nível</th><th>Qualidade</th><th>Bônus</th><th>Ações</th></tr>
          </thead>
          <tbody>
            <tr v-for="i in inv.itens.value" :key="i.id" :data-testid="`item-${i.id}`">
              <td>{{ i.nome }}</td>
              <td>{{ rotuloCategoria(i.categoria) }}</td>
              <td class="num">L{{ i.nivel }}</td>
              <td><span :style="{ color: corQualidade(i.qualidade), fontWeight: 600 }">{{ rotuloQualidade(i.qualidade) }}</span></td>
              <td>
                {{ resumoBonus(i) }}
                <em v-if="i.emAprimoramento" data-testid="em-aprimoramento"> · Em aprimoramento</em>
              </td>
              <td class="acoes">
                <Button label="Detalhes" size="small" severity="secondary" :data-testid="`detalhes-${i.id}`" @click="abrirDetalhes(i)" />
                <Button label="Aprimorar" size="small" :data-testid="`aprimorar-${i.id}`" :disabled="!podeAprimorar(i)"
                  :title="i.nivel >= 10 ? 'Nível máximo L10' : i.emAprimoramento ? 'Já em aprimoramento' : ''"
                  @click="abrirAprimoramento(i)" />
              </td>
            </tr>
          </tbody>
        </table>
        <Paginator v-if="inv.total.value > inv.pageSize.value" :rows="inv.pageSize.value" :total-records="inv.total.value"
          :first="inv.page.value * inv.pageSize.value" :rows-per-page-options="[10, 20, 50]" data-testid="paginador"
          @page="inv.irParaPagina($event.page, $event.rows)" />

        </TabPanel>
        <TabPanel value="pedras">
          <p v-if="erroPedras" role="alert" class="erro" data-testid="erro-pedras">{{ erroPedras }}</p>
          <p v-if="mensagemPedras" role="status" data-testid="sucesso-pedras">{{ mensagemPedras }}</p>
          <p v-if="!pedras.length" data-testid="sem-pedras">Nenhuma pedra no inventário.</p>
          <table v-else class="tabela" data-testid="pedras">
            <caption class="sr-only">Pedras do inventário</caption>
            <thead>
              <tr><th>Pedra</th><th>Bônus</th><th>Custo</th><th>Ações</th></tr>
            </thead>
            <tbody>
              <tr v-for="p in pedras" :key="p.id" :data-testid="`pedra-${p.id}`">
                <td><span :style="{ color: corQualidade(p.qualidade), fontWeight: 600 }">{{ rotuloQualidade(p.qualidade) }}</span></td>
                <td>{{ resumoPedra(p) }}</td>
                <td class="num">{{ p.custoEngaste }} Ouro</td>
                <td class="acoes">
                  <Button label="Engastar" size="small" :data-testid="`engastar-${p.id}`" @click="abrirEngaste(p)" />
                </td>
              </tr>
            </tbody>
          </table>
        </TabPanel>
      </TabPanels>
    </Tabs>

    <DetalhesItemModal v-if="detalheItem" :item="detalheItem" :pedras="inv.detalhe.value?.pedras"
      @pedra-removida="pedraRemovida" @fechar="detalheItem = null" />
    <ModalEngaste v-if="engastando" :pedra="engastando" :itens-compativeis="compativeis" :ouro-disponivel="ouro"
      :enviando="enviandoEngaste" :erro="erroEngaste" @engastar="engastar" @fechar="engastando = null" />
    <AprimoramentoModal v-if="aprimorando" :item="aprimorando" :oficinas="inv.oficinas.value"
      :oficina="inv.oficina.value" :receitas="inv.receitas.value" :estoque="estoque" :enviando="inv.enviando.value"
      :erro="inv.erroAcao.value" @selecionar-oficina="inv.carregarOficina($event)" @aprimorar="aprimorar"
      @fechar="aprimorando = null" />
  </section>
</template>

<style scoped>
.filtro { display: flex; flex-direction: column; gap: 0.25rem; max-width: 14rem; margin: 0.5rem 0; }
h1, h2, h3, h4 { font-family: var(--vl-font-display); color: var(--vl-text); }
.num { font-family: var(--vl-font-mono); }
.tabela { width: 100%; border-collapse: collapse; }
.tabela th { color: var(--vl-text-2); border-bottom: 1px solid var(--vl-border); }
.tabela th, .tabela td { text-align: left; padding: 0.35rem 0.5rem; }
.acoes { display: flex; gap: 0.5rem; }
.erro { color: var(--vl-error); }
.sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); }
</style>
