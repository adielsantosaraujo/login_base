# Tasks

## 1. Projeto frontend (Vue 3 + TS + Vite + PrimeVue 5)

- [x] 1.1 Gerar o scaffold em `frontend/` com `npm create vite@latest frontend -- --template vue-ts`, de forma não interativa, dentro de um container `node:26` com `npm install -g npm@12` (via `docker run --rm -v "$PWD":/w -w /w ...`), e verificar que `frontend/package.json` e `frontend/package-lock.json` existem e que os arquivos no host não ficaram com dono root
- [x] 1.2 Consultar o MCP do PrimeVue (`get_setup` environment `vite` e `get_guide` `configuration`), instalar `primevue` e `@primeuix/themes` (no container Node 26/npm 12), registrar o plugin em `src/main.ts` com o preset Aura e `license: import.meta.env.VITE_PRIMEUI_LICENSE` (tipada em `src/vite-env.d.ts`), e verificar que `npm run build` passa no container
- [x] 1.3 Substituir o conteúdo de exemplo do template por uma página única que exibe "Seja bem-vindo" dentro de um componente PrimeVue (ex.: `Card`), validar o uso com `validate_usage` do MCP, remover `HelloWorld.vue` e assets/estilos de demonstração, e verificar que `npm run build` passa e o HTML gerado ou renderizado contém "Seja bem-vindo"
- [x] 1.4 Configurar o `vite.config.ts` para ligar `server.watch.usePolling` quando `VITE_USE_POLLING=true`, e verificar que `npm run build` continua passando

## 2. Container do frontend

- [x] 2.1 Criar `frontend/Dockerfile` (base `node:26-trixie-slim`, `npm install -g npm@12`, `npm ci`, `EXPOSE 5173`, comando `npm install` + `npm run dev -- --host 0.0.0.0 --port 5173 --strictPort`) e `frontend/.dockerignore` (`node_modules`, `dist`), e verificar que `docker build frontend` conclui e que `docker run --rm <imagem> sh -c 'node --version && npm --version'` mostra 26.x e 12.x

## 3. Ambiente Docker (compose, Makefile, variáveis)

- [x] 3.1 Adicionar o serviço `frontend` ao `docker-compose.yml` (`build: ./frontend`, `profiles: ["${PROFILE_FRONTEND:-local}"]`, porta `5173:5173`, bind `./frontend:/app`, volume nomeado `frontend-node-modules:/app/node_modules`, `VITE_PRIMEUI_LICENSE: ${VITE_PRIMEUI_LICENSE:-}` e `VITE_USE_POLLING: "true"`), declarar o volume e adicionar `frontend/` ao `.dockerignore` da raiz. Verificar que `COMPOSE_PROFILES=local docker compose config --services` lista `db` e `frontend` e que, com `PROFILE_FRONTEND=desativado`, lista só `db`
- [x] 3.2 Atualizar o `Makefile`: `PROFILE_FRONTEND ?= local`, `down` de volta para `docker compose --profile "*" down` (mantendo `COMPOSE` e `export`) e o comentário de uso citando `PROFILE_FRONTEND`. Verificar com `make -n up`, `make -n down` e `make help`
- [x] 3.3 Adicionar ao `.env.example` `PROFILE_FRONTEND=local` e `VITE_PRIMEUI_LICENSE=` (vazio), cada um com um comentário curto, e verificar que `docker compose --env-file .env.example --profile local config --services` lista `db` e `frontend`
- [x] 3.4 Atualizar o `README.md`: nova seção do frontend (como subir, URL `http://localhost:5173`, chave de licença PrimeUI no `.env`, hot reload), a variável `PROFILE_FRONTEND` na seção do ambiente Docker e o aviso de que `make up` agora sobe também o frontend (`PROFILE_FRONTEND=desativado` para só o banco). Verificar que os comandos documentados rodam como escritos

## 4. Verificação integrada

- [x] 4.1 Com o Docker no WSL: `make up` sobe `db` e `frontend` (sem `app`), `curl -s http://localhost:5173` responde e a página renderizada mostra "Seja bem-vindo"; alterar o texto em `frontend/src` reflete sem rebuild (conferir no log do Vite ou via curl no módulo alterado) e depois desfazer; `node --version`/`npm --version` no container mostram 26.x/12.x; `make up PROFILE_FRONTEND=desativado` após `make down` sobe só `db`; `make down PROFILE_FRONTEND=desativado` com o frontend rodando também o derruba; o volume do Postgres é preservado
- [x] 4.2 Rodar `openspec validate add-vue-frontend --strict` sem erros e deixar o ambiente como estava antes da verificação (containers e `.env`)
