---
titulo: Login por email ou celular normalizados
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-09-24
atualizado_em: 2026-10-04
fontes:
  - src/main/java/com/example/loginbase/acesso/Usuario.java
  - src/main/java/com/example/loginbase/acesso/NormalizacaoContato.java
  - src/main/java/com/example/loginbase/seguranca/IdentificadorLogin.java
  - src/main/java/com/example/loginbase/seguranca/UsuarioDetailsService.java
  - src/main/resources/db/migration/V1__controle_acesso.sql
  - src/main/resources/application.properties
---

# 0007 — Login por email ou celular normalizados

**Status:** Aceita · **Data:** 2026-09-24

## Contexto

A aplicação precisa permitir login flexível (e-mail ou celular) sem duplicar registros quando o usuário digita variações (maiúsculas, espaços, máscaras de telefone). É necessário escolher como normalizar e validar esses identificadores.

## Decisão

- **Campo único:** `usuarios.email` e `usuarios.celular` são únicos (não null para e-mail, nullable para celular).
- **Normalização:** 
  - E-mail: minúsculas + sem espaços (setter/`@PrePersist`).
  - Celular: apenas dígitos (setter/`@PrePersist`), exatamente 11 dígitos (validação com `IllegalArgumentException` e `check (celular ~ '^[0-9]{11}$')` no banco).
- **Índice case-insensitive:** `CREATE UNIQUE INDEX ux_usuarios_email_lower ON usuarios (lower(email))` (V1__controle_acesso.sql) para garantir case-insensitivity na busca.
- **Login:** o usuário digita qualquer coisa no campo "E-mail ou celular".
  - Se contém `@` → normaliza e busca por e-mail.
  - Se não contém `@`, remove não-dígitos e, se sobrarem **exatamente 11 dígitos**, busca por celular.
  - Caso contrário → `UsernameNotFoundException` sem consultar o banco (evita enumeração).
- **`UsuarioDetailsService`:** retorna `User` com `username = email` (sempre o e-mail, mesmo que o login tenha sido pelo celular), `password = hash`, e `authorities = ROLE_<perfil> + nomes das permissões`. A mensagem de erro é genérica ("Usuário ou senha inválidos.") para todos os casos (usuário não encontrado, senha errada, conta desabilitada).

## Alternativas descartadas

- **Celular em formato E.164** (`+55...`) — Rejeitada porque o requisito é DDD + número com 11 dígitos, e guardar só dígitos simplifica a busca e evita masks diferentes.
- **Máscara visual do celular** (ex.: `(11) 9999-9999`) — Rejeitada porque a máscara varia por país e adiciona complexidade.

## Consequências

### Positivas

- **Flexibilidade:** o usuário consegue logar digitando o e-mail em maiúsculas, o celular com máscara ou parênteses, ou sem máscara.
- **Segurança simples:** mensagem genérica evita enumeração de usuários (não é possível saber se um e-mail existe).
- **Username único na sessão:** sempre o e-mail, independentemente de como o usuário logou. Simplifica auditoria, `AuditorAware` e principal da sessão.
- **Validação no banco:** o `check` do Postgres garante a regra mesmo para gravações fora da aplicação.

### Negativas

- **Celular é reatribuível:** a operadora pode atribuir um número a outra pessoa. Mitigado pela senha sendo necessária; o cadastro futuro deve permitir atualizar celular.
- **Facilita força bruta com celular:** é possível tentar números sequenciais (11 dígitos). Mitigado pela mensagem genérica (non-goal desta change; bloqueio por tentativas é candidato a mudança futura).
- **Campo celular pode ficar nulo:** um usuário sem celular cadastrado não consegue logar pelo telefone. Intencional; login por e-mail sempre funciona.
- **Dois identificadores únicos no login:** é levemente menos usual que só e-mail. A documentação orienta; os testes cobrem ambos os caminhos.

