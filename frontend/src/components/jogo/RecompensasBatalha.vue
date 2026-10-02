<script setup lang="ts">
import { computed } from 'vue'
import type { RecompensasBatalhaDTO } from '../../composables/useBatalhas'
import { rotuloBonus, rotuloQualidade, rotuloRecurso, rotuloSubtipo } from '../../composables/useItens'

const props = defineProps<{ recompensas: RecompensasBatalhaDTO | null }>()
const recursos = computed(() => Object.entries(props.recompensas?.recursos ?? {}))
const pedras = computed(() => props.recompensas?.pedras ?? [])
const qtdGuerreiros = computed(() => props.recompensas?.guerreirosXp?.length ?? 0)

const MAGNITUDES: Record<string, string> = { BAIXA: 'baixa', MEDIA: 'média', ALTA: 'alta' }
const rotuloMagnitude = (m: string) => MAGNITUDES[m] ?? m.toLowerCase()
</script>

<template>
  <div data-testid="RecompensasBatalha">
    <p v-if="!recompensas" data-testid="sem-recompensas">Sem recompensas</p>
    <ul v-else>
      <li data-testid="recompensa-ouro">Ouro: {{ recompensas.ouro }}</li>
      <li v-if="recursos.length" data-testid="recompensa-recursos">
        Recursos:
        <span v-for="[k, v] in recursos" :key="k" :data-testid="`recurso-${k}`" class="mr-2">{{ rotuloRecurso(k) }} x{{ v }}</span>
      </li>
      <li v-if="recompensas.item" data-testid="recompensa-item">
        Item: {{ rotuloSubtipo(recompensas.item.subtipo) }} ({{ rotuloQualidade(recompensas.item.qualidade) }}, nível {{ recompensas.item.nivel }})
        <span v-if="recompensas.item.bonus.length" data-testid="item-bonus">
          —
          <span v-for="(b, i) in recompensas.item.bonus" :key="i">{{ i ? ', ' : '' }}{{ rotuloBonus(b.codigo) }} +{{ b.valor }}</span>
        </span>
      </li>
      <li data-testid="recompensa-xp">XP: {{ recompensas.xpPorGuerreiro }} por guerreiro ({{ qtdGuerreiros }} guerreiros)</li>
      <li v-for="p in pedras" :key="p.pedraId" data-testid="recompensa-pedra">
        Pedra {{ rotuloQualidade(p.qualidade) }}:
        <span v-for="(b, i) in p.bonus" :key="i">{{ i ? ', ' : '' }}{{ rotuloBonus(b.codigo) }} +{{ b.valor }} ({{ rotuloMagnitude(b.magnitude) }})</span>
      </li>
    </ul>
  </div>
</template>
