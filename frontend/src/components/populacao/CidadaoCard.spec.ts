import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import CidadaoCard from './CidadaoCard.vue'
import { distribuirPessoa } from '../../domain/populacao'

function cidadao() {
  return { cidadaoId: 7, nome: 'Felipe', sexo: 'M' as const, idadeAnos: 40, papel: 'PAI' as const, ...distribuirPessoa('CONSTRUTOR') }
}
const props = { limiteCaracteristicas: 20, limiteProfissoes: 10 }

describe('CidadaoCard', () => {
  it('mostra cabeçalho, contadores e principal', () => {
    const w = mount(CidadaoCard, { props: { cidadao: cidadao(), ...props } })
    expect(w.text()).toContain('40 anos · Pai')
    expect(w.get('[data-testid="contador-car"]').text()).toBe('20 / 20')
    expect(w.get('[data-testid="contador-prof"]').text()).toBe('10 / 10')
    expect((w.get('[data-testid="principal"]').element as HTMLSelectElement).value).toBe('CONSTRUTOR')
  })

  it('emite editar e trocar-principal', async () => {
    const c = cidadao()
    c.caracteristicas.INT = 3
    const w = mount(CidadaoCard, { props: { cidadao: c, ...props } })
    await w.get('[data-testid="car-INT"] button[aria-label^="Diminuir"]').trigger('click')
    expect(w.emitted('editar')![0]).toEqual([7, 'INT', 2])
    await w.get('[data-testid="principal"]').setValue('GUERREIRO')
    expect(w.emitted('trocar-principal')![0]).toEqual([7, 'GUERREIRO'])
  })

  it('sem profissão mostra placeholder', () => {
    const c = cidadao()
    for (const k of Object.keys(c.profissoes)) (c.profissoes as Record<string, number>)[k] = 0
    const w = mount(CidadaoCard, { props: { cidadao: c, ...props } })
    expect(w.text()).toContain('Sem profissão')
  })
})
