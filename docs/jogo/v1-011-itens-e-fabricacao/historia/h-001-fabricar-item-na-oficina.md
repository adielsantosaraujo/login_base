# H-001 — Fabricar item na oficina

**Épico:** [../itens.md](../itens.md) · **Domínio:** [../itens.md](../itens.md), [../fabricacao.md](../fabricacao.md)

## História

Como artesão, quero fabricar itens nas oficinas (armas, armaduras, ferramentas, joias) para equipar meus cidadãos e melhorar sua eficácia.

## Contexto

A fabricação ocorre em oficinas (Ferraria, Alfaiataria, Carpintaria) e segue a seção 7.4 da bíblia:

- Requisitos de PE e nível máximo da oficina (4.11);
- Custo que aumenta com o nível (×L, com Ferro→Aço em L≥6);
- Qualidade sorteada pela margem `m = PE efetivo − (2L − 2)` (tabela em 7.4);
- Tempo em pontos de fabricação (PF = 1 + L; progresso por turno = eficiência × mult. nível).

## Critérios de aceite

### CA1 — Nível acima do permitido pela oficina rejeitado

- **Dado** artesão tentando fabricar Espada L4 em Ferraria N1 (máx. L3)
- **Quando** submeter solicitação de fabricação
- **Então** API retorna erro `validacao`: "Nível máximo L3 para esta oficina"

### CA2 — PE insuficiente rejeitado

- **Dado** Ferreiro com PE efetivo 5 na profissão
- **Quando** tentar fabricar Espada L6 (requisito: PE ≥ 2×6−2 = 10)
- **Então** API retorna erro `validacao`: "PE efetivo mínimo 10 necessário"

### CA3 — Recursos insuficientes rejeitado

- **Dado** vila com 5 Ferro no estoque; Espada L2 custa 6 Ferro + 2 Tábua
- **Quando** tentar fabricar
- **Então** API retorna erro `recursos`: "Ferro insuficiente (5 < 6)"

### CA4 — Custo correto com conversão Ferro→Aço

- **Dado** Espada L6 com receita (3 Ferro + 1 Tábua) × 6 = 18 Ferro + 6 Tábua
- **Quando** fabricar em L≥6
- **Então** custo debitado: 18 Aço + 6 Tábua (não Ferro)

### CA5 — Qualidade sorteada conforme margem

- **Dado** artesão com PE efetivo 12, fabricando Espada L5 (requisito PE ≥ 8)
- **Quando** completar fabricação (margem = 12 − 8 = 4, faixa 0–4)
- **Então** qualidade sorteada: 70% Simples, 25% Boa, 5% Excelente (não Divina em N1/N2; Divina só em N3)

### CA6 — Conclusão após pontos de fabricação

- **Dado** Espada L1 com PF = 2; artesão eficiência 1,0 em Ferraria N1
- **Quando** resolver 2 turnos de fabricação (progresso = 1,0 × 1,0 = 1,0 PF/turno)
- **Então** item concluído, movido para inventário da vila

## Tarefas

- [h-001-tarefa-001-modelo-de-dados-de-itens.md](h-001-tarefa-001-modelo-de-dados-de-itens.md)
- [h-001-tarefa-002-gerador-de-itens-qualidade-e-bonus.md](h-001-tarefa-002-gerador-de-itens-qualidade-e-bonus.md)
- [h-001-tarefa-003-fila-de-fabricacao-no-turno.md](h-001-tarefa-003-fila-de-fabricacao-no-turno.md)
- [h-001-tarefa-004-tela-da-oficina.md](h-001-tarefa-004-tela-da-oficina.md)

## Fora de escopo

- Venda/comércio de itens (fora da v1)
- Descarte de itens (fora de escopo nesta história)
