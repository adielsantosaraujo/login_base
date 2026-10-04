---
titulo: Documentação da API com springdoc
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-10-04
atualizado_em: 2026-10-04
fontes:
  - pom.xml
  - src/main/java/com/example/loginbase/web/OpenApiConfig.java
  - src/main/resources/application.properties
  - src/main/java/com/example/loginbase/seguranca/SecurityConfig.java
---

# 0012 — Documentação da API com springdoc

**Status:** Aceita · **Data:** 2026-10-04

## Contexto

A aplicação pode vir a ter endpoints REST (`/api/**`) futuros. É necessário escolher como documentar a API: manualmente em Markdown, com Swagger gerado por annotações, com OpenAPI escrito à mão, ou com springdoc automático.

## Decisão

Usar **springdoc-openapi 3.1.1** (com Spring Boot 4.1.1; a série 2.x é para Boot 3):

- **Dependency:** `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1` no `pom.xml`.
- **Configuração:** em `application.properties`:
  - `springdoc.paths-to-match=/api/**` (só endpoints REST documentados)
  - `springdoc.api-docs.enabled=${SPRINGDOC_ENABLED:true}` (ligável por variável)
  - `springdoc.swagger-ui.csrf.enabled=true` (integração com `csrf.spa()`)
- **Bean `OpenAPI`:** em `OpenApiConfig.java`, define título, versão, descrição em pt-BR e esquema de segurança `sessao` (cookie `JSESSIONID`), aplicado globalmente.
- **Acesso:** `/v3/api-docs` (JSON) e `/swagger-ui.html` (redirecionamento para `/swagger-ui/index.html`); ambos exigem autenticação (nenhum `permitAll` novo).
- **Resposta anônima:** GET `/v3/api-docs` sem login retorna 401 (via entry point para API); `/swagger-ui.html` redireciona para `/login` e, após entrar, o usuário volta ao Swagger.
- **Desligável:** variável `SPRINGDOC_ENABLED=false` desativa o Swagger UI.

## Alternativas descartadas

- **springdoc 2.x** — Rejeitada por incompatibilidade com Spring Boot 4.1.1 (2.x requer Boot 3).
- **OpenAPI escrito à mão** — Rejeitada porque exigiria manutenção manual sincronizada com o código.
- **Swagger público** — Rejeitada porque a API é protegida (autenticação por sessão); documentação pública exporia pontos de integração.
- **Documentar todas as rotas MVC** — Rejeitada porque `/login`, `/logout` e as rotas de SPA não são REST; Swagger documenta só `/api/**`.

## Consequências

### Positivas

- **Automático:** endpoints `@RestController` em `/api/**` com anotações `@Operation`/`@Tag` aparecem automaticamente no Swagger; sem anotações, ainda aparecem com assinatura padrão.
- **Seguro:** documentação só para usuários autenticados; sem exposição de API pública.
- **Flexível:** ligável/desligável por variável (`SPRINGDOC_ENABLED`).
- **CSRF integrado:** Swagger UI lê o cookie `XSRF-TOKEN` e envia `X-XSRF-TOKEN` automaticamente.

### Negativas

- **Hoje vazio:** sem endpoints REST implementados, Swagger mostra "No operations defined in spec!" — apenas o cabeçalho e o esquema de segurança aparecem.
- **Requer autenticação:** em produção, acessar a documentação exige login; útil para desenvolvimento, mas não para públicos externos.
- **Dependência extra:** springdoc adiciona alguns JARs; pode ser removida se documentação escrita à mão for preferida no futuro.

