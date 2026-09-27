# 0019 — Estado/Log/Loot da Batalha em JSON em Colunas `text`

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-26 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-city-builder-game`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/) |

## Contexto e problema

Batalha é complexa: estado (combatentes, posições, HP, flags), log de ações (textual), loot (rolagens e recursos). Há múltiplas estratégias: (1) tabelas normalizadas, (2) JSONB no Postgres, (3) JSON serializado em `text`. Normalizado é rígido (muitas joins); JSONB é poderoso mas índices são complexos; JSON em `text` é simples (serializa/deserializa, sem índices).

## Direcionadores da decisão

- Simplicidade: estado lido **inteiro** em cada ação (sem queries parciais)
- Sem índices: JSON não é consultado pelo banco, só pela aplicação
- Portabilidade: `text` funciona em todo banco (JSONB é Postgres-específico)
- Determinismo: testes controlam estado, logs rastreiam ações

## Opções considerados

| Opção | Descrição |
|---|---|
| **JSON serializado em `text` (Jackson)** | `estado`, `log`, `loot` como colunas `text`. Jackson serializa/deserializa `EstadoBatalha`, `Combatente`, `Loot`. Sem índices. |
| Tabelas normalizadas | `batalha_combatentes`, `batalha_acao_log`, `batalha_loot` etc. Rejeitada: rígido; múltiplas queries. |
| JSONB | Native Postgres. Rejeitada: índices complexos; não necessário (estado é lido inteiro). |

## Resultado da decisão

Adotou-se **JSON serializado em `text`**:

1. **Colunas** (`jogo_batalhas`, migração V3):
   ```sql
   estado text NOT NULL,   -- JSON de EstadoBatalha
   log text NOT NULL,      -- Texto, uma linha por evento
   loot text NULL,         -- JSON de Loot
   ```

2. **Serialização no serviço**: `Batalha.estado`/`loot` são `String`; `MasmorraService` serializa `EstadoBatalha` e `Loot` com `ObjectMapper` (Jackson). A base `JsonConverter<T>` existe mas não é usada.

3. **Estruturas**:
   - `EstadoBatalha` (record): combatentes, posições, turno, flags
   - `Combatente` (record): ID, HP, posição, lado (jogador/inimigo), flags
   - `Loot` (record): recursos e itens
   - `Posicao` (record): x, y

### Consequências positivas
- **Simples**: serialize/deserialize automático
- **Lido inteiro**: sem queries parciais (JSON em `text` sem índices)
- **Portável**: funciona em qualquer banco
- **Determinístico**: logs rastreiam cada ação
- **Versionável**: mudanças em estrutura = migração (não de DB, de Jackson)

### Consequências negativas
- **Sem índices**: buscar batalha por `estado.turno` exige full table scan (mitigado: poucas batalhas)
- **JSONB seria melhor em produção**: índices JSONB permitiriam queries (não pedido agora)
- **Tamanho**: JSON é verboso (mitigado: compressão no futuro)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| JSON em `text` | Simples; portável; lido inteiro; Jackson automático. | Sem índices; JSONB seria melhor em produção. |
| Tabelas normalizadas | Indexável. | Rígido; múltiplas queries. |
| JSONB | Índices nativos. | Postgres-específico; overkill (não é consultado pelo banco). |

## Mais informações

- **Design**: [`add-city-builder-game/design.md` §13, 19](../../openspec/changes/archive/2026-09-27-add-city-builder-game/design.md)
- **Código**:
  - [`jogo/dominio/JsonConverter.java`](../../src/main/java/com/example/loginbase/jogo/dominio/JsonConverter.java)
  - [`jogo/masmorra/combate/EstadoBatalha.java`](../../src/main/java/com/example/loginbase/jogo/masmorra/combate/EstadoBatalha.java)
  - [`jogo/masmorra/Loot.java`](../../src/main/java/com/example/loginbase/jogo/masmorra/Loot.java)
- **Migração**: [`src/main/resources/db/migration/V3__jogo.sql`](../../src/main/resources/db/migration/V3__jogo.sql) — colunas `text`

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
