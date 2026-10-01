# H-001 — Fabricar armaduras

**Épico:** [../armaduras.md](../armaduras.md) · **Domínio:** [../armaduras.md](../armaduras.md)

## História

Como armeiro (artesão da Ferraria) ou alfaiate (artesão da Alfaiataria), quero fabricar peças de armadura em diferentes níveis, para que os guerreiros possam equipar proteção melhorada ao longo do jogo.

## Contexto

Armaduras são fabricadas em duas oficinas: Ferraria (Peitoral, Capacete, Ombreiras) e Alfaiataria (Luvas, Calças, Sapato). Cada peça tem uma receita base que multiplica por L (nível 1–10) e oferece defesa crescente. Para L ≥ 6, todo Ferro da receita converte para Aço. Requisitos de PE e nível da oficina limitam quais itens cada artesão pode produzir (seção 7.4, 7.5, 4.11 [oficinas]).

## Critérios de aceite

### CA1 — Peitoral na Ferraria

- **Dado** um artesão da Ferraria N1 com PE efetivo Ferreiro 8; item Peitoral L1 é fabricável
- **Quando** o artesão começa a fabricação: custo 5 Ferro, PF 2
- **Então** a fila de fabricação da oficina inclui o item em andamento; se concluído, entra no inventário

### CA2 — Capacete na Ferraria com upgrade de nível

- **Dado** um artesão da Ferraria N2 com PE efetivo Ferreiro 14; Capacete L5 é fabricável (PE mín. = 2×5−2 = 8)
- **Quando** a fabricação progride: 1,2 PF por turno (eficiência 1,0 × mult. N2)
- **Então** em 5 turnos (6 PF ÷ 1,2) o capacete L5 é concluído com custo 15 Ferro

### CA3 — Aço para L ≥ 6

- **Dado** um artesão iniciando Ombreiras L6 (receita base 3 Ferro, logo 18 Ferro para L6)
- **Quando** a receita é processada
- **Então** o custo lista 18 Aço em vez de Ferro

### CA4 — Luvas na Alfaiataria

- **Dado** uma costureira com PE efetivo Costureiro 10; Luvas L4 é fabricável (PE mín. = 2×4−2 = 6)
- **Quando** a fabricação avança
- **Então** custo 8 Couro curtido; qualidade sorteada conforme margem (10−6 = 4, faixa 0–4: 70% Simples, 25% Boa, 5% Excelente)

### CA5 — Requisito de oficina bloqueado

- **Dado** um artesão da Ferraria N1 tentando fabricar Capacete L4 (limite N1 é L3)
- **Quando** submete o pedido
- **Então** erro: "Nível máximo L3 para Ferraria N1"

### CA6 — PE insuficiente

- **Dado** um artesão com PE efetivo Ferreiro 3 tentando fabricar Peitoral L5 (PE mín. = 8)
- **Quando** submete o pedido
- **Então** erro: "PE efetivo mínimo 8 necessário"

## Tarefas

- [H-001 · Tarefa 001 — Catálogo de armaduras](h-001-tarefa-001-catalogo-de-armaduras.md)

## Fora de escopo

- Aprimoramento de itens (L → L+1) — fica em v1-011-itens-e-fabricacao (H-003).
- Interface gráfica da fila de fabricação — fica em tarefa de frontend geral.
- Bônus intrínsecos sorteados — a qualidade e bônus gerais de itens ficam em v1-011.
