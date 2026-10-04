---
titulo: Resolver problemas comuns
publico: operacao
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - Makefile
  - docker-compose.yml
  - src/main/resources/application.properties
  - .env.example
  - frontend/vite.config.ts
  - scripts/build_front.py
---

# Resolver problemas comuns

## Quando usar

Consulte este guia quando encontrar erros ao subir o ambiente, conectar ao banco, rodar a aplicação ou acessar o frontend.

## Problemas e soluções

| Sintoma | Causa | Solução |
|---|---|---|
| `docker compose: no service selected` ao executar `make up` ou `docker compose up -d` | Nenhum profile foi ativado. Os serviços `db`, `app` e `frontend` dependem de profiles que precisam ser explicitamente ativados. | Use `make up` (que lê `PROFILE` do `.env`) ou `docker compose --profile local up -d`. Consulte [`docs/operacao/guias/subir-e-derrubar-o-ambiente-docker.md`](subir-e-derrubar-o-ambiente-docker.md). |
| Erro do Flyway na inicialização (ex.: schema inconsistente ou migração falha) | O volume do banco contém tabelas criadas por uma versão antiga ou falha anterior. O Flyway não consegue reaplicar a migração. | Execute `make down_v` para apagar o volume e recrie o banco. Ver [`docs/operacao/guias/recriar-o-banco-de-dados.md`](recriar-o-banco-de-dados.md). |
| `Hibernate validation failed` na inicialização | O schema não corresponde ao esperado (ddl-auto=validate falha). | Verifique os logs completos da aplicação (console IntelliJ, `mvnw` ou `docker compose logs app`). Se as migrações não rodaram, recreie o banco (ver acima). |
| Aviso de "administrador inicial não será criado" nos logs | A variável `ADMIN_PASSWORD` está vazia ou não foi definida. | Edite `.env` e preencha `ADMIN_PASSWORD` com uma senha; reinicie a aplicação. Ver [`docs/operacao/guias/configurar-o-administrador-inicial.md`](configurar-o-administrador-inicial.md). |
| Admin não foi criado, mas `ADMIN_PASSWORD` está preenchido | O e-mail já existe no banco (admin anterior). | O runner verifica se o e-mail existe e não sobrescreve. Altere `ADMIN_EMAIL` para um valor novo ou apague o usuário do banco. |
| Erro de licença do PrimeVue (`[PrimeUI]: License key not provided...`) no console do navegador | `VITE_PRIMEUI_LICENSE` não está definido ou é inválido. | Deixe vazio para usar a versão community (apenas aviso no console) ou configure uma chave válida em `.env`. Ver [`docs/operacao/referencia/variaveis-de-ambiente.md`](../referencia/variaveis-de-ambiente.md). |
| `node_modules` defasado, dependências não resolvem | O volume `frontend-node-modules` ficou dessincronizado com `package.json` após trocar de branch ou atualizar dependências. | Execute `make down`, depois `docker volume rm login_base_frontend-node-modules` e reinicie o container `frontend` (`make up PROFILE_FRONTEND=local`). |
| Erro 500 em `/app/index` com mensagem sobre template ausente | O arquivo `src/main/resources/templates/sistema/seguro/app/index.html` não foi gerado. Acontece quando a aplicação sobe antes de `make build_front` rodar. | Execute `make build_front` e reconstrua a imagem (`docker compose --profile local build app`, depois `make up PROFILE_APP=local`, ou em uma chamada: `PROFILE_APP=local docker compose --profile local up -d --build`). O `restart` não resolve: o template fica dentro da imagem Docker. |
| Porta 80 não responde na aplicação (`app` container) | O mapeamento de porta 80:80 não funciona conforme esperado. A aplicação escuta em 8080 interno e a exposição não está correta. | Verifique qual porta a aplicação realmente escuta: `docker compose logs app` procure por `Tomcat initialized with port`. Use essa porta ou configure `SERVER_PORT` no compose. Ver problema conhecido em [`docs/operacao/guias/executar-a-aplicacao-em-container.md`](executar-a-aplicacao-em-container.md). |
| Frontend no container não consegue conectar ao backend | O proxy do Vite não está apontando para o backend correto. | O `docker-compose.yml` passa `BACKEND_URL`, mas Vite lê `VITE_BACKEND_URL` (prefixo obrigatório). Dentro do container, o proxy aponta para `http://localhost:8080` do próprio container. Se o backend está em outro endereço, ajuste o `.env` ou configure `VITE_BACKEND_URL` no `docker-compose.yml`. Ver [`docs/operacao/referencia/variaveis-de-ambiente.md`](../referencia/variaveis-de-ambiente.md) (problema conhecido). |
| Container do frontend não inicia; erro `npm ERR!` | Erro no build das dependências ou compilação do Vite. | Veja os logs: `docker compose logs -f frontend`. Procure pela mensagem de erro específica; pode ser dependência faltante, versão incompatível ou erro de sintaxe no código. |
| Porta 5173 (frontend) não responde | O container não iniciou ou a porta está em uso. | Verifique `docker compose ps` e veja o status. Se `Up`, teste com `curl http://localhost:5173`. Se porta está em uso, configure `ports` diferente no compose ou mate o outro processo. |

## Mais recursos

Consulte estes documentos para soluções específicas:

- Variáveis de ambiente: [`docs/operacao/referencia/variaveis-de-ambiente.md`](../referencia/variaveis-de-ambiente.md)
- Serviços Docker Compose: [`docs/operacao/referencia/servicos-docker-compose.md`](../referencia/servicos-docker-compose.md)
- Subir/derrubar ambiente: [`docs/operacao/guias/subir-e-derrubar-o-ambiente-docker.md`](subir-e-derrubar-o-ambiente-docker.md)
- Configurar admin: [`docs/operacao/guias/configurar-o-administrador-inicial.md`](configurar-o-administrador-inicial.md)

## Veja também

- [`docs/desenvolvimento/guias/desenvolver-o-frontend.md`](../../desenvolvimento/guias/desenvolver-o-frontend.md) — configuração e desenvolvimento local do frontend.
