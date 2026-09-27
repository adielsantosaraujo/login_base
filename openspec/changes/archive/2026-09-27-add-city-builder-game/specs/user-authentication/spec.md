# Spec Delta

## MODIFIED Requirements

### Requirement: Proteção de rotas
Toda rota da aplicação SHALL exigir autenticação, exceto a página de login, o processamento do login e os recursos estáticos públicos (CSS, JS, imagens). Um visitante não autenticado que acessa uma rota protegida MUST ser redirecionado para `/login`. Requisições anônimas a endpoints de API (`/api/**`) MUST receber HTTP 401 sem redirecionamento, e MUST NOT ser guardadas como página de retorno do login — após autenticação, o usuário é redirecionado para `/` (página inicial), não para um endpoint de API.

#### Scenario: Acesso anônimo à página inicial
- **WHEN** um visitante não autenticado acessa `GET /`
- **THEN** ele é redirecionado para `/login`

#### Scenario: Recurso estático público
- **WHEN** um visitante não autenticado requisita um arquivo em `/css/`
- **THEN** o arquivo é servido sem redirecionamento para o login

#### Scenario: Acesso anônimo a endpoint de API
- **WHEN** um visitante não autenticado acessa `GET /api/jogo/vila`
- **THEN** a resposta é HTTP 401 sem redirecionamento

#### Scenario: Retorno à página inicial após login de acesso a API
- **WHEN** um visitante anônimo tenta acessar um endpoint de API, é negado com 401, faz login e se autentica com sucesso
- **THEN** ele é redirecionado para `/` (página inicial), não para a API

## ADDED Requirements

### Requirement: Proteção CSRF para clientes JavaScript
A aplicação MUST proteger requisições POST, PUT, DELETE a endpoints de API (`/api/**`) contra ataques CSRF por meio de um token enviado por JavaScript. O token MUST ser disponibilizado em um cookie `XSRF-TOKEN` (legível por JavaScript, não `HttpOnly`) e aceito no cabeçalho `X-XSRF-TOKEN` de qualquer requisição autenticada. O formulário de login Thymeleaf continua usando o parâmetro oculto `_csrf` como antes. Requisições autenticadas sem o token válido MUST ser rejeitadas com HTTP 403.

#### Scenario: Token CSRF em cookie
- **WHEN** um navegador carrega a página raiz (SPA)
- **THEN** a resposta inclui um cookie `XSRF-TOKEN` legível por JavaScript

#### Scenario: Requisição POST autenticada sem token
- **WHEN** um usuário autenticado envia `POST /api/jogo/predios/SERRARIA/melhorar` sem o cabeçalho `X-XSRF-TOKEN`
- **THEN** a resposta é HTTP 403

#### Scenario: Requisição POST autenticada com token válido
- **WHEN** um usuário autenticado envia `POST /api/jogo/predios/SERRARIA/melhorar` com o cabeçalho `X-XSRF-TOKEN` igual ao valor do cookie
- **THEN** a requisição é processada normalmente

#### Scenario: Formulário de login com _csrf
- **WHEN** um visitante anônimo envia `POST /login` com o parâmetro `_csrf` (formulário Thymeleaf)
- **THEN** a autenticação é bem-sucedida (proteção CSRF tradicional funciona)
