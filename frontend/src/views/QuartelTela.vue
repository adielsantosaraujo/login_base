<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Button from 'primevue/button'
import FormarTropaDialog from '../components/jogo/FormarTropaDialog.vue'
import ExpedicaoDialog from '../components/jogo/ExpedicaoDialog.vue'
import TropasLista from '../components/jogo/TropasLista.vue'
import { useQuartel, type DestinoDTO, type NovaTropa } from '../composables/useQuartel'
import { ROTULOS_ESTADO, rotulo } from '../composables/useMapa'

const route = useRoute()
const id = computed(() => Number(route.params.id))
const q = useQuartel()
const dialogo = ref<'formar' | 'adicionar' | null>(null)
const tropaAlvo = ref<number | null>(null)
const desfazendo = ref<number | null>(null)
const expedicaoTropa = ref<number | null>(null)
const destinos = ref<DestinoDTO[]>([])
const carregandoDestinos = ref(false)

const vagasLivres = computed(() => (q.quartel.value ? q.quartel.value.capacidade - q.quartel.value.membrosAtuais : 0))
const podeFormar = computed(
  () => !!q.quartel.value && q.quartel.value.estado === 'ATIVA' && q.quartel.value.tropas.length < q.quartel.value.maxTropas,
)
const tropaExpedicao = computed(() => q.quartel.value?.tropas.find((t) => t.id === expedicaoTropa.value) ?? null)
const nomeDesfazendo = computed(() => q.quartel.value?.tropas.find((t) => t.id === desfazendo.value)?.nome ?? '')

onMounted(() => q.carregar(id.value))
watch(id, (v) => q.carregar(v))

function fechar() {
  dialogo.value = null
  tropaAlvo.value = null
}

function abrirAdicionar(tropaId: number) {
  q.erro.value = null
  tropaAlvo.value = tropaId
  dialogo.value = 'adicionar'
}

async function confirmar(req: NovaTropa) {
  if (dialogo.value === 'formar') {
    if (await q.formarTropa(id.value, req)) fechar()
    return
  }
  const tropaId = tropaAlvo.value
  if (tropaId == null) return
  for (const m of req.membros) {
    if (!(await q.adicionarMembro(id.value, tropaId, m))) return
  }
  fechar()
}

function remover(tropaId: number, cidadaoId: number) {
  q.removerMembro(id.value, tropaId, cidadaoId)
}

async function abrirExpedicao(tropaId: number) {
  q.erro.value = null
  destinos.value = []
  expedicaoTropa.value = tropaId
  carregandoDestinos.value = true
  destinos.value = (await q.buscarDestinos(tropaId)) ?? []
  carregandoDestinos.value = false
}

function fecharExpedicao() {
  expedicaoTropa.value = null
  q.erro.value = null
}

async function enviarExpedicao(masmorraId: number) {
  const tropaId = expedicaoTropa.value
  if (tropaId == null) return
  if (await q.enviarExpedicao(id.value, tropaId, masmorraId)) fecharExpedicao()
}

async function confirmarDesfazer() {
  if (desfazendo.value == null) return
  const tropaId = desfazendo.value
  desfazendo.value = null
  await q.desfazerTropa(id.value, tropaId)
}
</script>

<template>
  <section class="quartel" data-testid="QuartelTela">
    <p v-if="q.carregando.value" data-testid="carregando">Carregando...</p>
    <template v-else-if="q.quartel.value">
      <h2>Quartel</h2>
      <p data-testid="resumo">
        {{ q.quartel.value.nivel }} · {{ rotulo(ROTULOS_ESTADO, q.quartel.value.estado) }}
      </p>
      <ul class="cabecalho">
        <li data-testid="instrutores">Instrutores: {{ q.quartel.value.instrutores }}/{{ q.quartel.value.vagasInstrutor }}</li>
        <li data-testid="membros">Membros: {{ q.quartel.value.membrosAtuais }}/{{ q.quartel.value.capacidade }}</li>
        <li data-testid="tropas-max">Tropas: {{ q.quartel.value.tropas.length }}/{{ q.quartel.value.maxTropas }}</li>
      </ul>

      <h3>Tropas</h3>
      <TropasLista :tropas="q.quartel.value.tropas" :enviando="q.enviando.value"
        @adicionar="abrirAdicionar" @remover="remover" @desfazer="(t) => (desfazendo = t)"
        @expedicao="abrirExpedicao" />

      <div v-if="desfazendo != null" class="confirmacao" role="alertdialog" data-testid="confirmar-desfazer">
        <p>Desfazer a tropa "{{ nomeDesfazendo }}"? Os membros voltam a ficar disponíveis.</p>
        <Button label="Confirmar" severity="danger" size="small" data-testid="confirmar-desfazer-sim"
          :disabled="q.enviando.value" @click="confirmarDesfazer" />
        <Button label="Cancelar" severity="secondary" size="small" data-testid="confirmar-desfazer-nao"
          @click="desfazendo = null" />
      </div>

      <div class="acoes">
        <Button label="Formar tropa" data-testid="formar-tropa" :disabled="!podeFormar"
          @click="q.erro.value = null; dialogo = 'formar'" />
      </div>
      <FormarTropaDialog v-if="dialogo" :modo="dialogo" :guerreiros="q.disponiveis.value" :vagas-livres="vagasLivres"
        :enviando="q.enviando.value" :erro="q.erro.value" @confirmar="confirmar" @fechar="fechar" />
      <ExpedicaoDialog v-if="tropaExpedicao" :tropa="tropaExpedicao" :destinos="destinos"
        :carregando="carregandoDestinos" :enviando="q.enviando.value" :erro="q.erro.value"
        @confirmar="enviarExpedicao" @fechar="fecharExpedicao" />
    </template>
    <p v-if="q.erro.value && !dialogo && !tropaExpedicao" class="erro" role="alert" data-testid="erro-quartel">{{ q.erro.value }}</p>
  </section>
</template>

<style scoped>
.cabecalho { list-style: none; padding: 0; display: flex; gap: 1.5rem; flex-wrap: wrap; }
.acoes, .confirmacao { display: flex; gap: 0.5rem; flex-wrap: wrap; margin-top: 0.75rem; align-items: center; }
.erro { color: var(--vl-error); }
.dica { font-size: 0.9em; opacity: 0.8; }
h2, h3 { font-family: var(--vl-font-display); }
</style>
