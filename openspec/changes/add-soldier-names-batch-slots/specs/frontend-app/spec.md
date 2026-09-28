# Spec Delta: frontend-app

## MODIFIED Requirements

### Requirement: Roteamento no modo history
A aplicação MUST usar `vue-router` em modo history (URLs sem `#`), roteando internamente para as telas do jogo sem recarregar a página. As rotas principais são `/` (vila), `/fazenda` (fazenda), `/forja` (forja), `/quartel` (quartel), `/quartel/unidades/:id` (detalhe da unidade no quartel), `/masmorras` (masmorras), `/batalhas/:id` (batalha tática).

#### Scenario: Navegação entre rotas
- **WHEN** o usuário clica em "Forja" no menu da aplicação
- **THEN** a URL muda para `/forja` e a tela da forja é exibida sem recarregar a página

#### Scenario: Rota de detalhe da unidade
- **WHEN** o usuário clica em uma unidade no quartel
- **THEN** a URL muda para `/quartel/unidades/42` e o detalhe da unidade é exibido sem recarregar a página

#### Scenario: URL history mode
- **WHEN** o usuário navega para `/masmorras` manualmente
- **THEN** a aplicação carrega a tela de masmorras (sem `#` na URL)

#### Scenario: Acesso autenticado e anônimo à rota de detalhe
- **WHEN** um usuário autenticado faz GET `/quartel/unidades/42`
- **THEN** a requisição retorna 200 com a view `sistema/seguro/index`
- **AND** um usuário anônimo faz GET `/quartel/unidades/42`, é redirecionado para `/login`
