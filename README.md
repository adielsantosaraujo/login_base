# login-base

Projeto Spring Boot (Java 25) com Web MVC, Data JPA, Security, HATEOAS e Thymeleaf, usando PostgreSQL.

## Dependências para desenvolvimento com Claude Code

Quem for desenvolver com o Claude Code neste projeto precisa instalar o plugin oficial
**PrimeVue** (`primevue@primeui`). Ele traz as skills de PrimeVue e o servidor MCP
`@primevue/mcp`. Requer Node.js no WSL (o `npx` já vem com ele):

```bash
npx -y @primeui/cli plugin install --tool claude --library primevue
# ou, com pnpm: pnpm dlx @primeui/cli plugin install --tool claude --library primevue
```

O plugin é instalado no escopo do usuário (`~/.claude/plugins`). Reinicie o Claude Code
depois de instalar.

Depois, confira a instalação com o diagnóstico do PrimeUI:

```bash
npx -y @primeui/cli doctor --tool claude --library primevue
# ou, com pnpm: pnpm dlx @primeui/cli doctor --tool claude --library primevue
```

Todas as verificações devem dar `[PASS]`, exceto `direct-mcp` e `duplicate-mcp`, que
aparecem como `[UNSUPPORTED]` no Claude Code. Por causa delas, o resumo final mostra
`Summary: blocked`, o que é esperado e não indica erro na instalação.

## Abrindo no IntelliJ

Abra a pasta do projeto normalmente pelo caminho do Windows
(`D:\desenvolvimento\projetos\login_base`) via **File > Open** e selecione o `pom.xml`.
O IntelliJ importa o projeto como Maven automaticamente. O JDK usado (Windows nativo ou
um SDK configurado apontando para dentro do WSL) não interfere no Docker, que roda
sempre no WSL — ver seção abaixo.

## Ambiente Docker (rodando no WSL)

Mesmo com os arquivos do projeto no drive Windows, o Docker deve ser executado a
partir de um terminal **WSL** (nunca PowerShell/cmd), com Docker Desktop com
integração WSL2 habilitada para a distro em uso, ou um Docker Engine nativo do WSL.

Cada serviço do `docker-compose.yml` pertence a um profile do Docker Compose,
controlado por quatro variáveis (definidas no `.env` ou na linha de comando):

- `PROFILE_DB` (padrão `local`): profile do serviço `db`.
- `PROFILE_APP` (padrão `desativado`): profile do serviço `app`.
- `PROFILE_FRONTEND` (padrão `local`): profile do serviço `frontend`.
- `PROFILE` (padrão `local`): profile ativado ao subir o ambiente. Só sobem os
  serviços cujo profile for igual a `PROFILE`.

`desativado` não é uma palavra especial do Compose, é só um nome de profile que
nunca é ativado — por isso, com os valores padrão, o `app` não sobe.

```bash
# a partir de um shell WSL, na pasta do projeto (/mnt/d/desenvolvimento/projetos/login_base)
cp .env.example .env   # ajuste as credenciais se quiser
make up    # sobe o Postgres (db) e o frontend com os valores padrão
make down  # derruba todos os serviços, mantendo os volumes
```

`make` (sem alvo) ou `make help` lista os alvos disponíveis.

**Atenção:** desde que o serviço `frontend` foi adicionado, `make up` sem
variáveis sobe `db` **e** `frontend` — não mais só o Postgres. Para manter só o
banco, desative o frontend:

```bash
make up PROFILE_FRONTEND=desativado   # sobe só o db
```

Para subir a aplicação (`app`) no Docker também, defina `PROFILE_APP=local` na
linha de comando ou no `.env`:

```bash
PROFILE_APP=local docker compose --profile local build  # se ainda não tiver a imagem
make up PROFILE_APP=local                                # sobe db, app e frontend
```

Equivalente sem o Makefile:

```bash
docker compose --profile local up -d
docker compose --profile "*" down
```

**Atenção:** `docker compose up -d` sem `--profile` (e sem `COMPOSE_PROFILES`) falha
com `no service selected` (exit code 1) e não sobe nenhum serviço, já que `db`,
`app` e `frontend` agora dependem de profile. Use `make up` ou
`docker compose --profile local up -d`.

## Autenticação e banco de dados

### Esquema do banco

O banco de dados é versionado com **Flyway**. As migrações em
`src/main/resources/db/migration/` criam e gerenciam o schema:

- **V1__controle_acesso.sql**: tabelas `usuarios`, `perfis`, `permissoes`,
  `usuario_rel_perfis`, `perfis_rel_permissoes` e `sessoes`, com campos de
  auditoria (`criado_em`, `criado_por`, `alterado_em`, `alterado_por`).
  O e-mail é único (índice funcional em minúsculas) e normalizado; celular é
  opcional, único e validado com 11 dígitos (DDD + número, armazenado só com
  dígitos).
- **V2__perfil_admin.sql**: insere o perfil `ADMIN`.

O Hibernate apenas **valida** o schema (`spring.jpa.hibernate.ddl-auto=validate`):
não cria nem altera tabelas — elas já existem no banco. Se o volume tiver
tabelas antigas criadas pelo antigo `ddl-auto=update`, o Flyway falha na
inicialização. Para recriar o volume:

```bash
make down
docker volume rm login_base_db-data  # ⚠️ apaga todos os dados
make up
```

### Administrador inicial

O perfil `ADMIN` é criado pela migração V2. Na primeira inicialização, a
aplicação cria o usuário administrador (nome "Administrador") vinculado ao perfil
`ADMIN`, lendo as variáveis de ambiente:

- **`ADMIN_EMAIL`** (padrão `admin@loginbase.local`): e-mail do admin.
- **`ADMIN_PASSWORD`** (padrão vazio): senha do admin. Se vazia, nenhum admin é
  criado — apenas um aviso é registrado no log.

As variáveis são definidas no `.env` (ver `.env.example`) e repassadas ao
serviço `app` no `docker-compose.yml`. O usuário é criado apenas se
`ADMIN_PASSWORD` estiver definida e o e-mail ainda não existir.

### URLs de acesso

- **`http://localhost:8080/login`**: tela de login (usuário anônimo).
- **`http://localhost:8080/`**: página inicial protegida, exibindo
  "Seja bem vindo" (requer autenticação).

Logout via `POST /logout`.

### Login

- Aceita **e-mail ou celular** + senha.
- E-mail é normalizado (minúsculas, sem espaços).
- Celular: 11 dígitos (DDD + número), com ou sem máscara. A aplicação normaliza
  para somente dígitos na busca.
- Em caso de falha (usuário inexistente, senha incorreta ou conta sem perfil
  ativo), a mensagem exibida é sempre "Usuário ou senha inválidos." (genérica,
  sem enumeração).

### Sessão

A sessão HTTP é configurada em `application.properties`:

- **`SESSION_TIMEOUT`** (padrão `30m`): tempo de inatividade até expiração.
- **`SESSION_COOKIE_SECURE`** (padrão `false`): use `true` apenas com HTTPS.

Cada login autenticado é registrado na tabela `sessoes` com IP da origem,
identificação do dispositivo (User-Agent), e timestamp de início/fim.

### Desenvolvimento local fora do Docker

Para rodar a aplicação fora do Docker (pelo IntelliJ ou `./mvnw`), o banco
deve estar de pé no Docker:

```bash
make up PROFILE_FRONTEND=desativado  # sobe só o banco
# ou
docker compose --profile local up -d db
```

Depois, execute a aplicação com as variáveis do `.env` exportadas, por exemplo:

```bash
set -a && . ./.env && set +a
ADMIN_PASSWORD=sua-senha ./mvnw spring-boot:run
```

Ou configure as variáveis no IntelliJ na aba "Run Configurations" (opção
"Environment variables").

## Rodando/depurando pelo IntelliJ

Para ter debugger, hot-swap e breakpoints, o fluxo recomendado no dia a dia é subir
banco e frontend no Docker (comportamento padrão de `make up`) e rodar o backend
diretamente pelo IntelliJ:

```bash
make up
```

Se não precisar do frontend, suba só o banco com `make up PROFILE_FRONTEND=desativado`.

Depois rode/depure a classe `LoginBaseApplication` normalmente pelo IntelliJ. A
aplicação lê as credenciais do banco das variáveis de ambiente `DB_NAME`, `DB_USER`
e `DB_PASSWORD` (com defaults em `application.properties` compatíveis com
`.env.example`), conectando em `jdbc:postgresql://localhost:5432/...` — como o
serviço `db` publica a porta 5432 no host, funciona tanto para o IntelliJ (rodando
fora do Docker) quanto para o serviço `app` (rodando dentro do Docker, via hostname
`db`).

## Frontend (Vue 3 + PrimeVue)

O frontend fica em `frontend/`: Vue 3 + TypeScript + Vite + PrimeVue 5 (tema
Aura), rodando em container próprio — não é preciso ter Node instalado no host.

Com os valores padrão, `make up` já sobe o frontend junto com o banco:

```bash
make up
```

A página fica disponível em `http://localhost:5173`, com hot reload: alterações
salvas em `frontend/src` aparecem no navegador sem reconstruir a imagem nem
reiniciar o container — inclusive com o projeto no drive Windows.

### Chave de licença do PrimeUI

O PrimeVue 5 é configurado com uma chave de licença lida de
`VITE_PRIMEUI_LICENSE`, no `.env` da raiz. Sem ela a página carrega normalmente,
só com um aviso de licença no console do navegador.

Para obter uma chave community gratuita:

1. Acesse https://primeui.dev/licenses/community e confira os critérios de
   elegibilidade (uso individual, estudante, projeto sem fins comerciais ou
   organização pequena).
2. Solicite a licença pelo site; a chave é válida por 12 meses e pode ser
   renovada sem custo enquanto a elegibilidade continuar valendo.
3. Copie a chave para `VITE_PRIMEUI_LICENSE` no `.env` da raiz e suba (ou
   reinicie) o serviço `frontend` (`make up PROFILE_FRONTEND=local`).

### Dependências desatualizadas

As dependências do frontend ficam no volume nomeado `frontend-node-modules`,
fora do drive Windows. Se ele ficar defasado em relação ao `package.json` (por
exemplo, depois de trocar de branch), com o serviço `frontend` parado remova o
volume e deixe o container reinstalar tudo na próxima subida:

```bash
docker volume rm login_base_frontend-node-modules
```

## Build manual (sem Docker)

```bash
./mvnw -DskipTests package
```
