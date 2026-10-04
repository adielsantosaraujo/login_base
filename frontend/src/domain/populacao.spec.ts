import { describe, expect, it } from 'vitest'
import fixture from './__fixtures__/distribuicao-populacao.json'
import {
  PLANO_PADRAO, PROFISSOES, bonusLider, bonusR3, distribuirPessoa, distribuirPopulacao, explicacaoBonusR3,
  familiaLiderSugerida, liderDaFamilia, principal, trocarPrincipal, validar,
  type FamiliaDistribuida, type Plano, type Profissao,
} from './populacao'

const familias = fixture.familias as FamiliaDistribuida[]

describe('distribuirPessoa', () => {
  it.each(Object.entries(fixture.pessoas))('reproduz a pessoa %s da fixture', (prof, esperado) => {
    expect(distribuirPessoa(prof as Profissao)).toEqual(esperado)
  })

  it('cobre as 12 profissões', () => {
    expect(Object.keys(fixture.pessoas)).toHaveLength(12)
    expect(PROFISSOES).toHaveLength(12)
  })

  it('é determinística', () => {
    expect(distribuirPessoa('CACADOR')).toEqual(distribuirPessoa('CACADOR'))
  })
})

describe('distribuirPopulacao', () => {
  it.each(fixture.casos)('caso $nome', (caso) => {
    const pop = distribuirPopulacao(familias, caso.plano as Plano)
    const obtido = pop.map((f) => f.cidadaos.map((c) => ({
      caracteristicas: c.caracteristicas, profissoes: c.profissoes, principal: principal(c),
    })))
    expect(obtido).toEqual(caso.resultado)
    expect(familiaLiderSugerida(pop)).toBe(caso.familiaLiderSugerida)
  })

  it('rejeita plano que não soma 16', () => {
    expect(() => distribuirPopulacao(familias, { CONSTRUTOR: 3 })).toThrow()
  })

  it('é determinística', () => {
    expect(distribuirPopulacao(familias)).toEqual(distribuirPopulacao(familias))
  })
})

describe('trocarPrincipal', () => {
  it('Construtor -> Guerreiro ajusta o plano e dá Guerreiro 5', () => {
    const construtor = { nome: 'X', ...distribuirPessoa('CONSTRUTOR') }
    const r = trocarPrincipal(construtor, 'GUERREIRO', PLANO_PADRAO)
    expect(r.plano.CONSTRUTOR).toBe(1)
    expect(r.plano.GUERREIRO).toBe(3)
    expect(r.cidadao.profissoes.GUERREIRO).toBe(5)
    expect(r.cidadao.nome).toBe('X')
  })
})

describe('bônus', () => {
  it('bonusR3 FOR 8 VEL 7 Carregador = 2', () => {
    const car = { VIT: 2, FOR: 8, VEL: 7, INT: 3, CAR: 0 }
    expect(bonusR3(car, 'CARREGADOR')).toBe(2)
    expect(explicacaoBonusR3(car, 'CARREGADOR')).toBe('Bônus R3: FOR 8÷5 + VEL 7÷5')
  })

  it('bonusLider limita em 10', () => {
    expect(bonusLider(13)).toBe(6)
    expect(bonusLider(40)).toBe(10)
  })
})

describe('líder e validação', () => {
  const pop = distribuirPopulacao(familias)

  it('líder é o adulto mais velho, empate PAI primeiro', () => {
    expect(liderDaFamilia(pop[0])?.papel).toBe('PAI')
  })

  it('validar do plano padrão retorna vazio', () => {
    expect(validar(pop, familiaLiderSugerida(pop))).toEqual([])
  })

  it('exige família líder', () => {
    expect(validar(pop, null)).toContain('Escolha uma família líder')
  })
})
