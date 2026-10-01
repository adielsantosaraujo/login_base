# H-001 — Fabricar ferramentas

**Épico:** [ferramentas.md](../ferramentas.md) · **Domínio:** [itens.md](../../v1-011-itens-e-fabricacao/itens.md)

## História

Como um jogador, quero fabricar ferramentas nas oficinas, para que meus cidadãos ganhem bônus de profissão durante o trabalho.

## Contexto

Ferramentas são itens fabricáveis em Ferraria ou Carpintaria (conforme a tabela 7.9 da bíblia). Cada ferramenta tem receita e requisitos de profissão e nível de oficina, como qualquer outro item. O bônus (+L PE) só se aplica quando a pessoa trabalha na profissão da ferramenta.

Referência: [fabricacao.md](../../v1-011-itens-e-fabricacao/fabricacao.md) — requisitos, custo, tempo, qualidade.

## Critérios de aceite

### CA1 — Fabricar Enxada em Ferraria N1
- **Dado** uma Ferraria N1 com 1 Ferreiro alocado (PE base 2, INT 6 → PE efetivo 3), 1 Ferro e 1 Tábua em estoque.
- **Quando** o jogador inicia a fabricação de Enxada L1.
- **Então** a oficina começa a fabricar o item: Enxada L1, receita 1 Ferro + 1 Tábua (conforme tabela 7.9), PF 2 (L+1), progresso 0,5 × 1,0 = 0,5 PF por turno. Tempo estimado: 4 turnos com 1 ferreiro.

### CA2 — Fabricar Machado L5 em Ferraria N3
- **Dado** uma Ferraria N3 com 1 Ferreiro de PE efetivo 12, 10 Ferro e 5 Tábua em estoque.
- **Quando** o jogador inicia a fabricação de Machado L5.
- **Então** receita: 10 Ferro + 5 Tábua (2 Ferro + 1 Tábua × 5 do L), PF 6 (5+1), progresso 1,2 × 1,0 × 1,5 = 1,8 PF por turno (N3 mult. 1,5). Tempo estimado: ~3 turnos. Qualidade por margem m = 12 - (2×5 - 2) = 12 - 8 = 4 → 70% Simples, 25% Boa, 5% Excelente.

### CA3 — Fabricar Carrinho de mão em Carpintaria
- **Dado** uma Carpintaria N1 com 1 Madeireiro alocado (PE base 1, FOR 7, VEL 6 → PE efetivo 3), 3 Tábua e 1 Ferro em estoque.
- **Quando** o jogador inicia a fabricação de Carrinho de mão L1.
- **Então** receita: 3 Tábua + 1 Ferro (conforme tabela 7.9), PF 2, progresso 0,5 PF por turno. Tempo estimado: 4 turnos.

### CA4 — Oficina errada rejeita fabricação
- **Dado** uma Alfaiataria (oficina correta para Kit de costura) e uma Enxada (ferramenta de Ferraria).
- **Quando** o jogador tenta fabricar Enxada em Alfaiataria.
- **Então** a aplicação rejeita: "Oficina incorreta. Enxada é fabricada na Ferraria."

### CA5 — PE insuficiente rejeita fabricação
- **Dado** uma Ferraria N1 com 1 Carregador alocado (PE base 1 em Ferreiro, insuficiente para L3 que exige PE ≥ 2×3-2 = 4).
- **Quando** o jogador tenta fabricar Enxada L3.
- **Então** a aplicação rejeita: "PE efetivo insuficiente. Mínimo: 4 PE de Ferreiro."

## Tarefas

- [H-001 · Tarefa 001 — Catálogo de ferramentas](h-001-tarefa-001-catalogo-de-ferramentas.md)

## Fora de escopo

- Aprimoramento de ferramentas (é para H-003 de itens-e-fabricacao).
- Comércio de ferramentas (fora da v1).
