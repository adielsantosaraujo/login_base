import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  // No build a SPA é servida em /app/ (BASE_URL); no dev server, em /
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // O backend entrega a SPA em /app/index, por isso '/index' é alias da home
    { path: '/', name: 'home', component: HomeView, alias: '/index' },
  ],
})

export default router
