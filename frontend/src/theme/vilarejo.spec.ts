import { describe, expect, it } from 'vitest'
import { Vilarejo } from './vilarejo'

describe('preset Vilarejo', () => {
  const semantic = (Vilarejo as any).semantic

  it('usa âmbar #e8b55a como primary 500', () => {
    expect(semantic.primary[500]).toBe('#e8b55a')
  })

  it('define superfícies quentes no modo escuro', () => {
    const surface = semantic.colorScheme.dark.surface
    expect(surface[950]).toBe('#1a1714')
    expect(surface[900]).toBe('#211d19')
    expect(surface[800]).toBe('#27231f')
    expect(surface[700]).toBe('#332e29')
    expect(surface[600]).toBe('#3d3731')
    expect(surface[0]).toBe('#ffffff')
  })

  it('define cor de contraste e highlight âmbar', () => {
    const dark = semantic.colorScheme.dark
    expect(dark.primary.color).toBe('#e8b55a')
    expect(dark.primary.contrastColor).toBe('#1d1a17')
    expect(dark.highlight.background).toContain('232, 181, 90')
  })
})
