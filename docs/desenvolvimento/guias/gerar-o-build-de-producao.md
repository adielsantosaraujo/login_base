---
titulo: Gerar o build de produção
publico: desenvolvimento
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - scripts/build_front.py
  - Makefile
  - docker-compose.yml
  - .gitignore
  - README.md
---

# Gerar o build de produção

## Quando usar

Use este guia quando quiser compilar o frontend e o backend para gerar um JAR pronto para deploy ou testes em ambiente integrado.

## Pré-requisitos

- Projeto clonado e `.env` configurado (veja [Primeiros passos](../tutoriais/primeiros-passos.md)).
- Docker Desktop com integração WSL2 habilitada.
- Maven wrapper (`./mvnw`) funcionando (Java 25 disponível no sistema host ou via IntelliJ).

## Passos

1. **Faça o build de todos os frontends.**

   Execute na raiz do projeto (WSL):

   ```bash
   make build_front
   ```

   Ou diretamente com Python:

   ```bash
   python3 ./scripts/build_front.py
   ```

   Esse comando:
   - Descobre todos os apps em `frontend/public/*/` e `frontend/seguro/*/`.
   - Para cada app (ex.: `cadastro_usuario`, `patrimonio`):
     - Remove builds anteriores (`frontend/<app>/dist`, `static/<nome>/`, template antigo).
     - Executa `npm run build` dentro de um container Docker com `FRONT_APP=<area>/<nome>` e `FRONT_APP_NOME=<nome>`.
     - Valida que `frontend/<app>/dist/index.html` foi gerado.
     - Copia os assets para `src/main/resources/static/<nome>/`.
     - Gera o template Thymeleaf em `src/main/resources/templates/sistema/<area>/<nome>/index.html`.
   - Registra tudo em `build.log`.

   A saída será algo como:

   ```
   ==> Iniciando build do frontend
   ==> App public/cadastro_usuario
   ==> Removendo frontend/public/cadastro_usuario/dist
   ==> Removendo src/main/resources/static/cadastro_usuario
   ==> Executando: docker compose run --rm --build frontend-build
   ==> App seguro/patrimonio
   ==> Removendo frontend/seguro/patrimonio/dist
   ==> Removendo src/main/resources/static/patrimonio
   ==> Executando: docker compose run --rm --build frontend-build
   Build concluido com sucesso: cadastro_usuario, patrimonio.
   ```

   > **Dica:** se o build falhar, verifique `build.log` para detalhes. Possível: erro de sintaxe em `.vue`, arquivo faltando ou dependência não instalada.

   **Builds seletivos:** durante desenvolvimento, você pode buildar só um app:

   ```bash
   python3 ./scripts/build_front.py cadastro_usuario
   python3 ./scripts/build_front.py patrimonio
   ```

2. **Valide que os arquivos foram gerados.**

   Após o build, confirme que todos os apps estão no lugar:

   ```bash
   ls -la src/main/resources/static/cadastro_usuario/ | head -20
   ls -la src/main/resources/static/patrimonio/ | head -20
   cat src/main/resources/templates/sistema/public/cadastro_usuario/index.html | head -10
   cat src/main/resources/templates/sistema/seguro/patrimonio/index.html | head -10
   ```

   Você deve ver em cada app:
   - Diretório `assets/` com `index-xxxxx.js`, `index-xxxxx.css` minificados.
   - `favicon.svg`.
   - Fontes primeicons.

3. **Compile o JAR do backend.**

   Com o frontend gerado, compile o backend com Maven:

   ```bash
   ./mvnw -DskipTests package
   ```

   Isso:
   - Compila o código Java.
   - Empacota tudo em um JAR executável em `target/login-base-0.0.1-SNAPSHOT.jar`.
   - Não acessa o banco (o `package` com `-DskipTests` não roda validação de banco).

   A compilação leva cerca de 1–2 minutos. Você verá:

   ```
   [INFO] Building jar: .../target/login-base-0.0.1-SNAPSHOT.jar
   [INFO] BUILD SUCCESS
   ```

4. **Teste o JAR (opcional).**

   Se quiser verificar que o JAR funciona:

   ```bash
   set -a && . ./.env && set +a
   java -jar target/login-base-0.0.1-SNAPSHOT.jar
   ```

   O backend deve iniciar e você acessa normalmente em `http://localhost:8080/login`.

   Pressione `Ctrl+C` para parar.

## Arquivos gerados

Após o build, o projeto contém (exemplo com 2 apps):

| Arquivo | Descrição |
|---|---|
| `frontend/public/cadastro_usuario/dist/` | Build output do Vite (HTML, JS, CSS minificados). Ignorado pelo git. |
| `frontend/seguro/patrimonio/dist/` | Build output do Vite (HTML, JS, CSS minificados). Ignorado pelo git. |
| `src/main/resources/static/cadastro_usuario/` | Assets copiados para o classpath do Spring. Ignorado pelo git. |
| `src/main/resources/static/patrimonio/` | Assets copiados para o classpath do Spring. Ignorado pelo git. |
| `src/main/resources/templates/sistema/public/cadastro_usuario/index.html` | Template Thymeleaf da SPA pública. Ignorado pelo git. |
| `src/main/resources/templates/sistema/seguro/patrimonio/index.html` | Template Thymeleaf da SPA segura. Ignorado pelo git. |
| `target/login-base-0.0.1-SNAPSHOT.jar` | JAR executável final com todos os apps. Ignorado pelo git. |
| `build.log` | Log da execução (data, hora, comandos, erros). Ignorado pelo git. |

## Limpeza

Se precisar recomeçar o build:

```bash
make build_front  # limpa automaticamente antes de buildar novamente
```

Ou com limpeza completa (sem rebuild imediato):

```bash
make limpar_front
```

Ou manualmente:

```bash
rm -rf frontend/public/*/dist frontend/seguro/*/dist
rm -rf src/main/resources/static/{cadastro_usuario,patrimonio}
rm -rf src/main/resources/templates/sistema/public/*/index.html
rm -rf src/main/resources/templates/sistema/seguro/*/index.html
rm -f build.log
```

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| `docker: command not found` | Docker não está instalado ou acessível | Instale Docker Desktop com integração WSL2. Rode do terminal WSL, não do PowerShell. |
| Build do frontend falha com `npm not found` | Container `frontend-build` não consegue executar npm | Reconstrói a imagem: `docker compose --profile local build --no-cache frontend-build` e tente novamente. |
| `frontend/dist` não foi criado | Build do Vite falhou silenciosamente | Verifique `build.log` para detalhes. Possível: erro de sintaxe em `.vue` ou `vite.config.ts`. |
| JAR falha com `ddl-auto validation error` | Esquema do banco não bate com as migrações | Execute `make down_v && make up PROFILE_FRONTEND=desativado` para recriar o banco e tente novamente. |
| `JAVA_HOME` aponta para Windows, falha no Maven | Caminho do JDK do Windows usado em vez do WSL | Configure `JAVA_HOME=/usr/lib/jvm/jdk-25.0.2-oracle-x64` (ou seu caminho) antes de rodar `./mvnw`. |

## Como verificar

Se o build foi bem-sucedido:

- ✓ `build.log` termina com "Build concluido com sucesso: ..." (listar apps).
- ✓ `src/main/resources/templates/sistema/public/cadastro_usuario/index.html` existe.
- ✓ `src/main/resources/templates/sistema/seguro/patrimonio/index.html` existe.
- ✓ `src/main/resources/static/cadastro_usuario/` contém assets.
- ✓ `src/main/resources/static/patrimonio/` contém assets.
- ✓ `target/login-base-0.0.1-SNAPSHOT.jar` foi criado (tamanho > 50MB).
- ✓ `./mvnw -DskipTests package` retornar "BUILD SUCCESS".
- ✓ Você consegue rodar `java -jar target/login-base-0.0.1-SNAPSHOT.jar` e acessar:
  - `http://localhost:8080/login` (formulário de login)
  - `http://localhost:8080/cadastro_usuario/index` (SPA pública)
  - `http://localhost:8080/patrimonio/index` (redireciona para login se não autenticado)

## Veja também

- [Primeiros passos](../tutoriais/primeiros-passos.md) — setup do projeto.
- [Desenvolver o frontend](./desenvolver-o-frontend.md) — dev server com hot reload.
- [Executar e depurar no IntelliJ](./executar-e-depurar-no-intellij.md) — debugging durante o desenvolvimento.
