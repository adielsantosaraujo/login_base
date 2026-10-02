<script setup lang="ts">
import { computed, ref } from 'vue'
import Button from 'primevue/button'
import type { DestinoDTO, TropaDTO } from '../../composables/useQuartel'

const props = defineProps<{
  tropa: TropaDTO
  destinos: DestinoDTO[]
  carregando?: boolean
  enviando?: boolean
  erro?: string | null
}>()
const emit = defineEmits<{
  (e: 'confirmar', masmorraId: number): void
  (e: 'fechar'): void
}>()

const escolhido = ref<number | null>(null)

const temFerido = computed(() => props.tropa.membros.some((m) => m.estado === 'FERIDO'))
const semMembros = computed(() => props.tropa.membros.length === 0)
const bloqueioTropa = computed<string | null>(() => {
  if (semMembros.value) return 'A tropa não tem membros.'
  if (temFerido.value) return 'A tropa tem membro ferido; remova-o para enviar a expedição.'
  return null
})

function insuficiente(d: DestinoDTO) {
  return d.comidaDisponivel < d.comidaNecessaria
}

const podeConfirmar = computed(() => {
  if (bloqueioTropa.value || props.enviando || escolhido.value == null) return false
  const d = props.destinos.find((x) => x.masmorraId === escolhido.value)
  return !!d && !insuficiente(d)
})

function confirmar() {
  if (podeConfirmar.value && escolhido.value != null) emit('confirmar', escolhido.value)
}
</script>

<template>
  <div class="expedicao-dialog" data-testid="ExpedicaoDialog" role="dialog" :aria-label="`Enviar expedição: ${tropa.nome}`">
    <h3>Enviar expedição: {{ tropa.nome }}</h3>
    <p v-if="semMembros" class="aviso" data-testid="aviso-sem-membros">A tropa não tem membros.</p>
    <p v-else-if="temFerido" class="aviso" data-testid="aviso-ferido">{{ bloqueioTropa }}</p>

    <p v-if="carregando" data-testid="carregando-destinos">Carregando destinos...</p>
    <p v-else-if="!destinos.length" data-testid="sem-masmorras">Nenhuma masmorra ativa.</p>
    <ul v-else class="lista">
      <li v-for="d in destinos" :key="d.masmorraId" :data-testid="`destino-${d.masmorraId}`"
        :class="{ insuficiente: insuficiente(d) }">
        <label>
          <input type="radio" name="destino" :value="d.masmorraId" :checked="escolhido === d.masmorraId"
            :disabled="insuficiente(d)" :data-testid="`sel-destino-${d.masmorraId}`"
            @change="escolhido = d.masmorraId" />
          Região {{ d.regiao }} · nível {{ d.nivel }} · {{ d.turnosViagem }} turno(s) de viagem ·
          comida {{ d.comidaNecessaria }} necessária / {{ d.comidaDisponivel }} disponível
        </label>
        <span v-if="insuficiente(d)" class="aviso" :data-testid="`aviso-comida-${d.masmorraId}`">Comida insuficiente</span>
      </li>
    </ul>

    <p v-if="erro" class="erro" role="alert" data-testid="erro-expedicao">{{ erro }}</p>
    <div class="acoes">
      <Button label="Enviar expedição" data-testid="confirmar-expedicao" :disabled="!podeConfirmar"
        :loading="enviando" @click="confirmar" />
      <Button label="Fechar" severity="secondary" data-testid="fechar-ExpedicaoDialog" @click="emit('fechar')" />
    </div>
  </div>
</template>

<style scoped>
.lista { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 0.4rem; }
.lista li { display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap; }
.insuficiente { opacity: 0.7; }
.aviso, .erro { color: var(--p-red-500, #dc2626); }
.acoes { display: flex; gap: 0.75rem; margin-top: 0.5rem; flex-wrap: wrap; }
</style>
