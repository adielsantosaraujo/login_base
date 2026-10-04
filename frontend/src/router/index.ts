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
import QuartelTela from '../views/QuartelTela.vue'
import JogoInventario from '../views/JogoInventario.vue'
import JogoBatalhas from '../views/JogoBatalhas.vue'
import BatalhaDetalhe from '../views/BatalhaDetalhe.vue'

const router = createRouter({
  // No build a SPA é servida em /app/ (BASE_URL); no dev server, em /
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // O backend entrega a SPA em /app/index, por isso '/index' é alias da home
    { path: '/', name: 'home', component: HomeView, alias: '/index' },
    {
      path: '/jogo',
      component: Jogo,
      redirect: '/jogo/mapa',
      children: [
        { path: 'criar-vila', name: 'criar-vila', component: CriacaoVila, meta: { etapaInicial: true, abaAtiva: 'mapa' } },
        {
          path: 'distribuir-populacao',
          name: 'distribuir-populacao',
          component: DistribuicaoPopulacao,
          meta: { etapaInicial: true, abaAtiva: 'familias' },
        },
        { path: 'populacao', redirect: '/jogo/distribuir-populacao' },
        { path: 'mapa', name: 'mapa', component: Mapa, meta: { abaAtiva: 'mapa' } },
        { path: 'estoque', name: 'estoque', component: JogoEstoque, meta: { abaAtiva: 'estoque' } },
        { path: 'familias', name: 'familias', component: JogoFamilias, meta: { abaAtiva: 'familias' } },
        { path: 'cidadao/:id', name: 'cidadao', component: PainelCidadao, meta: { abaAtiva: 'familias' } },
        { path: 'regiao/:indice', name: 'regiao', component: RegiaoVila, meta: { abaAtiva: 'mapa' } },
        { path: 'mercado', name: 'mercado', component: JogoMercado, meta: { abaAtiva: 'mercado' } },
        { path: 'oficina/:id', name: 'oficina', component: OficinaTela, meta: { abaAtiva: 'inventario' } },
        { path: 'quartel/:id', name: 'quartel', component: QuartelTela, meta: { abaAtiva: 'batalhas' } },
        { path: 'inventario', name: 'inventario', component: JogoInventario, meta: { abaAtiva: 'inventario' } },
        { path: 'batalhas', name: 'batalhas', component: JogoBatalhas, meta: { abaAtiva: 'batalhas' } },
        { path: 'batalhas/:id', name: 'batalha', component: BatalhaDetalhe, meta: { abaAtiva: 'batalhas' } },
      ],
    },
  ],
})

router.beforeEach(guardaVila)

export default router
