import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import RegiaoTile from './RegiaoTile.vue'
import type { RegiaoPrevia } from '../../domain/regioes'

const regiao: RegiaoPrevia = {
  indice: 6,
  tipo: 'URBANA',
  bonus: [
    { bonus: 'DESENVOLVIMENTO', posicao: 3, valor: 12 },
    { bonus: 'COMERCIO', posicao: 1, valor: 47 },
    { bonus: 'INDUSTRIA', posicao: 2, valor: 30 },
  ],
}

function montar(props: { ordem: number; disponivel: boolean }) {
  return mount(RegiaoTile, { props: { regiao, ...props } })
}

describe('RegiaoTile', () => {
  it('tem aria-label detalhado com bônus em ordem decrescente', () => {
    const w = montar({ ordem: 0, disponivel: true })
    expect(w.attributes('aria-label')).toBe(
      'Região 06 · Urbana · Comércio 47, Indústria 30, Desenvolvimento 12',
    )
    expect(w.attributes('aria-pressed')).toBe('false')
  })

  it('emite alternar ao clicar quando disponível', async () => {
    const w = montar({ ordem: 0, disponivel: true })
    await w.trigger('click')
    expect(w.emitted('alternar')).toEqual([[6]])
  })

  it('ignora o clique quando indisponível', async () => {
    const w = montar({ ordem: 0, disponivel: false })
    expect(w.attributes('aria-disabled')).toBe('true')
    await w.trigger('click')
    expect(w.emitted('alternar')).toBeUndefined()
  })

  it('mostra o selo de ordem quando selecionado e permite remover', async () => {
    const w = montar({ ordem: 2, disponivel: false })
    expect(w.attributes('aria-pressed')).toBe('true')
    expect(w.get('[data-testid="selo-ordem"]').text()).toBe('2')
    await w.trigger('click')
    expect(w.emitted('alternar')).toEqual([[6]])
  })

  it('emite foco e desfoco por mouse e teclado', async () => {
    const w = montar({ ordem: 0, disponivel: true })
    await w.trigger('mouseenter')
    await w.trigger('mouseleave')
    await w.trigger('focus')
    await w.trigger('blur')
    expect(w.emitted('foco')).toEqual([[6], [6]])
    expect(w.emitted('desfoco')).toHaveLength(2)
  })
})
