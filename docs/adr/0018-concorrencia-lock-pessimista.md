# 0018 — Concorrência: Lock Pessimista por Vila, `@Version` na Batalha

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-26 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-city-builder-game`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/) |

## Contexto e problema

Múltiplas requisições HTTP do mesmo jogador podem chegar simultâneas (ex.: 2 upgrades ao mesmo prédio). Sem controle de concorrência, ambas veem recursos suficientes e ambas debitam. Há dois padrões: (1) **lock pessimista** (SELECT...FOR UPDATE), (2) **lock otimista** (`@Version`, retry). Cada um tem tradeoffs: pessimista serializa (seguro, simples), otimista permite paralelismo mas exige retry.

## Direcionadores da decisão

- Vila = 1 por usuário; usuário = 1 por sessão → ações são inherentemente serializadas
- Simplicidade: lock pessimista é direto, sem retry
- Batalha: `@Version` como defesa extra (mudanças podem falhar 409)
- Criação concorrente: vila criada quando não existe (race condition) → handle com `unique(usuario_id)` + retry

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Lock pessimista (vila) + `@Version` (batalha)** | `findByUsuarioIdParaAtualizacao` com `@Lock(PESSIMISTIC_WRITE)`. Ações sincronizam. Batalha tem `@Version` extra. |
| Lock otimista puro | `@Version` em vila; retry na `OptimisticLockingFailureException`. Rejeitada: 1 vila/usuário = sem paralelo. |
| Sem lock | Rejeitada: race conditions garantidas. |

## Resultado da decisão

Adotou-se **lock pessimista + `@Version` na batalha**:

1. **Lock pessimista** (`VilaRepository`):
   ```java
   @Query("SELECT v FROM Vila v WHERE v.usuario.id = :usuarioId")
   @Lock(LockModeType.PESSIMISTIC_WRITE)
   Optional<Vila> findByUsuarioIdParaAtualizacao(long usuarioId);
   ```
   - SELECT...FOR UPDATE no Postgres
   - Trava a linha até fim da transação
   - Outras requisições aguardam

2. **Sincronização** (em `VilaService`):
   - `obterParaAtualizacao(usuarioId)` carrega com lock
   - `sincronizar(vila, agora)` recalcula dentro da transação
   - Ação executa com vila travada

3. **Criação concorrente**:
   - Primeira requisição GET `/api/jogo/vila` sem vila
   - Duas requisições simultâneas criam vila
   - `unique(usuario_id)` impede segunda inserção
   - Catch `DataIntegrityViolationException`
   - Retry com `REQUIRES_NEW` transação (relê com lock)

4. **Batalha** (`Batalha` entidade):
   ```java
   @Version
   private Long version;
   ```
   - Cada ação incrementa versão
   - Turno deve coincidir com versão (409 `CONFLITO` se não)
   - Extra defense contra race conditions

### Consequências positivas
- **Simples**: lock pessimista elimina race conditions
- **ACID**: ações dentro da transação são atômicas
- **Previsível**: sem retry, sem thundering herd
- **Seguro para vila**: 1/usuário, serialização é OK

### Consequências negativas
- **Não escalável horizontalmente**: múltiplas instâncias lutam pela mesma linha (mitigado: projeto = 1 instância)
- **Turnos ociosos**: lock espera (aceitável; ações são rápidas)
- **Versão em batalha**: `@Version` sem lock otimista é overhead (mitigado: defesa extra)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Lock pessimista + @Version | Simples; ACID; sem retry; seguro. | Não escalável horizontalmente; serializa. |
| Lock otimista | Paralelismo. | Retry complexo; 1 vila/usuário = desnecessário. |
| Sem lock | Simplíssimo. | Race conditions garantidas. |

## Mais informações

- **Design**: [`add-city-builder-game/design.md` §15](../../openspec/changes/archive/2026-09-27-add-city-builder-game/design.md)
- **Código**:
  - [`jogo/dominio/VilaRepository.java`](../../src/main/java/com/example/loginbase/jogo/dominio/VilaRepository.java)
  - [`jogo/economia/VilaService.java#obterParaAtualizacao`](../../src/main/java/com/example/loginbase/jogo/economia/VilaService.java)
  - [`jogo/dominio/Batalha.java`](../../src/main/java/com/example/loginbase/jogo/dominio/Batalha.java) — `@Version`
- **Erro**: 409 `CONFLITO` em `ErroApiHandler` (ObjectOptimisticLockingFailureException)

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
