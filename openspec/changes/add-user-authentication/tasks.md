# Tasks

## 1. Dependências e configuração base

- [ ] 1.1 Adicionar ao `pom.xml` `spring-boot-starter-flyway` e `org.flywaydb:flyway-database-postgresql` (versões do BOM do Boot) e verificar que `./mvnw -q -DskipTests package` compila
- [ ] 1.2 Atualizar `application.properties`: `spring.jpa.hibernate.ddl-auto=validate`, `server.servlet.session.timeout=${SESSION_TIMEOUT:30m}`, cookie de sessão `http-only=true`, `same-site=lax`, `secure=${SESSION_COOKIE_SECURE:false}`, `app.admin.email=${ADMIN_EMAIL:admin@loginbase.local}` e `app.admin.password=${ADMIN_PASSWORD:}`; verificar que o projeto compila

## 2. Esquema do banco (Flyway)

- [ ] 2.1 Criar `src/main/resources/db/migration/V1__controle_acesso.sql` com `usuarios`, `perfis`, `permissoes`, `usuario_rel_perfis`, `perfis_rel_permissoes` e `sessoes` (PK identity, FKs, `unique` em `perfis.nome`, `permissoes.nome`, `sessoes.token` e no par perfil/permissão, índice único em `lower(usuarios.email)`, `usuarios.celular varchar(11)` opcional com `unique` e `check (celular ~ '^[0-9]{11}$')`, `check` de `data_final >= data_inicial`, colunas de auditoria `not null` com `criado_em`/`alterado_em` `timestamptz` e `criado_por`/`alterado_por` `varchar(150)`), e verificar aplicando-o no Postgres do compose (`make up` + subir a aplicação ou `psql`) que as tabelas e constraints existem (incluindo, via `psql`, que celular duplicado, com 10/12 dígitos ou com máscara é rejeitado e que dois usuários sem celular são aceitos)
- [ ] 2.2 Criar `V2__perfil_admin.sql` inserindo o perfil `ADMIN` com auditoria `sistema`/`now()`, e verificar com `select * from perfis` após a migração; verificar também que um segundo start não reaplica migrações (`flyway_schema_history` com 2 linhas)

## 3. Entidades, auditoria e repositórios

- [ ] 3.1 Criar em `auditoria` a `EntidadeAuditavel` (`@MappedSuperclass`, `AuditingEntityListener`, `@CreatedDate`/`@CreatedBy` não atualizáveis, `@LastModifiedDate`/`@LastModifiedBy`), o `AuditorAware<String>` (e-mail do usuário autenticado ou `sistema`) e `@EnableJpaAuditing`; teste unitário do `AuditorAware` cobrindo autenticado, anônimo e sem contexto
- [ ] 3.2 Criar em `acesso` as entidades `Usuario` (e-mail normalizado em minúsculas e sem espaços; celular opcional normalizado para só dígitos e rejeitado se não tiver 11), com teste unitário da normalização/validação, `Perfil`, `Permissao`, `UsuarioPerfil`, `PerfilPermissao` e `Sessao`, mapeadas exatamente para as colunas da V1, e os repositórios Spring Data (incluindo busca de usuário por e-mail e por celular, a consulta de perfis vigentes com permissões por usuário e data, e as operações de `Sessao` por token e de fechamento das abertas); verificar que a aplicação sobe contra o Postgres do compose com `ddl-auto=validate` (`LoginBaseApplicationTests` passa)

## 4. Autenticação com Spring Security

- [ ] 4.1 Implementar o `IdentificadorLogin` (com `@` → e-mail normalizado; sem `@` → só dígitos, exige 11) e o `UsuarioDetailsService` (busca por e-mail ou celular, `username` sempre o e-mail, `UsernameNotFoundException` para inexistente ou celular com dígitos a mais/menos, authorities `ROLE_<perfil>` + permissões dos perfis vigentes, `disabled` sem perfil vigente) e testes unitários com repositório mockado cobrindo esses casos
- [ ] 4.2 Criar `SecurityConfig` com `SecurityFilterChain` (rotas públicas `/login`, `/css/**`, `/js/**`, `/images/**`, `/favicon.ico`, `/error`; form login com `login`/`senha`, `failureUrl("/login?error")`; logout `POST /logout` → `/login?logout` invalidando a sessão e apagando `JSESSIONID`; `changeSessionId`; CSRF padrão) e o `PasswordEncoder` delegante; verificar que compila
- [ ] 4.3 Criar `PaginaController` (`GET /login` → `sistema/public/login`, `GET /` → `sistema/seguro/index`) e os templates `templates/sistema/public/login.html` (form com `th:action="@{/login}"`, campo `login` rotulado "E-mail ou celular" e campo `senha`, mensagens "Usuário ou senha inválidos." e "Você saiu do sistema.") e `templates/sistema/seguro/index.html` (apenas "Seja bem vindo")
- [ ] 4.4 Testes `@WebMvcTest` com `spring-security-test` (serviços mockados): anônimo em `/` redireciona para `/login`; `/login` responde 200 e mostra as mensagens com `?error`/`?logout`; login válido por e-mail e por celular (com e sem máscara) redireciona para `/`; senha errada, e-mail inexistente, celular inexistente ou incompleto e conta desabilitada redirecionam para `/login?error`; login sem CSRF é rejeitado; retorno à página salva; usuário autenticado vê "Seja bem vindo"; logout redireciona para `/login?logout`. Verificar com `./mvnw test`

## 5. Registro de sessões

- [ ] 5.1 Implementar o `SessaoService` (abrir registro com token SHA-256 do ID da sessão, IP e `User-Agent` truncado; fechar por token; fechar todos os abertos) e o success handler que estende `SavedRequestAwareAuthenticationSuccessHandler`, registra a sessão engolindo/logando falhas e delega o redirecionamento; testes unitários do serviço (hash ≠ ID, truncamento) e do handler (falha no registro não impede o redirecionamento)
- [ ] 5.2 Registrar o bean `HttpSessionEventPublisher` e o listener de `HttpSessionDestroyedEvent` que preenche `data_fim`, e o `SessoesAbertasRunner` que fecha registros abertos na inicialização; teste unitário do listener e do runner, e ajuste dos testes `@WebMvcTest` para confirmar que o login chama o registro

## 6. Administrador inicial e documentação

- [ ] 6.1 Implementar o `AdminInicialRunner` (sem senha → `warn` e sai; cria usuário `Administrador` com senha codificada e vínculo vigente ao `ADMIN` se o e-mail não existir; não altera existente) e testes unitários dos três casos
- [ ] 6.2 Adicionar `ADMIN_EMAIL` e `ADMIN_PASSWORD` (vazio) com comentários ao `.env.example`, repassá-las ao serviço `app` no `docker-compose.yml`, e verificar com `docker compose --env-file .env.example config` que o serviço `app` recebe as variáveis
- [ ] 6.3 Atualizar o `README.md` com: Flyway e `ddl-auto=validate` (e recriar o volume `db-data` se houver tabelas antigas), variáveis do admin inicial, URLs `http://localhost:8080/login` e `/`, e timeout de sessão; verificar que os comandos documentados rodam como escritos

## 7. Verificação integrada

- [ ] 7.1 Com `make up` e a aplicação rodando contra o Postgres do compose com `ADMIN_EMAIL`/`ADMIN_PASSWORD` definidas: `/` redireciona para `/login`; login com o admin mostra "Seja bem vindo"; após gravar um celular para o admin via `psql`, login pelo celular também funciona e o registro em `sessoes` aponta o mesmo usuário; e cria registro em `sessoes` com IP, dispositivo e `data_fim` nula; `POST /logout` preenche `data_fim`; senha errada mostra a mensagem genérica; auditoria das tabelas preenchida com `sistema`; reinício não recria o admin e fecha sessões abertas
- [ ] 7.2 Rodar `./mvnw test` e `openspec validate add-user-authentication --strict` sem erros, e deixar o ambiente (containers e `.env`) como estava antes da verificação
