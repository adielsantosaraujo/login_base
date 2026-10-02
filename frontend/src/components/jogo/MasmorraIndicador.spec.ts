import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import MasmorraIndicador from './MasmorraIndicador.vue'

describe('MasmorraIndicador', () => {
  it('exibe ícone, nível e aria-label', () => {
    const w = mount(MasmorraIndicador, { props: { nivel: 5 } })
    expect(w.text()).toContain('N5')
    expect(w.find('i.pi-bolt').exists()).toBe(true)
    expect(w.attributes('aria-label')).toBe('Masmorra nível 5')
  })
})
