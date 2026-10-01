# H-001 · Tarefa 002 — Pipeline de resolução por vila

**História:** [H-001 — Processar turno global](h-001-processar-turno-global.md) · **Domínio:** [Turnos](../turnos.md) · **Depende de:** [H-001 · Tarefa 001](h-001-tarefa-001-agendador-do-turno-global.md) · **Camada:** Backend

## Objetivo

Implementar a pipeline de processamento de um turno para uma vila isolada, com os 13 passos em ordem determinística, transação por vila e garantia de idempotência.

## Contexto necessário

- Seção 2.2 da bíblia — Ordem de resolução por vila
  > 1. Produção; 2. Ouro passivo; 3. Consumo de comida; 4. Limite de armazenamento; 5. Obras; 6. Fabricação; 7. Quartel; 8. Movimentação de tropas; 9. Reprodução; 10. Envelhecimento; 11. Recuperação; 12. Masmorras; 13. Relatório do turno.

- Seção 2.1 — Idempotência
  > Cada vila é processada em sua própria transação; o processamento é idempotente por (vila, número do turno).

## Backend

- **Interface `EtapaTurno`** (Strategy pattern):
  ```java
  public interface EtapaTurno {
      int ordem(); // 1 a 13
      void executar(Vila vila, int turno);
      String nome(); // ex.: "Produção", "Ouro passivo", ...
  }
  ```

- **Implementações concretas** (uma por passo):
  - `EtapaProducao`, `EtapaOuroPassivo`, `EtapaConsumoComida`, etc.
  - Anotadas com `@Component` e ordenadas por `ordem()`.

- **Service `ProcessadorTurnoVila`**:
  - Método `processarVila(Vila vila, int numeroTurno)` com `@Transactional`.
    - Verifica se a vila já foi processada este turno (lê `vila.turno_processado`); se sim, retorna sem fazer nada (idempotência).
    - Itera sobre as etapas em ordem crescente.
    - Chama `etapa.executar(vila, numeroTurno)`.
    - Atualiza `vila.turno_processado = numeroTurno`.
    - Registra eventos em `evento_turno` conforme necessário.

- **Tabela `evento_turno` com migração Flyway V3:**
  - Campos: `id` (PK), `vila_id` (FK), `turno`, `tipo` (PRODUCAO, MORTE, NASCIMENTO, MASMORRA, ...), `mensagem`, `dados` (jsonb).

- **Vila com novo campo:**
  - `turno_processado`: armazena o último turno processado; usado para idempotência.

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/turno/EtapaTurno.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaTurno.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/ProcessadorTurnoVila.java](/src/main/java/com/example/loginbase/jogo/turno/ProcessadorTurnoVila.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/etapas/EtapaProducao.java](/src/main/java/com/example/loginbase/jogo/turno/etapas/EtapaProducao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/etapas/EtapaOuroPassivo.java](/src/main/java/com/example/loginbase/jogo/turno/etapas/EtapaOuroPassivo.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/etapas/...](/src/main/java/com/example/loginbase/jogo/turno/etapas/) (novos, um por passo)
- [/src/main/java/com/example/loginbase/jogo/turno/EventoTurnoRepository.java](/src/main/java/com/example/loginbase/jogo/turno/EventoTurnoRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EventoTurno.java](/src/main/java/com/example/loginbase/jogo/turno/EventoTurno.java) (novo)
- [/src/main/resources/db/migration/V4__adicionar_evento_turno.sql](/src/main/resources/db/migration/V4__adicionar_evento_turno.sql) (novo)

## Testes

- Teste unitário: mock de cada etapa; verificar que são chamadas em ordem.
- Teste de integração: processar um turno para uma vila; validar que todos os eventos são registrados.
- Teste de idempotência: processar duas vezes o mesmo turno para a mesma vila; verificar que o resultado é idêntico (sem duplicação de eventos).

## Definição de pronto

- Critérios de aceite CA2, CA3, CA4 (vilas, ordem, falha).
- Build sem erros.
- Testes de idempotência passando.
- Eventos registrados em `evento_turno`.

## Fora de escopo

- Implementação das etapas específicas (produção, morte, etc.); cada uma fica para seu épico/lote.
- Otimização de performance para milhões de vilas.
