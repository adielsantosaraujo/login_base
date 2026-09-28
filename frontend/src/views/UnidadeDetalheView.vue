<template>
  <div class="unidade-detalhe-view">
    <div v-if="carregando" class="carregando">Carregando...</div>

    <div v-else-if="!unidade" class="nao-encontrada">
      <h1>Unidade não encontrada</h1>
      <Button label="Voltar" icon="pi pi-arrow-left" @click="voltar" />
    </div>

    <div v-else>
      <div class="cabecalho">
        <Button label="Voltar" icon="pi pi-arrow-left" text @click="voltar" />
        <h1>{{ unidade.nomeExibicao }}</h1>
        <div class="meta">
          <Tag :value="NOMES_TROPA[unidade.tipo]" />
          <Tag :value="unidade.status" :severity="unidade.status === 'EM_MASMORRA' ? 'warn' : 'success'" />
        </div>
      </div>

      <Card class="atributos">
        <template #title>Atributos</template>
        <template #content>
          <div class="grid-atributos">
            <div class="atributo">
              <span class="rotulo">HP</span>
              <span class="valor">{{ unidade.hp }}</span>
            </div>
            <div class="atributo">
              <span class="rotulo">Ataque</span>
              <span class="valor">{{ unidade.ataque }}</span>
            </div>
            <div class="atributo">
              <span class="rotulo">Defesa</span>
              <span class="valor">{{ unidade.defesa }}</span>
            </div>
            <div class="atributo">
              <span class="rotulo">Alcance</span>
              <span class="valor">{{ unidade.alcance }}</span>
            </div>
            <div class="atributo">
              <span class="rotulo">Movimento</span>
              <span class="valor">{{ unidade.movimento }}</span>
            </div>
          </div>
        </template>
      </Card>

      <Divider />

      <h2>Equipamento</h2>
      <div class="grid-slots">
        <Card v-for="slot in SLOTS" :key="slot" class="slot-card">
          <template #title>{{ ROTULOS_SLOT[slot] }}</template>
          <template #content>
            <div v-if="unidade.equipamento[slot]" class="slot-ocupado">
              <p class="item-linha">{{ descricaoItem(unidade.equipamento[slot]!) }}</p>
              <p class="item-origem">Origem: {{ unidade.equipamento[slot]!.origem }}</p>
              <div v-if="slot === 'ARMA' || slot === 'ARMADURA'" class="slot-acao">
                <Button
                  label="Trocar"
                  size="small"
                  severity="secondary"
                  :disabled="unidade.status === 'EM_MASMORRA'"
                  :title="unidade.status === 'EM_MASMORRA' ? 'Indisponível durante a masmorra' : undefined"
                  @click="abrirTroca(slot)"
                />
              </div>
            </div>
            <div v-else class="slot-vazio">
              <span>Vazio</span>
              <div v-if="slot === 'ARMA' || slot === 'ARMADURA'" class="slot-acao">
                <Button
                  label="Trocar"
                  size="small"
                  severity="secondary"
                  :disabled="unidade.status === 'EM_MASMORRA'"
                  :title="unidade.status === 'EM_MASMORRA' ? 'Indisponível durante a masmorra' : undefined"
                  @click="abrirTroca(slot)"
                />
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <Dialog
      v-model:visible="dialogoVisivel"
      :header="slotEmTroca ? `Trocar ${ROTULOS_SLOT[slotEmTroca]}` : 'Trocar'"
      modal
      style="width: 28rem"
      @update:visible="(visivel: boolean) => !visivel && fecharDialogo()"
    >
      <Listbox
        :options="itensCompativeis"
        :option-label="descricaoItem"
        data-key="id"
        :disabled="trocando"
        empty-message="Nenhum item compatível disponível"
        class="listbox-itens-troca"
        @update:model-value="escolherItem"
      >
        <template #option="{ option }">
          <div class="item-troca-opcao">
            <span class="item-troca-descricao">{{ descricaoItem(option) }}</span>
            <span class="item-troca-origem">{{ option.origem }}</span>
          </div>
        </template>
      </Listbox>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Card from 'primevue/card'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Divider from 'primevue/divider'
import Dialog from 'primevue/dialog'
import Listbox from 'primevue/listbox'
import { useToast } from 'primevue/usetoast'
import { useVila } from '../composables/useVila'
import { jogoAPI } from '../api/jogo'
import { ErroApi } from '../api/http'
import type { ItemDto, ModeloItem, SlotEquipamento, TipoTropa } from '../api/tipos'

const NOMES_TROPA: Record<TipoTropa, string> = {
  SOLDADO: 'Soldado',
  ARQUEIRO: 'Arqueiro',
  LANCEIRO: 'Lanceiro',
}

const NOMES_MODELO: Record<ModeloItem, string> = {
  ESPADA: 'Espada',
  LANCA: 'Lança',
  ARCO: 'Arco',
  ARMADURA_COURO: 'Armadura de couro',
  ARMADURA_FERRO: 'Armadura de ferro',
}

// Ordem de exibição da grade de 9 slots (ver SlotEquipamento.java / design.md
// — D9): mesma ordem do type `SlotEquipamento` em tipos.ts.
const SLOTS: SlotEquipamento[] = [
  'ARMA',
  'ARMADURA',
  'CABECA',
  'BOTA',
  'LUVA',
  'COLAR',
  'ANEL_1',
  'ANEL_2',
  'ANEL_3',
]

const ROTULOS_SLOT: Record<SlotEquipamento, string> = {
  ARMA: 'Arma',
  ARMADURA: 'Armadura',
  CABECA: 'Capacete/chapéu',
  BOTA: 'Bota',
  LUVA: 'Luva',
  COLAR: 'Colar',
  ANEL_1: 'Anel 1',
  ANEL_2: 'Anel 2',
  ANEL_3: 'Anel 3',
}

const route = useRoute()
const router = useRouter()
const { vila, catalogo, carregando, carregar } = useVila()
const toast = useToast()

const unidadeId = computed(() => Number(route.params.id))

const unidade = computed(() => {
  return vila.value?.unidades.find((u) => u.id === unidadeId.value) ?? null
})

// Diálogo "Trocar" (task 3.4): estado do slot em troca e lista de itens
// compatíveis DISPONIVEL da vila. Filtro é só UX — o backend valida e
// rejeita item incompatível com 422 ITEM_INDISPONIVEL (ver spec
// game-frontend — "Sem cálculo de regras no frontend").
const dialogoVisivel = ref(false)
const slotEmTroca = ref<SlotEquipamento | null>(null)
const trocando = ref(false)

const itensCompativeis = computed<ItemDto[]>(() => {
  const slot = slotEmTroca.value
  if (!slot || !vila.value) return []
  if (slot === 'ARMA') {
    const armaExigida = unidade.value ? catalogo.value?.tropas[unidade.value.tipo]?.armaExigida : undefined
    if (!armaExigida) return []
    return vila.value.itens.filter((item) => item.status === 'DISPONIVEL' && item.modelo === armaExigida)
  }
  if (slot === 'ARMADURA') {
    return vila.value.itens.filter(
      (item) => item.status === 'DISPONIVEL' && catalogo.value?.modelosItem[item.modelo]?.categoria === 'ARMADURA',
    )
  }
  return []
})

function abrirTroca(slot: SlotEquipamento): void {
  slotEmTroca.value = slot
  dialogoVisivel.value = true
}

function fecharDialogo(): void {
  dialogoVisivel.value = false
  slotEmTroca.value = null
}

async function escolherItem(item: ItemDto | null): Promise<void> {
  if (!item || !unidade.value || !slotEmTroca.value) return
  trocando.value = true
  try {
    vila.value = await jogoAPI.trocarEquipamento(unidade.value.id, slotEmTroca.value, item.id)
    fecharDialogo()
    toast.add({ severity: 'success', summary: 'Equipamento trocado', detail: 'Item equipado com sucesso.', life: 3000 })
  } catch (e) {
    const mensagem = e instanceof ErroApi ? e.message : 'Erro ao trocar equipamento.'
    toast.add({ severity: 'error', summary: 'Erro ao trocar equipamento', detail: mensagem, life: 3000 })
  } finally {
    trocando.value = false
  }
}

// Descrição de um item ocupando um slot: "Modelo · NNível · Atributos" (ver
// spec game-frontend — Detalhe da unidade), ex.: "Espada · N1 · Ataque 6".
function descricaoItem(item: ItemDto): string {
  const partes: string[] = []
  if (item.ataque > 0) partes.push(`Ataque ${item.ataque}`)
  if (item.defesa > 0) partes.push(`Defesa ${item.defesa}`)
  if (item.alcance) partes.push(`Alcance ${item.alcance}`)
  const atributos = partes.join(', ')
  const modelo = NOMES_MODELO[item.modelo] ?? item.modelo
  return atributos ? `${modelo} · N${item.nivel} · ${atributos}` : `${modelo} · N${item.nivel}`
}

function voltar(): void {
  router.push('/quartel')
}

onMounted(() => {
  carregar()
})
</script>

<style scoped>
.unidade-detalhe-view {
  padding: 16px;
}

.carregando {
  padding: 16px;
}

.nao-encontrada {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.cabecalho {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  margin-bottom: 16px;
}

.cabecalho h1 {
  margin: 0;
}

.meta {
  display: flex;
  gap: 8px;
}

.atributos {
  max-width: 700px;
  margin-bottom: 16px;
}

.grid-atributos {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.atributo {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.atributo .rotulo {
  font-size: 0.85em;
  color: var(--p-text-muted-color);
}

.atributo .valor {
  font-size: 1.25em;
  font-weight: bold;
}

.grid-slots {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.slot-ocupado,
.slot-vazio {
  min-height: 64px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.slot-vazio {
  align-items: center;
  color: var(--p-text-muted-color);
}

.item-linha {
  margin: 0 0 4px 0;
}

.item-origem {
  margin: 0;
  font-size: 0.85em;
  color: var(--p-text-muted-color);
}

.slot-acao {
  min-height: 36px;
  margin-top: 8px;
}

.listbox-itens-troca {
  width: 100%;
}

.item-troca-opcao {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-troca-origem {
  font-size: 0.85em;
  color: var(--p-text-muted-color);
}
</style>
