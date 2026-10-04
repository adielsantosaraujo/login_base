import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import BarraValor from './BarraValor.vue'

describe('BarraValor', () => {
  it('calcula largura e expõe progressbar', () => {
    const w = mount(BarraValor, { props: { valor: 5, maximo: 20, cor: 'var(--vl-warn)' } })
    expect(w.attributes('role')).toBe('progressbar')
    expect(w.attributes('aria-valuenow')).toBe('5')
    expect(w.attributes('aria-valuemax')).toBe('20')
    const fill = w.get('.vl-preenchimento')
    expect(fill.attributes('style')).toContain('width: 25%')
    expect(fill.attributes('style')).toContain('var(--vl-warn)')
  })

  it('limita entre 0 e 100 e trata máximo 0', () => {
    expect(mount(BarraValor, { props: { valor: 50, maximo: 10 } }).get('.vl-preenchimento').attributes('style')).toContain('width: 100%')
    expect(mount(BarraValor, { props: { valor: -3, maximo: 10 } }).get('.vl-preenchimento').attributes('style')).toContain('width: 0%')
    expect(mount(BarraValor, { props: { valor: 3, maximo: 0 } }).get('.vl-preenchimento').attributes('style')).toContain('width: 0%')
  })
})
