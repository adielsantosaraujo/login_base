---
titulo: Executar a aplicação em container
publico: operacao
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - Dockerfile
  - docker-compose.yml
  - src/main/resources/application.properties
  - .dockerignore
  - scripts/build_front.py
  - src/main/java/com/example/loginbase/web/PaginaController.java
---

# Executar a aplicação em container

## Quando usar

Este guia descreve como subir a aplicação Spring Boot (serviço `app`) dentro de um container Docker, integrada com o banco de dados e opcionalmente com o frontend.

## Pré-requisitos

- Docker e Docker Compose funcionando no WSL.
- Arquivo `.env` configurado na raiz do projeto.
- Frontend já foi construído com `make build_front` (para que o `index.html` gerado e os assets estejam presentes).
- Terminal WSL.

## Passos

1. Construa o frontend (se ainda não fez):

   ```bash
   make build_front
   ```

   Isto descobre os apps em `frontend/public/<nome>/` e `frontend/seguro/<nome>/`, roda o build de cada um, e copia os assets para `src/main/resources/static/<nome>/` e o HTML para `src/main/resources/templates/sistema/<area>/<nome>/index.html`. Sem este passo, as rotas `/<nome>/index` falharão com erro de template ausente.

2. Configure o profile do serviço `app` no `.env`:

   ```bash
   PROFILE_APP=local
   ```

   Ou passe na linha de comando:

   ```bash
   make up PROFILE_APP=local
   ```

3. Se essa for a primeira construção ou o Dockerfile mudou, force o rebuild:

   ```bash
   docker compose --profile local build app
   make up PROFILE_APP=local
   ```

   Ou em uma única chamada:

   ```bash
   PROFILE_APP=local docker compose --profile local up -d --build
   ```

4. Suba o ambiente:

   ```bash
   make up PROFILE_APP=local
   ```

   Isto inicia o banco (`db`), a aplicação (`app`) e o frontend (se `PROFILE_FRONTEND=local`, que é o padrão).

5. Acompanhe os logs da aplicação:

   ```bash
   docker compose logs -f app
   ```

   Procure por mensagens como:
   - `AdminInicialRunner`: confirmação de que o admin foi criado ou já existe.
   - `SessoesAbertasRunner`: confirmação de fechamento de sessões abertas.
   - `Started LoginBaseApplication in ...` — aplicação pronta.

## Como verificar

- A aplicação deve estar escutando (a porta real depende da configuração; ver seção "Problemas conhecidos" abaixo):

  ```bash
  docker compose ps
  ```

  Verifique que `app` tem status `Up`.

- Tente acessar a aplicação via navegador (p. ex., `http://localhost/cadastro_usuario/index` para o app público ou `http://localhost/patrimonio/index` para o app seguro; porém há inconsistência de porta — ver aviso abaixo).

- Ou faça uma requisição simples:

  ```bash
  curl http://localhost/login
  ```

## Problemas conhecidos

> **Problema conhecido:** O `docker-compose.yml` mapeia a porta `80:80` e o `Dockerfile` faz `EXPOSE 80`. Porém, em `application.properties`, `server.port=${SERVER_PORT:8080}`, e o compose não repassa `SERVER_PORT` ao container. O arquivo `.env` também é excluído da imagem por `.dockerignore`. Resultado: o container provavelmente escuta em `8080` internamente, e o mapeamento `80:80` não funciona como pretendido. (fonte: docker-compose.yml, Dockerfile, application.properties, .dockerignore)

> **A confirmar com o responsável:** Qual deve ser a porta exposta e repassada? Deve-se adicionar `SERVER_PORT` ao `docker-compose.yml` ou ajustar o mapeamento de portas?

> **Nota:** Com múltiplos apps frontend (público e seguro), as rotas mudam de `/app/index` para `/<nome>/index`. Exemplos: `/cadastro_usuario/index` (público, sem login) e `/patrimonio/index` (seguro, exige login).

## Quando reconstruir a imagem

A imagem Docker da aplicação é construída automaticamente na primeira execução de `make up` se não existir. Porém, deve-se **reconstruí-la** se:

- O `Dockerfile` mudou.
- O código-fonte mudou (alterações em `src/`).
- O `make build_front` foi executado (os assets foram atualizados em `src/main/resources/`).
- Uma dependência foi adicionada ou atualizada (`pom.xml`).

Para reconstruir explicitamente:

```bash
docker compose --profile local build app
make up PROFILE_APP=local
```

ou em uma única chamada:

```bash
PROFILE_APP=local docker compose --profile local up -d --build
```

## Veja também

- [`docs/operacao/guias/subir-e-derrubar-o-ambiente-docker.md`](subir-e-derrubar-o-ambiente-docker.md) — como gerenciar containers.
- [`docs/operacao/guias/configurar-o-administrador-inicial.md`](configurar-o-administrador-inicial.md) — como definir credenciais iniciais.
- [`docs/desenvolvimento/guias/gerar-o-build-de-producao.md`](../../desenvolvimento/guias/gerar-o-build-de-producao.md) — detalhes sobre `make build_front`.
