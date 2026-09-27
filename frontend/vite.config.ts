import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

const usePolling = process.env.VITE_USE_POLLING === 'true'
const backendUrl = process.env.BACKEND_URL ?? 'http://localhost'

// Caminhos servidos pelo backend Spring Boot: API do jogo, login/logout
// Thymeleaf e recursos estáticos públicos. `changeOrigin: false` preserva o
// header `Host: localhost:5173` para que os redirects do Spring Security
// (e o cookie de sessão) continuem apontando para o domínio do Vite.
const proxyBackend = {
  target: backendUrl,
  changeOrigin: false,
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    watch: {
      usePolling,
    },
    proxy: {
      '/api': proxyBackend,
      '/login': proxyBackend,
      '/logout': proxyBackend,
      '/css': proxyBackend,
      '/js': proxyBackend,
      '/images': proxyBackend,
    },
  },
})
