import { createRouter, createWebHistory } from 'vue-router'
import { guardaVila } from './guardaVila'
import HomeView from '../views/HomeView.vue'
import Jogo from '../views/Jogo.vue'
import CriacaoVila from '../views/CriacaoVila.vue'
import Mapa from '../views/Mapa.vue'
import JogoEstoque from '../views/JogoEstoque.vue'
import DistribuicaoPopulacao from '../views/DistribuicaoPopulacao.vue'
import JogoFamilias from '../views/JogoFamilias.vue'
import PainelCidadao from '../views/PainelCidadao.vue'
import RegiaoVila from '../views/RegiaoVila.vue'
import JogoMercado from '../views/JogoMercado.vue'
import OficinaTela from '../views/OficinaTela.vue'
import JogoInventario from '../views/JogoInventario.vue'

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
        { path: 'populacao', name: 'populacao', component: DistribuicaoPopulacao },
        { path: 'familias', name: 'familias', component: JogoFamilias },
        { path: 'cidadao/:id', name: 'cidadao', component: PainelCidadao },
        { path: 'regiao/:indice', name: 'regiao', component: RegiaoVila },
        { path: 'mercado', name: 'mercado', component: JogoMercado },
        { path: 'oficina/:id', name: 'oficina', component: OficinaTela },
        { path: 'inventario', name: 'inventario', component: JogoInventario },
      ],
    },
  ],
})

router.beforeEach(guardaVila)

export default router
