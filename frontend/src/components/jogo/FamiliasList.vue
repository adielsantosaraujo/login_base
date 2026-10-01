<script setup lang="ts">
import { ref } from 'vue'
import Button from 'primevue/button'
import type { Familia, MembroFamilia } from '../../composables/useFamilias'
import { elegivelParaCasamento } from '../../composables/useFamilias'

defineProps<{ familias: Familia[] }>()
const emit = defineEmits<{ (e: 'casar', membro: MembroFamilia): void }>()

const expandidas = ref<Set<number>>(new Set())

function alternar(id: number) {
  const s = new Set(expandidas.value)
  if (s.has(id)) s.delete(id)
  else s.add(id)
  expandidas.value = s
}
</script>

<template>
  <div class="familias">
    <p v-if="familias.length === 0" data-testid="sem-familias">Nenhuma família na vila.</p>
    <table v-else class="tabela">
      <thead>
        <tr>
          <th></th>
          <th>Sobrenome</th>
          <th>Membros</th>
          <th>Casa</th>
        </tr>
      </thead>
      <tbody>
        <template v-for="f in familias" :key="f.id">
          <tr :data-testid="`familia-${f.id}`">
            <td>
              <Button
                :data-testid="`expandir-${f.id}`"
                :icon="expandidas.has(f.id) ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
                :aria-label="expandidas.has(f.id) ? 'Recolher' : 'Expandir'"
                text
                size="small"
                @click="alternar(f.id)"
              />
            </td>
            <td>{{ f.sobrenome }}</td>
            <td>{{ f.membros.length }}</td>
            <td>{{ f.casaId ? `#${f.casaId}` : 'Sem casa' }}</td>
          </tr>
          <tr v-if="expandidas.has(f.id)" :data-testid="`membros-${f.id}`">
            <td></td>
            <td colspan="3">
              <table class="tabela membros">
                <thead>
                  <tr>
                    <th>Nome</th>
                    <th>Idade</th>
                    <th>Sexo</th>
                    <th>Status</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="m in f.membros" :key="m.id" :data-testid="`membro-${m.id}`">
                    <td>
                      <router-link :to="`/jogo/cidadao/${m.id}`">{{ m.nome }}</router-link>
                    </td>
                    <td>{{ m.idadeAnos }}</td>
                    <td>{{ m.sexo === 'M' ? 'Masculino' : 'Feminino' }}</td>
                    <td>{{ m.estadoCivil === 'CASADO' ? 'Casado(a)' : 'Solteiro(a)' }}</td>
                    <td>
                      <Button
                        :data-testid="`casar-${m.id}`"
                        label="Casar"
                        size="small"
                        :disabled="!elegivelParaCasamento(m)"
                        :title="elegivelParaCasamento(m) ? '' : 'Só solteiros com 18 anos ou mais'"
                        @click="emit('casar', m)"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.tabela {
  width: 100%;
  border-collapse: collapse;
}
.tabela th,
.tabela td {
  text-align: left;
  padding: 0.4rem 0.6rem;
  border-bottom: 1px solid var(--p-content-border-color, #ddd);
}
.membros {
  margin: 0.25rem 0;
}
</style>
