# H-001 — Fabricar joias

**Épico:** [../joias.md](../joias.md) · **Domínio:** [../joias.md](../joias.md)

## História

Como artesão, quero fabricar colares e anéis na Ferraria, para produzir joias que aumentem os atributos da vila.

## Contexto

Joias são itens especiais fabricados apenas na Ferraria. Colares e anéis têm receitas fixas em Ferro e Ouro, custam recursos variáveis com o nível (×L), exigem artesão com PE suficiente, e levam tempo proporcional ao nível (PF = 1 + L). Anéis exigem que o jogador escolha a característica afetada no ato de fabricar.

Consultar:
- [../joias.md](../joias.md) — regras gerais de joias.
- [../../v1-011-itens-e-fabricacao/fabricacao.md](../../v1-011-itens-e-fabricacao/fabricacao.md) (seção Fabricação)
  > Requisitos: oficina certa, nível da oficina permite L, artesão alocado com PE efetivo ≥ 2L − 2, recursos no estoque.
  > Custo = receita base × L. Para L ≥ 6, todo "Ferro" vira "Aço".

## Critérios de aceite

### CA1 — Colar nível 1 fabricado com custo exato

- **Dado** um jogador com 5 Ferro e 20 Ouro em estoque; Ferraria N1 com 1 Ferreiro alocado (PE Ferreiro efetivo 5).
- **Quando** o jogador inicia fabricação de Colar L1.
- **Então** a receita consome 1 Ferro e 20 Ouro imediatamente; fila de fabricação criada com PF = 2; a fabricação está agendada.

### CA2 — Anel nível 3 com característica escolhida

- **Dado** um jogador com 3 Ferro e 45 Ouro; Ferraria N1 com 1 Ferreiro alocado (PE efetivo 4).
- **Quando** o jogador inicia fabricação de Anel L3, escolhendo característica "Força".
- **Então** receita consome 3 Ferro e 45 Ouro; fila criada com PF = 4; item será criado com `atributo_escolhido = FOR` ao concluir.

### CA3 — Nível acima do permitido pela oficina rejeitado

- **Dado** Ferraria N1 (máx. L3, seção 4.11).
- **Quando** o jogador tenta fabricar Colar L4.
- **Então** rejeitado com mensagem "Nível máximo permitido: L3".

### CA4 — PE artesão insuficiente rejeitado

- **Dado** Ferraria com 1 Ferreiro (PE efetivo 3).
- **Quando** o jogador tenta fabricar Anel L5 (requisito PE ≥ 2×5 − 2 = 8).
- **Então** rejeitado com mensagem "PE insuficiente" ou similar.

### CA5 — Recursos insuficientes rejeitados

- **Dado** 1 Ferro e 30 Ouro em estoque (faltam 15 Ouro para Anel L3).
- **Quando** o jogador tenta fabricar Anel L3.
- **Então** rejeitado com mensagem "Recursos insuficientes".

### CA6 — Mudança para Aço em nível ≥6

- **Dado** estoque com 6 Ferro e 120 Ouro; Ferraria N3.
- **Quando** o jogador inicia fabricação de Colar L6.
- **Então** receita exibida como "6 Aço, 120 Ouro" (Ferro convertido); custo final idêntico (6 unidades de Ferro/Aço, 120 Ouro); fila criada com PF = 7.

### CA7 — Conclusão de fabricação ao acumular PF

- **Dado** fila com Colar L1 (PF = 2) em andamento; eficiência do Ferreiro = 1,0; N1 (mult. = 1,0).
- **Quando** passam 2 turnos (progresso = 1,0 × 1 × 2 = 2,0 PF).
- **Então** item concluído no turno 2; qualidade sorteada (70% Simples, 25% Boa, 5% Excelente); item criado no inventário da vila.

### CA8 — Qualidade sorteada conforme margem de PE

- **Dado** Anel L3 com Ferreiro PE efetivo 7 (margem = 7 − 4 = 3, faixa 0–4).
- **Quando** fabricação concluída.
- **Então** qualidade: 70% Simples, 25% Boa, 5% Excelente (conforme tabela 7.4, margem 0–4).

## Tarefas

- [h-001-tarefa-001-catalogo-de-joias.md](h-001-tarefa-001-catalogo-de-joias.md)

## Fora de escopo

- Aprimoramento de joias (L→L+1) — fica em história de inventário.
- Engaste de pedras em joias — fica em história de pedras.
