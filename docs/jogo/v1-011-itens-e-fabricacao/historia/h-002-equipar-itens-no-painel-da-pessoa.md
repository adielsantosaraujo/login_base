# H-002 — Equipar itens no painel da pessoa

**Épico:** [../itens.md](../itens.md) · **Domínio:** [../itens.md](../itens.md), [../equipamento.md](../equipamento.md)

## História

Como jogador, quero equipar (e trocar) armas, armaduras, ferramentas e joias no painel de cada cidadão para aplicar seus bônus e melhorar desempenho em combate ou trabalho.

## Contexto

O painel da pessoa mostra slots de equipamento. Equipar um item (seção 7.7):

- Verifica idade mínima: ≥14 anos para ferramentas e joias; ≥16 para armas e armaduras (7.5)
- Verifica PE: Guerreiro ≥ L − 1 para armas/armaduras; PE base profissão ≥ L − 1 para ferramentas (7.5)
- Joias sem requisito (7.5)
- Limita 1 Colar e 2 Anéis por pessoa (7.1)
- Bloqueia troca de equipamento se membro de tropa em expedição (7.7)

## Critérios de aceite

### CA1 — Slot correto validado

- **Dado** Espada L3 no inventário; slot de Arma vazio
- **Quando** tentar equipar Espada em slot de Ferramenta
- **Então** API retorna erro `slot`: "Espada deve ir em slot Arma"

### CA2 — Limite de 2 Anéis

- **Dado** pessoa com 2 Anéis já equipados
- **Quando** tentar equipar 3º Anel
- **Então** API retorna erro `limite`: "Máximo 2 anéis por pessoa"

### CA3 — Requisito de PE validado

- **Dado** Guerreiro com PE efetivo 3 em Guerreiro
- **Quando** tentar equipar Espada L5 (requisito L − 1 = 4)
- **Então** API retorna erro `pe`: "PE efetivo mínimo 4 necessário"

### CA4 — Requisito de idade validado

- **Dado** cidadão com 13 anos
- **Quando** tentar equipar Enxada L1 (requisito ≥14 anos)
- **Então** API retorna erro `idade`: "Mínimo 14 anos para equipar ferramentas"

### CA5 — Trocar devolve item ao inventário

- **Dado** Guerreiro com Espada L3 equipada; Espada L5 disponível no inventário
- **Quando** equipar Espada L5 (requisito atendido)
- **Então** Espada L3 volta ao inventário; Espada L5 entra no slot

### CA6 — Membro de tropa em expedição bloqueado

- **Dado** Guerreiro em tropa `EM_VIAGEM_IDA` (em expedição a masmorra)
- **Quando** tentar trocar equipamento via API
- **Então** API retorna erro `estado`: "Não pode trocar equipamento em expedição"

## Tarefas

- [h-002-tarefa-001-regras-e-api-de-equipar.md](h-002-tarefa-001-regras-e-api-de-equipar.md)
- [h-002-tarefa-002-interface-de-equipamento.md](h-002-tarefa-002-interface-de-equipamento.md)

## Fora de escopo

- Remoção de item (deixar slot vazio) — fora desta história
- Troca de posição entre Anel 1 e Anel 2 — fora desta história
