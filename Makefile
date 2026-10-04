# Uso: make <alvo> [PROFILE=local] [PROFILE_DB=local] [PROFILE_APP=local] [PROFILE_FRONTEND=local]
# Exemplo: make up PROFILE_APP=local
# Frontend de dev: make up FRONT_APP=public/cadastro_usuario (caminho do app dentro de frontend/)
# Build: make build_front [APPS="cadastro_usuario patrimonio"]

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
	@grep -hE '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-12s %s\n", $$1, $$2}'

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

.PHONY: build_front
build_front: ## Gera o build de todos os frontends e copia para o backend (APPS=... para filtrar)
	python3 ./scripts/build_front.py $(APPS)

.PHONY: limpar_front
limpar_front: ## Remove os artefatos do build do frontend do backend
	python3 ./scripts/limpar_front.py


e:
	@$(MAKE) executar

.PHONY: executar
executar: ## Abre o menu interativo de comandos
	@python3 ./scripts/executar.py