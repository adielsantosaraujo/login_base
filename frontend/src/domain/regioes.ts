export type TipoRegiao = 'FLORESTA' | 'PLANICIE' | 'URBANA' | 'LITORAL' | 'MONTANHA'

export type BonusRegiao =
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

export interface BonusDaRegiao {
  bonus: BonusRegiao
  posicao: number
  valor: number
}

export interface RegiaoPrevia {
  indice: number
  tipo: TipoRegiao
  bonus: BonusDaRegiao[]
}

export interface PreviaMapa {
  previaId: string
  rodada: number
  regioes: RegiaoPrevia[]
}

export const LARGURA_GRADE = 4
export const TAMANHO_SELECAO = 3

export const TIPOS: TipoRegiao[] = ['FLORESTA', 'PLANICIE', 'URBANA', 'LITORAL', 'MONTANHA']

export const BONUS: BonusRegiao[] = [
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

export const ROTULO_TIPO: Record<TipoRegiao, string> = {
  FLORESTA: 'Floresta',
  PLANICIE: 'Planície',
  URBANA: 'Urbana',
  LITORAL: 'Litoral',
  MONTANHA: 'Montanha',
}

export const ROTULO_BONUS: Record<BonusRegiao, string> = {
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

export const COR_TIPO: Record<TipoRegiao, string> = Object.fromEntries(
  TIPOS.map((t) => [t, `var(--vl-tipo-${t.toLowerCase()})`]),
) as Record<TipoRegiao, string>

export const COR_BONUS: Record<BonusRegiao, string> = Object.fromEntries(
  BONUS.map((b) => [b, `var(--vl-bonus-${b.toLowerCase()})`]),
) as Record<BonusRegiao, string>

/** Bônus possíveis por tipo de região (D1). */
export const BONUS_POR_TIPO: Record<TipoRegiao, BonusRegiao[]> = {
  FLORESTA: ['FLORESTA', 'BARREIRO', 'PLANTACOES'],
  PLANICIE: ['PLANTACOES', 'CRIACOES', 'FLORESTA'],
  URBANA: ['INDUSTRIA', 'COMERCIO', 'DESENVOLVIMENTO'],
  LITORAL: ['SALINAS', 'ENXOFRE', 'MILITAR'],
  MONTANHA: ['ROCHA', 'FERRO', 'CARVAO'],
}

/** Vizinhança ortogonal na grade 4x4 (índices 1..16). */
export function adjacentes(a: number, b: number): boolean {
  const la = Math.floor((a - 1) / LARGURA_GRADE)
  const ca = (a - 1) % LARGURA_GRADE
  const lb = Math.floor((b - 1) / LARGURA_GRADE)
  const cb = (b - 1) % LARGURA_GRADE
  return Math.abs(la - lb) + Math.abs(ca - cb) === 1
}

/** O conjunto forma um grupo conexo (BFS ortogonal). Vazio é considerado não conexo. */
export function conectado(sel: number[]): boolean {
  if (sel.length === 0) return false
  const visitados = new Set<number>([sel[0]])
  const fila = [sel[0]]
  while (fila.length > 0) {
    const atual = fila.shift() as number
    for (const i of sel) {
      if (!visitados.has(i) && adjacentes(atual, i)) {
        visitados.add(i)
        fila.push(i)
      }
    }
  }
  return visitados.size === new Set(sel).size
}

/** Não olha o tipo: há menos de 3 selecionadas e (seleção vazia ou i vizinha de alguma). */
export function podeSelecionar(sel: number[], i: number): boolean {
  if (sel.length >= TAMANHO_SELECAO) return false
  return sel.length === 0 || sel.some((s) => adjacentes(s, i))
}

export function temUrbana(sel: number[], regioes: RegiaoPrevia[]): boolean {
  return sel.some((i) => regioes.find((r) => r.indice === i)?.tipo === 'URBANA')
}

export function selecaoValida(sel: number[], regioes: RegiaoPrevia[]): boolean {
  return sel.length === TAMANHO_SELECAO && conectado(sel) && temUrbana(sel, regioes)
}

export function totaisBonus(sel: number[], regioes: RegiaoPrevia[]): Record<BonusRegiao, number> {
  const totais = Object.fromEntries(BONUS.map((b) => [b, 0])) as Record<BonusRegiao, number>
  for (const i of sel) {
    const regiao = regioes.find((r) => r.indice === i)
    if (!regiao) continue
    for (const b of regiao.bonus) totais[b.bonus] += b.valor
  }
  return totais
}

export function bonusOrdenados(bonus: BonusDaRegiao[]): BonusDaRegiao[] {
  return [...bonus].sort((x, y) => y.valor - x.valor)
}

export function dicaSelecao(sel: number[], regioes: RegiaoPrevia[]): string {
  if (sel.length === 0) return 'Clique em uma região para começar.'
  if (sel.length < TAMANHO_SELECAO) return 'Regiões destacadas são vizinhas da sua seleção.'
  if (!conectado(sel)) return 'As regiões precisam ser vizinhas.'
  if (!temUrbana(sel, regioes)) return 'Inclua ao menos uma região Urbana.'
  return 'Tudo certo — crie sua vila.'
}
