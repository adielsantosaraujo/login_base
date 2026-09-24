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

.PHONY: help up down logs_front

help: ## Lista os alvos disponíveis com uma descrição curta
	@grep -hE '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-10s %s\n", $$1, $$2}'

up: ## Levanta os serviços do profile ativo (PROFILE)
	$(COMPOSE) up -d

down: ## Derruba todos os serviços do projeto, mantendo os volumes
	docker compose --profile "*" down

logs_front: ## Acompanha os logs do frontend (Ctrl+C para sair)
	$(COMPOSE) logs -f frontend
