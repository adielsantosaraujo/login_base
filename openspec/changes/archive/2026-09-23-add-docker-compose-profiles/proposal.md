# Proposal

## Why

Hoje `docker compose up -d` sobe sempre o banco (`db`) e a aplicação (`app`) juntos. Isso atrapalha o fluxo do dia a dia, em que a aplicação roda pelo IntelliJ e só o Postgres deveria estar no Docker: os dois disputam a porta 8080 e é preciso lembrar de digitar `docker compose up -d db`. Além disso, não existe um jeito padronizado e curto de levantar e derrubar o ambiente.

## What Changes

- Cada serviço do `docker-compose.yml` passa a pertencer a um profile do Docker Compose definido por variável de ambiente:
  - `db` → profile `${PROFILE_DB}` (padrão `local`)
  - `app` → profile `${PROFILE_APP}` (padrão `desativado`)
- Nova variável `PROFILE` (padrão `local`) define qual profile é ativado ao subir o ambiente. Com os padrões, só o `db` sobe; para subir a aplicação no Docker basta definir `PROFILE_APP=local`.
- `.env.example` passa a documentar `PROFILE=local`, `PROFILE_DB=local` e `PROFILE_APP=desativado`.
- Novo `Makefile` na raiz com os alvos `up` (levanta os serviços do profile ativo) e `down` (derruba todos os serviços do projeto, qualquer que seja o profile), além de `help`.
- README atualizado com o novo fluxo (`make up` / `make down`).
- **BREAKING** (apenas para quem usa o Docker diretamente): `docker compose up -d` sem profile ativo deixa de subir qualquer serviço. É preciso usar `make up`, `docker compose --profile local up -d` ou definir `COMPOSE_PROFILES`.

## Capabilities

### New Capabilities
- `docker-dev-environment`: como o ambiente Docker de desenvolvimento seleciona os serviços por profile e como é levantado e derrubado pelo Makefile.

### Modified Capabilities
<!-- Nenhuma: não há spec existente sobre o ambiente Docker. -->

## Impact

- Arquivos: `docker-compose.yml`, `.env.example`, `README.md` e o novo `Makefile`.
- Pré-requisito: `make` disponível no WSL (já instalado em `/usr/bin/make`). Os comandos continuam sendo executados no shell WSL.
- Sem impacto no código Java nem no `application.properties`.
