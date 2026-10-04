---
titulo: Desenvolver o frontend
publico: desenvolvimento
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - frontend/seguro/patrimonio/vite.config.ts
  - frontend/seguro/patrimonio/src/main.ts
  - frontend/seguro/patrimonio/src/router/index.ts
  - frontend/seguro/patrimonio/Dockerfile
  - docker-compose.yml
  - frontend/seguro/patrimonio/package.json
  - README.md
---

# Desenvolver o frontend

## Quando usar

Use este guia quando quiser rodar o dev server Vite em `http://localhost:5173` com hot reload automático, ou quando precisar configurar a conexão com o backend.

## Pré-requisitos

- Docker Desktop com integração WSL2 habilitada.
- Backend Spring Boot rodando em `http://localhost:8080` (veja [Primeiros passos](../tutoriais/primeiros-passos.md)).
- Arquivo `.env` configurado na raiz do projeto.

## Passos

1. **Inicie o serviço frontend (dev server).**

   Na raiz do projeto (WSL), execute:

   ```bash
   docker compose --profile local up -d frontend
   ```

   Isso levanta o container `frontend` que roda o dev server Vite. A saída será:

   ```
   [+] Running 1/1
    ⠿ frontend Started
   ```

   Você pode acompanhar os logs em tempo real:

   ```bash
   make logs_front
   # ou
   docker compose logs -f frontend
   ```

   Quando o servidor estiver pronto, você verá:

   ```
   VITE v8.3.x  ready in 456 ms

   ➜  Local:   http://localhost:5173/
   ```

2. **Acesse a aplicação.**

   Abra seu navegador:

   ```
   http://localhost:5173/
   ```

   O frontend carregará. Na raiz (`/`), o Vite serve a SPA diretamente, sem passar pelo proxy do backend. Para acessar a tela de login do backend, use as rotas de API (`/api/**`, `/login`, `/logout`) que são proxiadas para o backend em 8080.

3. **Edite os arquivos do frontend.**

   Qualquer alteração em `frontend/src/**/*` (Vue components, TypeScript, CSS) dispara um rebuild automático e atualiza o navegador instantaneamente (hot reload).

   Exemplos de arquivos para editar:

   - `frontend/seguro/patrimonio/src/views/HomeView.vue` — view principal da SPA.
   - `frontend/seguro/patrimonio/src/router/index.ts` — definição das rotas.
   - `frontend/seguro/patrimonio/src/main.ts` — setup global (PrimeVue, etc).

4. **Verifique a conexão com o backend.**

   O dev server proxy está configurado para encaminhar requisições para:

   ```
   /api/**  → http://localhost:8080/api/
   /login   → http://localhost:8080/login
   /logout  → http://localhost:8080/logout
   /css/**  → http://localhost:8080/css/
   ```

   Dentro do container Docker, o padrão `localhost:8080` pode apontar para o próprio container, não para o host. Neste caso, requisições para `/api/**` falham (problema conhecido). Para requisições funcionarem dentro do container, defina `VITE_BACKEND_URL=http://host.docker.internal:8080` no `.env`.

   > **Nota sobre changeOrigin:** a configuração `changeOrigin: false` preserva o Host original (5173), para que redirecionamentos pós-login retornem a 5173. Isso é importante para manter a sessão no frontend dev e evitar CORS.

## Configuração avançada

### Variáveis de ambiente do frontend

O container recebe as variáveis que o `docker-compose.yml` repassa. Note que editar o `.env` não atualiza o container automaticamente — é preciso recriar o container com `docker compose --profile local up -d frontend`:

- **`VITE_BACKEND_URL`**  
  URL do backend para proxy. O `docker-compose.yml` não a define; o proxy internamente aponta para o próprio container (problema conhecido). Para alterar, redefina no `.env` e recrie com `docker compose --profile local up -d frontend`.

- **`VITE_USE_POLLING`**  
  O `docker-compose.yml` fixa este valor como `"true"`. Editar o `.env` não muda nada no container; seria preciso alterar `docker-compose.yml` e reconstruir.

- **`VITE_PRIMEUI_LICENSE`** (padrão: vazio)  
  Chave de licença do PrimeUI (community ou comercial). Vazio funciona com um aviso no console.

### Teste de requisições para a API

No console do navegador, teste uma chamada para o backend:

```javascript
fetch('/api/seu-endpoint').then(r => r.json()).then(console.log)
```

Você deve receber uma resposta sem erro de CORS.

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| `VITE dev server connection timed out` ou conecta mas não vê mudanças | Polling desabilitado em DrvFs (ou arquivo não é detectado em watch) | Use `docker compose --profile local up -d frontend` para recriar o container e reativar o polling. |
| Requisições para `/api` dão 404 ou CORS error | Backend não está rodando ou proxy está desconfigurado | Confira se `http://localhost:8080/api/seu-endpoint` funciona no browser. Reinicie o frontend. |
| `aviso de licença PrimeUI` no console | Esperado quando `VITE_PRIMEUI_LICENSE` está vazio | Ignorar, ou definir uma chave de licença no `.env`. |
| Frontend não carrega após login | Host mismatch (proxy com changeOrigin incorreto) | Verifique em `frontend/seguro/patrimonio/vite.config.ts` que `changeOrigin: false` está definido. |
| `node_modules` defasado, falta de dependências | Dependências não foram instaladas ou estão desatualizadas | Rode `docker compose --profile local run --rm --build frontend npm install` e reinicie. |

## Parar o dev server

Para parar o frontend sem remover seu container:

```bash
docker compose stop frontend
```

Para parar e remover:

```bash
docker compose --profile local down
```

Para continuar depois:

```bash
docker compose --profile local up -d frontend
```

## Como verificar

Se o dev server estiver pronto:

- ✓ Você acessa `http://localhost:5173/` sem erro de conexão.
- ✓ O frontend carrega e você vê a página Home renderizada.
- ✓ Editar um arquivo `.vue` reflete no navegador em menos de 1 segundo (hot reload).
- ✓ Logs do docker mostram build bem-sucedido: `✓ built in <tempo> ms`.

## Veja também

- [Primeiros passos](../tutoriais/primeiros-passos.md) — setup completo do projeto.
- [Gerar o build de produção](./gerar-o-build-de-producao.md) — compilar para o deployment.
- [Executar os testes](./executar-os-testes.md) — rodar testes do frontend.
