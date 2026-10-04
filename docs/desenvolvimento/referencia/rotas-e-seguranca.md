---
titulo: Rotas e segurança
publico: desenvolvimento
tipo: referencia
atualizado_em: 2026-10-04
fontes:
  - src/main/java/com/example/loginbase/seguranca/SecurityConfig.java
  - src/main/java/com/example/loginbase/web/PaginaController.java
  - src/main/resources/templates/sistema/public/login.html
  - src/test/resources/templates/sistema/public/cadastro_usuario/index.html
  - src/test/resources/templates/sistema/seguro/patrimonio/index.html
  - src/main/resources/application.properties
  - src/test/java/com/example/loginbase/web/AutenticacaoWebMvcTest.java
---

# Rotas e segurança

Mapa das rotas HTTP, métodos permitidos, requisitos de autenticação e comportamento esperado.

## Rotas de autenticação

| Rota | Método | Acesso | Corpo | Resposta | Observações |
|---|---|---|---|---|---|
| `/login` | `GET` | Público | — | HTML (formulário) | Página de login com campos "E-mail ou celular" e "Senha"; CSRF token injetado pelo Thymeleaf |
| `/login` | `POST` | Público | `login` (e-mail ou celular), `senha`, `_csrf` | Sucesso: `302` para página salva ou `/patrimonio/index`; erro: `302` para `/login?error` | Validação no backend: `IdentificadorLogin` classifica entrada; mensagem genérica (mesma para usuário inexistente, senha errada ou conta desabilitada) |
| `/logout` | `POST` | Público | `_csrf` | `302` para `/login?logout` | Invalida a sessão HTTP, apaga o cookie `JSESSIONID`; configurado com `.permitAll()` |

## Rotas de navegação

| Rota | Método | Acesso | Resposta | Observações |
|---|---|---|---|---|
| `/` | `GET` | Autenticado | `302` para `/patrimonio/index`; anônimo recebe `302` para `/login` | Redirecionamento da raiz para a página inicial (SPA segura) |
| `/cadastro_usuario` | `GET` | Público | `302` para `/cadastro_usuario/index`; não requer login | Redirecionamento para a SPA pública |
| `/cadastro_usuario/` | `GET` | Público | `302` para `/cadastro_usuario/index` | Redirecionamento (barra final mapeada explicitamente) |
| `/cadastro_usuario/index` | `GET` | Público | HTML (SPA) | Servida pelo `PaginaController` via template `sistema/public/cadastro_usuario/index.html`; sem requer autenticação |
| `/cadastro_usuario/{s1}` a `/cadastro_usuario/{s1}/{s2}/{s3}/{s4}/{s5}` | `GET` | Público | HTML (SPA) | Rotas do cliente Vue, casadas até 5 níveis, sem extensão no último segmento (ex.: `/cadastro_usuario/termos`); sem requer autenticação |
| `/patrimonio` | `GET` | Autenticado | `302` para `/patrimonio/index`; anônimo recebe `302` para `/login` | Redirecionamento para a SPA segura |
| `/patrimonio/` | `GET` | Autenticado | `302` para `/patrimonio/index`; anônimo recebe `302` para `/login` | Redirecionamento (barra final mapeada explicitamente) |
| `/patrimonio/index` | `GET` | Autenticado | HTML (SPA) | Servida pelo `PaginaController` via template `sistema/seguro/patrimonio/index.html`; anônimo recebe `302` para `/login` |
| `/patrimonio/{s1}` a `/patrimonio/{s1}/{s2}/{s3}/{s4}/{s5}` | `GET` | Autenticado | HTML (SPA) | Rotas do cliente Vue, casadas até 5 níveis, sem extensão no último segmento (ex.: `/patrimonio/bens`, `/patrimonio/bens/123/editar`); anônimo recebe `302` para `/login` |

## Recursos estáticos e públicos

| Rota | Acesso | Observações |
|---|---|---|
| `/css/**` | Público | Folhas de estilo |
| `/js/**` | Público | Scripts |
| `/images/**` | Público | Imagens |
| `/favicon.ico` | Público | Ícone do navegador |
| `/error` | Público | Página de erro padrão do Spring |
| `/.well-known/**` | Público | Requisições automáticas (DevTools, assets de origem cruzada); não há `.well-known/security.txt` implementado |

## Rotas da API REST

| Rota | Padrão | Acesso | Resposta anônima |
|---|---|---|---|
| `/api/**` | Endpoints REST | Autenticado | `401 UNAUTHORIZED` (sem redirecionamento) |
| `/v3/api-docs/**` | Metadados OpenAPI (JSON) | Autenticado | `401 UNAUTHORIZED` (sem redirecionamento) |
| `/swagger-ui.html` | Redirecionamento | Autenticado | `302` para `/swagger-ui/index.html`; anônimo recebe `302` para `/login` e, após login, volta a `/swagger-ui/index.html` (SavedRequest) |
| `/swagger-ui/**` | Interface Swagger UI | Autenticado | Anônimo recebe `302` para `/login` via `LoginUrlAuthenticationEntryPoint`; autenticado, acessa normalmente |

## Segurança

### CSRF

- **Habilitado:** `csrf.spa()` — grava um token no cookie `XSRF-TOKEN` (legível por JavaScript) e aceita o cabeçalho `X-XSRF-TOKEN` ou o parâmetro `_csrf`.
- **Formulários HTML:** Thymeleaf injeta `<input type="hidden" name="_csrf" ...>` via `th:action`.
- **Requisições XHR/Fetch (endpoints REST):** incluir o cabeçalho `X-XSRF-TOKEN` com o valor do cookie `XSRF-TOKEN`.

### Cookies e sessão

| Propriedade | Valor | Configuração |
|---|---|---|
| Nome | `JSESSIONID` | Padrão do Servlet |
| Duração | `30m` (inatividade) | `SESSION_TIMEOUT` em `application.properties` |
| HTTPOnly | Sim | `server.servlet.session.cookie.http-only=true` |
| SameSite | `Lax` | `server.servlet.session.cookie.same-site=lax` |
| Secure | Configurável | `SESSION_COOKIE_SECURE=false` (padrão); `true` em produção com HTTPS |

### Mudança de ID de sessão

Após login bem-sucedido, o ID da sessão é trocado (`changeSessionId()`), evitando fixação de sessão.

### Cache de requisições

Requisições para `/api/**`, `/v3/api-docs/**`, `/.well-known/**`, `/cadastro_usuario/**`, `/patrimonio/**` (todas as SPAs) e `/favicon.ico` **não são salvas** como página anterior. Isso impede que requisições automáticas ou XHR sequestrem o redirecionamento pós-login. Todas as outras requisições (formulários, navegação) são salvas e respeitadas após login (redirecionamento para `SavedRequest`).

## Comportamento após login

1. O usuário preenche `/login` e faz POST.
2. `UsuarioDetailsService` valida o `login` (e-mail ou celular) e `senha`.
3. Se válido, `RegistroSessaoSuccessHandler` registra a sessão em `sessoes`.
4. O navegador é redirecionado para:
   - A página que estava tentando acessar (se salva), ou
   - `/patrimonio/index` (padrão, página inicial do app seguro)
5. Após login, a SPA em `/patrimonio/index` carrega e pode usar cookies de autenticação automaticamente.

## Comportamento após logout

1. O usuário submete `POST /logout` (ex.: ao clicar em um botão no cliente).
2. A sessão HTTP é invalidada.
3. O browser é redirecionado para `/login?logout` com a mensagem de confirmação.

> **A confirmar com o responsável:** Implementar um botão de logout na interface (atualmente não existe).

## Veja também

- [Autenticação e sessões](../explicacoes/autenticacao-e-sessoes.md)
- [API REST](./api-rest.md)
- [Frontend integrado ao backend](../explicacoes/frontend-integrado-ao-backend.md)
