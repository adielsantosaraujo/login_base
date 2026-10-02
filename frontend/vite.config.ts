/// <reference types="vitest/config" />
import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

const usePolling = process.env.VITE_USE_POLLING === 'true'
const backend = process.env.VITE_BACKEND_URL ?? 'http://localhost:8080'

// changeOrigin: false preserva o Host original (5173), para que o redirect pós-login volte ao frontend
const proxy = { target: backend, changeOrigin: false }

// https://vite.dev/config/
export default defineConfig(({ command }) => ({
  // Em producao os assets sao servidos pelo backend em /app/; no dev server a base continua '/'
  base: command === 'build' ? '/app/' : '/',
  plugins: [vue()],
  server: {
    watch: {
      usePolling,
    },
    proxy: {
      '/api': proxy,
      '/login': proxy,
      '/logout': proxy,
      '/css': proxy,
    },
  },
  test: {
    environment: 'jsdom',
    include: ['src/**/*.spec.ts'],
  },
}))
