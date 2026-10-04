---
titulo: Múltiplos frontends em public/seguro
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-10-04
atualizado_em: 2026-10-04
fontes:
  - scripts/build_front.py
  - scripts/apps_front.py
  - scripts/limpar_front.py
  - frontend/seguro/patrimonio/vite.config.ts
  - frontend/public/cadastro_usuario/vite.config.ts
  - src/main/java/com/example/loginbase/web/PaginaController.java
  - src/main/java/com/example/loginbase/seguranca/SecurityConfig.java
  - docker-compose.yml
  - .env.example
---

# 0014 — Múltiplos frontends em public/seguro

**Status:** Aceita · **Data:** 2026-10-04

## Contexto

A ADR 0011 estabeleceu uma única SPA integrada ao backend, servida em `/app/`. Conforme o projeto evolui, surgem novos frontends com públicos diferentes: uma SPA pública (cadastro de usuário, sem autenticação) e uma SPA segura (patrimônio, com autenticação obrigatória). Manter uma única estrutura `frontend/` prejudica a clareza dos escopos e complica o build.

É necessário organizar múltiplos frontends no mesmo repositório, cada um em seu próprio caminho (`/<nome>/`) e com seu próprio build, mantendo a integração com o backend.

## Decisão

Adotar estrutura de múltiplos frontends organizados por área:

1. **Estrutura de pastas:** Cada app Vue/Vite fica em `frontend/public/<nome>/` (público, sem autenticação) ou `frontend/seguro/<nome>/` (seguro, exige login).
   - Exemplo: `frontend/public/cadastro_usuario/` (formulário de cadastro)
   - Exemplo: `frontend/seguro/patrimonio/` (aplicação de patrimônio)

2. **Descoberta automática:** Script `scripts/apps_front.py` escaneia `frontend/public/*/package.json` e `frontend/seguro/*/package.json`, ordena por área/nome, valida nomes (`^[a-z0-9_-]+$` sem pontos), e rejeita nomes reservados (`css`, `js`, `images`, `login`, `logout`, `api`, `error`, `v3`, `swagger-ui`, `app`).

3. **Build:** Script Python `scripts/build_front.py` (equivalente a `make build_front`):
   - Descobre todos os apps via `apps_front.py`.
   - Limpa artefatos anteriores: `frontend/dist/`, `src/main/resources/static/<nome>/`, `src/main/resources/templates/sistema/<area>/<nome>/`.
   - Para cada app, executa `docker compose run --rm --build frontend-build` com variáveis `FRONT_APP=<area>/<nome>` e `FRONT_APP_NOME=<nome>`.
   - Valida que `dist/index.html` foi gerado (evita cópias de builds antigos).
   - Copia assets (tudo exceto `index.html`) para `src/main/resources/static/<nome>/`.
   - Copia `index.html` para `src/main/resources/templates/sistema/<area>/<nome>/index.html`.
   - Suporta filtro: `python3 scripts/build_front.py cadastro_usuario` constrói só esse app.

4. **Cleanup:** Script `scripts/limpar_front.py` (`make limpar_front`):
   - Remove todo `src/main/resources/templates/sistema/public/*/` e `seguro/*/` (preserva `login.html` e `.gitignore` da raiz).
   - Remove todo `src/main/resources/static/<nome>/` (incluindo o legado `static/app/`).
   - Remove `frontend/dist/` e `build.log`.

5. **Servimento no Spring:**
   - `PaginaController` mapeia `/cadastro_usuario/**` → `sistema/public/cadastro_usuario/index`.
   - `PaginaController` mapeia `/patrimonio/**` → `sistema/seguro/patrimonio/index`.
   - Padrões de rota: até 5 níveis, último segmento sem extensão (ex.: `/patrimonio/index`, `/patrimonio/usuarios/123/editar`; não casa `/patrimonio/assets/style.css`).
   - `/<nome>` e `/<nome>/` redirecionam para `/<nome>/index`.

6. **Base URL no Vite:** Cada `vite.config.ts` define `base: /<nomeApp>/` em produção (lê `FRONT_APP_NOME` do processo, fallback para basename do diretório), e `base: /` em desenvolvimento.

7. **Segurança:**
   - `/cadastro_usuario/**` está em `permitAll()` (público).
   - `/patrimonio/**` exige autenticação; rotas de arquivo (com extensão) caem fora e são servidas como estáticos.
   - Ambas as rotas estão na lista `naoSalvar` do request cache, impedindo que reloads da SPA sequestrem o redirecionamento pós-login.

8. **Desenvolvimento:** Docker Compose via `FRONT_APP`:
   - `make up FRONT_APP=public/cadastro_usuario` inicia Vite em dev mode para esse app (default: `seguro/patrimonio`).
   - Proxy em `vite.config.ts` encaminha `/api`, `/login`, `/logout`, `/css` para o backend (porta 8080 ou conforme `VITE_BACKEND_URL`).
   - `changeOrigin: false` mantém origin como `localhost:5173` para que redirecionamento pós-login volte ao Vite.

9. **Página inicial:** Constante `PAGINA_INICIAL = "/patrimonio/index"` em `PaginaController` usada por:
   - Redirecionamento de `/` (raiz da aplicação).
   - Redirecionamento pós-login (configurado em `RegistroSessaoSuccessHandler`).

## Alternativas descartadas

- **Um único frontend com features flags:** Rejeitada porque aumenta complexidade; múltiplos frontends deixam claro que são dois escopos distintos.
- **Nginx com vários upstreams:** Rejeitada porque a integração no JAR simplifica deployment; continuamos com um único servidor.
- **Paths fixos por app (ex.: `/app1/`, `/app2/`):** Adotada a convenção `/<nome>/` derivada do nome da pasta, permitindo nomes temáticos (ex.: `/patrimonio/`, `/cadastro/`).

## Consequências

### Positivas

- **Separação clara de escopos:** público e seguro isolados fisicamente; fácil entender o que está em qual caminho.
- **Apps independentes:** Cada app pode ter suas dependências, versões de bibliotecas e build config distintas.
- **Escalável:** Adicionar um novo frontend é tão simples quanto criar `frontend/<area>/<nome>/` com seu `package.json`.
- **Descoberta automática:** Scripts leem a estrutura; não exigem configuração manual de lista de apps.
- **Filtragem de build:** `python3 scripts/build_front.py <nomes>` permite builds rápidos durante desenvolvimento.
- **Um único JAR:** Todos os apps entram no mesmo JAR do backend; deployment único.

### Negativas

- **Validação de nomes:** Nomes de apps são restritos a `^[a-z0-9_-]+$` e alguns nomes (como `app`) são proibidos para evitar colisões com estáticos ou rotas conhecidas.
- **Build mais lento em CI:** Todos os apps são built por `make build_front` (sem filtro); é possível otimizar com filtro em CI, mas requer conhecimento de quais apps mudaram.
- **Sincronização de migrations:** Se um novo app exigir migrações de banco, estas precisam ser adicionadas manualmente antes do build.
- **Complexidade em desenvolvimento:** Trabalhar em múltiplos apps simultâneos exige múltiplas instâncias do container frontend ou Vite em paralelo.

## Veja também

- [ADR 0011 — SPA servida pelo Spring em /app](./0011-spa-servida-pelo-spring-em-app.md) (substituída por esta ADR)
- [Frontend integrado ao backend](../frontend-integrado-ao-backend.md)
- [Gerar o build de produção](../../guias/gerar-o-build-de-producao.md)
