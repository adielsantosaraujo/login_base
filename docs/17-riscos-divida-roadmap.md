# Riscos, Dívida Técnica, Divergências e Roadmap

| Campo | Valor |
|---|---|
| Versão | 1.2.0 |
| Data | 2026-09-27 |
| Status | Vigente — baseline do commit `454ae58` + change `add-frontend-build` implementada |
| Modelo/norma | Registro de riscos (ISO 31010) + backlog de evolução |
| Público | líderes, arquitetos, desenvolvedores |

> Parte da [documentação do login_base](README.md). Consolidação de riscos operacionais, dívida técnica acumulada, divergências entre planejamento e código, e roadmap de evolução.

---

## 1. Registro de Riscos

Riscos identificados nesta documentação (R-02, R-06 e R-10 derivam dos Risks do design do jogo; R-11, R-12, R-13 derivam do design da change `add-frontend-build` implementada; os demais são avaliação do revisor):

| # | Risco | Probabilidade | Impacto | Status Atual | Mitigação Existente | Risco Residual |
|---|---|---|---|---|---|---|
| R-01 | Falha de Postgres em produção (sem backup/DR definido) | **Alta** | **Crítico** | **Vigente** | Nenhuma (o manual de operação registra "Sem backup: volume db-data não tem snapshot"). | **Alto**: sem replicação, sem monitoração. Roadmap: procedimento de backup/restore. |
| R-02 | Escalabilidade: lock pessimista pode criar contentção com muitos usuários | **Média** | **Alto** | **Vigente** | Serializava via `@Lock(PESSIMISTIC_WRITE)` apenas por vila (não global); cálculo lazy reduz contenção. | **Médio**: sem teste de carga. Roadmap: load test, lock granular se necessário. |
| R-03 | Security: sem rate limiting, bloqueio por tentativa de login — brute force viável | **Média** | **Alto** | **Vigente** | Nenhuma (não foi implementado). Timeout de sessão (`SESSION_TIMEOUT`, padrão 30m) não atenua força bruta. | **Alto**: sem proteção. Roadmap: implementar rate limiting (Spring Security + Redis). |
| R-04 | Security: credenciais em `.env` local (não em produção, mas dev pode expor em git) | **Média** | **Alto** | **Vigente** | `.env` listado em `.gitignore`; `.env.example` versionado sem valores. | **Médio**: é responsabilidade do dev; sem scan de secrets no CI. Roadmap: pre-commit hook + secret scanning. |
| R-05 | Frontend: sem testes automatizados (Cypress/Playwright) — regressões viram caras | **Alta** | **Médio** | **Vigente** | Validação `npm run build` (TypeScript) + task 8.2 (cenário manual); sem E2E CI. | **Alto**: requer setup completo para testar UI. Roadmap: E2E tests + CI. |
| R-06 | Dados: Estado/log/loot em JSON `text` (não JSONB) — queries difíceis se necessário | **Baixa** | **Médio** | **Vigente** | Postgres ainda suporta `text` com bom performance para JPA neste volume; se SQL direto, complexo. | **Baixo**: schema V3 é imutável; mudança futura é migração pesada. Roadmap: considerar JSONB após load test. |
| R-07 | Performance: Polling 5 s no frontend — latência visível em combate rápido | **Média** | **Baixo** | **Vigente** | Intervalo fixo de 5 s em `useVila.ts` (`VITE_USE_POLLING` é o file-watch do Vite, não o polling da API). WebSocket seria melhor. | **Médio**: aceitável em dev, rejeitável em PvP. Roadmap: WebSocket opcional, upgrade a Vue 4. |
| R-08 | Build: sem CI/CD — validação manual, deploy manual, sem rollback automático | **Média** | **Crítico** | **Vigente** | Nenhuma. `openspec validate --strict` roda local. | **Crítico**: impossível em produção. Roadmap: CI com GitHub Actions (test, build, publish). |
| R-09 | Dependências: `spring-boot-starter-hateoas` declarado, não usado — acumula dívida | **Baixa** | **Baixo** | **Vigente** | Nenhuma. `pom.xml` intacto; poderia remover. | **Baixo**: harmless; vira tech debt. Roadmap: cleanup no próximo refactor. |
| R-10 | Frontend: `@primeicons/vue` importado implicitamente (transitive dependency) — pode quebrar | **Baixa** | **Médio** | **Vigente** | Entrada em `package-lock.json`; se PrimeVue atualiza, pode romper. | **Baixo**: atualmente estável; pinned no lock. Roadmap: declarar explicitamente. |
| R-11 | Clone limpo sem `make build_front` → erro 500 em `/` | **Média** | **Alto** | **Ativo — mitigado por docs** | Mitigação implementada: docs em `09-guia-desenvolvedor.md` §4.10, `10-implantacao-operacao.md` §5, README setup; CI futuro executa `make build_front` antes de `./mvnw package` / `docker build`. | **Médio**: template não existe até primeiro build. Documentação clara implementada. |
| R-12 | Rota nova da SPA fora do `PaginaController` → F5/link direto dá 404 | **Média** | **Médio** | **Ativo — mitigado por design** | Lista explícita de rotas `/`, `/fazenda`, `/forja`, `/quartel`, `/masmorras`, `/batalhas/{id}` implementada em `PaginaController`. Mitigação: padrão documentado; considerar catch-all futuro se rotas crescerem demais. | **Médio**: risco de esquecer atualizar lista em mudanças futuras; trade-off entre segurança (lista) e flexibilidade (catch-all). |
| R-13 | Build de produção depende de Docker | **Baixa** | **Médio** | **Ativo — aceito** | Docker já é obrigatório para dev (serviço `frontend`). Mitigação implementada: CI/CD futuro assume Docker disponível; sem Docker, sem build de produção (aceitável em projeto containerizado). | **Baixo**: alinhado com escolha de stack (Docker Compose obrigatório). |

### Resumo de Risco

- **Crítico:** 2 (R-01, R-08) → Exigem ação imediata se produção planejada.
- **Alto:** 5 (R-02, R-03, R-04, R-05, R-11) → Mitigar em próximas changes; R-11 será documentado.
- **Médio/Baixo:** 6 (R-06, R-07, R-09, R-10, R-12, R-13) → Backlog.

---

## 2. Dívida Técnica

| # | Título | Categoria | Impacto | Prazo Sugerido |
|---|---|---|---|---|
| DT-01 | Remover `spring-boot-starter-hateoas` não usado | Cleanup | Negligenciável (10 linhas) | P4 (próximo refactor) |
| DT-02 | Declarar explicitamente `@primeicons/vue` no `package.json` | Cleanup | Negligenciável (1 linha) | P4 |
| DT-03 | Sem cobertura de código (JaCoCo) — métricas desconhecidas | Observabilidade | Médio (impossível saber % real) | P3 (após E2E) |
| DT-04 | Sem CI/CD configurado — validações são manuais | DevOps | Crítico | P1 (antes de produção) |
| DT-05 | Sem testes de frontend (E2E com Cypress/Playwright) | Teste | Alto | P2 (com CI) |
| DT-06 | Sem OpenAPI gerado dinamicamente — mantém-se YAML/Markdown manual | API | Médio | P3 (após cleanup de spec) |
| DT-07 | `UsuarioAtual` guarda o repositório em campo estático (acoplamento/ordem de inicialização; não é problema de thread) — injetar o bean nos controllers | Design | Médio | P2 (antes de load test) |
| DT-08 | Dois tipos `ItemDto` diferentes (api.ItemDto com categoria, VilaDto.ItemDto com alcance) — confusão | Design | Médio | P2 (unificar) |
| DT-09 | Sem Testcontainers (testes de integração contra Postgres real do compose, em máquina do dev) | Teste | Médio | P3 (após CI, opcional) |
| DT-10 | Specs principais (`user-authentication`, `frontend-app`) desatualizadas (delta não sincronizado) | Documentação | Alto | P1 (sincronizar ao arquivar jogo) |
| DT-11 | `comando_git_push.md` vazio e `chat.md` (pedido original) na raiz (`/`) | Limpeza | Negligenciável | P4 |
| DT-12 | Exemplo cURL no README.md incorreto (não inclui `_csrf`, token vem de cookie) | Documentação | Médio | P3 (corrigir em próximo release notes) |
| DT-13 | Sem configuração de rate limiting, bloqueio por tentativa, MFA | Segurança | Crítico (produção) | P1 (roadmap: change de segurança) |
| DT-14 | Lista de rotas duplicada entre `frontend/src/router/index.ts` e `PaginaController` — difícil manter sincronizadas | Design | Médio | P2 (considerar catch-all genérico ou gerador de rotas futuro) |

### Total: 14 itens de dívida

**Distribuição por urgência:**

- **P1 (Bloqueador para produção):** 3 (CI, sync specs, segurança).
- **P2 (Antes de carga/teste):** 4 (E2E, UsuarioAtual, ItemDto, rotas duplicadas).
- **P3 (Próximo sprint):** 4 (JaCoCo, OpenAPI, Testcontainers, README).
- **P4 (Nice-to-have):** 3 (Cleanup, PrimeIcons, chat.md).

---

## 3. Divergências entre Planejamento e Código

Consolidadas de `proposal.md`, `design.md`, specs, código-fonte e artefatos implementados da change `add-frontend-build` com a marca "o código prevalece":

| ID | Donde Citado | Planejado/Documentado | Código Real | Documentação Afetada | Recomendação |
|---|---|---|---|---|---|
| **D-01** | `README.md` exemplo cURL | Login com `login=...&senha=...` sem `_csrf`; token de `.xsrfToken` do JSON catálogo | `CatalogoDto` **não tem** `xsrfToken`; token vem do cookie `XSRF-TOKEN` (lido pelo navegador); POST `/login` obriga `_csrf` (formulário ou header) | `06-api-rest.md` (exemplo corrigido), `17` (aqui) | Corrigir exemplo cURL na task 8.1 ou release notes; atualizados docs. **Status:** Resolvido em `16`, faltam exemplos. |
| **D-02** | README padrão do jogo | "Canteiros até 5 por nível fazenda"; "Login com e-mail + senha" | Nº de canteiros **= nível da fazenda** (máx. 5, constraint `ck_jogo_canteiros_posicao 1-5`); **login aceita e-mail OU celular** | `12-gdd.md` (atualizado), `13-manual-jogador.md` | Ambos corretos no documento (código prevalece); sem ação adicional. **Status:** Resolvido. |
| **D-03** | spec `game-village` › Operações serializadas | "Segunda melhoria simultânea → `422 RECURSOS_INSUFICIENTES`" | Ordem de validação em `ConstrucaoService`: **`FILA_OCUPADA` vem ANTES** de recursos → 2ª recebe `FILA_OCUPADA` | `02-requisitos.md` (registrado), `15-rastreabilidade.md` | Código correto (fila é mais relevante); spec levemente enganosa. Atualizar spec na revisão de divergências. **Status:** Código prevalece. |
| **D-04** | spec `game-village`, design §1 | "Custos já refletem velocidade" | `JogoMapper.toCatalogoDto()` aplica velocidade **apenas aos tempos** (prédio, forja, treino); custos NÃO variam com velocidade | `02-requisitos.md` (clarificado), `06-api-rest.md` (campo `custo` sem velocity) | Código correto: Velocidade afeta **quando** construir, não o **preço**. Spec é confusa; testes validam. **Status:** Design incorreto; código prevalece. |
| **D-05** | spec `game-village`, design § 4 | "`401 UNAUTHORIZED` como corpo" (JSON) em anônimo a `/api/**`; ref a "valores iniciais A.4" | `HttpStatusEntryPoint` devolve `401` **sem corpo**; "A.4" não existe (corresponde design §4, não spec) | `02-requisitos.md`, `06-api-rest.md` (example correto) | Especificação confusa. Código correto (sem corpo é padrão REST). **Status:** Design enganoso, docs corrigidas. |
| **D-06** | proposal do jogo §What | "API REST versionada" (comentário) | Rotas implementadas: `/api/jogo/...` (sem versão em path como `/api/v1/jogo/...`) | `06-api-rest.md` (sem version) | Spec não exige versão; proposal comentário enganoso. Código e docs alinhados. **Status:** Resolvido. |
| **D-07** | `docker-compose.yml`, proposal | `JOGO_VELOCIDADE` repassada ao `frontend` ("ambas as variáveis") | Frontend **não lê** `JOGO_VELOCIDADE` (lê: `VITE_PRIMEUI_LICENSE`, `VITE_USE_POLLING`, `BACKEND_URL`) | `10-implantacao-operacao.md` (§2, tabela corrigida) | Variável no compose é harmless (não usada); UI não reflete velocidade (por design, é server-side). Docs atualizadas. **Status:** Resolvido. |
| **D-08** | archive `setup-java-project` design | `postgres:16-alpine`; datasource `jdbc:postgresql://db:5432/...`; Dockerfile `./mvnw` | `postgres:17-trixie` no compose; `${DB_HOST:localhost}` em config; Dockerfile usa `mvn` da imagem Maven | `04-arquitetura.md` (spec, não dockerfile) | Versões evoluem; datasource parametrizado (correto). Sem ação. **Status:** Changes de tempo. |
| **D-09** | design do jogo §19/§20 | "Frontend com Testcontainers" como **non-goal** (texto confuso) | Não há testes de frontend de tipo algum | `08-plano-testes.md` (§4, confirmado: sem E2E) | Proposal confuso (parece meta, não non-goal). Realidade: nenhum teste frontend; E2E em roadmap. **Status:** Design enganoso; teste é P2. |
| **D-10** | task 8.2 verificação integrada | Soldado Espada N1 + Armadura Couro N1 = "ataque 8"; IDs `J<unidadeId>` | Ataque = **arma = 6** (`base 6 + bonus 2×(L−1)` → N1=6); IDs = **`J1..J4`** pela ordem do esquadrão em `MasmorraService` | `03-casos-de-uso.md` (corrigido), `08-plano-testes.md`, `12-gdd.md` (§9, exemplo numérico) | Task 8.2 tinha cálculo errado; números recalculados (agente 34 ajustou). **Status:** Resolvido. |
| **D-11** | specs principais (vigentes) | "Página inicial de boas-vindas" (FRE-005); "Proteção de rotas" sem 401 (AUT-004 original) | Código implementou delta: página inicial **= vila** (jogo); **401 sem cache** em `/api/**` | `02-requisitos.md` (marca REMOVIDO/MODIFICADO), `15-rastreabilidade.md` | Specs vigentes sincronizadas. Change jogo arquivada em 2026-09-27; specs vigentes atualizadas. **Status:** Resolvido. |
| **D-12** | `pom.xml` / `frontend/package.json` | HATEOAS no README; ícones importados mas não declarados | `spring-boot-starter-hateoas` sem uso; `@primeicons/vue` em transitive (lockfile) | `04-arquitetura.md` (ADR 0012 registra), `17` (DT-01, DT-02) | Dívida técnica, sem impacto atual. **Status:** Cleanup P4. |
| **D-13** | design change `add-frontend-build` task 1.1 vs. implementação | Serviço `frontend-build` com `image: node:20-alpine` + volume nomeado | Projeto usa `node:26-trixie-slim` e implementação usa `build: ./frontend` + volume anônimo (descartado em `--rm`) — design corrigido na implementação | `10-implantacao-operacao.md` §1 (Imagens base) | **Resolvida na implementação.** Código correto (build + volume anônimo é mais leve e limpo). |
| **D-14** | design change `add-frontend-build`, Migration Plan item 2 | Build "automático em CI/CD" | Não há CI configurado (R-08); build é manual via `make build_front` | `11-processo-desenvolvimento.md` §6, `17` (R-08, CH-CI roadmap) | **Resolvida conforme design.** Esperado: CI não existe; build é manual via `make build_front` até CH-CI. Documentação clara. |
| **D-15** | tasks 1.2, 2.1 change `add-frontend-build` | Code snippets (exemplos de `@RestController`, uso de `antMatchers`, import `CORES`) | Task 1.2 importa `CORES` de `cores.py`; implementação usa `scripts/cores.py` real com constantes BLUE_COLOR/RED_COLOR/CYAN_COLOR. Task 2.1 usa `@Controller` + `RequestMatchers` conforme SecurityConfig real. | Tasks 1.2 e 2.1 da change (validação em implementação) | **Resolvida na implementação.** Snippets eram guias; executor validou contra código real. |
| **D-16** | docs (README/09/10: 'porta 80') | Porta do backend = 80 | `application.properties`: `server.port=${SERVER_PORT:8080}`, `.env.example`: `SERVER_PORT=8080`, compose app: não recebe `SERVER_PORT`, mapeia `"80:80"` → app provavelmente inacessível em container | `09-guia-desenvolvedor.md` §7 (SERVER_PORT), `10-implantacao-operacao.md` §3 (Variáveis, nota D-16) | **Divergência pré-existente, não originária de add-frontend-build.** Requer sync futuro (fora do escopo desta change). |

### Consolidação de Divergências

- **D-01 … D-12:** 12 divergências do código vigente, registradas acima. **Status:** Resolvidas.
- **D-13 … D-16:** 4 divergências entre design/tasks da change `add-frontend-build` e implementação real, registradas acima.
  - **D-13:** Resolvida na implementação (volume anônimo + build).
  - **D-14:** Resolvida conforme design (build manual até CI).
  - **D-15:** Resolvida na implementação (snippets validados, cores.py usado).
  - **D-16:** Divergência pré-existente, fora do escopo de add-frontend-build.
- **Total:** 16 divergências de conhecimento; 15 resolvidas, 1 pré-existente em backlog.
- **Origem comum:** Specs legadas (ainda em `openspec/specs/`, não atualizadas); change jogo tinha delta separado não arquivado; change `add-frontend-build` agora implementada com design validado.
- **Resolução:** Change jogo arquivada em 2026-09-27 (D-11 resolvida, specs sincronizadas). Change `add-frontend-build` implementada em 2026-09-27 (D-13 a D-15 resolvidas; D-16 em backlog futuro).

---

## 4. Roadmap de Evolução

Propostas de evolução (não aprovadas). Só os itens marcados com * constam como Non-Goal/"outra change" nas changes: autocadastro/CRUD de usuários*, recuperação de senha*, MFA*, bloqueio por tentativas*, upkeep*, cancelamento*, colheita*, mapas procedurais*, login em Vue*, Testcontainers*. Os demais (PvP, mobile, GraphQL, Gatling, Redis, Sonar, Heroku/AWS) são sugestões desta documentação.

### Fase 1: Foundation (Concluído)

- [x] Autenticação com login, sessões, auditoria.
- [x] Docker Compose com profiles, Makefile.
- [x] Frontend Vue 3 com roteamento.
- [x] City builder + combate tático.

### Fase 2: Frontend & Build

- [x] **CH-FRONT-BUILD:** Build de produção integrado do frontend; SPA servida pelo backend (implementado 2026-09-27).
  - Realizado: change `add-frontend-build` (implementada, 6 tasks).
  - Mitigou: Riscos R-11, R-12, R-13; confirmou dívida: DT-14 (lista de rotas duplicada).

### Fase 2B: Segurança & Operação

- [ ] **CH-SEC:** Rate limiting, bloqueio por tentativa, MFA.
  - Reduz: Risco R-03; resolve: DT-13.
- [ ] **CH-CI:** CI/CD com automação (GitHub Actions ou similar).
  - Reduz: Risco R-08; resolve: DT-04.
  - Passo crítico: `make build_front` antes de `./mvnw package` / `docker build`.
- [x] **CH-SPEC:** Arquivar `add-city-builder-game`, sincronizar specs vigentes (2026-09-27).
  - Resolve: D-11.

### Fase 3: Qualidade & Performance

- [ ] **CH-E2E:** Testes E2E com Cypress.
  - Reduz: Risco R-05; resolve: DT-05.
- [ ] **CH-COVERAGE:** Métricas de cobertura.
  - Resolve: DT-03.
- [ ] **CH-PERF:** Load test → análise de lock, caching, índices.
  - Reduz: Riscos R-02, R-07.

### Fase 4: User Features

- [ ] **CH-USERS*:** Cadastro/CRUD de usuários.
- [ ] **CH-UPKEEP*:** Manutenção de tropas, cancelamento de ordens.
- [ ] **CH-HARVEST*:** Colheita com ciclos.
- [ ] **CH-MAPGEN*:** Mapas procedurais.
- [ ] **CH-AI*:** IA adaptativa.

### Fase 5: Platform

- [ ] **CH-PvP:** Batalhas contra jogadores.
- [ ] **CH-PROD:** Deploy em produção.
- [ ] **CH-MOBILE:** App mobile.
- [ ] **CH-TESTCONTAINERS*:** Testcontainers (opcional).
- [ ] **CH-SPLIT:** GraphQL + serviços.
- [ ] **CH-FIXDIV:** Cleanup de dívida técnica.
  - Resolve: DT-01, DT-02, DT-07, DT-08, DT-12.

---

## 5. Referência Cruzada: Risco → Roadmap

| Risco | Sugestão de Mitigação | Tipo |
|---|---|---|
| R-01 (Backup/DR) | CH-PROD | Infra |
| R-02 (Contenção lock) | CH-PERF | Performance |
| R-03 (Rate limiting) | CH-SEC | Segurança |
| R-04 (Secrets) | CI (pre-commit) | DevSecOps |
| R-05 (E2E) | CH-E2E | Teste |
| R-06 (JSONB) | CH-PERF (load test) | Infra |
| R-07 (Polling) | CH-PERF (WebSocket) + CH-PvP | Performance |
| R-08 (CI/CD) | CH-CI | DevOps |
| R-09, R-10 (Cleanup) | CH-FIXDIV | Manutenção |
| R-11 (Clone sem build → 500 em `/`) | CH-FRONT-BUILD (docs), CH-CI (automação) | DevOps |
| R-12 (Rotas fora do controller → 404) | CH-FRONT-BUILD (design padrão), CH-FIXDIV (catch-all futuro) | Design |
| R-13 (Docker obrigatório) | CH-FRONT-BUILD (aceito), CH-CI (Docker no pipeline) | Infra |

---

## Histórico de revisões

| Versão | Data | Resumo | Autor |
|---|---|---|---|
| 1.2.0 | 2026-09-27 | Change add-frontend-build implementada: atualiza R-11/R-12/R-13 para "Ativo — mitigado", D-13/D-14/D-15 para "Resolvida", roadmap CH-FRONT-BUILD marca como [x] realizado | Adiel, com apoio de agentes Claude |
| 1.1.0 | 2026-09-27 | Atualização para a change add-frontend-build (prevista, aberta): riscos R-11 a R-13, dívida DT-14, divergências D-13 a D-16, roadmap CH-FRONT-BUILD | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial (jogo) | Adiel, com apoio de agentes Claude |
