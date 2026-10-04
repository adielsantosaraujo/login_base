import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import { Vilarejo } from './theme/vilarejo'
import App from './App.vue'
import router from './router'
import 'primeicons/primeicons.css'
import './styles/tokens.css'

document.documentElement.classList.add('vl-escuro')

createApp(App)
  .use(router)
  .use(PrimeVue, {
    theme: {
      preset: Vilarejo,
      options: { darkModeSelector: '.vl-escuro' },
    },
    license: import.meta.env.VITE_PRIMEUI_LICENSE,
  })
  .mount('#app')
