<template>
  <div class="quartel-view">
    <h1>Quartel (nível {{ nivelQuartel }})</h1>

    <Card class="formulario">
      <template #title>Treinar unidades</template>
      <template #content>
        <div class="grid">
          <div class="campo">
            <label for="quartel-tipo">Tipo</label>
            <Select
              id="quartel-tipo"
              v-model="tipo"
              :options="tiposDisponiveis"
              optionLabel="nome"
              optionValue="id"
            />
          </div>
          <div class="campo">
            <label for="quartel-arma-nivel">Nível da arma ({{ armaExigida ?? '—' }})</label>
            <Select
              id="quartel-arma-nivel"
              v-model="armaNivel"
              :options="armaNivelOpcoes"
              optionLabel="label"
              optionValue="value"
              placeholder="Selecione o nível"
            />
          </div>
          <div class="campo">
            <label for="quartel-armadura">Armadura</label>
            <Select
              id="quartel-armadura"
              v-model="armaduraSelecao"
              :options="armaduraOpcoesAgrupadas"
              optionLabel="label"
              optionValue="value"
              optionGroupLabel="label"
              optionGroupChildren="items"
              placeholder="Selecione a armadura"
            />
          </div>
          <div class="campo">
            <label for="quartel-quantidade">Quantidade (máx. sugerido: {{ dicaMaximo }})</label>
            <div class="campo-quantidade">
              <InputNumber id="quartel-quantidade" v-model="quantidade" :min="1" :max="15" showButtons />
              <Button label="Máx." severity="secondary" :disabled="dicaMaximo < 1" @click="preencherMaximo" />
            </div>
          </div>
        </div>

        <Button label="Treinar" :disabled="!podeTreinar" @click="treinar" />

        <div v-if="ordemTreino" class="ordem-em-andamento">
          <ProgressBar :value="percentualOrdem" />
          <p>Treinando {{ ordemTreino.quantidade }} unidades — tempo restante {{ tempoRestanteFormatado }}</p>
        </div>
      </template>
    </Card>

    <div class="capacidade">
      <h2>Capacidade do exército: {{ unidadesAtivas }}/{{ capacidadeTotal }}</h2>
    </div>

    <h2>Unidades</h2>
    <DataTable :value="vila?.unidades ?? []" dataKey="id" paginator :rows="10">
      <Column header="Nome">
        <template #body="{ data }">
          <a href="#" class="nome-link" @click.prevent="abrirDetalhe(data.id)">{{ data.nomeExibicao }}</a>
        </template>
      </Column>
      <Column field="tipo" header="Tipo" />
      <Column field="status" header="Status" />
      <Column field="hp" header="HP" />
      <Column field="ataque" header="Ataque" />
      <Column field="defesa" header="Defesa" />
      <Column field="alcance" header="Alcance" />
      <Column field="movimento" header="Movimento" />
      <Column header="Arma">
        <template #body="{ data }">{{ nomeItem(data.equipamento.ARMA) }}</template>
      </Column>
      <Column header="Armadura">
        <template #body="{ data }">{{ nomeItem(data.equipamento.ARMADURA) }}</template>
      </Column>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import Card from 'primevue/card'
import Select from 'primevue/select'
import InputNumber from 'primevue/inputnumber'
import Button from 'primevue/button'
import ProgressBar from 'primevue/progressbar'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { useToast } from 'primevue/usetoast'
import { useRouter } from 'vue-router'
import { useVila } from '../composables/useVila'
import { jogoAPI } from '../api/jogo'
import { ErroApi } from '../api/http'
import type { CodigoErro, ItemDto, ModeloItem, TipoTropa } from '../api/tipos'

// Modelos de armadura reconhecidos (ver spec game-army — Validação dos
// itens): qualquer modelo de armadura serve para qualquer tipo de tropa,
// diferente da arma, que deve ser exatamente a exigida pelo tipo.
const MODELOS_ARMADURA: ModeloItem[] = ['ARMADURA_COURO', 'ARMADURA_FERRO']

// Níveis possíveis de arma/armadura (ver TreinarRequest: @Min(1) @Max(5)).
const NIVEIS = [1, 2, 3, 4, 5]

const NOMES_TROPA: Record<TipoTropa, string> = {
  SOLDADO: 'Soldado',
  ARQUEIRO: 'Arqueiro',
  LANCEIRO: 'Lanceiro',
}

const TIPOS_TROPA: TipoTropa[] = ['SOLDADO', 'ARQUEIRO', 'LANCEIRO']

// Mensagens amigáveis para os códigos de erro 422 que `QuartelService.treinar`
// pode devolver (ver design.md — D4).
const MENSAGENS_ERRO_422: Partial<Record<CodigoErro, string>> = {
  CAPACIDADE_EXERCITO: 'Capacidade do exército insuficiente para essa quantidade.',
  ITEM_INDISPONIVEL: 'Não há armas ou armaduras suficientes disponíveis para essa configuração.',
  RECURSOS_INSUFICIENTES: 'Comida insuficiente para treinar essa quantidade.',
}

const { vila, catalogo, carregar, iniciarPoll, deslocamentoRelogio } = useVila()
const toast = useToast()
const router = useRouter()

const tipo = ref<TipoTropa>('SOLDADO')
const armaNivel = ref<number | null>(null)
// Chave combinada "MODELO::NIVEL" da opção de armadura escolhida (ver
// `chaveArmadura`/`parseArmadura` abaixo); o Select do PrimeVue precisa de um
// valor primitivo estável, não de um objeto recriado a cada render.
const armaduraSelecao = ref<string | null>(null)
const quantidade = ref<number>(1)
const treinando = ref(false)

const nivelQuartel = computed(() => {
  return vila.value?.predios.find((p) => p.tipo === 'QUARTEL')?.nivel ?? 0
})

function catalogoDoTipo(t: TipoTropa) {
  return catalogo.value?.tropas[t]
}

// Só lista tipos cujo requisito de nível de quartel já foi atendido (ver
// spec game-army — Tropas liberadas pelo nível do quartel). Enquanto o
// catálogo não chegou, nenhum tipo é oferecido para evitar uma seleção que
// a API ainda não validou.
const tiposDisponiveis = computed(() => {
  return TIPOS_TROPA.filter((t) => {
    const nivelMinimo = catalogoDoTipo(t)?.nivelMinimoQuartel
    if (nivelMinimo === undefined) return false
    return nivelMinimo <= nivelQuartel.value
  }).map((t) => ({ id: t, nome: NOMES_TROPA[t] }))
})

const armaExigida = computed(() => catalogoDoTipo(tipo.value)?.armaExigida)

function contarDisponiveis(modelo: ModeloItem, nivel: number): number {
  return (vila.value?.itens ?? []).filter(
    (item) => item.status === 'DISPONIVEL' && item.modelo === modelo && item.nivel === nivel,
  ).length
}

function chaveArmadura(modelo: ModeloItem, nivel: number): string {
  return `${modelo}::${nivel}`
}

function parseArmadura(chave: string): { modelo: ModeloItem; nivel: number } | null {
  const [modelo, nivelTexto] = chave.split('::')
  const nivel = Number(nivelTexto)
  if (!modelo || Number.isNaN(nivel)) return null
  return { modelo: modelo as ModeloItem, nivel }
}

// Dica de nível de arma, agrupada implicitamente pelo modelo exigido pelo
// tipo escolhido (ver design.md — D9): cada tipo só aceita um modelo de
// arma, então não há necessidade de um segundo nível de agrupamento aqui.
const armaNivelOpcoes = computed(() => {
  const modelo = armaExigida.value
  if (!modelo) return []
  return NIVEIS.map((nivel) => ({
    label: `Nível ${nivel} (${contarDisponiveis(modelo, nivel)} disponível)`,
    value: nivel,
  }))
})

// Armadura agrupada por modelo, com contagem por nível (ex.: "ARMADURA_COURO
// N1 (4 disponível)"), conforme design.md — D9.
const armaduraOpcoesAgrupadas = computed(() => {
  return MODELOS_ARMADURA.map((modelo) => ({
    label: modelo,
    items: NIVEIS.map((nivel) => ({
      label: `${modelo} N${nivel} (${contarDisponiveis(modelo, nivel)} disponível)`,
      value: chaveArmadura(modelo, nivel),
    })),
  }))
})

const ordemTreino = computed(() => vila.value?.ordens.find((o) => o.categoria === 'TREINO') ?? null)

const temOrdemEmAndamento = computed(() => ordemTreino.value !== null)

// Soma a quantidade de unidades vivas com a quantidade em ordens TREINO em
// andamento (ver design.md — D9: "unidadesAtivas soma quantidade").
const unidadesAtivas = computed(() => {
  const unidadesVivas = vila.value?.unidades.length ?? 0
  const emTreino = vila.value?.ordens
    .filter((o) => o.categoria === 'TREINO')
    .reduce((soma, o) => soma + o.quantidade, 0)
  return unidadesVivas + (emTreino ?? 0)
})

const capacidadeTotal = computed(() => vila.value?.capacidadeExercito ?? 0)

const capacidadeLivre = computed(() => Math.max(0, capacidadeTotal.value - unidadesAtivas.value))

// Dica de quantidade máxima treinável, só para UX (ver spec game-frontend —
// "Exceção para UX no Quartel"); o backend valida estritamente e rejeita com
// 422 qualquer quantidade acima do real.
const dicaMaximo = computed(() => {
  const modeloArma = armaExigida.value
  if (!modeloArma || armaNivel.value == null || !armaduraSelecao.value) return 0
  const armadura = parseArmadura(armaduraSelecao.value)
  if (!armadura) return 0

  const armasDisponiveis = contarDisponiveis(modeloArma, armaNivel.value)
  const armadurasDisponiveis = contarDisponiveis(armadura.modelo, armadura.nivel)

  const comidaPorTreino = catalogoDoTipo(tipo.value)?.comida ?? 0
  const comidaDisponivel = vila.value?.recursos.COMIDA ?? 0
  const porComida = comidaPorTreino > 0 ? Math.floor(comidaDisponivel / comidaPorTreino) : Number.POSITIVE_INFINITY

  return Math.max(0, Math.min(armasDisponiveis, armadurasDisponiveis, porComida, capacidadeLivre.value))
})

function preencherMaximo(): void {
  if (dicaMaximo.value >= 1) {
    quantidade.value = dicaMaximo.value
  }
}

const podeTreinar = computed(() => {
  if (treinando.value) return false
  if (nivelQuartel.value === 0) return false
  if (temOrdemEmAndamento.value) return false
  if (armaNivel.value == null || !armaduraSelecao.value) return false
  if (!quantidade.value || quantidade.value < 1) return false
  return true
})

const percentualOrdem = computed(() => {
  const ordem = ordemTreino.value
  if (!ordem) return 0
  const inicio = new Date(ordem.iniciadaEm).getTime()
  const conclusao = new Date(ordem.concluiEm).getTime()
  if (conclusao <= inicio) return 100
  const agora = Date.now() + deslocamentoRelogio.value
  return Math.min(100, Math.max(0, ((agora - inicio) / (conclusao - inicio)) * 100))
})

const tempoRestanteFormatado = computed(() => {
  const ordem = ordemTreino.value
  if (!ordem) return '0m 0s'
  const conclusao = new Date(ordem.concluiEm).getTime()
  const agora = Date.now() + deslocamentoRelogio.value
  const segundos = Math.max(0, Math.round((conclusao - agora) / 1000))
  const minutos = Math.floor(segundos / 60)
  const resto = segundos % 60
  return `${minutos}m ${resto}s`
})

function nomeItem(item: ItemDto | null): string {
  if (!item) return '—'
  return `${item.modelo} N${item.nivel}`
}

function abrirDetalhe(id: number): void {
  router.push(`/quartel/unidades/${id}`)
}

async function treinar(): Promise<void> {
  if (armaNivel.value == null || !armaduraSelecao.value) return
  const armadura = parseArmadura(armaduraSelecao.value)
  if (!armadura) return

  treinando.value = true
  try {
    await jogoAPI.treinar(tipo.value, armaNivel.value, armadura.modelo, armadura.nivel, quantidade.value)
    await carregar()
    quantidade.value = 1
    toast.add({ severity: 'success', summary: 'Treino iniciado', detail: 'Ordem de treino criada.', life: 3000 })
  } catch (e) {
    let mensagem = 'Erro ao treinar unidade.'
    if (e instanceof ErroApi) {
      if (e.status === 422) {
        mensagem = MENSAGENS_ERRO_422[e.codigo as CodigoErro] ?? e.message
      } else if (e.status === 400) {
        mensagem = 'Quantidade inválida.'
      } else {
        mensagem = e.message
      }
    }
    toast.add({ severity: 'error', summary: 'Erro ao treinar', detail: mensagem, life: 3000 })
  } finally {
    treinando.value = false
  }
}

onMounted(() => {
  carregar()
  iniciarPoll()
})
</script>

<style scoped>
.quartel-view {
  padding: 16px;
}

.formulario {
  margin-bottom: 24px;
  max-width: 800px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.campo-quantidade {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ordem-em-andamento {
  margin-top: 16px;
}

.capacidade {
  background: var(--p-content-background);
  padding: 16px;
  margin: 16px 0;
  border-radius: 4px;
}

.nome-link {
  color: var(--p-primary-color);
  text-decoration: none;
}

.nome-link:hover {
  text-decoration: underline;
}
</style>
