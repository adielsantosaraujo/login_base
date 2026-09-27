<template>
  <div class="masmorras-view">
    <h1>Masmorras</h1>

    <Card v-if="batalhaAtivaId" class="retomar">
      <template #content>
        <p>Você tem uma batalha em andamento!</p>
        <Button label="Retomar batalha" @click="$router.push(`/batalhas/${batalhaAtivaId}`)" />
      </template>
    </Card>

    <div class="niveis">
      <Card v-for="nivel in 5" :key="nivel" class="cartao-nivel">
        <template #title>Masmorra {{ nivel }}</template>

        <template #content>
          <p v-if="nivel > masmorraNivelLiberado" class="bloqueado">Bloqueado</p>
          <div v-else>
            <p>Inimigos: {{ composicao[nivel]?.join(', ') ?? '—' }}</p>

            <MultiSelect
              v-model="unidadesEscolhidasPorNivel[nivel]"
              :options="unidadesDisponiveis"
              optionLabel="descricao"
              optionValue="id"
              placeholder="Selecione até 4 unidades"
              :maxSelectedLabels="4"
              class="selecao-unidades"
            />

            <Button
              label="Entrar"
              class="botao-entrar"
              :disabled="
                (unidadesEscolhidasPorNivel[nivel]?.length ?? 0) === 0 ||
                (unidadesEscolhidasPorNivel[nivel]?.length ?? 0) > 4 ||
                !!batalhaAtivaId
              "
              @click="entrar(nivel)"
            />
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Card from 'primevue/card'
import Button from 'primevue/button'
import MultiSelect from 'primevue/multiselect'
import { useToast } from 'primevue/usetoast'
import { useVila } from '../composables/useVila'
import { jogoAPI } from '../api/jogo'
import { ErroApi } from '../api/http'

const router = useRouter()
const toast = useToast()
const { vila, catalogo, carregar } = useVila()

// Seleção de unidades independente por nível de masmorra (um `MultiSelect`
// por card): usar um único ref compartilhado faria a seleção de um card
// aparecer nos outros.
const unidadesEscolhidasPorNivel = ref<Record<number, number[]>>({ 1: [], 2: [], 3: [], 4: [], 5: [] })

const masmorraNivelLiberado = computed(() => vila.value?.masmorraNivelLiberado ?? 1)
const batalhaAtivaId = computed(() => vila.value?.batalhaAtivaId ?? null)

const composicao = computed(() => {
  const comp: Record<number, string[]> = {}
  const masmorras = catalogo.value?.masmorras ?? {}
  Object.values(masmorras).forEach((m) => {
    comp[m.nivel] = m.composicaoInimigos
  })
  return comp
})

const unidadesDisponiveis = computed(() => {
  return (vila.value?.unidades ?? [])
    .filter((u) => u.status === 'DISPONIVEL')
    .map((u) => ({
      id: u.id,
      descricao: `${u.tipo} (HP ${u.hp})`,
    }))
})

async function entrar(nivel: number) {
  try {
    const unidadeIds = unidadesEscolhidasPorNivel.value[nivel] ?? []
    const batalha = await jogoAPI.iniciarBatalha(nivel, unidadeIds)
    router.push(`/batalhas/${batalha.id}`)
  } catch (e) {
    const mensagem = e instanceof ErroApi ? e.message : 'Erro ao iniciar batalha.'
    toast.add({ severity: 'error', summary: 'Erro', detail: mensagem, life: 4000 })
  }
}

onMounted(() => carregar())
</script>

<style scoped>
.masmorras-view {
  padding: 16px;
}

.niveis {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  margin-top: 24px;
}

.retomar {
  max-width: 600px;
  margin-bottom: 24px;
}

.bloqueado {
  color: var(--p-text-muted-color, #888);
  font-style: italic;
}

.selecao-unidades {
  width: 100%;
  margin: 12px 0;
}

.botao-entrar {
  margin-top: 8px;
}
</style>
