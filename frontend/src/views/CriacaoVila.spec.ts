import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))
const push = vi.fn()
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))

import { get, post } from '../api/http'
import CriacaoVila from './CriacaoVila.vue'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

async function montar() {
  const w = mount(CriacaoVila, { global: { plugins: [PrimeVue] } })
  await flushPromises()
  return w
}

describe('CriacaoVila', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    push.mockReset()
    getMock.mockResolvedValue({
      semente: 7,
      regioes: [{ indice: 1, jazidas: { FLORESTA: 3, ROCHA: 1 } }],
    })
  })

  it('mostra a prévia de jazidas e seleciona região', async () => {
    const w = await montar()
    expect(w.find('h1').text()).toBe('Criar minha vila')
    expect(w.get('[data-testid="regiao-1"]').text()).toContain('Floresta 3')
    await w.get('[data-testid="regiao-1"]').trigger('click')
    expect(w.get('[data-testid="regiao-1"]').classes()).toContain('selecionada')
  })

  it('recusa região não adjacente', async () => {
    const w = await montar()
    await w.get('[data-testid="regiao-1"]').trigger('click')
    await w.get('[data-testid="regiao-3"]').trigger('click')
    expect(w.get('[data-testid="regiao-3"]').classes()).not.toContain('selecionada')
    expect(w.find('[data-testid="aviso-selecao"]').exists()).toBe(true)
  })

  it('habilita o botão e envia, redirecionando à população', async () => {
    postMock.mockResolvedValue({})
    const w = await montar()
    const botao = () => w.get('[data-testid="criar"]')
    expect(botao().attributes('disabled')).toBeDefined()
    for (const i of [1, 2, 6]) await w.get(`[data-testid="regiao-${i}"]`).trigger('click')
    expect(botao().attributes('disabled')).toBeDefined()
    await w.get('[data-testid="tipo-1"]').setValue('URBANA')
    expect(botao().attributes('disabled')).toBeUndefined()
    await botao().trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/vila', expect.objectContaining({
      regioesEscolhidas: [1, 2, 6], semente: 7,
    }))
    expect(push).toHaveBeenCalledWith('/jogo/populacao')
  })
})
