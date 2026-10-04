# Prédios de Coleta

**Épico:** [construcoes.md](construcoes.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Prédios de coleta extraem recursos naturais de jazidas específicas. Existem 8 tipos, um para cada tipo de jazida (Floresta, Rocha, Barreiro, etc.). Cada prédio exige marcação de ladrilhos com jazida compatível, conectados ortogonalmente, determinando quantidade de trabalhadores produtivos.

## Regras

- **R1**: Um prédio de coleta só pode ser construído em região possuída cujo tipo tenha o bônus correspondente do prédio (seção 1.4, 4.6) [req].
- **R2**: Cada prédio coleta apenas sua jazida correspondente (tabela 1.5) (seção 4.6) [req].
- **R3**: Marcação de ladrilhos obrigatória: mesma região, jazida compatível, sem prédio em cima, cada marcado ligado a no máximo 1 prédio, área conectada ortogonalmente ao prédio (seção 4.6) [proposta].
- **R4**: Máximo de marcados por nível: N1 = 4, N2 = 10, N3 = 20 (seção 4.6) [proposta].
- **R5**: Trabalhadores produtivos = `min(alocados, floor(marcados ÷ 2))` (seção 4.6) [proposta].
- **R6**: Próprio ladrilho do prédio pode ter qualquer jazida (seção 4.6) [proposta].
- **R7**: Marcar/desmarcar é gratuito e vale a partir do próximo turno (seção 4.6) [proposta].
- **R8**: Produção = `Σ (eficiência) × base × multiplicador do nível` (seção 4.3, 4.5) [proposta]. Bônus de região associado ao prédio aumenta produção em +1% por ponto.

## Números e tabelas

### Prédios e recursos [proposta]

| Prédio | Bônus | Tipo(s) de região | Jazida | Recurso | Profissão | Produção base/trabalhador/turno (N1) |
|---|---|---|---|---|---|---|
| Acampamento de lenhadores | Floresta | Floresta, Planície | Floresta | Madeira | Madeireiro | 5 Madeira |
| Pedreira | Rocha | Montanha | Rocha | Pedra | Mineiro | 4 Pedra |
| Barreiro | Barreiro | Floresta | Barreiro | Argila | Mineiro | 4 Argila |
| Mina de ferro | Ferro | Montanha | Veio de ferro | Minério de ferro | Mineiro | 3 Minério |
| Mina de carvão | Carvão | Montanha | Veio de carvão | Carvão | Mineiro | 3 Carvão |
| Salina | Salinas | Litoral | Salina | Sal | Mineiro | 3 Sal |
| Mina de enxofre | Enxofre | Litoral | Enxofre | Enxofre | Mineiro | 2 Enxofre |
| Cabana de caça | Floresta | Floresta, Planície | Floresta | Carne, Couro | Caçador | 2 Carne + 1 Couro |

### Custos por nível [proposta]

| Prédio | N1 Madeira | N1 Pedra | N1 Ferro | PO N1 | PO N2 | PO N3 |
|---|---|---|---|---|---|---|
| Acampamento | 15 | 5 | — | 4 | 10 | 20 |
| Pedreira | 20 | — | — | 4 | 10 | 20 |
| Barreiro | 15 | — | — | 4 | 10 | 20 |
| Mina de ferro | 30 | 20 | — | 6 | 15 | 30 |
| Mina de carvão | 30 | 20 | — | 6 | 15 | 30 |
| Salina | 20 | 10 | — | 4 | 10 | 20 |
| Mina de enxofre | 30 | 30 | 5 | 8 | 20 | 40 |
| Cabana de caça | 15 | — | — | 4 | 10 | 20 |

### Garantias de distribuição de jazidas [proposta]

Toda região tem pelo menos (em Urbana a jazida é ignorada):
- 10 ladrilhos de Floresta
- 10 ladrilhos de Rocha
- 8 ladrilhos de Barreiro

## Exemplos

**Exemplo 1: Coleta com marcação efetiva**
- Acampamento de lenhadores N1 colocado em (3, 3).
- Marcados 4 ladrilhos adjacentes, todos Floresta.
- 2 Madeireiros alocados (vagas N1: 2).
- Trabalhadores produtivos: `min(2, floor(4 ÷ 2)) = min(2, 2) = 2`.
- Produção: `2 × 1,0 × 5 Madeira × 1,0 = 10 Madeira/turno`.

**Exemplo 2: Coleta com trabalhador insuficiente**
- Pedreira N1 com 4 ladrilhos Rocha marcados.
- Apenas 1 Mineiro alocado.
- Trabalhadores produtivos: `min(1, floor(4 ÷ 2)) = 1`.
- Produção: `1 × eff × 4 Pedra`.

**Exemplo 3: Exemplo do plano**
- Prédio de coleta N1 com 4 marcados e 2 alocados → 2 produtivos.
- Prédio de coleta N1 com 3 marcados e 2 alocados → 1 produtivo (floor(3 ÷ 2) = 1).

**Exemplo 4: Upgrade para N2**
- Acampamento N1→N2: 37,5 Madeira (arredonda), 12,5 Pedra (arredonda), 10 PO.
- Máximo de marcados aumenta: 4 → 10.
- Produção com 3 Madeireiros, cada PE 4: `3 × 1,0 × 5 × 1,2 = 18 Madeira/turno`.

## Interações com outros domínios

- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — recursos extraídos
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — profissões Madeireiro, Mineiro, Caçador
- [construcoes.md](construcoes.md) — custos, PO, marcação
- [vila.md](../v1-008-vila-e-mapa/vila.md) — tipos de região, jazidas

## Modelo de dados

Prédio de coleta é um tipo de `construcao`. Marcação em tabela `construcao_marcacao`: construcao_id, x, y (1+ por prédio).

## Questões em aberto

- Marcar ladrilho com jazida errada: rejeitado ou apenas não contribui?
- Limite de prédios de coleta por vila: há limite?
