import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

const usePolling = process.env.VITE_USE_POLLING === 'true'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    watch: {
      usePolling,
    },
  },
})
