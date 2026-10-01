# H-001 — Fabricar Armas

**Épico:** [../armas.md](../armas.md) · **Domínio:** [../armas.md](../armas.md)

## História
Como armeiro/artesão, quero fabricar armas (espadas, lanças, arcos e bestas) nas oficinas apropriadas, para que os guerreiros tenham equipamento de combate.

## Contexto
Armas são itens especiais com custo e requisitos de PE específicos (seção 7.4 e 7.8 da bíblia). Cada arma requer uma oficina correta:
- Espada, Lança: Ferraria
- Arco, Besta: Carpintaria

A receita é multiplicada pelo nível desejado (L1–L10), e o artesão precisa de PE efetivo suficiente (≥ 2L − 2). Qualidade é sorteada ao concluir, conforme margem de PE.

## Critérios de aceite

### CA1 — Fabricar Espada com receita correta
- **Dado** um artesão com PE Ferreiro ≥ 2L − 2, materiais em estoque e Ferraria disponível.
- **Quando** o jogador inicia fabricação de Espada L3 (receita 9 Ferro + 3 Tábua).
- **Então** os recursos são debitados, a fila de fabricação armazena a ordem com 4 PF (1 + L), e o progresso avança conforme a eficiência do artesão no turno seguinte.

### CA2 — Rejeitar receita fora do nível da oficina
- **Dado** Ferraria N1 (limite L3) com artesão disponível.
- **Quando** o jogador tenta fabricar Espada L5.
- **Então** a ordem é rejeitada com mensagem "Nível máximo L3 nesta oficina".

### CA3 — Fabricar Arco com receita correta
- **Dado** um artesão com PE de Carpinteiro ≥ 2L − 2, materiais e Carpintaria N2 (limite L6).
- **Quando** o jogador inicia fabricação de Arco L5 (receita 15 Tábua + 5 Tecido).
- **Então** a receita é processada e armazenada corretamente sem invocar Ferro/Aço.

### CA4 — Trocar Ferro para Aço em nível ≥ 6
- **Dado** Ferraria N3 com artesão e materiais (20 Aço em estoque).
- **Quando** o jogador fabricar Besta L6 (receita original 12 Ferro + 18 Tábua + 6 Tecido).
- **Então** a receita é ajustada para 12 Aço + 18 Tábua + 6 Tecido, e o material é debitado.

### CA5 — Conclusão com qualidade sorteada
- **Dado** uma Espada L5 em fabricação com 5 PF totais, artesão com PE Ferreiro 12 (margem 2L − 2 = 8, margem real = 4).
- **Quando** completar 5 PF (após turno de progresso).
- **Então** a arma é concluída, qualidade sorteada conforme tabela de margem 0–4 (70% Simples, 25% Boa, 5% Excelente), e transferida para o inventário.

### CA6 — Rejeitar sem PE suficiente
- **Dado** artesão com PE Ferreiro = 3; tenta fabricar Espada L5 (requer ≥ 2 × 5 − 2 = 8).
- **Quando** o jogador inicia a ordem.
- **Então** é rejeitado com mensagem "PE insuficiente. Requer PE ≥ 8".

## Tarefas
- [h-001-tarefa-001-catalogo-de-armas.md](h-001-tarefa-001-catalogo-de-armas.md)

## Fora de escopo
- Aprimoramento de armas (L → L+1); fica para a história v1-011.
- Engaste de pedras em armas; fica para v1-012.
- Venda de armas já fabricadas; fica para comércio entre jogadores (fora da v1).
