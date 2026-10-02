export type Categoria = 'ARMA' | 'FERRAMENTA' | 'ARMADURA' | 'JOIA'
export type QualidadeItem = 'SIMPLES' | 'BOA' | 'EXCELENTE' | 'DIVINA'

export interface BonusItem {
  codigo: string
  valor: number
}

export interface ItemDTO {
  id: number
  categoria: Categoria
  subtipo: string
  nome: string
  nivel: number
  qualidade: QualidadeItem
  bonus: BonusItem[]
  atributoEscolhido: string | null
  slot: string | null
  cidadaoId: number | null
  emAprimoramento: boolean
  atributoPrincipal: { tipo: string; valor: number } | null
}

export const ROTULOS_SUBTIPO: Record<string, string> = {
  ESPADA: 'Espada', LANCA: 'Lança', ARCO: 'Arco', BESTA: 'Besta',
  MARTELO: 'Martelo', CARRINHO_DE_MAO: 'Carrinho de mão', ENXADA: 'Enxada', FORCADO: 'Forcado',
  PICARETA: 'Picareta', MACHADO: 'Machado', MALHO: 'Malho', CUTELO: 'Cutelo',
  KIT_DE_COSTURA: 'Kit de costura', FACA_DE_CACA: 'Faca de caça', BALANCA: 'Balança',
  PEITORAL: 'Peitoral', CAPACETE: 'Capacete', OMBREIRAS: 'Ombreiras', LUVAS: 'Luvas',
  CALCAS: 'Calças', SAPATO: 'Sapato', COLAR: 'Colar', ANEL: 'Anel',
}

export const ROTULOS_CATEGORIA: Record<string, string> = {
  ARMA: 'Arma', FERRAMENTA: 'Ferramenta', ARMADURA: 'Armadura', JOIA: 'Joia',
}

export const ROTULOS_QUALIDADE: Record<string, string> = {
  SIMPLES: 'Simples', BOA: 'Boa', EXCELENTE: 'Excelente', DIVINA: 'Divina',
}

export const CORES_QUALIDADE: Record<string, string> = {
  SIMPLES: '#6b7280', BOA: '#16a34a', EXCELENTE: '#2563eb', DIVINA: '#d97706',
}

export const ROTULOS_SLOT: Record<string, string> = {
  ARMA: 'Arma', FERRAMENTA: 'Ferramenta', PEITORAL: 'Peitoral', CAPACETE: 'Capacete',
  OMBREIRAS: 'Ombreiras', LUVAS: 'Luvas', CALCAS: 'Calças', SAPATO: 'Sapato',
  COLAR: 'Colar', ANEL_1: 'Anel 1', ANEL_2: 'Anel 2',
}

export const ROTULOS_BONUS: Record<string, string> = {
  VIT: 'Vitalidade', FOR: 'Força', VEL: 'Velocidade', INT: 'Inteligência', CAR: 'Carisma',
  ATK: 'Ataque', DEF: 'Defesa', VIDA: 'Vida', INI: 'Iniciativa', CRIT: 'Crítico',
  PROF: 'Proficiência', PROD: 'Produção',
}

/** Atributos que podem ser escolhidos em itens que exigem atributo (anéis). */
export const ATRIBUTOS_ESCOLHA = ['VIT', 'FOR', 'VEL', 'INT', 'CAR']

export const ROTULOS_RECURSO: Record<string, string> = {
  MADEIRA: 'Madeira', PEDRA: 'Pedra', FERRO: 'Ferro', ACO: 'Aço', COURO: 'Couro',
  TECIDO: 'Tecido', LA: 'Lã', OURO: 'Ouro', ARGILA: 'Argila', TIJOLO: 'Tijolo', TABUA: 'Tábua',
}

function capitalizar(chave: string): string {
  const t = chave.toLowerCase().replace(/_/g, ' ')
  return t.charAt(0).toUpperCase() + t.slice(1)
}

function rotuloDe(mapa: Record<string, string>, chave: string | null | undefined): string {
  if (!chave) return '-'
  return mapa[chave] ?? capitalizar(chave)
}

export const rotuloSubtipo = (c?: string | null) => rotuloDe(ROTULOS_SUBTIPO, c)
export const rotuloCategoria = (c?: string | null) => rotuloDe(ROTULOS_CATEGORIA, c)
export const rotuloQualidade = (c?: string | null) => rotuloDe(ROTULOS_QUALIDADE, c)
export const rotuloSlot = (c?: string | null) => rotuloDe(ROTULOS_SLOT, c)
export const rotuloBonus = (c?: string | null) => rotuloDe(ROTULOS_BONUS, c)
export const rotuloRecurso = (c?: string | null) => rotuloDe(ROTULOS_RECURSO, c)
export const corQualidade = (q?: string | null) => (q ? CORES_QUALIDADE[q] ?? CORES_QUALIDADE.SIMPLES : CORES_QUALIDADE.SIMPLES)
