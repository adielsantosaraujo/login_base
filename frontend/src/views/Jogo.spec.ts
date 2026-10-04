import { mount, flushPromises } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { computed, ref } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import Jogo from './Jogo.vue'

const carregarEventos = vi.fn()
vi.mock('../composables/useTurno', async (orig) => {
  const real = await orig<typeof import('../composables/useTurno')>()
  return {
    ...real,
    useTurno: () => ({
      turno: ref({ numero: 5, iniciadoEm: null, proximoEm: null, segundosRestantes: 90 }),
      segundosRestantes: ref(90),
      eventos: computed(() => []),
      turnoExibido: computed(() => 5),
      carregando: ref(false),
      erro: ref(null),
      erroEventos: ref(null),
      carregarEventos,
      carregarTurnoAnterior: vi.fn(),
      iniciar: vi.fn(),
      parar: vi.fn(),
    }),
  }
})

async function montar(path: string) {
  const vazio = { template: '<div data-testid="filha" />' }
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/jogo',
        component: Jogo,
        children: [
          { path: 'mapa', component: vazio, meta: { abaAtiva: 'mapa' } },
          { path: 'criar-vila', component: vazio, meta: { etapaInicial: true, abaAtiva: 'mapa' } },
        ],
      },
    ],
  })
  await router.push(path)
  await router.isReady()
  const w = mount(
    { template: '<div><RouterView /></div>' },
    { global: { plugins: [router, PrimeVue], stubs: { RelatorioTurno: { template: '<div data-testid="relatorio" />' }, Drawer: { template: '<div v-if="visible" data-testid="drawer"><slot /></div>', props: ['visible'] } } }, attachTo: document.body },
  )
  await flushPromises()
  return w
}

describe('Jogo', () => {
  it('mostra cabeçalho com aba ativa, turno e relatório', async () => {
    const w = await montar('/jogo/mapa')
    expect(w.get('[data-testid="aba-mapa"]').classes()).toContain('ativa')
    expect(w.get('[data-testid="aba-mapa"]').classes()).not.toContain('inativa')
    expect(w.get('[data-testid="pilula-turno"]').text()).toContain('5')
    expect(w.get('[data-testid="pilula-proximo"]').text()).toContain('01:30')
    expect(w.find('.vl-botao-relatorio').exists()).toBe(true)
    expect(w.find('[data-testid="filha"]').exists()).toBe(true)
  })

  it('abre o drawer do relatório ao clicar', async () => {
    const w = await montar('/jogo/mapa')
    expect(w.find('[data-testid="drawer"]').exists()).toBe(false)
    await w.get('.vl-botao-relatorio').trigger('click')
    expect(carregarEventos).toHaveBeenCalled()
    expect(w.find('[data-testid="drawer"]').exists()).toBe(true)
  })

  it('desativa abas e esconde relatório na etapa inicial', async () => {
    const w = await montar('/jogo/criar-vila')
    expect(w.get('[data-testid="aba-mapa"]').classes()).toContain('inativa')
    expect(w.find('.vl-botao-relatorio').exists()).toBe(false)
    expect(w.get('[data-testid="pilula-turno"]').text()).toContain('5')
  })
})
