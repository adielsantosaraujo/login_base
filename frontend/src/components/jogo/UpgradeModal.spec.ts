import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { get, post } from '../../api/http'
import UpgradeModal from './UpgradeModal.vue'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

function preparar(madeira: number) {
  getMock.mockImplementation(async (url: string) => {
    if (url.endsWith('/catalogo')) return [{ tipo: 'CASA', custoN1: { MADEIRA: 10 }, poN1: 4 }]
    if (url.endsWith('/estoque')) return { recursos: [{ recurso: 'MADEIRA', quantidade: madeira }] }
    return { id: 3, tipo: 'CASA', nivel: 'N1', x: 1, y: 1, tamanho: 1, estado: 'ATIVA' }
  })
}

async function montar() {
  const w = mount(UpgradeModal, { props: { construcaoId: 3 }, global: { plugins: [PrimeVue] } })
  await flushPromises()
  return w
}

describe('UpgradeModal', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
  })

  it('mostra nível atual, próximo, custo e novo tamanho', async () => {
    preparar(100)
    const w = await montar()
    expect(w.get('[data-testid="atual"]').text()).toContain('N1')
    expect(w.get('[data-testid="proximo"]').text()).toContain('N2')
    expect(w.get('[data-testid="proximo"]').text()).toContain('2x2')
    expect(w.get('[data-testid="custo-MADEIRA"]').text()).toContain('25')
    expect(w.get('[data-testid="confirmar-upgrade"]').attributes('disabled')).toBeUndefined()
  })

  it('bloqueia com recursos insuficientes', async () => {
    preparar(5)
    const w = await montar()
    expect(w.get('[data-testid="confirmar-upgrade"]').attributes('disabled')).toBeDefined()
    expect(w.get('[data-testid="motivo"]').text()).toContain('Recursos insuficientes')
  })

  it('confirma com POST e emite atualizado', async () => {
    preparar(100)
    postMock.mockResolvedValue({ id: 3 })
    const w = await montar()
    await w.get('[data-testid="confirmar-upgrade"]').trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/construcoes/3/upgrade', { novoNivel: 'N2', novaX: 1, novaY: 1 })
    expect(w.emitted('atualizado')).toBeTruthy()
  })

  it('exibe erro do backend', async () => {
    preparar(100)
    postMock.mockRejectedValue(new Error('Ladrilho ocupado'))
    const w = await montar()
    await w.get('[data-testid="confirmar-upgrade"]').trigger('click')
    await flushPromises()
    expect(w.get('[data-testid="erro-upgrade"]').text()).toBe('Ladrilho ocupado')
    expect(w.emitted('atualizado')).toBeFalsy()
  })
})
