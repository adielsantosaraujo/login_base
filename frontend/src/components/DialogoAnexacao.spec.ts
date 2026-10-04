import { describe, expect, it, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'

const { getMock, postMock } = vi.hoisted(() => ({ getMock: vi.fn(), postMock: vi.fn() }))
vi.mock('../api/http', () => ({ get: getMock, post: postMock }))

import DialogoAnexacao from './DialogoAnexacao.vue'

const regiao = {
  indice: 2, tipo: 'MONTANHA' as const, possuida: false, masmorraAtiva: false, nivelMasmorra: null, masmorraId: null,
  terrenos: [{ terreno: 'ROCHA' as const, posicao: 1, percentual: 30 }, { terreno: 'FERRO' as const, posicao: 2, percentual: 20 }],
}

function montar() {
  return mount(DialogoAnexacao, {
    props: { visivel: true, indice: 2, regiao },
    global: { plugins: [PrimeVue], stubs: { teleport: true } },
    attachTo: document.body,
  })
}

describe('DialogoAnexacao', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    getMock.mockImplementation(async (url: string) => {
      if (url.includes('custo-anexacao')) return { ouro: 10, madeira: 5, pedra: 2 }
      return { recursos: [
        { recurso: 'OURO', quantidade: 100 }, { recurso: 'MADEIRA', quantidade: 100 }, { recurso: 'PEDRA', quantidade: 100 },
      ] }
    })
  })

  it('exibe tipo e terrenos e não tem seletor de tipo', async () => {
    const w = montar()
    await flushPromises()
    const doc = document.body
    expect(doc.querySelector('[data-testid="regiao-tipo"]')?.textContent).toContain('Montanha')
    expect(doc.querySelector('[data-testid="terreno-ROCHA"]')?.textContent).toContain('30%')
    expect(doc.querySelector('[data-testid="terreno-FERRO"]')).not.toBeNull()
    expect(doc.querySelector('[data-testid="tipo-anexacao"]')).toBeNull()
    w.unmount()
  })

  it('Anexar envia POST sem corpo', async () => {
    postMock.mockResolvedValue({ regiao: { indice: 2, tipo: 'MONTANHA', possuida: true }, estoque: {}, custo: {} })
    const w = montar()
    await flushPromises()
    ;(document.body.querySelector('[data-testid="confirmar-anexacao"]') as HTMLElement).click()
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/regioes/2/anexar')
    expect(w.emitted('anexada')?.[0]).toEqual([2])
    w.unmount()
  })
})
