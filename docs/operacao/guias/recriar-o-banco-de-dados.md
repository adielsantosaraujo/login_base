---
titulo: Recriar o banco de dados
publico: operacao
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - README.md
  - Makefile
  - docker-compose.yml
  - docker-compose.yml
---

# Recriar o banco de dados

## Quando usar

Use este guia quando:

- O Flyway falha na inicialização (erro de migração).
- O Hibernate valida o schema e encontra inconsistências (`ddl-auto=validate` falha).
- Você deseja apagar todos os dados e começar do zero.
- O volume do banco ficou corrompido ou inconsistente.

## Pré-requisitos

- Terminal WSL.
- Docker em execução.
- Os serviços estão parados ou você está pronto para detê-los.

## Passos

1. Derrube todos os serviços:

   ```bash
   make down
   ```

2. Remova o volume do banco de dados:

   ```bash
   docker volume rm login_base_db-data
   ```

   > **Alerta:** Este comando é **destrutivo**. Todos os dados armazenados no banco (usuarios, sessões, auditoria, etc.) serão **permanentemente apagados** e não poderão ser recuperados.

3. Suba o banco de dados:

   ```bash
   make up
   ```

   Isto inicia o PostgreSQL. O banco fica vazio.

4. Suba a aplicação:

   ```bash
   make up PROFILE_APP=local
   ```

   Ou, se ainda não subiu com o passo anterior, suba o app também. O Flyway executa automaticamente **ao iniciar a aplicação Spring** e recria o schema completo:
   - `V1__controle_acesso.sql`: tabelas de usuários, perfis, permissões e sessões.
   - `V2__perfil_admin.sql`: insere o perfil ADMIN.

5. Acompanhe os logs da aplicação para verificar que o Flyway finalizou com sucesso:

   ```bash
   docker compose logs app
   ```

   Procure por mensagens como:
   ```
   ... Executing SQL migration: ... Executed 2 migrations ...
   ```

## Como verificar

- Verifique que o banco está ativo:

  ```bash
  docker compose exec db psql -U login_base login_base -c "SELECT version();"
  ```

  Você verá a versão do PostgreSQL.

- Liste as tabelas:

  ```bash
  docker compose exec db psql -U login_base login_base -c "\dt"
  ```

  Você deve ver as tabelas: `flyway_schema_history`, `usuarios`, `perfis`, `permissoes`, `usuario_rel_perfis`, `perfis_rel_permissoes`, `sessoes`.

- A aplicação agora pode ser iniciada normalmente e o admin inicial será criado conforme configurado (ver [`docs/operacao/guias/configurar-o-administrador-inicial.md`](configurar-o-administrador-inicial.md)).

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| "Volume not found" ao tentar remover | O volume já não existe | Prossiga; a recriação acontecerá na próxima subida |
| Flyway falha novamente após recriação | Migração tem erro ou há problemas de permissão no Postgres | Verifique os logs da aplicação (`docker compose logs app`, console do IntelliJ ou terminal `./mvnw`); se necessário, reinicie o Docker daemon |
| Aplicação ainda não inicia após recriação | `ddl-auto=validate` falha por schema incompleto | Aguarde alguns segundos para o Flyway terminar; ou verifique se as migrações foram executadas |

## Veja também

- [`docs/operacao/guias/subir-e-derrubar-o-ambiente-docker.md`](subir-e-derrubar-o-ambiente-docker.md) — como gerenciar containers.
- [`docs/operacao/guias/configurar-o-administrador-inicial.md`](configurar-o-administrador-inicial.md) — como criar o admin após a recriação.
- [`docs/desenvolvimento/referencia/modelo-de-dados.md`](../../desenvolvimento/referencia/modelo-de-dados.md) — estrutura do schema e tabelas.
