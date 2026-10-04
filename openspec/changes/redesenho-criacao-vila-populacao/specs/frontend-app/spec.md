# Spec Delta

## MODIFIED Requirements

### Requirement: Projeto frontend Vue 3 com PrimeVue 5
O repositório SHALL conter na pasta `frontend/` um projeto Vite com Vue 3, TypeScript e PrimeVue 5, com o PrimeVue registrado como plugin da aplicação usando o preset "Vilarejo", derivado do tema Aura com `definePreset`, em modo escuro sempre ativo. O comando de build do projeto MUST terminar sem erros de tipo nem de compilação.

#### Scenario: Build do projeto
- **WHEN** o build do frontend é executado (`npm run build`) no container Node do projeto
- **THEN** o build termina com sucesso e gera os arquivos estáticos
- **AND** a checagem de tipos não reporta erros

#### Scenario: Preset escuro registrado
- **WHEN** a aplicação é carregada
- **THEN** o PrimeVue usa o preset "Vilarejo" e o seletor de modo escuro está ativo no documento

### Requirement: Página inicial de boas-vindas
A aplicação SHALL exibir, na rota raiz (`/`, com alias `/index`), uma página que mostra o texto "Seja bem-vindo"; as demais telas da aplicação ficam em rotas próprias (como as do jogo em `/jogo/...`).

#### Scenario: Abrir a página inicial
- **WHEN** o usuário acessa a raiz do frontend no navegador
- **THEN** a página exibe o texto "Seja bem-vindo"
