# Decisões Arquiteturais (ADRs)

| Campo | Valor |
|---|---|
| Versão | 1.1.0 |
| Data | 2026-09-27 |
| Status | Vigente — baseline do commit `454ae58` + change `add-frontend-build` implementada |
| Modelo/norma | MADR 4.0 (Markdown Any Decision Records) |
| Público | arquitetos, desenvolvedores, revisores |
| Fontes | `design.md` das changes OpenSpec; código-fonte; `design.md` da change `add-frontend-build` |

> Parte da [documentação do login_base](../README.md). Índice de todas as decisões arquiteturais significativas do projeto, registradas no formato MADR 4.0 para contexto, motivação e resultado. Cada arquivo é autocontido e linkável.

---

## Índice de Decisões

| Nº | Título | Data | Change de Origem | Status |
|---|---|---|---|---|
| [0001](0001-spring-boot-java-25-maven.md) | Spring Boot 4.1 + Java 25 + Maven Wrapper | 2026-09-23 | `setup-java-project` (arquivada) | Vigente |
| [0002](0002-ambiente-hibrido-ide-docker.md) | Backend na IDE contra Postgres no Docker | 2026-09-23 | `setup-java-project` | Vigente |
| [0003](0003-compose-profiles-makefile.md) | Profiles por variável + Makefile (`up`/`down`/`help`) | 2026-09-23 | `add-docker-compose-profiles` | Vigente |
| [0004](0004-frontend-vue-vite-primevue.md) | Vue 3 + TypeScript + Vite + PrimeVue 5 (Aura) | 2026-09-23 | `add-vue-frontend` | Vigente |
| [0005](0005-subagentes-por-tipo-de-trabalho.md) | Orquestração por subagentes (Opus/Sonnet/Haiku) | 2026-09-23 | `add-subagent-dev-skill` | Vigente |
| [0006](0006-openspec-tasks-em-arquivos.md) | Spec-driven com OpenSpec; `tasks.md` como índice | 2026-09-25 | OpenSpec config + commit 6ce0641 | Vigente |
| [0007](0007-flyway-ddl-validate.md) | Flyway para migrações; `ddl-auto=validate` | 2026-09-24 | `add-user-authentication` | Vigente |
| [0008](0008-pacotes-por-dominio.md) | Pacotes por domínio de negócio | 2026-09-24 | `add-user-authentication` + jogo | Vigente |
| [0009](0009-auditoria-jpa-auditing.md) | Spring Data JPA Auditing; campos `criado_por`, `alterado_em`, etc. | 2026-09-24 | `add-user-authentication` | Vigente |
| [0010](0010-login-formulario-sessao.md) | Form login Spring Security + sessão HTTP stateful | 2026-09-24 | `add-user-authentication` | Vigente |
| [0011](0011-senhas-delegating-encoder.md) | `DelegatingPasswordEncoder` (BCrypt) | 2026-09-24 | `add-user-authentication` | Vigente |
| [0012](0012-registro-sessoes-tabela-propria.md) | Tabela `sessoes` com SHA-256 do ID (vs Spring Session JDBC) | 2026-09-24 | `add-user-authentication` | Vigente |
| [0013](0013-admin-inicial-por-ambiente.md) | Admin criado por `ApplicationRunner` via `ADMIN_EMAIL`/`ADMIN_PASSWORD` | 2026-09-24 | `add-user-authentication` | Vigente |
| [0014](0014-spa-mesma-origem-proxy-vite.md) | SPA na mesma origem via proxy do Vite | 2026-09-26 | `add-city-builder-game` | Vigente (a ser complementada por 0023) |
| [0015](0015-api-401-e-csrf-spa.md) | `/api/**` anônimo → 401; CSRF SPA via cookie + header | 2026-09-26 | `add-city-builder-game` | Vigente |
| [0016](0016-catalogo-em-codigo.md) | Catálogo em enums/records Java (vs YAML) | 2026-09-26 | `add-city-builder-game` | Vigente |
| [0017](0017-calculo-preguicoso-milesimos.md) | Cálculo lazy de produção; recursos em milésimos | 2026-09-26 | `add-city-builder-game` | Vigente |
| [0018](0018-concorrencia-lock-pessimista.md) | Lock pessimista por vila; `@Version` na batalha | 2026-09-26 | `add-city-builder-game` | Vigente |
| [0019](0019-estado-batalha-json-text.md) | Estado/log/loot da batalha em JSON em colunas `text` | 2026-09-26 | `add-city-builder-game` | Vigente |
| [0020](0020-determinismo-clock-aleatorio.md) | Motor puro determinístico; Clock/Aleatorio injetáveis | 2026-09-26 | `add-city-builder-game` | Vigente |
| [0021](0021-velocidade-configuravel.md) | `JOGO_VELOCIDADE` multiplica taxas e divide tempos | 2026-09-26 | `add-city-builder-game` | Vigente |
| [0022](0022-testes-postgres-compose.md) | Testes de integração contra Postgres do compose (sem Testcontainers) | 2026-09-24 | `add-user-authentication` + `add-city-builder-game` | Vigente |
| [0023](0023-spa-servida-pelo-backend.md) | SPA servida pelo backend (`/app/**` + view) | 2026-09-27 | `add-frontend-build` | Vigente |

---

## Como Usar Este Índice

1. **Navegar**: cada linha da tabela é um link para o arquivo da ADR correspondente.
2. **Ler uma ADR**: abra o arquivo (ex.: `0001-spring-boot-java-25-maven.md`) e procure pelas seções **Contexto e Problema**, **Decisão** e **Consequências**.
3. **Entender o histórico**: a coluna "Change de Origem" indica qual mudança (change OpenSpec) levou à decisão.
4. **Buscar relacionadas**: muitas ADRs estão relacionadas; links internos (`../0007-…`) conectam decisões.

---

## Template MADR 4.0 — Para Novas ADRs

Quando criar uma nova ADR, copie e preencha este template:

```markdown
# NNNN — Título da Decisão

| Campo | Valor |
|---|---|
| Status | Proposta / Aceita / Deprecada / Substituída por [NNNN] |
| Data | AAAA-MM-DD |
| Decisores | Nomes (ex.: Adiel, com apoio de agentes Claude) |
| Change de Origem | [link-ou-não-aplicável] |

> Contexto e motivação em uma linha.

## Contexto e Problema

Descreva a situação que levou à necessidade de uma decisão:
- Qual é o problema/oportunidade?
- Por que precisa de uma decisão agora?
- Que restrições ou objetivos existem?

Cite requisitos (`RF-*`, `RNF-*`) ou specs OpenSpec aplicáveis.

## Direcionadores da Decisão

- Critério 1 (ex.: velocidade de desenvolvimento)
- Critério 2 (ex.: testabilidade)
- Critério 3 (ex.: custo operacional)

## Opções Consideradas

### Opção 1: [Nome conciso]
Descrição curta. Prós: A, B. Contras: C.

### Opção 2: [Nome conciso]
Descrição curta. Prós: X, Y. Contras: Z.

### Opção 3: [Nome conciso]
...

## Resultado da Decisão

**Decidimos por [Opção N]** porque...

(Explique a razão — critérios atendidos, trade-offs aceitos.)

### Consequências Positivas
- Benefício 1
- Benefício 2

### Consequências Negativas
- Risco ou trade-off 1
- Risco ou trade-off 2

## Mais Informações

- **Spec relacionada**: [link/caminho relativo a spec OpenSpec]
- **Design relacionado**: [link/caminho relativo a design.md]
- **Código principal**: [link relativo a arquivo Java/TS, ex.: `../../src/main/java/.../Classe.java`]
- **Testes**: [link relativo a teste, ex.: `../../src/test/java/.../ClasseTest.java`]

## Alternativas Não Registradas

Se nenhuma alternativa constar no design.md da change, escrever aqui:
"Alternativas não registradas no design.md da change."

---

**Histórico de Revisões:**

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | AAAA-MM-DD | Versão inicial | [Nome] |
```

### Instruções de Preenchimento

1. **Status**: use `Proposta` durante discussão, `Aceita` após aprovação, `Deprecada` se deixar de valer, `Substituída por [NNNN]` se outra ADR a supera.
2. **Decisores**: nomes dos responsáveis. Padrão: "Adiel (autor) com apoio de agentes Claude".
3. **Change de Origem**: link para a change OpenSpec (ex.: `../../../openspec/changes/add-user-authentication/proposal.md`), ou "N/A" se não aplicável.
4. **Contexto**: seja específico; cite requisitos, constraints, fatos-chave.
5. **Direcionadores**: critérios que guiaram a escolha.
6. **Opções**: liste alternativas reais que foram consideradas (não inventadas). Se a change design.md não registra alternativas, escreva "Alternativas não registradas".
7. **Resultado**: a decisão tomada e por quê (justificação concisa).
8. **Consequências**: impactos positivos e negativos reais.
9. **Mais Informações**: links internos (relativos) para specs, código, testes.
10. **Histórico de Revisões**: mantenha a tabela atualizada se a ADR for revisada.

### Boas Práticas

- **Não inventar**: registre apenas o que consta na documentação de design ou no código.
- **Links relativos**: use caminhos como `../04-arquitetura.md`, `../../src/main/java/…/Classe.java`.
- **Concisão**: 1 a 2 páginas por ADR.
- **Tipagem**: estruture as seções com cabeçalhos de segundo nível (`##`).
- **Versionamento**: quando revisar, incremente a versão (1.0.0 → 1.1.0 para pequenos ajustes, 2.0.0 para mudanças maiores).

---

## Histórico de Revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.2.0 | 2026-09-27 | ADR 0023 implementada pela change add-frontend-build: muda status de Proposta para Vigente | Adiel, com apoio de agentes Claude |
| 1.1.0 | 2026-09-27 | Adiciona ADR 0023 (SPA servida pelo backend, proposta para change add-frontend-build) | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial (índice + template MADR 4.0) | Adiel, com apoio de agentes Claude |
