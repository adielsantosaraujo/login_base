<script setup lang="ts">
import { computed } from 'vue'
import BotaoCta from '../vilarejo/BotaoCta.vue'
import ChecklistItem from '../vilarejo/ChecklistItem.vue'

const props = withDefaults(defineProps<{
  construtores: number
  minConstrutores: number
  carregadores: number
  minCarregadores: number
  pendentes: number
  liderEscolhido: boolean
  enviando: boolean
  /** Demais obrigatórios (população completa, limites). */
  outrosOk?: boolean
}>(), { outrosOk: true })
const emit = defineEmits<{ confirmar: [] }>()

const valido = computed(
  () =>
    props.construtores >= props.minConstrutores &&
    props.carregadores >= props.minCarregadores &&
    props.liderEscolhido &&
    props.outrosOk,
)
</script>

<template>
  <section aria-labelledby="cf-titulo" class="cf-painel">
    <h2 id="cf-titulo" class="cf-titulo">Confirmação</h2>
    <ul class="cf-lista">
      <ChecklistItem
        :estado="construtores >= minConstrutores ? 'ok' : 'pendente'"
        :texto="`Construtores principais ${construtores} / ${minConstrutores}`"
      />
      <ChecklistItem
        :estado="carregadores >= minCarregadores ? 'ok' : 'pendente'"
        :texto="`Carregadores principais ${carregadores} / ${minCarregadores}`"
      />
      <ChecklistItem
        :estado="pendentes > 0 ? 'aviso' : 'ok'"
        :texto="pendentes > 0 ? `Pontos pendentes ${pendentes}` : 'Todos os pontos usados'"
      />
    </ul>
    <BotaoCta
      :ativo="valido"
      :carregando="enviando"
      data-testid="confirmar"
      @click="emit('confirmar')"
    >
      {{ valido ? 'Confirmar população' : 'Ajuste os mínimos' }}
    </BotaoCta>
  </section>
</template>

<style scoped>
.cf-painel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.cf-titulo {
  margin: 0;
  font-family: var(--vl-font-display);
  font-size: 17px;
  color: var(--vl-text);
}
.cf-lista {
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
