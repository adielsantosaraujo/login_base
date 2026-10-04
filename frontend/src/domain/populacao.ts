/**
 * Domínio da população inicial — porte de docs/designe/handoff/referencia/distribuicao-populacao.js.
 * Determinístico; paridade garantida por __fixtures__/distribuicao-populacao.json.
 */

export type Caracteristica = 'VIT' | 'FOR' | 'VEL' | 'INT' | 'CAR'
export type Profissao =
  | 'CONSTRUTOR' | 'CARREGADOR' | 'AGRICULTOR' | 'FAZENDEIRO' | 'MINEIRO' | 'MADEIREIRO'
  | 'FERREIRO' | 'COZINHEIRO' | 'COSTUREIRO' | 'CACADOR' | 'GUERREIRO' | 'COMERCIANTE'
export type Papel = 'PAI' | 'MAE' | 'FILHO' | 'FILHA'

export type MapaCaracteristicas = Record<Caracteristica, number>
export type MapaProfissoes = Record<Profissao, number>
export type Plano = Partial<Record<Profissao, number>>

export interface Distribuicao {
  caracteristicas: MapaCaracteristicas
  profissoes: MapaProfissoes
}

export interface CidadaoDistribuido extends Partial<Distribuicao> {
  nome: string
  sexo?: 'M' | 'F'
  idadeAnos: number
  papel: Papel
  [chave: string]: unknown
}

export type CidadaoCompleto = CidadaoDistribuido & Distribuicao

export interface FamiliaDistribuida<C = CidadaoDistribuido> {
  id?: number
  sobrenome: string
  cidadaos: C[]
}

export const CARACTERISTICAS: readonly Caracteristica[] = ['VIT', 'FOR', 'VEL', 'INT', 'CAR']

// Ordem da tabela = critério de desempate da profissão principal.
export const PROFISSOES_LIGADAS: Record<Profissao, readonly Caracteristica[]> = {
  CONSTRUTOR: ['INT'],
  CARREGADOR: ['FOR', 'VEL'],
  AGRICULTOR: ['INT'],
  FAZENDEIRO: ['INT'],
  MINEIRO: ['FOR'],
  MADEIREIRO: ['FOR', 'VIT'],
  FERREIRO: ['INT'],
  COZINHEIRO: ['VEL', 'CAR'],
  COSTUREIRO: ['VEL', 'CAR'],
  CACADOR: ['VIT', 'VEL', 'CAR'],
  GUERREIRO: ['FOR', 'VIT', 'VEL'],
  COMERCIANTE: ['CAR'],
}

export const CARACTERISTICAS_LIGADAS = PROFISSOES_LIGADAS

export const PROFISSOES: readonly Profissao[] = Object.keys(PROFISSOES_LIGADAS) as Profissao[]

export const ROTULOS_PROFISSAO: Record<string, string> = {
  CONSTRUTOR: 'Construtor', CARREGADOR: 'Carregador', AGRICULTOR: 'Agricultor', FAZENDEIRO: 'Fazendeiro',
  MINEIRO: 'Mineiro', MADEIREIRO: 'Madeireiro', FERREIRO: 'Ferreiro', COZINHEIRO: 'Cozinheiro',
  COSTUREIRO: 'Costureiro', CACADOR: 'Caçador', GUERREIRO: 'Guerreiro', COMERCIANTE: 'Comerciante',
}

export const SECUNDARIA: Record<Profissao, Profissao> = {
  CONSTRUTOR: 'CARREGADOR', CARREGADOR: 'CONSTRUTOR', AGRICULTOR: 'FAZENDEIRO', FAZENDEIRO: 'AGRICULTOR',
  MINEIRO: 'MADEIREIRO', MADEIREIRO: 'MINEIRO', FERREIRO: 'MINEIRO', COZINHEIRO: 'COMERCIANTE',
  COSTUREIRO: 'COZINHEIRO', CACADOR: 'GUERREIRO', GUERREIRO: 'CACADOR', COMERCIANTE: 'COZINHEIRO',
}

export const APOIO_CANDIDATAS: readonly Profissao[] = ['CARREGADOR', 'CONSTRUTOR', 'MADEIREIRO']

// Ordem em que os papéis do plano são atribuídos.
export const ORDEM_PLANO: readonly Profissao[] = [
  'COMERCIANTE', 'CONSTRUTOR', 'CARREGADOR', 'MADEIREIRO', 'MINEIRO', 'AGRICULTOR',
  'FAZENDEIRO', 'COZINHEIRO', 'GUERREIRO', 'FERREIRO', 'COSTUREIRO', 'CACADOR',
]

export const PLANO_PADRAO: Plano = {
  COMERCIANTE: 1, CONSTRUTOR: 2, CARREGADOR: 2, MADEIREIRO: 2, MINEIRO: 2, AGRICULTOR: 2,
  FAZENDEIRO: 1, COZINHEIRO: 1, GUERREIRO: 2, FERREIRO: 1, COSTUREIRO: 0, CACADOR: 0,
}

export const MINIMOS: Plano = { CONSTRUTOR: 2, CARREGADOR: 2 }
export const LIMITES = { caracteristicasTotal: 20, profissoesTotal: 10 }

const PONTOS_PROF = [5, 3, 2] as const // principal / secundária / apoio
const PESOS = { base: { VIT: 0.5 } as Partial<Record<Caracteristica, number>>, principal: 3, secundaria: 1.5, apoio: 0.5 }
const POPULACAO = 16
const ORDEM_PAPEL: readonly Papel[] = ['PAI', 'MAE', 'FILHO', 'FILHA']

function zerado<K extends string>(chaves: readonly K[]): Record<K, number> {
  return Object.fromEntries(chaves.map((k) => [k, 0])) as Record<K, number>
}

export function soma(mapa: Record<string, number>): number {
  return Object.values(mapa).reduce((a, b) => a + b, 0)
}

/** Distribui os pontos de uma pessoa para a profissão principal `prof`. */
export function distribuirPessoa(prof: Profissao): Distribuicao {
  const sec = SECUNDARIA[prof]
  const apoio = APOIO_CANDIDATAS.find((k) => k !== prof && k !== sec) as Profissao
  const profissoes = zerado(PROFISSOES)
  profissoes[prof] = PONTOS_PROF[0]
  profissoes[sec] = PONTOS_PROF[1]
  profissoes[apoio] = PONTOS_PROF[2]

  const peso: Record<Caracteristica, number> = { ...zerado(CARACTERISTICAS), ...PESOS.base }
  PROFISSOES_LIGADAS[prof].forEach((c) => { peso[c] += PESOS.principal })
  PROFISSOES_LIGADAS[sec].forEach((c) => { peso[c] += PESOS.secundaria })
  PROFISSOES_LIGADAS[apoio].forEach((c) => { peso[c] += PESOS.apoio })

  const caracteristicas = zerado(CARACTERISTICAS)
  for (let i = 0; i < LIMITES.caracteristicasTotal; i++) {
    let melhor: Caracteristica = CARACTERISTICAS[0]
    let score = -1
    for (const c of CARACTERISTICAS) {
      const s = peso[c] / (caracteristicas[c] + 1)
      if (s > score) { score = s; melhor = c }
    }
    caracteristicas[melhor]++
  }
  return { caracteristicas, profissoes }
}

/** Distribui a população inteira (4 famílias x 4 membros) conforme o plano (soma 16). */
export function distribuirPopulacao<F extends { cidadaos: object[] }>(
  familias: F[],
  plano: Plano = PLANO_PADRAO,
): (Omit<F, 'cidadaos'> & { cidadaos: (F['cidadaos'][number] & Distribuicao)[] })[] {
  if (soma(plano as Record<string, number>) !== POPULACAO) throw new Error('Plano deve somar ' + POPULACAO)
  const papeis: Profissao[] = ORDEM_PLANO.flatMap((p) => Array<Profissao>(plano[p] || 0).fill(p))
  const resultado = familias.map((f) => ({ ...f, cidadaos: f.cidadaos.map((c) => ({ ...c })) })) as
    (Omit<F, 'cidadaos'> & { cidadaos: (F['cidadaos'][number] & Distribuicao)[] })[]
  let j = 0
  for (let membro = 0; membro < 4; membro++) {
    for (let fam = 0; fam < resultado.length; fam++) {
      Object.assign(resultado[fam]!.cidadaos[membro]!, distribuirPessoa(papeis[j++] || 'CARREGADOR'))
    }
  }
  return resultado
}

/* ---------- Derivados e validação ---------- */

/** Profissão principal = maior PE; empate pela ordem de PROFISSOES; null se tudo 0. */
export function principal(cidadao: { profissoes: Partial<Record<string, number>> }): Profissao | null {
  let melhor: Profissao | null = null
  let v = 0
  for (const p of PROFISSOES) {
    const pe = cidadao.profissoes[p] || 0
    if (pe > v) { v = pe; melhor = p }
  }
  return melhor
}

/** Troca a principal de um cidadão e ajusta o plano (-1 na antiga, +1 na nova). */
export function trocarPrincipal<C extends { profissoes: Partial<Record<string, number>> }>(
  cidadao: C,
  novaProf: Profissao,
  plano: Plano,
): { cidadao: C & Distribuicao; plano: Plano } {
  const antiga = principal(cidadao)
  const novoPlano: Plano = { ...plano }
  if (antiga && (novoPlano[antiga] || 0) > 0) novoPlano[antiga] = (novoPlano[antiga] as number) - 1
  novoPlano[novaProf] = (novoPlano[novaProf] || 0) + 1
  return { cidadao: { ...cidadao, ...distribuirPessoa(novaProf) }, plano: novoPlano }
}

/** Bônus R3: soma de floor(característica / 5) das características ligadas à profissão. */
export function bonusR3(caracteristicas: Partial<Record<string, number>>, profissao: Profissao): number {
  return PROFISSOES_LIGADAS[profissao].reduce((a, c) => a + Math.floor((caracteristicas[c] || 0) / 5), 0)
}

/** Ex.: "Bônus R3: FOR 8÷5 + VEL 7÷5". */
export function explicacaoBonusR3(caracteristicas: Partial<Record<string, number>>, profissao: Profissao): string {
  const termos = PROFISSOES_LIGADAS[profissao].map((c) => `${c} ${caracteristicas[c] || 0}÷5`)
  return 'Bônus R3: ' + termos.join(' + ')
}

/** Líder = adulto (>= 18) mais velho; empate PAI, MAE, FILHO, FILHA. */
export function liderDaFamilia<C extends { idadeAnos: number; papel: Papel }>(familia: { cidadaos: C[] }): C | undefined {
  return familia.cidadaos
    .filter((c) => c.idadeAnos >= 18)
    .sort((a, b) => b.idadeAnos - a.idadeAnos || ORDEM_PAPEL.indexOf(a.papel) - ORDEM_PAPEL.indexOf(b.papel))[0]
}

/** Bônus do líder em %: min(10, floor(CAR / 2)). */
export function bonusLider(car: number | { car?: number; CAR?: number }): number {
  const valor = typeof car === 'number' ? car : (car.CAR ?? car.car ?? 0)
  return Math.min(10, Math.floor(valor / 2))
}

/** Índice da família sugerida = líder com maior CAR (estrito; empate fica com a primeira). */
export function familiaLiderSugerida(familias: FamiliaDistribuida<CidadaoCompleto>[]): number {
  const carLider = (f: FamiliaDistribuida<CidadaoCompleto>) => liderDaFamilia(f)?.caracteristicas.CAR ?? 0
  let idx = 0
  familias.forEach((f, i) => { if (carLider(f) > carLider(familias[idx] as FamiliaDistribuida<CidadaoCompleto>)) idx = i })
  return idx
}

/**
 * Validação completa. Retorna lista de erros (vazia = ok).
 * `familiaLiderId` é o `id` da família, se as famílias tiverem id; senão, o índice.
 */
export function validar(familias: FamiliaDistribuida<CidadaoCompleto>[], familiaLiderId: number | null): string[] {
  const erros: string[] = []
  const todos = familias.flatMap((f) => f.cidadaos)
  if (todos.length !== POPULACAO) erros.push('POPULACAO_INCOMPLETA')
  todos.forEach((c) => {
    if (Object.values(c.caracteristicas).some((v) => v < 0) || soma(c.caracteristicas) > LIMITES.caracteristicasTotal)
      erros.push(`${c.nome}: Máximo ${LIMITES.caracteristicasTotal} pontos de característica`)
    if (Object.values(c.profissoes).some((v) => v < 0) || soma(c.profissoes) > LIMITES.profissoesTotal)
      erros.push(`${c.nome}: Máximo ${LIMITES.profissoesTotal} pontos de profissão`)
  })
  const contagem: Partial<Record<Profissao, number>> = {}
  todos.forEach((c) => {
    const p = principal(c)
    if (p) contagem[p] = (contagem[p] || 0) + 1
  })
  ;(Object.entries(MINIMOS) as [Profissao, number][]).forEach(([p, min]) => {
    if ((contagem[p] || 0) < min) erros.push(`MINIMO_${p}`)
  })
  const valida = familiaLiderId !== null && familiaLiderId !== undefined && (
    familias.some((f) => f.id !== undefined)
      ? familias.some((f) => f.id === familiaLiderId)
      : familiaLiderId >= 0 && familiaLiderId < familias.length
  )
  if (!valida) erros.push('Escolha uma família líder')
  return erros
}
