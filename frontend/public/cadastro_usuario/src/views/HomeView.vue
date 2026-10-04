<script setup lang="ts">
import Card from 'primevue/card'
</script>

<template>
  <main class="app-shell">
    <Card>
      <template #title>Seja bem-vindo</template>
    </Card>
  </main>
</template>

<style scoped>
.app-shell {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}
</style>
