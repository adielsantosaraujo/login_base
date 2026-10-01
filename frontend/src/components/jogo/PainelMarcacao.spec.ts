import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../../api/http', () => ({ get: vi.fn(), post: vi.fn(), del: vi.fn() }))

import { del, get, post } from '../../api/http'
import PainelMarcacao from './PainelMarcacao.vue'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)
const delMock = vi.mocked(del)

let marcadas: { id: number; construcaoId: number; x: number; y: number }[]

function montar() {
  return mount(PainelMarcacao, { props: { construcaoId: 7 }, global: { plugins: [PrimeVue] } })
}

describe('PainelMarcacao', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    delMock.mockReset()
    marcadas = [{ id: 1, construcaoId: 7, x: 2, y: 0 }]
    getMock.mockImplementation(async (url: string) => {
      if (url === '/api/jogo/construcoes/7')
        return { id: 7, tipo: 'PEDREIRA', nivel: 'N1', regiaoIndice: 3, x: 0, y: 0, tamanho: 2, estado: 'ATIVA' }
      if (url === '/api/jogo/construcoes/7/marcacoes') return marcadas
      if (url === '/api/jogo/regioes/3')
        return {
          regiao: { id: 3, indice: 3, tipo: 'COLETA', possuida: true },
          ladrilhos: [
            { x: 2, y: 0, jazida: 'ROCHA', construcao: null },
            { x: 3, y: 0, jazida: 'ROCHA', construcao: null },
            { x: 0, y: 2, jazida: 'ROCHA', construcao: null },
          ],
        }
      throw new Error('url inesperada ' + url)
    })
  })

  it('mostra contador e destaca marcados', async () => {
    const w = montar()
    await flushPromises()
    expect(w.get('[data-testid="marcacao-contador"]').text()).toBe('1/4 marcados')
    expect(w.findAll('.destaque')).toHaveLength(1)
  })

  it('marca um ladrilho livre', async () => {
    postMock.mockResolvedValue({ id: 2, construcaoId: 7, x: 3, y: 0 })
    const w = montar()
    await flushPromises()
    await w.get('[data-x="3"][data-y="0"]').trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/construcoes/7/marcacoes', { x: 3, y: 0 })
    expect(w.get('[data-testid="marcacao-contador"]').text()).toBe('2/4 marcados')
    expect(w.emitted('atualizado')).toBeTruthy()
  })

  it('desmarca um ladrilho marcado', async () => {
    delMock.mockImplementation(async () => {
      marcadas = []
    })
    const w = montar()
    await flushPromises()
    await w.get('[data-x="2"][data-y="0"]').trigger('click')
    await flushPromises()
    expect(delMock).toHaveBeenCalledWith('/api/jogo/construcoes/7/marcacoes/2/0')
    expect(w.get('[data-testid="marcacao-contador"]').text()).toBe('0/4 marcados')
  })

  it('exibe o erro do backend', async () => {
    postMock.mockRejectedValue(new Error('Ladrilho sem conexão ortogonal com o prédio'))
    const w = montar()
    await flushPromises()
    await w.get('[data-x="0"][data-y="2"]').trigger('click')
    await flushPromises()
    expect(w.get('[data-testid="marcacao-erro"]').text()).toContain('sem conexão')
    expect(w.emitted('atualizado')).toBeFalsy()
  })

  it('fecha o painel', async () => {
    const w = montar()
    await flushPromises()
    await w.get('[data-testid="fechar-PainelMarcacao"]').trigger('click')
    expect(w.emitted('fechar')).toBeTruthy()
  })
})
