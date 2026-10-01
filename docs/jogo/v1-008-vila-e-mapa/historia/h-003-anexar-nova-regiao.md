# H-003 — Anexar nova região

**Épico:** [../vila.md](../vila.md) · **Domínio:** [../regioes.md](../regioes.md)

## História

Como jogador com vila já criada, quero anexar uma nova região adjacente, pagando recursos, e escolhendo seu tipo.

## Contexto

- Adjacência ortogonal: regiões precisam estar ligadas (cima, baixo, esquerda, direita).
- Custo em Ouro, Madeira, Pedra, proporcionalmente ao número de regiões já possuídas (k).
- Fórmula: Ouro = round(150 × 1,35^(k−3)); Madeira = Pedra = 50 × (k−2).
- Regiões com masmorra ativa não podem ser anexadas.
- Regiões limpas de masmorra ficam 6 turnos sem poder gerar nova masmorra (mas podem ser anexadas).
- Anexação é imediata ao pagar; o jogador escolhe o tipo no ato.

## Critérios de aceite

### CA1 — Custo aumenta com cada região anexada

- **Dado** uma vila com 3 regiões possuídas (k=3)
- **Quando** tenta anexar a 4ª região
- **Então** custo exibido é Ouro 203, Madeira 100, Pedra 100 (k=4)

### CA2 — Região não adjacente é rejeitada

- **Dado** vila com regiões 6 e 7 possuídas
- **Quando** tenta anexar região 1 (não adjacente)
- **Então** erro: "Região deve ser adjacente a uma já possuída"

### CA3 — Região com masmorra ativa é rejeitada

- **Dado** região 10 com masmorra nível 3 ativa
- **Quando** tenta anexar região 10
- **Então** erro: "Não é possível anexar região com masmorra ativa"

### CA4 — Recursos insuficientes são rejeitados

- **Dado** vila com Ouro 100, Madeira 50, Pedra 50 (menos que custo Ouro 203, Madeira 100, Pedra 100)
- **Quando** tenta anexar com k=4
- **Então** erro: "Recursos insuficientes"

### CA5 — Anexação conclui com tipo escolhido

- **Dado** vila com Ouro 203, Madeira 100, Pedra 100 disponíveis, região 3 está vazia e adjacente
- **Quando** submete anexação de região 3 tipo COLETA
- **Então** região 3 passa a ser possuída com tipo COLETA; estoque debita Ouro 203, Madeira 100, Pedra 100; região aparece no mapa com cor/ícone de Coleta

## Tarefas

- [h-003-tarefa-001-regra-e-api-de-anexacao.md](h-003-tarefa-001-regra-e-api-de-anexacao.md)
- [h-003-tarefa-002-interface-de-anexacao.md](h-003-tarefa-002-interface-de-anexacao.md)

## Fora de escopo

- Cancelamento de anexação já iniciada.
- Histórico de anexações.
- Troca de tipo de região (definitivo na v1).
