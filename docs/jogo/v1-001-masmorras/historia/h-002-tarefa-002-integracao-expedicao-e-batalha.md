# H-002 · Tarefa 002 — Integração expedição e batalha

**História:** [h-002-atacar-masmorra.md](h-002-atacar-masmorra.md) · **Domínio:** [../masmorras.md](../masmorras.md), [../../v1-013-quartel-e-tropas/expedicoes.md](../../v1-013-quartel-e-tropas/expedicoes.md), [../../v1-014-batalha/batalha.md](../../v1-014-batalha/batalha.md) ·
**Depende de:** [h-002-tarefa-001-geracao-dos-inimigos-por-nivel.md](h-002-tarefa-001-geracao-dos-inimigos-por-nivel.md), [../../v1-013-quartel-e-tropas/historia/h-003-tarefa-001-viagem-de-tropas-no-turno.md](../../v1-013-quartel-e-tropas/historia/h-003-tarefa-001-viagem-de-tropas-no-turno.md), [../../v1-014-batalha/historia/h-001-tarefa-002-motor-de-batalha.md](../../v1-014-batalha/historia/h-001-tarefa-002-motor-de-batalha.md) · **Camada:** Backend

## Objetivo

Integrar chegada de expedição de tropa à masmorra, gerando inimigos e resolvendo batalha no turno, com tratamento de vitória (removido masmorra) e derrota (restaurar inimigos).

## Contexto necessário

- [../../v1-013-quartel-e-tropas/expedicoes.md](../../v1-013-quartel-e-tropas/expedicoes.md) — viagem de tropas até masmorra.
  > Turnos de viagem = max(1; distância Manhattan). Ao chegar, executar batalha no mesmo turno.

- [../inimigos.md](../inimigos.md) — geração de inimigos por nível (tarefa 001).

- [../../v1-014-batalha/batalha.md](../../v1-014-batalha/batalha.md) — motor de batalha e consequências.
  > Vitória/derrota; morte de abatidos (20% vitória, 50% derrota); perda de itens (derrota).

## Backend

- **Etapa do turno** `EtapaMovimentacaoTropas` (existente, modificação)
  - Passo 8 do pipeline (seção 2.2).
  - Ao chegar à masmorra (turnos_restantes == 0):
    1. Gerar inimigos chamando `GeradorInimigos.gerarInimigos(masmorra.nivel, semente)`.
    2. Executar batalha chamando `MotorBatalha.resolverBatalha(tropa, inimigos, semente)`.
    3. Registrar resultado em nova entrada `batalha` com log e recompensas.
    4. Se vitória:
       - Chamar `MasmorraService.removerMasmorra(masmorra)`.
       - Registrar no evento_turno.
    5. Se derrota:
       - Manter masmorra ativa com inimigos restaurados (vida cheia).
       - Chamar `MasmorraService.registrarAtaque(masmorra)` para resetar contador.
       - Registrar perdas (membros mortos/feridos, itens perdidos).
    6. Retornar tropa ao estado AQUARTELADA (em ambos os casos).

- **Serviço** `MasmorraService` (modificação)
  - `removerMasmorra(masmorra)`: define `ativa = false`; calcula `regiao.limpa_ate_turno = numeroTurno + 6`.

- **Evento** `EventoTurno` (registro)
  - Registrar "Tropa X venceu masmorra N3 em região Y" ou "Tropa X foi derrotada em masmorra N3".

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/turno/EtapaMovimentacaoTropas.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaMovimentacaoTropas.java) (modificação)
- [/src/main/java/com/example/loginbase/jogo/masmorra/MasmorraService.java](/src/main/java/com/example/loginbase/jogo/masmorra/MasmorraService.java) (modificação)

## Testes

- Teste de vitória: masmorra N2 → batalha vencida → `ativa = false`, `limpa_ate_turno = T+6`.
- Teste de derrota: masmorra N3 → batalha perdida → `ativa = true`, inimigos com vida cheia, `turnos_sem_ataque = 0`.
- Teste de registro de evento: "Tropa X venceu" ou "Tropa X foi derrotada" aparece em `EventoTurno`.
- Teste de integração: expedição 2 turnos ida, batalha no turno 3, retorno turno 4.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA2–CA4 (vitória, derrota, contador).
- Build do backend (`./mvnw verify`) sem erros.
- Testes de integração com MotorBatalha e GeradorInimigos passando.

## Fora de escopo

- Recompensas específicas (história h-003).
- Renderização visual de batalha (épico v1-014).
