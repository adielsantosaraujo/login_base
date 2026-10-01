<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import { ROTULOS_JAZIDA, ROTULOS_TIPO, rotulo, type TipoRegiao } from '../composables/useMapa'
import { useCriacaoVila } from '../composables/useVila'

const router = useRouter()
const {
  previa, selecao, tipos, carregando, enviando, erro, avisoSelecao, completo,
  carregarPrevia, alternar, definirTipo, criar,
} = useCriacaoVila()

onMounted(() => carregarPrevia())

const INDICES = Array.from({ length: 16 }, (_, i) => i + 1)

function jazidas(indice: number): [string, number][] {
  const r = previa.value?.regioes.find((x) => x.indice === indice)
  return r ? Object.entries(r.jazidas).filter(([, n]) => n > 0) : []
}

function aoMudarTipo(indice: number, ev: Event) {
  definirTipo(indice, (ev.target as HTMLSelectElement).value as TipoRegiao)
}

async function enviar() {
  if (await criar()) router.push('/jogo/populacao')
}
</script>

<template>
  <section class="criacao-vila">
    <h1>Criar minha vila</h1>
    <p>Escolha 3 regiões vizinhas (ao menos 1 Urbana) para começar.</p>

    <p v-if="erro" role="alert" class="erro" data-testid="erro">{{ erro }}</p>
    <p v-if="avisoSelecao" role="alert" class="aviso" data-testid="aviso-selecao">{{ avisoSelecao }}</p>
    <p v-if="carregando">Carregando prévia...</p>

    <div class="grade-criacao" data-testid="grade-criacao">
      <button
        v-for="i in INDICES"
        :key="i"
        type="button"
        class="celula"
        :class="{ selecionada: selecao.includes(i) }"
        :data-testid="`regiao-${i}`"
        :aria-pressed="selecao.includes(i)"
        :aria-label="`Região ${i}`"
        @click="alternar(i)"
      >
        <span class="numero">{{ i }}</span>
        <span v-for="[nome, n] in jazidas(i)" :key="nome" class="jazida" :class="`jazida-${nome.toLowerCase()}`">
          {{ rotulo(ROTULOS_JAZIDA, nome) }} {{ n }}
        </span>
      </button>
    </div>

    <ul class="tipos" data-testid="tipos">
      <li v-for="i in selecao" :key="i">
        <label>
          Região {{ i }}
          <select :value="tipos[i]" :data-testid="`tipo-${i}`" @change="aoMudarTipo(i, $event)">
            <option v-for="(r, k) in ROTULOS_TIPO" :key="k" :value="k">{{ r }}</option>
          </select>
        </label>
      </li>
    </ul>

    <Button
      label="Criar vila"
      data-testid="criar"
      :disabled="!completo || enviando"
      :loading="enviando"
      @click="enviar"
    />
  </section>
</template>

<style scoped>
.grade-criacao { display: grid; grid-template-columns: repeat(4, 6.5rem); gap: 0.5rem; margin: 1rem 0; }
.celula {
  min-height: 6.5rem;
  border: 2px solid #d1d5db;
  border-radius: 6px;
  background: #f3f4f6;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.1rem;
  padding: 0.25rem;
}
.celula.selecionada { border-color: #2563eb; background: #dbeafe; }
.numero { font-size: 1.25rem; font-weight: 700; }
.jazida { font-size: 0.7rem; }
.jazida-floresta, .jazida-campo { color: #15803d; }
.jazida-rocha, .jazida-barreiro { color: #78350f; }
.tipos { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 0.5rem; margin-bottom: 1rem; }
.erro { color: #b91c1c; }
.aviso { color: #b45309; }
</style>
