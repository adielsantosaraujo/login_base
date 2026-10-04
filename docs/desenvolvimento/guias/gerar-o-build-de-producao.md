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

1. **Faça o build do frontend.**

   Execute na raiz do projeto (WSL):

   ```bash
   make build_front
   ```

   Ou diretamente com Python:

   ```bash
   python3 ./scripts/build_front.py
   ```

   Esse comando:
   - Remove builds anteriores (`frontend/dist`, `static/app`, template legado).
   - Executa `npm run build` dentro de um container Docker (`frontend-build`).
   - Valida que `frontend/dist/index.html` foi gerado.
   - Copia os assets para `src/main/resources/static/app/`.
   - Gera o template Thymeleaf em `src/main/resources/templates/sistema/seguro/app/index.html`.
   - Registra tudo em `build.log`.

   A saída será algo como:

   ```
   ==> Iniciando build do frontend
   ==> Removendo frontend/dist
   ==> Removendo src/main/resources/static/app
   ==> Removendo src/main/resources/templates/sistema/seguro/index.html
   ==> Executando: docker compose run --rm --build frontend-build
   Build concluido com sucesso.
   ```

   > **Dica:** se o build falhar, verifique `build.log` para detalhes.

2. **Valide que os arquivos foram gerados.**

   Após o build, confirme que os arquivos estão no lugar:

   ```bash
   ls -la src/main/resources/static/app/ | head -20
   cat src/main/resources/templates/sistema/seguro/app/index.html | head -10
   ```

   Você deve ver arquivos como `favicon.svg`, `assets/index-xxxxx.js`, `assets/index-xxxxx.css` e fontes primeicons.

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

Após o build, o projeto contém:

| Arquivo | Descrição |
|---|---|
| `frontend/dist/` | Build output do Vite (HTML, JS, CSS minificados). Ignorado pelo git. |
| `src/main/resources/static/app/` | Assets copiados para o classpath do Spring. Ignorado pelo git. |
| `src/main/resources/templates/sistema/seguro/app/index.html` | Template Thymeleaf com o index.html da SPA. Ignorado pelo git. |
| `target/login-base-0.0.1-SNAPSHOT.jar` | JAR executável final. Ignorado pelo git. |
| `build.log` | Log da execução (data, hora, comandos, erros). Ignorado pelo git. |

## Limpeza

Se precisar recomeçar o build:

```bash
make build_front  # limpa automaticamente
```

Ou manualmente:

```bash
rm -rf frontend/dist src/main/resources/static/app src/main/resources/templates/sistema/seguro/app/index.html
rm -f src/main/resources/templates/sistema/seguro/index.html  # template legado
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

- ✓ `build.log` termina com "Build concluido com sucesso."
- ✓ `src/main/resources/templates/sistema/seguro/app/index.html` existe (o template Thymeleaf).
- ✓ `target/login-base-0.0.1-SNAPSHOT.jar` foi criado (tamanho > 50MB).
- ✓ `./mvnw -DskipTests package` retornar "BUILD SUCCESS".
- ✓ Você consegue rodar `java -jar target/login-base-0.0.1-SNAPSHOT.jar` e acessar `http://localhost:8080/login`.

## Veja também

- [Primeiros passos](../tutoriais/primeiros-passos.md) — setup do projeto.
- [Desenvolver o frontend](./desenvolver-o-frontend.md) — dev server com hot reload.
- [Executar e depurar no IntelliJ](./executar-e-depurar-no-intellij.md) — debugging durante o desenvolvimento.
