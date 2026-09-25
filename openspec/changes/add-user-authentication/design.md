# Design

## Context

Ver proposal.md - Why. Estado atual e restrições relevantes:

- Spring Boot 4.1.1 (Spring Security 7, Hibernate 7) com Java 25; o `pom.xml` já traz `spring-boot-starter-security`, `-thymeleaf`, `-webmvc`, `-data-jpa`, `thymeleaf-extras-springsecurity6`, Lombok e o driver do Postgres, além dos starters de teste correspondentes.
- Só existe `LoginBaseApplication`; não há entidades, controllers nem templates. `application.properties` usa `spring.jpa.hibernate.ddl-auto=update`.
- O Postgres roda pelo `docker-compose.yml` (serviço `db`); a aplicação normalmente roda pelo IntelliJ apontando para ele, ou no serviço `app`.
- O frontend Vue é independente e não participa desta change.
- Requisitos: ver `specs/access-control-data/spec.md` e `specs/user-authentication/spec.md`.

## Goals / Non-Goals

**Goals:**
- Usar os mecanismos padrão do Spring Security (form login, `UserDetailsService`, `PasswordEncoder`, `SecurityFilterChain`, `HttpSessionSecurityContextRepository`, eventos de sessão) em vez de lógica própria de autenticação.
- Esquema do banco reprodutível por migrações.
- Código organizado por pacote de domínio, fácil de estender com telas de cadastro no futuro.

**Non-Goals:**
- CRUD/telas de usuários, perfis e permissões.
- "Lembrar-me", recuperação de senha, bloqueio por tentativas, MFA, limite de sessões simultâneas.
- Autenticação da API/frontend Vue (JWT, CORS) — o frontend continua sem integração com o backend.
- Autorização fina por permissão nas rotas (as autoridades ficam disponíveis, mas nesta change só se exige "autenticado").

## Decisions

1. **Estrutura de pacotes** em `com.example.loginbase`:
   - `auditoria` — `EntidadeAuditavel` (`@MappedSuperclass`) e `AuditorAware`.
   - `acesso` — entidades `Usuario`, `Perfil`, `Permissao`, `UsuarioPerfil`, `PerfilPermissao`, `Sessao` e seus repositórios Spring Data.
   - `seguranca` — `SecurityConfig`, `UsuarioDetailsService`, handlers/listeners de sessão, `AdminInicialRunner`.
   - `web` — `PaginaController` (rotas `/login` e `/`).
   - Alternativa: pacotes por camada (`entity`, `repository`, `service`). Rejeitada: espalha o domínio de acesso por vários pacotes.

2. **Flyway para o esquema**; `spring.jpa.hibernate.ddl-auto=validate`. Dependências `spring-boot-starter-flyway` e `org.flywaydb:flyway-database-postgresql` (versões do BOM do Boot). Migrações em `src/main/resources/db/migration`:
   - `V1__controle_acesso.sql` — as seis tabelas, PKs `bigint generated always as identity`, FKs, `unique`, índice único em `lower(email)`, `celular varchar(11) unique` (nulo permitido; `unique` do Postgres aceita vários nulos) com `check (celular ~ '^[0-9]{11}$')`, e `check (data_final is null or data_final >= data_inicial)`.
   - `V2__perfil_admin.sql` — insere o perfil `ADMIN` com `criado_por = 'sistema'`.
   - Alternativa: manter `ddl-auto=update` (escolha do usuário foi Flyway). Hibernate não gera índice funcional em `lower(email)` nem check constraints de forma controlada, e `update` não é seguro para produção.

3. **Auditoria via Spring Data JPA Auditing**: `@EnableJpaAuditing` + `@EntityListeners(AuditingEntityListener.class)` na superclasse, com `@CreatedDate`/`@CreatedBy` (`updatable = false`) e `@LastModifiedDate`/`@LastModifiedBy`. O `AuditorAware<String>` lê o `SecurityContextHolder` e devolve o nome do usuário autenticado (e-mail) ou `"sistema"` quando anônimo/ausente. Tipos: `criado_em`/`alterado_em` `timestamp with time zone` ↔ `OffsetDateTime`/`Instant`; `criado_por`/`alterado_por` `varchar(150)`.
   - Alternativa: `criado_por` como FK para `usuarios.id`. Rejeitada: cria dependência circular (usuários auditados por usuários), complica o seed e o registro de ações de processos do sistema.
   - Colunas de auditoria também são `not null` no banco; nas migrações de seed o SQL as preenche explicitamente.

4. **E-mail e celular normalizados**: a aplicação grava o e-mail em minúsculas e sem espaços e o celular somente com dígitos (normalização no setter/`@PrePersist` da entidade, que também rejeita com `IllegalArgumentException` celular que não tenha exatamente 11 dígitos — sem adicionar Bean Validation ao projeto nesta change). O índice único em `lower(email)` e o `check` do celular garantem as regras mesmo para gravações fora da aplicação.
   - Alternativa: guardar o celular com máscara ou em formato E.164 (`+55...`). Rejeitada: o requisito é DDD + número com 11 dígitos, e guardar só dígitos simplifica a busca no login.

5. **`UsuarioDetailsService` implementa `UserDetailsService`**: o valor recebido em `loadUserByUsername` é o identificador digitado. Um componente `IdentificadorLogin` o classifica: com `@` → busca por e-mail normalizado; sem `@` → remove não dígitos e, se sobrarem exatamente 11, busca por celular; caso contrário, `UsernameNotFoundException` sem consultar o banco. Encontrado o usuário, carrega, em uma consulta JPQL com `join fetch`/projeção, os perfis vigentes em `LocalDate.now()` e suas permissões. Retorna `org.springframework.security.core.userdetails.User` com `username = email` (sempre o e-mail, mesmo quando o login foi pelo celular — assim o `AuditorAware`, o registro de sessões e o principal da sessão têm um identificador único), `password = hash`, `disabled = true` quando não há perfil vigente, e authorities `ROLE_<perfil>` + nomes das permissões. Usuário inexistente → `UsernameNotFoundException`. O `DaoAuthenticationProvider` padrão (com `hideUserNotFoundExceptions`) transforma usuário inexistente em `BadCredentials`; conta desabilitada gera `DisabledException`, que o failure handler padrão também redireciona para `/login?error` — a página exibe sempre a mesma mensagem genérica, sem ler a exceção.
   - Alternativa: `Usuario` implementando `UserDetails` diretamente. Rejeitada: acopla a entidade JPA à sessão HTTP (serialização de entidade com coleções lazy).

6. **`PasswordEncoder`**: `PasswordEncoderFactories.createDelegatingPasswordEncoder()` (BCrypt por padrão, hash com prefixo `{bcrypt}`), permitindo trocar o algoritmo no futuro sem migrar senhas. A coluna `senha` é `varchar(255)`.

7. **`SecurityFilterChain`** (bean, sem estender classes depreciadas):
   - `authorizeHttpRequests`: `permitAll` para `/login`, `/css/**`, `/js/**`, `/images/**`, `/favicon.ico`, `/error`; `anyRequest().authenticated()`.
   - `formLogin`: `loginPage("/login")`, `usernameParameter("login")`, `passwordParameter("senha")`, `defaultSuccessUrl("/")` (sem `alwaysUse`, para respeitar a página salva pelo `RequestCache`), `failureUrl("/login?error")`, com o success handler descrito na decisão 9.
   - `logout`: `POST /logout`, `logoutSuccessUrl("/login?logout")`, `invalidateHttpSession(true)`, `deleteCookies("JSESSIONID")`.
   - `sessionManagement`: `sessionCreationPolicy(IF_REQUIRED)` e `sessionFixation().changeSessionId()` (padrão, declarado explicitamente).
   - CSRF habilitado (padrão); o Thymeleaf injeta o token automaticamente em `th:action`.

8. **Configuração da sessão** em `application.properties`: `server.servlet.session.timeout=${SESSION_TIMEOUT:30m}`, `server.servlet.session.cookie.http-only=true`, `server.servlet.session.cookie.same-site=lax`. `secure` fica configurável (`${SESSION_COOKIE_SECURE:false}`), já que em desenvolvimento a aplicação roda em HTTP.

9. **Registro em `sessoes`**:
   - **Abertura**: um `AuthenticationSuccessHandler` que estende `SavedRequestAwareAuthenticationSuccessHandler` (mantém o redirecionamento padrão) e, antes de delegar ao `super`, chama `SessaoService.registrarInicio(email, sessionId, ip, userAgent)`. O handler roda depois da troca de ID de sessão, então o ID registrado é o definitivo. Exceções do registro são logadas e engolidas (spec: falha no registro não impede o login).
   - **Token**: SHA-256 (hex) do ID da sessão HTTP — permite localizar o registro a partir da sessão sem guardar o ID utilizável.
   - **IP**: `request.getRemoteAddr()`. Com `server.forward-headers-strategy=native` o valor já reflete `X-Forwarded-For` quando houver proxy confiável; nesta change fica o padrão (`none`). `dispositivo`: cabeçalho `User-Agent` truncado para o tamanho da coluna (`varchar(500)`).
   - **Encerramento**: bean `HttpSessionEventPublisher` (publica eventos do container) e um `@EventListener` de `HttpSessionDestroyedEvent` que calcula o hash do ID e preenche `data_fim` se ainda nula. Cobre logout (invalidação) e expiração. Como não há usuário autenticado no contexto do evento de expiração, `alterado_por` fica `sistema`.
   - Alternativa: Spring Session JDBC (tabelas `SPRING_SESSION`). Rejeitada nesta change: impõe esquema próprio diferente de `sessoes`, e o requisito é registrar/auditar sessões, não externalizar o armazenamento.

10. **Controller e templates**: `PaginaController` com `GET /login` → `sistema/public/login` e `GET /` → `sistema/seguro/index`. `login.html` usa `th:action="@{/login}"`, campo `login` (rótulo "E-mail ou celular", `type="text"`, `autocomplete="username"`) e `senha`, e `th:if="${param.error}"`/`${param.logout}` para as mensagens fixas ("Usuário ou senha inválidos." / "Você saiu do sistema."). `index.html` contém apenas "Seja bem vindo" (sem botão de logout, conforme pedido). HTML simples com CSS mínimo inline; sem dependência de CDN.

11. **Administrador inicial**: `AdminInicialRunner` (`ApplicationRunner`, transacional) lê `app.admin.email=${ADMIN_EMAIL:admin@loginbase.local}` e `app.admin.password=${ADMIN_PASSWORD:}`. Senha vazia → `log.warn` e retorna. Se não existe usuário com o e-mail, cria `Usuario` (nome `Administrador`, sem celular) com a senha codificada e `UsuarioPerfil` vigente desde hoje ao perfil `ADMIN` (criado pela migração V2). Não altera usuário existente. Auditoria resulta em `sistema`.
   - Alternativa: inserir o admin por migração SQL. Rejeitada: a senha precisaria estar no repositório (hash fixo) e não viria de variável de ambiente.

12. **Configuração de ambiente**: `.env.example` ganha `ADMIN_EMAIL` e `ADMIN_PASSWORD` (vazio); `docker-compose.yml` repassa ambas ao serviço `app`. README ganha seção curta sobre login e admin inicial.

13. **Testes**:
    - `@WebMvcTest` + `spring-security-test` (importando `SecurityConfig` e mockando `UserDetailsService`/`SessaoService`): redirecionamento anônimo para `/login`, `/login` público, login válido/ inválido/ sem CSRF, logout, `index` exibe "Seja bem vindo", conta desabilitada gera `/login?error`.
    - Teste unitário do `UsuarioDetailsService` (repositório mockado): authorities, conta desabilitada, e-mail normalizado, login por celular (com e sem máscara), celular com dígitos a mais/menos, usuário inexistente.
    - Teste unitário do `AuditorAware` e do `AdminInicialRunner`.
    - O `LoginBaseApplicationTests` existente (contexto completo) continua exigindo o Postgres do compose; ele valida Flyway + `ddl-auto=validate`. Não se adiciona Testcontainers nesta change.

## Risks / Trade-offs

- [`ddl-auto=validate` falha a inicialização em bancos de desenvolvimento com tabelas criadas pelo antigo `update`] → Hoje não há entidades, então não existem tabelas conflitantes; se aparecerem, basta recriar o volume `db-data`. Registrado no README.
- [Registro em `sessoes` fica aberto se a aplicação cair sem destruir as sessões (sessões em memória se perdem no restart)] → Um `ApplicationRunner` (`SessoesAbertasRunner`) fecha na inicialização (`data_fim = now()`) os registros ainda abertos. Se no futuro houver várias instâncias ou sessão externalizada, essa regra precisa ser revista.
- [Consulta de perfis vigentes usa a data do servidor da aplicação] → Documentado; o fuso segue a JVM (`user.timezone`). Suficiente para vigência em dias.
- [`thymeleaf-extras-springsecurity6` com Spring Security 7] → Não é usado pelos templates desta change (não há `sec:` nas páginas); se causar incompatibilidade, a dependência é removida.
- [Usuário não precisa de perfil ativo para existir, mas não consegue logar sem ele] → Comportamento intencional (conta desabilitada); mensagem genérica evita enumeração, ao custo de o usuário não saber o motivo.
- [Celular como identificador de login facilita tentativa de enumeração/força bruta com números sequenciais] → Mesma mensagem genérica para todos os erros; bloqueio por tentativas fica fora do escopo (Non-Goals) e é candidato a change futura.
- [Número de celular pode ser reatribuído pela operadora a outra pessoa] → Aceito; a senha continua sendo necessária, e o cadastro futuro deve permitir atualizar o celular.
- [IP real atrás de proxy] → Sem `forward-headers-strategy` o IP gravado é o do proxy; habilitar quando houver proxy confiável.

## Migration Plan

1. Subir o Postgres (`make up`).
2. Definir `ADMIN_EMAIL`/`ADMIN_PASSWORD` no `.env` ou na configuração de execução do IntelliJ.
3. Iniciar a aplicação: o Flyway aplica V1 e V2 e o runner cria o admin.
4. Rollback: reverter o commit e remover as tabelas (ou recriar o volume `db-data`) — não há dados de produção.
