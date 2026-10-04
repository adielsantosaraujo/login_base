---
titulo: Frontend Vue e PrimeVue em container
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-09-23
atualizado_em: 2026-10-04
fontes:
  - frontend/public/cadastro_usuario/Dockerfile
  - frontend/seguro/patrimonio/Dockerfile
  - docker-compose.yml
  - frontend/public/cadastro_usuario/vite.config.ts
  - frontend/seguro/patrimonio/vite.config.ts
  - frontend/public/cadastro_usuario/src/main.ts
  - frontend/seguro/patrimonio/src/main.ts
  - .env.example
---

# 0003 — Frontend Vue e PrimeVue em container

**Status:** Aceita · **Data:** 2026-09-23

## Contexto

O projeto precisa de um frontend Vue moderno. É necessário escolher: scaffold (Vite), biblioteca de componentes (PrimeVue), versão do Node, e como rodar tudo em Docker sem exigir Node no host.

Contexto técnico:
- Em 2026-09-23: `node:26` disponível com npm 11.19; npm 12 publicado (12.1.0).
- PrimeVue 5.0.1 com tema Aura, `@primeuix/license-manager` e licença por variável de ambiente.
- Vue 3.5.x, TypeScript, Vite 8.x.

## Decisão

- **Scaffold Vite oficial:** `npm create vite@latest frontend -- --template vue-ts`, gerado dentro de container Node 26 com npm 12.
- **PrimeVue 5** conforme documentação oficial do MCP: `npm install primevue @primeuix/themes`. Em `src/main.ts`: `app.use(PrimeVue, { theme: { preset: Aura }, license: import.meta.env.VITE_PRIMEUI_LICENSE })`.
- **Imagem Node 26-trixie-slim** no `frontend/Dockerfile` com `npm@12` instalado globalmente.
- **Desenvolvimento:** hot reload via `npm install … && npm run dev -- --host 0.0.0.0 --port 5173 --strictPort` dentro do container.
- **Polling no DrvFs:** `vite.config.ts` liga `server.watch.usePolling` quando `VITE_USE_POLLING=true` (passado pelo compose em desenvolvimento).
- **Volumes:** bind mount `./frontend:/app` para o código; volume nomeado `frontend-node-modules:/app/node_modules` para as dependências (filesystem Linux, rápido).
- **Licença PrimeVue:** variável `VITE_PRIMEUI_LICENSE` do compose, vindo do `.env` da raiz. Sem chave, a licença fica `undefined` e o PrimeVue exibe um aviso (aceito).

## Alternativas descartadas

- **Alpine em vez de Debian trixie-slim** — Rejeitada para manter a mesma família Debian trixie do Postgres e evitar incompatibilidades de musl com pacotes nativos.
- **Escrever os arquivos à mão** — Rejeitada porque o scaffold oficial do Vite já traz `tsconfig` e `vue-tsc` coerentes.
- **Node no host** — Rejeitada porque o objetivo é evitar dependências no host Windows/WSL.

## Consequências

### Positivas

- **Reprodutível:** o scaffold e as dependências vêm sempre do container, mesmo que versões globais do Node variem.
- **Hot reload no desenvolvimento:** as alterações no Windows (DrvFs) geram eventos dentro do container via polling e o Vite recompila.
- **Tema PrimeVue integrado:** Aura é o tema moderno, e a licença é configurável por variável (community gratuita ou comercial).
- **Sem node_modules no drive Windows:** o volume Linux mantém as dependências rápidas e não polui o `git status`.

### Negativas

- **Node 26 ainda não é LTS:** pode ter incompatibilidades pontuais. Mitigado pelo build (`npm run build`) rodar nas tasks e pegar problemas cedo.
- **npm@12 depende do registry no build:** pode haver atrasos ou falhas de rede.
- **Polling consome CPU:** ligado apenas no container e durante desenvolvimento; compensado pelo pequeno tamanho do projeto.
- **Volume `frontend-node-modules` pode ficar defasado:** ao trocar dependências, é necessário sincronizá-lo (`npm install` no container) ou deletá-lo (`docker volume rm login_base_frontend-node-modules`).
- **Aviso de licença PrimeVue:** sem chave community ou comercial, o browser exibe mensagem de aviso. > **A confirmar com o responsável:** qual é a solução prevista para mitigá-lo em produção.

