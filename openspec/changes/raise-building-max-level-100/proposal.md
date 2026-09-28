# Proposal

## Why

Uma edição local não commitada subiu `TipoPredio.NIVEL_MAXIMO` de 5 para 100 sem ajustar as fórmulas:
`GET /api/jogo/catalogo` passou a lançar `ArithmeticException: Overflow` em `Custo.paraNivel`
(`1,5^(N−1)` e `2^(N−1)` explodem acima de ~N60), a capacidade do armazém (`500 << (N−1)`) estoura o
`long`, a fazenda criaria canteiros além da constraint `1..5` e a constraint do banco foi editada
diretamente no `V3__jogo.sql` já aplicado. O jogo precisa de uma progressão longa (até o nível 100)
com curvas que não estourem, mantendo os níveis 1–5 idênticos aos atuais.

## What Changes

- Nível máximo de prédio passa de 5 para **100** (`NIVEL_MAXIMO`); a melhoria no nível 100 é rejeitada
  com 422 `NIVEL_MAXIMO`.
- Custo de construção: níveis 1–5 inalterados (`round_half_up(base × 1,5^(N−1))`); acima de 5,
  `round_half_up(base × 1,5^4 × (N/5)^p)`.
- Capacidade do armazém: níveis 1–5 inalterados (`500 × 2^(N−1)`); acima de 5,
  `round_half_up(8000 × (N/5)^p)`.
- Expoente `p` **configurável** em `app.jogo.expoente-curva` (variável `JOGO_EXPOENTE_CURVA`, padrão
  1,5; faixa 1,0–2,0, múltiplo de 0,25), validado na inicialização (a aplicação não sobe com valor
  inválido nem se algum custo/capacidade até o nível 100 estourar).
- Tempo de construção: níveis 1–5 inalterados (`tempoBase × 2^(N−1)`); acima de 5, linear
  `ceil(tempoBase × 16 × N / 5)`.
- Canteiros da fazenda: N até o nível 5; acima, `5 + ⌊(N−5)/5⌋` (24 no nível 100). Um canteiro novo só
  é criado quando o número aumenta; o plantio valida a posição contra o número de canteiros.
- Forja: nível máximo de item forjável passa a ser por faixas (N até 10; +1 a cada 5 níveis de 11 a 50;
  +1 a cada 10 níveis de 51 a 100 → 23 no nível 100). O nível máximo de item passa de 5 para **23**
  (forja, treino e troca de equipamento aceitam itens 1–23); atributos, custo e tempo de forja seguem
  as fórmulas lineares atuais.
- `VilaDto` ganha o campo `nivelMaximoForjavel`; a tela da forja limita o nível por ele e o quartel lista
  os níveis de arma/armadura presentes no inventário.
- Loot e masmorras permanecem limitados ao nível 5 (comportamento atual preservado explicitamente).
- `CalculadoraProducao.produzirAte` deixa de ter overflow silencioso: multiplicação checada e saturação
  na capacidade.
- Banco: a edição local em `V3__jogo.sql` é revertida e a nova migração `V5__niveis_estendidos.sql`
  troca as constraints (prédio 0..100, posição de canteiro 1..24, nível de item 1..23).
- **BREAKING** (API): `GET /api/jogo/catalogo` passa a trazer 100 níveis por prédio; `ForjarRequest` e
  `TreinarRequest` aceitam níveis até 23; `VilaDto` ganha `nivelMaximoForjavel`.
- Documentação técnica, GDD e novo ADR 0025 (curva de progressão configurável) atualizados.

## Capabilities

### New Capabilities

(nenhuma)

### Modified Capabilities

- `game-buildings`: nível máximo 100; fórmulas de custo/tempo em duas faixas; efeitos por nível de
  armazém, fazenda e forja; canteiro novo só quando o número aumenta.
- `game-village`: capacidade do armazém em duas faixas; produção sem overflow; `nivelMaximoForjavel`
  no `VilaDto`; catálogo com os níveis 1–100; novo requisito do expoente configurável.
- `game-farming`: número de canteiros por faixas (máx. 24) e validação da posição de plantio.
- `game-forge`: itens de nível 1–23; nível máximo forjável por faixas do nível da forja.
- `game-data`: constraints de nível de prédio (0–100), posição de canteiro (1–24) e nível de item
  (1–23); migração V5.
- `game-army`: treino em lote aceita arma/armadura de nível 1–23.
- `game-frontend`: forja limitada por `nivelMaximoForjavel`; quartel lista os níveis do inventário.

## Impact

- **Código (backend)**: `jogo/catalogo` (`Custo`, `TipoPredio`, `TipoRecurso`, `ModeloItem`, nova
  `CurvaNiveis`), `jogo/config/JogoProperties`, `jogo/api` (`JogoMapper`, `VilaDto`, `ForjarRequest`,
  `TreinarRequest`), `jogo/construcao/ConstrucaoService`, `jogo/economia` (`CalculadoraProducao`,
  `VilaService`, `AplicadorOrdens`), `jogo/fazenda/FazendaService`, `jogo/forja/ForjaService`,
  `jogo/masmorra` (`GeradorLoot`, `MasmorraService`).
- **Banco**: reversão da edição local em `V3__jogo.sql`; nova `V5__niveis_estendidos.sql`. Sem
  `flyway repair`: o banco de desenvolvimento será limpo manualmente pelo usuário.
- **Configuração**: `application.properties` (`app.jogo.expoente-curva`), `.env.example`,
  `docker-compose.yml` (serviço `app`).
- **Frontend**: `api/tipos.ts`, `views/ForjaView.vue`, `views/QuartelView.vue`.
- **Testes**: `CatalogoTest`, nova `CurvaNiveisTest`, `JogoPropertiesTest`, `CalculadoraProducaoTest`,
  `ConstrucaoServiceTest`, `FazendaServiceTest`, `ForjaServiceTest`, `GeradorLootTest`,
  `RepositoriosJogoTest`, `VilaControllerWebMvcTest`, `AcoesVilaControllerWebMvcTest`.
- **Documentação**: `docs/01`, `02`, `03`, `04`, `05`, `06`, `08`, `09`, `10`, `13`, `14`, `15`, `16`,
  `17`, `docs/README.md`, `12-gdd.md` e `12-gdd/12.3`, `12.4`, `12.5`, `12.6`, `12.14`, ADR 0017, índice
  de ADRs, novo ADR 0025, `README.md` da raiz.
- **Balanceamento**: itens de nível alto tornam as masmorras 1–5 muito mais fáceis (ver design —
  Open Questions).
