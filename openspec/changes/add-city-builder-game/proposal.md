# Proposal

## Why

O `login_base` tem autenticação de usuários e um frontend Vue mínimo, mas nenhuma funcionalidade de jogo. A adição de um **construtor de cidades tático** (city builder com mecânicas de estratégia) transforma a aplicação em uma experiência completa: o usuário autenticado gerencia uma vila (recursos, prédios, tropas) e enfrenta **masmorras com combate tático por turnos**. A mudança integra game design detalhado, persistência complexa de estado, API REST versionada e frontend reativo — servindo como foundation para games futuros e demonstrando o padrão de evolução do projeto.

## What Changes

- Modelo de dados do jogo no Postgres: 8 tabelas (`jogo_vilas`, `jogo_predios`, `jogo_canteiros`, `jogo_sementes`, `jogo_itens`, `jogo_unidades`, `jogo_ordens`, `jogo_batalhas`) com migração `V3__jogo.sql`, todas auditadas.
- Pacotes Java novos em `com.example.loginbase.jogo`: catálogo estático (prédios, recursos, sementes, itens, tropas, inimigos, masmorras com mapas), entidades e repositórios JPA com lock pessimista, economia (cálculo lazy de produção, sincronização sob demanda), serviços por domínio (construção, fazenda, forja, quartel, masmorra com motor de combate tático por turnos), e API REST (`/api/jogo/**`) com mapeamento de erros.
- Motor de **combate tático determinístico**: grade 8×8, movimento por BFS, ataque por alcance Manhattan, defesa com dobro, IA dos inimigos com desempates, 30 turnos máximos, vitória/derrota/render, loot em vitória.
- **Configuração de velocidade** (`JOGO_VELOCIDADE`): multiplicador de taxas de produção e inversor de tempos de construção/forja/treino, essencial para testes manuais.
- Ajustes de **segurança da API**: respostas 401 (anônimo em `/api/**`) sem redirecionamento, CSRF por cookie `XSRF-TOKEN` + cabeçalho `X-XSRF-TOKEN` (paralelo ao login Thymeleaf).
- Dependências: Jackson 3 (suporte JSON; já está), nenhuma nova no `pom.xml`.
- Frontend Vue 3 expandido: roteamento (`vue-router` em history mode), proxy do Vite para `/api` e `/login` (mesma origem, sem CORS), 6 rotas (vila, fazenda, forja, quartel, masmorras, batalha), componentes reativos (`PainelRecursos`, `CartaoPredio`, `GradeBatalha`), polling a cada 5 s, contagem regressiva de ordens, `docker-compose.yml` com variáveis `BACKEND_URL` e `JOGO_VELOCIDADE`.

## Capabilities

### New Capabilities

- `game-data`: Tabelas do jogo (V3), constraints de integridade (unique vila por usuário, fila de 1 por categoria, uma batalha ativa por vila), auditoria.
- `game-village`: Criação automática da vila (estado inicial), sincronização lazy de produção em tempo real (avanço por trechos entre conclusões de ordens), consomição de recursos respeitando capacidade, isolamento por usuário.
- `game-buildings`: Prédios (8 tipos, níveis 1–5), custos/tempos por nível conforme fórmula, efeitos (limite pelo centro, produção de recursos, canteiros, capacidade do exército), validação (pré-requisitos, limite de nível).
- `game-farming`: Fazenda com canteiros (nº = nível), cultivos (trigo/milho/batata/abóbora com produção escalonada), sementes (obtidas em masmorras), plantio instantâneo.
- `game-forge`: Forja com 5 modelos (espada, lança, arco, armaduras couro/ferro), receitas de custo e tempo por nível, itens com nível próprio (1–5) e atributos derivados (ataque/defesa/alcance), fila de 1 ordem.
- `game-army`: Quartel com 3 tipos de tropa (soldado, arqueiro, lanceiro), liberação por nível, atributos derivados (arma+armadura → ataque/defesa/HP/movimento), treino consome comida e itens, capacidade escalonada.
- `game-dungeon-combat`: Masmorras níveis 1–5 (gradualmente liberadas), mapa 8×8 com obstáculos/spawns, inimigos com tipos fixos (goblin/esqueleto/orc/troll), composição por nível, início com até 4 unidades, turno do jogador (mover/atacar/defender/encerrar turno), IA dos inimigos (seleção de alvo por distância/HP, movimento + ataque no mesmo turno), dano determinístico, defesa dobrada,HP restaurado entre batalhas.
- `game-dungeon-loot`: Recursos garantidos por nível (40N comida / 50N madeira / 50N pedra / 20N ferro), rolagens de loot (sementes/materiais/itens com distribuição de chances por nível), liberação automática do próximo nível ao vencer.
- `game-frontend`: Navegação por rotas (`/`, `/fazenda`, `/forja`, `/quartel`, `/masmorras`, `/batalhas/:id`), painel de recursos fixo, prédios com card e contagem, fazenda com canteiros e sementes, forja com cálculo de custo, quartel com seleção de tropas, masmorras com composição e entrada, batalha tática com grade clicável, log e resultado com loot, polling a cada 5 s, mensagens de erro (422/409 em Toast).

### Modified Capabilities

- `user-authentication`: Adição de requisitos de API (respostas anônimas a `/api/**` MUST receber 401 sem redirecionamento; request cache MUST NOT guardar requisições `/api/**`; CSRF MUST ser transmitido por cookie `XSRF-TOKEN` + cabeçalho `X-XSRF-TOKEN` em POSTs, paralelo ao formulário Thymeleaf).
- `frontend-app`: Mudança da página inicial: de "Seja bem-vindo" (boas-vindas estáticas) para a tela interativa da vila (exibição da vila do usuário autenticado); adição de proxy de desenvolvimento do Vite para `/api`, `/login`, `/logout`, `/css`, `/js`, `/images` (preservando Host para redirects); roteamento em history mode (em vez de hash).

## Impact

- **Banco de dados**: nova migração `V3__jogo.sql` (8 tabelas, ~100 linhas), aplicada automaticamente ao iniciar sobre banco V1/V2.
- **Backend (Spring Boot)**:
  - Novos pacotes: `com.example.loginbase.jogo` (catálogo, domínio, economia, construção, fazenda, forja, quartel, masmorra, api).
  - Novos arquivos: ~40 classes Java (entidades, repositórios, serviços, controllers, DTOs, conversor JSON, exception handler, config).
  - Modificados: `SecurityConfig` (adição de handler de erro 401 para `/api/**`, request cache), `application.properties` (propriedade `app.jogo.velocidade`).
  - Dependências: nenhuma nova (Jackson 3 já está incluído via `spring-boot-starter-webmvc`).
- **Frontend (Vue 3)**:
  - Nova dependência: `vue-router` (já compatível com Vue 3.5).
  - Novos arquivos: router, 6 views, 3 componentes principais, composable de estado, módulo de HTTP com CSRF e handler de 401, tipos TypeScript.
  - Modificados: `vite.config.ts` (proxy), `docker-compose.yml` (`BACKEND_URL`, variável de velocidade, `extra_hosts`), `.env.example`.
- **Configuração**:
  - `.env.example`: `BACKEND_URL` (padrão `http://localhost:8080`; em container `http://host.docker.internal:8080`), `JOGO_VELOCIDADE` (padrão 1).
  - `docker-compose.yml`: serviço `frontend` ganha `BACKEND_URL`, `extra_hosts`, e ambas as variáveis (frontend lê `BACKEND_URL`, app lê `JOGO_VELOCIDADE`).
- **Testes**: ~15 classes de teste (puros: catálogo, calculadora, motor; com Spring: entidades, serviços contra Postgres do compose, controllers; suporte: relógio ajustável, aleatório sequência).

