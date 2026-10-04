---
titulo: Variáveis de ambiente
publico: operacao
tipo: referencia
atualizado_em: 2026-10-04
fontes:
  - src/main/resources/application.properties
  - docker-compose.yml
  - .env.example
  - Makefile
  - frontend/vite.config.ts
  - frontend/src/main.ts
---

# Variáveis de ambiente

Lista das variáveis de ambiente lidas pela aplicação, seus valores padrão e onde são processadas.

## Banco de dados

| Variável | Padrão | Descrição | Onde é lido |
|---|---|---|---|
| `DB_HOST` | `localhost` | Hostname do servidor PostgreSQL. Em Docker, use `db` para o serviço. | `application.properties`, `docker-compose.yml` |
| `DB_NAME` | `login_base` | Nome do banco de dados PostgreSQL. | `application.properties`, `docker-compose.yml` |
| `DB_USER` | `login_base` | Usuário do PostgreSQL. | `application.properties`, `docker-compose.yml` |
| `DB_PASSWORD` | `login_base` | Senha do PostgreSQL. | `application.properties`, `docker-compose.yml` |

## Servidor (Spring Boot)

| Variável | Padrão | Descrição | Onde é lido |
|---|---|---|---|
| `SERVER_PORT` | `8080` | Porta na qual o Tomcat escuta. **Problema conhecido:** não é repassado ao container Docker. | `application.properties` |
| `SESSION_TIMEOUT` | `30m` | Timeout de inatividade da sessão HTTP (formato Spring: `30m`, `2h`, etc.). | `application.properties` |
| `SESSION_COOKIE_SECURE` | `false` | Se `true`, o cookie de sessão é marcado como `Secure` (exige HTTPS). Use em produção. | `application.properties` |

## Autenticação e administrador

| Variável | Padrão | Descrição | Onde é lido |
|---|---|---|---|
| `ADMIN_EMAIL` | `admin@loginbase.local` | E-mail do administrador inicial. Criado apenas se não existir e `ADMIN_PASSWORD` não estiver vazio. | `application.properties`, `docker-compose.yml` |
| `ADMIN_PASSWORD` | (vazio) | Senha do administrador inicial. Se vazia, o admin não é criado. | `application.properties`, `docker-compose.yml` |

## Documentação API (Swagger/springdoc)

| Variável | Padrão | Descrição | Onde é lido |
|---|---|---|---|
| `SPRINGDOC_ENABLED` | `true` | Se `true`, habilita Swagger UI e `/v3/api-docs`. Se `false`, desativa a documentação. | `application.properties` |

## Profiles do Docker Compose

Estas variáveis controlam quais serviços Docker Compose são ativados:

| Variável | Padrão | Descrição | Onde é lido |
|---|---|---|---|
| `PROFILE` | `local` | Profile principal ativado por `docker compose --profile`. Determina quais serviços sobem. | `Makefile` |
| `PROFILE_DB` | `local` | Profile do serviço `db` (PostgreSQL). | `docker-compose.yml` |
| `PROFILE_APP` | `desativado` | Profile do serviço `app` (aplicação). Use `local` para subir em container. | `docker-compose.yml` |
| `PROFILE_FRONTEND` | `local` | Profile do serviço `frontend` (Vue/Vite dev server). | `docker-compose.yml` |

## Frontend (Vite/Vue)

| Variável | Padrão | Descrição | Onde é lido |
|---|---|---|---|
| `VITE_PRIMEUI_LICENSE` | (vazio) | Chave de licença do PrimeUI. Vazio = aviso no console. | `frontend/src/main.ts`, `docker-compose.yml` |
| `VITE_USE_POLLING` | `false` | Se `true`, usa polling para hot-reload em ambientes como WSL/DrvFs. O `docker-compose.yml` fixa o valor em `"true"` para o container `frontend`. | `frontend/vite.config.ts`, `docker-compose.yml` |
| `VITE_BACKEND_URL` | `http://localhost:8080` | URL base do backend para proxy de requisições (`/api`, `/login`, etc.) no dev server. | `frontend/vite.config.ts` |
| `BACKEND_URL` | `http://host.docker.internal` | URL do backend passada ao serviço `frontend` em Docker. **Problema conhecido:** o `docker-compose.yml` passa `BACKEND_URL` (sem prefixo `VITE_`), que Vite não lê; o proxy do container aponta para `localhost:8080` do próprio container. Nenhum código da aplicação lê `BACKEND_URL`. | `docker-compose.yml` |

## Variáveis sem uso (a confirmar)

| Variável | Padrão | Descrição | Onde é lido |
|---|---|---|---|
| `JOGO_VELOCIDADE` | `1` | **Sem uso no código**. Passadas pelos serviços `app` e `frontend` (a confirmar utilidade). | `docker-compose.yml` |
| `JOGO_EXPOENTE_CURVA` | `1.5` | **Sem uso no código**. Passada pelo serviço `app` (a confirmar utilidade). | `docker-compose.yml` |

## Nota sobre `.env`, Makefile, docker compose e Spring

A precedência de variáveis de ambiente varia conforme o contexto:

### Makefile

O Makefile inclui o `.env` com `-include .env` (linha 4) e `export` (linha 13), exportando todas as variáveis. Isto significa:

- Variáveis definidas no `.env` têm **precedência sobre** as variáveis de shell;
- Se você passa `VARIAVEL=valor make comando` (antes do alvo), o `.env` prevalece;
- Se você passa `make comando VARIAVEL=valor` (após o alvo), a variável passada no comando prevalece (vence o Makefile).

### docker compose chamado direto

Ao chamar `docker compose` diretamente (sem Makefile), **variáveis de ambiente do shell têm precedência** sobre o `.env`:

```bash
VARIAVEL=novo-valor docker compose up -d
```

Neste caso, `novo-valor` será usado, mesmo que o `.env` defina outro valor.

### Spring Boot

A aplicação Spring Boot lê o arquivo `.env` da raiz via:

```properties
spring.config.import=optional:file:.env[.properties]
```

**Variáveis de ambiente do SO (shell) têm precedência** sobre o `.env` quando a aplicação inicia. Se você define `VARIAVEL=valor` no shell e depois inicia a aplicação, ela usará `valor`.

**No Docker:** o `.env` **não** entra na imagem (excluído por `.dockerignore`). Variáveis devem ser passadas explicitamente no `docker-compose.yml` via `environment:` ou na linha de comando.

## Veja também

- [`docs/operacao/guias/subir-e-derrubar-o-ambiente-docker.md`](../guias/subir-e-derrubar-o-ambiente-docker.md) — como usar estas variáveis ao subir o ambiente.
- [`docs/operacao/referencia/servicos-docker-compose.md`](servicos-docker-compose.md) — como os serviços recebem e usam estas variáveis.
