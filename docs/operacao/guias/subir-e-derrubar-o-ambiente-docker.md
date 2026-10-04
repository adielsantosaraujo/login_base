---
titulo: Subir e derrubar o ambiente Docker
publico: operacao
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - Makefile
  - docker-compose.yml
  - .env.example
  - README.md
---

# Subir e derrubar o ambiente Docker

## Quando usar

Este guia serve para iniciar, parar e limpar o ambiente de desenvolvimento ou produção, gerenciado por Docker Compose. Sempre execute estes comandos a partir de um terminal WSL, nunca de PowerShell ou cmd.

## Pré-requisitos

- Docker Desktop com integração WSL2 habilitada, ou Docker Engine nativo do WSL.
- Arquivo `.env` na raiz do projeto (copie de `.env.example` se não existir).
- Terminal WSL no diretório raiz do projeto.

## Passos

1. Copie o arquivo de configuração padrão, se ainda não existir:

   ```bash
   cp .env.example .env
   ```

2. Ajuste as variáveis do `.env` se necessário (credenciais do banco, e-mail do admin, etc.). Consulte [`docs/operacao/referencia/variaveis-de-ambiente.md`](../referencia/variaveis-de-ambiente.md).

3. Para subir o ambiente com o profile padrão (banco e frontend):

   ```bash
   make up
   ```

   Isto levanta o Postgres e o frontend (Vite) em containers.

4. Para subir apenas o banco de dados (sem o frontend):

   ```bash
   make up PROFILE_FRONTEND=desativado
   ```

5. Para subir a aplicação (app) também:

   ```bash
   make up PROFILE_APP=local
   ```

   Ou adicione `PROFILE_APP=local` ao `.env` e execute `make up`.

6. Para derrubar todos os serviços **mantendo os volumes** (dados persistem):

   ```bash
   make down
   ```

7. Para derrubar todos os serviços **e remover os volumes** (apaga todos os dados):

   ```bash
   make down_v
   ```

   > **Alerta:** este comando é destrutivo. Os dados do banco, usuarios, sessões e demais registros são perdidos. Use apenas se desejar limpar completamente o ambiente.

## Como verificar

- Verifique se os serviços subiram:

  ```bash
  docker compose ps
  ```

  Você deve ver `db` com status `Up` (e `frontend`/`app` se foram inclusos).

- Teste a conexão com o banco:

  ```bash
  docker compose exec db psql -U login_base login_base -c "SELECT 1"
  ```

  Resposta esperada: `1`.

- Se o frontend subiu, abra http://localhost:5173 no navegador.

- Se a aplicação subiu no container, abra http://localhost:80 (ou a porta configurada) no navegador.

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| `no service selected` | Nenhum profile foi ativado (falta `--profile` ou `PROFILE` não está definido) | Use `make up` (que lê `PROFILE` do `.env`) ou `docker compose --profile local up -d` |
| O frontend não sobe | `PROFILE_FRONTEND=desativado` está no `.env` ou na linha de comando | Remova ou configure `PROFILE_FRONTEND=local` |
| Erro de conexão com o banco | O Postgres não iniciou ou a porta 5432 está em uso | Aguarde alguns segundos e tente novamente; se persistir, verifique se outra instância de Postgres está rodando |
| Porta 5173 em uso | Outro serviço ocupou a porta | Configure uma porta diferente ou mate o processo que ocupa 5173 |

## Veja também

- [`docs/operacao/guias/recriar-o-banco-de-dados.md`](recriar-o-banco-de-dados.md) — como limpar os dados do banco se o Flyway ou o schema ficarem corrompidos.
- [`docs/operacao/referencia/variaveis-de-ambiente.md`](../referencia/variaveis-de-ambiente.md) — lista completa de variáveis e seus significados.
- [`docs/operacao/referencia/servicos-docker-compose.md`](../referencia/servicos-docker-compose.md) — descrição técnica de cada serviço.
