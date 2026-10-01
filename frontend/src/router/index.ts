import { createRouter, createWebHistory } from 'vue-router'
import { guardaVila } from './guardaVila'
import HomeView from '../views/HomeView.vue'
import Jogo from '../views/Jogo.vue'
import CriacaoVila from '../views/CriacaoVila.vue'
import Mapa from '../views/Mapa.vue'
import JogoEstoque from '../views/JogoEstoque.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    {
      path: '/jogo',
      component: Jogo,
      redirect: '/jogo/mapa',
      children: [
        { path: 'criar-vila', name: 'criar-vila', component: CriacaoVila },
        { path: 'mapa', name: 'mapa', component: Mapa },
        { path: 'estoque', name: 'estoque', component: JogoEstoque },
      ],
    },
  ],
})

router.beforeEach(guardaVila)

export default router
