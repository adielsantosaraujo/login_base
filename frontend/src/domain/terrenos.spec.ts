import { describe, expect, it } from 'vitest'
import {
  COR_TERRENO,
  SIGLA_TERRENO,
  TERRENOS,
  TERRENOS_POR_TIPO,
  composicaoTexto,
  enderecoLadrilho,
  ordenarTerrenos,
  totaisLadrilhos,
  type TerrenoDaRegiao,
} from './terrenos'

const t = (terreno: TerrenoDaRegiao['terreno'], posicao: number, percentual: number): TerrenoDaRegiao => ({
  terreno,
  posicao,
  percentual,
})

describe('terrenos', () => {
  it('tem 13 siglas distintas de 2 letras', () => {
    const siglas = TERRENOS.map((x) => SIGLA_TERRENO[x])
    expect(siglas).toHaveLength(13)
    expect(new Set(siglas).size).toBe(13)
    expect(siglas.every((s) => s.length === 2)).toBe(true)
  })

  it('monta a cor a partir da sigla', () => {
    expect(COR_TERRENO.FLORESTA).toBe('var(--vl-terreno-fl)')
    expect(COR_TERRENO.DESENVOLVIMENTO).toBe('var(--vl-terreno-de)')
  })

  it('cada tipo tem 3 terrenos e o conjunto cobre os 13', () => {
    const todos = new Set<string>()
    for (const lista of Object.values(TERRENOS_POR_TIPO)) {
      expect(lista).toHaveLength(3)
      lista.forEach((x) => todos.add(x))
    }
    expect(todos.size).toBe(13)
  })

  it('ordena por percentual decrescente sem mutar a entrada', () => {
    const entrada = [t('FLORESTA', 1, 25), t('BARREIRO', 2, 40), t('PLANTACOES', 3, 35)]
    const copia = [...entrada]
    expect(ordenarTerrenos(entrada).map((x) => x.terreno)).toEqual(['BARREIRO', 'PLANTACOES', 'FLORESTA'])
    expect(entrada).toEqual(copia)
  })

  it('desempata por posição', () => {
    const r = ordenarTerrenos([t('BARREIRO', 2, 30), t('FLORESTA', 1, 30), t('PLANTACOES', 3, 40)])
    expect(r.map((x) => x.terreno)).toEqual(['PLANTACOES', 'FLORESTA', 'BARREIRO'])
  })

  it('gera o texto da composição', () => {
    expect(composicaoTexto([t('FLORESTA', 1, 40), t('PLANTACOES', 2, 35), t('BARREIRO', 3, 25)])).toBe(
      'Floresta 40% · Plantações 35% · Barreiro 25%',
    )
  })

  it('soma ladrilhos por terreno nas regiões selecionadas', () => {
    const regioes = [
      { indice: 1, terrenos: [t('COMERCIO', 1, 47), t('INDUSTRIA', 2, 30), t('DESENVOLVIMENTO', 3, 23)] },
      { indice: 2, terrenos: [t('SALINAS', 1, 38), t('MILITAR', 2, 40), t('ENXOFRE', 3, 22)] },
      { indice: 3, terrenos: [t('PLANTACOES', 1, 45), t('CRIACOES', 2, 26), t('FLORESTA', 3, 29)] },
    ]
    const totais = totaisLadrilhos([1, 2, 3, 99], regioes)
    expect(Object.keys(totais)).toHaveLength(13)
    expect(Object.values(totais).reduce((a, b) => a + b, 0)).toBe(300)
    expect(totais).toMatchObject({
      COMERCIO: 47,
      INDUSTRIA: 30,
      DESENVOLVIMENTO: 23,
      SALINAS: 38,
      MILITAR: 40,
      ENXOFRE: 22,
      PLANTACOES: 45,
      CRIACOES: 26,
      FLORESTA: 29,
      ROCHA: 0,
    })
  })

  it('formata o endereço do ladrilho', () => {
    expect(enderecoLadrilho(0, 0)).toBe('(A,1)')
    expect(enderecoLadrilho(2, 4)).toBe('(C,5)')
    expect(enderecoLadrilho(9, 9)).toBe('(J,10)')
  })
})
