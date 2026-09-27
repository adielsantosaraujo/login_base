# login_base — Documentação

| Campo | Valor |
|---|---|
| Versão | 1.1.0 |
| Data | 2026-09-27 |
| Status | Vigente — baseline do commit `454ae58` + change `add-frontend-build` implementada |
| Público | todos |

> Mapa completo da documentação do sistema login_base — um framework de autenticação e autorização que evoluiu para um city builder tático baseado em web, com build de frontend integrado.

---

## 1. Sobre o Sistema

**login_base** é uma aplicação web Spring Boot + Vue 3 que integra:

- **Autenticação e controle de acesso**: login com e-mail/celular, sessão HTTP segura, perfis e permissões, registro de auditoria.
- **City builder tático**: o usuário autenticado gerencia uma vila (produção de recursos, construção de prédios, treinamento de tropas) e enfrenta masmorras com combate tático por turnos.
- **Processo de desenvolvimento**: spec-driven com OpenSpec, orquestração por subagentes Claude (Opus/Sonnet/Haiku), ondas paralelas de tasks, e versionamento com git.

A plataforma é reproduzível via Docker Compose no WSL2, com testes automatizados contra Postgres, e segue normas de documentação ISO/IEC/IEEE 29148 (SRS), arc42 (arquitetura) e Keep a Changelog (histórico).

**Repositório:** github.com/adielsantosaraujo/login_base · **Baseline:** commit `454ae58` (2026-09-27)

---

## 2. Mapa dos Documentos

| Nº | Título | Propósito | Público |
|---|---|---|---|
| **01** | [Visão do produto](01-visao-produto.md) | Contexto, stakeholders, escopo, objetivos, não-objetivos, premissas | Todos, stakeholders |
| **02** | [Requisitos (SRS)](02-requisitos.md) | 104 requisitos funcionais vigentes, 15 não-funcionais, 8 regras de negócio | Desenvolvedores, QA, revisores |
| **03** | [Casos de uso e histórias](03-casos-de-uso.md) | 12 casos de uso com fluxos, 12 histórias com critérios Gherkin | QA, PO, desenvolvedores |
| **04** | [Arquitetura (arc42 + C4)](04-arquitetura.md) | 12 seções arc42 + C4 níveis 1–3 em Mermaid, 23 ADRs vigentes | Desenvolvedores, arquitetos |
| **adr/** | [ADRs (decisões)](adr/README.md) | 23 decisões arquiteturais vigentes no formato MADR 4.0 | Arquitetos, desenvolvedores |
| **05** | [Modelo de dados](05-modelo-dados.md) | ER bidirecional, 14 tabelas, dicionário, constraints, enums | Desenvolvedores, DBA |
| **06** | [API REST](06-api-rest.md) | 9 endpoints, esquemas DTOs, mapeamento de erros, exemplos cURL | Frontend/backend, QA |
| **07** | [Segurança](07-seguranca.md) | STRIDE, OWASP ASVS L1, LGPD, autenticação, sessão, CSRF | Desenvolvedores, revisores |
| **08** | [Plano de testes](08-plano-testes.md) | Estratégia, 239 testes (27 classes) no baseline; contagem após add-frontend-build a confirmar, levels, rastreabilidade | QA, desenvolvedores |
| **09** | [Guia do desenvolvedor](09-guia-desenvolvedor.md) | Onboarding, instalação, tutorial, how-tos, convenções | Novos desenvolvedores |
| **10** | [Implantação e operação](10-implantacao-operacao.md) | Topologia, profiles, variáveis, Makefile, runbooks | Desenvolvedores/operação |
| **11** | [Processo de desenvolvimento](11-processo-desenvolvimento.md) | OpenSpec, orquestração por subagentes, git, Definition of Done | Desenvolvedores, líderes |
| **12** | [Game Design Document (GDD)](12-gdd.md) | Conceito, core loop, recursos, prédios, combate, loot, UI | Game designers, QA |
| **13** | [Manual do jogador](13-manual-jogador.md) | Acesso, primeiros passos, telas, como fazer, dicas (sem termos técnicos) | Jogador final |
| **14** | [Glossário](14-glossario.md) | Termos de domínio, acesso, técnicos, processo (alfabético) | Todos |
| **15** | [Rastreabilidade (RTM)](15-rastreabilidade.md) | Matriz RF → spec → task → código → teste (104 RF vigentes) | QA, revisores |
| **16** | [Histórico e changelog](16-historico-changelog.md) | Uma seção por change (7 total: 6 arquivadas + 1 implementada), com commits e estatísticas | Todos |
| **17** | [Riscos, dívida e roadmap](17-riscos-divida-roadmap.md) | 16 divergências D-01…D-16, dívida técnica, riscos, roadmap | Líderes, arquitetos |

---

## 3. Roteiros de Leitura por Público

### Novo desenvolvedor
Comece aqui para entender o sistema de ponta a ponta.

1. [01 — Visão](01-visao-produto.md) (5 min): O que é o sistema?
2. [09 — Guia do desenvolvedor](09-guia-desenvolvedor.md) (15 min): Como levantar o ambiente e rodá-lo?
3. [04 — Arquitetura](04-arquitetura.md) (20 min): Visão geral da solução (C4 níveis 1–3).
4. [11 — Processo de desenvolvimento](11-processo-desenvolvimento.md) (15 min): Como fazer uma change?
5. [14 — Glossário](14-glossario.md) (referência): Termos do domínio.
6. [05 — Modelo de dados](05-modelo-dados.md) ou [06 — API REST](06-api-rest.md) (conforme necessário): Aprofundar em um componente específico.

**Tempo total:** ~1 hora.

### Revisor/Arquiteto
Foco em decisões, qualidade, risco e cobertura.

1. [01 — Visão](01-visao-produto.md): Escopo e não-objetivos (2 min).
2. [02 — Requisitos](02-requisitos.md) (20 min): Os 101 requisitos, ordem de implementação, divergências.
3. [04 — Arquitetura](04-arquitetura.md) + [adr/](adr/README.md) (25 min): Decisões principais (3 primeiras, últimas); C4.
4. [05 — Modelo de dados](05-modelo-dados.md) §5 (Constraints) e [07 — Segurança](07-seguranca.md) (15 min): Integridade, segurança.
5. [08 — Plano de testes](08-plano-testes.md) (10 min): Cobertura, estratégia.
6. [17 — Riscos, dívida e roadmap](17-riscos-divida-roadmap.md) (15 min): Divergências, riscos residuais, dívida.

**Tempo total:** ~1h 30min.

### QA / Tester
Requisitos, casos de uso, testes, rastreabilidade.

1. [02 — Requisitos](02-requisitos.md) (20 min): 101 RF organizados por capability.
2. [03 — Casos de uso](03-casos-de-uso.md) (15 min): UC-01…UC-12 com fluxos e histórias Gherkin.
3. [08 — Plano de testes](08-plano-testes.md) (15 min): Estratégia, inventário de testes, última execução (239 testes).
4. [15 — Rastreabilidade](15-rastreabilidade.md) (15 min): RF → teste; lacunas de cobertura (UIJ, FRE, AMB, PRC são manual/build).
5. [12 — GDD](12-gdd.md) §9 (Combate) e §10 (Loot): Regras de negócio críticas para teste.

**Tempo total:** ~1 hora.

### Jogador (usuário final)
Instruções para jogar, sem termos técnicos.

1. [13 — Manual do jogador](13-manual-jogador.md): Acesso, telas, como fazer, dicas.
2. [12 — GDD](12-gdd.md) §2–§6: Recursos, prédios, fazenda, forja, tropas (para estratégia).

**Tempo total:** 20–30 min.

### Operação (produção futura)
Ambiente, configuração, runbooks.

1. [10 — Implantação e operação](10-implantacao-operacao.md): Variáveis, profiles, runbook (sintomas e soluções).
2. [04 — Arquitetura](04-arquitetura.md) §5 (Visão de blocos) e [10 — Implantação e operação](10-implantacao-operacao.md) §1 (Topologia): Topologia.

**Tempo total:** 15 min.

---

## 4. Convenções

### 4.1 Idioma, estilo, referências

- **Português do Brasil** em todo texto; termos de código (classe, rota, enum), nomes técnicos (RFC, CSRF, SPA) e OpenSpec keywords (`Requirement`, `Scenario`, `SHALL`, `MUST`) ficam como no código/spec, entre crases.
- **Frases curtas**, voz ativa, sem emojis.
- **Tabelas** para dados tabulados; **listas** para sequências.
- **Nunca inventar** fatos não presentes nas fontes; quando código divergir de spec, **o código prevalece** e a divergência é registrada com marca `> **Divergência:** ...` em vermelho.

### 4.2 Cabeçalho padrão de todo arquivo

```markdown
# <Título do documento>

| Campo | Valor |
|---|---|
| Versão | 1.0.0 |
| Data | 2026-09-27 |
| Status | Vigente — baseline do commit `454ae58` |
| Modelo/norma | <ex.: ISO/IEC/IEEE 29148:2018> |
| Público | <ex.: desenvolvedores> |

> Parte da [documentação do login_base](README.md). <uma frase de propósito>.
```

Ao final de todo arquivo: seção `## Histórico de revisões` com uma linha `| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |`.

### 4.3 Links

- **Entre documentos**: relativos (`[Arquitetura](04-arquitetura.md#5-visão-de-blocos--c4-nível-2)`).
- **Para arquivos do repositório**: relativos a partir de `docs/` (ex.: `[SecurityConfig.java](../src/main/java/com/example/loginbase/seguranca/SecurityConfig.java)`). **Não usar** barras iniciais (`/`).
- **Para ADRs**: número fixo (ex.: `[ADR 0001](adr/0001-spring-boot-java-25-maven.md)`).
- **Para specs vigentes**: `../openspec/specs/<cap>/spec.md` (specs principais arquivadas).
- **Para specs da change jogo**: `../openspec/changes/archive/2026-09-27-add-city-builder-game/specs/<cap>/spec.md` (sufixo primeira ocorrência: "delta da change `add-city-builder-game`, arquivada em 2026-09-27").
- **Âncoras**: minúsculas com hífens (ex.: `#5-visão-de-blocos`); preferir linkar só o arquivo em dúvida.

### 4.4 Como citar specs OpenSpec

Formato: `` `<capability>` › *Requirement: <título exato>* `` + link.

Exemplo: `` `user-authentication` › *Requirement: Página inicial segura* `` → [spec](../openspec/specs/user-authentication/spec.md).

### 4.5 Diagramas

- **Somente Mermaid** em blocos `` ```mermaid ```.
- **C4** (flowchart): nós com `"Nome<br/>[Tipo: tecnologia]<br/>descrição"`, `subgraph` para fronteiras.
- **ER** com `erDiagram`; fluxos com `sequenceDiagram`; estados com `stateDiagram-v2`.
- **Legenda** de 1–2 linhas logo abaixo de cada diagrama.

### 4.6 Identificadores fixos (contrato entre lotes — usar exatamente)

**Requisitos funcionais (RF):** `RF-<CAP>-NNN` (104 total vigentes, em `02-requisitos.md` tabela §3).

| Prefixo | Capability | Qtde | Nota |
|---|---|---|---|
| ACD | `access-control-data` | 7 | Vigente |
| AUT | `user-authentication` | 10 | Vigente (modificado pela change jogo) |
| AMB | `docker-dev-environment` | 6 | Vigente |
| FRE | `frontend-app` | 10 | Vigente |
| PRC | `subagent-dev-workflow` | 5 | Vigente |
| DAD | `game-data` | 6 | Vigente (change jogo, arquivada) |
| VIL | `game-village` | 10 | Vigente (change jogo, arquivada) |
| PRD | `game-buildings` | 7 | Vigente (change jogo, arquivada) |
| FAZ | `game-farming` | 4 | Vigente (change jogo, arquivada) |
| FOR | `game-forge` | 6 | Vigente (change jogo, arquivada) |
| EXE | `game-army` | 6 | Vigente (change jogo, arquivada) |
| COM | `game-dungeon-combat` | 10 | Vigente (change jogo, arquivada) |
| LOO | `game-dungeon-loot` | 6 | Vigente (change jogo, arquivada) |
| UIJ | `game-frontend` | 11 | Vigente (change jogo, arquivada) |

**Requisitos não-funcionais (RNF):** `RNF-<ÁREA>-NNN` (15 total em `02-requisitos.md` tabela §4; ex.: `RNF-SEG-001`, `RNF-CON-001`).

**Casos de uso (UC):** `UC-01` … `UC-12` (fixos).

**ADRs:** `0001-spring-boot-java-25-maven.md` … `0023-spa-servida-pelo-backend.md` (23 total).

**Divergências:** `D-01` … `D-16` (16 total em `17-riscos-divida-roadmap.md`).

---

## 5. Fontes de Verdade

Hierarquia de autoridade:

1. **Código rodando** (prevalece sempre): o que o compilador aceitou, os testes validam.
2. **OpenSpec specs vigentes** (`openspec/specs/*/spec.md`, arquivadas): requisitos ratificados.
3. **Design.md da change** (todas arquivadas): decisões e rationale.
4. **Este conjunto de docs**: derivado das três anteriores (registro de decisões, specs vigentes e comportamento implementado).

**Conflito?** Código prevalece; registre a divergência com marca `> **Divergência:** ...` no doc afetado e consolide em `17-riscos-divida-roadmap.md`.

---

## 6. Como Manter esta Documentação

### Docs-as-Code: atualizar em cada change

Após submeter/arquivar uma change OpenSpec:

1. **Leia** `proposal.md`, `design.md`, `specs/`, `tasks.md` da change.
2. **Atualize** os arquivos de docs afetados:
   - Novo requisito/caso de uso → `02`, `03`, `15`.
   - Nova decision → `04`, `adr/`.
   - Novo endpoint → `06`.
   - Novo risco/divergência → `17`.
   - Histórico → `16` (nova seção com data, commits, tarefas, tokens).
   - Glossário → `14` (novos termos).
3. **Verifique** links (relativos, RFC, anchor).
4. **Commit junto** com a change: "docs: atualiza para a change X" ou "docs: registro de divergências".

### Checklist antes de arquivar/sincronizar

- [ ] Todos os 17 docs (ou 20 com ADRs) têm o cabeçalho padrão (versão, data, status, públic).
- [ ] `16-historico-changelog.md` registra a change com commits (`git log`).
- [ ] `17-riscos-divida-roadmap.md` consolida riscos, dívida, divergências identificadas.
- [ ] Nenhum link quebrado (`docs/**/*.md` → arquivo existente; `RF-*` em `02` §3; âncoras em headings).
- [ ] Nenhum arquivo fora de `docs/` foi alterado.

### Rastrear as divergências

Quando código ou spec divergem:

1. Identifique a raiz (falta de sync, design descartado, simplificação).
2. **Registre com ID** no doc afetado: `> **Divergência D-0N:** ...` + link a `17`.
3. **Consolide em `17`** numa tabela com (ID, local, planejado, código, docs afetados, recomendação).
4. **Resolva** criando uma change `fix-docs-divergencias` ou `sync-specs` (não bloqueia entrega).

---

## Histórico de revisões

| Versão | Data | Resumo | Autor |
|---|---|---|---|
| 1.2.0 | 2026-09-27 | Change add-frontend-build implementada: remove marcadores de previsto/aberta/proposto | Adiel, com apoio de agentes Claude |
| 1.1.0 | 2026-09-27 | Atualização para a change add-frontend-build (prevista, aberta): RF 101→104, ADRs 22→23 (0023 proposto), divergências D-01…D-16, 7 changes (6 arquivadas + 1 aberta) | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
