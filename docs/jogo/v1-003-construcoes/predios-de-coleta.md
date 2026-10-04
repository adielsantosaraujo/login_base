# Prédios de Coleta

**Épico:** [construcoes.md](construcoes.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Prédios de coleta extraem recursos naturais de tipos de terreno específicos. Existem 8 tipos, um para cada terreno de coleta (Floresta, Rocha, Barreiro, etc.). Cada prédio exige marcação de ladrilhos com o mesmo tipo de terreno do prédio, conectados ortogonalmente, determinando quantidade de trabalhadores produtivos.

## Regras

- **R1**: Um prédio de coleta só pode ser construído em região possuída que possua o terreno correspondente do prédio (seção 1.4, 4.6) [req].
- **R2**: Cada prédio coleta apenas seu terreno correspondente (tabela 1.5) (seção 4.6) [req].
- **R3**: Marcação de ladrilhos obrigatória: mesma região, terreno compatível com o prédio, sem prédio em cima, cada marcado ligado a no máximo 1 prédio, área conectada ortogonalmente ao prédio (seção 4.6) [proposta].
- **R4**: Máximo de marcados por nível: N1 = 4, N2 = 10, N3 = 20 (seção 4.6) [proposta].
- **R5**: Trabalhadores produtivos = `min(alocados, floor(marcados ÷ 2))` (seção 4.6) [proposta].
- **R6**: Próprio ladrilho do prédio pode ter qualquer terreno (mas só há bônus se a âncora for do terreno do prédio) (seção 4.6) [proposta].
- **R7**: Marcar/desmarcar é gratuito e vale a partir do próximo turno (seção 4.6) [proposta].
- **R8**: Produção = `Σ eficiência × base × mult. nível × (1 + bônus ÷ 100)` (seção 4.3, 4.5) [proposta]. Bônus do prédio é o bonus_total do seu ladrilho-âncora, se o terreno bater; caso contrário, bônus = 0.

## Números e tabelas

### Prédios e recursos [proposta]

| Prédio | Terreno | Tipo(s) de região | Recurso | Profissão | Produção base/trabalhador/turno (N1) |
|---|---|---|---|---|---|
| Acampamento de lenhadores | Floresta | Floresta, Planície | Madeira | Madeireiro | 5 Madeira |
| Pedreira | Rocha | Montanha | Pedra | Mineiro | 4 Pedra |
| Barreiro | Barreiro | Floresta | Argila | Mineiro | 4 Argila |
| Mina de ferro | Ferro | Montanha | Minério de ferro | Mineiro | 3 Minério |
| Mina de carvão | Carvão | Montanha | Carvão | Mineiro | 3 Carvão |
| Salina | Salinas | Litoral | Sal | Mineiro | 3 Sal |
| Mina de enxofre | Enxofre | Litoral | Enxofre | Mineiro | 2 Enxofre |
| Cabana de caça | Floresta | Floresta, Planície | Carne, Couro | Caçador | 2 Carne + 1 Couro |

### Bônus por terreno [proposta]

Cada prédio de coleta recebe bônus apenas quando construído em ladrilho do terreno correspondente:
- Acampamento de lenhadores: bônus se terreno = **Floresta**
- Pedreira: bônus se terreno = **Rocha**
- Barreiro: bônus se terreno = **Barreiro**
- Mina de ferro: bônus se terreno = **Ferro**
- Mina de carvão: bônus se terreno = **Carvão**
- Salina: bônus se terreno = **Salinas**
- Mina de enxofre: bônus se terreno = **Enxofre**
- Cabana de caça: bônus se terreno = **Floresta**

O bônus é o `bonus_total` do ladrilho-âncora (onde o prédio está colocado). Se o terreno não bater, o bônus é 0.

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

### Distribuição de terrenos por região [proposta]

Os 100 ladrilhos (10×10) de cada região contêm apenas os 3 tipos de terreno da região, distribuídos conforme os percentuais da criação de vila. A Urbana continua com todos os ladrilhos construíveis por qualquer prédio urbano, sem distinção de terreno.

## Exemplos

**Exemplo 1: Coleta com marcação efetiva**
- Acampamento de lenhadores N1 colocado em (3, 3) com terreno Floresta.
- Marcados 4 ladrilhos adjacentes, todos Floresta.
- 2 Madeireiros alocados (vagas N1: 2).
- Trabalhadores produtivos: `min(2, floor(4 ÷ 2)) = min(2, 2) = 2`.
- Bônus do ladrilho-âncora: suponha bonus_total = 30.
- Produção: `2 × 1,0 × 5 × 1,0 (N1) × 1,30 = 13 Madeira/turno` (fator de bônus 1 + 30/100 = 1,30).

**Exemplo 2: Coleta com trabalhador insuficiente**
- Pedreira N1 (terreno Rocha) com 4 ladrilhos Rocha marcados.
- Apenas 1 Mineiro alocado.
- Trabalhadores produtivos: `min(1, floor(4 ÷ 2)) = 1`.
- Produção: `1 × eff × 4 Pedra × (1 + bonus_total/100)`.

**Exemplo 3: Exemplo do plano**
- Prédio de coleta N1 com 4 marcados e 2 alocados → 2 produtivos.
- Prédio de coleta N1 com 3 marcados e 2 alocados → 1 produtivo (floor(3 ÷ 2) = 1).

**Exemplo 4: Upgrade para N2**
- Acampamento N1→N2: com PE 4 a eficiência é 0,9 → `3 × 0,9 × 5 × 1,2 = 16,2 Madeira/turno` (sem bônus).
- Custo: 37,5 Madeira (arredonda para 38), 12,5 Pedra (arredonda para 13), 10 PO.
- Máximo de marcados aumenta: 4 → 10.

## Interações com outros domínios

- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — recursos extraídos
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — profissões Madeireiro, Mineiro, Caçador
- [construcoes.md](construcoes.md) — custos, PO, marcação
- [regioes.md](../v1-008-vila-e-mapa/regioes.md) — tipos de região, terrenos dos ladrilhos

## Modelo de dados

Prédio de coleta é um tipo de `construcao`. Marcação em tabela `construcao_marcacao`: construcao_id, x, y (1+ por prédio).

## Questões em aberto

- Limite de prédios de coleta por vila: há limite?
