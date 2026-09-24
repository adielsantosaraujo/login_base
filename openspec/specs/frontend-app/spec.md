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

### Requirement: Página inicial de boas-vindas
A aplicação SHALL ter uma única página, exibida na rota raiz (`/`), que mostra o texto "Seja bem-vindo".

#### Scenario: Abrir a página inicial
- **WHEN** o usuário acessa a raiz do frontend no navegador
- **THEN** a página exibe o texto "Seja bem-vindo"

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
