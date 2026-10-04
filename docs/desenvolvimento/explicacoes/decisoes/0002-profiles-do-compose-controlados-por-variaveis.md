---
titulo: Profiles do Compose controlados por variáveis
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-09-23
atualizado_em: 2026-10-04
fontes:
  - Makefile
  - docker-compose.yml
  - .env.example
---

# 0002 — Profiles do Compose controlados por variáveis

**Status:** Aceita · **Data:** 2026-09-23

## Contexto

O Docker Compose tem três serviços: `db` (Postgres 17), `app` (a aplicação) e `frontend` (Vue). Em desenvolvimento, o fluxo principal é subir apenas o banco e rodar a aplicação no IntelliJ. Futuramente pode ser necessário rodar a app também no container (frontend integrado, testes end-to-end).

Necessário escolher qual serviço sobe por padrão e permitir mudar isso com um comando simples, sem editar o YAML.

## Decisão

- Os serviços usam `profiles` em `docker-compose.yml`: `db` ← `"${PROFILE_DB:-local}"`, `app` ← `"${PROFILE_APP:-desativado}"`.
- Variáveis `PROFILE_DB`, `PROFILE_APP` e `PROFILE_FRONTEND` (adicionada depois) são exportadas do Makefile.
- `PROFILE` é um apelido que vira `--profile` na linha de comando (ex.: `make up PROFILE_APP=local`).
- `down` executa `docker compose --profile "*" down` para descer todos os serviços, independente de qual profile estava ativo.
- Perfis `local` (ativo) e `desativado` (nunca ativado) são os dois nomes usados no projeto.

## Alternativas descartadas

- **Profiles fixos no YAML** (ex.: `db: [local]`, `app: [full]`) — Rejeitada porque o requisito é escolher o profile de cada serviço por variável.
- **Usar diretamente `COMPOSE_PROFILES` do Docker** — Rejeitada porque o nome pedido é `PROFILE` e é mais intuitivo traduzir isso em `--profile` no Makefile.

## Consequências

### Positivas

- **Sem editar YAML:** quem não conhece Docker Compose consegue rodar `make up PROFILE_APP=local` e a app sobe no container.
- **Composição flexível:** é possível subir só o banco, banco+app, banco+app+frontend, ou qualquer outra combinação, tudo por variáveis.
- **Padrão útil:** por padrão o banco e o frontend sobem (`PROFILE_DB=local`, `PROFILE_APP=desativado`, `PROFILE_FRONTEND=local`); o desenvolvedor escolhe explicitamente se quer o container da app.
- **Makefile legível:** cada variável tem um propósito e um padrão documentado em comentário.

### Negativas

- **Complexidade adicionada:** é necessário entender como profiles e variáveis de ambiente funcionam no Compose para debugar problemas.
- **Armadilha comum:** esquecer de exportar a variável resulta em `no service selected`. O Makefile documenta os parâmetros na linha 1 e 2, mas não há mensagem interativa.
- **Nome `desativado` é mágico:** o profile nunca é ativado em comando algum, por isso não há serviço com `profiles: ["local"]` que execute via `--profile desativado`.

