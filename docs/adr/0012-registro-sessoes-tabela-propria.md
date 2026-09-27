# 0012 — Tabela Própria `sessoes` com SHA-256 do ID

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-24 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-user-authentication`](../../openspec/changes/archive/2026-09-25-add-user-authentication/) |

## Contexto e problema

Sessões HTTP precisa de rastreabilidade: quem entrou, quando, de onde (IP), que dispositivo (User-Agent). Spring oferece múltiplas formas: (1) `HttpSession` em memória (padrão, sem persistência), (2) Spring Session JDBC (tabelas `SPRING_SESSION`), (3) tabela customizada. Requisito é auditoria explícita (tabela própria), não externalização de estado.

## Direcionadores da decisão

- Auditoria: registrar IP, User-Agent, data/hora de abertura e fechamento
- Não-mutilação do esquema: tabela `sessoes` própria (não `SPRING_SESSION*` do Spring Session)
- Segurança: armazenar hash (SHA-256) do ID da sessão, não o ID utilizável
- Recuperação: rastrear sessão ativa a partir do hash
- Evento de logout/expiração: aplicação precisa fechar o registro

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Tabela `sessoes` com SHA-256 do ID** | Migração V1 cria tabela com token (SHA-256 hex), IP, User-Agent, data_fim (nullable). `AuthenticationSuccessHandler` registra abertura. Listener de `HttpSessionDestroyedEvent` fecha. |
| Spring Session JDBC | Usa tabelas padrão `SPRING_SESSION`, `SPRING_SESSION_ATTRIBUTES`. Rejeitada: impõe esquema que não alinha com requisitos de auditoria simplificada. |
| Sem registro | Apenas conta de login no log de aplicação. Rejeitada: não atende requisito de auditoria. |

## Resultado da decisão

Adotou-se **tabela customizada `sessoes`**:

1. **Migração V1** (`db/migration/V1__controle_acesso.sql`):
   ```sql
   CREATE TABLE sessoes (
       id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       usuario_id bigint NOT NULL REFERENCES usuarios(id),
       token varchar(64) NOT NULL UNIQUE, -- SHA-256 hex (64 chars)
       ip varchar(45),                    -- IPv6 max
       dispositivo varchar(500),          -- User-Agent
       criado_em timestamptz NOT NULL,
       criado_por varchar(150) NOT NULL,
       data_inicio timestamptz NOT NULL,
       data_fim timestamptz,              -- NULL = aberta
       alterado_em timestamptz NOT NULL,
       alterado_por varchar(150) NOT NULL,
       INDEX idx_usuario (usuario_id),
       INDEX idx_token (token)
   );
   ```

2. **Abertura**: `RegistroSessaoSuccessHandler` (extends `SavedRequestAwareAuthenticationSuccessHandler`)
   - Chamado após troca de ID de sessão (seguro)
   - Calcula `SHA256(sessionId)` → `token`
   - Insere: `usuario_id`, `token`, `ip` (request.getRemoteAddr()), `dispositivo` (User-Agent truncado), `data_inicio=now()`
   - Erros → log + engolir (não impede login)

3. **Fechamento**: `SessaoEncerradaListener` (`@EventListener` de `HttpSessionDestroyedEvent`)
   - Evento disparado por servlet container (logout manual ou expiração)
   - Calcula `SHA256(sessionId)`, atualiza `data_fim = now()`
   - Ausência de usuário no contexto → `alterado_por='sistema'`

4. **Startup cleanup**: `SessoesAbertasRunner` (`ApplicationRunner`)
   - Na inicialização, fecha todos os registros com `data_fim is null`
   - Cobre caso de crash anterior (sessões em memória se perdem)

### Consequências positivas
- **Auditória explícita**: IP, User-Agent, data/hora de login/logout
- **Segurança**: hash SHA-256 do ID impede vazamento de sessão ativa se tabela for comprometida
- **Recuperação**: falha na aplicação não deixa "buracos" (startup cleanup)
- **Simplificado**: não requer Spring Session, menos configuração

### Consequências negativas
- **Sem histórico completo**: apenas última abertura (não todas as ações)
- **Sem sincronização de múltiplas instâncias**: se houver várias app instances, race condition ao fechar (mitigado: projeto atual = 1 instância)
- **Imprecisão de timestamp**: usa data do servidor (sem timezone-aware clock injetável)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Tabela `sessoes` customizada | Esquema simples; auditória explícita; controle total. | Sem histórico granular; múltiplas instâncias precisam sincronização. |
| Spring Session JDBC | Padrão; externaliza estado. | Impõe schema próprio; overhead (não necessário para atual). |
| Sem registro | Simples. | Não atende requisito de auditória. |

## Mais informações

- **Design**: [`add-user-authentication/design.md` §Decisions 9](../../openspec/changes/archive/2026-09-25-add-user-authentication/design.md)
- **Migração**: [`src/main/resources/db/migration/V1__controle_acesso.sql`](../../src/main/resources/db/migration/V1__controle_acesso.sql)
- **Código**:
  - [`seguranca/RegistroSessaoSuccessHandler.java`](../../src/main/java/com/example/loginbase/seguranca/RegistroSessaoSuccessHandler.java)
  - [`seguranca/SessaoEncerradaListener.java`](../../src/main/java/com/example/loginbase/seguranca/SessaoEncerradaListener.java)
  - [`seguranca/SessoesAbertasRunner.java`](../../src/main/java/com/example/loginbase/seguranca/SessoesAbertasRunner.java)

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
