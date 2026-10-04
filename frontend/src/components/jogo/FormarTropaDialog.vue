<script setup lang="ts">
import { computed, ref } from 'vue'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import type { GuerreiroDisponivelDTO, NovaTropa, PosicaoTropa } from '../../composables/useQuartel'

const props = defineProps<{
  /** 'formar' pede o nome da tropa; 'adicionar' só escolhe membros para uma tropa existente. */
  modo?: 'formar' | 'adicionar'
  guerreiros: GuerreiroDisponivelDTO[]
  /** Vagas livres na capacidade do quartel (opcional; limita a seleção). */
  vagasLivres?: number
  enviando?: boolean
  erro?: string | null
}>()
const emit = defineEmits<{
  (e: 'confirmar', req: NovaTropa): void
  (e: 'fechar'): void
}>()

const nome = ref('')
const selecionados = ref<Record<number, PosicaoTropa>>({})

const formando = computed(() => (props.modo ?? 'formar') === 'formar')
const total = computed(() => Object.keys(selecionados.value).length)
const excedeu = computed(() => props.vagasLivres != null && total.value > props.vagasLivres)
const bloqueio = computed<string | null>(() => {
  if (formando.value && !nome.value.trim()) return 'Informe o nome da tropa'
  if (!formando.value && total.value === 0) return 'Selecione ao menos um guerreiro'
  if (excedeu.value) return 'Capacidade do quartel excedida'
  return null
})

function alternar(g: GuerreiroDisponivelDTO, marcado: boolean) {
  if (!g.elegivel) return
  const s = { ...selecionados.value }
  if (marcado) s[g.id] = s[g.id] ?? 'FRENTE'
  else delete s[g.id]
  selecionados.value = s
}

function posicao(id: number, valor: PosicaoTropa) {
  selecionados.value = { ...selecionados.value, [id]: valor }
}

function confirmar() {
  if (bloqueio.value) return
  emit('confirmar', {
    nome: nome.value.trim(),
    membros: Object.entries(selecionados.value).map(([id, p]) => ({ cidadaoId: Number(id), posicao: p })),
  })
}
</script>

<template>
  <div class="formar-tropa" data-testid="FormarTropaDialog" role="dialog"
    :aria-label="formando ? 'Formar tropa' : 'Adicionar membros'">
    <h3>{{ formando ? 'Formar tropa' : 'Adicionar membros' }}</h3>
    <label v-if="formando" class="campo">Nome da tropa
      <InputText v-model="nome" maxlength="100" data-testid="nome-tropa" />
    </label>

    <h4>Guerreiros</h4>
    <p v-if="!guerreiros.length" data-testid="sem-guerreiros">Nenhum cidadão disponível.</p>
    <ul v-else class="lista">
      <li v-for="g in guerreiros" :key="g.id" :data-testid="`guerreiro-${g.id}`" :class="{ inelegivel: !g.elegivel }">
        <label>
          <input type="checkbox" :disabled="!g.elegivel" :checked="g.id in selecionados"
            :data-testid="`sel-${g.id}`" @change="alternar(g, ($event.target as HTMLInputElement).checked)" />
          {{ g.nome }} · {{ g.idadeAnos }} anos · PE {{ g.peGuerreiro }} · {{ g.arma ?? 'sem arma' }}
        </label>
        <select v-if="g.id in selecionados" :value="selecionados[g.id]" :aria-label="`Posição de ${g.nome}`"
          :data-testid="`posicao-${g.id}`" @change="posicao(g.id, ($event.target as HTMLSelectElement).value as PosicaoTropa)">
          <option value="FRENTE">Frente</option>
          <option value="RETAGUARDA">Retaguarda</option>
        </select>
        <span v-if="!g.elegivel" class="motivo" :data-testid="`motivo-${g.id}`">{{ g.motivo }}</span>
      </li>
    </ul>

    <p data-testid="contagem">
      Selecionados: {{ total }}<template v-if="vagasLivres != null"> (vagas livres: {{ vagasLivres }})</template>
    </p>
    <p v-if="bloqueio" class="dica" data-testid="bloqueio">{{ bloqueio }}</p>
    <p v-if="erro" class="erro" role="alert" data-testid="erro-formar-tropa">{{ erro }}</p>
    <div class="acoes">
      <Button :label="formando ? 'Formar tropa' : 'Adicionar'" data-testid="confirmar-tropa"
        :disabled="!!bloqueio || enviando" :loading="enviando" @click="confirmar" />
      <Button label="Fechar" severity="secondary" data-testid="fechar-FormarTropaDialog" @click="emit('fechar')" />
    </div>
  </div>
</template>

<style scoped>
.campo { display: flex; flex-direction: column; gap: 0.25rem; max-width: 24rem; }
.lista { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 0.4rem; }
.lista li { display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap; }
.inelegivel { opacity: 0.6; }
.motivo, .erro { color: var(--vl-error); }
.dica { font-size: 0.9em; opacity: 0.8; }
.acoes { display: flex; gap: 0.75rem; margin-top: 0.5rem; flex-wrap: wrap; }
</style>
