<script setup lang="ts">
import { computed, ref } from 'vue'
import Button from 'primevue/button'
import { corQualidade, rotuloBonus, rotuloQualidade, type ItemDTO } from '../../composables/useItens'
import { slotsPedra } from '../../composables/useInventario'
import { rotuloMagnitude, usePedras, type PedraDTO } from '../../composables/usePedras'

const props = defineProps<{ item: Pick<ItemDTO, 'id' | 'qualidade'>; pedras?: PedraDTO[]; editavel?: boolean }>()
const emit = defineEmits<{ (e: 'removida', pedraId: number): void }>()

const { removerPedra } = usePedras()
const confirmando = ref<number | null>(null)
const removendo = ref(false)
const erro = ref<string | null>(null)
const sucesso = ref<string | null>(null)

const lista = computed(() => props.pedras ?? [])
const total = computed(() => slotsPedra(props.item.qualidade))
const vazios = computed(() => Math.max(0, total.value - lista.value.length))

function resumo(p: PedraDTO) {
  return p.bonus.map((b) => `${rotuloBonus(b.codigo)} +${b.valor} (${rotuloMagnitude(b.magnitude)})`).join(', ') || '-'
}

function pedir(id: number) {
  erro.value = null
  sucesso.value = null
  confirmando.value = id
}

async function confirmar(id: number) {
  removendo.value = true
  erro.value = null
  try {
    await removerPedra(props.item.id, id)
    confirmando.value = null
    sucesso.value = 'Pedra removida e destruída.'
    emit('removida', id)
  } catch (e) {
    erro.value = e instanceof Error ? e.message : 'Erro ao remover a pedra'
  } finally {
    removendo.value = false
  }
}
</script>

<template>
  <div class="slots-pedras" data-testid="SlotsPedras">
    <p v-if="!total" data-testid="sem-slots">Esta qualidade não possui slots de pedra.</p>
    <ul v-else data-testid="slots-pedra" class="lista">
      <li v-for="p in lista" :key="p.id" :data-testid="`slot-pedra-${p.id}`" :title="resumo(p)">
        <span :style="{ color: corQualidade(p.qualidade), fontWeight: 600 }">Pedra {{ rotuloQualidade(p.qualidade) }}</span>:
        <span :data-testid="`bonus-pedra-${p.id}`">{{ resumo(p) }}</span>
        <template v-if="editavel">
          <Button v-if="confirmando !== p.id" label="Remover" size="small" severity="secondary"
            :data-testid="`remover-${p.id}`" @click="pedir(p.id)" />
          <span v-else class="confirmacao" :data-testid="`confirmacao-${p.id}`">
            Remover a pedra a destruirá permanentemente. Continuar?
            <Button label="Confirmar" size="small" severity="danger" :disabled="removendo"
              :data-testid="`confirmar-remocao-${p.id}`" @click="confirmar(p.id)" />
            <Button label="Cancelar" size="small" severity="secondary" :data-testid="`cancelar-remocao-${p.id}`"
              @click="confirmando = null" />
          </span>
        </template>
      </li>
      <li v-for="n in vazios" :key="`v${n}`" data-testid="slot-vazio">Slot {{ lista.length + n }}: vazio</li>
    </ul>
    <p v-if="erro" role="alert" class="erro" data-testid="erro-remocao">{{ erro }}</p>
    <p v-if="sucesso" role="status" data-testid="sucesso-remocao">{{ sucesso }}</p>
  </div>
</template>

<style scoped>
.lista { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 0.35rem; }
.confirmacao { display: inline-flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; margin-left: 0.5rem; }
.erro { color: var(--vl-error); }
</style>
