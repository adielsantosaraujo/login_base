# H-002 — Atacar masmorra

**Épico:** [../masmorras.md](../masmorras.md) · **Domínio:** [../masmorras.md](../masmorras.md), [../inimigos.md](../inimigos.md), [../../v1-014-batalha/batalha.md](../../v1-014-batalha/batalha.md)

## História

Como jogador, quero enviar minha tropa para atacar uma masmorra, lutando contra os inimigos nela contidos, para ganhar recompensas valiosas (ouro, itens, XP, pedras).

## Contexto

Ataque a masmorra é acionado por expedição de tropa (seção 9.3). A tropa viaja até a masmorra; ao chegar, inimigos são gerados pelo nível da masmorra (seção 8.3) e a batalha é resolvida imediatamente em rodadas (seção 10). Vitória remove a masmorra; derrota restaura os inimigos com vida cheia (seção 8.2).

## Critérios de aceite

### CA1 — Inimigos gerados pelo nível
- **Dado** masmorra nível 3 e um ataque iniciado.
- **Quando** a expedição chegar à masmorra.
- **Então** inimigos são gerados com base em M(3), quantidade `min(8; 2 + 3) = 5` comuns + 1 chefe (seção 8.3).

### CA2 — Vitória remove masmorra
- **Dado** masmorra nível 2 ativa.
- **Quando** a tropa derrota todos os inimigos.
- **Então** masmorra é removida (`ativa = false`); região fica 6 turnos sem poder gerar nova masmorra (seção 8.2, 1.6).

### CA3 — Derrota restaura inimigos
- **Dado** masmorra nível 3 com inimigos parcialmente derrotados (30 rodadas atingidas ou tropa sem sobreviventes).
- **Quando** a tropa sofre derrota.
- **Então** inimigos voltam com vida cheia para próximo ataque; contador de evolução é resetado (seção 8.2).

### CA4 — Contador de evolução é resetado
- **Dado** masmorra nível 4 com `turnos_sem_ataque = 15`.
- **Quando** a tropa atacá-la (vitória ou derrota).
- **Então** `turnos_sem_ataque = 0` (seção 8.2).

### CA5 — Regras de bloqueio de anexação
- **Dado** região com masmorra ativa.
- **Quando** jogador tentar anexar região.
- **Então** operação é rejeitada (seção 1.6).

## Tarefas

- [h-002-tarefa-001 — Geração dos inimigos por nível](h-002-tarefa-001-geracao-dos-inimigos-por-nivel.md)
- [h-002-tarefa-002 — Integração expedição e batalha](h-002-tarefa-002-integracao-expedicao-e-batalha.md)

## Fora de escopo

- Recompensas (história h-003).
- Detalhe de cálculo de batalha (épico v1-014).
- Efeito de morte de guerreiros (épico v1-014).
