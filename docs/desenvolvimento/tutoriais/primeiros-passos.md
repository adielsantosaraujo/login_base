---
titulo: Primeiros passos
publico: desenvolvimento
tipo: tutorial
atualizado_em: 2026-10-04
fontes:
  - .env.example
  - Makefile
  - docker-compose.yml
  - README.md
  - src/main/resources/application.properties
  - src/main/java/com/example/loginbase/seguranca/AdminInicialRunner.java
  - scripts/build_front.py
  - src/main/java/com/example/loginbase/web/PaginaController.java
---

# Primeiros passos

Neste tutorial você vai clonar o repositório, subir o banco de dados com Docker, compilar o frontend, rodar a aplicação e fazer login. Ao final, você terá uma instância local completa rodando em `http://localhost:8080/login`.

## O que você precisa

- WSL (Windows Subsystem for Linux) com Docker Desktop configurado para usar a integração WSL2.
- Git.
- JDK 25 ou IntelliJ IDEA (que traz seu próprio JDK).
- Node.js e npm (não são necessários no host, pois o frontend é compilado no Docker).
- Conhecimento básico de terminal e `make`.

## Passo 1 — Clonar o repositório

Se ainda não tiver o código, clone o repositório:

```bash
git clone https://github.com/seu-usuario/login_base.git
cd login_base
```

Se já tiver o repositório local, atualize:

```bash
git pull origin main
```

## Passo 2 — Copiar e configurar o arquivo de ambiente

Copie o arquivo de exemplo de variáveis de ambiente:

```bash
cp .env.example .env
```

Abra o `.env` gerado e procure a linha:

```
ADMIN_PASSWORD=
```

Defina uma senha para o administrador inicial:

```
ADMIN_PASSWORD=sua-senha-segura
```

Também confirme o e-mail padrão:

```
ADMIN_EMAIL=admin@loginbase.local
```

## Passo 3 — Subir o banco de dados

Na raiz do projeto, execute:

```bash
make up PROFILE_FRONTEND=desativado
```

Isso levanta o serviço de banco de dados (PostgreSQL) usando Docker Compose. A saída será algo como:

```
[+] Running 1/1
 ⠿ db Pulled
 ⠿ db Started
```

Você pode verificar que o banco está pronto assim:

```bash
docker compose logs db | grep "database system is ready to accept connections"
```

## Passo 4 — Compilar o frontend

O frontend precisa ser compilado antes da primeira execução, porque o template Thymeleaf em `templates/sistema/seguro/app/index.html` é gerado pelo build:

```bash
make build_front
```

Esse comando:
- Remove builds anteriores (`frontend/dist`, `static/app`, `templates/sistema/seguro/index.html`).
- Executa `npm run build` dentro de um container.
- Copia assets e scripts para `src/main/resources/static/app/` (não copia `index.html`, que fica em `templates/`).
- Gera o template `templates/sistema/seguro/app/index.html` necessário para servir a SPA integrada ao backend.

A saída mostra o progresso e finaliza com:

```
Build concluido com sucesso.
```

Você pode verificar que os arquivos foram criados:

```bash
ls -la src/main/resources/static/app/
ls -la src/main/resources/templates/sistema/seguro/app/
```

## Passo 5 — Rodar a aplicação

Com o banco e o frontend prontos, execute a aplicação. Há duas opções:

### Opção A: Pela linha de comando

Exporte o `.env` e rode com Maven:

```bash
set -a && . ./.env && set +a
./mvnw spring-boot:run
```

Você deve ver mensagens como:

```
Administrador inicial criado com e-mail admin@loginbase.local.
Started LoginBaseApplication in 3.456 seconds
```

A aplicação rodará em `http://localhost:8080` (porta padrão).

### Opção B: Pelo IntelliJ

1. Abra a pasta do projeto pelo caminho Windows (`D:\desenvolvimento\projetos\login_base`).
2. Clique em **Run > Edit Configurations**.
3. Crie ou edite uma configuração Spring Boot para a classe `LoginBaseApplication`.
4. Configure as variáveis de ambiente no formulário (ou deixe vazias para ler do `.env`).
5. Clique em **Run**.

Ver [Executar e depurar no IntelliJ](../guias/executar-e-depurar-no-intellij.md) para mais detalhes.

## Passo 6 — Fazer login

Abra seu navegador e acesse:

```
http://localhost:8080/login
```

Você verá a tela de login. Preencha:

- **E-mail ou celular:** `admin@loginbase.local` (ou o valor de `ADMIN_EMAIL` que você definiu)
- **Senha:** a senha que você definiu em `ADMIN_PASSWORD`

Clique em **Entrar**.

Você deve ver:

```
Seja bem-vindo
```

Isso indica que o login funcionou e a SPA (single-page application) Vue foi carregada com sucesso.

## Resultado

Parabéns! Você tem uma instância completa do login-base rodando localmente com:

- ✓ Banco de dados PostgreSQL no Docker.
- ✓ Frontend Vue compilado e integrado.
- ✓ Backend Spring Boot rodando.
- ✓ Administrador inicial criado.
- ✓ Login funcionando.

## Próximos passos

- **Desenvolver o frontend:** Veja [Desenvolver o frontend](../guias/desenvolver-o-frontend.md) para rodar o dev server com hot reload.
- **Executar testes:** Confira [Executar os testes](../guias/executar-os-testes.md).
- **Adicionar endpoints REST:** Leia [Adicionar um endpoint REST](../guias/adicionar-um-endpoint-rest.md).
- **Entender a arquitetura:** Consulte [Arquitetura](../explicacoes/arquitetura.md).
