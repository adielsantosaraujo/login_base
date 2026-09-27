# Spec Delta

## MODIFIED Requirements

### Requirement: Página inicial segura
A aplicação SHALL servir em `GET /` a página da SPA do frontend (o `index.html` gerado pelo build do frontend, entregue pela view Thymeleaf `sistema/seguro/index`), acessível apenas a usuários autenticados. O placeholder "Seja bem vindo" deixa de existir.

#### Scenario: Usuário autenticado acessa a página inicial
- **WHEN** um usuário autenticado acessa `GET /`
- **THEN** a resposta é HTTP 200 com a view `sistema/seguro/index` (a SPA)

#### Scenario: Visitante anônimo acessa a página inicial
- **WHEN** um visitante não autenticado acessa `GET /`
- **THEN** ele é redirecionado para `/login`
