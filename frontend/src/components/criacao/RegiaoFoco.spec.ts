import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import RegiaoFoco from './RegiaoFoco.vue'

describe('RegiaoFoco', () => {
  it('mostra mensagem quando não há região em foco', () => {
    const w = mount(RegiaoFoco, { props: { regiao: null } })
    expect(w.text()).toContain('Passe o mouse numa região')
  })

  it('mostra título e composição da região', () => {
    const w = mount(RegiaoFoco, {
      props: {
        regiao: {
          indice: 6,
          tipo: 'URBANA',
          terrenos: [
            { terreno: 'COMERCIO', posicao: 2, percentual: 30 },
            { terreno: 'INDUSTRIA', posicao: 1, percentual: 45 },
          ],
        },
      },
    })
    expect(w.get('[data-testid="foco-titulo"]').text()).toBe('Região 06 · Urbana')
    expect(w.text()).toContain('Comércio')
    expect(w.get('[data-testid="terreno-INDUSTRIA"]').text()).toContain('45%')
  })
})
