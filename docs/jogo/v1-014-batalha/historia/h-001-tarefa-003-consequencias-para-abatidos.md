# H-001 · Tarefa 003 — Consequências para abatidos

**História:** [H-001 — Resolver batalha por rodadas](h-001-resolver-batalha-por-rodadas.md) · **Domínio:** [batalha.md](../batalha.md) ·
**Depende de:** [H-001 · Tarefa 002 — Motor de batalha](h-001-tarefa-002-motor-de-batalha.md) · **Camada:** Backend

## Objetivo

Implementar lógica de consequências pós-batalha: determinar se cada abatido fica ferido ou morre, registrar ferimentos, devolver itens ao inventário, gravar resultado da batalha no banco.

## Contexto necessário

- Consequências para abatidos (seção 10.6)
  > - Abatido com 0 PV: após batalha, vitória → 80% ferido / 20% morto; derrota → 50% / 50%
  > - Ferido: fica 6 turnos sem trabalhar nem lutar
  > - Itens de mortos: voltam ao inventário na vitória; perdidos na derrota

- Estados de cidadão (11.2, 5.3)
  > Campos: estado (SAUDAVEL/FERIDO), ferido_ate_turno, vivo

- Idade e morte em batalha (5.5)
  > Morte por causa: itens equipados voltam ao inventário

## Backend

**Serviço**: `ConsequenciasBatalhaService`

Métodos:
- `aplicarConsequencias(resultado_batalha: ResultadoBatalha, tropa: Tropa, vila: Vila, turno_atual: int) → void`
  - Para cada membro da tropa que foi abatido (0 PV no log):
    1. Sorteia morte/ferimento (80/20 na vitória, 50/50 na derrota)
    2. Se morte:
       - Define cidadao.vivo = false
       - Se derrota: itens equipados são destruídos (saem do jogo)
       - Se vitória: itens equipados voltam ao inventário da vila
    3. Se ferimento:
       - Define cidadao.estado = FERIDO
       - Define cidadao.ferido_ate_turno = turno_atual + 6
       - Define cidadao.construcao_id = null (sai do prédio)
       - Define cidadao.tropa_id = null (sai da tropa)
  - Persiste todos os cidadãos atualizados

- `gravarBatalha(batalha_resolvida: ResultadoBatalha, vila: Vila, tropa: Tropa, masmorra: Masmorra) → Batalha`
  - Cria registro em tabela `batalha`:
    - villa_id, tropa_id, masmorra_id, turno, semente, resultado, log (jsonb), recompensas (jsonb)
  - Retorna entidade persistida

**Modelo de dados**:

Entidade `Cidadao` (campos afetados):
- vivo: boolean (default true)
- estado: enum SAUDAVEL / FERIDO (default SAUDAVEL)
- ferido_ate_turno: int (null se não ferido)
- construcao_id: FK (null se não alocado)
- tropa_id: FK (null se não em tropa)

Tabela `batalha` (11.2):
- id (PK), vila_id (FK), tropa_id (FK), masmorra_id (FK), turno, semente, resultado, log (jsonb), recompensas (jsonb)

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/batalha/ConsequenciasBatalhaService.java](/src/main/java/com/example/loginbase/jogo/batalha/ConsequenciasBatalhaService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/Batalha.java](/src/main/java/com/example/loginbase/jogo/batalha/Batalha.java) (novo, entidade JPA)
- [/src/main/java/com/example/loginbase/jogo/batalha/BatalhaRepository.java](/src/main/java/com/example/loginbase/jogo/batalha/BatalhaRepository.java) (novo, JpaRepository)
- [/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoRepository.java](/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoRepository.java) (atualização: se ainda não existe)
- [/src/main/resources/db/migration/V<N>__Adiciona_tabela_batalha.sql](/src/main/resources/db/migration/V<N>__Adiciona_tabela_batalha.sql) (novo)
- [/src/test/java/com/example/loginbase/jogo/batalha/ConsequenciasBatalhaServiceTest.java](/src/test/java/com/example/loginbase/jogo/batalha/ConsequenciasBatalhaServiceTest.java) (novo)

## Testes

1. **Vitória: 80% ferimento, 20% morte**: 10 abatidos na vitória
   - Esperado: ~8 ficam FERIDO (ferido_ate_turno = turno + 6), ~2 morrem (vivo = false)

2. **Derrota: 50% morte, itens perdidos**: 5 abatidos na derrota
   - Esperado: ~2-3 morrem, seus itens são destruídos; ~2-3 ficam FERIDO (mantêm itens? não — perdem tudo na derrota)

3. **Itens na vitória**: guerreiro morto equipava Espada L5
   - Esperado: Espada L5 volta ao inventário da vila

4. **Ferido é removido de construção**: guerreiro alocado no Quartel é ferido
   - Esperado: cidadao.construcao_id = null; cidadao.estado = FERIDO

5. **Ferido é removido de tropa**: guerreiro em tropa é ferido (o que não deveria acontecer durante expedição, mas testa idempotência)
   - Esperado: cidadao.tropa_id = null

6. **Gravação de batalha**: resultado persistido
   - Esperado: SELECT * FROM batalha WHERE vila_id = X retorna registro com log jsonb correto

## Definição de pronto

- Critérios de aceite da história cobertos: CA2 (consequências implícitas)
- Build do backend sem erros
- Testes listados passando
- Migração Flyway cria tabela `batalha` com campos corretos
- Integração com [Motor de batalha](h-001-tarefa-002-motor-de-batalha.md)
- Itens em inventário gerenciados corretamente (módulo item)

## Fora de escopo

- Recompensas de masmorra (ouro, recursos, itens) — tarefa 002 de H-002
- Remoção de masmorra após vitória — integração com módulo masmorra
- Teste de morte por idade ou fome (regras de 5.5) — fora do escopo de batalha
