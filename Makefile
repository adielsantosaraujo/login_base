# Uso: make <alvo> [PROFILE=local] [PROFILE_DB=local] [PROFILE_APP=local] [PROFILE_FRONTEND=local]
# Exemplo: make up PROFILE_APP=local

-include .env

PROFILE ?= local
PROFILE_DB ?= local
PROFILE_APP ?= desativado
PROFILE_FRONTEND ?= local

COMPOSE ?= docker compose --profile "$(PROFILE)"

export

.DEFAULT_GOAL := help

.PHONY: help
help: ## Lista os alvos disponíveis com uma descrição curta
	@grep -hE '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-10s %s\n", $$1, $$2}'

.PHONY: up
up: ## Levanta os serviços do profile ativo (PROFILE)
	$(COMPOSE) up -d

.PHONY: down
down: ## Derruba todos os serviços do projeto, mantendo os volumes
	docker compose --profile "*" down

.PHONY: down_v
down_v: ## Derruba todos os serviços e remove os volumes
	docker compose --profile "*" down -v

.PHONY: logs_front
logs_front: ## Acompanha os logs do frontend (Ctrl+C para sair)
	$(COMPOSE) logs -f frontend


e:
	@$(MAKE) executar

.PHONY: executar
executar: ## Abre o menu interativo de comandos
	@python3 ./scripts/executar.py