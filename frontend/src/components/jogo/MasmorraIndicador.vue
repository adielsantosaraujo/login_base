<script setup lang="ts">
defineProps<{ nivel: number }>()
</script>

<template>
  <span class="masmorra-indicador" role="img" :aria-label="`Masmorra nível ${nivel}`" data-testid="masmorra">
    <i class="pi pi-bolt" aria-hidden="true"></i>
    <span aria-hidden="true">N{{ nivel }}</span>
  </span>
</template>

<style scoped>
.masmorra-indicador {
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
  font-size: 0.7rem;
  font-weight: 700;
  background: #7f1d1d;
  color: #fff;
  border-radius: 999px;
  padding: 0.05rem 0.4rem;
}
</style>
