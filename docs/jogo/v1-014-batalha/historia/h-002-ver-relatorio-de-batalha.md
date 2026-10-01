# H-002 — Ver relatório de batalha

**Épico:** [batalha.md](../batalha.md) · **Domínio:** [batalha.md](../batalha.md)

## História

Como jogador, quero ver um relatório detalhado de cada batalha (lista com replay rodada a rodada e recompensas) para acompanhar o resultado e receber as recompensas corretamente.

## Contexto

Após cada batalha (vitória ou derrota), o sistema grava log com rodadas, ações, danos e resultado. O jogador deve poder:
1. Listar batalhas da vila (resumo: data, masmorra, resultado);
2. Ver replay rodada a rodada (ações, danos, abatidos);
3. Visualizar recompensas (ouro, recursos, itens, XP, pedras) — seção 8.4.

Referências rápidas:
- Log da batalha (seção 10.5): participantes, ações por rodada, resultado
- Recompensas (seção 8.4): ouro, recursos, itens aleatórios, XP, pedras
- Tabela `batalha` (11.2): estrutura de persistência

## Critérios de aceite

### CA1 — Listar batalhas da vila
- **Dado** uma vila com 3 batalhas anteriores (2 vitórias, 1 derrota)
- **Quando** acessa a tela de histórico de batalhas
- **Então** lista exibe as 3 batalhas em ordem cronológica decrescente, com resultado, masmorra, e data do turno

### CA2 — Replay rodada a rodada
- **Dado** um replay da batalha do exemplo 10.4 (Guerreiro vs. Goblin)
- **Quando** abre o replay
- **Então** exibe: "Rodada 1: Guerreiro ataca Goblin por 23 (crítico? não), Goblin (40 → 17 PV). Goblin ataca Guerreiro por 11, Guerreiro (79 → 68 PV)."
- E para rodada 2 similar até a morte do Goblin

### CA3 — Recompensas na vitória
- **Dado** masmorra N5 com vitória garantida
- **Quando** exibe resultado
- **Então** mostra: Ouro (fórmula 40×5 + 0..100 = 200..300), Recursos (5 sorteios de 50 u.), Item (chance ~50%), XP (5 por guerreiro), Pedras (2 sorteios)

### CA4 — Sem recompensas na derrota
- **Dado** uma derrota em batalha
- **Quando** exibe resultado
- **Então** mostra apenas "Derrota", sem ouro, recursos ou itens

### CA5 — XP de Guerreiro aos sobreviventes
- **Dado** um Guerreiro que sobrevive a masmorra N6
- **Quando** a batalha conclui
- **Então** Guerreiro ganha N XP de Guerreiro (6 XP neste caso)

## Tarefas

- [H-002 · Tarefa 001 — API de relatório de batalha](h-002-tarefa-001-api-de-relatorio-de-batalha.md)
- [H-002 · Tarefa 002 — Tela de replay da batalha](h-002-tarefa-002-tela-de-replay-da-batalha.md)

## Fora de escopo

- Filtros avançados (por masmorra, por resultado)
- Export de relatório (CSV, PDF)
- Replay em tempo real com animações
