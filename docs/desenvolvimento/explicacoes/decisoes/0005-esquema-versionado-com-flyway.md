---
titulo: Esquema versionado com Flyway
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-09-24
atualizado_em: 2026-10-04
fontes:
  - pom.xml
  - src/main/resources/db/migration
  - src/main/resources/application.properties
---

# 0005 — Esquema versionado com Flyway

**Status:** Aceita · **Data:** 2026-09-24

## Contexto

O banco Postgres 17 precisa de um esquema que evolua de forma controlada e reprodutível. O Hibernate poderia gerar as tabelas automaticamente via `ddl-auto=update`, mas isso não oferece controle suficiente para funcionalidades como índices funcionais ou constraints complexas.

Necessário escolher entre deixar o Hibernate gerar o esquema ou usar migrações versionadas.

## Decisão

- **Flyway** para migrações versionadas (`spring-boot-starter-flyway` e `flyway-database-postgresql`).
- **`spring.jpa.hibernate.ddl-auto=validate`** para validar que o esquema atual bate com as entidades (sem criar/atualizar tabelas automaticamente).
- Migrações SQL em `src/main/resources/db/migration/`:
  - `V1__controle_acesso.sql` — tabelas `usuarios`, `perfis`, `permissoes`, `usuario_rel_perfis`, `perfis_rel_permissoes`, `sessoes` com PKs `bigint generated always as identity`, índice único em `lower(email)`, `check` de celular (11 dígitos), `check` de datas, e constraint de unicidade em `(perfil_id, permissao_id)`.
  - `V2__perfil_admin.sql` — insere o perfil inicial `ADMIN` com `criado_por = 'sistema'`.
  - Futuras migrações incrementam a versão.

## Alternativas descartadas

- **`ddl-auto=update`** — Rejeitada porque:
  - O Hibernate não gera índice funcional em `lower(email)` nem check constraints de forma controlada.
  - `update` permite schemas inconsistentes em bancos de desenvolvimento antigos.
  - Não é seguro para produção (pode deixar dados órfãos).

## Consequências

### Positivas

- **Reprodutibilidade:** o esquema é idêntico em dev, teste e produção. Novos desenvolvedores rodando `make up` obtêm o estado correto.
- **Histórico:** cada mudança tem uma data e um comentário; é fácil ver quando uma tabela ou índice foi adicionado.
- **Índices e constraints:** controle total sobre as regras do banco. O índice em `lower(email)` garante busca rápida e case-insensitive.
- **Rollback controlado:** se um script falhar, o Flyway para e reporta o erro; não há esquema parcialmente aplicado.

### Negativas

- **Responsabilidade do desenvolvedor:** esquecer de criar uma migração resulta em `ddl-auto=validate` errando na inicialização. Mitigado pela recomendação de sempre rodar testes de integração que criam o contexto.
- **Dependência de SQL:** requer conhecimento mínimo de SQL; mudanças de estrutura exigem escrita de migração à mão.
- **Bancos de desenvolvimento com tabelas velhas:** se tabelas foram criadas pelo antigo `update` e não correspondem à migração, é necessário recriar o volume `db-data`. Documentado em guia de troubleshooting.

