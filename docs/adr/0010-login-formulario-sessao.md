# 0010 — Login por Formulário + Sessão HTTP Stateful

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-24 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-user-authentication`](../../openspec/changes/archive/2026-09-25-add-user-authentication/) |

## Contexto e problema

Autenticação pode ser stateful (sessão HTTP, cookie) ou stateless (JWT, token). Para um projeto monolítico inicial (backend + frontend SPA na mesma origem via proxy), sessão HTTP é mais simples. O identificador de login é flexível: e-mail único, celular único, ou ambos. Username único simplifica auditoria e principal da sessão.

## Direcionadores da decisão

- Statefulness: sessão HTTP (cookie `JSESSIONID`), simples e padrão no Spring Security
- Identificadores: usuário pode logar com e-mail **ou** celular, mas `username` na sessão é sempre o e-mail (único)
- Normalização: e-mail em minúsculas, celular apenas dígitos
- Segurança de sessão: timeout 30 min, cookie `HttpOnly`, `SameSite=Lax`, troca de ID na autenticação
- Resiliência: logout (invalidação), expiração automática

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Sessão HTTP com `UserDetailsService` padrão** | Form login Spring Security, `UsuarioDetailsService` personalizado que classifica login (e-mail vs. celular), carrega perfis vigentes, retorna `User` com `username=email`. |
| JWT stateless | Alternativa moderna; rejeitada pois requer frontend integrado (não pedido) e maior complexidade. |
| Sessão + JWT | Híbrido; rejeitado por over-engineering inicial. |

## Resultado da decisão

Adotou-se **sessão HTTP + Spring Security form login**:

1. **UsuarioDetailsService** (`seguranca.UsuarioDetailsService`):
   - `loadUserByUsername(login)`: classifica entrada com `IdentificadorLogin`
     - Com `@` → busca por email normalizado
     - Sem `@` → remove não-dígitos, se = 11 dígitos busca celular, else `UsernameNotFoundException`
   - Carrega usuário + perfis vigentes (em `LocalDate.now()`) + permissões
   - Retorna `org.springframework.security.core.userdetails.User` com:
     - `username = email` (sempre, mesmo se login foi por celular)
     - `password = hash` (BCrypt)
     - `disabled = true` se nenhum perfil vigente
     - `authorities = ROLE_<perfil> + permissão_nomes`

2. **SecurityFilterChain**:
   - `formLogin()`: `loginPage("/login")`, `usernameParameter("login")`, `passwordParameter("senha")`, `defaultSuccessUrl("/")`, `failureUrl("/login?error")`
   - `authorizeHttpRequests()`: `/login`, `/css/**`, `/js/**`, `/images/**`, `/favicon.ico`, `/error` → `permitAll`; outros → `authenticated()`
   - `sessionFixation().changeSessionId()` (padrão, explícito)
   - CSRF habilitado (padrão)

3. **Templates Thymeleaf**:
   - `login.html`: campos `login` (rótulo "E-mail ou celular"), `senha`, `_csrf`
   - Mensagens: "Usuário ou senha inválidos." (genérica, sem enumeração)
   - `index.html`: "Seja bem vindo" (sem logout button) — **Previsto ser substituído**: `/` passará a servir a SPA em produção ([ADR 0023](0023-spa-servida-pelo-backend.md), change `add-frontend-build`)

### Consequências positivas
- **Simples**: Spring Security padrão, sem código de autenticação customizado
- **Flexível**: login com e-mail ou celular, mas username único
- **Seguro**: genérica "usuário ou senha inválidos", troca de ID de sessão
- **Confortável**: perfis e permissões disponíveis no `Authentication`, baixo latência
- **Testável**: `UserDetailsService` é fácil de mockar em `@WebMvcTest`

### Consequências negativas
- **Stateful**: servidor mantém estado (sessões). Não escalável horizontalmente (mitigado: projeto inicial, 1 instância)
- **Sem MFA/2FA**: não está incluído (Non-Goal, pode vir em change futura)
- **Sem JWT**: SPA frontend pode não integrar bem com JWT no futuro (mudança de arquitetura, não bloqueadora)
- **Conta sem perfil vigente**: aparenta desabilitada sem indicar por quê (mensagem genérica, segurança)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Sessão HTTP + UserDetailsService | Simples; padrão Spring; perfis carregados; flexível (e-mail ou celular). | Stateful; não escalável horizontalmente. |
| JWT stateless | Sem estado; escalável. | Requer frontend integrado; token refresh; não pedido. |
| Sessão + JWT | Híbrido. | Over-engineering; requer ambos os mecanismos. |

## Mais informações

- **Design**: [`add-user-authentication/design.md` §Decisions 5, 7, 8](../../openspec/changes/archive/2026-09-25-add-user-authentication/design.md)
- **Código**:
  - [`seguranca/UsuarioDetailsService.java`](../../src/main/java/com/example/loginbase/seguranca/UsuarioDetailsService.java)
  - [`seguranca/IdentificadorLogin.java`](../../src/main/java/com/example/loginbase/seguranca/IdentificadorLogin.java)
  - [`seguranca/SecurityConfig.java`](../../src/main/java/com/example/loginbase/seguranca/SecurityConfig.java)
- **Template**: [`src/main/resources/templates/sistema/public/login.html`](../../src/main/resources/templates/sistema/public/login.html)

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.1.0 | 2026-09-27 | Referência para ADR 0023 (substituição do placeholder `/` por SPA) | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
