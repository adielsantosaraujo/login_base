import type { TipoRegiao } from './regioes'

export type TipoTerreno =
  | 'FLORESTA'
  | 'BARREIRO'
  | 'PLANTACOES'
  | 'CRIACOES'
  | 'ROCHA'
  | 'FERRO'
  | 'CARVAO'
  | 'SALINAS'
  | 'ENXOFRE'
  | 'MILITAR'
  | 'INDUSTRIA'
  | 'COMERCIO'
  | 'DESENVOLVIMENTO'

export interface TerrenoDaRegiao {
  terreno: TipoTerreno
  posicao: number
  percentual: number
}

export const TERRENOS: TipoTerreno[] = [
  'FLORESTA',
  'BARREIRO',
  'PLANTACOES',
  'CRIACOES',
  'ROCHA',
  'FERRO',
  'CARVAO',
  'SALINAS',
  'ENXOFRE',
  'MILITAR',
  'INDUSTRIA',
  'COMERCIO',
  'DESENVOLVIMENTO',
]

export const SIGLA_TERRENO: Record<TipoTerreno, string> = {
  FLORESTA: 'Fl',
  BARREIRO: 'Ba',
  PLANTACOES: 'Pl',
  CRIACOES: 'Cr',
  ROCHA: 'Ro',
  FERRO: 'Fe',
  CARVAO: 'Ca',
  SALINAS: 'Sa',
  ENXOFRE: 'En',
  MILITAR: 'Mi',
  INDUSTRIA: 'In',
  COMERCIO: 'Co',
  DESENVOLVIMENTO: 'De',
}

export const ROTULO_TERRENO: Record<TipoTerreno, string> = {
  FLORESTA: 'Floresta',
  BARREIRO: 'Barreiro',
  PLANTACOES: 'Plantações',
  CRIACOES: 'Criações',
  ROCHA: 'Rocha',
  FERRO: 'Ferro',
  CARVAO: 'Carvão',
  SALINAS: 'Salinas',
  ENXOFRE: 'Enxofre',
  MILITAR: 'Militar',
  INDUSTRIA: 'Indústria',
  COMERCIO: 'Comércio',
  DESENVOLVIMENTO: 'Desenvolvimento',
}

export const COR_TERRENO: Record<TipoTerreno, string> = Object.fromEntries(
  TERRENOS.map((t) => [t, `var(--vl-terreno-${SIGLA_TERRENO[t].toLowerCase()})`]),
) as Record<TipoTerreno, string>

/** Terrenos possíveis por tipo de região. */
export const TERRENOS_POR_TIPO: Record<TipoRegiao, TipoTerreno[]> = {
  FLORESTA: ['FLORESTA', 'BARREIRO', 'PLANTACOES'],
  PLANICIE: ['PLANTACOES', 'CRIACOES', 'FLORESTA'],
  URBANA: ['INDUSTRIA', 'COMERCIO', 'DESENVOLVIMENTO'],
  LITORAL: ['SALINAS', 'ENXOFRE', 'MILITAR'],
  MONTANHA: ['ROCHA', 'FERRO', 'CARVAO'],
}

/** Percentual decrescente; desempate pela posição. Não muta a entrada. */
export function ordenarTerrenos(terrenos: TerrenoDaRegiao[]): TerrenoDaRegiao[] {
  return [...terrenos].sort((x, y) => y.percentual - x.percentual || x.posicao - y.posicao)
}

/** Ex.: "Floresta 40% · Plantações 35% · Barreiro 25%". */
export function composicaoTexto(terrenos: TerrenoDaRegiao[]): string {
  return ordenarTerrenos(terrenos)
    .map((t) => `${ROTULO_TERRENO[t.terreno]} ${t.percentual}%`)
    .join(' · ')
}

/** Soma dos percentuais (= ladrilhos) por terreno nas regiões selecionadas. */
export function totaisLadrilhos(
  sel: number[],
  regioes: { indice: number; terrenos: TerrenoDaRegiao[] }[],
): Record<TipoTerreno, number> {
  const totais = Object.fromEntries(TERRENOS.map((t) => [t, 0])) as Record<TipoTerreno, number>
  for (const i of sel) {
    const regiao = regioes.find((r) => r.indice === i)
    if (!regiao) continue
    for (const t of regiao.terrenos) totais[t.terreno] += t.percentual
  }
  return totais
}

export const COLUNAS_LADRILHO = 'ABCDEFGHIJ'

/** Endereço do ladrilho: coluna em letra, linha em número a partir de 1. (0,0) = "(A,1)". */
export function enderecoLadrilho(coluna: number, linha: number): string {
  return `(${COLUNAS_LADRILHO[coluna]},${linha + 1})`
}
