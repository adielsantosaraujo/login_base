<script setup lang="ts">
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import { proximoDoLimite, type LinhaEstoque } from '../composables/useEstoque'

defineProps<{ recursos: LinhaEstoque[] }>()

const inteiro = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 0 })

function formatar(valor: number): string {
  return inteiro.format(Math.floor(valor))
}

function largura(linha: LinhaEstoque): string {
  return `${Math.min(100, linha.percentualUsado ?? 0)}%`
}

function linhaClasse(linha: LinhaEstoque): string {
  return proximoDoLimite(linha) ? 'linha-alerta' : ''
}
</script>

<template>
  <DataTable :value="recursos" data-key="recurso" :row-class="linhaClasse" data-testid="estoque-tabela">
    <Column field="nome" header="Recurso" />
    <Column header="Quantidade">
      <template #body="{ data }">
        <span data-testid="quantidade">{{ formatar(data.quantidade) }}</span>
      </template>
    </Column>
    <Column header="Capacidade">
      <template #body="{ data }">
        <span data-testid="capacidade">{{ data.capacidade === null ? 'Ilimitada' : formatar(data.capacidade) }}</span>
      </template>
    </Column>
    <Column header="Uso">
      <template #body="{ data }">
        <div v-if="data.capacidade !== null" class="barra" role="progressbar" :aria-valuenow="Math.round(data.percentualUsado ?? 0)" aria-valuemin="0" aria-valuemax="100" :aria-label="`Uso de ${data.nome}`">
          <div class="barra-preenchida" :class="{ alerta: proximoDoLimite(data) }" :style="{ width: largura(data) }"></div>
        </div>
        <span v-else>-</span>
      </template>
    </Column>
    <Column header="Status">
      <template #body="{ data }">
        <span v-if="proximoDoLimite(data)" class="status-alerta" data-testid="status-alerta">
          <i class="pi pi-exclamation-triangle" aria-hidden="true"></i> Próximo do limite
        </span>
        <span v-else>Normal</span>
      </template>
    </Column>
  </DataTable>
</template>

<style scoped>
.barra { background: var(--vl-surface-4); border-radius: var(--vl-radius-chip); height: 0.6rem; width: 8rem; overflow: hidden; }
.barra-preenchida { background: var(--vl-accent); height: 100%; }
.barra-preenchida.alerta { background: var(--vl-error); }
.status-alerta { color: var(--vl-error); font-weight: 600; }
:deep(.linha-alerta) { background: color-mix(in oklch, var(--vl-error) 14%, transparent) !important; } /* transparencia derivada do token */
[data-testid="quantidade"], [data-testid="capacidade"] { font-family: var(--vl-font-mono); }
</style>
