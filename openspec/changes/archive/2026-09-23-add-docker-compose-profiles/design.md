# Design

## Context

- O `docker-compose.yml` tem dois serviços sem profile, `db` (Postgres 17) e `app` (imagem do `Dockerfile`), e o volume nomeado `db-data`. As credenciais vêm do `.env` (não versionado, modelo em `.env.example`).
- O Docker roda sempre no WSL (Docker Compose v2.32.4) e o `make` já está instalado em `/usr/bin/make`.
- Motivação: ver proposal.md, seção Why. Requisitos: ver `specs/docker-dev-environment/spec.md`.

## Goals / Non-Goals

**Goals:**
- Com os valores padrão, subir apenas o Postgres, que é o fluxo de desenvolvimento com o IntelliJ.
- Poder mudar quais serviços sobem só editando variáveis, sem editar o `docker-compose.yml`.

**Non-Goals:**
- Alvos extras no Makefile (build, logs, testes); podem vir em changes futuras.
- Suporte a rodar o Makefile no PowerShell/cmd do Windows.
- Novos serviços ou profiles além de `local` e `desativado`.

## Decisions

1. **Profile interpolado por variável no próprio serviço**: `db` recebe `profiles: ["${PROFILE_DB:-local}"]` e `app` recebe `profiles: ["${PROFILE_APP:-desativado}"]`. O Compose interpola variáveis em `profiles`, o que foi testado na v2.32.4: com `COMPOSE_PROFILES=local`, só o `db` aparece em `docker compose config --services`. O padrão `:-` garante o comportamento esperado mesmo sem `.env`.
   - Alternativa considerada: profiles fixos no YAML (ex.: `db: [local]`, `app: [full]`). Rejeitada porque o pedido é escolher o profile de cada serviço por variável.
   - `desativado` é só um nome de profile que nunca é ativado; não é uma palavra especial do Compose.

2. **`PROFILE` vira `--profile` no Makefile**: o alvo `up` executa `docker compose --profile "$(PROFILE)" up -d`. `PROFILE` não é uma variável que o Compose reconhece, por isso a tradução fica no Makefile.
   - Alternativa considerada: usar direto `COMPOSE_PROFILES` no `.env`. Rejeitada porque o nome pedido é `PROFILE`; quem quiser usar `docker compose` sem o Makefile pode passar `--profile local`, o que fica documentado no README.

3. **Leitura do `.env` com precedência da linha de comando**: o Makefile faz `-include .env` (sem erro se o arquivo não existir), depois define os padrões com `?=` (`PROFILE ?= local`, `PROFILE_DB ?= local`, `PROFILE_APP ?= desativado`) e exporta `PROFILE_DB` e `PROFILE_APP` para o `docker compose`. No make, variáveis da linha de comando têm prioridade sobre as do arquivo, então `make up PROFILE_APP=local` funciona com ou sem `.env`.

4. **`down` com todos os profiles**: o alvo `down` executa `docker compose --profile "*" down`, sem `-v`. O curinga `*` foi testado e inclui todos os serviços, então um `app` iniciado antes com outro profile também é derrubado. Sem `-v`, o volume `db-data` é mantido.
   - Alternativa considerada: `down` só com `--profile $(PROFILE)`. Rejeitada porque deixaria containers de outros profiles rodando e a rede do projeto presa.

5. **`help` como alvo padrão**: comentários `## descrição` nos alvos, lidos por um `grep`/`awk` simples. `.DEFAULT_GOAL := help` e todos os alvos em `.PHONY`.

## Risks / Trade-offs

- [`docker compose up -d` sem profile passa a não subir nada] → README atualizado com `make up` e com o equivalente `docker compose --profile local up -d`. O item está marcado como BREAKING no proposal.
- [`.env` com espaços ou aspas nos valores é lido de forma diferente pelo make e pelo Compose] → Manter o formato simples `CHAVE=valor` no `.env.example`.
- [Uma variável `PROFILE` já exportada no shell do usuário para outro fim seria usada pelo make] → Baixo risco. O valor do `.env` e o da linha de comando têm prioridade sobre o do ambiente.
- [O Makefile precisa de tabs de verdade nas receitas] → A verificação com `make help` e `make up` nas tasks pega esse erro.
