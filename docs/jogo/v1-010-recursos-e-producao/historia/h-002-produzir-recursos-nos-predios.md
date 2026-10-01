# H-002 — Produzir recursos nos prédios

**Épico:** [../recursos.md](../recursos.md) · **Domínio:** [../producao.md](../producao.md), [../recursos.md](../recursos.md)

## História

Como gerenciador de produção, quero que prédios rurais, de coleta e fábricas produzam recursos automaticamente cada turno, para que minha economia cresça sem ação manual.

## Contexto

A produção é resolvida no passo 1 do turno (seção 2.2 da bíblia): prédios de coleta e rurais (Fazendas), depois fábricas em ordem (Serraria → Olaria → Fundição → Tecelagem → Curtume → Cozinha).

Eficiência do trabalhador = 0,5 + 0,1 × PE efetivo (capped em 3,0), multiplicada por bônus e multiplicador de nível.

**Referência:** [../producao.md#regras](../producao.md#regras) e [../producao.md#números-e-tabelas](../producao.md#números-e-tabelas).

## Critérios de aceite

### CA1 — Produção de coleta com limite de ladrilhos
- **Dado** um Acampamento de lenhadores N1 com 2 Madeireiros alocados (eficiência 1,0 cada), 4 ladrilhos marcados
- **Quando** o turno é processado
- **Então** produção = 2 × 1,0 × 5 × 1,0 = 10 Madeira (4 marcados ÷ 2 = 2 produtivos, não 4)

### CA2 — Fábrica sem insumo suficiente
- **Dado** uma Serraria N1 com 1 Madeireiro (eficiência 1,0), 1 Madeira em estoque
- **Quando** o turno é processado
- **Então** receita (2 Madeira → 1 Tábua) executa 0 ciclos completos; nada é produzido; a 1 Madeira permanece

### CA3 — Multiplicador de nível
- **Dado** uma Fazenda de plantio N2 com 1 Agricultor (eficiência 1,0)
- **Quando** o turno é processado
- **Então** produção = 1 × 1,0 × 6 × 1,2 (multiplicador N2) = 7,2 Grãos

## Tarefas

- [h-002-tarefa-001-calculo-de-eficiencia-do-trabalhador.md](h-002-tarefa-001-calculo-de-eficiencia-do-trabalhador.md)
- [h-002-tarefa-002-producao-de-coleta-e-rural.md](h-002-tarefa-002-producao-de-coleta-e-rural.md)
- [h-002-tarefa-003-producao-das-fabricas.md](h-002-tarefa-003-producao-das-fabricas.md)

## Fora de escopo

- Mudança de cultura/rebanho (fica para H-002 futura ou UI)
- Escolha de Ferro vs Aço na Fundição N2+ (fica para tarefa de alocação)
