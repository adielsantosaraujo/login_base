# Especificação de Requisitos de Software (SRS)

| Campo | Valor |
|---|---|
| Versão | 1.2.0 |
| Data | 2026-09-28 |
| Status | Vigente — baseline do commit `454ae58` + changes `add-frontend-build` e `add-soldier-names-batch-slots` implementadas |
| Modelo/norma | ISO/IEC/IEEE 29148:2018 (SRS) + ISO/IEC 25010:2023 (RNF) |
| Público | Desenvolvedores, QA, revisores |
| Fontes | 16 arquivos de spec OpenSpec (8 changes implementadas); designs das changes; código em jogo/; jogo/CodigoErro.java; `openspec/changes/add-soldier-names-batch-slots/` (proposal, design, specs, implementada) |

> Parte da [documentação do login_base](README.md). Requisitos funcionais, não funcionais e regras de negócio do sistema.

---

## 1. Introdução

### 1.1 Propósito

Este documento especifica os **101 requisitos funcionais**, **15 requisitos não funcionais** e **8 categorias de regras de negócio** que compõem o sistema `login_base`, versão 0.0.1-SNAPSHOT, baseline commit `454ae58`. Serve como contrato entre desenvolvedor, revisor e QA para validação de implementação.

### 1.2 Escopo

Sistema web de autenticação + jogo de construção de cidades tático, implantado em:
- Backend: Spring Boot 4.1 + Java 25 + PostgreSQL 17.
- Frontend: Vue 3 + PrimeVue 5 em container Node 26.
- Infraestrutura: Docker Compose com profiles, executado em WSL2.

Fora de escopo: cadastro de usuários, MFA, rate limiting, JWT, producção, CI/CD.

### 1.3 Definições

- **Capability**: funcionalidade agrupada em uma spec OpenSpec (ex.: `user-authentication`).
- **Requirement**: enunciado SHALL/MUST em spec; associado a ID `RF-*`.
- **Scenario**: caso de teste derivado de `### Scenario` em spec; rastreável em testes automatizados.
- **Vigente**: spec sincronizada em `openspec/specs/`.
- **Delta**: requisito novo/alterado da change `add-city-builder-game`, em `openspec/changes/archive/2026-09-27-add-city-builder-game/specs/`.

### 1.4 Referências

- Vision & Scope: [01-visao-produto.md](01-visao-produto.md)
- Arquitetura: [04-arquitetura.md](04-arquitetura.md)
- Casos de uso: [03-casos-de-uso.md](03-casos-de-uso.md)
- Testes: [08-plano-testes.md](08-plano-testes.md)
- Rastreabilidade: [15-rastreabilidade.md](15-rastreabilidade.md)
- Modelo de dados: [05-modelo-dados.md](05-modelo-dados.md)
- API REST: [06-api-rest.md](06-api-rest.md)

---

## 2. Descrição geral

### 2.1 Perspectiva do produto

Sistema web monolítico Spring Boot com autenticação stateful (sessão HTTP), API REST para operações de jogo, frontend SPA reativo em Vue 3, e persistência em PostgreSQL.

### 2.2 Funções principais

1. **Autenticação**: login por e-mail/celular + senha; sessão com timeout; logout.
2. **Autorização**: controle por perfis e permissões (estrutura presente, não usada em rotas no escopo atual).
3. **Gerenciamento de vila**: estado inicial, sincronização lazy de produção, consulta em tempo real.
4. **Construção**: melhorias de prédios com custo, tempo, validação de pré-requisitos.
5. **Agricultura**: plantio de cultivos com sementes; produção automática.
6. **Forja**: criação de armas e armaduras com fila de 1 ordem por vez.
7. **Quartel**: treino de tropas com armas/armaduras; capacidade escalonada.
8. **Masmorras**: combate tático em grid 8×8; IA dos inimigos; loot em vitória.

### 2.3 Classes de usuários

| Classe | Permissões | Acesso |
|---|---|---|
| **Jogador autenticado** | Leitura/escrita de própria vila; combate; jogo | SPA via Vite (porta 5173, dev); backend na porta 80 após `make build_front` |
| **Admin inicial** | Idem jogador (no escopo atual; futuro: gestão de usuários) | SPA |
| **Visitante anônimo** | Leitura de página de login; nenhum acesso ao jogo | Thymeleaf `/login` |

### 2.4 Ambiente operacional

- **Browser**: Chrome, Firefox, Edge recentes (ES2020+).
- **Backend**: JVM + Spring Boot em WSL2 (Docker ou IDE) ou bare metal.
- **Banco**: PostgreSQL 17 em Docker (ou bare metal).
- **Frontend**: Node 26 + npm 12 em Docker.

### 2.5 Restrições de projeto

- Esquema apenas por Flyway; Hibernate em `ddl-auto=validate`.
- Passwords com BCrypt via `DelegatingPasswordEncoder`.
- Sessão HTTP com `HttpOnly`, `SameSite=Lax`, `Secure` configurável.
- Operações serializadas por vila (lock pessimista em JPA).
- Sem framework ORM para operações não-SQL; puro JDBC não usado.

### 2.6 Premissas e dependências

- Admin criado por variável de ambiente `ADMIN_EMAIL`/`ADMIN_PASSWORD` na inicialização.
- Usuário = único registro por e-mail (índice funcional minúsculo).
- Vila criada automaticamente no 1º acesso ao jogo.
- Nenhuma dependência externa em runtime além de PrimeUI (chave local).

---

## 3. Requisitos funcionais

### 3.1 Controle de Acesso e Dados (ACD)

Fonte: [`openspec/specs/access-control-data/spec.md`](../openspec/specs/access-control-data/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-ACD-001 | Sistema mantém tabela de usuários com e-mail único (índice funcional minúsculas), celular opcional único (11 dígitos), nome e auditoria | Obrigatório | Inspeção: V1 + código | 2 |
| RF-ACD-002 | Sistema mantém tabelas de perfis e permissões | Obrigatório | Inspeção: V2 + `Usuario.perfis` | 1 |
| RF-ACD-003 | Vínculo usuário-perfil com data de início (obrigatória) e fim (nula = vigente); validação de vigência no login | Obrigatório | Inspeção: `usuario_rel_perfis` constraint; UsuarioDetailsServiceTest | 1 |
| RF-ACD-004 | Vínculo perfil-permissão em tabela de relacionamento | Obrigatório | Inspeção: tabela `perfis_rel_permissoes` | 1 |
| RF-ACD-005 | Registro de sessões HTTP com SHA-256 do ID, IP origem, User-Agent, data de início e fechamento | Obrigatório | Inspeção: tabela `sessoes` + SessaoServiceTest | 2 |
| RF-ACD-006 | Campos de auditoria em todas as tabelas: `criado_em`, `criado_por`, `alterado_em`, `alterado_por` (todos not null, criado_por = e-mail ou `sistema`) | Obrigatório | Inspeção: migrations + `@CreatedBy`/`@LastModifiedBy` | 2 |
| RF-ACD-007 | Esquema versionado por migrações Flyway; nenhuma alteração manual | Obrigatório | Inspeção: V1, V2, V3 + `ddl-auto=validate` + test | 1 |

### 3.2 Autenticação de Usuários (AUT)

Fonte: [`openspec/specs/user-authentication/spec.md`](../openspec/specs/user-authentication/spec.md) (vigente)

| ID | Requisito | Prioridade | Verificação | Cenários | Status |
|---|---|---|---|---|---|
| RF-AUT-001 | Página de login pública (Thymeleaf) em `/login`; exibe "Usuário ou senha inválidos." em falha genérica | Obrigatório | Demonstração | 2 | Vigente |
| RF-AUT-002 | Login aceita e-mail (normalizado minúsculo) **ou celular** (11 dígitos, normalizado só números) + senha | Obrigatório | Teste + inspeção: `NormalizacaoContato` | 3 | Vigente |
| RF-AUT-003 | Perfis vigentes e permissões carregados como autoridades Spring (`ROLE_<nome>`, não usadas em rotas | Obrigatório | Inspeção: `UsuarioDetailsService` + `UserDetails` | 1 | Vigente |
| RF-AUT-004 | Proteção de rotas: qualquer rota requer autenticação, exceto `/login` e estáticos; `/app/**` também público (assets da SPA, ver RF-FRE-010); `/api/**` anônimo → 401 **sem request cache**, sem redirecionamento | Obrigatório | Teste: `ApiSegurancaWebMvcTest` | 2 | **MODIFICADO** |
| RF-AUT-005 | Página inicial segura em `/` (view `sistema/seguro/index`). Serve a SPA (index.html gerado pelo `make build_front`); anônimo → `/login` | Obrigatório | Demonstração | 2 | Vigente + **MODIFICADO** |
| RF-AUT-006 | Logout via POST `/logout` com CSRF; redirect para `/login?logout` com mensagem "Você saiu do sistema." | Obrigatório | Teste: `AutenticacaoWebMvcTest` | 2 | Vigente |
| RF-AUT-007 | Sessão HTTP com `JSESSIONID` cookie `HttpOnly`, `SameSite=Lax`, `Secure` (configurável por `SESSION_COOKIE_SECURE`); timeout 30 min por `SESSION_TIMEOUT`; ID trocado ao login | Obrigatório | Teste: `SessaoServiceTest` | 2 | Vigente |
| RF-AUT-008 | Cada autenticação registrada em tabela `sessoes` com IP, User-Agent, data/hora, fecha em logout/expiração/startup | Obrigatório | Teste: `SessaoServiceTest` | 1 | Vigente |
| RF-AUT-009 | Admin inicial criado por `ApplicationRunner` se `ADMIN_PASSWORD` definido e e-mail não existir | Obrigatório | Teste: `AdminInicialRunnerTest` | 1 | Vigente |
| RF-AUT-010 | **CSRF para SPA**: `csrf.spa()` em config, cookie `XSRF-TOKEN`, POST exige header `X-XSRF-TOKEN`; logout via formulário com `_csrf` | Obrigatório | Teste: `ApiSegurancaWebMvcTest` | 2 | **ADICIONADO** |

### 3.3 Ambiente de Desenvolvimento (AMB)

Fonte: [`openspec/specs/docker-dev-environment/spec.md`](../openspec/specs/docker-dev-environment/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-AMB-001 | Profile por serviço configurável por variável (`PROFILE_DB`, `PROFILE_APP`, `PROFILE_FRONTEND`), padrão `local` | Obrigatório | Demonstração: `make up` | 1 |
| RF-AMB-002 | Profile ativo definido por `PROFILE` (sobrescreve padrões); valor `desativado` nunca é ativado | Obrigatório | Inspeção: docker-compose.yml | 1 |
| RF-AMB-003 | Variáveis de profile documentadas em `.env.example` e referência `10-implantacao-operacao.md` | Obrigatório | Inspeção | 1 |
| RF-AMB-004 | Levantar ambiente: `make up` sobe serviços conforme PROFILE, sem erros | Obrigatório | Demonstração | 2 |
| RF-AMB-005 | Derrubar ambiente: `make down` para todos, `make down_v` remove volumes | Obrigatório | Demonstração | 1 |
| RF-AMB-006 | `make help` lista alvos; `make` sem alvo exibe ajuda | Obrigatório | Demonstração | 1 |

### 3.4 Frontend Web (FRE)

Fonte: [`openspec/specs/frontend-app/spec.md`](../openspec/specs/frontend-app/spec.md) (vigente)

| ID | Requisito | Prioridade | Verificação | Cenários | Status |
|---|---|---|---|---|---|
| RF-FRE-001 | Projeto frontend Vue 3 com PrimeVue 5 (tema Aura) em `frontend/` | Obrigatório | Demonstração: `npm run build` | 1 | Vigente |
| RF-FRE-002 | Chave de licença PrimeUI lida de `VITE_PRIMEUI_LICENSE` (variável); sem chave = aviso no console | Obrigatório | Inspeção: frontend/src/main.ts (import.meta.env.VITE_PRIMEUI_LICENSE) | 1 | Vigente |
| RF-FRE-003 | Container de desenvolvimento com Node 26 e npm 12 em `frontend/Dockerfile` | Obrigatório | Inspeção + demonstração | 1 | Vigente |
| RF-FRE-004 | Recarga automática ao editar código (hot reload via polling, `VITE_USE_POLLING=true` no Docker) | Obrigatório | Demonstração | 1 | Vigente |
| RF-FRE-005 | Página inicial do jogo exibindo vila (substitui "Página inicial de boas-vindas") | Obrigatório | Demonstração: `/` → vila | 1 | **ADICIONADO** + **REMOVIDO** (página de boas-vindas) |
| RF-FRE-006 | Proxy de desenvolvimento para backend: `/api`, `/login`, `/logout`, `/css`, `/js`, `/images` → `BACKEND_URL` | Obrigatório | Inspeção: vite.config.ts | 1 | **ADICIONADO** |
| RF-FRE-007 | Roteamento em history mode (não hash); rotas: `/`, `/fazenda`, `/forja`, `/quartel`, `/masmorras`, `/batalhas/:id` | Obrigatório | Demonstração + inspeção: router/index.ts | 1 | **ADICIONADO** |
| RF-FRE-008 | **ADICIONADO (add-frontend-build)**: Build de produção integrado ao backend via script Python `scripts/build_front.py`; comando `make build_front` executa via Docker Compose, valida saída, copia assets para `static/app/` e `index.html` para template | Obrigatório | Demonstração: `make build_front` exit 0 | 3 | **ADICIONADO (add-frontend-build)**: [spec delta](../openspec/specs/frontend-app/spec.md) |
| RF-FRE-009 | **ADICIONADO (add-frontend-build)**: Rotas da SPA servidas pelo backend (história do browser permite recarregar página interna `/fazenda` diretamente) | Obrigatório | Teste: `AutenticacaoWebMvcTest` modificado | 2 | **ADICIONADO (add-frontend-build)**: [spec delta](../openspec/specs/frontend-app/spec.md) |
| RF-FRE-010 | **ADICIONADO (add-frontend-build)**: Assets do frontend públicos em `/app/**` (acessíveis sem autenticação) | Obrigatório | Teste: `AutenticacaoWebMvcTest` modificado | 1 | **ADICIONADO (add-frontend-build)**: [spec delta](../openspec/specs/frontend-app/spec.md) |

### 3.5 Workflow de Desenvolvimento (PRC)

Fonte: [`openspec/specs/subagent-dev-workflow/spec.md`](../openspec/specs/subagent-dev-workflow/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-PRC-001 | Código criado por subagentes Sonnet; trabalho executado em sessão dedicada | Obrigatório | Inspeção: commits com `Co-Authored-By` | 1 |
| RF-PRC-002 | Sessão limpa por tarefa (sem contexto acumulado); reuso de contexto entre tasks na mesma change | Obrigatório | Inspeção: `.claude/skills/dev-subagentes/SKILL.md` | 1 |
| RF-PRC-003 | Paralelismo de tasks independentes em ondas | Obrigatório | Inspeção: `resumo_utilizacao_agentes.md` | 1 |
| RF-PRC-004 | Relatório final com lista de agentes, tokens consumidos, status | Obrigatório | Inspeção: `resumo_utilizacao_agentes.md` | 1 |
| RF-PRC-005 | Skill global e local disponíveis em `~/.claude/skills` e `.claude/skills/` | Obrigatório | Inspeção + demonstração | 1 |

### 3.6 Dados do Jogo (DAD)

Fonte: delta [`openspec/specs/game-data/spec.md`](../openspec/specs/game-data/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-DAD-001 | 8 tabelas do jogo (vilas, prédios, canteiros, sementes, itens, unidades, ordens, batalhas) em migração V3 | Obrigatório | Inspeção: V3__jogo.sql | 1 |
| RF-DAD-002 | Uma vila por usuário; constraint unique (`uk_jogo_vilas_usuario`) | Obrigatório | Teste: `RepositoriosJogoTest` | 2 |
| RF-DAD-003 | Integridade de níveis e quantidades de recursos: constraints `ck_jogo_vilas_recursos` (≥0), `ck_jogo_vilas_masmorra_nivel` (1–5) | Obrigatório | Inspeção: SQL + teste | 1 |
| RF-DAD-004 | Fila de 1 ordem por categoria (`CONSTRUCAO`, `FORJA`, `TREINO`); constraint unique (`uk_jogo_ordens_vila_categoria`) | Obrigatório | Teste: `RepositoriosJogoTest` | 2 |
| RF-DAD-005 | Uma batalha em andamento por vila; constraint unique parcial `ux_jogo_batalhas_vila_em_andamento` where status='EM_ANDAMENTO' | Obrigatório | Teste: `MasmorraServiceTest` | 1 |
| RF-DAD-006 | Auditoria em todas as 8 tabelas do jogo: `criado_em`, `criado_por`, `alterado_em`, `alterado_por` (not null) | Obrigatório | Inspeção: V3 + entities | 1 |

### 3.7 Vila (VIL)

Fonte: delta [`openspec/specs/game-village/spec.md`](../openspec/specs/game-village/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-VIL-001 | Criação automática da vila no 1º acesso via GET `/api/jogo/vila`; estado inicial: 6 prédios nível 1, 1 canteiro TRIGO, recursos 300/400/300/50 | Obrigatório | Teste: `VilaControllerWebMvcTest` | 2 |
| RF-VIL-002 | Isolamento por usuário: acesso a vila de outro → 404 `NAO_ENCONTRADO` | Obrigatório | Teste: `VilaControllerWebMvcTest` | 1 |
| RF-VIL-003 | Recursos com capacidade por nível de armazém: `500 × 2^(N−1)`; inserção/consumo respeitam limite | Obrigatório | Teste: `VilaServiceTest` | 2 |
| RF-VIL-004 | Produção em tempo real calculada sob demanda por trechos entre conclusões de ordens; sem jobs em background | Obrigatório | Teste: `CalculadoraProducaoTest` | 3 |
| RF-VIL-005 | Conclusão de ordens vencidas sincronizada sob demanda ao consultar vila | Obrigatório | Teste: `VilaServiceTest` | 1 |
| RF-VIL-006 | Velocidade configurável por `JOGO_VELOCIDADE` (≥1); multiplica taxas, divide tempos (ceil) | Obrigatório | Teste: `JogoPropertiesTest` + `CalculadoraProducaoTest` | 2 |
| RF-VIL-007 | GET `/api/jogo/vila` retorna `VilaDto` com estado completo (recursos, prédios, canteiros, sementes, itens, unidades, ordens, batalha ativa) | Obrigatório | Teste: `VilaControllerWebMvcTest` | 2 |
| RF-VIL-008 | Catálogo de regras acessível via GET `/api/jogo/catalogo`; inclui custos, cultivos, inimigos, masmorras por nível | Obrigatório | Teste: `CatalogoTest` | 1 |
| RF-VIL-009 | Operações serializadas por vila: lock pessimista `PESSIMISTIC_WRITE` em `findByUsuarioIdParaAtualizacao` | Obrigatório | Inspeção: `VilaRepository` + teste | 2 |
| RF-VIL-010 | Erros de regra padronizados: 18 códigos (`CodigoErro`); 422 Unprocessable Entity com `{ "codigo": "...", "mensagem": "..." }` | Obrigatório | Teste: `ErroApiHandler` + `AcoesVilaControllerWebMvcTest` | 2 |

### 3.8 Prédios (PRD)

Fonte: delta [`openspec/specs/game-buildings/spec.md`](../openspec/specs/game-buildings/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-PRD-001 | 8 tipos de prédio (CENTRO_VILA, ARMAZEM, FAZENDA, SERRARIA, PEDREIRA, MINA_FERRO, FORJA, QUARTEL); níveis 0–5; custo base × 1,5^(N−1), tempo base × 2^(N−1) | Obrigatório | Teste: `CatalogoTest` | 1 |
| RF-PRD-002 | Cada prédio tem efeito (limite de nível, capacidade, canteiros, produção, capacidade do exército); consulta via catálogo | Obrigatório | Teste: `CatalogoTest` | 1 |
| RF-PRD-003 | Limite de nível pelo CENTRO_VILA: máx. nível prédio = nível do centro + 1 | Obrigatório | Teste: `ConstrucaoServiceTest` | 2 |
| RF-PRD-004 | Pré-requisitos (ex.: QUARTEL requer nível ≥3 CENTRO_VILA); validação antes de débito | Obrigatório | Teste: `ConstrucaoServiceTest` | 2 |
| RF-PRD-005 | Fila de construção única por vila (`CategoriaOrdem.CONSTRUCAO`); nova ordem rejeita se há ordem ativa | Obrigatório | Teste: `ConstrucaoServiceTest` | 2 |
| RF-PRD-006 | Nível máximo 5; tentativa de melhorar nível 5 retorna erro `NIVEL_MAXIMO` | Obrigatório | Teste: `ConstrucaoServiceTest` | 1 |
| RF-PRD-007 | Débito de recursos no início da ordem (ou falha); efeito aplicado na conclusão; ordem armazenada com `nivel`, `tipo`, `data_conclusao` | Obrigatório | Teste: `ConstrucaoServiceTest` | 2 |

### 3.9 Fazenda (FAZ)

Fonte: delta [`openspec/specs/game-farming/spec.md`](../openspec/specs/game-farming/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-FAZ-001 | Canteiros iguais ao nível da fazenda (máx. 5); posição 1–5 | Obrigatório | Teste: `VilaServiceTest` | 1 |
| RF-FAZ-002 | 4 cultivos (TRIGO: 20/h sem semente; MILHO: 30/h masmorra≥1; BATATA: 45/h ≥2; ABOBORA_DOURADA: 70/h ≥4); produção por hora em recurso COMIDA | Obrigatório | Teste: `FazendaServiceTest` | 2 |
| RF-FAZ-003 | Plantio consome 1 semente; rejeita se nenhuma disponível ou cultivo ≠ TRIGO sem masmorra > 0 | Obrigatório | Teste: `FazendaServiceTest` | 2 |
| RF-FAZ-004 | Trocar cultivo em canteiro já plantado preserva produção acumulada | Obrigatório | Teste: `FazendaServiceTest` | 1 |

### 3.10 Forja (FOR)

Fonte: delta [`openspec/specs/game-forge/spec.md`](../openspec/specs/game-forge/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-FOR-001 | 5 modelos (ESPADA, LANCA, ARCO, ARMADURA_COURO, ARMADURA_FERRO) com atributos (ataque, defesa, alcance) por nível 1–5 | Obrigatório | Teste: `CatalogoTest` | 1 |
| RF-FOR-002 | Receitas: custo por modelo/nível (base × L × quantidade); tempo base por modelo (ex.: ESPADA 60 s); tempo = base × L × quantidade / velocidade | Obrigatório | Teste: `ForjaServiceTest` | 1 |
| RF-FOR-003 | Nível máximo de item limitado pela forja (nível forja N → max item nível N) | Obrigatório | Teste: `ForjaServiceTest` | 1 |
| RF-FOR-004 | Uma ordem de forja por vez (`CategoriaOrdem.FORJA`); fila de 1 | Obrigatório | Teste: `ForjaServiceTest` | 1 |
| RF-FOR-005 | Item criado e armazenado na vila ao concluir ordem; status DISPONIVEL | Obrigatório | Teste: `ForjaServiceTest` | 1 |
| RF-FOR-006 | Validação de ordem: quantidade 1–5, nível 1–5, modelo válido; falha = 422 com código apropriado | Obrigatório | Teste: `ForjaServiceTest` | 1 |

### 3.11 Quartel (EXE)

Fonte: delta [`openspec/specs/game-army/spec.md`](../openspec/specs/game-army/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-EXE-001 | 3 tipos de tropa (SOLDADO, ARQUEIRO, LANCEIRO) com atributos derivados de arma+armadura (HP, ataque, defesa, alcance, movimento) | Obrigatório | Teste: `QuartelServiceTest` | 2 |
| RF-EXE-002 | Treino consome armas e armaduras (quantidade N) reservadas no início; validação de disponibilidade e compatibilidade | Obrigatório | Teste: `QuartelServiceTest` | 1 |
| RF-EXE-003 | Validação: tipo válido, armaNível 1–5, armaduraModelo/Nível válidos, quantidade 1–15, itens suficientes, não reservados/equipados | Obrigatório | Teste: `QuartelServiceTest` | 2 |
| RF-EXE-004 | Tropas liberadas (acesso) conforme nível do quartel | Obrigatório | Teste: `QuartelServiceTest` | 1 |
| RF-EXE-005 | Capacidade do exército escalonada: cap = `3 × nível_quartel − unidades_vivas − Σ quantidade_ordens_TREINO`; nova ordem rejeita se quantidade > capacidade | Obrigatório | Teste: `QuartelServiceTest` | 2 |
| RF-EXE-006 | Uma ordem de treino por vez (`CategoriaOrdem.TREINO`); fila de 1 | Obrigatório | Teste: `QuartelServiceTest` | 1 |
| RF-EXE-007 | Nomes e sobrenomes: par sorteado ao treino, sufixo ordinal para duplicatas (`"Ana Silva (2)"`); contagem por vila, histórica (inclui mortas pré-V4); ordinal único com (vila_id, nome, sobrenome) | Obrigatório | Teste: `NumeradorNomesTest`, `VilaServiceTest` | 2 |
| RF-EXE-008 | Treino em lote: quantidade (1–15) selecionada; nível arma/armadura e modelo configuráveis; botão "Máx." calcula máximo treinável; validações e rejeição 422 se insuficiente | Obrigatório | Teste: `QuartelServiceTest`, demonstração frontend | 2 |
| RF-EXE-009 | Tela de detalhe (`/quartel/unidades/{id}`): exibição de 9 slots de equipamento (ARMA, ARMADURA, CABECA, BOTA, LUVA, COLAR, ANEL_1/2/3); troca de Arma/Armadura em slots preenchidos; slots futuros vazios; bloqueado em masmorra; nome clicável na lista abre detalhe | Obrigatório | Teste: `AcoesVilaControllerWebMvcTest`, demonstração frontend | 2 |
| RF-EXE-010 | Sufixo ordinal para nomes repetidos: contagem por vila/nome, histórica, ordem determinística no lote; `NumeradorNomes.proximoOrdinal()` incrementa contador persistido; sem decremento em morte | Obrigatório | Teste: `NumeradorNomesTest`, `VilaServiceTest` | 1 |
| RF-EXE-011 | Troca de Arma/Armadura: POST `/api/jogo/unidades/{id}/equipamento` {slot, itemId}; item antigo → DISPONIVEL, novo → EQUIPADO; bloqueado se `EM_MASMORRA` (422 UNIDADE_EM_MASMORRA); arma/armadura obrigatórias (sem desequipar) | Obrigatório | Teste: `EquipamentoServiceTest`, `AcoesVilaControllerWebMvcTest` | 2 |

### 3.12 Masmorras (COM)

Fonte: delta [`openspec/specs/game-dungeon-combat/spec.md`](../openspec/specs/game-dungeon-combat/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-COM-001 | Masmorras níveis 1–5; nível atual liberado = número de masmorras vencidas; nível 1 liberada no início | Obrigatório | Teste: `MasmorraServiceTest` | 1 |
| RF-COM-002 | Início de batalha: até 4 unidades apontadas por lista de IDs; validação de disponibilidade, vigência na masmorra | Obrigatório | Teste: `MasmorraServiceTest` + `MasmorraControllerWebMvcTest` | 2 |
| RF-COM-003 | Mapa 8×8 com obstáculos (posições fixas por nível), spawns de inimigos, posições iniciais de jogador; 1–5 inimigos por nível | Obrigatório | Teste: `MotorCombateTest` | 2 |
| RF-COM-004 | Movimento: BFS até movimento do combatente; para no obstáculo | Obrigatório | Teste: `MotorCombateTest` | 2 |
| RF-COM-005 | Ataque: verifica alcance Manhattan, aplica dano = max(1, ataque − defesa do alvo) | Obrigatório | Teste: `MotorCombateTest` | 3 |
| RF-COM-006 | Defesa: reduz dano em dobro (defesa × 2) durante turno; reverte ao final | Obrigatório | Teste: `MotorCombateTest` | 1 |
| RF-COM-007 | IA dos inimigos: seleciona alvo por distância (menor primeiro), desempate por HP (maior primeiro); mover + atacar no mesmo turno | Obrigatório | Teste: `MotorCombateTest` | 2 |
| RF-COM-008 | Turnos: contador global, incrementado a cada ação; máximo 30; validação de atualização (400 se desatualizado vs. banco); `@Version` optimistic lock | Obrigatório | Teste: `MotorCombateTest` | 2 |
| RF-COM-009 | Fim de batalha: vitória (todos inimigos derrotados), derrota (todas unidades do jogador derrotadas), render (ação do jogador); loot gerado em vitória | Obrigatório | Teste: `MotorCombateTest` + `GeradorLootTest` | 3 |
| RF-COM-010 | Estado, log e loot persistidos em colunas JSON (`text`) na tabela `jogo_batalhas` | Obrigatório | Teste: `MasmorraServiceTest` | 1 |

### 3.13 Loot (LOO)

Fonte: delta [`openspec/specs/game-dungeon-loot/spec.md`](../openspec/specs/game-dungeon-loot/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-LOO-001 | Recursos garantidos por nível: `40N` comida, `50N` madeira, `50N` pedra, `20N` ferro (N = nível masmorra) | Obrigatório | Teste: `GeradorLootTest` | 1 |
| RF-LOO-002 | Rolagens de loot: `1 + ceil(N/2)` rolagens; distribuição de chance 35% sementes, 25% material, 40% item | Obrigatório | Teste: `GeradorLootTest` | 2 |
| RF-LOO-003 | Sementes: distribuição 60% trigo, 30% milho, 10% batata/abóbora; filtro por nível liberado | Obrigatório | Teste: `GeradorLootTest` | 1 |
| RF-LOO-004 | Itens: nível `min(5, N + [0\|1])` (aleatório); modelo por chance (35% espada, 25% lança, 40% arco/armaduras) | Obrigatório | Teste: `GeradorLootTest` | 1 |
| RF-LOO-005 | Nenhum loot na derrota; render não gera loot | Obrigatório | Teste: `GeradorLootTest` | 1 |
| RF-LOO-006 | Loot registrado na batalha (coluna `loot` JSON) com ID gerado no banco | Obrigatório | Teste: `MasmorraServiceTest` | 1 |

### 3.14 Frontend do Jogo (UIJ)

Fonte: delta [`openspec/specs/game-frontend/spec.md`](../openspec/specs/game-frontend/spec.md)

| ID | Requisito | Prioridade | Verificação | Cenários |
|---|---|---|---|---|
| RF-UIJ-001 | Navegação por menu: Vila, Fazenda, Forja, Quartel, Masmorras, Sair (links/rotas) | Obrigatório | Demonstração | 1 |
| RF-UIJ-002 | Redirecionamento ao login: 401 → GET `/login` (redirect automático) | Obrigatório | Teste: `ApiSegurancaWebMvcTest` + demonstração | 1 |
| RF-UIJ-003 | Painel de recursos fixo (comida/madeira/pedra/ferro) com produção por hora e capacidade | Obrigatório | Demonstração + demonstração do componente | 1 |
| RF-UIJ-004 | Vila: prédios em cards com nível, custo da próxima melhoria, botão "Melhorar"; fila com contagem regressiva | Obrigatório | Demonstração | 1 |
| RF-UIJ-005 | Fazenda: canteiros com cultivo, sementes, produção; botão "Plantar" com seletor | Obrigatório | Demonstração | 1 |
| RF-UIJ-006 | Forja: seletor de modelo/nível/quantidade, custo total, fila com contagem; botão "Forjar" | Obrigatório | Demonstração | 1 |
| RF-UIJ-007 | Quartel: seletor de tipo, arma, armadura, custo, fila; botão "Treinar" | Obrigatório | Demonstração | 1 |
| RF-UIJ-008 | Masmorras: botão por nível (liberado/bloqueado); seletor de unidades (até 4); POST → battleId → redireciona para `/batalhas/:id` | Obrigatório | Demonstração | 1 |
| RF-UIJ-009 | Batalha: grade clicável 8×8; combatente selecionado com ações (Mover, Atacar, Defender, Encerrar Turno, Render); log de ações; resultado com loot | Obrigatório | Demonstração | 2 |
| RF-UIJ-010 | Mensagens de erro: 422/409 em Toast; código e mensagem legíveis em pt-BR | Obrigatório | Demonstração | 2 |
| RF-UIJ-011 | Sem cálculo de regras no frontend: todos os cálculos (custo, tempo, dano) via API | Obrigatório | Inspeção: `api/jogo.ts` + serviços | 1 |

---

## 4. Requisitos não funcionais

| ID | Característica ISO 25010 | Enunciado | Critério | Verificação |
|---|---|---|---|---|
| **RNF-SEG-001** | Segurança/autenticidade | Toda rota exige autenticação, exceto `/login`, `/app/**` (assets públicos) e estáticos; `/api/**` anônimo → 401 | Implementado em SecurityConfig (`/app/**` em permitAll) | Teste + inspeção |
| **RNF-SEG-002** | Segurança/integridade | CSRF em todo POST (form `_csrf`; SPA `X-XSRF-TOKEN` via header) | Habilitado em SecurityConfig | Teste `ApiSegurancaWebMvcTest` |
| **RNF-SEG-003** | Segurança/confidencialidade | Senha com `DelegatingPasswordEncoder` (BCrypt, prefixo `{bcrypt}`) | Implementado em UsuarioDetailsService | Inspeção + teste |
| **RNF-SEG-004** | Segurança | Cookie `HttpOnly`, `SameSite=Lax`, `Secure` (config `SESSION_COOKIE_SECURE`); troca de ID ao login; timeout 30 min | Implementado em SecurityConfig | Inspeção + teste SessaoServiceTest |
| **RNF-SEG-005** | Segurança/não-repúdio | Falha de login genérica (sem enumeração) | Mensagem "Usuário ou senha inválidos." | Inspeção + demonstração |
| **RNF-SEG-006** | Segurança/confidencialidade | Isolamento por usuário: recurso de outro usuário → 404 | VilaService valida propriedade via `usuarioId` | Teste `VilaControllerWebMvcTest` |
| **RNF-AUD-001** | Segurança/responsabilização | Auditoria `criado_em/por`, `alterado_em/por` em todas as tabelas; registro de sessões (IP, User-Agent) | Implementado em JPA Auditing + tabela `sessoes` | Inspeção + teste `SessaoServiceTest` |
| **RNF-CON-001** | Confiabilidade | Operações serializadas por vila (lock pessimista); `@Version` e turno na batalha (409 se desatualizado) | Implementado em VilaRepository + MotorCombate | Teste `MotorCombateTest` |
| **RNF-CON-002** | Confiabilidade/integridade | Constraints de banco garantem invariantes (1 vila/user, 1 fila/cat, 1 batalha ativa) | Constraints unique + unique parcial | Inspeção V3 + teste `RepositoriosJogoTest` |
| **RNF-MAN-001** | Manutenibilidade | Esquema só por Flyway; Hibernate `ddl-auto=validate` | V1, V2, V3 em `src/main/resources/db/migration/` | Inspeção + demonstração |
| **RNF-MAN-002** | Manutenibilidade/modularidade | Pacotes por domínio (acesso, auditoria, seguranca, web, jogo.*) | 12 pacotes raiz em `com.example.loginbase` | Inspeção: `git ls-files src/main/java` |
| **RNF-TES-001** | Manutenibilidade/testabilidade | Tempo (`Clock`) e aleatoriedade (`Aleatorio`) injetáveis; motor determinístico | Implementado com `@Component` injetável + RelogioAjustavel/AleatorioSequencia | Teste `MotorCombateTest` |
| **RNF-POR-001** | Portabilidade | Ambiente reproduzível via Docker Compose com profiles, WSL2 | docker-compose.yml + Makefile | Demonstração: `make up` |
| **RNF-USA-001** | Usabilidade | Interface e mensagens de erro em pt-BR; erros de regra em Toast | Strings em `CodigoErro` + frontend Toast | Demonstração |
| **RNF-DES-001** | Eficiência | Produção calculada sob demanda; polling 5 s; sem jobs background | CalculadoraProducao + useVila.ts | Inspeção + teste |

---

## 5. Regras de negócio

### RN-ACE: Acesso e Contato

1. E-mail é normalizado para minúsculas na busca e armazenamento.
2. Celular aceita DDD + número (11 dígitos); armazenado só números; busca normaliza a entrada.
3. Perfil vigente = data_inicial ≤ hoje ≤ data_final (ou data_final nula).
4. Conta sem perfil vigente = desabilitada (falha no login).
5. Username sempre e-mail (identificador único).

### RN-VIL: Vila e Estado

1. Vila criada no 1º acesso com estado inicial: CENTRO_VILA nível 1, ARMAZEM/FAZENDA/SERRARIA/PEDREIRA nível 1, MINA_FERRO/FORJA/QUARTEL nível 0, 1 canteiro TRIGO, recursos 300/400/300/50.
2. Produção integrada ao longo do tempo (milésimos); recurso = `base + produção_acumulada`.
3. Capacidade por nível armazém: `500 × 2^(N−1)` unidades (× 1000 em milésimos) por tipo; excedente = descartado.
4. Velocidade configurada por `JOGO_VELOCIDADE` (inteiro ≥1); aplica-se a taxas (× velocidade) e tempos (÷ velocidade, ceil).

### RN-PRD: Construção

1. Ordem de validação: `NIVEL_MAXIMO` → limite do centro da vila (`REQUISITO_NAO_ATENDIDO`) → pré-requisitos FORJA/QUARTEL (`REQUISITO_NAO_ATENDIDO`) → `FILA_OCUPADA` → `RECURSOS_INSUFICIENTES`.
2. Débito no início da ordem; não há estorno em falha (falha antes de débito).
3. Nenhum outro prédio pode passar do nível do centro da vila; ex.: centro nível 3 → demais prédios até nível 3. `FORJA` exige `MINA_FERRO` ≥ 1 e `QUARTEL` exige `FORJA` ≥ 1.

### RN-FAZ: Fazenda

1. Nº canteiros = nível fazenda (máx. 5).
2. Plantio de cultivo requer 1 semente (consumida); cultivos libertos por masmorra (TRIGO sempre).
3. Troca de cultivo preserva produção (não zera o relógio).

### RN-FOR: Forja

1. Custo item = custo_base × nível × quantidade.
2. Tempo forja = (tempo_base × nível × quantidade) / velocidade, com `ceil`.
3. Nível máx. item = nível forja.

### RN-EXE: Exército

1. Tropas liberadas conforme nível do quartel (ex.: LANCEIRO requer quartel ≥3).
2. Atributos derivados: HP/ataque/defesa/alcance do tipo + modificadores da arma/armadura.
3. Capacidade exército = `3 × nível quartel − unidades_vivas − Σ quantidade_ordens_TREINO_em_andamento`; exceder = rejeição 422 CAPACIDADE_EXERCITO.
4. Treino em lote: N armas (modelo exigido pelo tipo, nível exato) + N armaduras (modelo/nível exato) reservadas no início; quantidade 1–15.
5. Nomes: cada unidade recebe nome + sobrenome sorteados de listas; nomes duplicados ganham sufixo ordinal ("Ana Silva (2)"); contagem por vila, histórica (não decrementa com morte).
6. Troca de equipamento (Arma/Armadura): POST `/api/jogo/unidades/{id}/equipamento`; proibida se `EM_MASMORRA`; item antigo → DISPONIVEL, novo → EQUIPADO; sem desequipar.

### RN-COM: Combate

1. Dano = `max(1, ataque_combatente − defesa_alvo)` (defesa dobrada se em ação DEFENDER).
2. Turno de inimigo = IA (seleciona alvo por menor distância Manhattan, desempate por menor HP e depois menor id) + mover BFS + atacar se em alcance, tudo no mesmo turno.
3. Máximo 30 turnos; ultrapassar = derrota automática.
4. HP restaurado entre batalhas (não persistido).

### RN-LOO: Loot

1. Recursos garantidos: `40×N` comida, `50×N` madeira, `50×N` pedra, `20×N` ferro (N = nível).
2. Faixa 0–34 da rolagem gera 1 semente sorteada entre as liberadas pelo nível da masmorra, com pesos MILHO 60, BATATA 30, ABOBORA_DOURADA 10 (TRIGO não tem semente).
3. Faixa 60–99 gera 1 item: modelo sorteado com chance igual entre os 5 modelos; nível `min(5, N + 0 ou 1)`. (35/25/40 % são as faixas da rolagem: semente / ferro extra / item.)
4. Nenhum loot em derrota ou render.

---

## 6. Interfaces externas

### UI (Apresentação)

- **Login**: Thymeleaf em `/login` (formulário POST com campos `login`, `senha`, `_csrf`).
- **Página inicial protegida**: Thymeleaf em `/` servindo a SPA Vue 3 após `make build_front`.
- **Jogo**: SPA Vue 3 em 6 rotas, servido via proxy Vite em `localhost:5173` (dev), backend na porta 80 em produção após build.

### API REST

- **Base**: `/api/jogo/` em JSON.
- **Autenticação**: cookie `JSESSIONID`.
- **CSRF**: header `X-XSRF-TOKEN` (lido do cookie `XSRF-TOKEN`).
- Endpoints: 9 (GET/POST para vila, prédios, canteiros, forja, quartel, masmorras, batalhas).

Detalhes em [06-api-rest.md](06-api-rest.md).

### Banco de dados

- **Postgres 17** com 14 tabelas (6 acesso + auditoria, 8 jogo).
- **Migrações Flyway**: V1 (acesso), V2 (perfil ADMIN), V3 (jogo).
- Consultas: JPA com lock pessimista; sem stored procedures.

Detalhes em [05-modelo-dados.md](05-modelo-dados.md).

---

## 7. Requisitos de dados

- Armazenamento persistente em PostgreSQL 17.
- Todos os dados de usuários, vila e batalhas auditados (criação/modificação).
- Backups: não definido (fora de escopo).

---

## 8. Apêndice: Contagem e matriz de cobertura

### 8.1 Resumo de requisitos

**Vigente (baseline commit `454ae58`)**:

| Capability | RF | RNF | RN | Cenários | Testes automatizados |
|---|---|---|---|---|---|
| ACD | 7 | — | — | 21 | 3 testes (`UsuarioTest`, `UsuarioAuditorAwareTest`, `LoginBaseApplicationTests`) |
| AUT | 10 (9 da spec base + 1 ADDED; 1 MODIFIED) | 5 | 1 | 28 (+8 delta) | 8 testes (auth + session) |
| AMB | 6 | 1 | — | 13 | Inspeção (Docker/Makefile) |
| FRE | 7 (4 da spec base + 3 ADDED; 1 REMOVED) | 1 | — | 7 (+7 delta) | Build (vue-tsc); demonstração |
| PRC | 5 | — | — | 8 | Inspeção (skill + commits) |
| DAD | 6 | 2 | — | 12 | 2 testes (`RepositoriosJogoTest`, migrations) |
| VIL | 10 | 2 | 1 | 18 | 4 testes (vila, catálogo, calculadora) |
| PRD | 7 | — | 1 | 15 | 2 testes (construção, catálogo) |
| FAZ | 4 | — | 1 | 10 | 1 teste (fazenda) |
| FOR | 6 | — | 1 | 12 | 1 teste (forja) |
| EXE | 6 | — | 1 | 17 | 1 teste (quartel) |
| COM | 10 | 1 | 1 | 33 | 3 testes (motor, masmorra, controller) |
| LOO | 6 | — | 1 | 16 | 2 testes (gerador loot, masmorra) |
| UIJ | 11 | 1 | — | 32 | Build (npm) + demonstração manual |
| **Vigente total** | **101** | **15** | **8 cat.** | **262** | **27 classes / 239 testes** |

**Com change `add-frontend-build` (implementada; delta em sync)**:

| Capability | RF | RNF | Cenários adicionais |
|---|---|---|---|
| AUT | — (RF-AUT-005 MODIFICADO) | — | +1 (RF-AUT-005: 1 → 2 cenários) |
| FRE | +3 (RF-FRE-008, FRE-009, FRE-010, build + rotas + assets públicos) | — | +6 (3 + 2 + 1) |
| **Total implementado** | **104** | **15** | **+7 (262 → 269)** |

Observação: AUT vigente = 10 (base 9 + ADDED 1); FRE vigente = 7 (base 4 + ADDED 3). Com change implementada: AUT = 10 (RF-AUT-005 MODIFICADO, sem novo RF), FRE = 10 (+3 RF), total 104 RF vigentes.

### 8.2 Códigos de erro (CodigoErro.java)

| Código | HTTP | Quando ocorre |
|---|---|---|
| `RECURSOS_INSUFICIENTES` | 422 | Vila não tem recursos para ação |
| `FILA_OCUPADA` | 422 | Já existe ordem ativa da categoria |
| `NIVEL_MAXIMO` | 422 | Prédio está no nível máximo |
| `REQUISITO_NAO_ATENDIDO` | 422 | Pré-requisito de prédio não atendido |
| `CANTEIRO_INEXISTENTE` | 422 | Canteiro não existe (posição inválida) |
| `SEMENTE_INDISPONIVEL` | 422 | Não há sementes para plantar |
| `ITEM_INDISPONIVEL` | 422 | Item não existe ou reservado |
| `CAPACIDADE_EXERCITO` | 422 | Exército no máximo |
| `MASMORRA_BLOQUEADA` | 422 | Masmorra não liberada |
| `BATALHA_EM_ANDAMENTO` | 422 | Já existe batalha ativa |
| `UNIDADE_INDISPONIVEL` | 422 | Unidade não pode entrar em batalha |
| `ESQUADRAO_INVALIDO` | 422 | Esquadrão vazio ou > 4 unidades |
| `ACAO_INVALIDA` | 422 | Ação de combate não permitida (ex.: movimento impossível) |
| `BATALHA_ENCERRADA` | 422 | Batalha já terminou; sem novas ações |
| `TURNO_DESATUALIZADO` | 409 | Turno local ≠ turno banco (reconectar) |
| `CONFLITO` | 409 | Lock otimista falhou (`@Version`) |
| `NAO_ENCONTRADO` | 404 | Recurso não existe ou pertence a outro usuário |
| `REQUISICAO_INVALIDA` | 400 | Validação falhou ou JSON inválido |

---

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.2.0 | 2026-09-27 | Change add-frontend-build implementada: remove marcadores de previsto | Adiel, com apoio de agentes Claude |
| 1.1.0 | 2026-09-27 | Atualização para a change add-frontend-build (add-frontend-build implementada) | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
