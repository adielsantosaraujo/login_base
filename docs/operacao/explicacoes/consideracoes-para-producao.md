---
titulo: Considerações para produção
publico: operacao
tipo: explicacao
atualizado_em: 2026-10-04
fontes:
  - src/main/resources/application.properties
  - src/main/java/com/example/loginbase/seguranca/SecurityConfig.java
  - src/main/java/com/example/loginbase/seguranca/SessaoService.java
  - src/main/java/com/example/loginbase/seguranca/AdminInicialRunner.java
---

# Considerações para produção

## Contexto

Este projeto é uma **base de autenticação reutilizável** (Spring Boot com segurança, sessões e auditoria). Ele foi desenvolvido com padrões seguros, mas nem todas as configurações e decisões arquiteturais foram feitas com produção em mente. Esta página documenta os pontos que devem ser verificados ou ajustados antes de colocar uma aplicação baseada nele em produção.

## Ambiente de produção

O repositório não define um profile Spring de produção (`prod`), nem um Kubernetes manifest. O Dockerfile atual é multi-stage e funciona em produção, com otimizações básicas de layer caching (separa `dependency:go-offline` em camada própria e usa `--mount=type=cache` para o repositório Maven). As considerações abaixo tratam do que o código permite ou exige.

## Configurações críticas

### Segurança da sessão (HTTPS)

**Código:** `application.properties`

```properties
server.servlet.session.cookie.secure=${SESSION_COOKIE_SECURE:false}
```

**Ação necessária:** Em produção com HTTPS, defina:

```
SESSION_COOKIE_SECURE=true
```

Isto marca o cookie de sessão como `Secure`, impedindo que navegadores o enviem via HTTP não-criptografado.

### IP real atrás de proxy

**Código:** `application.properties` — nenhuma configuração de forward headers.

**Situação:** Hoje o `RemoteAddr` da requisição reflete o IP direto do cliente. Se a aplicação estiver atrás de um proxy/load balancer, o IP será sempre o do proxy.

**Ação recomendada:** Se em produção houver proxy/load balancer:

```properties
server.forward-headers-strategy=native
```

Isto permite que o Spring leia `X-Forwarded-For` (ou similar). Ative apenas se confiar na origem do header (proxy interno).

### Documentação API (Swagger)

**Código:** `application.properties`

```properties
springdoc.api-docs.enabled=${SPRINGDOC_ENABLED:true}
springdoc.swagger-ui.enabled=${SPRINGDOC_ENABLED:true}
```

**Ação recomendada:** Em produção, se não quiser expor a API ou se a API for apenas interna:

```
SPRINGDOC_ENABLED=false
```

Isto desativa o Swagger UI (`/swagger-ui.html`) e o OpenAPI JSON (`/v3/api-docs`). Ambos exigem login; mesmo assim, desabilitar em produção reduz a superfície de ataque.

### Senhas fortes

**Código:** `seguranca/AdminInicialRunner.java` — usa `PasswordEncoder` (bcrypt).

**Ação necessária:** 

- Defina `ADMIN_PASSWORD` com uma senha muito forte (20+ caracteres, mix de maiúsculas, minúsculas, números, símbolos).
- Defina `DB_PASSWORD` com uma senha muito forte também.

Não use padrões como `login_base` ou `admin@loginbase.local`.

### Timeout de sessão

**Código:** `application.properties`

```properties
server.servlet.session.timeout=${SESSION_TIMEOUT:30m}
```

**Ação recomendada:** Em produção, avaliar o timeout apropriado. `30 minutos` (padrão) é razoável para aplicações web internas. Para públicas, considere `15m` ou menos.

```
SESSION_TIMEOUT=15m
```

## Limitações de design

### Uma única instância

**Código:** Registro de sessões em tabela `sessoes` sem suporte a múltiplas instâncias.

**Limitação:** O projeto foi desenhado com a premissa de **uma instância única** da aplicação. Não há sincronização de sessões entre múltiplas instâncias.

**Impacto:** Se escalar horizontalmente (múltiplas pods/containers), cada instância terá seu próprio registro de sessão em memória. O banco terá os registros, mas a sessão ativa pode estar em uma instância e a requisição chegar em outra.

**Soluções futuras:**
- Implementar Spring Session com backend JDBC ou Redis.
- Usar JWT em vez de sessão HTTP (requer redesenho).

### Sem bloqueio por tentativas

**Código:** `seguranca/UsuarioDetailsService.java` — simples busca e comparação de senha.

**Limitação:** Não há proteção contra ataques de força bruta. Alguém pode fazer milhares de tentativas de login sem limite.

**Impacto:** Vulnerabilidade a brute force.

**Solução futura:** Implementar bloqueio progressivo (e.g., após N falhas em T minutos).

### IP do cliente atrás de proxy

**Código:** `seguranca/RegistroSessaoSuccessHandler.java` — registra `request.getRemoteAddr()` diretamente.

**Limitação:** Se estiver atrás de proxy/load balancer, o IP registrado será sempre o do proxy.

**Impacto:** Histórico de sessões não refletirá o IP real do cliente.

**Solução futura:** Ler `X-Forwarded-For` se em ambiente de proxy (e configurar `server.forward-headers-strategy`).

## Checklist pré-produção

- [ ] `SESSION_COOKIE_SECURE=true` se HTTPS.
- [ ] `SPRINGDOC_ENABLED=false` se não precisa do Swagger.
- [ ] `ADMIN_PASSWORD` definida com senha forte.
- [ ] `DB_PASSWORD` definida com senha forte.
- [ ] `SESSION_TIMEOUT` ajustado conforme política de segurança.
- [ ] `server.forward-headers-strategy` configurado se atrás de proxy.
- [ ] Backup do volume `db-data` configurado (a confirmar com o responsável).
- [ ] Logs sendo capturados e armazenados (e.g., agregador de logs centralizado).
- [ ] Certificado SSL/TLS válido se HTTPS.
- [ ] Teste de carga realizado para dimensionar recursos.
- [ ] Plano de disaster recovery e rollback documentado.

## Dados pessoais (LGPD/GDPR)

**Dados armazenados:**
- Nome, e-mail, celular (normalizados a 11 dígitos).
- Hash SHA-256 de sessões (não permite recuperar ID da sessão).
- IP origem, User-Agent da sessão.
- Timestamps de criação/alteração de registros.

**Política de retenção:**

> **A confirmar com o responsável:** Qual é a política de retenção de dados? Quando deletar registros de usuários inativos? Como exportar dados para conformidade LGPD/GDPR?

**Ações recomendadas:**
- Documentar política de retenção conforme regulações aplicáveis.
- Implementar rotina de limpeza de dados expirados (a confirmar).
- Implementar endpoints de export/deleção de dados (a confirmar).

## Veja também

- [`docs/desenvolvimento/explicacoes/autenticacao-e-sessoes.md`](../../desenvolvimento/explicacoes/autenticacao-e-sessoes.md) — detalhes técnicos de sessões.
- [`docs/operacao/referencia/variaveis-de-ambiente.md`](../referencia/variaveis-de-ambiente.md) — como configurar cada variável.
