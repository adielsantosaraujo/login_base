# H-003 — Alimentar a população

**Épico:** [../recursos.md](../recursos.md) · **Domínio:** [../alimentacao.md](../alimentacao.md)

## História

Como gerenciador de recursos, quero que a população consuma alimentos automaticamente cada turno, e que a falta de comida gere fome com penalidades, para que alimento seja um recurso crítico.

## Contexto

Consumo resolvido no passo 3 do turno (seção 2.2 da bíblia). Pessoas ≥14 anos consomem 1 alimento/turno; menores de 14 consomem 0,5. Ordem: Refeição → Grãos → Carne. Fome dura 3+ turnos → perda de VIT.

**Referência:** [../alimentacao.md#regras](../alimentacao.md#regras).

## Critérios de aceite

### CA1 — Consumo normal
- **Dado** uma vila com 10 adultos e 4 menores, 12 Refeições em estoque
- **Quando** o turno é processado
- **Então** consumo total = 10 × 1,0 + 4 × 0,5 = 12 alimentos; Refeições caem para 0

### CA2 — Bem alimentada
- **Dado** consumo igual a CA1 (12 alimentos, 100% Refeição)
- **Quando** processado
- **Então** ≥50% de Refeições → vila.bem_alimentada = true; próximo turno todo mundo tem +10% de eficiência

### CA3 — Fome parcial
- **Dado** 10 pessoas (10 alimentos), 5 Refeições + 3 Grãos
- **Quando** processado
- **Então** 8 alimentos consumidos (5+3); 2 pessoas ficam famintas; eficiência ×0,5; não concebem

### CA4 — Morte por fome prolongada
- **Dado** pessoa faminta há 3+ turnos, VIT 2
- **Quando** o turno é processado sem alimento
- **Então** perde 1 VIT (2 → 1); próximo turno sem alimento: VIT 0 → morte

## Tarefas

- [h-003-tarefa-001-consumo-de-comida-e-fome.md](h-003-tarefa-001-consumo-de-comida-e-fome.md)

## Fora de escopo

- Preferência por alimento específico (sempre ordem fixa)
- Armazenamento de alimento (fica em H-001)
