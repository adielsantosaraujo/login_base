# Tasks

## 1. Infraestrutura do build

- [x] 1.1 [Serviço frontend-build e base do Vite](tasks/1.1-servico-frontend-build.md) — verificação: `docker compose config --profiles` lista `build`; `docker compose run --rm --build frontend-build` gera `frontend/dist/index.html`
- [x] 1.2 [Script scripts/build_front.py](tasks/1.2-script-build-front.md) — verificação: `python3 -m py_compile scripts/build_front.py`; sem syntax errors
- [x] 1.3 [Alvo make build_front e .gitignore](tasks/1.3-alvo-make-e-gitignore.md) — verificação: `make help` mostra `build_front`; `git check-ignore` confirma os 3 caminhos

## 2. Backend servindo a SPA

- [x] 2.1 [Rotas da SPA, /app/** público e testes](tasks/2.1-rotas-spa-e-testes.md) — verificação: `./mvnw test` passa; `GET /` autenticado → 200 + view; `/app/assets/**` anônimo não redireciona

## 3. Documentação

- [x] 3.1 [Atualizar guia e implantação](tasks/3.1-atualizar-docs.md) — verificação: docs/09 e 10 descrevem build_front; marcadores "previsto" removidos em todos os docs; links válidos; ordem de execução documentada

## 4. Verificação final

- [x] 4.1 [Build ponta a ponta](tasks/4.1-verificacao-ponta-a-ponta.md) — verificação: `make build_front` executa; `git status` não mostra gerados; `./mvnw test` passa
