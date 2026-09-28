# Design

## Context

Atualmente, a aplicação Spring Boot serve apenas um placeholder Thymeleaf em `/` e não atende as rotas da SPA Vue (ex.: `/fazenda`, `/forja`). O `frontend/vite.config.ts` não define `base`, e não existe mapeamento no `PaginaController` para essas rotas nem serviço Docker dedicado ao build de produção do frontend.

Ver **proposal.md** para a motivação (por que precisamos servir a SPA em produção).

## Goals / Non-Goals

**Goals:**

- Executar o build Vue/Vite via script Python (`scripts/build_front.py`) e Docker Compose, com validação e log.
- Copiar os assets gerados para `src/main/resources/static/app/` e o `index.html` para `src/main/resources/templates/sistema/seguro/index.html`.
- Configurar o Spring Boot para servir a SPA em `/` e suas rotas (fallback do history mode).
- Permitir acesso público aos assets em `/app/**` (sem autenticação).
- Não versionar os artefatos gerados (`.gitignore` e testes independentes do build).

**Non-Goals:**

- Melhorar o dev server do frontend (vite dev já funciona).
- Implementar SSR ou pré-renderização das rotas.
- Adicionar novas funcionalidades ou componentes à SPA (escopo apenas técnico de build/serving).
- Suportar build do frontend sem Docker.

## Decisions

### 1. Script Python `scripts/build_front.py` no estilo de `executar.py`

**Decisão:** Criar um script Python dedicado que executa o build via `docker compose run --rm --build frontend-build`, valida que `frontend/dist/index.html` foi gerado, copia os assets e registra log em `build.log`. Funções: `log`, `run`, `gerar_dist`, `copiar_dist`, `main`.

**Rationale:** Padroniza com os scripts existentes (`executar.py`, `cores.py`). Log persistente facilita debug em CI/CD. Validação do `index.html` captura falhas de build antes de corromper a distribuição anterior.

**Alternativa descartada:** Shell script — menos portável entre Windows (WSL) e Linux; menos legível para lógica complexa.

### 2. Build via `docker compose run --rm --build` em vez de `up`

**Decisão:** Usar `docker compose run --rm --build frontend-build` em vez de `up` ou um container manual.

**Rationale:** `run --rm` resgata `node_modules` novo da imagem recém-buildada (não reutiliza cache antigo) e propaga corretamente o exit code do npm build. Com `up`, um build com erro sairia com status 0 e o dist antigo (ou inexistente) seria copiado sem aviso. `--build` força rebuild da imagem se o Dockerfile mudou.

**Alternativa descartada:** Executar npm build no host — requer Node.js instalado; problemas de versão entre dev e CI.

### 3. Serviço dedicado `frontend-build` com profile `build`

**Decisão:** Criar um serviço separado no `docker-compose.yml` com profile `build` em vez de reutilizar o serviço `frontend` existente.

**Rationale:** Não interfere no volume nomeado `frontend-node_modules` do dev (que persiste entre startups). Volume anônimo `/app/node_modules` do build é descartado no `--rm`, evitando poluição. Profile `build` garante que o serviço não sobe automaticamente em `make up`.

**Alternativa descartada:** Estender o serviço `frontend` — risco de corromper o volume do dev ou cache antigo impedir rebuild limpo.

### 4. Destinos: assets em `/app/` e template em templates/

**Decisão:** Assets em `src/main/resources/static/app/` (servidos em `/app/**` pelo handler estático padrão do Spring Boot); `index.html` em `src/main/resources/templates/sistema/seguro/index.html` (view devolvida pelo `PaginaController`).

**Rationale:** Separar assets (imutáveis, versionados em CDN potencial) da view (renderizada, CSRF via cookie). `index.html` não vai para static para não ficar acessível cru e desprotegido. O index gerado não precisa de atributos Thymeleaf (CSRF via cookie `XSRF-TOKEN`, `csrf.spa()` em SecurityConfig).

**Alternativa descartada:** Servir tudo de static/ — violaria controle de acesso; GET direto no index.html sem passar pelo controller.

### 5. Vite: `base: '/app/'` apenas no build

**Decisão:** `frontend/vite.config.ts`: `defineConfig(({ command }) => ({ base: command === 'build' ? '/app/' : '/', ... }))`. Dev server inalterado (base `/`); rotas do router não mudam.

**Rationale:** Dev funciona na raiz. Produção referencia assets em `/app/**`. Router continua na raiz (fallback do PaginaController redireciona para o index servido em `/`).

**Alternativa descartada:** Base estática `'/app/'` — quebrava o dev server.

### 6. `PaginaController`: lista explícita de rotas em vez de catch-all

**Decisão:** `@GetMapping({"/", "/fazenda", "/forja", "/quartel", "/masmorras", "/batalhas/{id}"})` mapeado para `sistema/seguro/index`, fallback do history mode.

**Rationale:** Lista explícita evita conflitar com `/api/**`, `/login` e erros 404 reais. Mais seguro que catch-all `/**`.

**Alternativa descartada:** Catch-all `@GetMapping("/**")` — capturaria rotas de erro, API, etc.; difícil diagnosticar problemas.

### 7. `SecurityConfig`: `/app/**` em permitAll

**Decisão:** Adicionar `/app/**` ao permitAll (sem autenticação necessária).

**Rationale:** Assets são públicos, sem dados sensíveis. Evita que o request cache da segurança salve um asset como destino pós-login (bug comum em SPAs).

**Alternativa descartada:** Require authentication para `/app/**` — aumentaria latência, criaria logs de negação falsos.

### 8. Não versionar gerados: `.gitignore` e `git rm --cached`

**Decisão:** `.gitignore` recebe `build.log`, `/src/main/resources/static/app/`, `/src/main/resources/templates/sistema/seguro/index.html`. Template placeholder atual sai do índice com `git rm --cached`.

**Rationale:** Artefatos gerados não devem ocupar repositório. Clone limpo executa `make build_front` antes de `./mvnw package` / `docker build` (documentado).

**Alternativa descartada:** Versionar template gerado — aumenta size do repo, conflita com merges, cria dificuldade em rastrear versão correta da SPA no git.

### 9. Testes sem depender do build

**Decisão:** Criar um template mínimo só de teste em `src/test/resources/templates/sistema/seguro/index.html` (ex.: `<div id="app"></div>`). Testes verificam status 200 e `view().name("sistema/seguro/index")`, não renderizam conteúdo.

**Rationale:** Testes rodam sem o build estar executado; classpath de teste tem precedência. Não força build como pré-requisito de testes.

**Alternativa descartada:** Versionar o template gerado — vira lixo nos commits; não sincroniza com source do frontend.

### 10. Makefile: alvo `build_front` com formatação de help

**Decisão:** Alvo `build_front: ## Gera o build do frontend e copia para o backend` chamando `python3 ./scripts/build_front.py`. Ajustar `%-10s` do help para `%-12s`.

**Rationale:** Aparece no menu `make e` existente; nomes descritivos facilitam descoberta.

**Alternativa descartada:** Esconder em arquivo de documentação — menos visível.

## Risks / Trade-offs

**[Risco] Clone limpo sem `make build_front` → erro 500 em `/`**
- **Causa:** Template não existe até o build rodar.
- **Mitigação:** Documentar em README e `docs/09-guia-desenvolvedor.md` (ordem de execução). CI deve executar `make build_front` antes de `./mvnw package` / `docker build`.

**[Risco] Novas rotas da SPA exigem atualizar a lista do `PaginaController`**
- **Causa:** Lista explícita não é dinâmica.
- **Mitigação:** Documentar o padrão. No futuro, considerar catch-all se escopo de rotas ficar muito grande (trade-off entre segurança e flexibilidade).

**[Risco] Build do frontend depende de Docker**
- **Causa:** Imagem Node.js fornece ambiente limpo e versionado.
- **Mitigação:** Docker é já required para desenvolvimento (`frontend` service). CI/CD deve ter Docker disponível. Sem Docker, não há build de produção (aceitável para projeto com deploy containerizado).

## Migration Plan

**Deploy:**
1. Executar `make build_front` uma vez para gerar `frontend/dist`, `static/app/` e o template.
2. Executar `./mvnw package` ou `docker compose build app` (frontend-build rodar antes automaticamente em CI/CD).
3. Deploy da nova imagem/jar (serve a SPA em `/`).

**Rollback:**
- Reverter a change (desfaz o script, controller, etc.) e redeploy.
- Clone anterior serve o placeholder "Seja bem vindo" em `/`.

## Open Questions

Nenhuma. Todas as decisões foram resolvidas com base no plano aprovado pelo usuário e na arquitetura existente.
