<template>
  <div class="grade-batalha">
    <ConfirmDialog />

    <div class="grade">
      <div
        v-for="(pos, idx) in todasAsPosicoes"
        :key="idx"
        class="celula"
        :class="{
          obstaculo: isObstaculo(pos),
          combatente: !!getCombatente(pos),
          jogador: getCombatente(pos)?.lado === 'JOGADOR',
          inimigo: getCombatente(pos)?.lado === 'INIMIGO',
          selecionado: selecionado?.x === pos.x && selecionado?.y === pos.y,
          alvo: isAlvo(pos),
        }"
        @click="clicar(pos)"
      >
        <span v-if="getCombatente(pos)" class="label">
          {{ getCombatente(pos)?.tipo }}
          <br />
          {{ getCombatente(pos)?.hp }}/{{ getCombatente(pos)?.hpMaximo }}
        </span>
      </div>
    </div>

    <div class="painel-selecionado">
      <div v-if="combatenteSelecionado">
        <h3>{{ combatenteSelecionado.id }}</h3>
        <p>{{ combatenteSelecionado.tipo }} · {{ combatenteSelecionado.lado }}</p>
        <p>HP: {{ combatenteSelecionado.hp }}/{{ combatenteSelecionado.hpMaximo }}</p>
        <p>Ataque: {{ combatenteSelecionado.ataque }} · Alcance: {{ combatenteSelecionado.alcance }}</p>
        <p>Defesa: {{ combatenteSelecionado.defesa }} · Movimento: {{ combatenteSelecionado.movimento }}</p>
        <p v-if="combatenteSelecionado.defendendo">Defendendo</p>
        <template v-if="combatenteSelecionado.lado === 'JOGADOR'">
          <p>Moveu: {{ combatenteSelecionado.moveu ? 'Sim' : 'Não' }}</p>
          <p>Agiu: {{ combatenteSelecionado.agiu ? 'Sim' : 'Não' }}</p>
        </template>
        <p v-if="modoMover" class="dica">Clique em uma casa livre para mover.</p>
        <p v-if="modoAtacar" class="dica">Clique em um inimigo para atacar.</p>
      </div>
      <p v-else class="dica">Selecione um combatente na grade.</p>
    </div>

    <div class="botoes-acao">
      <Button
        v-if="podeAgir"
        label="Mover"
        :disabled="combatenteSelecionado?.moveu"
        @click="modoMover = true; modoAtacar = false"
      />
      <Button
        v-if="podeAgir"
        label="Atacar"
        @click="modoAtacar = true; modoMover = false"
      />
      <Button v-if="podeAgir" label="Defender" @click="defender" />
      <Button label="Encerrar turno" severity="secondary" @click="encerrarTurno" />
      <Button label="Render" severity="danger" @click="confirmarRender" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import Button from 'primevue/button'
import ConfirmDialog from 'primevue/confirmdialog'
import { useConfirm } from 'primevue/useconfirm'
import ExclamationTriangle from '@primeicons/vue/exclamation-triangle'
import type { AcaoCombateRequest, BatalhaDto, CombatenteDto } from '../api/tipos'

const props = defineProps<{ batalha: BatalhaDto }>()
const emit = defineEmits<{
  acao: [acao: AcaoCombateRequest]
}>()

const confirm = useConfirm()

const selecionado = ref<{ x: number; y: number } | null>(null)
const modoMover = ref(false)
const modoAtacar = ref(false)

const todasAsPosicoes = computed(() => {
  const posicoes: { x: number; y: number }[] = []
  for (let y = 0; y < props.batalha.altura; y++) {
    for (let x = 0; x < props.batalha.largura; x++) {
      posicoes.push({ x, y })
    }
  }
  return posicoes
})

const combatenteSelecionado = computed<CombatenteDto | null>(() => {
  if (!selecionado.value) return null
  return (
    props.batalha.combatentes.find(
      (c) => c.x === selecionado.value?.x && c.y === selecionado.value?.y && c.vivo,
    ) ?? null
  )
})

const podeAgir = computed(
  () => combatenteSelecionado.value?.lado === 'JOGADOR' && !combatenteSelecionado.value.agiu,
)

function isObstaculo(pos: { x: number; y: number }): boolean {
  return props.batalha.obstaculos.some((o) => o.x === pos.x && o.y === pos.y)
}

function getCombatente(pos: { x: number; y: number }): CombatenteDto | undefined {
  return props.batalha.combatentes.find((c) => c.x === pos.x && c.y === pos.y && c.vivo)
}

function isAlvo(pos: { x: number; y: number }): boolean {
  return modoAtacar.value && !!getCombatente(pos) && getCombatente(pos)?.lado === 'INIMIGO'
}

function clicar(pos: { x: number; y: number }) {
  if (modoMover.value) {
    if (combatenteSelecionado.value && !getCombatente(pos) && !isObstaculo(pos)) {
      emit('acao', {
        tipo: 'MOVER',
        combatenteId: combatenteSelecionado.value.id,
        x: pos.x,
        y: pos.y,
        turno: props.batalha.turno,
      })
      modoMover.value = false
    }
    return
  }

  if (modoAtacar.value) {
    const alvo = getCombatente(pos)
    if (alvo && alvo.lado === 'INIMIGO' && combatenteSelecionado.value) {
      emit('acao', {
        tipo: 'ATACAR',
        combatenteId: combatenteSelecionado.value.id,
        alvoId: alvo.id,
        turno: props.batalha.turno,
      })
      modoAtacar.value = false
    }
    return
  }

  selecionado.value = pos
}

function defender() {
  if (combatenteSelecionado.value) {
    emit('acao', {
      tipo: 'DEFENDER',
      combatenteId: combatenteSelecionado.value.id,
      turno: props.batalha.turno,
    })
  }
}

function encerrarTurno() {
  emit('acao', {
    tipo: 'ENCERRAR_TURNO',
    combatenteId: 'dummy',
    turno: props.batalha.turno,
  })
}

function confirmarRender() {
  confirm.require({
    message: 'Render encerra a batalha com derrota. Tem certeza?',
    header: 'Confirmar render',
    icon: ExclamationTriangle,
    rejectProps: { label: 'Cancelar', severity: 'secondary', outlined: true },
    acceptProps: { label: 'Render', severity: 'danger' },
    accept: () => {
      emit('acao', {
        tipo: 'RENDER',
        combatenteId: 'dummy',
        turno: props.batalha.turno,
      })
    },
  })
}
</script>

<style scoped>
.grade-batalha {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.grade {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  grid-template-rows: repeat(8, 1fr);
  gap: 2px;
  width: 400px;
  height: 400px;
  background: var(--p-surface-300, #ccc);
  flex-shrink: 0;
}

.celula {
  background: var(--p-surface-0, #fff);
  cursor: pointer;
  border: 1px solid var(--p-surface-200, #eee);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.celula.obstaculo {
  background: #333;
  cursor: not-allowed;
}

.celula.selecionado {
  outline: 3px solid #fbc02d;
  outline-offset: -3px;
}

.celula.combatente.jogador {
  background: #cce5ff;
}

.celula.combatente.inimigo {
  background: #ffcccc;
}

.celula.alvo {
  outline: 3px solid #e53935;
  outline-offset: -3px;
}

.label {
  font-size: 10px;
  font-weight: bold;
  line-height: 1.2;
}

.painel-selecionado {
  width: 220px;
  padding: 16px;
  background: var(--p-content-background, #f5f5f5);
  border-radius: 4px;
}

.dica {
  font-style: italic;
  color: var(--p-text-muted-color, #666);
}

.botoes-acao {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 160px;
}
</style>
