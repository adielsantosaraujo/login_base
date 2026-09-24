# Proposal

## Why

O `login_base` tem hoje só o backend Spring Boot (com Thymeleaf) e ainda não tem um frontend moderno para construir as telas. Queremos começar um frontend em Vue 3 com a biblioteca de componentes PrimeVue 5, desenvolvido inteiramente em Docker como o resto do ambiente, para que ninguém precise de Node instalado na máquina.

## What Changes

- Novo projeto frontend na pasta `frontend/`: Vue 3 + TypeScript + Vite + PrimeVue 5 (tema Aura), configurado conforme o guia de instalação com Vite do MCP do PrimeVue (plugin `primevue@primeui`).
- Página inicial única com o texto **"Seja bem-vindo"**.
- Chave de licença PrimeUI lida da variável `VITE_PRIMEUI_LICENSE` (definida no `.env`, não versionado).
- `frontend/Dockerfile` baseado em Node 26 com npm 12, rodando o servidor de desenvolvimento do Vite (porta 5173) com hot reload sobre o código montado do host.
- Novo serviço `frontend` no `docker-compose.yml`, com profile controlado pela nova variável `PROFILE_FRONTEND` (padrão `local`).
- `Makefile` e `.env.example` passam a considerar `PROFILE_FRONTEND` (e `VITE_PRIMEUI_LICENSE` no exemplo).
- Correção: `make down` volta a derrubar os serviços de todos os profiles (`--profile "*"`). Uma edição posterior ao arquivamento da change anterior tinha passado a derrubar só o profile ativo, contrariando a spec `docker-dev-environment`. A variável `COMPOSE` e o `export` adicionados nessa edição são mantidos.
- `.dockerignore` da raiz passa a ignorar `frontend/`, para o build da imagem do backend não enviar o frontend.
- README atualizado.
- **BREAKING** (comportamento padrão): `make up` sem variáveis passa a subir `db` **e** `frontend`, não mais só o `db`.

## Capabilities

### New Capabilities
- `frontend-app`: o projeto frontend Vue 3 + PrimeVue 5 (estrutura, página inicial, configuração do PrimeVue e licença) e sua execução em container de desenvolvimento.

### Modified Capabilities
- `docker-dev-environment`: novo serviço `frontend` com profile `PROFILE_FRONTEND`; padrões de `make up`, `make down` e `.env.example` passam a incluir o frontend.

## Impact

- Novos arquivos: `frontend/**` (projeto Vite, `Dockerfile`, `.dockerignore`).
- Alterados: `docker-compose.yml`, `Makefile`, `.env.example`, `.dockerignore` (raiz), `README.md`.
- Dependências npm novas (só no frontend): `vue`, `primevue`, `@primeuix/themes`, `vite`, `@vitejs/plugin-vue`, `typescript`, `vue-tsc`.
- Imagem Docker nova: `node:26` (com npm atualizado para 12.x no build). A imagem oficial do Node 26 vem com npm 11.
- Porta 5173 publicada no host.
- Backend Java sem alterações. Integração do frontend com a API fica para changes futuras.
