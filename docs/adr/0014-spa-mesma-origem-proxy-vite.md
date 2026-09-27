# 0014 — SPA na Mesma Origem via Proxy do Vite

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-26 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-city-builder-game`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/) (complementada por ADR 0023 — change `add-frontend-build`) |

## Contexto e problema

Frontend Vue (SPA) precisa comunicar com backend Spring (API REST). Há múltiplas estratégias: (1) CORS aberto (inseguro), (2) backend serve SPA em produção + proxy em dev, (3) proxy do Vite em desenvolvimento. Proxy do Vite é mais simples para dev e permite reutilizar autenticação existente (sessão HTTP, cookies) sem mudança de origem.

## Direcionadores da decisão

- Integração simples: reutilizar login Thymeleaf existente (`/login`)
- Cookies de sessão: `JSESSIONID` funciona sem CORS (mesma origem)
- Sem duplicação: login não é reescrito em Vue (fica em Thymeleaf)
- Desenvolvimento: proxy do Vite torna dev simples; em produção será diferente

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Proxy Vite em dev, SPA em produção** | Dev: Vite proxy `/api`, `/login`, etc. para backend. Prod: SPA servida por backend (não implementado). |
| CORS aberto | `Access-Control-Allow-Origin: *`. Rejeitada: inseguro; credenciais não passam. |
| Login em Vue | Reescrever `/login` em Vue. Rejeitada: fora de escopo (Non-Goal); Thymeleaf já funciona. |

## Resultado da decisão

Adotou-se **proxy do Vite em desenvolvimento**:

1. **Vite config** (`frontend/vite.config.ts`):
   ```typescript
   server: {
     proxy: {
       '/api': {
         target: process.env.BACKEND_URL || 'http://localhost:8080',
         changeOrigin: false,  // keep Host header
       },
       '/login': { ... },
       '/logout': { ... },
       '/css': { ... },
       '/js': { ... },
       '/images': { ... },
     }
   }
   ```
   > **Nota**: O código atual usa `process.env.BACKEND_URL ?? 'http://localhost'` (sem porta 8080); consulte `frontend/vite.config.ts` para valores exatos. A variável `BACKEND_URL` é configurada no `docker-compose.yml` e no `.env.example`.

2. **Fluxo de login**:
   - Usuário acessa `http://localhost:5173/`
   - App redireciona para `/login` (via proxy → backend `/login`)
   - Login Thymeleaf em porta 5173 (realmente 8080, mas proxy)
   - POST `/login` com credenciais + cookie `XSRF-TOKEN` (lido antes via GET `/api/catalogo`)
   - Redirect `/` com `JSESSIONID` cookie
   - App lê cookie, API acessível

3. **Envio de CSRF**: fetch (`src/api/http.ts`) lê `XSRF-TOKEN` cookie e envia em `X-XSRF-TOKEN` header

### Consequências positivas
- **Simples em dev**: sem CORS, sem duplicação de login
- **Cookies funcionam**: mesma origem (do ponto de vista do browser)
- **Desenvolvimento rápido**: hot reload do Vite +backend no IntelliJ
- **Login reutilizado**: não reescrever em Vue

### Consequências negativas
- **BREAKING**: proxy não funciona em produção (seria diferente)
- **Mudança de arquitetura**: em produção precisa de build SPA + servidor (não pedido agora — **Previsto na [ADR 0023](0023-spa-servida-pelo-backend.md) — change `add-frontend-build` (aberta)**)
- **Chave de licença PrimeUI**: com redirect, chave precisa estar em `.env` do frontend (mitigado)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Proxy Vite | Simples; sem CORS; login reutilizado; desenvolvimento rápido. | Não funciona em produção; requer refatoração depois. |
| CORS aberto | Mais flexível. | Inseguro; credenciais não passam. |
| Login em Vue | Centralizado em SPA. | Duplicação; fora de escopo. |

## Mais informações

- **Design**: [`add-city-builder-game/design.md` §19–21](../../openspec/changes/archive/2026-09-27-add-city-builder-game/design.md)
- **Vite config**: [`frontend/vite.config.ts`](../../frontend/vite.config.ts) — proxy
- **HTTP client**: [`frontend/src/api/http.ts`](../../frontend/src/api/http.ts) — fetch com CSRF
- **Docker Compose**: [`docker-compose.yml`](../../docker-compose.yml) — `BACKEND_URL` no serviço `frontend`

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.1.0 | 2026-09-27 | Referência para ADR 0023 (complementação para produção); nota sobre BACKEND_URL | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
