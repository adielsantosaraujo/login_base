# H-003 — Gerenciar inventário e aprimorar itens

**Épico:** [../itens.md](../itens.md) · **Domínio:** [../itens.md](../itens.md), [../fabricacao.md](../fabricacao.md), [../equipamento.md](../equipamento.md)

## História

Como jogador, quero consultar e gerenciar meu inventário de itens, aplicar filtros, e aprimorar itens (aumentar nível sem perder qualidade e bônus).

## Contexto

O inventário da vila armazena todos os itens não equipados, sem limite de quantidade (seção 7.6). Aprimoramento (seção 7.4):

- Feito na mesma oficina onde o item foi fabricado
- Custa 50% do custo de fabricar o nível seguinte
- Requisitos: mesmos de fabricar L+1
- Mantém qualidade, bônus e pedras
- Máximo L10

## Critérios de aceite

### CA1 — Listar inventário com filtros

- **Dado** vila com 15 itens (5 armas, 4 armaduras, 3 ferramentas, 3 joias)
- **Quando** acessar inventário com filtro categoria "Arma"
- **Então** lista mostra 5 armas; outros ocultados

### CA2 — Aprimorar item (L1 → L2)

- **Dado** Espada L1 Boa com 1 Pedra Simples engastada, no inventário
- **Quando** aprimorar em Ferraria N2
- **Então** custo debitado: 50% × (3 Ferro + 1 Tábua) × 2 = 3 Ferro + 1 Tábua (arredondado)
- **E** Espada L2 Boa mantém a Pedra Simples
- **E** Espada L1 removida do inventário

### CA3 — Requisito de PE para aprimoramento validado

- **Dado** Ferreiro com PE efetivo 7 tentando aprimorar Espada L5 → L6
- **Quando** submeter aprimoramento
- **Então** API retorna erro `pe`: "PE efetivo mínimo 10 necessário para L6" (2×6−2=10)

### CA4 — Recursos insuficientes rejeitado

- **Dado** vila com 2 Ferro; aprimoramento exige 3 Ferro + 1 Tábua
- **Quando** tentar aprimorar
- **Então** API retorna erro `recursos`: "Ferro insuficiente (2 < 3)"

### CA5 — Qualidade mantida após aprimoramento

- **Dado** Anel L3 Excelente com bônus +5% ATK e +1 INI
- **Quando** aprimorar para L4 com êxito
- **Então** Anel L4 Excelente mantém bônus +5% ATK e +1 INI (ou equivalentes recalculados?)

### CA6 — Máximo L10 bloqueado

- **Dado** Espada L10
- **Quando** tentar aprimorar para L11
- **Então** API retorna erro `limite`: "Nível máximo L10 atingido"

## Tarefas

- [h-003-tarefa-001-api-de-inventario-e-aprimoramento.md](h-003-tarefa-001-api-de-inventario-e-aprimoramento.md)
- [h-003-tarefa-002-tela-de-inventario.md](h-003-tarefa-002-tela-de-inventario.md)

## Fora de escopo

- Venda ou descarte de itens (fora da v1)
- Reordenação do inventário (fora desta história)
