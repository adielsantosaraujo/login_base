# 0003 — Docker Compose com Profiles + Makefile

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-23 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-docker-compose-profiles`](../../openspec/changes/archive/2026-09-23-add-docker-compose-profiles/) |

## Contexto e problema

O `docker-compose.yml` tem múltiplos serviços (`db`, `app`, e futuramente `frontend`). Durante desenvolvimento, cada projeto precisa de uma combinação diferente: apenas `db`, ou `db` + `app`, ou `db` + `app` + `frontend`. Sem profiles, teríamos de editar o YAML ou usar `--profile` repetidamente. É necessário abstrair essa escolha numa interface simples (Makefile).

## Direcionadores da decisão

- Simplificar `docker compose up` em dev: `make up` deve ser suficiente
- Permitir customizar quais serviços sobem via variáveis (`PROFILE`, `PROFILE_DB`, `PROFILE_APP`, `PROFILE_FRONTEND`)
- Sem exigir edição do `docker-compose.yml` para mudar serviços
- Makefile como interface portável (WSL/Windows/Linux)
- Valor padrão sensato: banco ligado, app/frontend desligados por padrão

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Profiles no Compose + Makefile** | Cada serviço tem `profiles: ["${PROFILE_*:-padrão}"]`, Makefile traduz `PROFILE` para `--profile` ou variáveis específicas. |
| Profiles fixos em `docker-compose.yml` | `db: [dev]`, `app: [full]`. Rejeitada: menos flexível, perde a abstração do Makefile. |
| Variável `COMPOSE_PROFILES` no `.env` | Rejeitada: usuário pediu `PROFILE`, e Makefile oferece melhor controle. |

## Resultado da decisão

Adotou-se **profiles por serviço**, interpolados no YAML:

```yaml
services:
  db:
    profiles: ["${PROFILE_DB:-local}"]
  app:
    profiles: ["${PROFILE_APP:-desativado}"]
  frontend:
    profiles: ["${PROFILE_FRONTEND:-local}"]
```

**Makefile**:
- `-include .env` (sem erro se ausente), depois `PROFILE ?= local`, `PROFILE_DB ?= local`, `PROFILE_APP ?= desativado`, `PROFILE_FRONTEND ?= local`
- `COMPOSE := docker compose --profile "$(PROFILE)"`
- Alvo `up`: `$(COMPOSE) up -d`
- Alvo `down`: `docker compose --profile "*" down` (tira todos, sem `-v`)
- Alvo `help`: lista alvos com comentários `## descrição`

Assim, `make up` = `db` + (app se `PROFILE_APP=local`), `make up PROFILE_APP=desativado` = apenas `db`.

### Consequências positivas
- **Interface simples**: `make up`, `make down`, `make help`
- **Flexibilidade**: customização via `.env` ou linha de comando (`make up PROFILE_APP=local`)
- **Compatibilidade**: `.env` interpolado por Makefile e Compose (precedência: CLI > `.env` > padrão)
- **Down completo**: `--profile "*"` derruba mesmo containers de outros profiles deixados anteriormente

### Consequências negativas
- **BREAKING**: `docker compose up` sem profile deixa de subir algo (`make up` torna-se obrigatório)
- **Complexidade**: interpolação dupla (Makefile → `.env`, Compose → interpolação Compose)
- **Espaços em .env**: variáveis com espaços/aspas podem ser lidas diferentemente por Makefile e Compose (mitigado com formato simples `CHAVE=valor`)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Profiles + Makefile | Interface unificada; flexível; `.env` é lido em ordem (CLI > arquivo > padrão); `down --profile "*"` limpa tudo. | BREAKING change em `docker compose up -d`; requires Makefile on system. |
| Profiles fixos | Simples, sem Makefile. | Menos flexível; cada novo serviço exige editar YAML. |
| COMPOSE_PROFILES no `.env` | Menos verboso. | Perde controle granular por serviço. |

## Mais informações

- **Design**: [`add-docker-compose-profiles/design.md`](../../openspec/changes/archive/2026-09-23-add-docker-compose-profiles/design.md)
- **Docker Compose**: [`docker-compose.yml`](../../docker-compose.yml) — profiles e interpolação de variáveis
- **Makefile**: [`Makefile`](../../Makefile) — alvos `up`, `down`, `help`, lógica de PROFILE

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
