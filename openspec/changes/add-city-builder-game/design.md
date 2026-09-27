# Design

## Context

Ver proposal.md – Why. Estado atual e restrições relevantes:

- Spring Boot 4.1.1 (Spring Security 7.1.1, Hibernate 7) com Java 25. O `pom.xml` já traz Jackson 3, Lombok, Spring Data JPA, Spring Security, Flyway.
- Autenticação existente: form login Thymeleaf em `/login` (params `login`/`senha`), sessão HTTP (`JSESSIONID`), e-mail como `username`, `SecurityConfig` redireciona anônimo para login (exceto rotas públicas `/css/**`, `/js/**`, `/images/**`, `/favicon.ico`, `/error`).
- Banco: Postgres com migrações V1 (controle de acesso), V2 (admin inicial) → próxima: V3 (jogo).
- Frontend: Vue 3.5 + Vite 8 + PrimeVue 5 (Aura) em `frontend/`, sem router, sem estado global (Pinia). Container na porta 5173; backend roda host na 8080 ou em container.
- O `CsrfConfigurer#spa()` existe no Spring Security 7.1.1; `EntidadeAuditavel` já está no pacote `auditoria`.
- `LoginBaseApplicationTests` (`@SpringBootTest`) exige Postgres do compose (`make up`). Sem Testcontainers.

## Goals / Non-Goals

**Goals:**
- Sistema de jogo completo, autossuficiente: vila + recursos + prédios + fazenda + forja + quartel + masmorras com combate tático determinístico.
- Cálculo lazy de produção sob demanda (sem threads de background), integrado com persistência e lock pessimista por vila.
- API REST versionada, idempotente, com mapeamento de erros de regra.
- Frontend reativo com polling e contagem regressiva, sem cálculo de regras (tudo vem do servidor).
- Testes: puros (catálogo, calculadora, motor) e com Spring (entidades, serviços contra Postgres, controllers).

**Non-Goals:**
- CRUD/telas de configuração de jogo, balanceamento dinâmico.
- Gravidade, física, tile-map gerado proceduralmente.
- Sem manutenção de tropas (upkeep), sem cancelamento de ordens, sem colheita (plantio instantâneo), sem crítico/esquiva (dano determinístico), sem limite de sessões simultâneas, sem MFA.
- Frontend com Testcontainers; suporte a Testcontainers fica para outra change.
- Tela de login em Vue (continua em Thymeleaf).

## Decisions

### 1. Game Design — Recursos

Armazenados em **milésimos** (`bigint`, 1 unidade = 1000) para acumular produção fracionária; API mostra `floor(valor/1000)`.

| Código | Nome | Origem |
|---|---|---|
| `COMIDA` | Comida | Fazenda (canteiros), loot |
| `MADEIRA` | Madeira | Serraria, loot |
| `PEDRA` | Pedra | Pedreira, loot |
| `FERRO` | Ferro (minério) | Mina de ferro, loot |

**Capacidade** por recurso (igual para todos): Armazém nível N = `500 × 2^(N−1)`:
- N1 500 · N2 1000 · N3 2000 · N4 4000 · N5 8000

Produção e loot nunca ultrapassam capacidade (excedente perdido).

### 2. Velocidade do Jogo (Configuração)

Propriedade `app.jogo.velocidade=${JOGO_VELOCIDADE:1}` (inteiro ≥ 1; valor inválido → falha). Taxas efetivas = taxa × v; tempos efetivos = `ceil(tempo / v)` segundos.

### 3. Prédios

Um de cada tipo por vila; níveis 0–5. Custo para **atingir** nível N = `round_half_up(base × 1,5^(n−1))` por recurso; tempo = `tempoBase × 2^(n−1)` s.

| Prédio | N1 | N2 | N3 | N4 | N5 |
|---|---|---|---|---|---|
| `CENTRO_VILA` | M150 P150 · 120 | M225 P225 · 240 | M338 P338 · 480 | M506 P506 · 960 | M759 P759 · 1920 |
| `ARMAZEM` | M100 P60 · 60 | M150 P90 · 120 | M225 P135 · 240 | M338 P203 · 480 | M506 P304 · 960 |
| `FAZENDA` | M80 P40 · 60 | M120 P60 · 120 | M180 P90 · 240 | M270 P135 · 480 | M405 P203 · 960 |
| `SERRARIA` | M60 P40 · 60 | M90 P60 · 120 | M135 P90 · 240 | M203 P135 · 480 | M304 P203 · 960 |
| `PEDREIRA` | M80 P20 · 60 | M120 P30 · 120 | M180 P45 · 240 | M270 P68 · 480 | M405 P101 · 960 |
| `MINA_FERRO` | M100 P80 · 90 | M150 P120 · 180 | M225 P180 · 360 | M338 P270 · 720 | M506 P405 · 1440 |
| `FORJA` | M120 P100 F40 · 120 | M180 P150 F60 · 240 | M270 P225 F90 · 480 | M405 P338 F135 · 960 | M608 P506 F203 · 1920 |
| `QUARTEL` | M150 P120 F40 · 120 | M225 P180 F60 · 240 | M338 P270 F90 · 480 | M506 P405 F135 · 960 | M759 P608 F203 · 1920 |

**Efeitos** por nível N:

| Prédio | Efeito |
|---|---|
| `CENTRO_VILA` | Nível máximo de todos os outros = nível do centro. |
| `ARMAZEM` | Capacidade `500 × 2^(N−1)` por recurso. |
| `FAZENDA` | Nº de canteiros = N (canteiro novo nasce com `TRIGO`). |
| `SERRARIA` | +`30 × N` madeira/h. |
| `PEDREIRA` | +`20 × N` pedra/h. |
| `MINA_FERRO` | +`10 × N` ferro/h. |
| `FORJA` | Forja itens nível ≤ N. |
| `QUARTEL` | Capacidade exército = `3 × N` unidades; libera tropas (N1 Soldado, N2 +Arqueiro, N3 +Lanceiro). |

**Validação** (nesta ordem):
1. Nível = 5 → `NIVEL_MAXIMO`.
2. Prédio ≠ `CENTRO_VILA` e nível alvo > nível do centro → `REQUISITO_NAO_ATENDIDO`.
3. `FORJA` exige `MINA_FERRO ≥ 1`; `QUARTEL` exige `FORJA ≥ 1` → `REQUISITO_NAO_ATENDIDO`.
4. Construção em andamento → `FILA_OCUPADA`.
5. Recursos insuficientes → `RECURSOS_INSUFICIENTES`.

Recursos debitados no início; nível sobe na conclusão. Sem cancelamento.

### 4. Estado Inicial da Vila

Criada automaticamente no 1º acesso:
- Nome: `Vila de <nome do usuário>`.
- Prédios: `CENTRO_VILA` 1, `ARMAZEM` 1, `FAZENDA` 1, `SERRARIA` 1, `PEDREIRA` 1, `MINA_FERRO` 0, `FORJA` 0, `QUARTEL` 0.
- Recursos: COMIDA 300, MADEIRA 400, PEDRA 300, FERRO 50 (capacidade 500).
- 1 canteiro (posição 1) com `TRIGO`.
- Produção: comida 20/h, madeira 30/h, pedra 20/h, ferro 0/h.

### 5. Fazenda — Cultivos e Sementes

| Cultivo | Comida/h | Semente | Obtida em |
|---|---|---|---|
| `TRIGO` | 20 | não exige | — |
| `MILHO` | 30 | 1 semente MILHO | masmorra N ≥ 1 |
| `BATATA` | 45 | 1 semente BATATA | masmorra N ≥ 2 |
| `ABOBORA_DOURADA` | 70 | 1 semente ABOBORA | masmorra N ≥ 4 |

Plantio instantâneo, consome semente (exceto TRIGO). Erro: canteiro inexistente → `CANTEIRO_INEXISTENTE`; sem semente → `SEMENTE_INDISPONIVEL`.

### 6. Itens (Armas e Armaduras)

| Modelo | Categoria | Ataque | Defesa | Alcance |
|---|---|---|---|---|
| `ESPADA` | ARMA | 6 + 2(L−1) | 0 | 1 |
| `LANCA` | ARMA | 5 + 2(L−1) | 0 | 1 |
| `ARCO` | ARMA | 4 + 2(L−1) | 0 | 3 |
| `ARMADURA_COURO` | ARMADURA | 0 | 2 + 1(L−1) | — |
| `ARMADURA_FERRO` | ARMADURA | 0 | 3 + 2(L−1) | — |

Status: `DISPONIVEL`, `RESERVADO` (treino), `EQUIPADO` (em unidade). Origem: `FORJA` ou `MASMORRA`.

### 7. Forja — Receitas

Custo por unidade nível L = `base × L`; tempo = `tempoBase × L` s.

| Modelo | Custo base | Tempo base |
|---|---|---|
| `ESPADA` | M20 F30 | 60 s |
| `LANCA` | M40 F20 | 60 s |
| `ARCO` | M50 F5 | 60 s |
| `ARMADURA_COURO` | C20 M10 F5 | 45 s |
| `ARMADURA_FERRO` | M10 F40 | 90 s |

Ordem: `{modelo, nivel, quantidade 1..5}`; custo total = custo × quantidade; tempo = `ceil(tempoBase × L × quantidade / v)`. Todos entregues juntos, status `DISPONIVEL`, origem `FORJA`. Uma ordem por vez. Erros: forja nível 0 ou `nivel > nível da forja` → `REQUISITO_NAO_ATENDIDO`; quantidade/nível 1–5 → 400; recursos → `RECURSOS_INSUFICIENTES`.

### 8. Tropas (Quartel)

Unidade = tipo + 1 arma (modelo exigido) + 1 armadura, ambas `DISPONIVEL`.

| Tipo | Arma | HP | Defesa | Movimento | Comida | Tempo | Quartel mín. |
|---|---|---|---|---|---|---|---|
| `SOLDADO` | ESPADA | 30 | 1 | 3 | 50 | 60 s | 1 |
| `ARQUEIRO` | ARCO | 22 | 0 | 3 | 50 | 60 s | 2 |
| `LANCEIRO` | LANCA | 40 | 2 | 2 | 60 | 75 s | 3 |

Atributos: ataque = arma, alcance = arma, defesa = base + armadura, HP e movimento = tipo. Ordem: 1 unidade, uma por vez. Início: debita comida, marca itens `RESERVADO`; conclusão: cria unidade `DISPONIVEL`, marca itens `EQUIPADO`. Itens são consumidos. Erros: quartel abaixo mínimo → `REQUISITO_NAO_ATENDIDO`; item inválido → `ITEM_INDISPONIVEL`; unidades + treino ≥ `3 × nível` → `CAPACIDADE_EXERCITO`; comida → `RECURSOS_INSUFICIENTES`.

### 9. Masmorras — Níveis, Mapa, Inimigos

**Níveis 1–5**. Jogador entra ≤ `masmorra_nivel_liberado`; vitória no N → `masmorra_nivel_liberado = max(atual, min(5, N+1))`.

**Mapa 8×8** (igual para todos):
```
   x: 0 1 2 3 4 5 6 7
y0:   . S4 . S1 . . S5 .
y1:   . . S2 . . S3 . .
y2:   . . . # # . . .
y3:   . # . . . . # .
y4:   . # . . . . # .
y5:   . . . # # . . .
y6:   . . . . . . . .
y7:   . . P P P P . .
```
- Obstáculos: (3,2) (4,2) (1,3) (6,3) (1,4) (6,4) (3,5) (4,5).
- Posições do jogador: (2,7) (3,7) (4,7) (5,7).
- Spawns inimigos: S1 (3,0) · S2 (2,1) · S3 (5,1) · S4 (1,0) · S5 (6,0).

**Inimigos**:

| Tipo | HP | Ataque | Defesa | Alcance | Movimento |
|---|---|---|---|---|---|
| `GOBLIN` | 15 | 6 | 1 | 1 | 3 |
| `ESQUELETO_ARQUEIRO` | 12 | 6 | 0 | 3 | 2 |
| `ORC` | 30 | 9 | 3 | 1 | 2 |
| `TROLL` | 70 | 13 | 5 | 1 | 2 |

**Composição**:

| Nível | Inimigos |
|---|---|
| 1 | GOBLIN, GOBLIN, GOBLIN |
| 2 | ESQUELETO_ARQUEIRO, GOBLIN, GOBLIN, GOBLIN |
| 3 | ORC, GOBLIN, GOBLIN, ESQUELETO_ARQUEIRO, ESQUELETO_ARQUEIRO |
| 4 | ORC, ORC, ORC, ESQUELETO_ARQUEIRO, ESQUELETO_ARQUEIRO |
| 5 | TROLL, ORC, ORC, ESQUELETO_ARQUEIRO, ESQUELETO_ARQUEIRO |

### 10. Combate Tático — Regras

**Início**: 1–4 unidades distintas, `DISPONIVEL` (senão `ESQUADRAO_INVALIDO`/`UNIDADE_INDISPONIVEL`). Uma batalha ativa por vila (senão `BATALHA_EM_ANDAMENTO`). Unidades → `EM_MASMORRA`, HP cheio. Turno 1, vez do jogador.

**Turno do Jogador** — cada combatente vivo, uma vez:
- `MOVER {id, x, y}`: não moveu/agiu. Destino livre (sem obstáculo/combatente), alcançável por BFS ≤ movimento.
- `ATACAR {id, alvoId}`: não agiu. Alvo inimigo vivo ≤ alcance (Manhattan). Após agir, não pode mover.
- `DEFENDER {id}`: não agiu; defesa dobrada até próximo turno do jogador.
- `ENCERRAR_TURNO`: executa IA dos inimigos, incrementa turno (se batalha continua), zera flags, remove "defendendo".
- `RENDER`: imediatamente → `DERROTA`.

**Dano**: `max(1, ataque − defesaEfetiva)`; `defesaEfetiva = defesa × 2` se defendendo. HP ≤ 0 → morto.

**IA dos Inimigos** (turno do jogador, em ordem I1..I5, vivos):
1. Alvo = menor distância Manhattan; empate → menor HP → menor id.
2. Se no alcance → ataca.
3. Senão → move (BFS, casas livres) minimizando distância; empate → menor y → menor x. Depois, se no alcance, ataca.

**Fim**:
- Todos inimigos mortos → `VITORIA`, sobreviventes `DISPONIVEL`.
- Todas unidades mortas, `RENDER`, ou turno 30 com inimigos vivos → `DERROTA`. Unidades mortas excluídas com itens; sobreviventes → `DISPONIVEL`.

**Persistência**: estado (combatentes, posições, HP, flags, turno) em JSON; log textual acumulado.

### 11. Loot (Vitória Nível N)

- Garantido: COMIDA `40N`, MADEIRA `50N`, PEDRA `50N`, FERRO `20N`.
- Rolagens: `1 + ceil(N/2)` → N1: 2 · N2: 2 · N3: 3 · N4: 3 · N5: 4.
- Cada rolagem (chamar `Aleatorio.proximoInt(limite)` em ordem):
  1. `d = proximoInt(100)`.
  2. `d` 0–34 → **semente**: MILHO 60 (N≥1), BATATA 30 (N≥2), ABOBORA 10 (N≥4); escolhe na ordem, +1.
  3. `d` 35–59 → **material**: FERRO `30N`.
  4. `d` 60–99 → **item**: modelo `proximoInt(5)` (ESPADA, LANCA, ARCO, ARMADURA_COURO, ARMADURA_FERRO); nível = `min(5, N + proximoInt(2))`; origem `MASMORRA`.

Recursos respeitam capacidade (excesso perdido). Loot registrado na batalha (JSON).

### 12. Arquitetura — Pacotes (`com.example.loginbase.jogo`)

| Pacote | Conteúdo |
|---|---|
| `jogo` | `RegraJogoException(CodigoErro, mensagem)`, `CodigoErro` (enum), `RecursoNaoEncontradoException`. |
| `jogo.config` | `JogoConfig` (Bean `Clock`, Bean `Aleatorio`), `JogoProperties` (`@ConfigurationProperties("app.jogo")`, `velocidade` ≥ 1), `Aleatorio` (interface), `AleatorioPadrao` (RandomGenerator). |
| `jogo.catalogo` | Enums/records: `TipoRecurso`, `Custo`, `TipoPredio`, `Cultivo`, `ModeloItem`, `TipoTropa`, `TipoInimigo`, `MapaMasmorra`, `Masmorra` (record), `CatalogoMasmorras` (níveis 1–5). |
| `jogo.dominio` | Entidades JPA + repositórios: `Vila`, `Predio`, `Canteiro`, `EstoqueSemente`, `Item`, `Unidade`, `Ordem`, `Batalha`, enums `StatusItem`, `OrigemItem`, `StatusUnidade`, `StatusBatalha`, `CategoriaOrdem`. |
| `jogo.economia` | `Estoque`, `CalculadoraProducao` (pura), `VilaService`, `AplicadorOrdens`. |
| `jogo.construcao` | `ConstrucaoService`. |
| `jogo.fazenda` | `FazendaService`. |
| `jogo.forja` | `ForjaService`. |
| `jogo.quartel` | `QuartelService`. |
| `jogo.masmorra.combate` | Motor puro: `EstadoBatalha`, `Combatente`, `Lado`, `Posicao`, `AcaoCombate`, `MotorCombate`, `Caminhos` (BFS). |
| `jogo.masmorra` | `GeradorLoot` (puro), `Loot` (record), `MasmorraService`. |
| `jogo.api` | `UsuarioAtual`, controllers, DTOs, `JogoMapper`, `ErroApiHandler`. |

Catálogo em **código Java** (enums + records), não YAML — tipado, testável. Exposto por `GET /api/jogo/catalogo`.

### 13. Banco — `V3__jogo.sql`

PKs `bigint generated always as identity`, colunas de auditoria (`criado_em timestamptz`, `criado_por varchar(150)`, `alterado_em`, `alterado_por`, todas `not null`). Enums como `varchar(EnumType.STRING)`. Índice em todo `vila_id`.

| Tabela | Colunas principais | Restrições |
|---|---|---|
| `jogo_vilas` | `usuario_id`, `nome varchar(100)`, `comida`, `madeira`, `pedra`, `ferro` (bigint, milésimos), `recursos_atualizados_em timestamptz`, `masmorra_nivel_liberado int default 1` | FK usuarios, `unique(usuario_id)`, checks recursos ≥ 0 e nível 1–5 |
| `jogo_predios` | `vila_id`, `tipo varchar(30)`, `nivel int` | `unique(vila_id,tipo)`, `check nivel between 0 and 5` |
| `jogo_canteiros` | `vila_id`, `posicao int`, `cultivo varchar(30)`, `plantado_em timestamptz` | `unique(vila_id,posicao)`, `check posicao between 1 and 5` |
| `jogo_sementes` | `vila_id`, `cultivo varchar(30)`, `quantidade int` | `unique(vila_id,cultivo)`, `check quantidade >= 0` |
| `jogo_itens` | `vila_id`, `modelo varchar(30)`, `nivel int`, `origem varchar(20)`, `status varchar(20)` | `check nivel between 1 and 5` |
| `jogo_unidades` | `vila_id`, `tipo varchar(20)`, `arma_item_id`, `armadura_item_id`, `status varchar(20)` | FKs itens, `unique` em cada item |
| `jogo_ordens` | `vila_id`, `categoria varchar(20)`, `alvo varchar(30)`, `nivel int null`, `quantidade int default 1`, `arma_item_id null`, `armadura_item_id null`, `iniciada_em`, `conclui_em` | `unique(vila_id,categoria)` (fila 1), FKs itens |
| `jogo_batalhas` | `vila_id`, `masmorra_nivel int`, `status varchar(20)`, `turno int`, `estado text`, `log text`, `loot text null`, `iniciada_em`, `finalizada_em null`, `version bigint` | índice parcial `(vila_id) where status = 'EM_ANDAMENTO'` |

`estado`/`log`/`loot` em `text` com JSON serializado por `AttributeConverter` (Jackson 3).

### 14. Cálculo Preguiçoso do Tempo

`VilaService.sincronizar(vila, agora)` — chamado no início de toda leitura/comando, dentro da transação, com vila travada:
```
ordensVencidas = ordens com concluiEm <= agora, ordenadas por (concluiEm, id)
para cada ordem: produzirAte(vila, ordem.concluiEm); aplicador.aplicar(ordem); excluir ordem
produzirAte(vila, agora)

produzirAte(vila, t):
  dtMs = t − vila.recursosAtualizadosEm; se dtMs <= 0: retorna
  para cada recurso: ganho = taxaHora × velocidade × dtMs / 3600 (long, milésimos, floor)
                     novo = atual >= cap×1000 ? atual : min(cap×1000, atual + ganho)
  vila.recursosAtualizadosEm = t
```

Taxas/capacidade vêm dos níveis vigentes naquele trecho.

### 15. Concorrência

- **Lock pessimista**: `VilaRepository.findByUsuarioIdParaAtualizacao` com `@Lock(PESSIMISTIC_WRITE)` (SELECT ... FOR UPDATE).
- Criação concorrente: `unique(usuario_id)` + reler em `DataIntegrityViolationException` (nova transação `REQUIRES_NEW`).
- `jogo_batalhas.version` (`@Version`) como defesa extra; `ObjectOptimisticLockingFailureException` → 409 `CONFLITO`. Ações de combate exigem `turno` igual ao atual (409).

### 16. Serviços (Assinaturas)

- `VilaService`: `Vila obterParaAtualizacao(long usuarioId)` (cria se não existir, trava, sincroniza); `EstadoVila consultar(long usuarioId)`.
- `ConstrucaoService.melhorar(long usuarioId, TipoPredio tipo)`.
- `FazendaService.plantar(long usuarioId, int posicao, Cultivo cultivo)`.
- `ForjaService.forjar(long usuarioId, ModeloItem modelo, int nivel, int quantidade)`.
- `QuartelService.treinar(long usuarioId, TipoTropa tipo, long armaId, long armaduraId)`.
- `MasmorraService.iniciar(long usuarioId, int nivel, List<Long> unidadeIds) → Batalha`; `consultar(long usuarioId, long batalhaId)`; `agir(long usuarioId, long batalhaId, AcaoCombate)`.

Todos `@Transactional`. Violações → `RegraJogoException(CodigoErro, mensagem pt-BR)`.

### 17. API REST (JSON, `/api/jogo`, sessão + CSRF)

| Verbo | Rota | Request | Resposta |
|---|---|---|---|
| GET | `/api/jogo/catalogo` | — | 200 `CatalogoDto` |
| GET | `/api/jogo/vila` | — | 200 `VilaDto` (cria no 1º acesso) |
| POST | `/api/jogo/predios/{tipo}/melhorar` | — | 200 `VilaDto` |
| POST | `/api/jogo/canteiros/{posicao}/plantar` | `{ "cultivo": "MILHO" }` | 200 `VilaDto` |
| POST | `/api/jogo/forja/ordens` | `{ "modelo": "ESPADA", "nivel": 2, "quantidade": 1 }` | 200 `VilaDto` |
| POST | `/api/jogo/quartel/ordens` | `{ "tipo": "SOLDADO", "armaId": 1, "armaduraId": 2 }` | 200 `VilaDto` |
| POST | `/api/jogo/masmorras/{nivel}/batalhas` | `{ "unidadeIds": [3,4] }` | 201 `BatalhaDto` |
| GET | `/api/jogo/batalhas/{id}` | — | 200 `BatalhaDto` |
| POST | `/api/jogo/batalhas/{id}/acoes` | `{ "tipo": "MOVER", "turno": 1, "combatenteId": "J3", "x": 2, "y": 5, "alvoId": null }` | 200 `BatalhaDto` |

Erros (`ErroApiHandler`) — `{ "codigo": "...", "mensagem": "..." }`:
- `RegraJogoException` → 422 (códigos: `RECURSOS_INSUFICIENTES`, `FILA_OCUPADA`, `NIVEL_MAXIMO`, `REQUISITO_NAO_ATENDIDO`, `CANTEIRO_INEXISTENTE`, `SEMENTE_INDISPONIVEL`, `ITEM_INDISPONIVEL`, `CAPACIDADE_EXERCITO`, `MASMORRA_BLOQUEADA`, `BATALHA_EM_ANDAMENTO`, `UNIDADE_INDISPONIVEL`, `ESQUADRAO_INVALIDO`, `ACAO_INVALIDA`, `BATALHA_ENCERRADA`), exceto `TURNO_DESATUALIZADO` → 409.
- Lock otimista → 409 `CONFLITO`; 404 `NAO_ENCONTRADO`; 400 `REQUISICAO_INVALIDA`.

### 18. Segurança — Integração com Autenticação Existente

Ajustes em `SecurityConfig`:
1. `exceptionHandling.defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(UNAUTHORIZED), PathPatternRequestMatcher.withDefaults().matcher("/api/**"))` → anônimo em `/api/**` recebe **401** sem redirecionamento.
2. `requestCache`: `HttpSessionRequestCache` que **não salva** requisições `/api/**`.
3. `csrf(csrf -> csrf.spa())`: token em cookie `XSRF-TOKEN` (legível), enviado em `X-XSRF-TOKEN`; formulário Thymeleaf continua com `_csrf`.

Usuário: `UsuarioAtual.id(Authentication)` → `usuarioRepository.findByEmail(auth.getName())`. Cada vila só pelo dono. Nenhuma permissão específica.

### 19. Frontend — Estrutura e Integração

**Dependência nova**: `vue-router`. Sem Pinia (estado em composable singleton).

**Proxy do Vite** (mesma origem, sem CORS): `/api`, `/login`, `/logout`, `/css`, `/js`, `/images` → `${BACKEND_URL ?? 'http://localhost:8080'}`, `changeOrigin: false`.

**docker-compose.yml**: serviço `frontend` ganha `BACKEND_URL`, `extra_hosts`. Serviço `app`: `JOGO_VELOCIDADE`.

**Estrutura**:
- `src/api/http.ts` — fetch com credenciais, lê cookie `XSRF-TOKEN`, envia `X-XSRF-TOKEN`, trata 401.
- `src/api/tipos.ts` — tipos TypeScript (CatalogoDto, VilaDto, BatalhaDto, requests).
- `src/api/jogo.ts` — endpoints.
- `src/composables/useVila.ts` — estado singleton, polling a cada 5 s.
- `src/router/index.ts` — rotas history mode: `/`, `/fazenda`, `/forja`, `/quartel`, `/masmorras`, `/batalhas/:id`.
- `App.vue` — Menubar, PainelRecursos fixo, Toast, RouterView.
- Componentes: `PainelRecursos.vue`, `CartaoPredio.vue`, `GradeBatalha.vue`.
- Views: VilaView, FazendaView, ForjaView, QuartelView, MasmorrasView, BatalhaView.

### 20. Testes

- **Puros** (JUnit 5 + AssertJ): catálogo, `CalculadoraProducao`, `MotorCombate`, `GeradorLoot`.
- **Com Spring**: `@DataJpaTest` contra Postgres do compose (mesma premissa de `LoginBaseApplicationTests`), `@WebMvcTest` dos controllers com serviços mockados.
- **Suporte**: `RelogioAjustavel` (Clock mutável), `AleatorioSequencia`.

### 21. Recomendações Padrão para Dúvidas Abertas (Seção E do Plano)

1. **Frontend ↔ backend**: Proxy do Vite (mesma origem, sem CORS) — reaproveitando login Thymeleaf e cookie de sessão.
2. **Login**: Página Thymeleaf existente servida via proxy. Login em Vue fica para outra change.
3. **Multiplicador `JOGO_VELOCIDADE`**: Incluir, padrão 1 — essencial para testes.
4. **Testes de serviço**: Contra Postgres do compose (sem Testcontainers) — mesma premissa do `LoginBaseApplicationTests`.
5. **Simplificações de jogo**: Manter (sem upkeep, sem cancelamento, fila 1, plantio instantâneo, dano determinístico, 1 mapa, HP restaurado) — ciclo básico completo; cada simplificação é extensão futura.
6. **Página `/` Thymeleaf**: Continua existindo no backend (8080), mas via 5173 o `/` é o SPA.

## Risks / Trade-offs

- [Cálculo lazy pode introduzir discrepâncias se a data/hora do servidor mudar durante operação] → Documentado; cálculos sempre usam `clock.instant()` (injetável, mockável).
- [Lock pessimista por vila serializa todas as ações do usuário] → Aceitável (1 vila por usuário, ações são rápidas). Alternativa: lock granular por prédio/ordem.
- [JSON serializado em `text` sem índices] → Suficiente (estado lido inteiro em cada ação). JSONB seria melhor em produção.
- [IA dos inimigos determinística, sem estratégia adaptativa] → Por design (playtests reproduzíveis). Estratégia futura.
- [Proxy do Vite não funciona em produção (build SPA servida por backend)] → Esperado (proxy = dev only). Produção: SPA em CDN ou servida por backend com caminho `/spa/**`.
- [Plantio instantâneo ignora ciclos sazonais] → Simplificação intencional; ciclos sazonais = extensão futura.
- [Um mapa para todos os níveis de masmorra] → Simplificação; mapas por nível = extensão.

