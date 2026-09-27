# 07 — Segurança

| Campo | Valor |
|---|---|
| Versão | 1.0.0 |
| Data | 2026-09-27 |
| Status | Vigente — baseline do commit `454ae58` |
| Modelo/norma | STRIDE, OWASP ASVS 4.0 L1 (seleção), LGPD |
| Público | Desenvolvedores, revisores de segurança |
| Fontes | `src/main/java/com/example/loginbase/seguranca/*.java`, `src/main/resources/application.properties`, `frontend/src/App.vue`, design jogo §18 |

> Parte da [documentação do login_base](README.md). Modela ameaças por STRIDE, verifica conformidade com ASVS nível 1 e registra compliance com LGPD.

---

## Índice

1. [Autenticação e Autorização](#autenticação-e-autorização)
2. [Sessões e Tokens](#sessões-e-tokens)
3. [Proteção CSRF](#proteção-csrf)
4. [STRIDE — Análise de Ameaças](#stride--análise-de-ameaças)
5. [OWASP ASVS L1 — Checklist](#owasp-asvs-l1--checklist)
6. [LGPD — Compliance](#lgpd--compliance)

---

## Autenticação e Autorização

### Estratégia de Autenticação

**Modelo:** Autenticação por Formulário (Form Login) — Spring Security  

- Usuários fazem login via POST `/login` com e-mail/celular e senha
- Identidade armazenada em sessão HTTP (cookie `JSESSIONID`)
- Senha armazenada como hash bcrypt (delegado)

**Fluxo:**

```
1. Usuário acessa GET /login → PaginaController retorna template Thymeleaf
2. Usuário preenche formulário (e-mail/celular + senha + _csrf)
3. POST /login → Spring Security valida credenciais
   ✓ Válidas → Cria sessão, registra em `sessoes`, redireciona para / (GET)
   ✗ Inválidas → Redireciona para /login?error
4. Usuários autenticados: à cada requisição, Spring injeta Authentication
5. POST /logout (com CSRF) → invalida a sessão; o `SessaoEncerradaListener` grava `data_fim`; redireciona para /login?logout
```

### Resolução de Identidade

- Identificador de login: **E-mail** (normalizado, case-insensitive) ou **Celular** (11 dígitos)
- Spring Security aceita ambos via `seguranca/UsuarioDetailsService` + `IdentificadorLogin` (classifica e normaliza e-mail/celular)
- Após login: `Authentication.getName()` = e-mail normalizado
- Controllers acessam ID do usuário via `UsuarioAtual.id(auth)` → `UsuarioRepository.findByEmail()`

### Controle de Acesso Baseado em Papel (RBAC)

**Tabelas:** `perfis`, `permissoes`, `usuario_rel_perfis`, `perfis_rel_permissoes`

```
Usuário → (usuário_rel_perfis) → Perfil → (perfis_rel_permissoes) → Permissão
```

**Vigência Temporal:**
- Cada associação usuário-perfil tem `data_inicial` e `data_final`
- Perfis expirados são ignorados em validações

**Implementação Atual:**
- Apenas perfil `ADMIN` seed (V2)
- Endpoints de jogo (`/api/jogo/**`) verificam autenticação, não autorização (todos os usuários autenticados podem acessar sua própria vila)
- Endpoints administrativos: não implementados nesta release

### Isolamento de Dados por Usuário

Cada usuário só acessa **seus próprios** recursos:

- **Vila:** `jogo_vilas.usuario_id` = ID do usuário autenticado
  - `VilaController.consultarVila()` valida via `UsuarioAtual.id(auth)`
  - `MasmorraService` lança `RecursoNaoEncontradoException` (404) se a batalha não pertencer à vila do usuário

---

## Sessões e Tokens

### Gerenciamento de Sessões HTTP

**Cookie de Sessão:** `JSESSIONID`
- **Atributos:** `HttpOnly`, `SameSite=Lax`; `Secure` configurável por `SESSION_COOKIE_SECURE` (padrão `false`)
- **Duração:** Configurável via `server.servlet.session.timeout` (padrão: 30 minutos)
- **Fixação de Sessão:** Mitigada por `sessionFixation().changeSessionId()` em `SecurityConfig`
  - Após login bem-sucedido, Spring gera novo ID de sessão

**Registro Auditável:** Tabela `sessoes`

| Campo | Preenchimento |
|-------|---|
| `usuario_id` | Extraído de `Authentication` |
| `data_inicio` | Timestamp de login |
| `data_fim` | Preenchido ao destruir a sessão (logout ou expiração) ou no startup |
| `token` | SHA-256 (hex) do ID da sessão |
| `ip` | `request.getRemoteAddr()` (sem tratamento de X-Forwarded-For) |
| `dispositivo` | User-Agent da requisição |

**Comportamento:**
- Sessão criada: `RegistroSessaoSuccessHandler` registra em `sessoes` após autenticação bem-sucedida
- Sessão expirada: `SessaoEncerradaListener` preenche `data_fim`; sessões que ficaram abertas são fechadas por `SessoesAbertasRunner` no startup
- Logout: `data_fim` preenchido explicitamente

### Cache de Requisição (Request Cache)

- **Comportamento:** Após login, Spring redireciona usuário para a requisição original (não autenticada)
- **Configuração:** Desabilitado para `/api/**` (APIs devem ser idempotentes; não salvar estado)
- **Efeito:** Usuário anonimato que tenta acessar API recebe 401, não é redirecionado para login

---

## Proteção CSRF

### Defesa contra Cross-Site Request Forgery

**Configuração:** `SecurityConfig.csrf().spa()`
- Estratégia SPA (Single-Page Application) — essencial para Vue 3/PrimeVue

### Token XSRF — Fluxo

```
1. Usuário autenticado faz requisição GET /api/** ou acessa página com formulário
2. Spring Security expõe o token no cookie `XSRF-TOKEN` legível por JavaScript (não HttpOnly)
   Cookie: XSRF-TOKEN=<token_aleatorio>
3. Frontend (Vue 3) lê cookie XSRF-TOKEN
   → Outra origem não consegue ler o cookie nem enviar o header; o cookie de sessão é SameSite=Lax
4. Requisição POST/PUT/DELETE → Frontend inclui token em cabeçalho
   Header: X-XSRF-TOKEN=<token_do_cookie>
5. Spring valida: cabeçalho X-XSRF-TOKEN deve = cookie XSRF-TOKEN
   ✓ Válido → Processa requisição
   ✗ Inválido/Ausente → 403 Forbidden
```

### Formulário de Login

- Token CSRF gerado por Thymeleaf via `th:action="@{/login}"`
- Campo oculto: `<input type="hidden" name="_csrf" th:value="${_csrf.token}" />`
- Validação automática: Spring intercepta POST /login

### Endpoints Afetados

**CSRF Requerido:**
- `POST /login` — Parâmetro `_csrf` (formulário)
- `POST /logout` — Campo `_csrf` (formulário gerado pela SPA)
- Todas as ações: `POST /api/jogo/**`

**CSRF Não Aplicável:**
- `GET /` — Leitura de dados
- `GET /api/jogo/vila` — Leitura de dados
- `GET /api/jogo/catalogo` — Leitura de dados
- `GET /api/jogo/batalhas/{id}` — Leitura de dados

---

## STRIDE — Análise de Ameaças

### Spoofing

**Ameaça:** Atacante finge ser outro usuário  
**Mitigação:**
- ✓ Autenticação por e-mail/celular + bcrypt
- ✓ Sessão HTTP segura (HttpOnly, SameSite; Secure desligado em dev)
- ⚠ TLS/HTTPS não configurado (sem ambiente de produção)
- ⚠ Recomendação: Multi-factor authentication (2FA) em futuras versões

---

### Tampering

**Ameaça:** Atacante modifica dados em trânsito ou em repouso  
**Mitigação:**
- ⚠ TLS/HTTPS não configurado em dev (recomendado em produção)
- ✓ CSRF token em estado modificador (POST/PUT/DELETE)
- ✓ Validação de entrada: `@Valid`, enums, limites numéricos
- ✓ Lock pessimista por vila + `@Version` na batalha previnem race conditions
- ✓ Spring Data JPA audita alterações (`criado_em/por`, `alterado_em/por`)
- ⚠ Recomendação: Assinatura de API (ex.: HMAC) para APIs públicas

---

### Repudiation

**Ameaça:** Usuário nega ter executado uma ação  
**Mitigação:**
- ✓ Logs de auditoria em `sessoes`, `usuario_rel_perfis` (alterado_em/por)
- ✓ Tabelas de domínio (`jogo_*`) registram `alterado_em/por` em toda alteração
- ✓ Timestamp imutável (`timestamptz` UTC)
- ⚠ Recomendação: Log centralizado (ELK, Splunk) em produção

---

### Information Disclosure

**Ameaça:** Exposição não autorizada de dados sensíveis  
**Mitigação:**
- ✓ Isolamento por usuário: cada usuário só acessa sua vila
- ✓ Senhas hasheadas (bcrypt)
- ✓ Sessão não expõe token em URL (apenas cookie)
- ✓ Mensagens de erro genéricas (404 para vila não encontrada, não "usuário X não existe")
- ⚠ Recomendação: Rate limiting em endpoints sensíveis (/login)
- ⚠ Recomendação: Mascarar IDs de usuário/vila em APIs (usar slugs)

---

### Denial of Service (DoS)

**Ameaça:** Atacante sobrecarrega servidor com requisições  
**Mitigação:**
- ✓ Validação de entrada (tamanho máximo de arrays, strings)
- ⚠ Limite de tentativas de login: não implementado
- ⚠ Recomendação: Rate limiting global (ex.: Spring Cloud Gateway, WAF)
- ⚠ Recomendação: Circuit breaker para serviços críticos

---

### Elevation of Privilege

**Ameaça:** Usuário comum obtém acesso administrativo  
**Mitigação:**
- ⚠ Perfis vigentes viram autoridades `ROLE_<perfil>`, mas nenhuma rota exige perfil/permissão (autorização por permissão é Non-Goal)
- ✓ Endpoints de jogo não expõem funcionalidades admin
- ✓ Parâmetros de requisição não podem alterar role (ex.: não há `?role=ADMIN`)
- ⚠ Recomendação: Auditoria regular de permissões atribuídas

---

## OWASP ASVS L1 — Checklist

**Nível:** 1 (Basic)  
**Escopo:** Autenticação, Sessão, Validação de Entrada, Criptografia

### V2 — Autenticação

| ID | Controle | Status | Nota |
|----|----------|--------|------|
| 2.1 | Força de senha mínima | ✗ | Não há política de senha (não existe cadastro; admin definido por variável) |
| 2.2 | Proteção contra brute-force login | ⚠ | Não implementado — adicionar rate limiting |
| 2.3 | Recuperação de conta segura | ⚠ | Não implementado — usuário perde acesso permanentemente |
| 2.4 | Logout efectivo | ✓ | Invalida sessão, registra data_fim |
| 2.5 | Senhas hasheadas (não plain-text) | ✓ | bcrypt delegado (Spring Security) |

### V3 — Gerenciamento de Sessão

| ID | Controle | Status | Nota |
|----|----------|--------|------|
| 3.1 | Cookie seguro (Secure, HttpOnly) | ⚠ | HttpOnly sim; Secure só com SESSION_COOKIE_SECURE=true |
| 3.2 | SameSite=Lax em cookies | ✓ | Proteção contra CSRF cross-site |
| 3.3 | Invalidação após logout | ✓ | Sessão destruída, cookie deletado |
| 3.4 | Timeout de inatividade | ✓ | Configurável (padrão 30 min) |
| 3.5 | Proteção contra fixation | ✓ | `sessionFixation().changeSessionId()` |

### V5 — Validação de Entrada

| ID | Controle | Status | Nota |
|----|----------|--------|------|
| 5.1 | Validação de tipo | ✓ | `@Valid`, enums, `@Min/@Max` |
| 5.2 | Tamanho de string/array | ✓ | `@Size`, `@NotEmpty`, limites de cultivo |
| 5.3 | Rejeição de entrada malformada | ✓ | `HttpMessageNotReadableException` → 400 |
| 5.4 | SQL Injection (prevenção) | ✓ | Spring Data JPA (prepared statements) |
| 5.5 | XSS (prevenção) | ✓ | Response JSON (não HTML inline), Thymeleaf escapa |

### V6 — Criptografia

| ID | Controle | Status | Nota |
|----|----------|--------|------|
| 6.1 | Criptografia em trânsito (TLS) | ⚠ | Não configurado em dev |
| 6.2 | Certificado válido | ⚠ | Depende de infraestrutura (Let's Encrypt, etc.) |
| 6.3 | Senhas hasheadas | ✓ | bcrypt (salt + iterações) |
| 6.4 | Chaves de criptografia protegidas | ⚠ | Spring Security key store (configurar em produção) |

### Resumo

| Categoria | Conformidade |
|-----------|---|
| Autenticação | ✓ Parcial (sem 2FA, rate limiting) |
| Sessão | ✓ Completo |
| Validação | ✓ Completo |
| Criptografia | ✓ Parcial (TLS recomendado em produção) |

**Nível ASVS L1:** atendimento parcial — faltam proteção contra força bruta (V2.2), TLS e política de senha  
**Recomendações para L2+:** 2FA, rate limiting, HSTS, CSP

---

## LGPD — Compliance

**Lei Geral de Proteção de Dados Pessoais (Brasil)**  
**Escopo:** V1–V3 (controle de acesso, jogo)

### Dados Pessoais Coletados

| Tabela | Campo | Tipo | Finalidade | Base Legal |
|--------|-------|------|-----------|-----------|
| `usuarios` | `nome` | PII | Identificação do usuário | Contrato |
| `usuarios` | `email` | PII | Autenticação, contato | Contato |
| `usuarios` | `celular` | PII | Login alternativo | Contato |
| `usuarios` | `senha` | PII (hash) | Autenticação | Contrato |
| `sessoes` | `ip` | PII | Auditoria de acesso | Interesse legítimo |
| `sessoes` | `dispositivo` | PII (User-Agent) | Auditoria de acesso | Interesse legítimo |

### Direitos do Titular (LGPD Art. 18)

| Direito | Implementação | Status |
|--------|---|--------|
| Acesso | Endpoint `GET /api/jogo/vila` expõe dados do usuário | ✓ Possível |
| Correção | Usuário pode alterar `usuarios.nome` (não implementado) | ⚠ Recomendado |
| Exclusão (Right to Be Forgotten) | Remoção de `usuarios`, cascade em `sessoes`, `usuario_rel_perfis` | ⚠ Recomendado |
| Portabilidade | Exportação de dados de jogo (não implementado) | ⚠ Recomendado |
| Consentimento | Aceitar LGPD em `/login` (não implementado) | ⚠ Recomendado |

### Segurança de Dados

| Controle | Status | Nota |
|----------|--------|------|
| Criptografia em repouso | ⚠ | Senhas hasheadas; emails/IPs em plain-text (PostgreSQL) |
| Criptografia em trânsito | ⚠ | HTTPS recomendado em produção |
| Backup/Recuperação | ⚠ | Não documentado nesta release |
| Auditoria | ✓ | Logs de `criado_em/por`, `alterado_em/por` em todas as tabelas |
| Retenção de Dados | ⚠ | Política não definida (ex.: deletar sessões expiradas após 90 dias?) |

### Aviso de Privacidade

**Obrigatório no Brasil:**
- Informar quais dados são coletados
- Qual é a finalidade
- Qual é a base legal
- Como o usuário exerce seus direitos

**Recomendado:**
- Página `/privacidade` ou `/lgpd` com termos
- Checkbox de consentimento ao criar conta
- Política de retenção de dados

### Implementações Recomendadas

1. **Direito ao Esquecimento:**
   ```java
   DELETE FROM jogo_batalhas WHERE vila_id IN (SELECT id FROM jogo_vilas WHERE usuario_id = ?)
   DELETE FROM jogo_vilas WHERE usuario_id = ?
   DELETE FROM sessoes WHERE usuario_id = ?
   DELETE FROM usuario_rel_perfis WHERE usuario_id = ?
   DELETE FROM usuarios WHERE id = ?
   ```

2. **Aviso de Privacidade:**
   - Exigir aceite em `/login`
   - Manter registro de consentimento

3. **Política de Retenção:**
   - Sessões expiradas: deletar após 30 dias
   - Dados de usuário: deletar após período de inatividade (ex.: 1 ano)

4. **Exportação de Dados:**
   - Endpoint `GET /api/usuario/exportar` retorna JSON com dados pessoais e de jogo

---

## Checklist de Segurança para Deploy

- [ ] HTTPS habilitado (TLS 1.2+)
- [ ] HSTS header configurado (`Strict-Transport-Security`)
- [ ] CSP (Content Security Policy) configurado
- [ ] Rate limiting ativado em `/login`
- [ ] Logs centralizados (ELK, CloudWatch)
- [ ] Monitoramento de erros (Sentry, DataDog)
- [ ] Backup automático de banco de dados
- [ ] Política de LGPD publicada em `/privacidade`
- [ ] Teste de penetração realizado
- [ ] Certificado TLS válido e renovação automatizada
- [ ] WAF (Web Application Firewall) em frente à aplicação
- [ ] Secrets management (senhas de BD, chaves) fora do código
- [ ] Rotate de chaves de sessão periodicamente

---

## Referências

- **OWASP Top 10 (2021):** https://owasp.org/Top10/
- **OWASP ASVS:** https://owasp.org/www-project-application-security-verification-standard/
- **STRIDE:** https://en.wikipedia.org/wiki/STRIDE_(security)
- **LGPD (Lei 13.709/2018):** http://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709.htm
- **Spring Security Docs:** https://spring.io/projects/spring-security
- **CWE (Common Weakness Enumeration):** https://cwe.mitre.org/

---

**Código Relevante:**
- `../src/main/java/com/example/loginbase/seguranca/SecurityConfig.java` — Configuração de segurança
- `../src/main/java/com/example/loginbase/acesso/` — Entidades de autenticação
- `../src/main/java/com/example/loginbase/jogo/api/ErroApiHandler.java` — Tratamento de erros
- `../src/main/resources/application.properties` — Configuração de session timeout, HTTPS, etc.
- `../src/main/resources/templates/sistema/public/login.html` — Formulário de login com CSRF

---

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
