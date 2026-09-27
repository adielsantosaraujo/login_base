# 0007 — Flyway para Migrações, `ddl-auto=validate`

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-24 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-user-authentication`](../../openspec/changes/archive/2026-09-25-add-user-authentication/) |

## Contexto e problema

O projeto usa JPA/Hibernate para persistência. A estratégia de criação/atualização de esquema precisa ser decidida: `ddl-auto=update` (automática, menos reprodutível), `ddl-auto=create` (reset sempre), ou **migrações explícitas** (Flyway). Migrações garantem rastreabilidade e reversibilidade, críticas para produção e equipes. O Postgres precisa de índices funcionais (ex.: `lower(email)`) que Hibernate não gera automaticamente.

## Direcionadores da decisão

- Reprodutibilidade: esquema deve ser criável de zero sem surpresas
- Auditoria: histórico de mudanças (`V1`, `V2`, ...) rastreável
- Índices especializados: índice único em `lower(email)`, checks de domínio (`celular ~ '^[0-9]{11}$'`)
- Segurança: `ddl-auto=update` em produção é arriscado (pode dropar colunas, non-breaking)
- Versionamento: migrações no git, parte do código

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Flyway + `ddl-auto=validate`** | Todas as DDLs (CREATE TABLE, ALTER, etc.) em `V*.sql` (Flyway); Hibernate só valida (`ddl-auto=validate`). |
| `ddl-auto=update` (Hibernate automático) | Automático, mas perde controle; não gera índices especializados; não é reversível. Rejeitada por riscos de produção. |
| `ddl-auto=create` (reset sempre) | Simples, mas perde dados; não indicado para desenvolvimento contínuo. |
| Liquibase | Alternativa a Flyway; XML verboso. Rejeitada: Flyway é mais direto para SQL puro. |

## Resultado da decisão

Adotou-se **Flyway + SQL puro**:

1. **Dependências**:
   - `spring-boot-starter-flyway`
   - `org.flywaydb:flyway-database-postgresql`

2. **Configuração**:
   - `spring.jpa.hibernate.ddl-auto=validate` (apenas valida, não altera)
   - `spring.flyway.locations=classpath:db/migration`

3. **Migrações** em `src/main/resources/db/migration/`:
   - `V1__controle_acesso.sql` — tabelas de usuário, perfil, permissão, sessão
   - `V2__perfil_admin.sql` — insere perfil ADMIN com `criado_por='sistema'`
   - `V3__jogo.sql` — tabelas do jogo (vila, prédio, canteiro, etc.)

4. **Convenções SQL**:
   - PKs: `bigint generated always as identity`
   - Colunas de auditoria: `criado_em`, `criado_por`, `alterado_em`, `alterado_por` (todas `not null`)
   - Enums: `varchar` com enum Java (`EnumType.STRING`)
   - Índices especializados: inclusos (ex.: `unique index ux_usuarios_email_lower on usuarios (lower(email))`)

### Consequências positivas
- **Reprodutível**: schema pode ser criado de zero (dropando volume, volume fica vazio, Flyway reconstrói)
- **Auditável**: `V1`, `V2`, `V3` mostram história de mudanças no git
- **Índices especializados**: `lower(email)` e checks (`celular ~ '^[0-9]{11}$'`) definidos explicitamente
- **Validação**: `ddl-auto=validate` impede Hibernate fazer DDL acidental
- **Reversibilidade**: futuro suporte a `V*__undo.sql` (não implementado ainda)

### Consequências negativas
- **Gerência manual**: qualquer mudança de schema exige nova migração (menos automático)
- **Erro em migração**: SQL inválido só é descoberto no startup (mitiga com testes)
- **Recriar volume**: `ddl-auto=validate` com tabelas antigas falha; necessário `docker volume rm` (documentado no README)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Flyway + `ddl-auto=validate` | Reprodutível; auditável; índices especializados; seguro para produção. | Gerência manual; setup inicial; erro em SQL descobre na aplicação. |
| `ddl-auto=update` | Automático; rápido de desenvolver. | Não reprodutível; sem índices especializados; arriscado em produção. |
| `ddl-auto=create` | Simples. | Perde dados; não adequado para desenvolvimento contínuo. |

## Mais informações

- **Design**: [`add-user-authentication/design.md` §Decisions 2](../../openspec/changes/archive/2026-09-25-add-user-authentication/design.md)
- **Migrações**: [`src/main/resources/db/migration/`](../../src/main/resources/db/migration/) — V1, V2, V3
- **Configuração**: [`src/main/resources/application.properties`](../../src/main/resources/application.properties) — `spring.jpa.hibernate.ddl-auto`

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
