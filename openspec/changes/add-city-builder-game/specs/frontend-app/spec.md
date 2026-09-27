# Spec Delta

## REMOVED Requirements

### Requirement: Página inicial de boas-vindas

**Reason**: A página inicial agora exibe o jogo (tela da vila do usuário autenticado), não mais apenas uma mensagem de boas-vindas.

**Migration**: Substituir por "Página inicial do jogo" na seção ADDED.

## ADDED Requirements

### Requirement: Página inicial do jogo
A aplicação SHALL exibir na rota raiz (`/`) a tela da vila do usuário autenticado — a qual mostra recursos, prédios, ordens, canteiros, sementes, itens, unidades e batalhas. A página é renderizada pelo Vue usando dados obtidos do backend via API REST.

#### Scenario: Usuário autenticado acessa a página inicial
- **WHEN** um usuário autenticado abre `http://localhost:5173/`
- **THEN** a página exibe a tela da vila com recursos, prédios e demais elementos do jogo

#### Scenario: Acesso anônimo redireciona para login
- **WHEN** um visitante não autenticado acessa `http://localhost:5173/`
- **THEN** a API retorna 401 e o navegador é redirecionado para `/login`

### Requirement: Proxy de desenvolvimento para o backend
O servidor de desenvolvimento do Vite MUST encaminhar requisições a endpoints do backend para a URL configurada em `BACKEND_URL`, preservando o `Host` original da requisição para que os redirects do Spring Security retornem para o domínio do Vite. Caminhos cobertos: `/api`, `/login`, `/logout`, `/css`, `/js`, `/images`. O valor padrão de `BACKEND_URL` é `http://localhost:8080` para desenvolvimento local; em container, usa-se `http://host.docker.internal:8080`. O proxy MUST permitir que a SPA compartilhe cookies de sessão HTTP com o backend.

#### Scenario: Proxy de API
- **WHEN** a SPA em `http://localhost:5173` chama `fetch('/api/jogo/vila')`
- **THEN** a requisição é encaminhada para `http://localhost:8080/api/jogo/vila` com o cookie de sessão intacto
- **AND** a resposta volta para a SPA

#### Scenario: Proxy de login
- **WHEN** a SPA precisa autenticar e redireciona para `/login`
- **THEN** a página Thymeleaf do backend é entregue via proxy

#### Scenario: Proxy em container
- **WHEN** o serviço `frontend` está rodando em container com `BACKEND_URL=http://host.docker.internal:8080`
- **THEN** requisições são encaminhadas para o backend rodando no host

### Requirement: Roteamento no modo history
A aplicação MUST usar `vue-router` em modo history (URLs sem `#`), roteando internamente para as telas do jogo sem recarregar a página. As rotas principais são `/` (vila), `/fazenda` (fazenda), `/forja` (forja), `/quartel` (quartel), `/masmorras` (masmorras), `/batalhas/:id` (batalha tática).

#### Scenario: Navegação entre rotas
- **WHEN** o usuário clica em "Forja" no menu da aplicação
- **THEN** a URL muda para `/forja` e a tela da forja é exibida sem recarregar a página

#### Scenario: URL history mode
- **WHEN** o usuário navega para `/masmorras` manualmente
- **THEN** a aplicação carrega a tela de masmorras (sem `#` na URL)
