---
titulo: Comandos make e scripts
publico: operacao
tipo: referencia
atualizado_em: 2026-10-04
fontes:
  - Makefile
  - scripts/build_front.py
  - scripts/limpar_front.py
  - scripts/executar.py
  - scripts/cores.py
  - scripts/apps_front.py
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
| `build_front` | Descobre apps em `frontend/public/<nome>/` e `frontend/seguro/<nome>/`, roda build de cada um via Docker, e copia assets e templates para o backend. Remove builds anteriores. Use `APPS=...` para filtrar. Equivalente: `python3 scripts/build_front.py`. | `make build_front` ou `make build_front APPS=patrimonio cadastro_usuario` |
| `limpar_front` | Remove todos os templates, estáticos e dist gerados pelo build do frontend. Equivalente: `python3 scripts/limpar_front.py`. | `make limpar_front` |
| `executar` ou `e` | Abre menu interativo que lista os alvos do Makefile com `## descrição` (exceto `executar`) e permite escolher qual rodar. Equivalente: `python3 scripts/executar.py`. | `make executar` ou `make e` |

## Variáveis do Makefile

Defina via `.env` ou na linha de comando:

| Variável | Padrão | Significado |
|---|---|---|
| `PROFILE` | `local` | Profile ativado por `docker compose --profile`. Determina quais serviços sobem. |
| `PROFILE_DB` | `local` | Profile do serviço `db`. |
| `PROFILE_APP` | `desativado` | Profile do serviço `app`. Use `local` para subir em container. |
| `PROFILE_FRONTEND` | `local` | Profile do serviço `frontend`. |
| `FRONT_APP` | `seguro/patrimonio` | Caminho do app frontend dentro de `frontend/` (ex.: `public/cadastro_usuario`, `seguro/patrimonio`). Usado pelos serviços `frontend` e `frontend-build` no docker-compose. |
| `FRONT_APP_NOME` | `patrimonio` | Nome do app frontend (sem a área). Usado pelo `vite.config.ts` para definir `base: /<nome>/` no build. |
| `APPS` | (vazio) | Filtro de apps para `make build_front`. Se vazio, builda todos; senão, apenas os nomeados (ex.: `APPS=patrimonio cadastro_usuario`). |

## Scripts Python

Localizados em `scripts/`:

### `build_front.py`

Gera o build de produção do frontend (equivalente a `make build_front`).

```bash
python3 scripts/build_front.py                    # Builda todos os apps
python3 scripts/build_front.py cadastro_usuario   # Builda apenas app(s) nomeado(s)
```

O que faz (para cada app em `frontend/public/<nome>/` e `frontend/seguro/<nome>/`):

1. Remove diretórios antigos: `frontend/<area>/<nome>/dist`, `src/main/resources/static/<nome>/`, templates correspondentes.
2. Executa `docker compose run --rm --build frontend-build` com `FRONT_APP=<area>/<nome>` e `FRONT_APP_NOME=<nome>` para compilar o Vite.
3. Valida que `frontend/<area>/<nome>/dist/index.html` foi gerado.
4. Copia os assets (exceto `index.html`) para `src/main/resources/static/<nome>/`.
5. Copia `index.html` para `src/main/resources/templates/sistema/<area>/<nome>/index.html`.
6. Registra tudo em `build.log`.

Sem argumentos, limpa tudo via `scripts/limpar_front.py`. Com argumentos (nomes de apps), limpa apenas os artefatos dos apps selecionados.

Usado antes de subir a aplicação em container ou gerar o JAR de produção.

### `limpar_front.py`

Remove todos os artefatos do build do frontend do backend (equivalente a `make limpar_front`).

```bash
python3 scripts/limpar_front.py
```

O que faz:

1. Remove todas as subpastas de `src/main/resources/templates/sistema/public/` e `src/main/resources/templates/sistema/seguro/` (preserva `login.html` e `.gitignore` no nível raiz).
2. Remove `src/main/resources/static/<nome>/` para todos os apps descobertos em `frontend/public/` e `frontend/seguro/`, mais o legado `app`.
3. Remove `frontend/<area>/<nome>/dist` de cada app.
4. Registra as ações no terminal.

Use quando quiser descartar todos os builds gerados e deixar apenas o código-fonte.

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
# Edite ADMIN_PASSWORD, ADMIN_EMAIL, FRONT_APP (app a desenvolver), etc.

# 2. Subir banco e frontend (do app em FRONT_APP)
make up

# 3. Desenvolver backend (IntelliJ ou terminal)
./mvnw spring-boot:run

# 4. Desenvolver frontend (hot reload automático em http://localhost:5173)
# Nada a fazer; o container frontend já rodando em make up
# Para trocar de app: make down && make up FRONT_APP=public/cadastro_usuario

# 5. Antes de gerar release, fazer build de todos os frontends
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
# Edite ADMIN_PASSWORD, ADMIN_EMAIL, etc. (FRONT_APP e FRONT_APP_NOME leem do .env)

# 2. Build de todos os frontends
make build_front
# Ou filtrar: make build_front APPS=patrimonio cadastro_usuario

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
