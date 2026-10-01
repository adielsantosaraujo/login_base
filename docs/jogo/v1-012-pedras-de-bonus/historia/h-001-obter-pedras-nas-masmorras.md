# H-001 — Obter pedras nas masmorras

**Épico:** [../pedras-de-bonus.md](../pedras-de-bonus.md) · **Domínio:** [../pedras-de-bonus.md](../pedras-de-bonus.md), [../../v1-001-masmorras/masmorras.md](../../v1-001-masmorras/masmorras.md)

## História

Como jogador, quero obter pedras de bônus ao vencer masmorras, para ter novos atributos a engastar em meus itens e aprimorar meu equipamento.

## Contexto

Pedras de bônus são recompensas exclusivas de vitórias em combate contra masmorras. Cada nível de masmorra gera um número determinístico de sorteios, e cada sorteio resulta em uma pedra de um tipo específico (Simples, Boa, Excelente ou Divina) com bônus distintos sorteados do catálogo unificado.

Seção relevante: [Recompensas de masmorra](../../v1-001-masmorras/masmorras.md) (seção 8.4 da bíblia).

## Critérios de aceite

### CA1 — Sorteio correto por nível
- **Dado** uma tropa vence uma masmorra nível 3
- **Quando** a batalha é resolvida com vitória
- **Então** 2 sorteios de pedra são executados (conforme fórmula `1 + floor(3 ÷ 3) = 2`)

### CA2 — Distribuição de tipo conforme nível
- **Dado** um sorteio é executado para nível 5
- **Quando** o sorteio escolhe o tipo de pedra
- **Então** as probabilidades respeitam [30% Nada, 45% Simples, 20% Boa, 5% Excelente, 0% Divina] (tabela N4–6)

### CA3 — Bônus distintos por pedra
- **Dado** uma pedra Excelente é sorteada (tipo com 3 bônus)
- **Quando** os 3 bônus são gerados
- **Então** nenhum código de bônus se repete (ex.: [VIT +2, VIT +2, ATK +5%] é inválido)

### CA4 — Faixa de magnitude pelo nível
- **Dado** uma pedra Boa é sorteada em nível 7 (faixa Alta)
- **Quando** os 2 bônus são gerados
- **Então** ambos vêm com magnitude "Alta" (ex.: [VIDA +25, DEF +8%])

### CA5 — Pedras adicionadas ao inventário
- **Dado** uma masmorra nível 6 é vencida com 3 sorteios (2 sem resultado, 1 Excelente)
- **Quando** a batalha conclui com vitória
- **Então** a pedra Excelente é adicionada ao inventário da vila (tabela `pedra`)

### CA6 — Casos de erro: derrota não gera pedras
- **Dado** uma tropa é derrotada em batalha
- **Quando** a batalha termina
- **Então** nenhuma pedra é gerada (mesmo que a masmorra tivesse nível alto)

## Tarefas

- [H-001 · Tarefa 001 — Modelo e gerador de pedras](h-001-tarefa-001-modelo-e-gerador-de-pedras.md)

## Fora de escopo

- Engaste de pedras em itens (H-002).
- Detalhes de recompensas de ouro/recursos/XP (cobertas em v1-001-masmorras).
- Interface do relatório de batalha (coberida em v1-014-batalha).
