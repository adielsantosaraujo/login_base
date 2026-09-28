// Tipos TypeScript espelhando os DTOs da API do jogo (ver design.md, seções
// 12 e 17). O frontend não calcula regras — apenas exibe o que a API retorna
// (custos, tempos, atributos derivados já vêm prontos do backend).

export type TipoRecurso = 'COMIDA' | 'MADEIRA' | 'PEDRA' | 'FERRO'

export type TipoPredio =
  | 'CENTRO_VILA'
  | 'ARMAZEM'
  | 'FAZENDA'
  | 'SERRARIA'
  | 'PEDREIRA'
  | 'MINA_FERRO'
  | 'FORJA'
  | 'QUARTEL'

export type Cultivo = 'TRIGO' | 'MILHO' | 'BATATA' | 'ABOBORA_DOURADA'

export type ModeloItem = 'ESPADA' | 'LANCA' | 'ARCO' | 'ARMADURA_COURO' | 'ARMADURA_FERRO'

export type CategoriaItem = 'ARMA' | 'ARMADURA'

export type StatusItem = 'DISPONIVEL' | 'RESERVADO' | 'EQUIPADO'

export type OrigemItem = 'FORJA' | 'MASMORRA'

export type TipoTropa = 'SOLDADO' | 'ARQUEIRO' | 'LANCEIRO'

// Slot de equipamento de uma unidade (ver SlotEquipamento.java): 9 slots em
// ordem de exibição; só ARMA e ARMADURA têm persistência hoje, os demais
// ficam sempre vazios (null) até extensão futura.
export type SlotEquipamento =
  | 'ARMA'
  | 'ARMADURA'
  | 'CABECA'
  | 'BOTA'
  | 'LUVA'
  | 'COLAR'
  | 'ANEL_1'
  | 'ANEL_2'
  | 'ANEL_3'

// Só existem estes dois status (ver StatusUnidade.java): uma unidade nunca
// fica "reservada" — ou está na vila, disponível para treino/batalha, ou
// está em uma masmorra (batalha em andamento).
export type StatusUnidade = 'DISPONIVEL' | 'EM_MASMORRA'

export type TipoInimigo = 'GOBLIN' | 'ESQUELETO_ARQUEIRO' | 'ORC' | 'TROLL'

// Categoria de uma ordem na fila de produção da vila (ver CategoriaOrdem.java).
// Não existe categoria `QUARTEL`: ordens de treino de tropa usam `TREINO`.
export type CategoriaOrdem = 'CONSTRUCAO' | 'FORJA' | 'TREINO'

export type StatusBatalha = 'EM_ANDAMENTO' | 'VITORIA' | 'DERROTA'

export type Lado = 'JOGADOR' | 'INIMIGO'

export type TipoAcaoCombate = 'MOVER' | 'ATACAR' | 'DEFENDER' | 'ENCERRAR_TURNO' | 'RENDER'

export type CodigoErro =
  | 'RECURSOS_INSUFICIENTES'
  | 'FILA_OCUPADA'
  | 'NIVEL_MAXIMO'
  | 'REQUISITO_NAO_ATENDIDO'
  | 'CANTEIRO_INEXISTENTE'
  | 'SEMENTE_INDISPONIVEL'
  | 'ITEM_INDISPONIVEL'
  | 'CAPACIDADE_EXERCITO'
  | 'MASMORRA_BLOQUEADA'
  | 'BATALHA_EM_ANDAMENTO'
  | 'UNIDADE_INDISPONIVEL'
  | 'UNIDADE_EM_MASMORRA'
  | 'ESQUADRAO_INVALIDO'
  | 'ACAO_INVALIDA'
  | 'BATALHA_ENCERRADA'
  | 'TURNO_DESATUALIZADO'
  | 'CONFLITO'
  | 'NAO_ENCONTRADO'
  | 'REQUISICAO_INVALIDA'
  // Não vem de CodigoErro.java (não é violação de regra de jogo): usado pelo
  // ErroApiHandler como fallback para qualquer exceção não tratada (500).
  | 'ERRO_INTERNO'

export interface CustoDto {
  recurso: TipoRecurso
  quantidade: number
}

export interface ProximoNivelDto {
  nivel: number
  custo: CustoDto[]
  tempoSegundos: number
}

export interface OrdemEmAndamentoDto {
  concluiEm: string
  tempoRestanteSegundos: number
}

export interface PredioDto {
  tipo: TipoPredio
  nivel: number
  nivelMaximo: number
  proximoNivel: ProximoNivelDto | null
  ordemEmAndamento: OrdemEmAndamentoDto | null
}

export interface CanteiroDto {
  posicao: number
  cultivo: Cultivo
  producaoComidaPorHora: number
}

export interface ItemDto {
  id: number
  modelo: ModeloItem
  nivel: number
  origem: OrigemItem
  status: StatusItem
  ataque: number
  defesa: number
  alcance: number | null
}

// Item completo de loot de masmorra (ver ItemDto.java, do pacote `jogo.api`,
// distinto de VilaDto.ItemDto acima): mesmos campos, mais `categoria` e sem
// `alcance` opcional (backend sempre envia o valor calculado, mesmo 0 para
// armaduras).
export interface ItemLootDto {
  id: number
  modelo: ModeloItem
  categoria: CategoriaItem
  nivel: number
  ataque: number
  defesa: number
  alcance: number
  origem: OrigemItem
  status: StatusItem
}

export interface UnidadeDto {
  id: number
  nome: string
  sobrenome: string
  // Sufixo de desambiguação entre unidades com mesmo nome+sobrenome na vila
  // (ver design.md — D11); 1 quando não há duplicata.
  ordinalNome: number
  // Nome pronto para exibição, já com sufixo quando ordinalNome > 1 (ex.:
  // "Ana Silva (2)"); o frontend não recalcula, só exibe.
  nomeExibicao: string
  tipo: TipoTropa
  status: StatusUnidade
  hp: number
  ataque: number
  defesa: number
  alcance: number
  movimento: number
  // LinkedHashMap com os 9 slots, na ordem de SlotEquipamento; slots vazios
  // vêm como null (ver design.md — D6).
  equipamento: Record<SlotEquipamento, ItemDto | null>
}

export interface OrdemDto {
  id: number
  categoria: CategoriaOrdem
  alvo: string
  nivel: number | null
  quantidade: number
  iniciadaEm: string
  concluiEm: string
}

export interface VilaDto {
  agora: string
  nome: string
  recursos: Record<TipoRecurso, number>
  capacidade: Record<TipoRecurso, number>
  producaoPorHora: Record<TipoRecurso, number>
  masmorraNivelLiberado: number
  batalhaAtivaId: number | null
  predios: PredioDto[]
  canteiros: CanteiroDto[]
  sementes: Record<Cultivo, number>
  itens: ItemDto[]
  unidades: UnidadeDto[]
  // Limite de unidades vivas do exército: `3 × nível do quartel` (ver spec
  // game-army — Capacidade do exército).
  capacidadeExercito: number
  ordens: OrdemDto[]
}

// Regra da forja (ver spec game-forge e design.md seção 7): custo por unidade
// no nível L = `custoBase × L`; custo total da ordem = custo por unidade ×
// quantidade. `custoBase` é o custo de forjar 1 unidade no nível 1 (backend
// envia como lista, igual a `ProximoNivelDto.custo`, não como mapa).
// `tempoBaseSegundos` já reflete a fórmula `tempoBase × L × qtd / velocidade`
// a ser aplicada pelo frontend só para exibição de progresso.
export interface ModeloItemCatalogoDto {
  categoria: CategoriaItem
  custoBase: CustoDto[]
  tempoBaseSegundos: number
}

// Tipo de tropa treinável no quartel (ver spec game-army — Tipos de tropa e
// atributos derivados): HP, defesa base e movimento vêm do tipo; ataque e
// alcance finais vêm da arma equipada, defesa final soma a da armadura.
export interface TropaCatalogoDto {
  armaExigida: ModeloItem
  hp: number
  defesaBase: number
  movimento: number
  comida: number
  tempoTreinoSegundos: number
  nivelMinimoQuartel: number
}

export interface CultivoCatalogoDto {
  comidaPorHora: number
  exigeSemente: boolean
  nivelMasmorraParaSemente: number
}

export interface InimigoCatalogoDto {
  hp: number
  ataque: number
  defesa: number
  alcance: number
  movimento: number
}

export interface PosicaoDto {
  x: number
  y: number
}

export interface MapaMasmorraDto {
  largura: number
  altura: number
  obstaculos: PosicaoDto[]
  posicoesJogador: PosicaoDto[]
  spawnsInimigos: Record<string, PosicaoDto>
}

export interface MasmorraCatalogoDto {
  nivel: number
  mapa: MapaMasmorraDto
  composicaoInimigos: TipoInimigo[]
}

export interface CatalogoDto {
  predios: Record<TipoPredio, ProximoNivelDto[]>
  cultivos: Record<Cultivo, CultivoCatalogoDto>
  modelosItem: Record<ModeloItem, ModeloItemCatalogoDto>
  tropas: Record<TipoTropa, TropaCatalogoDto>
  inimigos: Record<TipoInimigo, InimigoCatalogoDto>
  masmorras: Record<number, MasmorraCatalogoDto>
}

export interface ObstaculoDto {
  x: number
  y: number
}

export interface CombatenteDto {
  id: string
  lado: Lado
  // Id da Unidade (jogador) equipada por este combatente; `null` para
  // inimigos (ver BatalhaDto.CombatenteDto.java).
  unidadeId: number | null
  tipo: string
  x: number
  y: number
  hp: number
  hpMaximo: number
  ataque: number
  defesa: number
  alcance: number
  movimento: number
  vivo: boolean
  defendendo: boolean
  agiu: boolean
  moveu: boolean
}

export interface LootDto {
  recursos: Partial<Record<TipoRecurso, number>>
  sementes: Partial<Record<Cultivo, number>>
  itens: ItemLootDto[]
}

export interface BatalhaDto {
  id: number
  masmorraNivel: number
  status: StatusBatalha
  turno: number
  turnoMaximo: number
  largura: number
  altura: number
  obstaculos: ObstaculoDto[]
  combatentes: CombatenteDto[]
  log: string[]
  loot: LootDto | null
}

export interface MelhorarPredioRequest {
  tipo: TipoPredio
}

export interface PlantarRequest {
  cultivo: Cultivo
}

export interface ForjarRequest {
  modelo: ModeloItem
  nivel: number
  quantidade: number
}

export interface TreinarRequest {
  tipo: TipoTropa
  armaNivel: number
  armaduraModelo: ModeloItem
  armaduraNivel: number
  quantidade: number
}

export interface TrocarEquipamentoRequest {
  slot: SlotEquipamento
  itemId: number
}

export interface IniciarBatalhaRequest {
  unidadeIds: number[]
}

export interface AcaoCombateRequest {
  tipo: TipoAcaoCombate
  turno: number
  combatenteId: string
  x?: number | null
  y?: number | null
  alvoId?: string | null
}

export interface ErroApiDto {
  codigo: CodigoErro | string
  mensagem: string
}
