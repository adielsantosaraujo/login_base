import { createRouter, createWebHistory } from 'vue-router'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('../views/VilaView.vue') },
    { path: '/fazenda', component: () => import('../views/FazendaView.vue') },
    { path: '/forja', component: () => import('../views/ForjaView.vue') },
    { path: '/quartel', component: () => import('../views/QuartelView.vue') },
    { path: '/quartel/unidades/:id', component: () => import('../views/UnidadeDetalheView.vue') },
    { path: '/masmorras', component: () => import('../views/MasmorrasView.vue') },
    { path: '/batalhas/:id', component: () => import('../views/BatalhaView.vue') },
  ],
})
