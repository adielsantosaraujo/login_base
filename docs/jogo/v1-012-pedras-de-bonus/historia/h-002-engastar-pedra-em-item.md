# H-002 — Engastar pedra em item

**Épico:** [../pedras-de-bonus.md](../pedras-de-bonus.md) · **Domínio:** [../pedras-de-bonus.md](../pedras-de-bonus.md), [../bonus.md](../bonus.md), [../../v1-011-itens-e-fabricacao/itens.md](../../v1-011-itens-e-fabricacao/itens.md)

## História

Como jogador, quero engastar pedras de bônus em itens na Ferraria, para conferir novos atributos permanentes e aprimorar meu equipamento.

## Contexto

Engaste é o processo de inserir uma pedra em um slot livre de um item. A Ferraria realiza engaste de forma imediata, exigindo que:
1. O item tenha um slot de pedra livre (conforme qualidade: Boa 1 slot, Excelente 3, Divina 5).
2. Exista ouro suficiente para pagar o custo (conforme tipo de pedra: Simples 10, Boa 25, Excelente 60, Divina 150).
3. Remover uma pedra engastada é permanente e destrói a pedra.

Seções relevantes: [Pedras de bônus](../pedras-de-bonus.md), [Tipos de pedra](../pedras-de-bonus.md) (magnitudes e custos), [Catálogo de bônus](../bonus.md).

## Critérios de aceite

### CA1 — Engaste com custo em ouro
- **Dado** uma pedra Boa (custo 25 Ouro) e um item com slot livre
- **Quando** o jogador confirma engaste e a vila tem ≥25 Ouro
- **Então** o custo é debitado do estoque, a pedra é ligada ao item e fica permanente

### CA2 — Rejeição: sem slot livre
- **Dado** um item de qualidade Boa (1 slot) com 1 pedra já engastada
- **Quando** o jogador tenta engastar outra pedra
- **Então** a ação é rejeitada (mensagem: "Sem slots de pedra disponíveis")

### CA3 — Rejeição: sem ouro suficiente
- **Dado** uma pedra Divina (custo 150 Ouro) e a vila tem 140 Ouro
- **Quando** o jogador tenta engastar
- **Então** a ação é rejeitada (mensagem: "Ouro insuficiente")

### CA4 — Item Simples sem slot
- **Dado** um item de qualidade Simples (0 slots de pedra)
- **Quando** o jogador tenta engastar uma pedra
- **Então** a ação é rejeitada (mensagem: "Itens Simples não aceitam pedras")

### CA5 — Remoção destrói pedra permanentemente
- **Dado** uma pedra Boa engastada em um item
- **Quando** o jogador remove a pedra
- **Então** a pedra é destruída, não volta ao inventário; o slot fica livre

### CA6 — Bônus da pedra aplicam imediatamente
- **Dado** uma pedra com bônus [VIT +2, ATK +5%] é engastada em uma Espada
- **Quando** o engaste é concluído
- **Então** os bônus passam a contar nos cálculos de atributo (ex.: se a Espada é equipada, Ataque e Vitalidade aumentam)

## Tarefas

- [H-002 · Tarefa 001 — API de engaste](h-002-tarefa-001-api-de-engaste.md)
- [H-002 · Tarefa 002 — Interface de engaste](h-002-tarefa-002-interface-de-engaste.md)

## Fora de escopo

- Engaste automático de múltiplas pedras.
- Desconto em custo de engaste por nível de Ferreiro.
- Troca de pedra sem destruição (permanente nesta v1).
