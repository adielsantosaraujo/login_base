# 0013 — Administrador Inicial por Variáveis de Ambiente

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-24 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-user-authentication`](../../openspec/changes/archive/2026-09-25-add-user-authentication/) |

## Contexto e problema

Todo sistema precisa de um administrador inicial para bootstrap. Há múltiplas formas: (1) INSERT SQL em migração (senha fixa, no repositório), (2) variáveis de ambiente, (3) wizard interativo. Variáveis de ambiente são seguras (não no git) e explícitas, permitindo diferentes credenciais por ambiente (dev, staging, prod).

## Direcionadores da decisão

- Segurança: senha não deve estar no repositório ou em SQL
- Flexibilidade: diferentes ambientes (dev, prod) têm senhas diferentes
- Simplicidade: ApplicationRunner não requer wizard interativo
- Idempotência: não alterar admin existente (safe to re-run)
- Documentação: `.env.example` mostra what's needed

## Opções consideradas

| Opção | Descrição |
|---|---|
| **ApplicationRunner com `ADMIN_EMAIL`/`ADMIN_PASSWORD`** | `AdminInicialRunner` lê variáveis, cria usuário + perfil ADMIN se não existir. Senha vazia = skip com warn. |
| Migração SQL com INSERT | Seed SQL em `V2__perfil_admin.sql`. Rejeitada: senha fixa ou omitida. |
| Wizard interativo | `CommandLineRunner` que pede dados. Rejeitada: não é automático, manual em cada inicialização. |

## Resultado da decisão

Adotou-se **`AdminInicialRunner`** (`ApplicationRunner` transacional):

1. **Configuração** (`application.properties`):
   ```properties
   app.admin.email=${ADMIN_EMAIL:admin@loginbase.local}
   app.admin.password=${ADMIN_PASSWORD:}
   ```

2. **Implementação** (`seguranca.AdminInicialRunner`):
   - Lê `appAdminEmail` e `appAdminPassword`
   - Se `password` vazio → `log.warn` e retorna (skip silencioso)
   - Se usuário com email já existe → não altera (idempotente)
   - Senão:
     - Cria `Usuario(nome="Administrador", email, password codificada, sem celular)`
     - Cria `UsuarioPerfil` vigente desde `LocalDate.now()` para perfil `ADMIN` (criado em V2)
     - Auditoria: `criado_por='sistema'`

3. **Variáveis de ambiente** (`.env`, não versionado):
   ```
   ADMIN_EMAIL=admin@example.com
   ADMIN_PASSWORD=sua_senha_aqui
   ```

4. **Exemplo** (`.env.example`, versionado):
   ```
   ADMIN_EMAIL=admin@loginbase.local
   ADMIN_PASSWORD=
   ```

### Consequências positivas
- **Seguro**: senha fora do repositório, em variável de ambiente
- **Flexível**: diferentes senhas por ambiente (dev, staging, prod)
- **Idempotente**: roda várias vezes sem erro (update é skip)
- **Gracioso**: senha vazia = skip com log (não falha)
- **Simples**: nenhuma interação manual

### Consequências negativas
- **Sem feedback visual**: desenvolvedor precisa verificar log para confirmar criação
- **Sem wizard**: senha deve estar em `.env` ou CLI (não é interativa)
- **Timeout de transação**: se runner falhar, aplicação falha no startup

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| ApplicationRunner + variáveis | Seguro; flexível; idempotente; automático. | Sem feedback visual; requer `.env`. |
| SQL seed em migração | Simples; no git. | Senha no repositório (inseguro) ou omitida. |
| Wizard interativo | Flexível; captura senha segura. | Manual; não é automático. |

## Mais informações

- **Design**: [`add-user-authentication/design.md` §Decisions 11](../../openspec/changes/archive/2026-09-25-add-user-authentication/design.md)
- **Código**: [`seguranca/AdminInicialRunner.java`](../../src/main/java/com/example/loginbase/seguranca/AdminInicialRunner.java)
- **Configuração**: [`src/main/resources/application.properties`](../../src/main/resources/application.properties) — `app.admin.*`
- **Exemplo**: [`.env.example`](../../.env.example) — `ADMIN_EMAIL`, `ADMIN_PASSWORD`

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
