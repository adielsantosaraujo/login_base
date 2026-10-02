import { describe, expect, it } from 'vitest'
import { ROTULOS_SLOT, ROTULOS_SUBTIPO, corQualidade, rotuloBonus, rotuloQualidade, rotuloRecurso, rotuloSubtipo } from './useItens'

describe('useItens', () => {
  it('possui 23 subtipos e 11 slots', () => {
    expect(Object.keys(ROTULOS_SUBTIPO)).toHaveLength(23)
    expect(Object.keys(ROTULOS_SLOT)).toHaveLength(11)
  })
  it('traduz rótulos e usa fallback', () => {
    expect(rotuloSubtipo('CARRINHO_DE_MAO')).toBe('Carrinho de mão')
    expect(rotuloQualidade('DIVINA')).toBe('Divina')
    expect(rotuloBonus('VIT')).toBe('Vitalidade')
    expect(rotuloRecurso('NOVO_RECURSO')).toBe('Novo recurso')
    expect(rotuloSubtipo(null)).toBe('-')
  })
  it('cor por qualidade', () => {
    expect(corQualidade('DIVINA')).not.toBe(corQualidade('SIMPLES'))
  })
})
