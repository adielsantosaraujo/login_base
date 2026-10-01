# H-001 — Surgimento e evolução de masmorras

**Épico:** [../masmorras.md](../masmorras.md) · **Domínio:** [../masmorras.md](../masmorras.md)

## História

Como jogador, quero que masmorras apareçam naturalmente em regiões não possuídas de minha vila, evoluindo em nível ao longo do tempo, para que o jogo tenha desafios crescentes e objetivos a perseguir.

## Contexto

Masmorras são a principal fonte de desafios militares e recompensas valiosas (itens, pedras de bônus, XP). Surgem em regiões elegíveis com base em probabilidade (1% por turno), com evolução automática a cada 18 turnos sem ataque. Um ataque (vitória ou derrota) zera o contador de evolução. Vitória remove a masmorra; derrota deixa os inimigos com vida cheia para próximo ataque (seções 8.1, 8.2).

## Critérios de aceite

### CA1 — Surgimento com 1% de probabilidade
- **Dado** uma região elegível (não possuída, sem masmorra, fora do período de 6 turnos após limpeza) em uma vila com ≥12 turnos de idade.
- **Quando** o turno for processado.
- **Então** a região tem 1% de chance de gerar uma masmorra nível 1 naquele turno (seção 8.1).

### CA2 — Limite de 3 masmorras ativas
- **Dado** uma vila com 3 masmorras ativas.
- **Quando** tentar gerar uma 4ª masmorra.
- **Então** nenhuma nova masmorra é criada (seção 8.1).

### CA3 — Carência após limpeza
- **Dado** uma masmorra foi eliminada no turno T.
- **Quando** o turno T+5 ou anterior.
- **Então** nenhuma masmorra pode surgir naquela região (bloqueio de 6 turnos, seção 1.6).

### CA4 — Evolução a cada 18 turnos sem ataque
- **Dado** uma masmorra nível 1, surgida no turno T, sem sofrer ataque.
- **Quando** o turno T+18 for processado.
- **Então** a masmorra sobe para nível 2 (turnos_sem_ataque é incrementado cada turno, seção 8.2).

### CA5 — Ataque zera o contador
- **Dado** uma masmorra nível 3 com turnos_sem_ataque = 10.
- **Quando** uma tropa atacá-la (vitória ou derrota).
- **Então** turnos_sem_ataque é resetado para 0 (seção 8.2).

### CA6 — Vitória remove a masmorra
- **Dado** uma masmorra nível 2 ativa.
- **Quando** a tropa derrota todos os inimigos.
- **Então** a masmorra é removida e a região fica marcada "limpa até turno T+6" (seção 8.2).

### CA7 — Derrota restaura inimigos
- **Dado** uma masmorra nível 3 com inimigos vivos.
- **Quando** a tropa sofrer derrota (30 rodadas ou sem sobreviventes).
- **Então** os inimigos voltam com vida cheia para o próximo ataque; turnos_sem_ataque é resetado (seção 8.2).

## Tarefas

- [h-001-tarefa-001 — Modelo de dados de masmorras](h-001-tarefa-001-modelo-de-dados-de-masmorras.md)
- [h-001-tarefa-002 — Surgimento e evolução no turno](h-001-tarefa-002-surgimento-e-evolucao-no-turno.md)
- [h-001-tarefa-003 — Masmorras no mapa](h-001-tarefa-003-masmorras-no-mapa.md)

## Fora de escopo

- Geração de inimigos específicos (tarefa h-002-tarefa-001).
- Interface de ataque (tela de batalha).
- Consequências de derrota na tropa (tarefa h-002-tarefa-002).
