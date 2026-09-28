# Proposal

## Why

Atualmente, não existe forma de gerar o build de produção do frontend Vue/Vite e colocá-lo dentro da aplicação Java. O Spring serve apenas um placeholder Thymeleaf "Seja bem vindo" em `/`, não atendendo as rotas da SPA. Queremos um comando único que gere o `dist` e o copie para o backend, permitindo que a Spring sirva a SPA em produção.

## What Changes

- **Novo script `scripts/build_front.py`**: Executa o build via `docker compose run --rm --build frontend-build`, valida que `frontend/dist/index.html` foi gerado, copia os assets para `src/main/resources/static/app/` e o `index.html` para `src/main/resources/templates/sistema/seguro/index.html`; registra log em `build.log`; aborta com o exit code em caso de erro.
- **Novo serviço `frontend-build` no `docker-compose.yml`**: Profile `build`, não sobe automaticamente no `make up`.
- **Novo alvo `make build_front`**: Executa o script; aparece no menu `make e`.
- **`frontend/vite.config.ts`**: `base: '/app/'` apenas no build; dev server inalterado.
- **`PaginaController`**: Mapeia `/`, `/fazenda`, `/forja`, `/quartel`, `/masmorras`, `/batalhas/{id}` para a view da SPA (fallback do history mode).
- **`SecurityConfig`**: `/app/**` público (assets sem dados sensíveis).
- **BREAKING**: `/` deixa de mostrar o placeholder "Seja bem vindo" e passa a servir a SPA.
- **BREAKING**: `static/app/` e o template `sistema/seguro/index.html` passam a ser gerados e ficam no `.gitignore`; clone limpo requer `make build_front` antes de `./mvnw package` / `docker build`.
- **Testes WebMvc**: Ajustados para não depender do conteúdo gerado; docs atualizadas.

## Capabilities

### New Capabilities

<!-- Nenhuma nova capability introduzida. -->

### Modified Capabilities

- `user-authentication`: Requisito "Página inicial segura" — `/` passa a servir a SPA em vez do placeholder "Seja bem vindo".
- `frontend-app`: Build de produção servido pelo Spring em `/` e nas rotas do history; assets em `/app/**`; gerado por `make build_front`.

## Impact

- **Código**: `scripts/build_front.py`, `frontend/vite.config.ts`, `docker-compose.yml`, `src/main/java/com/example/loginbase/web/PaginaController.java`, `src/main/java/com/example/loginbase/seguranca/SecurityConfig.java`, `Makefile`, `.gitignore`.
- **Testes**: `src/test/java/com/example/loginbase/web/AutenticacaoWebMvcTest.java`.
- **Documentação**: `docs/09-guia-desenvolvedor.md`, `docs/10-implantacao-operacao.md`.
- **Build e deployment**: Build do frontend precede o do backend; requer Docker para o build do frontend; clone limpo precisa executar `make build_front` antes de `./mvnw package` ou `docker build`.
