/// <reference types="vitest/config" />
import { basename } from 'node:path'
import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// Nome do app: define o prefixo /<nome>/ dos assets em producao (no build, vem de FRONT_APP_NOME)
const nomeApp = process.env.FRONT_APP_NOME ?? basename(import.meta.dirname)
const usePolling = process.env.VITE_USE_POLLING === 'true'
const backend = process.env.VITE_BACKEND_URL ?? 'http://localhost:8080'

// changeOrigin: false preserva o Host original (5173), para que o redirect pós-login volte ao frontend
const proxy = { target: backend, changeOrigin: false }

// https://vite.dev/config/
export default defineConfig(({ command }) => ({
  // Em producao os assets sao servidos pelo backend em /<nome>/; no dev server a base continua '/'
  base: command === 'build' ? `/${nomeApp}/` : '/',
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
