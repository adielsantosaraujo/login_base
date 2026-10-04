---
titulo: Comandos make e scripts
publico: operacao
tipo: referencia
atualizado_em: 2026-10-04
fontes:
  - Makefile
  - scripts/build_front.py
  - scripts/executar.py
  - scripts/cores.py
---

# Comandos make e scripts

Referência dos comandos disponíveis no Makefile e scripts Python para gerenciar o ambiente.

## Alvos do Makefile

Execute com `make <alvo>` [VARIÁVEIS].

| Alvo | Descrição | Exemplo |
|---|---|---|
| `help` (padrão) | Lista todos os alvos disponíveis com descrição. | `make help` |
| `up` | Levanta os serviços do profile ativo (`PROFILE`). Lê variáveis do `.env` ou linha de comando. | `make up` ou `make up PROFILE_APP=local` |
| `down` | Derruba todos os serviços em todos os profiles, **mantendo os volumes**. | `make down` |
| `down_v` | Derruba todos os serviços e **remove os volumes** (dados são apagados). | `make down_v` ⚠️ |
| `logs_front` | Acompanha os logs do serviço `frontend` em tempo real (Ctrl+C para sair). | `make logs_front` |
| `build_front` | Gera o build do frontend (copia para backend) e remove builds anteriores. Equivalente: `python3 scripts/build_front.py`. | `make build_front` |
| `executar` ou `e` | Abre menu interativo que lista os alvos do Makefile com `## descrição` (exceto `executar`) e permite escolher qual rodar. Equivalente: `python3 scripts/executar.py`. | `make executar` ou `make e` |

## Variáveis do Makefile

Defina via `.env` ou na linha de comando:

| Variável | Padrão | Significado |
|---|---|---|
| `PROFILE` | `local` | Profile ativado por `docker compose --profile`. Determina quais serviços sobem. |
| `PROFILE_DB` | `local` | Profile do serviço `db`. |
| `PROFILE_APP` | `desativado` | Profile do serviço `app`. Use `local` para subir em container. |
| `PROFILE_FRONTEND` | `local` | Profile do serviço `frontend`. |

## Scripts Python

Localizados em `scripts/`:

### `build_front.py`

Gera o build de produção do frontend (equivalente a `make build_front`).

```bash
python3 scripts/build_front.py
```

O que faz:

1. Remove diretórios antigos: `frontend/dist`, `src/main/resources/static/app`, template legado.
2. Executa `docker compose run --rm --build frontend-build` para compilar o Vite.
3. Valida que `frontend/dist/index.html` foi gerado.
4. Copia os assets (exceto `index.html`) para `src/main/resources/static/app/`.
5. Copia `index.html` para `src/main/resources/templates/sistema/seguro/app/index.html`.
6. Registra tudo em `build.log`.

Usado antes de subir a aplicação em container ou gerar o JAR de produção.

### `executar.py`

Menu interativo para executar comandos comuns (equivalente a `make executar` ou `make e`).

```bash
python3 scripts/executar.py
```

Menu típico:

```
    0)  sair ---------- Encerra o menu
    1)  help ---------- Lista os alvos disponíveis com uma descrição curta
    2)  up ------------ Levanta os serviços do profile ativo (PROFILE)
    3)  down ---------- Derruba todos os serviços do projeto, mantendo os volumes
    4)  down_v -------- Derruba todos os serviços e remove os volumes
    5)  logs_front ---- Acompanha os logs do frontend (Ctrl+C para sair)
    6)  build_front --- Gera o build do frontend e copia para o backend

Escolha um numero: 
```

### `cores.py`

Utilidades para colorir a saída de outros scripts. **Não executar diretamente.**

Define constantes:

- `BLUE_COLOR`, `CYAN_COLOR`, `YELLOW_COLOR`, `RED_COLOR`, `RESET_COLOR`

Usadas por `build_front.py` e `executar.py` para output legível.

## Fluxo típico de desenvolvimento

```bash
# 1. Criar/editar arquivo .env
cp .env.example .env
# Edite ADMIN_PASSWORD, ADMIN_EMAIL, etc.

# 2. Subir banco e frontend
make up

# 3. Desenvolver backend (IntelliJ ou terminal)
./mvnw spring-boot:run

# 4. Desenvolver frontend (hot reload automático em http://localhost:5173)
# Nada a fazer; o container frontend já rodando em make up

# 5. Antes de gerar release, fazer build do frontend
make build_front

# 6. Compilar o JAR
./mvnw -DskipTests package

# 7. Parar ambiente
make down
```

## Fluxo com aplicação em container

```bash
# 1. Setup
cp .env.example .env

# 2. Build do frontend
make build_front

# 3. Subir banco, app e frontend em containers
make up PROFILE_APP=local

# 4. Acompanhar logs
docker compose logs -f app

# 5. Derrubar
make down
```

## Troubleshooting

Se `make` não for encontrado no WSL:

```bash
# Instalar make via apt
sudo apt-get update && sudo apt-get install -y make
```

Se um script Python falhar com erro de importação ou módulo não encontrado, verifique se está rodando do diretório correto (a raiz do projeto, onde estão os scripts):

```bash
# Certifique-se de estar no diretório raiz
cd /caminho/para/login_base
```

(Os scripts são sempre chamados com `python3`, então `chmod +x` não é necessário.)

## Veja também

- [`docs/operacao/guias/subir-e-derrubar-o-ambiente-docker.md`](../guias/subir-e-derrubar-o-ambiente-docker.md) — como usar `make up/down`.
- [`docs/desenvolvimento/guias/gerar-o-build-de-producao.md`](../../desenvolvimento/guias/gerar-o-build-de-producao.md) — detalhes sobre `make build_front` e o build final.
