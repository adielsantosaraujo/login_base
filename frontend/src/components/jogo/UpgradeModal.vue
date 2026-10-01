<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import { LADO_REGIAO, useUpgrade } from '../../composables/useUpgrade'
import { ROTULOS_ESTADO, ROTULOS_TIPO, rotulo } from '../../composables/useMapa'

const props = defineProps<{ construcaoId: number }>()
const emit = defineEmits<{ (e: 'fechar'): void; (e: 'atualizado'): void }>()

const u = useUpgrade()
const novaX = ref<number>(0)
const novaY = ref<number>(0)

onMounted(() => u.carregar(props.construcaoId))
watch(() => props.construcaoId, (id) => u.carregar(id))
watch(
  () => u.predio.value,
  (p) => {
    if (p) {
      novaX.value = p.x
      novaY.value = p.y
    }
  },
)

const nome = computed(() => rotulo(ROTULOS_TIPO, u.predio.value?.tipo))

const motivoBloqueio = computed<string | null>(() => {
  const p = u.predio.value
  if (!p) return 'Carregando'
  if (p.estado !== 'ATIVA') return 'Só é possível melhorar um prédio ativo'
  if (!u.nivelNovo.value) return 'Prédio já está no nível máximo'
  if (u.faltantes.value.length) return `Recursos insuficientes: ${u.faltantes.value.join(', ')}`
  const t = u.tamanhoNovo.value
  const x = novaX.value
  const y = novaY.value
  if (x < 0 || y < 0 || x + t > LADO_REGIAO || y + t > LADO_REGIAO) return 'Posição fora da região'
  if (x > p.x || y > p.y || x + t < p.x + p.tamanho || y + t < p.y + p.tamanho) {
    return 'A nova área deve conter a área atual do prédio'
  }
  return null
})

async function confirmar() {
  const r = await u.confirmar(props.construcaoId, {
    novoNivel: u.nivelNovo.value ?? undefined,
    novaX: novaX.value,
    novaY: novaY.value,
  })
  if (r) emit('atualizado')
}
</script>

<template>
  <div class="upgrade-modal" data-testid="UpgradeModal">
    <h3>Melhorar {{ nome }}</h3>
    <p v-if="u.carregando.value" data-testid="carregando">Carregando...</p>
    <template v-else-if="u.predio.value">
      <p data-testid="atual">
        Atual: {{ u.predio.value.nivel }} · posição ({{ u.predio.value.x }}, {{ u.predio.value.y }}) ·
        {{ u.predio.value.tamanho }}x{{ u.predio.value.tamanho }} ·
        {{ rotulo(ROTULOS_ESTADO, u.predio.value.estado) }}
      </p>
      <template v-if="u.nivelNovo.value">
        <p data-testid="proximo">
          {{ u.predio.value.nivel }} → {{ u.nivelNovo.value }} · novo tamanho
          {{ u.tamanhoNovo.value }}x{{ u.tamanhoNovo.value }} · PO {{ u.poNovo.value }}
        </p>
        <ul data-testid="custos">
          <li
            v-for="(q, r) in u.custo.value"
            :key="r"
            :data-testid="`custo-${r}`"
            :class="{ falta: u.faltantes.value.includes(String(r)) }"
          >
            {{ r }}: {{ q }} (disponível {{ u.estoque.value[r] ?? 0 }})
          </li>
        </ul>
        <div class="posicao">
          <label>Nova X <InputNumber v-model="novaX" :min="0" :max="LADO_REGIAO - 1" input-id="nova-x" /></label>
          <label>Nova Y <InputNumber v-model="novaY" :min="0" :max="LADO_REGIAO - 1" input-id="nova-y" /></label>
        </div>
      </template>
    </template>
    <p v-if="motivoBloqueio && !u.carregando.value" data-testid="motivo">{{ motivoBloqueio }}</p>
    <p v-if="u.erro.value" class="erro" role="alert" data-testid="erro-upgrade">{{ u.erro.value }}</p>
    <div class="acoes">
      <Button
        label="Confirmar upgrade"
        data-testid="confirmar-upgrade"
        :disabled="!!motivoBloqueio || u.enviando.value"
        :loading="u.enviando.value"
        @click="confirmar"
      />
      <Button label="Fechar" severity="secondary" data-testid="fechar-UpgradeModal" @click="emit('fechar')" />
    </div>
  </div>
</template>

<style scoped>
.falta { color: var(--p-red-500, #dc2626); }
.erro { color: var(--p-red-500, #dc2626); }
.posicao, .acoes { display: flex; gap: 1rem; margin-top: 0.5rem; }
</style>
