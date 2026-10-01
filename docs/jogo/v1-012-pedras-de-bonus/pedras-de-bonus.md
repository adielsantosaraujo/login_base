# Pedras de bônus

**Épico:** [pedras-de-bonus.md](pedras-de-bonus.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Pedras de bônus são itens especiais encontrados exclusivamente em masmorras que conferem atributos adicionais aos itens equipados. Cada pedra contém um ou mais bônus sorteados do catálogo comum, podendo ser engastadas em slots de qualidade superior para aprimorar permanentemente o desempenho de armas, armaduras, ferramentas e joias.

## Regras

- **R1** [req]: Pedras de bônus só são obtidas em vitórias contra masmorras.
- **R2** [proposta]: Cada pedra possui um tipo (Simples, Boa, Excelente ou Divina) que determina quantos bônus ela contém.
- **R3** [proposta]: Bônus de uma mesma pedra são distintos entre si, sorteados do catálogo (seção 6.2 da bíblia).
- **R4** [proposta]: Qualquer bônus vale em qualquer pedra, independentemente do tipo de item.
- **R5** [proposta]: Engaste é realizado na Ferraria, é imediato e permanente.
- **R6** [proposta]: Remover uma pedra engastada destrói a pedra.

## Tipos de pedra [proposta]

| Tipo | Nº de bônus | Faixa de magnitude | Custo de engaste (Ouro) |
|---|---|---|---|
| Simples | 1 | Baixa | 10 |
| Boa | 2 | Baixa | 25 |
| Excelente | 3 | Média | 60 |
| Divina | 4 | Alta | 150 |

**Notas:**
- Simples: magnitudes baixas (+1 característica, +3% ataque/defesa, +8 vida, etc.).
- Boa e Excelente: magnitudes médias (+2 característica, +5% ataque/defesa, +15 vida, etc.).
- Divina: magnitudes altas (+3 característica, +8% ataque/defesa, +25 vida, etc.).

## Números e tabelas

### Sorteios de pedras por nível (recompensa de vitória)

Quantidade de pedras sorteadas por nível de masmorra vencida: `1 + floor(N ÷ 3)`.

| Nível | Sorteios | Exemplo |
|---|---|---|
| N1–2 | 1 | Uma pedra por vitória |
| N3–5 | 2 | Duas pedras por vitória |
| N6–8 | 3 | Três pedras por vitória |
| N9–10 | 4 | Quatro pedras por vitória |

### Distribuição de tipo por nível (tabela de drop)

Percentual de obtenção de cada tipo quando uma pedra é sorteada na recompensa.

| Nível | Nada | Simples | Boa | Excelente | Divina |
|---|---|---|---|---|---|
| N1–3 | 50% | 45% | 5% | 0% | 0% |
| N4–6 | 30% | 45% | 20% | 5% | 0% |
| N7–9 | 20% | 30% | 30% | 17% | 3% |
| N10 | 10% | 20% | 35% | 27% | 8% |

**Nota:** "Nada" (50% em N1–3) significa o sorteio não gera pedra; o contador de sorteios não consome essa tentativa. A probabilidade cumulativa de obter uma pedra é: N1–3 50%, N4–6 70%, N7–9 80%, N10 90%.

## Exemplos

### Exemplo 1: Recompensa de N3 com 2 sorteios
Vila vence uma masmorra nível 3. A recompensa inclui 2 sorteios de pedra. Suponha os resultados:
1. Sorteio 1: rola Boa (20% de chance para N3–5), sorteiam-se 2 bônus distintos: +2 Força e +5% Ataque.
2. Sorteio 2: rola Nada (30% de chance para N3–5); nenhuma pedra.

Resultado: **1 pedra Boa** com bônus [FOR +2, ATK +5%], que pode ser engastada em um item com slot livre da qualidade Boa ou superior.

### Exemplo 2: Engaste de pedra Divina
A vila possui uma pedra Divina com 4 bônus: [VIT +3, VIDA +25, INI +3, CRIT +5].
- Custo de engaste: **150 Ouro**.
- Item alvo: uma Espada L5 de qualidade Divina (5 slots, 3 bônus intrínsecos já preenchidos, 2 slots livres).
- Engaste ocupa 1 slot; a pedra fica **permanentemente** no item.
- Se o jogador quiser remover, deve usar o recurso de remoção (que destrói a pedra e libera o slot).

### Exemplo 3: Bônus em pedras vs. itens
Um Colar de qualidade Boa tem 1 slot de pedra e 1 bônus intrínseco (sorteado na fabricação).
Se engastar uma pedra Boa (2 bônus) no único slot, o Colar passa a ter 1 + 2 = 3 bônus totais.

## Interações com outros domínios

- [Itens e fabricação](../v1-011-itens-e-fabricacao/itens.md) — pedras são engastadas em slots de qualidade Boa, Excelente e Divina.
- [Masmorras](../v1-001-masmorras/masmorras.md) — recompensas de vitória (tabela de drop).
- [Construções](../v1-003-construcoes/construcoes.md) — Ferraria realiza engaste de pedras (4.11 da bíblia).

## Modelo de dados (resumo)

### Tabela `pedra`

| Campo | Tipo | Descrição |
|---|---|---|
| id | UUID/Long | Identificador único da pedra |
| vila_id | FK | Referência à vila proprietária |
| qualidade | ENUM | SIMPLES, BOA, EXCELENTE, DIVINA |
| bonus | JSONB | Lista de bônus (código + magnitude) |
| item_id | FK (nullable) | Referência ao item que contém a pedra; nulo se no inventário |

### Bônus (estrutura JSONB)

```json
{
  "codigo": "FOR",
  "magnitude": "BAIXA" | "MÉDIA" | "ALTA"
}
```

Exemplo de uma pedra Excelente:
```json
{
  "qualidade": "EXCELENTE",
  "bonus": [
    {"codigo": "VIT", "magnitude": "MÉDIA"},
    {"codigo": "ATK", "magnitude": "MÉDIA"},
    {"codigo": "DEF", "magnitude": "MÉDIA"}
  ]
}
```

## Histórias

- [H-001 — Obter pedras nas masmorras](historia/h-001-obter-pedras-nas-masmorras.md)
- [H-002 — Engastar pedra em item](historia/h-002-engastar-pedra-em-item.md)

## Questões em aberto

- [proposta] Remover pedra destrói a pedra permanentemente (sem possibilidade de recuperação)?
- [proposta] A faixa de magnitude determina apenas o número visível ou também afeta números internos (ex.: Baixa sempre +1, Média sempre +2)?
