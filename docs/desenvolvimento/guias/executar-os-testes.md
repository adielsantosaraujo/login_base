---
titulo: Executar os testes
publico: desenvolvimento
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - src/test/java/com/example/loginbase/web/AutenticacaoWebMvcTest.java
  - src/test/java/com/example/loginbase/web/OpenApiIntegracaoTest.java
  - pom.xml
  - frontend/public/cadastro_usuario/package.json
  - frontend/seguro/patrimonio/package.json
  - frontend/public/cadastro_usuario/vite.config.ts
  - frontend/seguro/patrimonio/vite.config.ts
  - scripts/apps_front.py
  - docker-compose.yml
---

# Executar os testes

## Quando usar

Use este guia para rodar a suite de testes do backend (Java/JUnit) e do frontend (TypeScript/Vitest) de forma isolada ou integrada.

## Pré-requisitos

- Projeto clonado (veja [Primeiros passos](../tutoriais/primeiros-passos.md)).
- Para testes de integração do backend: PostgreSQL rodando no Docker (`make up PROFILE_FRONTEND=desativado`).
- Para testes do frontend: Docker configurado.

## Passos

### Testes do backend

1. **Suba o banco de dados (se não estiver rodando).**

   Alguns testes precisam do Postgres. Execute:

   ```bash
   make up PROFILE_FRONTEND=desativado
   ```

2. **Rode todos os testes.**

   Na raiz do projeto (WSL):

   ```bash
   ./mvnw test
   ```

   O Maven executa todas as classes `*Test.java` em `src/test/`. A saída mostra:

   ```
   [INFO] Tests run: 85, Failures: 0, Errors: 0, Skipped: 0
   [INFO] BUILD SUCCESS
   ```

   > **Nota:** Se `JAVA_HOME` apontar para um caminho do Windows em vez do WSL, use um JDK do Linux. Exemplo: `JAVA_HOME=/usr/lib/jvm/jdk-25.0.2-oracle-x64 ./mvnw test`

3. **Rode um teste específico.**

   Para rodar só uma classe ou método:

   ```bash
   ./mvnw test -Dtest=AutenticacaoWebMvcTest
   ./mvnw test -Dtest=AutenticacaoWebMvcTest#apiDocsAnonimoRetorna401SemRedirecionar
   ```

4. **Rode testes excluindo os de integração.**

   Se não quiser esperar pelo banco ou a conexão falhar, rode só testes de unidade:

   ```bash
   ./mvnw test -Dtest='!LoginBaseApplicationTests,!OpenApiIntegracaoTest'
   ```

   Isso executa testes `@WebMvcTest` (fatias sem banco) e unitários.

### Testes do frontend

1. **Rode os testes do Vue/TypeScript.**

   Na raiz do projeto:

   ```bash
   docker compose run --rm --build frontend-build npm test
   ```

   Isso executa o Vitest (equivalente Jest) dentro de um container. A saída mostra:

   ```
   ✓ src/views/HomeView.spec.ts (1 test)
   ✓ src/router/index.spec.ts (2 tests)

   Tests  3 passed (3)
   ```

2. **Rode um teste específico.**

   Acesse a pasta `frontend/` e use Vitest diretamente:

   ```bash
   cd frontend
   npm test -- --reporter=verbose HomeView.spec.ts
   cd ..
   ```

   > **Nota:** este método exige Node.js e npm instalados no host. Se você não tem Node no host, use o container conforme o passo anterior (com `docker compose run`).

## Testes inclusos

### Backend

A suite cobre:

| Classe | Propósito |
|---|---|
| `AutenticacaoWebMvcTest` | Fatia (`@WebMvcTest`): login, logout, autenticação, CSRF, Swagger UI e `/v3/api-docs` (sem banco). |
| `OpenApiIntegracaoTest` | Integração (`@SpringBootTest`): OpenAPI com banco, validação do Swagger. |
| `LoginBaseApplicationTests` | Contexto completo (`@SpringBootTest`): startup da aplicação, beans básicos. |
| `acesso/UsuarioTest` | Acesso: entidade e persistência de usuário. |
| `auditoria/UsuarioAuditorAwareTest` | Auditoria: registro automático de quem alterou. |
| `seguranca/AdminInicialRunnerTest` | Inicialização do administrador ao startup. |
| `seguranca/IdentificadorLoginTest` | Extração de identificador do login. |
| `seguranca/RegistroSessaoSuccessHandlerTest` | Registro de sessão após autenticação bem-sucedida. |
| `seguranca/SessaoEncerradaListenerTest` | Encerramento de sessão. |
| `seguranca/SessaoServiceTest` | Serviço de gerenciamento de sessão. |
| `seguranca/SessoesAbertasRunnerTest` | Listar sessões abertas ao startup. |
| `seguranca/UsuarioDetailsServiceTest` | Carregamento de dados do usuário para autenticação. |

### Frontend

| Arquivo | Propósito |
|---|---|
| `src/views/HomeView.spec.ts` | Renderização da tela inicial (Seja bem-vindo). |
| `src/router/index.spec.ts` | Rotas do vue-router e navegação. |

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| `org.postgresql.util.PSQLException: Connection refused` | Banco não está rodando | Execute `make up PROFILE_FRONTEND=desativado` e aguarde o banco ficar pronto. |
| `Flyway validation failed` | Banco com schema divergente | Execute `make down_v && make up PROFILE_FRONTEND=desativado` para recriar do zero. |
| Container `frontend-build` falha no `npm test` | Imagem desatualizada após mudança no código | Reconstrua a imagem com `docker compose build --no-cache frontend-build` ou suba com `docker compose up --build` e tente novamente. |

## Stub para testes

Os stubs HTML mínimos em `src/test/resources/templates/sistema/{public,seguro}/<nome>/index.html` são usados pelos testes para servir as SPAs sem rodar o build completo. 

Exemplos:
- `src/test/resources/templates/sistema/public/cadastro_usuario/index.html` — stub da SPA pública.
- `src/test/resources/templates/sistema/seguro/patrimonio/index.html` — stub da SPA segura.

> **Problema conhecido:** estes stubs são ignorados pelo git (`.gitignore`: `/src/test/resources/templates/sistema/{public,seguro}/*`) e não são versionados. Num clone limpo, sem executar `make build_front`, os testes tendem a falhar. Execute `make build_front` antes dos testes para gerar os templates necessários.

## Como verificar

Se todos os testes passarem:

- ✓ `./mvnw test` retorna "BUILD SUCCESS" com todos os testes passando.
- ✓ `docker compose run --rm --build frontend-build npm test` mostra "Test Files X passed (X)".
- ✓ Nenhuma falha ou erro na saída.

## Veja também

- [Primeiros passos](../tutoriais/primeiros-passos.md) — setup inicial.
- [Gerar o build de produção](./gerar-o-build-de-producao.md) — empacotar para deploy.
- [Desenvolver o frontend](./desenvolver-o-frontend.md) — dev server com testes em tempo real.
