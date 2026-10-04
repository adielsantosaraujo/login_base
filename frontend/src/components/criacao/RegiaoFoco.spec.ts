import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import RegiaoFoco from './RegiaoFoco.vue'

describe('RegiaoFoco', () => {
  it('mostra mensagem quando não há região em foco', () => {
    const w = mount(RegiaoFoco, { props: { regiao: null } })
    expect(w.text()).toContain('Passe o mouse numa região')
  })

  it('mostra título e bônus da região', () => {
    const w = mount(RegiaoFoco, {
      props: {
        regiao: {
          indice: 6,
          tipo: 'URBANA',
          bonus: [
            { bonus: 'INDUSTRIA', posicao: 2, valor: 30 },
            { bonus: 'COMERCIO', posicao: 1, valor: 47 },
          ],
        },
      },
    })
    expect(w.get('[data-testid="foco-titulo"]').text()).toBe('Região 06 · Urbana')
    expect(w.text()).toContain('Comércio')
    expect(w.text()).toContain('47')
  })
})
