<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import Menubar from 'primevue/menubar'
import Toast from 'primevue/toast'
import type { MenuItem } from 'primevue/menuitem'
import Home from '@primeicons/vue/home'
import Warehouse from '@primeicons/vue/warehouse'
import Hammer from '@primeicons/vue/hammer'
import Shield from '@primeicons/vue/shield'
import MapIcon from '@primeicons/vue/map'
import SignOut from '@primeicons/vue/sign-out'
import { useVila } from './composables/useVila'
import { lerTokenCsrf } from './api/http'
import PainelRecursos from './components/PainelRecursos.vue'

const router = useRouter()
const { vila, carregar, iniciarPoll, pararPoll } = useVila()

// O logout continua sendo processado pelo Spring Security (`/logout`) via
// formulário, exatamente como o login Thymeleaf, por isso reaproveita o
// token CSRF lido do cookie `XSRF-TOKEN` no parâmetro `_csrf`.
function sair(): void {
  const token = lerTokenCsrf()
  const form = document.createElement('form')
  form.method = 'POST'
  form.action = '/logout'
  if (token) {
    const campo = document.createElement('input')
    campo.type = 'hidden'
    campo.name = '_csrf'
    campo.value = token
    form.appendChild(campo)
  }
  document.body.appendChild(form)
  form.submit()
}

const menu: MenuItem[] = [
  { label: 'Vila', icon: Home, command: () => router.push('/') },
  { label: 'Fazenda', icon: Warehouse, command: () => router.push('/fazenda') },
  { label: 'Forja', icon: Hammer, command: () => router.push('/forja') },
  { label: 'Quartel', icon: Shield, command: () => router.push('/quartel') },
  { label: 'Masmorras', icon: MapIcon, command: () => router.push('/masmorras') },
  { label: 'Sair', icon: SignOut, command: sair },
]

onMounted(() => {
  carregar()
  iniciarPoll()
})

onUnmounted(() => {
  pararPoll()
})
</script>

<template>
  <div class="app-shell">
    <Menubar :model="menu" />
    <PainelRecursos v-if="vila" />
    <main class="app-content">
      <RouterView />
    </main>
    <Toast />
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-content {
  flex: 1;
  padding: 1rem;
}
</style>
