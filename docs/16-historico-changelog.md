# Histórico e Changelog

| Campo | Valor |
|---|---|
| Versão | 1.1.0 |
| Data | 2026-09-27 |
| Status | Vigente — baseline do commit `454ae58` + previsto da change `add-frontend-build` (aberta) |
| Modelo/norma | Keep a Changelog 1.1 (adaptado por change OpenSpec) |
| Público | todos |

> Parte da [documentação do login_base](README.md). Registro completo de mudanças, organizadas por change OpenSpec com commits, funcionalidades e estatísticas de desenvolvimento.

---

## 1. Linha do Tempo

| # | Change | Status | Data | Commits | Tasks | Agentes | Tokens |
|---|---|---|---|---|---|---|---|
| 1 | setup-java-project | Arquivada | 2026-09-23 | 1 | 22 | n/d | n/d |
| 2 | add-docker-compose-profiles | Arquivada | 2026-09-23 | 1 | 6 | n/d | n/d |
| 3 | add-vue-frontend | Arquivada | 2026-09-23 | 1 | 11 | n/d | n/d |
| 4 | add-subagent-dev-skill | Arquivada | 2026-09-23 | 1 | 6 | n/d | n/d |
| 5 | add-user-authentication | Arquivada | 2026-09-25 | 4 | 17 | 20 | 1.254.764 |
| 6 | add-city-builder-game | Arquivada (2026-09-27) | 2026-09-27 | 1 | 25 | 36 | 4.268.464 |
| 7 | add-frontend-build | Aberta (não implementada) | 2026-09-27 | 1 | 6 | n/d | n/d |

---

## 2. Entradas por Change

### Change 1: setup-java-project

**Status:** Arquivada (2026-09-23) · **Propósito:** Foundation Java com Spring Boot 4.1.1 e Maven Wrapper

**Commits:**

| Hash | Data | Mensagem |
|---|---|---|
| `66565dc` | 2026-09-24 | commit inicial (inclui as 4 changes arquivadas em 2026-09-23) |

**Funcionalidades:**

- **Adicionado:**
  - Spring Boot 4.1.1, Java 25, Maven Wrapper, `pom.xml` com starters essenciais (webmvc, data-jpa, security, flyway).
  - Pacote `com.example.loginbase`, classe `LoginBaseApplication` com `@SpringBootApplication`.
  - Arquivo `application.properties` com datasource configurável.
  - README.md inicial.

- **Capabilities afetadas:** Nenhuma OpenSpec criada (setup puro).

**Estatísticas:** 7 tasks, data não registrada (change anterior ao novo formato).

**Notas:** Sem relatório de agentes (formato legado pré-subagentes formalizados).

---

### Change 2: add-docker-compose-profiles

**Status:** Arquivada (2026-09-23) · **Propósito:** Ambiente reproduzível com Docker Compose e profiles

**Commits:**

| Hash | Data | Mensagem |
|---|---|---|
| `23d04e5` | 2026-09-24 | Adiciona comando para push inicial |

**Funcionalidades:**

- **Adicionado:**
  - `docker-compose.yml` com 3 serviços (db, app, frontend) e profiles variáveis (db, app, frontend).
  - `Makefile` com targets `help`, `up`, `down`, `logs_front`.
  - `.env.example` com `DB_*`, `PROFILE*`.
  - `.dockerignore` e `frontend/.dockerignore`.

- **BREAKING:** `docker compose up -d` sem `--profile` deixa de subir serviços; exigir profiles explícitos.

- **Capabilities:** `docker-dev-environment` (6 RF: AMB-001…006).

**Estatísticas:** 6 tasks, relatório não disponível.

---

### Change 3: add-vue-frontend

**Status:** Arquivada (2026-09-23) · **Propósito:** SPA Vue 3 + Vite + PrimeVue com proxy de desenvolvimento

**Commits:**

| Hash | Data | Mensagem |
|---|---|---|
| `66565dc` | 2026-09-24 | commit inicial |

**Funcionalidades:**

- **Adicionado:**
  - Projeto `frontend/` com Vue 3.5, Vite 8, PrimeVue 5 (Aura).
  - `vite.config.ts` com polling configurável (`VITE_USE_POLLING`).
  - `frontend/Dockerfile` de desenvolvimento (Node 26, npm 12; estágio único).
  - `frontend/src/main.ts`, `App.vue`, package.json com dependências.

- **BREAKING:** `make up` passa a subir também o serviço `frontend` (porta 5173).

- **Capabilities:** `frontend-app` (5 RF iniciais; o jogo removeu 1 e adicionou 3 = 7 vigentes).

**Estatísticas:** 11 tasks, relatório não disponível.

---

### Change 4: add-subagent-dev-skill

**Status:** Arquivada (2026-09-23) · **Propósito:** Orquestração de desenvolvimento com subagentes Claude (Opus/Sonnet/Haiku)

**Commits:**

| Hash | Data | Mensagem |
|---|---|---|
| `26b38ad` | 2026-09-24 | Atualiza dev-subagentes: Opus planeja, Sonnet codifica, Haiku escreve .md |

**Funcionalidades:**

- **Adicionado:**
  - `.claude/skills/dev-subagentes/SKILL.md` com regras de orquestração (papéis, sessão limpa, ondas, relatório).
  - `CLAUDE.md` do projeto (replicado do global) com referência à skill.
  - `.claude/commands/opsx/*.md` (6 commands de OpenSpec).

- **Capabilities:** `subagent-dev-workflow` (5 RF: PRC-001…005).

**Estatísticas:** 6 tasks, relatório não disponível.

---

### Change 5: add-user-authentication

**Status:** Arquivada (2026-09-25) · **Propósito:** Autenticação de usuários com modelo de acesso, login, sessões, auditoria

**Commits:**

| Hash | Data | Mensagem |
|---|---|---|
| `eb618a4` | 2026-09-24 | Adiciona autenticação de usuários: modelo de acesso, login, sessões e admin inicial |
| `94609b9` | 2026-09-24 | Implementa autenticação de usuários (change add-user-authentication) |
| `4d45323` | 2026-09-25 | Arquiva a change add-user-authentication e sincroniza as specs |

**Funcionalidades:**

- **Adicionado:**
  - Modelo de acesso: tabelas `usuarios`, `perfis`, `permissoes`, `usuario_rel_perfis`, `perfis_rel_permissoes`, `sessoes` (migrações V1, V2).
  - Spring Security 7.1.1: `SecurityConfig`, `UsuarioDetailsService`, form login `/login` (POST com campos `login`, `senha`), logout `/logout`.
  - Session management: `JSESSIONID` com `HttpOnly`, `SameSite=Lax`, `Secure` configurável (env `SESSION_COOKIE_SECURE`, timeout 30 min).
  - Auditoria: `EntidadeAuditavel` com campos `criado_em/por`, `alterado_em/por`; `UsuarioAuditorAware` e `AuditoriaConfig` para tracking.
  - Admin inicial por `ApplicationRunner` a partir de env `ADMIN_EMAIL`/`ADMIN_PASSWORD`.
  - Registro de sessões em tabela com SHA-256 do ID.
  - Thymeleaf login template em `templates/sistema/public/login.html`.

- **Capabilities:** `access-control-data` (7 RF), `user-authentication` (9 RF).

- **Estatísticas:**
  - **17 tasks** (1.1–7.2; formato antigo, sem pasta `tasks/`).
  - **20 agentes:** 1 Opus, 17 Sonnet, 2 Haiku (+ 1 Haiku no archive/sync).
  - **Tokens:** 1.215.955 (execução) + 38.809 (archive/sync) = **1.254.764 total**.
  - **Relatório:** `openspec/changes/archive/2026-09-25-add-user-authentication/resumo_utilizacao_agentes.md`.

---

### Change 6: add-city-builder-game

**Status:** Arquivada (2026-09-27) · **Propósito:** City builder tático com economia, combate por turnos, frontend reativo

**Commits:**

| Hash | Data | Mensagem |
|---|---|---|
| `454ae58` | 2026-09-27 | criação do sistema de jogo |

**Funcionalidades:**

- **Adicionado:**
  - **Banco de dados (V3):** 8 tabelas (vilas, prédios, canteiros, sementes, itens, unidades, ordens, batalhas) com constraints de integridade.
  - **Backend (pacote `com.example.loginbase.jogo`):
    - Catálogo: TipoPredio, TipoRecurso, Cultivo, ModeloItem, TipoTropa, TipoInimigo, Masmorra com MapaMasmorra.
    - Entidades: Vila, Predio, Canteiro, EstoqueSemente, Item, Unidade, Ordem, Batalha.
    - Economia: CalculadoraProducao (lazy, milésimos, velocidade), VilaService (criação/sincronização), EstadoVila (estado imutável).
    - Serviços de ação: ConstrucaoService, FazendaService, ForjaService, QuartelService.
    - Masmorra: MasmorraService, MotorCombate (determinístico, IA), GeradorLoot.
    - API REST: VilaController (vila, catálogo), AcoesVilaController (prédios, fazenda, forja, quartel), MasmorraController (batalhas).
    - Infraestrutura: CodigoErro (18 erros de regra), ErroApiHandler (mapeamento HTTP), Clock e Aleatorio (injetáveis).
  - **Frontend (Vue 3):**
    - Router: 6 rotas (/, /fazenda, /forja, /quartel, /masmorras, batalha/:id).
    - Views: VilaView, FazendaView, ForjaView, QuartelView, MasmorrasView, BatalhaView.
    - Componentes: PainelRecursos, CartaoPredio, GradeBatalha.
    - Composable: useVila (estado em singleton, polling 5 s).
    - HTTP: http.ts (CSRF header, interceptor 401), jogo.ts (chamadas API), tipos.ts (DTOs).
  - **Configuração:**
    - `JOGO_VELOCIDADE` (multiplicador de taxas/tempos) em Spring e Docker.
    - Segurança API: 401 sem cache para `/api/**`, CSRF `X-XSRF-TOKEN`.

- **Modificado:**
  - `user-authentication` spec: Adição de RF-AUT-010 (CSRF SPA), ajuste RF-AUT-004 (401 sem cache).
  - `frontend-app` spec: Mudança página inicial (boas-vindas → vila), adição de proxy.
  - `SecurityConfig`: Handler para 401 em `/api/**`, `csrf.spa()`.
  - `.env.example`, `docker-compose.yml`: Novas variáveis `BACKEND_URL`, `JOGO_VELOCIDADE`.

- **BREAKING:** Página inicial `/` agora exibe a vila do jogador (interativa) em vez de "Seja bem-vindo".

- **Capabilities:**
  - Novas (9): `game-data`, `game-village`, `game-buildings`, `game-farming`, `game-forge`, `game-army`, `game-dungeon-combat`, `game-dungeon-loot`, `game-frontend`.
  - Modificadas (2): `user-authentication`, `frontend-app`.

- **Estatísticas:**
  - **25 tasks** (1.1–8.2; grupos 1–8: base, persistência, economia, ações, masmorras, API, frontend, docs/verificação).
  - **35 subagentes + orquestrador (36 linhas no relatório):** 1 Opus, 25 Sonnet (1 sem consumo reportado), 9 Haiku.
  - **Tokens:** 4.268.464 total (Opus 124.676, Haiku 658.372, Sonnet 3.204.944, orquestrador 280.472).
  - **Progresso:** 25/25 tasks concluídas (100%).
  - **Testes:** 239 testes no total (≈170 novos na change), 0 falhas.
  - **Relatório:** `openspec/changes/archive/2026-09-27-add-city-builder-game/resumo_utilizacao_agentes.md`.

---

### Change 7: add-frontend-build

**Status:** Aberta (não implementada) · **Propósito:** Build de produção integrado do frontend Vue/Vite, servido pelo backend Spring em `/` com assets públicos em `/app/**`

**Commits (artefatos da change, código não implementado):**

| Hash | Data | Mensagem |
|---|---|---|
| `690f4d8` | 2026-09-27 | Implementa serviço `frontend-build`, script Python e integração ao backend (artefatos: proposal.md, design.md, specs, tasks) |

**Nota:** commit `690f4d8` contém os artefatos da change e a mudança de porta 8080 → 80 (`.env.example`, `Dockerfile` EXPOSE 80, `docker-compose.yml` "80:80", `vite.config.ts` BACKEND_URL, `application.properties` server.port, `openspec/specs/frontend-app/spec.md`) — origem da divergência D-16; a mensagem descreve a implementação, que ainda não ocorreu no código.

**Funcionalidades Previstas:**

- **Adicionado** (não implementado):
  - **Script Python `scripts/build_front.py`:** Executa `docker compose run --rm --build frontend-build`, valida `frontend/dist/index.html`, copia assets para `src/main/resources/static/app/` e index para `src/main/resources/templates/sistema/seguro/index.html`, registra `build.log`.
  - **Serviço Docker `frontend-build`:** Profile `build`, volume anônimo `/app/node_modules`, imagem Node 26.
  - **Alvo Makefile `make build_front`:** Executa o script; aparece em `make help`.
  - **Configuração Vite:** `base: '/app/'` apenas em builds (`command === 'build'`); dev server inalterado.
  - **Rotas da SPA no `PaginaController`:** Fallback do history mode para `/`, `/fazenda`, `/forja`, `/quartel`, `/masmorras`, `/batalhas/{id}`.
  - **Acesso público a assets:** `/app/**` em `SecurityConfig.permitAll()`.
  - **Gitignore:** Artefatos gerados (`static/app/`, template index, `build.log`, `frontend/dist/`).
  - **Testes:** Template mínimo em `src/test/resources/`; verifica rotas autenticadas e acesso público.

- **Modificado** (previsto):
  - `PaginaController`: Adiciona rotas da SPA.
  - `SecurityConfig`: Adiciona `/app/**` ao permitAll.
  - `AutenticacaoWebMvcTest`: Ajusta testes de rotas para verificar view + status 200.
  - `.gitignore`, `Makefile`, `docker-compose.yml`, `frontend/vite.config.ts`.
  - Docs: `09-guia-desenvolvedor.md`, `10-implantacao-operacao.md`.

- **BREAKING** (previsto):
  - `/` deixa de mostrar placeholder "Seja bem vindo"; passa a servir a SPA.
  - `static/app/` e template index.html passam a ser gerados, não versionados.
  - Clone limpo exige `make build_front` antes de `./mvnw package` ou `docker build`.

- **Capabilities** (previsto):
  - Novas: Nenhuma.
  - Modificadas: `user-authentication` (RF-AUT-005: Página inicial segura), `frontend-app` (RF-FRE-008, RF-FRE-009, RF-FRE-010: Build integrado, rotas, assets públicos).

- **Estatísticas** (previsto):
  - **6 tasks** (1.1–4.1; grupos: infraestrutura do build, backend servindo a SPA, docs, verificação).
  - **Nenhum agente designado** (em aberto).
  - **Tokens:** a confirmar após implementação.
  - **Progresso:** 0/6 tasks concluídas (não iniciada).
  - **Relatório:** pendente.

---

## 3. Commits sem Change OpenSpec

Alguns commits evolutivos ocorreram fora do ciclo formal de change ou como evolução posterior:

| Hash | Data | Mensagem | Contexto |
|---|---|---|---|
| `23d04e5` | 2026-09-24 | Adiciona comando para push inicial | Inicial de push (pré-OpenSpec formalizado) |
| `fd9405e` | 2026-09-26 | Adiciona script `executar.py`, suporte a cores em scripts e comando `executar` ao Makefile | Utilitários posteriores (não integrados em change) |
| `6ce0641` | 2026-09-25 | OpenSpec: tasks em arquivos separados com links clicáveis | Atualização de formato de tasks (melhoria processo) |

---

## 4. Versão do Produto

| Artefato | Versão | Data | Nota |
|---|---|---|---|
| `pom.xml` (`<version>`) | 0.0.1-SNAPSHOT | Sempre | Maven standard; sem release tag |
| `frontend/package.json` | 0.0.0 | Sempre | Não versionado; dev only |
| Git tags | Nenhuma | — | Sem release formal (alpha/beta/GA) |
| Baseline docs | 1.0.0 | 2026-09-27 | Primeira documentação formal (change jogo arquivada) |
| Docs + previsto (change 7) | 1.1.0 | 2026-09-27 | Atualização para add-frontend-build (aberta, não implementada) |

---

## 5. Estatísticas Agregadas

### Por tipo de trabalho

| Modelo | Agentes | Tokens | % |
|---|---|---|---|
| Opus (planejamento) | 2 | 124.676 + n/d | ~3% |
| Sonnet (código) | 41 | 3.204.944 + n/d | ~75% |
| Haiku (docs) | 12 | 658.372 + n/d | ~15% |
| Orquestrador | 1 | 280.472 | ~7% |
| **Total** | **56** | **~4.269.464** | **100%** |

(Primeiro relatório formal é auth change; jogo change tem contagem completa.)

### Por fase

| Fase | Changes | Tasks | Agentes | Tokens |
|---|---|---|---|---|
| Setup (1–3) | 3 | ~16 | n/d | n/d |
| Infra (4) | 1 | 3 | n/d | n/d |
| Autenticação (5) | 1 | 17 | 20 | 1.254.764 |
| Jogo (6) | 1 | 25 | 36 | 4.268.464 |
| Frontend build (7, previsto) | 1 | 6 | n/d | n/d |
| **Total (vigente + previsto)** | **7** | **67** | **56+** | **~5.523.228** |

---

## 6. Visibilidade de Changelog

### Change `add-city-builder-game` (já arquivada em 2026-09-27)

1. Movida para `openspec/changes/archive/2026-09-27-add-city-builder-game/`.
2. Tabela acima atualizada: "Status: Arquivada (2026-09-27)".
3. Specs delta agora em `openspec/specs/` (sincronizadas via `/opsx:sync`).
4. Documentação refs atualizada (links removidos de archive, sufixos removidos).

### Change `add-frontend-build` (aberta, não implementada)

1. Estrutura em `openspec/changes/add-frontend-build/` com proposal.md, design.md, specs/, tasks.md.
2. Status: "Aberta (não implementada)" na tabela (§1).
3. Documentação marcada como "previsto — change add-frontend-build" em todos os docs afetados.

---

## Histórico de revisões

| Versão | Data | Resumo | Autor |
|---|---|---|---|
| 1.1.0 | 2026-09-27 | Atualização para a change add-frontend-build (prevista, aberta) | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial (jogo arquivada) | Adiel, com apoio de agentes Claude |
