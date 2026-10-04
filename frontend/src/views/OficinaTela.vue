<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Button from 'primevue/button'
import ProgressBar from 'primevue/progressbar'
import Select from 'primevue/select'
import FabricacaoModal from '../components/jogo/FabricacaoModal.vue'
import { percentualPf, useOficina, type NovaFabricacao } from '../composables/useOficina'
import { useEstoque } from '../composables/useEstoque'
import { rotuloSubtipo } from '../composables/useItens'
import { ROTULOS_ESTADO, ROTULOS_TIPO, ROTULOS_CONSTRUCAO, rotulo } from '../composables/useMapa'

const route = useRoute()
const id = computed(() => Number(route.params.id))
const o = useOficina()
const est = useEstoque()
const modalAberto = ref(false)
const reatribuindo = ref<number | null>(null)
const novoArtesao = ref<number | null>(null)

const nome = computed(() => rotulo({ ...ROTULOS_TIPO, ...ROTULOS_CONSTRUCAO }, o.oficina.value?.tipo))
const estoque = computed<Record<string, number>>(() =>
  Object.fromEntries(est.recursos.value.map((l) => [l.recurso, l.quantidade])),
)
const opcoesLivres = computed(() =>
  o.artesaosLivres.value.map((a) => ({ label: `${a.nome} (PE ${a.peEfetivo})`, value: a.cidadaoId })),
)

async function carregar() {
  await Promise.all([o.carregar(id.value), est.carregar()])
}
onMounted(carregar)
watch(id, carregar)

async function fabricar(req: NovaFabricacao) {
  if (await o.fabricar(id.value, req)) {
    modalAberto.value = false
    await est.carregar()
  }
}

function iniciarReatribuicao(fabId: number) {
  reatribuindo.value = fabId
  novoArtesao.value = null
}

async function confirmarReatribuicao(fabId: number) {
  if (novoArtesao.value == null) return
  if (await o.reatribuir(id.value, fabId, novoArtesao.value)) reatribuindo.value = null
}

function rotuloEstadoFab(e: string) {
  return e === 'PAUSADA' ? 'Pausada' : 'Em andamento'
}
</script>

<template>
  <section class="oficina" data-testid="OficinaTela">
    <p v-if="o.carregando.value" data-testid="carregando">Carregando...</p>
    <template v-else-if="o.oficina.value">
      <h2>{{ nome }}</h2>
      <p data-testid="resumo">
        {{ o.oficina.value.nivel }} · {{ rotulo(ROTULOS_ESTADO, o.oficina.value.estado) }} ·
        nível máximo de item L{{ o.oficina.value.nivelMaximoItem }}
      </p>

      <h3>Artesãos</h3>
      <p v-if="!o.oficina.value.artesaos.length" data-testid="sem-artesaos">Nenhum artesão alocado.</p>
      <ul v-else class="lista" data-testid="artesaos">
        <li v-for="a in o.oficina.value.artesaos" :key="a.cidadaoId" :data-testid="`artesao-${a.cidadaoId}`">
          <router-link :to="`/jogo/cidadao/${a.cidadaoId}`">{{ a.nome }}</router-link>
          <span>PE {{ a.peEfetivo }} · eficiência {{ a.eficiencia.toFixed(2).replace('.', ',') }} ·
            {{ a.ocupado ? 'ocupado' : 'livre' }}</span>
        </li>
      </ul>

      <h3>Fila de fabricação</h3>
      <p v-if="!o.fila.value.length" data-testid="fila-vazia">Nenhum item em fabricação.</p>
      <table v-else class="fila" data-testid="fila">
        <caption class="sr-only">Fila de fabricação</caption>
        <thead>
          <tr><th>Item</th><th>Nível</th><th>Artesão</th><th>Progresso</th><th>Estado</th><th>Estimativa</th><th></th></tr>
        </thead>
        <tbody>
          <tr v-for="f in o.fila.value" :key="f.id" :data-testid="`fab-${f.id}`">
            <td>
              {{ rotuloSubtipo(f.subtipo) }}
              <em v-if="f.itemId != null" data-testid="aprimoramento">(Aprimoramento)</em>
            </td>
            <td class="num">L{{ f.nivel }}</td>
            <td>{{ f.artesaoNome }}</td>
            <td class="progresso">
              <ProgressBar :value="percentualPf(f)" :show-value="false" />
              <span class="num" data-testid="pf">{{ f.pfAtual }}/{{ f.pfTotal }} PF</span>
            </td>
            <td :data-testid="`estado-${f.id}`">{{ rotuloEstadoFab(f.estado) }}</td>
            <td class="num">{{ f.turnosEstimados != null ? `${f.turnosEstimados} turno(s)` : '-' }}</td>
            <td>
              <template v-if="f.estado === 'PAUSADA'">
                <Button v-if="reatribuindo !== f.id" label="Reatribuir artesão" size="small" severity="secondary"
                  :data-testid="`reatribuir-${f.id}`" @click="iniciarReatribuicao(f.id)" />
                <div v-else class="reatribuir">
                  <Select v-model="novoArtesao" :options="opcoesLivres" option-label="label" option-value="value"
                    placeholder="Artesão livre" :data-testid="`select-reatribuir-${f.id}`" />
                  <Button label="Confirmar" size="small" :data-testid="`confirmar-reatribuir-${f.id}`"
                    :disabled="novoArtesao == null || o.enviando.value" @click="confirmarReatribuicao(f.id)" />
                </div>
              </template>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="acoes">
        <Button label="Nova fabricação" data-testid="nova-fabricacao" :disabled="o.oficina.value.estado !== 'ATIVA'"
          @click="modalAberto = true" />
      </div>
      <FabricacaoModal v-if="modalAberto" :oficina="o.oficina.value" :receitas="o.receitas.value" :estoque="estoque"
        :enviando="o.enviando.value" :erro="o.erro.value" @fabricar="fabricar" @fechar="modalAberto = false" />
    </template>
    <p v-if="o.erro.value && !modalAberto" class="erro" role="alert" data-testid="erro-oficina">{{ o.erro.value }}</p>
  </section>
</template>

<style scoped>
h1, h2, h3, h4 { font-family: var(--vl-font-display); color: var(--vl-text); }
.num { font-family: var(--vl-font-mono); }
.lista { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 0.4rem; }
.lista li { display: flex; gap: 0.75rem; flex-wrap: wrap; }
.fila { width: 100%; border-collapse: collapse; display: block; overflow-x: auto; }
.fila th { color: var(--vl-text-2); border-bottom: 1px solid var(--vl-border); }
.fila th, .fila td { text-align: left; padding: 0.4rem 0.6rem; }
.progresso { min-width: 8rem; }
.reatribuir, .acoes { display: flex; gap: 0.5rem; flex-wrap: wrap; margin-top: 0.5rem; }
.erro { color: var(--vl-error); }
.sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); }
</style>
