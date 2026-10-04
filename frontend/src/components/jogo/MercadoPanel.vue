<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputNumber from 'primevue/inputnumber'
import Select from 'primevue/select'
import SelectButton from 'primevue/selectbutton'
import { useMercado } from '../../composables/useMercado'
import { useEstoque } from '../../composables/useEstoque'

const m = useMercado()
const e = useEstoque()

const recurso = ref<string | null>(null)
const tipo = ref<'VENDA' | 'COMPRA'>('VENDA')
const quantidade = ref<number>(1)
const opcoesTipo = [
  { label: 'Vender', value: 'VENDA' },
  { label: 'Comprar', value: 'COMPRA' },
]

onMounted(async () => {
  await Promise.all([m.carregar(), e.carregar()])
})

const linhas = computed(() => {
  const mapa = new Map(e.recursos.value.map((r) => [r.recurso, r.quantidade]))
  return (m.precos.value?.precos ?? []).map((p) => ({ ...p, estoque: mapa.get(p.recurso) ?? 0 }))
})

const selecionada = computed(() => linhas.value.find((l) => l.recurso === recurso.value) ?? null)
const precoUnitario = computed(() =>
  selecionada.value ? (tipo.value === 'VENDA' ? selecionada.value.precoVenda : selecionada.value.precoCompra) : 0,
)
const total = computed(() => Math.round(precoUnitario.value * (quantidade.value || 0) * 100) / 100)
const ouro = computed(() => e.recursos.value.find((r) => r.recurso === 'OURO')?.quantidade ?? 0)
const podeEnviar = computed(
  () => !!m.precos.value?.mercadoAtivo && !!recurso.value && (quantidade.value ?? 0) > 0 && !m.enviando.value,
)

async function enviar() {
  if (!recurso.value) return
  const ok = await m.negociar(recurso.value, tipo.value, quantidade.value)
  if (ok) await e.carregar()
}
</script>

<template>
  <div class="mercado" data-testid="MercadoPanel">
    <p v-if="m.precos.value && !m.precos.value.mercadoAtivo" class="aviso" role="status" data-testid="sem-mercado">
      Não há Mercado ativo na vila. Construa um Mercado e aloque um Comerciante para negociar.
    </p>

    <p v-if="m.precos.value" data-testid="volume">
      Volume do turno: {{ m.precos.value.volumeUsado }} / {{ m.precos.value.volumeMaximo }}
      (restante {{ m.precos.value.volumeRestante }}) · PE do melhor Comerciante:
      {{ m.precos.value.peMelhorComerciante }}
    </p>

    <DataTable :value="linhas" data-key="recurso" size="small" data-testid="tabela-precos">
      <Column field="nome" header="Recurso" />
      <Column field="estoque" header="Em estoque" body-class="num" />
      <Column field="precoVenda" header="Preço de venda" body-class="num" />
      <Column field="precoCompra" header="Preço de compra" body-class="num" />
    </DataTable>

    <form class="ordem" data-testid="form-ordem" @submit.prevent="enviar">
      <SelectButton v-model="tipo" :options="opcoesTipo" option-label="label" option-value="value" :allow-empty="false" />
      <Select
        v-model="recurso"
        :options="linhas"
        option-label="nome"
        option-value="recurso"
        placeholder="Recurso"
        data-testid="select-recurso"
      />
      <InputNumber v-model="quantidade" :min="1" input-id="quantidade-ordem" show-buttons />
      <span v-if="selecionada" data-testid="resumo">
        Preço unitário: {{ precoUnitario }} · Total: {{ total }} Ouro
        <template v-if="tipo === 'VENDA'"> · Disponível: {{ selecionada.estoque }}</template>
        <template v-else> · Ouro disponível: {{ ouro }}</template>
      </span>
      <Button
        type="submit"
        :label="tipo === 'VENDA' ? 'Vender' : 'Comprar'"
        :disabled="!podeEnviar"
        :loading="m.enviando.value"
        data-testid="enviar-ordem"
      />
    </form>

    <p v-if="m.sucesso.value" class="sucesso" role="status" data-testid="sucesso-ordem">{{ m.sucesso.value }}</p>
    <p v-if="m.erro.value" class="erro" role="alert" data-testid="erro-ordem">{{ m.erro.value }}</p>

    <h3>Histórico de ordens</h3>
    <DataTable :value="m.ordens.value" data-key="id" size="small" data-testid="tabela-historico">
      <template #empty>Nenhuma ordem realizada.</template>
      <Column field="turno" header="Turno" body-class="num" />
      <Column field="tipo" header="Tipo" />
      <Column field="recurso" header="Recurso" />
      <Column field="quantidade" header="Qtd." body-class="num" />
      <Column field="precoUnitario" header="Preço unit." body-class="num" />
      <Column field="ouroTotal" header="Ouro total" body-class="num" />
    </DataTable>
  </div>
</template>

<style scoped>
.mercado { display: flex; flex-direction: column; gap: 1rem; }
.ordem { display: flex; flex-wrap: wrap; gap: 0.75rem; align-items: center; }
.aviso { color: var(--vl-warn); }
.sucesso { color: var(--vl-accent); }
.erro { color: var(--vl-error); }
.mercado h3 { font-family: var(--vl-font-display); color: var(--vl-text); }
.mercado :deep(.num) { font-family: var(--vl-font-mono); }
[data-testid="resumo"] { font-family: var(--vl-font-mono); }
</style>
