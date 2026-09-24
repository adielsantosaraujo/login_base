import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'
import App from './App.vue'

createApp(App)
  .use(PrimeVue, {
    theme: {
      preset: Aura,
    },
    license: import.meta.env.VITE_PRIMEUI_LICENSE,
  })
  .mount('#app')
