# 0015 — `/api/**` Retorna 401 Sem Redirecionamento, CSRF para SPA

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-26 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-city-builder-game`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/) |

## Contexto e problema

A API REST (`/api/**`) é acessada por uma SPA que não sabe lidar com HTML de login. Se a sessão expirar, a API deve responder **401** (sem corpo, sem redirecionamento) para a SPA saber que precisa relogar. CSRF é crítico: POST precisa proteger contra ataques. Para SPA, CSRF vem via cookie + header (não via form).

## Direcionadores da decisão

- Sem redirecionamento: 401 simples permite SPA lidar (redirect para `/login` sem HTML)
- Sem request cache: redirect `/login?next=/api/...` não faz sentido (API não pode voltar)
- CSRF para SPA: token em cookie `XSRF-TOKEN`, enviado em header `X-XSRF-TOKEN`
- Form login continua com `_csrf` (compatibilidade com Thymeleaf)

## Opções consideradas

| Opção | Descrição |
|---|---|
| **401 sem body + `csrf.spa()`** | Anônimo em `/api/**` → 401; request cache desabilitado para `/api/**`; CSRF via `csrf.spa()` (cookie + header). |
| 302 redirect para login | Padrão Spring Security. Rejeitada: SPA recebe HTML, pode causar parsing error. |
| 403 Forbidden | Rejeitada: 403 é para autorização, 401 é para autenticação. |

## Resultado da decisão

Adotou-se **401 sem body + CSRF SPA**:

1. **SecurityConfig** (ajustes):
   ```java
   http
     .exceptionHandling(e -> e
       .defaultAuthenticationEntryPointFor(
         new HttpStatusEntryPoint(UNAUTHORIZED),
         PathPatternRequestMatcher.withDefaults().matcher("/api/**")
       )
     )
     .requestCache(cache -> cache
       .requestCache(new HttpSessionRequestCache() {
         @Override
         public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
           // skip /api/**, don't save
         }
       })
     )
     .csrf(csrf -> csrf.spa());
   ```

2. **CSRF SPA**:
   - Token vem em cookie `XSRF-TOKEN` (legível)
   - Cliente envia em header `X-XSRF-TOKEN`
   - Thymeleaf form continua com `_csrf` (compatível)

3. **Resposta 401**:
   - Corpo vazio (nenhum JSON, nenhum HTML)
   - SPA interpreta como "non-authenticated", redireciona para `/login`

### Consequências positivas
- **SPA-friendly**: 401 é esperado, sem surpresas de HTML
- **Sem request cache**: `/api/...` não fica salvo em sessão (mais limpo)
- **CSRF duplo**: suporta form + SPA simultaneamente
- **Simples**: não requer token refresh, token é legível (público)

### Consequências negativas
- **CSRF token público**: `XSRF-TOKEN` é legível (mas esperado em CSRF SPA)
- **Sem XSS protection**: token em localStorage ou cookie legível (mitigado: dev only)
- **Mudança de headers**: cliente precisa saber enviar `X-XSRF-TOKEN` (documentado)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| 401 sem body + csrf.spa() | SPA-friendly; duplo CSRF; simples. | Token público (esperado em SPA). |
| 302 redirect para login | Padrão Spring. | SPA recebe HTML (parsing error). |
| 403 Forbidden | Distingue autenticação de autorização. | Semântica incorreta (403 = autorização). |

## Mais informações

- **Design**: [`add-city-builder-game/design.md` §18](../../openspec/changes/archive/2026-09-27-add-city-builder-game/design.md)
- **Código**: [`seguranca/SecurityConfig.java`](../../src/main/java/com/example/loginbase/seguranca/SecurityConfig.java) — exceptionHandling, csrf.spa()
- **Teste**: [`web/ApiSegurancaWebMvcTest.java`](../../src/test/java/com/example/loginbase/web/ApiSegurancaWebMvcTest.java) — 401 anônimo
- **HTTP client**: [`frontend/src/api/http.ts`](../../frontend/src/api/http.ts) — lê `XSRF-TOKEN`, envia `X-XSRF-TOKEN`

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
