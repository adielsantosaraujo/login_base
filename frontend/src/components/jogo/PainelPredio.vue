<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import Button from 'primevue/button'
import Select from 'primevue/select'
import { useAlocacoes } from '../../composables/useAlocacoes'
import { ROTULOS_ESTADO, ROTULOS_TIPO, ROTULOS_CONSTRUCAO, rotulo } from '../../composables/useMapa'
import { ROTULOS_PROFISSAO } from '../../composables/usePopulacao'
import { ehOficina } from '../../composables/useOficina'

const props = defineProps<{ construcaoId: number }>()
const emit = defineEmits<{ (e: 'fechar'): void; (e: 'atualizado'): void }>()

const a = useAlocacoes()
const cidadaoSel = ref<number | null>(null)
const profissaoSel = ref<string | null>(null)

onMounted(() => a.carregar(props.construcaoId))
watch(() => props.construcaoId, (id) => a.carregar(id))

const nome = computed(() => rotulo({ ...ROTULOS_TIPO, ...ROTULOS_CONSTRUCAO }, a.predio.value?.tipo))
const opcoesCidadao = computed(() =>
  a.disponiveis.value.map((c) => ({ label: `${c.nome} (${c.idadeAnos} anos)`, value: c.id })),
)
const opcoesProfissao = computed(() =>
  a.profissoesPermitidas.value.map((p) => ({ label: rotulo(ROTULOS_PROFISSAO, p), value: p })),
)
const precisaEscolherProfissao = computed(() => opcoesProfissao.value.length > 1)
const emObraAgora = computed(() => a.predio.value?.estado === 'EM_OBRA' || a.predio.value?.estado === 'EM_UPGRADE')
const podeAlocar = computed(
  () =>
    cidadaoSel.value != null &&
    a.vagasLivres.value > 0 &&
    (!precisaEscolherProfissao.value || !!profissaoSel.value) &&
    !a.enviando.value,
)

function formatarEf(v: number) {
  return v.toFixed(2).replace('.', ',')
}

async function alocar() {
  if (cidadaoSel.value == null) return
  const prof = precisaEscolherProfissao.value ? profissaoSel.value : (opcoesProfissao.value[0]?.value ?? null)
  if (await a.alocar(props.construcaoId, cidadaoSel.value, prof)) {
    cidadaoSel.value = null
    profissaoSel.value = null
    emit('atualizado')
  }
}

async function desalocar(cidadaoId: number) {
  if (await a.desalocar(props.construcaoId, cidadaoId)) emit('atualizado')
}
</script>

<template>
  <div class="painel-predio" data-testid="PainelPredio">
    <p v-if="a.carregando.value" data-testid="carregando">Carregando...</p>
    <template v-else-if="a.predio.value">
      <h3>{{ nome }}</h3>
      <p data-testid="resumo">
        {{ a.predio.value.nivel }} · posição ({{ a.predio.value.x }}, {{ a.predio.value.y }}) ·
        {{ rotulo(ROTULOS_ESTADO, a.predio.value.estado) }}
        <span v-if="emObraAgora" data-testid="progresso">
          · obra {{ a.predio.value.poAtual }}/{{ a.predio.value.poTotal }} PO
        </span>
      </p>
      <p data-testid="vagas">Vagas: {{ a.alocados.value.length }}/{{ a.limite.value }} ocupadas</p>
      <p v-if="emObraAgora" class="dica" data-testid="aviso-obra">
        Em obra, só Construtor ou Carregador podem trabalhar.
      </p>

      <h4>Trabalhadores</h4>
      <p v-if="!a.alocados.value.length" data-testid="sem-alocados">Nenhum trabalhador alocado.</p>
      <ul v-else class="lista" data-testid="alocados">
        <li v-for="t in a.alocados.value" :key="t.cidadaoId" :data-testid="`alocado-${t.cidadaoId}`">
          <router-link :to="`/jogo/cidadao/${t.cidadaoId}`" data-testid="link-cidadao">{{ t.nome }}</router-link>
          <span>{{ t.idadeAnos }} anos · {{ rotulo(ROTULOS_PROFISSAO, t.profissao) }} · eficiência {{ formatarEf(t.eficiencia) }}</span>
          <Button
            label="Desalocar"
            size="small"
            severity="secondary"
            :data-testid="`desalocar-${t.cidadaoId}`"
            :disabled="a.enviando.value"
            @click="desalocar(t.cidadaoId)"
          />
        </li>
      </ul>

      <h4>Alocar cidadão</h4>
      <p v-if="a.vagasLivres.value === 0" data-testid="sem-vagas">Sem vagas livres.</p>
      <p v-else-if="!opcoesCidadao.length" data-testid="sem-disponiveis">Nenhum cidadão disponível.</p>
      <div v-else class="alocar">
        <Select
          v-model="cidadaoSel"
          :options="opcoesCidadao"
          option-label="label"
          option-value="value"
          placeholder="Cidadão"
          data-testid="select-cidadao"
        />
        <Select
          v-if="precisaEscolherProfissao"
          v-model="profissaoSel"
          :options="opcoesProfissao"
          option-label="label"
          option-value="value"
          placeholder="Profissão"
          data-testid="select-profissao"
        />
        <Button label="Alocar" data-testid="alocar" :disabled="!podeAlocar" :loading="a.enviando.value" @click="alocar" />
      </div>
    </template>
    <p v-if="a.erro.value" class="erro" role="alert" data-testid="erro-alocacao">{{ a.erro.value }}</p>
    <div class="acoes">
      <router-link
        v-if="a.predio.value && ehOficina(a.predio.value.tipo) && a.predio.value.estado === 'ATIVA'"
        :to="{ name: 'oficina', params: { id: props.construcaoId } }"
        data-testid="abrir-oficina"
      >
        <Button label="Abrir oficina" as="span" />
      </router-link>
      <router-link
        v-if="a.predio.value && a.predio.value.tipo === 'QUARTEL' && a.predio.value.estado === 'ATIVA'"
        :to="{ name: 'quartel', params: { id: props.construcaoId } }"
        data-testid="abrir-quartel"
      >
        <Button label="Abrir quartel" as="span" />
      </router-link>
      <Button label="Fechar" severity="secondary" data-testid="fechar-PainelPredio" @click="emit('fechar')" />
    </div>
  </div>
</template>

<style scoped>
.erro { color: var(--p-red-500, #dc2626); }
.lista { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 0.4rem; }
.lista li { display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap; }
.alocar, .acoes { display: flex; gap: 0.75rem; margin-top: 0.5rem; flex-wrap: wrap; }
.dica { font-size: 0.9em; opacity: 0.8; }
</style>
