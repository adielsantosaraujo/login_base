<script setup lang="ts">
import { onMounted } from 'vue'
import { ROTULOS_RESULTADO, useBatalhas } from '../composables/useBatalhas'

const b = useBatalhas()
onMounted(() => b.listar())
</script>

<template>
  <section data-testid="JogoBatalhas">
    <h2>Batalhas</h2>
    <p v-if="b.carregando.value" data-testid="carregando">Carregando...</p>
    <p v-else-if="b.erro.value" class="erro" role="alert" data-testid="erro-batalhas">{{ b.erro.value }}</p>
    <p v-else-if="!b.batalhas.value.length" data-testid="sem-batalhas">Nenhuma batalha ainda</p>
    <div v-else class="tabela">
      <table>
        <caption class="sr-only">Histórico de batalhas</caption>
        <thead>
          <tr><th>Turno</th><th>Tropa</th><th>Masmorra</th><th>Resultado</th><th>Rodadas</th><th></th></tr>
        </thead>
        <tbody>
          <tr v-for="x in b.batalhas.value" :key="x.id" :data-testid="`batalha-${x.id}`">
            <td>{{ x.turno }}</td>
            <td>{{ x.tropaNome }}</td>
            <td>Nível {{ x.masmorraNivel }} · Região {{ x.regiaoIndice }}</td>
            <td>
              <strong :data-testid="`resultado-${x.id}`" :class="x.resultado === 'VITORIA' ? 'vitoria' : 'derrota'">
                {{ ROTULOS_RESULTADO[x.resultado] ?? x.resultado }}
              </strong>
            </td>
            <td>{{ x.rodadas }}</td>
            <td><router-link :to="`/jogo/batalhas/${x.id}`" :data-testid="`ver-${x.id}`">Ver</router-link></td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.tabela { overflow-x: auto; }
.vitoria { color: var(--vl-terreno-fl); }
.derrota { color: var(--vl-error); }
.erro { color: var(--vl-error); }
h2, h3 { font-family: var(--vl-font-display); }
</style>
