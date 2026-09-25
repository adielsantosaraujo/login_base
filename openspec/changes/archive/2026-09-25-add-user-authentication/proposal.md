# Proposal

## Why

O projeto `login_base` já tem Spring Security, Thymeleaf, JPA e Postgres no `pom.xml`, mas nenhuma regra de negócio: hoje qualquer rota cai no login gerado automaticamente pelo Spring Security, com um usuário em memória. É preciso a base de autenticação real — usuários, perfis e permissões persistidos no banco, com login baseado em sessão HTTP — para que as próximas funcionalidades possam ser protegidas.

## What Changes

- Modelo de dados de controle de acesso no Postgres: `usuarios`, `perfis`, `permissoes`, `usuario_rel_perfis`, `perfis_rel_permissoes` e `sessoes`, todas com os campos de auditoria `criado_em`, `criado_por`, `alterado_em`, `alterado_por`.
- Esquema do banco passa a ser versionado com **Flyway** (migrações SQL); o Hibernate deixa de criar/alterar tabelas (`ddl-auto` passa de `update` para `validate`). **BREAKING** para quem dependia do `ddl-auto=update`.
- Autenticação por formulário (form login) no padrão do Spring Security, com sessão HTTP (stateful), senha com hash BCrypt, CSRF habilitado e logout.
- Usuário autenticado por e-mail **ou celular** + senha; o celular (antes "telefone") é opcional, único e tem 11 dígitos (DDD + número); authorities formadas pelos perfis vigentes (`ROLE_<perfil>`) e pelas permissões associadas a esses perfis.
- Vínculo usuário↔perfil respeita vigência (`data_inicial`/`data_final`).
- Registro de cada sessão autenticada na tabela `sessoes` (início, fim, IP, dispositivo e token), encerrada no logout ou na expiração.
- Tela de login em Thymeleaf em `src/main/resources/templates/sistema/public/login.html`.
- Página protegida em `src/main/resources/templates/sistema/seguro/index.html` exibindo apenas "Seja bem vindo".
- Criação automática, na inicialização, do perfil `ADMIN` e de um usuário administrador inicial, com e-mail/senha vindos de variáveis de ambiente.

## Capabilities

### New Capabilities
- `access-control-data`: modelo persistido de usuários, perfis, permissões, vínculos com vigência, registro de sessões e auditoria padrão das tabelas.
- `user-authentication`: login por formulário com sessão HTTP, logout, proteção de rotas, telas Thymeleaf de login e página inicial segura, registro de sessões e usuário administrador inicial.

### Modified Capabilities
<!-- Nenhuma: as specs existentes (docker-dev-environment, frontend-app, subagent-dev-workflow) não mudam de comportamento. -->

## Impact

- **Dependências** (`pom.xml`): adiciona Flyway (`spring-boot-starter-flyway` + `flyway-database-postgresql`).
- **Configuração**: `application.properties` (Flyway, `ddl-auto=validate`, timeout de sessão, variáveis do admin inicial); `.env.example` e `docker-compose.yml` ganham `ADMIN_EMAIL`/`ADMIN_PASSWORD`.
- **Código novo** em `com.example.loginbase`: entidades/repositórios JPA, configuração do Spring Security, `UserDetailsService`, listeners de sessão, controller das páginas e seed do admin.
- **Recursos**: migrações em `src/main/resources/db/migration/` e templates em `src/main/resources/templates/sistema/`.
- **Banco**: tabelas novas no schema `public`; bancos de desenvolvimento já criados pelo `ddl-auto=update` (sem tabelas de negócio hoje) não são afetados.
- **Frontend Vue**: não é alterado nesta change.
