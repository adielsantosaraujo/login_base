<script setup lang="ts">
import Button from 'primevue/button'
import ExpedicaoStatus from './ExpedicaoStatus.vue'
import {
  formatarXp, ROTULOS_ESTADO_TROPA, ROTULOS_POSICAO, type TropaDTO,
} from '../../composables/useQuartel'

defineProps<{ tropas: TropaDTO[]; enviando?: boolean }>()
const emit = defineEmits<{
  (e: 'adicionar', tropaId: number): void
  (e: 'remover', tropaId: number, cidadaoId: number): void
  (e: 'desfazer', tropaId: number): void
  (e: 'expedicao', tropaId: number): void
}>()
</script>

<template>
  <div data-testid="TropasLista">
    <p v-if="!tropas.length" data-testid="sem-tropas">Nenhuma tropa formada.</p>
    <ul v-else class="tropas">
      <li v-for="t in tropas" :key="t.id" :data-testid="`tropa-${t.id}`" class="tropa">
        <h4>
          {{ t.nome }}
          <span :data-testid="`estado-tropa-${t.id}`" class="estado">{{ ROTULOS_ESTADO_TROPA[t.estado] ?? t.estado }}</span>
        </h4>
        <ExpedicaoStatus v-if="t.estado !== 'AQUARTELADA'" :tropa="t" />
        <p :data-testid="`total-${t.id}`">{{ t.totalMembros }} membro(s)</p>
        <p v-if="!t.membros.length" :data-testid="`sem-membros-${t.id}`">Sem membros.</p>
        <table v-else class="membros">
          <caption class="sr-only">Membros de {{ t.nome }}</caption>
          <thead>
            <tr><th>Nome</th><th>Posição</th><th>Estado</th><th>XP</th><th>PE Guerreiro</th><th></th></tr>
          </thead>
          <tbody>
            <tr v-for="m in t.membros" :key="m.cidadaoId" :data-testid="`membro-${t.id}-${m.cidadaoId}`"
              :class="{ ferido: m.estado === 'FERIDO' }">
              <td><router-link :to="`/jogo/cidadao/${m.cidadaoId}`">{{ m.nome }}</router-link></td>
              <td>{{ ROTULOS_POSICAO[m.posicao] ?? m.posicao }}</td>
              <td :data-testid="`estado-membro-${m.cidadaoId}`">
                <strong v-if="m.estado === 'FERIDO'" class="badge-ferido">Ferido</strong>
                <template v-else>Saudável</template>
              </td>
              <td>{{ formatarXp(m.xpGuerreiro) }}</td>
              <td>{{ m.peGuerreiro }}</td>
              <td>
                <Button v-if="t.estado === 'AQUARTELADA'" label="Remover" size="small" severity="secondary"
                  :data-testid="`remover-${t.id}-${m.cidadaoId}`" :disabled="enviando"
                  @click="emit('remover', t.id, m.cidadaoId)" />
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="t.estado === 'AQUARTELADA'" class="acoes">
          <Button label="Adicionar membro" size="small" :data-testid="`adicionar-${t.id}`" :disabled="enviando"
            @click="emit('adicionar', t.id)" />
          <Button label="Desfazer tropa" size="small" severity="danger" :data-testid="`desfazer-${t.id}`"
            :disabled="enviando" @click="emit('desfazer', t.id)" />
          <Button label="Enviar expedição" size="small" :data-testid="`expedicao-${t.id}`" :disabled="enviando"
            @click="emit('expedicao', t.id)" />
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.tropas { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 1rem; }
.tropa { border: 1px solid var(--vl-border); border-radius: var(--vl-radius-chip); padding: 0.75rem; }
.estado { font-size: 0.8em; font-weight: normal; margin-left: 0.5rem; opacity: 0.8; }
.membros { width: 100%; border-collapse: collapse; display: block; overflow-x: auto; }
.membros th, .membros td { text-align: left; padding: 0.3rem 0.6rem; }
.ferido { background: var(--vl-accent-bg); }
.badge-ferido { color: var(--vl-error); }
.acoes { display: flex; gap: 0.5rem; flex-wrap: wrap; margin-top: 0.5rem; }
.sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); }
.tropa h3, .tropa h4 { font-family: var(--vl-font-display); }
.membros td, .estado { font-family: var(--vl-font-mono); }
</style>
