# Tasks

## 1. Profiles no docker-compose e variáveis

- [x] 1.1 Adicionar `profiles: ["${PROFILE_DB:-local}"]` ao serviço `db` e `profiles: ["${PROFILE_APP:-desativado}"]` ao serviço `app` no `docker-compose.yml`, e verificar que `COMPOSE_PROFILES=local docker compose config --services` lista só `db` e que `PROFILE_APP=local COMPOSE_PROFILES=local docker compose config --services` lista `db` e `app`
- [x] 1.2 Adicionar `PROFILE=local`, `PROFILE_DB=local` e `PROFILE_APP=desativado` ao `.env.example`, com um comentário curto explicando cada uma, e verificar que um `.env` copiado do exemplo faz `docker compose --profile local config --services` listar só `db`

## 2. Makefile

- [x] 2.1 Criar o `Makefile` na raiz com `-include .env`, padrões `PROFILE ?= local`, `PROFILE_DB ?= local`, `PROFILE_APP ?= desativado`, `export` de `PROFILE_DB`/`PROFILE_APP`, alvos `.PHONY` `help` (padrão), `up` (`docker compose --profile "$(PROFILE)" up -d`) e `down` (`docker compose --profile "*" down`, sem `-v`), e verificar que `make` sem alvo lista `up`, `down` e `help` sem mexer em containers
- [x] 2.2 Verificar com o Docker no WSL: `make up` sobe só o `db` (`docker compose ps`), `make up PROFILE_APP=local` sobe `db` e `app`, e `make down` remove os dois containers mantendo o volume `login_base_db-data` (`docker volume ls`)
- [x] 2.3 Atualizar o `README.md` (seções "Ambiente Docker" e "Rodando/depurando pelo IntelliJ") para usar `make up`/`make down`, explicar as variáveis `PROFILE`, `PROFILE_DB` e `PROFILE_APP` e mostrar o equivalente sem make (`docker compose --profile local up -d`), e verificar que os comandos documentados rodam como escritos

## 3. Validação final

- [x] 3.1 Rodar `openspec validate add-docker-compose-profiles` sem erros e conferir que nenhum container ficou rodando por causa dos testes (`docker compose ps`)
