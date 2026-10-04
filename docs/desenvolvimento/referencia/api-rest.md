---
titulo: API REST
publico: desenvolvimento
tipo: referencia
atualizado_em: 2026-10-04
fontes:
  - pom.xml
  - src/main/resources/application.properties
  - src/main/java/com/example/loginbase/web/OpenApiConfig.java
  - src/main/java/com/example/loginbase/seguranca/SecurityConfig.java
---

# API REST

Resumo da documentação OpenAPI/Swagger UI e das convenções para endpoints REST futuros.

## Status atual

Hoje a aplicação **não contém endpoints REST de negócio**. O Swagger UI abre em `http://localhost:8080/swagger-ui.html` (após login) e exibe "No operations defined in spec!" — apenas o cabeçalho (título, descrição, esquema de segurança) está definido.

## Acesso à documentação

| Recurso | URL | Autenticação | Nota |
|---|---|---|---|
| Swagger UI (interface visual) | `http://localhost:8080/swagger-ui.html` | Sim (redireciona anônimo) | Redireciona para `/swagger-ui/index.html` |
| JSON da API (metadados OpenAPI) | `http://localhost:8080/v3/api-docs` | Sim (retorna 401 anônimo) | Sem redirecionamento |
| Interface (destino do redirect) | `http://localhost:8080/swagger-ui/index.html` | Sim (redireciona anônimo) | Destino do redirect de `/swagger-ui.html` |

## Configuração

| Item | Valor | Localização |
|---|---|---|
| Versão do springdoc | 3.1.1 | `pom.xml` (`springdoc.version`) |
| Título da API | `login-base API` | `OpenApiConfig.java` |
| Versão documentada | `0.0.1-SNAPSHOT` | `OpenApiConfig.java` |
| Prefixo de endpoints | `/api/**` | `application.properties` (`springdoc.paths-to-match`) |
| Desligar via variável | `SPRINGDOC_ENABLED=false` | `application.properties` |

## Autenticação na API

### No Swagger UI

O Swagger UI lê o cookie `XSRF-TOKEN` (criado pelo filtro CSRF do Spring Security) e o envia automaticamente como cabeçalho `X-XSRF-TOKEN` em requisições que alteram estado (POST, PUT, DELETE), devido à configuração `springdoc.swagger-ui.csrf.enabled=true`.

1. Faça login em `http://localhost:8080/login`.
2. Abra `http://localhost:8080/swagger-ui.html`.
3. Teste endpoints diretamente da interface.

### Via Fetch ou cURL

Requisições programáticas precisam:

1. **Enviar cookie de autenticação:**
   ```bash
   curl -b "JSESSIONID=<id>" http://localhost:8080/api/...
   ```
   Ou com Fetch:
   ```javascript
   fetch('http://localhost:8080/api/...', {
     credentials: 'include'
   })
   ```

2. **Incluir token XSRF em mutações** (POST, PUT, DELETE):
   - Ler o cookie `XSRF-TOKEN` (disponível após login ou vindo do servidor)
   - Enviar no cabeçalho `X-XSRF-TOKEN`:
   ```bash
   curl -b "JSESSIONID=<id>; XSRF-TOKEN=<token>" \
     -H "X-XSRF-TOKEN: <token>" \
     -X POST http://localhost:8080/api/...
   ```

## Convenções para novos endpoints

### Padrão de rota

- **Prefixo:** `/api/` (obrigatório, define o que aparece no Swagger)
- **Recurso:** nome no plural (ex.: `/api/usuarios`, `/api/perfis`)
- **Operação:** padrão REST (GET, POST, PUT, DELETE)

### Autenticação de requisições

- **Endpoint anônimo → 401** (não redireciona para `/login` como rotas MVC)
- **Usuário autenticado é lido de:** `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` (do tipo `UserDetails`)

### CSRF

Requisições que alteram estado **exigem o cabeçalho `X-XSRF-TOKEN`** com o valor do cookie `XSRF-TOKEN`. Exemplo com Spring `@PostMapping`:

```java
@PostMapping
public ResponseEntity<?> criar(@RequestBody Dto dto) {
    // O CSRF está validado automaticamente
    return ResponseEntity.ok().build();
}
```

### Anotações OpenAPI (opcional)

Os endpoints são documentados automaticamente no Swagger a partir de suas assinaturas e tipos de retorno. Para melhorar a documentação, use anotações:

```java
@PostMapping
@Operation(summary = "Criar novo usuário", description = "Cria um usuário com e-mail único")
public ResponseEntity<?> criar(@RequestBody UsuarioDto dto) {
    // ...
}
```

> **Nota:** `@SecurityRequirement(name = "sessao")` é desnecessário em operações individuais, pois `OpenApiConfig` já aplica o requisito de segurança "sessao" globalmente a todos os endpoints.

### Sessão vs. Token JWT

Hoje a autenticação é **por sessão HTTP** (cookie `JSESSIONID`), não por JWT. Tokens JWT **não são usados** e não devem ser adicionados sem decisão de arquitetura (ADR).

## Desabilitar Swagger

Para desabilitar a documentação OpenAPI e o Swagger UI em produção:

```bash
SPRINGDOC_ENABLED=false ./mvnw spring-boot:run
```

Ou em `docker-compose.yml` (adicione ao serviço `app`):
```yaml
app:
  environment:
    SPRINGDOC_ENABLED: "false"
```

## Veja também

- [Rotas e segurança](./rotas-e-seguranca.md)
- [Adicionar um endpoint REST](../guias/adicionar-um-endpoint-rest.md)
- [Autenticação e sessões](../explicacoes/autenticacao-e-sessoes.md)
