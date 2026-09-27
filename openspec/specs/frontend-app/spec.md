# frontend-app Specification

## Purpose

Define o frontend web do login_base: um projeto Vue 3 com PrimeVue 5 e Vite na pasta `frontend/`, como ele é configurado e como roda em container de desenvolvimento.

## Requirements

### Requirement: Projeto frontend Vue 3 com PrimeVue 5
O repositório SHALL conter na pasta `frontend/` um projeto Vite com Vue 3, TypeScript e PrimeVue 5, com o PrimeVue registrado como plugin da aplicação usando o tema Aura. O comando de build do projeto MUST terminar sem erros de tipo nem de compilação.

#### Scenario: Build do projeto
- **WHEN** o build do frontend é executado (`npm run build`) no container Node do projeto
- **THEN** o build termina com sucesso e gera os arquivos estáticos
- **AND** a checagem de tipos não reporta erros

### Requirement: Licença PrimeUI por variável de ambiente
A chave de licença do PrimeVue SHALL ser lida da variável de ambiente `VITE_PRIMEUI_LICENSE` e passada na configuração do plugin. A chave MUST NOT ficar escrita no código versionado. Quando a variável estiver ausente ou vazia, a aplicação MUST continuar carregando a página inicial.

#### Scenario: Chave definida no .env
- **WHEN** `VITE_PRIMEUI_LICENSE` está definida no `.env` da raiz e o serviço `frontend` é iniciado
- **THEN** a aplicação configura o PrimeVue com essa chave

#### Scenario: Chave ausente
- **WHEN** `VITE_PRIMEUI_LICENSE` não está definida
- **THEN** a página inicial é exibida mesmo assim (o PrimeVue pode mostrar um aviso de licença)

### Requirement: Container de desenvolvimento com Node 26 e npm 12
O frontend SHALL rodar em um container cuja imagem usa Node.js na versão 26.x e npm na versão 12.x, executando o servidor de desenvolvimento do Vite acessível no host pela porta 5173.

#### Scenario: Versões da imagem
- **WHEN** `node --version` e `npm --version` são executados no container do `frontend`
- **THEN** o Node reporta uma versão 26.x
- **AND** o npm reporta uma versão 12.x

#### Scenario: Acesso pelo host
- **WHEN** o serviço `frontend` está rodando
- **THEN** a página inicial responde em `http://localhost:5173`

### Requirement: Recarga automática ao editar o código
O container de desenvolvimento SHALL usar o código de `frontend/` montado a partir do host, de forma que alterações salvas nos arquivos do projeto apareçam no navegador sem reconstruir a imagem nem reiniciar o container, inclusive com o projeto em um drive Windows acessado pelo WSL.

#### Scenario: Editar o texto da página
- **WHEN** o desenvolvedor altera e salva um componente Vue em `frontend/src` com o serviço `frontend` rodando
- **THEN** o servidor do Vite detecta a alteração e o navegador passa a mostrar o conteúdo novo sem reconstruir a imagem

### Requirement: Página inicial do jogo
A aplicação SHALL exibir na rota raiz (`/`) a tela da vila do usuário autenticado — a qual mostra recursos, prédios, ordens, canteiros, sementes, itens, unidades e batalhas. A página é renderizada pelo Vue usando dados obtidos do backend via API REST.

#### Scenario: Usuário autenticado acessa a página inicial
- **WHEN** um usuário autenticado abre `http://localhost:5173/`
- **THEN** a página exibe a tela da vila com recursos, prédios e demais elementos do jogo

#### Scenario: Acesso anônimo redireciona para login
- **WHEN** um visitante não autenticado acessa `http://localhost:5173/`
- **THEN** a API retorna 401 e o navegador é redirecionado para `/login`

### Requirement: Proxy de desenvolvimento para o backend
O servidor de desenvolvimento do Vite MUST encaminhar requisições a endpoints do backend para a URL configurada em `BACKEND_URL`, preservando o `Host` original da requisição para que os redirects do Spring Security retornem para o domínio do Vite. Caminhos cobertos: `/api`, `/login`, `/logout`, `/css`, `/js`, `/images`. O valor padrão de `BACKEND_URL` é `http://localhost` para desenvolvimento local; em container, usa-se `http://host.docker.internal`. O proxy MUST permitir que a SPA compartilhe cookies de sessão HTTP com o backend.

#### Scenario: Proxy de API
- **WHEN** a SPA em `http://localhost:5173` chama `fetch('/api/jogo/vila')`
- **THEN** a requisição é encaminhada para `http://localhost/api/jogo/vila` com o cookie de sessão intacto
- **AND** a resposta volta para a SPA

#### Scenario: Proxy de login
- **WHEN** a SPA precisa autenticar e redireciona para `/login`
- **THEN** a página Thymeleaf do backend é entregue via proxy

#### Scenario: Proxy em container
- **WHEN** o serviço `frontend` está rodando em container com `BACKEND_URL=http://host.docker.internal`
- **THEN** requisições são encaminhadas para o backend rodando no host

### Requirement: Roteamento no modo history
A aplicação MUST usar `vue-router` em modo history (URLs sem `#`), roteando internamente para as telas do jogo sem recarregar a página. As rotas principais são `/` (vila), `/fazenda` (fazenda), `/forja` (forja), `/quartel` (quartel), `/masmorras` (masmorras), `/batalhas/:id` (batalha tática).

#### Scenario: Navegação entre rotas
- **WHEN** o usuário clica em "Forja" no menu da aplicação
- **THEN** a URL muda para `/forja` e a tela da forja é exibida sem recarregar a página

#### Scenario: URL history mode
- **WHEN** o usuário navega para `/masmorras` manualmente
- **THEN** a aplicação carrega a tela de masmorras (sem `#` na URL)
