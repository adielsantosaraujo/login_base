# Spec Delta

## ADDED Requirements

### Requirement: Build de produção integrado ao backend
O comando `make build_front` (script `scripts/build_front.py`) SHALL gerar o build de produção do frontend em container (`docker compose run --rm --build frontend-build`), copiar os assets de `frontend/dist` (exceto `index.html`) para `src/main/resources/static/app/` e o `index.html` para `src/main/resources/templates/sistema/seguro/index.html`. O build de produção MUST usar `base` `/app/` (dev server continua em `/`). Os arquivos gerados MUST NOT ser versionados.

#### Scenario: Build bem-sucedido
- **WHEN** `make build_front` é executado e o build termina sem erro
- **THEN** `static/app/` contém apenas os assets do build atual (conteúdo anterior removido)
- **AND** o template recebe o `index.html` novo, com referências a `/app/assets/...`

#### Scenario: Build com erro
- **WHEN** o build do Vite ou a checagem de tipos falha
- **THEN** o script termina com código de saída diferente de zero
- **AND** nada é copiado para o backend

#### Scenario: Log do build
- **WHEN** o script roda
- **THEN** a saída aparece no terminal
- **AND** é gravada em `build.log` na raiz

### Requirement: Rotas da SPA servidas pelo backend
O backend SHALL responder às rotas do history (`/`, `/fazenda`, `/forja`, `/quartel`, `/masmorras`, `/batalhas/{id}`) com a view da SPA para usuários autenticados, permitindo recarregar a página ou abrir link direto.

#### Scenario: Recarregar rota interna
- **WHEN** um usuário autenticado acessa `GET /fazenda` diretamente
- **THEN** a resposta é HTTP 200 com a view `sistema/seguro/index`

#### Scenario: Rota interna sem autenticação
- **WHEN** um visitante não autenticado acessa `GET /forja`
- **THEN** é redirecionado para `/login`

### Requirement: Assets do frontend públicos
Os recursos em `/app/**` SHALL ser acessíveis sem autenticação.

#### Scenario: Asset sem sessão
- **WHEN** um visitante não autenticado requisita `/app/assets/<arquivo>`
- **THEN** a requisição não é redirecionada para `/login`
