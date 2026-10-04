---
titulo: SPA servida pelo Spring em /app
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-10-02
atualizado_em: 2026-10-04
fontes:
  - src/main/java/com/example/loginbase/web/PaginaController.java
  - scripts/build_front.py
  - frontend/vite.config.ts
  - frontend/src/router/index.ts
  - src/main/java/com/example/loginbase/seguranca/SecurityConfig.java
---

# 0011 — SPA servida pelo Spring em /app

**Status:** Aceita · **Data:** 2026-10-02

## Contexto

O frontend Vue (Vite + vue-router) roda isolado durante desenvolvimento. Ao deployar, a SPA precisa ser integrada ao backend (servidor único). É necessário escolher: outro servidor (Nginx), CORS/JWT, ou servir tudo pelo Spring.

A autenticação é por formulário e sessão HTTP (já em produção no backend). O frontend não possui autenticação própria; todas as rotas clientside são seguras (exigem login).

## Decisão

Integrar a SPA ao backend Spring:

1. **Build:** script Python `build_front.py` roda `docker compose run frontend-build` (Vite), limpa `frontend/dist`, copia assets para `src/main/resources/static/app/` e o `index.html` para `src/main/resources/templates/sistema/seguro/app/index.html`.

2. **Base URL:** no build (produção), `vite.config.ts` define `base: '/app/'`; no dev (`npm run dev`), fica `'/'`.

3. **Rotas no Spring:** `PaginaController` mapeia:
   - `/app/index` → template `index.html`
   - `/app/{s1}`, `/app/{s1}/{s2}`, ... (até 5 níveis) sem ponto → template `index.html` (delegando navegação ao vue-router)
   - `/app/**` com extensão (ex.: `.js`, `.css`) → recursos estáticos em `static/app/`
   - `/`, `/app`, `/app/` → redirect para `/app/index`

4. **CSRF na SPA:** `csrf.spa()` no SecurityConfig faz o Spring enviar o token em cookie `XSRF-TOKEN` (sem `HttpOnly`) e esperar `X-XSRF-TOKEN` no cabeçalho de requisições POST/PUT/DELETE. Bibliotecas JS/frameworks capturam o cookie automaticamente.

5. **Request cache sem SPA:** configurado para não cacher `/app/**`, `/favicon.ico`, `/.well-known/**`, `/api/**` e `/v3/api-docs/**`, impedindo que reloads da SPA sequestrem o redirecionamento pós-login.

6. **Desenvolvimento:** `npm run dev` inicia Vite em `http://localhost:5173` com proxy para `/api`, `/login`, `/logout` e `/css` apontando para o backend (porta 8080 ou conforme `VITE_BACKEND_URL`). `changeOrigin: false` faz o redirect pós-login voltar ao frontend.

## Alternativas descartadas

- **Nginx separado** — Rejeitada porque aumenta complexidade e exige sincronização de deployment.
- **CORS e JWT** — Rejeitada porque não é objetivo do design de autenticação; formulário e sessão HTTP já resolvem.

## Consequências

### Positivas

- **Um único servidor:** deployment simplificado; sem sincronização entre frontend e backend.
- **Autenticação única:** a SPA herda a sessão HTTP autenticada pelo formulário (passando no cookie `JSESSIONID`).
- **Recursos estáticos integrados:** assets da SPA são servidos pelo Spring como recursos estáticos.
- **Sem CORS:** SPA e backend na mesma origem eliminam complexidade de CORS.

### Negativas

- **Build acoplado:** `make build_front` é obrigatório antes do deploy; sem ele o template `/app/index` não existe.
- **Profundidade limitada:** padrões de rota explícitos (até 5 níveis) para não sombrear recursos estáticos com extensão.
- **Solteiro em produção:** exigindo uma instância única; escalabilidade horizontal exige revisão arquitetural.

