<script setup lang="ts">
import type { RodadaDTO } from '../../composables/useBatalhas'

defineProps<{ rodadas: RodadaDTO[] }>()
</script>

<template>
  <div data-testid="RodadaReplay">
    <p v-if="!rodadas.length" data-testid="sem-rodadas">Nenhuma rodada registrada.</p>
    <section v-for="r in rodadas" :key="r.numero" :data-testid="`rodada-${r.numero}`" class="rodada">
      <h4>Rodada {{ r.numero }}</h4>
      <ul>
        <li v-for="(a, i) in r.acoes" :key="i" :data-testid="`acao-${r.numero}-${i}`"
          :class="['acao', a.atacante.lado === 'TROPA' ? 'aliado' : 'inimigo']">
          <span>{{ a.atacante.nome }} → {{ a.alvo.nome }}: {{ a.dano }}</span>
          <strong v-if="a.critico" class="critico" data-testid="critico"> (crítico!)</strong>
          <span class="pv"> · PV {{ a.pvAntes }} → {{ a.pvDepois }}</span>
          <strong v-if="a.abatido" class="abatido" data-testid="abatido"> · abatido</strong>
        </li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.rodada ul { list-style: none; padding: 0; margin: 0 0 1rem; }
.acao { padding: 0.15rem 0; overflow-wrap: anywhere; }
.critico, .abatido { color: var(--vl-error); }
.pv { opacity: 0.8; }
.pv { font-family: var(--vl-font-mono); }
</style>
