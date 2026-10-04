# Documentação do login-base

Toda a documentação do projeto está organizada por público (desenvolvedor, operador, usuário, negócio) usando a metodologia Diátaxis: **Tutoriais** para aprender, **Guias** para realizar tarefas, **Referência** para consultar fatos, e **Explicações** para entender como funciona.

## Por onde começar

| Se você é… | Comece por… |
|---|---|
| Desenvolvedor | [Primeiros passos](desenvolvimento/tutoriais/primeiros-passos.md) |
| Operador | [Subir e derrubar o ambiente Docker](operacao/guias/subir-e-derrubar-o-ambiente-docker.md) |
| Usuário | [Fazer seu primeiro acesso](usuario/tutoriais/primeiro-acesso.md) |
| Negócio/Gestor | [Visão geral do produto](negocio/explicacoes/visao-geral-do-produto.md) |

## Desenvolvimento

Documentação para quem altera o código, estuda a arquitetura ou contribui com pull requests.

### Tutoriais

- [Primeiros passos](desenvolvimento/tutoriais/primeiros-passos.md) — Clone o repositório, suba o banco e a aplicação, faça seu primeiro login.

### Guias

- [Configurar o Claude Code](desenvolvimento/guias/configurar-o-claude-code.md) — Instale o plugin PrimeVue e as skills do projeto para desenvolver no Claude Code.
- [Executar e depurar no IntelliJ](desenvolvimento/guias/executar-e-depurar-no-intellij.md) — Use o IntelliJ para rodar a aplicação com debugging, breakpoints e hot-swap.
- [Desenvolver o frontend](desenvolvimento/guias/desenvolver-o-frontend.md) — Suba o servidor de desenvolvimento Vue 3 com hot reload.
- [Gerar o build de produção](desenvolvimento/guias/gerar-o-build-de-producao.md) — Compile o frontend e empacote a aplicação para produção.
- [Executar os testes](desenvolvimento/guias/executar-os-testes.md) — Rode os testes unitários e de integração do backend e frontend.
- [Criar uma migração Flyway](desenvolvimento/guias/criar-uma-migracao-flyway.md) — Crie migrações de banco de dados versionadas.
- [Adicionar um endpoint REST](desenvolvimento/guias/adicionar-um-endpoint-rest.md) — Implemente novos endpoints documentados no Swagger UI.

### Referência

- [Stack e versões](desenvolvimento/referencia/stack-e-versoes.md) — Componentes, versões e arquivos onde estão definidas.
- [Estrutura do repositório](desenvolvimento/referencia/estrutura-do-repositorio.md) — Árvore comentada de pastas, código gerado e ignorado.
- [Modelo de dados](desenvolvimento/referencia/modelo-de-dados.md) — Tabelas, colunas, tipos, restrições e relacionamentos.
- [Rotas e segurança](desenvolvimento/referencia/rotas-e-seguranca.md) — URLs, métodos, autenticação, CSRF e cache.
- [API REST](desenvolvimento/referencia/api-rest.md) — Documentação dos endpoints sob `/api/**`, autenticação e convenções.

### Explicações

- [Arquitetura](desenvolvimento/explicacoes/arquitetura.md) — Pacotes, responsabilidades e fluxo entre frontend, backend e banco.
- [Autenticação e sessões](desenvolvimento/explicacoes/autenticacao-e-sessoes.md) — Como o login funciona, normalização de contato, registro de sessões, senhas e segurança.
- [Auditoria](desenvolvimento/explicacoes/auditoria.md) — Rastreamento automático de criação e alteração (quem, quando).
- [Frontend integrado ao backend](desenvolvimento/explicacoes/frontend-integrado-ao-backend.md) — Build do frontend, base path, rotas, CSRF e cache.

### Decisões de arquitetura

- [Índice de ADRs](desenvolvimento/explicacoes/decisoes/README.md) — Todas as decisões sobre tecnologia, padrões e infraestrutura.

## Operação

Documentação para quem sobe, configura e mantém o ambiente de desenvolvimento e produção.

### Guias

- [Subir e derrubar o ambiente Docker](operacao/guias/subir-e-derrubar-o-ambiente-docker.md) — Inicie, pause e limpe containers com `make up` e `make down`.
- [Executar a aplicação em container](operacao/guias/executar-a-aplicacao-em-container.md) — Compile o frontend e suba a aplicação Spring Boot no Docker.
- [Configurar o administrador inicial](operacao/guias/configurar-o-administrador-inicial.md) — Defina credenciais do admin na primeira inicialização.
- [Recriar o banco de dados](operacao/guias/recriar-o-banco-de-dados.md) — Limpe dados e resete o schema quando o Flyway ou validação falhar.
- [Consultar sessões registradas](operacao/guias/consultar-sessoes-registradas.md) — Inspecione logins, IP, navegador e histórico de sessões.
- [Resolver problemas comuns](operacao/guias/resolver-problemas-comuns.md) — Diagnóstico e soluções para erros frequentes (porta, profile, frontend, etc).

### Referência

- [Variáveis de ambiente](operacao/referencia/variaveis-de-ambiente.md) — Todas as variáveis, padrões, onde são lidas e o que controlam.
- [Serviços Docker Compose](operacao/referencia/servicos-docker-compose.md) — Descrição técnica dos containers, portas, volumes, perfis e diagrama.
- [Comandos make e scripts](operacao/referencia/comandos-make-e-scripts.md) — Referência dos alvos do Makefile e scripts Python.

### Explicações

- [Considerações para produção](operacao/explicacoes/consideracoes-para-producao.md) — Checklist e ajustes necessários antes de ir para produção.

## Usuário

Documentação para quem usa a aplicação pelas telas.

### Tutoriais

- [Fazer seu primeiro acesso](usuario/tutoriais/primeiro-acesso.md) — Receba suas credenciais, abra o aplicativo e faça login.

### Guias

- [Entrar e sair do sistema](usuario/guias/entrar-e-sair-do-sistema.md) — Formatos aceitos de login, o que fazer em caso de erro e como fazer logout.

### Referência

- [Mensagens e formatos de login](usuario/referencia/mensagens-e-formatos-de-login.md) — Mensagens exatas, formatos de e-mail e celular, validações.

## Negócio

Documentação para gestão, sem jargão técnico.

### Explicações

- [Visão geral do produto](negocio/explicacoes/visao-geral-do-produto.md) — O que é o login-base, capacidades atuais, público-alvo.
- [Limitações e próximos passos](negocio/explicacoes/limitacoes-e-proximos-passos.md) — O que está fora do escopo hoje e possibilidades futuras.

### Referência

- [Regras de acesso](negocio/referencia/regras-de-acesso.md) — Regras implementadas de autenticação, permissões, validade, auditoria e privacidade.
- [Glossário](negocio/referencia/glossario.md) — Termos principais explicados em linguagem acessível.
