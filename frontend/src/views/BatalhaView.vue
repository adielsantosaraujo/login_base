<template>
  <div v-if="batalha" class="batalha-view">
    <h1>Masmorra {{ batalha.masmorraNivel }} — Turno {{ batalha.turno }}/{{ batalha.turnoMaximo }}</h1>

    <div class="conteudo">
      <GradeBatalha :batalha="batalha" @acao="agir" />

      <div class="direita">
        <Card class="log">
          <template #title>Log</template>
          <template #content>
            <ScrollPanel style="width: 100%; height: 300px">
              <div v-for="(linha, idx) in batalha.log" :key="idx" class="log-linha">
                {{ linha }}
              </div>
            </ScrollPanel>
          </template>
        </Card>
      </div>
    </div>

    <Dialog v-model:visible="mostrarResultado" header="Resultado da batalha" :closable="false" modal>
      <div v-if="batalha.status === 'VITORIA'">
        <p class="sucesso">VITÓRIA!</p>
        <div v-if="batalha.loot">
          <h4>Loot</h4>
          <p v-for="[recurso, valor] in Object.entries(batalha.loot.recursos)" :key="recurso">
            {{ recurso }}: +{{ valor }}
          </p>
          <p v-for="[cultivo, qtd] in Object.entries(batalha.loot.sementes)" :key="cultivo">
            Semente {{ cultivo }}: +{{ qtd }}
          </p>
          <p v-for="(item, idx) in batalha.loot.itens" :key="idx">
            {{ item.modelo }} N{{ item.nivel }}
          </p>
        </div>
      </div>
      <div v-else-if="batalha.status === 'DERROTA'">
        <p class="derrota">DERROTA</p>
      </div>
      <template #footer>
        <Button label="Voltar" @click="router.push('/masmorras')" />
      </template>
    </Dialog>
  </div>
  <div v-else class="carregando">Carregando...</div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Card from 'primevue/card'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import ScrollPanel from 'primevue/scrollpanel'
import { useToast } from 'primevue/usetoast'
import { jogoAPI } from '../api/jogo'
import { ErroApi } from '../api/http'
import { useVila } from '../composables/useVila'
import GradeBatalha from '../components/GradeBatalha.vue'
import type { AcaoCombateRequest, BatalhaDto } from '../api/tipos'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { carregar } = useVila()

const batalha = ref<BatalhaDto | null>(null)
const mostrarResultado = ref(false)

const batalhaId = computed(() => Number(route.params.id))

async function recarregarBatalha() {
  try {
    batalha.value = await jogoAPI.consultarBatalha(batalhaId.value)
    if (batalha.value.status !== 'EM_ANDAMENTO') {
      mostrarResultado.value = true
    }
  } catch (e) {
    const mensagem = e instanceof ErroApi ? e.message : 'Erro ao carregar batalha.'
    toast.add({ severity: 'error', summary: 'Erro', detail: mensagem, life: 4000 })
  }
}

async function agir(acao: AcaoCombateRequest) {
  try {
    batalha.value = await jogoAPI.agir(batalhaId.value, acao)
    if (batalha.value.status !== 'EM_ANDAMENTO') {
      mostrarResultado.value = true
      await carregar()
    }
  } catch (e) {
    // Turno desatualizado (409): recarrega a batalha silenciosamente, sem
    // exibir erro ao usuário (ver spec game-frontend — Mensagens de erro).
    if (e instanceof ErroApi && e.status === 409) {
      await recarregarBatalha()
      return
    }
    const mensagem = e instanceof ErroApi ? e.message : 'Erro ao executar ação.'
    toast.add({ severity: 'error', summary: 'Erro', detail: mensagem, life: 4000 })
  }
}

onMounted(() => recarregarBatalha())
</script>

<style scoped>
.batalha-view {
  padding: 16px;
}

.conteudo {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.direita {
  flex: 1;
  min-width: 260px;
}

.log {
  max-width: 320px;
}

.log-linha {
  font-size: 12px;
  line-height: 1.6;
}

.sucesso {
  color: green;
  font-size: 24px;
  font-weight: bold;
}

.derrota {
  color: red;
  font-size: 24px;
  font-weight: bold;
}

.carregando {
  padding: 16px;
}
</style>
