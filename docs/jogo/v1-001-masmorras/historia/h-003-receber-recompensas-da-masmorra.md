# H-003 — Receber recompensas da masmorra

**Épico:** [../masmorras.md](../masmorras.md) · **Domínio:** [../recompensas.md](../recompensas.md)

## História

Como jogador, quero receber recompensas ao derrotar uma masmorra, incluindo ouro, recursos, chance de item, XP para guerreiros e pedras de bônus.

## Contexto

Recompensas são calculadas pelo nível da masmorra derrotada, aplicando fórmulas documentadas (seção 8.4). Pedras de bônus só são obtidas em masmorras, nunca em combate comum. XP é distribuído aos guerreiros sobreviventes.

## Critérios de aceite

### CA1 — Ouro por nível
- **Dado** masmorra nível 6 derrotada.
- **Quando** a batalha termina com vitória.
- **Então** ouro é adicionado ao estoque: `40 × 6 + aleatório(0..20 × 6) = 240–360 Ouro` (seção 8.4).

### CA2 — Recursos por nível
- **Dado** masmorra nível 6.
- **Quando** vitória.
- **Então** 6 sorteios, cada um dando 60 unidades de 1 recurso (Madeira, Pedra, Ferro, Couro curtido, Tecido, e Aço se N6+) (seção 8.4).

### CA3 — Item aleatório com chance por nível
- **Dado** masmorra nível 6.
- **Quando** vitória.
- **Então** 60% de chance de 1 item (arma, armadura, joia) **nível 6**, qualidade por margem 0–4 (Simples/Boa/Excelente) ou 15+ para N8–10 (seção 8.4, 7.4).

### CA4 — XP para guerreiros sobreviventes
- **Dado** masmorra nível 6; tropa com 5 guerreiros vivos (2 mortos).
- **Quando** vitória.
- **Então** cada guerreiro vivo ganha **6 XP de Guerreiro** (seção 8.4).

### CA5 — Pedras sorteadas por nível
- **Dado** masmorra nível 6.
- **Quando** vitória.
- **Então** `1 + floor(6 ÷ 3) = 3` sorteios de pedra; cada um dá % chance de Nada/Simples/Boa/Excelente/Divina conforme tabela N4–6 (seção 8.4).

## Tarefas

- [h-003-tarefa-001 — Tabela de recompensas e drop](h-003-tarefa-001-tabela-de-recompensas-e-drop.md)

## Fora de escopo

- Engaste de pedras em itens (épico v1-012).
- Morte de guerreiros na derrota (já coberto em épico v1-014).
- Inventário de itens (épico v1-011).
