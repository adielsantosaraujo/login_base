# Comércio

**Épico:** [recursos.md](recursos.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

O Mercado permite compra e venda de recursos com um mercador NPC. A Estalagem gera ouro ao servir refeições a viajantes e permite imigração de novos cidadãos. Ambos requerem comerciantes alocados e têm limite de volume por turno.

## Regras

### Mercado [proposta — seção 4.9 da bíblia]
- R1: Vende e compra recursos com um mercador NPC (comércio entre jogadores fica fora da v1).
- R2: Volume máximo negociado por turno = `20 × Σ eficiência dos Comerciantes × mult. nível` unidades.
- R3: Preço de venda = preço base × `min(1,0; 0,5 + 0,02 × PE Comerciante do melhor comerciante)`.
- R4: Preço de compra = preço base × `max(1,0; 1,5 − 0,02 × PE Comerciante do melhor comerciante)`.
- R5: Ordens são executadas imediatamente, consumindo o volume do turno corrente.

### Estalagem [proposta — seção 4.10 da bíblia]
- R6: Vagas: Cozinheiros ou Comerciantes (2/5/10).
- R7: Serve Refeições a viajantes: por turno consome até `5 × Σ eficiência × mult. nível` Refeições e gera **4 Ouro por Refeição** servida.
- R8: Imigração: a cada turno, chance de `2% × nível` (N1 2%, N2 4%, N3 6%) de chegar um viajante adulto (18–30 anos, 20 pontos de característica e 10 de profissão distribuídos aleatoriamente), se existir núcleo livre em alguma casa; ele vira um núcleo próprio (solteiro).

## Números e tabelas

**Tabela 3.1 — Preços base de recursos (para cálculo de venda/compra)**

| Recurso | Preço base (Ouro) |
|---|---|
| Madeira | 1 |
| Pedra | 1 |
| Argila | 1 |
| Minério de ferro | 2 |
| Carvão | 2 |
| Sal | 3 |
| Enxofre | 4 |
| Grãos | 1 |
| Fibra (linho) | 1 |
| Carne | 2 |
| Couro | 2 |
| Lã | 2 |
| Tábua | 3 |
| Tijolo | 3 |
| Ferro (lingote) | 6 |
| Aço | 20 |
| Tecido | 4 |
| Couro curtido | 6 |
| Refeição | 1 |

**Estalagem — geração de ouro**

| Nível | Capacidade de Refeições | Máx. Ouro gerado por turno |
|---|---|---|
| N1 | 5 × eficiência | até 20 Ouro |
| N2 | 10 × eficiência | até 40 Ouro |
| N3 | 15 × eficiência | até 60 Ouro |

**Estalagem — imigração**

| Nível | Chance por turno |
|---|---|
| N1 | 2% |
| N2 | 4% |
| N3 | 6% |

Imigrante: adulto (18–30 anos), 20 pontos de característica + 10 de profissão distribuídos aleatoriamente.

## Exemplos

**Exemplo 1: Mercado de venda**
- Comerciante único com PE 12
- Preço de venda de Madeira: 1 × min(1,0; 0,5 + 0,02 × 12) = 1 × min(1,0; 0,74) = **0,74 Ouro por Madeira**
- Volume máximo: 20 × 1,0 × 1,0 = 20 unidades
- Vendendo 20 Madeira: 20 × 0,74 = **14,8 Ouro**

**Exemplo 2: Mercado de compra**
- Mesmo Comerciante com PE 12
- Preço de compra de Aço: 20 × max(1,0; 1,5 − 0,02 × 12) = 20 × max(1,0; 1,26) = **25,2 Ouro por Aço**
- Volume máximo: 20 unidades
- Comprando 10 Aço: 10 × 25,2 = **252 Ouro** (debita do estoque)

**Exemplo 3: Estalagem N2**
- 2 Cozinheiros com eficiência média 1,2
- Refeições disponíveis: 30
- Capacidade: 5 × (1,2 + 1,2) = 12 Refeições por turno (com mult. N2 ×1,2: 12 × 1,2 = 14,4)
- Efetivamente servidas: 14 Refeições
- Ouro gerado: 14 × 4 = **56 Ouro**
- Chance de imigração: 4% (imigrante se houver núcleo livre)

## Interações com outros domínios

- [recursos.md](recursos.md) — estoque de recursos vendidos/comprados
- [../../v1-003-construcoes/comercios.md](../v1-003-construcoes/comercios.md) — Mercado (construção e vagas)
- [../../v1-003-construcoes/estalagem.md](../v1-003-construcoes/estalagem.md) — Estalagem (construção e vagas)
- [../../v1-002-cidadaos/cidadao.md](../v1-002-cidadaos/cidadao.md) — Comerciante e Cozinheiro

## Modelo de dados (resumo)

Mercado: ordens executadas imediatamente, sem persistência (transação do turno).

Estalagem: campo na tabela vila ou evento_turno registrando ouro gerado e imigração.

## Questões em aberto

- O volume de 20 unidades no Mercado é compartilhado entre venda e compra, ou cada uma tem seu limite?
- Qual é a precedência na Estalagem entre Cozinheiro e Comerciante? (Proposta: Cozinheiro tem prioridade)
