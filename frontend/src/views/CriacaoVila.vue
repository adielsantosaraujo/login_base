<script setup lang="ts">
import { onMounted } from 'vue'
import AvisoToast from '../components/vilarejo/AvisoToast.vue'
import MapaPrevia from '../components/criacao/MapaPrevia.vue'
import SelecaoPainel from '../components/criacao/SelecaoPainel.vue'
import { useCriacaoVila } from '../composables/useVila'

const {
  previa, selecionadas, gerando, enviando, erro,
  conectado, totaisBonus, dicaSelecao, valida, emFoco,
  carregarPrevia, alternar, gerar, criar, setHover, limparHover,
} = useCriacaoVila()

onMounted(() => carregarPrevia())

function limpar() {
  selecionadas.value = []
}
</script>

<template>
  <main class="cv-tela">
    <header class="cv-intro">
      <p class="cv-sobretitulo">INÍCIO DE JOGO</p>
      <h1 class="cv-titulo">Criar minha vila</h1>
      <p class="cv-texto">
        Escolha 3 regiões vizinhas — ao menos 1 Urbana — para fundar sua vila. Os bônus das regiões escolhidas
        definem o ponto de partida da vila.
      </p>
    </header>

    <div class="cv-linha">
      <section class="cv-mapa" aria-label="Mapa de regiões">
        <MapaPrevia
          v-if="previa"
          :regioes="previa.regioes"
          :selecionadas="selecionadas"
          @alternar="alternar"
          @foco="setHover"
          @desfoco="limparHover"
        />
        <p v-else-if="gerando" class="cv-dica">Carregando mapa...</p>
        <p class="cv-dica" data-testid="dica">{{ dicaSelecao }}</p>
        <div class="cv-gerar">
          <button
            type="button"
            class="cv-botao-gerar"
            data-testid="gerar"
            :disabled="gerando"
            :aria-busy="gerando ? 'true' : undefined"
            @click="gerar"
          >
            <i :class="gerando ? 'pi pi-spin pi-spinner' : 'pi pi-refresh'" aria-hidden="true"></i>
            Gerar novo mapa
          </button>
          <span v-if="previa" class="cv-rodada" data-testid="rodada">Mapa nº {{ previa.rodada }}</span>
        </div>
      </section>

      <div v-if="previa" class="cv-painel">
        <SelecaoPainel
          :selecionadas="selecionadas"
          :regioes="previa.regioes"
          :totais="totaisBonus"
          :conectado="conectado"
          :valida="valida"
          :enviando="enviando"
          :em-foco="emFoco"
          @limpar="limpar"
          @criar="criar"
        />
      </div>
    </div>

    <AvisoToast v-if="erro" tipo="erro" :mensagem="erro" data-testid="erro" />
  </main>
</template>

<style scoped>
.cv-tela {
  max-width: 1180px;
  margin: 0 auto;
  padding: 36px 28px 48px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}
.cv-sobretitulo {
  margin: 0 0 6px;
  font-size: 11px;
  letter-spacing: 0.12em;
  color: var(--vl-accent);
}
.cv-titulo {
  margin: 0 0 8px;
  font-family: var(--vl-font-display);
  font-size: 32px;
}
.cv-texto {
  margin: 0;
  max-width: 640px;
  color: var(--vl-text-2);
}
.cv-linha {
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  align-items: flex-start;
}
.cv-mapa {
  flex: 1 1 560px;
  min-width: 0;
}
.cv-painel {
  flex: 1 1 320px;
  max-width: 400px;
  position: sticky;
  top: 20px;
}
.cv-dica {
  margin: 14px 0;
  text-align: center;
  font-size: 13px;
  color: var(--vl-text-2);
}
.cv-gerar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.cv-botao-gerar {
  height: 42px;
  padding: 0 20px;
  border-radius: var(--vl-radius-pill);
  border: 1px solid var(--vl-border);
  background: var(--vl-surface-3);
  color: var(--vl-text);
  font-family: var(--vl-font-sans);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.cv-botao-gerar:disabled {
  cursor: progress;
  opacity: 0.7;
}
.cv-rodada {
  font-family: var(--vl-font-mono);
  font-size: 11px;
  letter-spacing: 0.08em;
  color: var(--vl-text-3);
}
@media (max-width: 900px) {
  .cv-painel {
    position: static;
    max-width: none;
    flex-basis: 100%;
  }
}
</style>
