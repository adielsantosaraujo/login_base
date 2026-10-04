---
titulo: Estrutura do repositório
publico: desenvolvimento
tipo: referencia
atualizado_em: 2026-10-04
fontes:
  - .gitignore
  - .dockerignore
---

# Estrutura do repositório

Visão geral dos diretórios e arquivos principais do projeto.

## Backend (Java / Spring Boot)

```
src/main/
├── java/com/example/loginbase/
│   ├── LoginBaseApplication.java          — Classe principal
│   ├── acesso/                            — Entidades de usuários, perfis, permissões e sessões
│   │   ├── Usuario.java
│   │   ├── Perfil.java
│   │   ├── Permissao.java
│   │   ├── UsuarioPerfil.java
│   │   ├── PerfilPermissao.java
│   │   ├── Sessao.java
│   │   ├── UsuarioRepository.java
│   │   ├── UsuarioPerfilRepository.java
│   │   ├── PerfilRepository.java
│   │   ├── PermissaoRepository.java
│   │   ├── PerfilPermissaoRepository.java
│   │   ├── SessaoRepository.java
│   │   ├── NormalizacaoContato.java       — Normalização de e-mail e celular
│   │   └── PerfilPermissaoVigente.java    — Projeção para consultas
│   ├── auditoria/                         — Suporte a auditoria (quem criou/alterou e quando)
│   │   ├── EntidadeAuditavel.java         — Superclasse com campos de auditoria
│   │   ├── AuditoriaConfig.java           — Configuração do JPA Auditing
│   │   └── UsuarioAuditorAware.java       — Provedor do usuário atual para auditoria
│   ├── seguranca/                         — Autenticação, sessões e segurança
│   │   ├── SecurityConfig.java            — Configuração da cadeia de filtros
│   │   ├── UsuarioDetailsService.java     — Carregamento de usuários para autenticação
│   │   ├── IdentificadorLogin.java        — Interpretação de e-mail ou celular
│   │   ├── RegistroSessaoSuccessHandler.java — Registro de sessões após login
│   │   ├── SessaoService.java             — Serviço de registro/encerramento de sessões
│   │   ├── SessoesAbertasRunner.java       — Fechamento de sessões pendentes na inicialização
│   │   ├── AdminInicialRunner.java         — Criação do administrador inicial
│   │   ├── SessaoEncerradaListener.java    — Listener de encerramento de sessões
│   │   └── SessaoEventosConfig.java        — Publicador de eventos de sessão HTTP
│   └── web/
│       ├── PaginaController.java           — Rotas de navegação (login, SPA, redirecionamentos)
│       └── OpenApiConfig.java              — Configuração OpenAPI/Swagger
└── resources/
    ├── application.properties               — Propriedades da aplicação
    ├── db/migration/
    │   ├── V1__controle_acesso.sql        — Tabelas de usuários, perfis, permissões e sessões
    │   └── V2__perfil_admin.sql           — Seed do perfil ADMIN
    ├── templates/
    │   └── sistema/
    │       ├── public/
    │       │   └── login.html              — Formulário de login
    │       └── seguro/
    │           └── app/
    │               └── index.html          — Template da SPA (gerado por build_front.py)
    └── static/                             — Recursos estáticos (CSS, imagens, etc.)
        └── app/                            — Assets do frontend (gerados por build_front.py)
```

## Testes

```
src/test/
├── java/com/example/loginbase/
│   ├── web/
│   │   ├── AutenticacaoWebMvcTest.java     — Testes de slices do Spring Security
│   │   └── OpenApiIntegracaoTest.java      — Testes de OpenAPI/Swagger (requer Postgres)
│   ├── acesso/
│   │   └── UsuarioTest.java
│   ├── auditoria/
│   │   └── UsuarioAuditorAwareTest.java
│   ├── seguranca/
│   │   ├── AdminInicialRunnerTest.java
│   │   ├── IdentificadorLoginTest.java
│   │   ├── RegistroSessaoSuccessHandlerTest.java
│   │   ├── SessaoEncerradaListenerTest.java
│   │   ├── SessaoServiceTest.java
│   │   ├── SessoesAbertasRunnerTest.java
│   │   └── UsuarioDetailsServiceTest.java
│   └── LoginBaseApplicationTests.java      — Testes de integração (requer Postgres)
└── resources/
    ├── static/app/assets/
    │   └── teste-estatico.js               — Recurso estático para testes
    └── templates/
        └── sistema/seguro/app/
            └── index.html                  — Stub do template para testes
```

## Frontend (Vue 3)

```
frontend/
├── src/
│   ├── main.ts                            — Inicialização do Vue
│   ├── App.vue                            — Componente raiz
│   ├── vite-env.d.ts                      — Tipos do Vite
│   ├── views/
│   │   ├── HomeView.vue                   — Página de boas-vindas
│   │   └── HomeView.spec.ts               — Testes do HomeView
│   └── router/
│       ├── index.ts                       — Configuração de rotas
│       └── index.spec.ts                  — Testes de rotas
├── public/                                — Recursos públicos (favicon, etc.)
├── index.html                             — Ponto de entrada do Vite
├── package.json                           — Dependências do npm
├── package-lock.json                      — Lock de dependências
├── vite.config.ts                         — Configuração do Vite
├── tsconfig.json                          — Configuração global do TypeScript
├── tsconfig.app.json                      — Configuração do TypeScript para app
├── tsconfig.node.json                     — Configuração do TypeScript para build
├── Dockerfile                             — Imagem de desenvolvimento do frontend
├── .dockerignore
├── .gitignore
├── dist/                                  — Build de produção (gerado, ignorado)
└── README.md                              — Documentação do Vite (boilerplate)
```

## Configuração

```
.
├── pom.xml                                — Definição do projeto Maven
├── Dockerfile                             — Imagem multi-stage do backend
├── docker-compose.yml                     — Orquestração dos serviços
├── Makefile                               — Alvos auxiliares
├── .env.example                           — Exemplo de variáveis de ambiente
├── .env                                   — Variáveis reais (gerado, ignorado)
├── .gitignore                             — Arquivos ignorados pelo git
├── .dockerignore                          — Arquivos ignorados pelo Docker
├── README.md                              — Documentação da raiz
├── CLAUDE.md                              — Instruções para Claude Code
└── .claude/
    └── skills/
        ├── dev-subagentes/                — Orquestração de subagentes
        └── documentacao/                  — Skill de documentação
```

## Scripts e ferramentas

```
scripts/
├── build_front.py                         — Build do frontend integrado ao backend
├── executar.py                            — Menu interativo de execução
└── cores.py                               — Utilitário de cores para scripts
```

## Documentação

```
docs/
├── README.md                              — Índice de toda a documentação
├── desenvolvimento/                       — Documentação para desenvolvedores
│   ├── referencia/                        — Referências técnicas
│   ├── explicacoes/                       — Explicações arquiteturais
│   │   └── decisoes/                      — Decisões de arquitetura (ADRs)
│   ├── guias/                             — Guias de como fazer
│   └── tutoriais/                         — Tutoriais passo a passo
├── operacao/                              — Documentação para operações
├── usuario/                               — Documentação para usuários
└── negocio/                               — Documentação para negócios
```

## Arquivos gerados e ignorados

Os seguintes arquivos e pastas são gerados durante o desenvolvimento ou build e não são versionados:

| Caminho | Origem | Ignorado em |
|---|---|---|
| `.env` | Copiado de `.env.example` e customizado | `.gitignore` |
| `target/` | Build Maven | `.gitignore` |
| `build.log` | Script `build_front.py` | `.gitignore` |
| `frontend/dist/` | Build do Vite | `frontend/.gitignore` |
| `frontend/node_modules/` | npm ci/install | `frontend/.gitignore` |
| `src/main/resources/static/app/` | Script `build_front.py` | `.gitignore` |
| `src/main/resources/templates/sistema/seguro/app/index.html` | Script `build_front.py` | `src/main/resources/templates/sistema/seguro/.gitignore` |
| `src/test/resources/templates/sistema/seguro/app/index.html` | Stub para testes | `.gitignore` (linha 39: `/src/test/resources/templates/sistema/seguro/app/*`) |
| `frontend-node-modules` (volume Docker) | Docker Compose | N/A |
| `db-data` (volume Docker) | PostgreSQL | N/A |

## Veja também

- [Stack e versões](./stack-e-versoes.md)
- [Modelo de dados](./modelo-de-dados.md)
- [Rotas e segurança](./rotas-e-seguranca.md)
