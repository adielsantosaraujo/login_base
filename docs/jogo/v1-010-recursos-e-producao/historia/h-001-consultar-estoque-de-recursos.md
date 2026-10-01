# H-001 — Consultar estoque de recursos

**Épico:** [../recursos.md](../recursos.md) · **Domínio:** [../recursos.md](../recursos.md)

## História

Como administrador da vila, quero consultar o estoque de todos os recursos, para saber quanto tenho disponível e planejar produção e construção.

## Contexto

O estoque é persistido na tabela `estoque` com quantidade em numeric(14,2). A capacidade base é 500 por recurso (ilimitada para Ouro). Armazéns aumentam a capacidade conforme sua eficiência média de Carregadores.

**Referência:** [../recursos.md#regras](../recursos.md#regras) — Regras R3, R4, R5.

## Critérios de aceite

### CA1 — Consultar estoque básico
- **Dado** um painel de estoque da vila
- **Quando** o jogador abre a tela de estoque
- **Então** vejo a quantidade de cada um dos 19 recursos listados na tabela 3.1, com 2 casas decimais internas e exibição inteira (arredondado para baixo)

### CA2 — Exibir capacidade sem Armazém
- **Dado** uma vila sem nenhum Armazém construído
- **Quando** consulto o estoque
- **Então** a capacidade de cada recurso é 500 e o Ouro é ilimitado; se tiver mais de 500 de um recurso, aparece um aviso de que o excedente será perdido no próximo turno

### CA3 — Exibir capacidade com Armazém N1
- **Dado** um Armazém N1 com 1 Carregador alocado (eficiência 1,0)
- **Quando** consulto a capacidade
- **Então** é 500 (base) + 500 × 1,0 = 1.000 por recurso

### CA4 — Exibir capacidade com Armazém N2 incompleto
- **Dado** um Armazém N2 com 1 Carregador apenas (precisa de 2)
- **Quando** consulto a capacidade
- **Então** é 500 (base); o Armazém não soma porque não tem o mínimo de Carregadores

### CA5 — Excedente será perdido no próximo turno
- **Dado** 800 Madeira (500 base + 300 de Armazém N1) e nenhuma produção
- **Quando** o turno é processado
- **Então** no passo 4 do turno (limite de armazenamento), os 800 caem para 500 e um evento registra a perda de 300 Madeira

## Tarefas

- [h-001-tarefa-001-modelo-de-estoque-e-capacidade.md](h-001-tarefa-001-modelo-de-estoque-e-capacidade.md)
- [h-001-tarefa-002-painel-de-estoque.md](h-001-tarefa-002-painel-de-estoque.md)

## Fora de escopo

- Histórico de variações de estoque (fica para uma história futura de "relatório detalhado")
- Alertas de produção próxima de limite
