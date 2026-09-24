# Design

## Context

- O repositório tem o backend Spring Boot na raiz. O Docker roda no WSL (Compose v2.32.4) com os arquivos no drive Windows (`/mnt/d`, DrvFs), e os serviços são escolhidos por profiles (spec `docker-dev-environment`).
- Versões verificadas em 23/09/2026: imagem `node:26` disponível (26.10.0, ainda não é LTS), que vem com **npm 11.19**; npm 12 publicado (12.1.0); `primevue` 5.0.1 (depende de `@primeui/license-manager`), `@primeuix/themes` 3.0.1, `vue` 3.5.x, `vite` 8.x.
- O guia de instalação com Vite, consultado no MCP do PrimeVue (`get_setup`, environment `vite`), pede: `npm install primevue @primeuix/themes`, `app.use(PrimeVue, { theme: { preset: Aura }, license: '<chave>' })` e verificar com um componente.
- Em 23/09/2026 o `Makefile` tinha `COMPOSE ?= docker compose --profile "$(PROFILE)"`, `export` sem argumentos e `down` usando `$(COMPOSE) down`, ou seja, só o profile ativo. Isso diverge da spec, e o usuário decidiu voltar o `down` para `--profile "*"`.
- Motivação: ver proposal.md. Requisitos: `specs/frontend-app/spec.md` e `specs/docker-dev-environment/spec.md`.

## Goals / Non-Goals

**Goals:**
- Frontend funcional em Docker, com hot reload, sem exigir Node no host.
- Setup do PrimeVue fiel à documentação oficial, obtida pelo MCP.

**Non-Goals:**
- Build de produção servido por Nginx ou imagem de produção do frontend.
- Integração com a API do backend, CORS ou proxy para o Spring.
- Roteamento (vue-router), estado global, testes de frontend e lint. Ficam para changes futuras.

## Decisions

1. **Scaffold oficial do Vite, gerado dentro de um container Node 26**: `npm create vite@latest frontend -- --template vue-ts`, de forma não interativa, executado com `docker run` em `node:26` com npm 12. Assim o `package-lock.json` sai do npm 12 e ninguém precisa de Node 26 no host. Depois, remove-se o exemplo do template (`HelloWorld.vue`, assets e estilos de demonstração).
   - Alternativa considerada: escrever os arquivos à mão. Rejeitada porque o template oficial já traz `tsconfig` e `vue-tsc` coerentes com a versão do Vite.

2. **PrimeVue conforme o MCP**: instalar `primevue` e `@primeuix/themes`. Em `src/main.ts`, `app.use(PrimeVue, { theme: { preset: Aura }, license: import.meta.env.VITE_PRIMEUI_LICENSE })`, com `VITE_PRIMEUI_LICENSE` tipada em `src/vite-env.d.ts`. Antes de escrever, o implementador consulta de novo o MCP (`get_setup` vite e `get_guide` configuration) para confirmar a forma exata. Se o guia exigir, uma chave vazia vira `undefined`.
   - A licença entra pelo ambiente do container (`environment` do compose, vindo do `.env` da raiz). O Vite expõe em `import.meta.env` as variáveis `VITE_*` do processo, então não é preciso um `.env` dentro de `frontend/`.

3. **Página inicial**: o `App.vue` exibe "Seja bem-vindo" dentro de um componente PrimeVue simples (por exemplo `Card`), para comprovar que o tema e o plugin funcionam. O uso é validado com a ferramenta `validate_usage` do MCP. Se o MCP apontar o componente como obsoleto, usa-se o substituto indicado. Não há vue-router: a única página é a própria raiz.

4. **Dockerfile de desenvolvimento** (`frontend/Dockerfile`): `FROM node:26-trixie-slim`, `RUN npm install -g npm@12`, `WORKDIR /app`, cópia de `package.json`/`package-lock.json`, `RUN npm ci`, cópia do restante, `EXPOSE 5173` e comando `sh -c "npm install --no-audit --no-fund && npm run dev -- --host 0.0.0.0 --port 5173 --strictPort"`. O `npm install` no início é rápido quando nada mudou e mantém o volume de dependências sincronizado com o `package.json`.
   - Alternativa considerada: a imagem `alpine`. Rejeitada para manter a mesma família Debian trixie do Postgres e evitar diferenças da musl com pacotes nativos.

5. **Serviço `frontend` no compose**: `build: ./frontend`, `profiles: ["${PROFILE_FRONTEND:-local}"]`, `ports: "5173:5173"`, bind mount `./frontend:/app` e volume nomeado `frontend-node-modules:/app/node_modules`. Com o volume, as dependências ficam no filesystem Linux (rápido) e o `node_modules` não vai para o drive Windows. `environment` recebe `VITE_PRIMEUI_LICENSE: ${VITE_PRIMEUI_LICENSE:-}`.

6. **Hot reload no DrvFs**: alterações feitas no Windows não geram eventos inotify dentro do container. O `vite.config.ts` liga `server.watch.usePolling` quando a variável `VITE_USE_POLLING=true`, que o compose define. Fora do Docker, o polling fica desligado.

7. **Makefile**: adiciona `PROFILE_FRONTEND ?= local` e mantém `COMPOSE` e `export` (edição do usuário). O `down` volta a `docker compose --profile "*" down`, sem `-v`, conforme a spec. O comentário de uso passa a citar `PROFILE_FRONTEND`.

8. **Ignorar arquivos**: `frontend/` entra no `.dockerignore` da raiz, para o build do backend não enviar o frontend. `frontend/.dockerignore` ignora `node_modules` e `dist`. O `.gitignore` gerado pelo template dentro de `frontend/` é mantido.

9. **MCP disponível para quem implementa**: o plugin `primevue@primeui` foi instalado nesta máquina, mas as ferramentas MCP só aparecem numa sessão do Claude Code iniciada depois da instalação. Antes do apply, a sessão deve ser reiniciada, para que o orquestrador e os subagentes tenham `get_setup`, `get_guide`, `get_component` e `validate_usage`.

## Risks / Trade-offs

- [O Node 26 ainda não é LTS e pode ter incompatibilidades pontuais com ferramentas] → A versão foi pedida explicitamente. O build (`npm run build`) nas tasks pega problemas cedo.
- [A imagem `node:26` traz npm 11; `npm install -g npm@12` depende do registry no build] → Fixar a major (`npm@12`) e verificar `npm --version` no container.
- [O volume `frontend-node-modules` pode ficar defasado ao trocar dependências] → O `npm install` no início do container sincroniza. Em último caso, `docker volume rm login_base_frontend-node-modules`.
- [Polling consome CPU] → Só é ligado no container, e o projeto é pequeno.
- [Sem chave de licença o PrimeVue pode mostrar um aviso] → É aceito pela spec. O README explica como obter a chave community gratuita e onde colocá-la.
- [`make up` passa a subir o frontend por padrão (BREAKING)] → Documentado no README. `PROFILE_FRONTEND=desativado` volta ao comportamento antigo.
- [Arquivos gerados por um container rodando como root] → No DrvFs as permissões são mapeadas para o usuário do Windows/WSL, então não há arquivos root no host. Isso é verificado ao gerar o scaffold.
