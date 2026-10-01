# Armaduras

**Épico:** [armaduras.md](armaduras.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Armaduras são peças de proteção que reduzem o dano recebido em combate. O sistema inclui 6 peças de equipamento (Peitoral, Capacete, Ombreiras, Luvas, Calças e Sapato), fabricadas em níveis L1–L10 em duas oficinas diferentes. Um guerreiro pode equipar até uma peça de cada tipo, formando um conjunto que oferece defesa cumulativa.

## Regras

- R1: Existem 6 peças de armadura: Peitoral, Capacete, Ombreiras, Luvas, Calças e Sapato (seção 7.10).
- R2: Cada peça tem nível L1–L10, receita base e valor de defesa base específico (seção 7.10).
- R3: Peças de Ferraria: Peitoral, Capacete, Ombreiras; peças de Alfaiataria: Luvas, Calças, Sapato (seção 7.10).
- R4: Custo de fabricação = receita base × L, arredondado para cima (seção 7.4) [proposta].
- R5: Para L ≥ 6, todo "Ferro" da receita vira "Aço" (seção 7.4) [proposta].
- R6: Requisito para equipar: pessoa ≥16 anos com PE efetivo Guerreiro ≥ L − 1 (seção 7.5) [proposta].
- R7: Defesa total de um personagem = soma das defesas de todas as peças equipadas (seção 10.1) [proposta].
- R8: A Defesa entra na fórmula de dano como defesa_efetiva; a Besta reduz para 75% da defesa do alvo (seção 10.2) [proposta].
- R9: Sapato concede +1 de Iniciativa como efeito adicional (seção 7.10) [proposta].

## Números e tabelas

### Tabela 7.10 — Armaduras (base)

| Peça | Oficina | Receita base (×L) | Defesa base | Extra |
|---|---|---|---|---|
| Peitoral | Ferraria | 5 Ferro | 8 | — |
| Capacete | Ferraria | 3 Ferro | 4 | — |
| Ombreiras | Ferraria | 3 Ferro | 3 | — |
| Luvas | Alfaiataria | 2 Couro curtido | 2 | — |
| Calças | Alfaiataria | 3 Couro curtido, 1 Tecido | 5 | — |
| Sapato | Alfaiataria | 2 Couro curtido | 2 | +1 Iniciativa |

**Conjunto completo base** (1 peça de cada tipo, L1):
- Defesa total L1: 8 + 4 + 3 + 2 + 5 + 2 = **24**
- Defesa total L5 (×1,8): 24 × 1,8 = **43,2**
- Defesa total L10 (×2,8): 24 × 2,8 = **67,2**

(Fórmula geral: atributo principal(L) = base × (1 + 0,2 × (L − 1)) → L1 ×1,0; L5 ×1,8; L10 ×2,8, seção 7.3.)

### Bônus intrínsecos permitidos

Conforme seção 7.2, armaduras podem ter bônus intrínsecos sorteados do subconjunto: **VIT, DEF, VIDA, VEL**.

| Qualidade | Slots de pedra | Bônus intrínsecos | Faixa por nível |
|---|---|---|---|
| Simples | 0 | 0 | — |
| Boa | 1 | 1 | L1–4 baixa; L5–7 média; L8–10 alta |
| Excelente | 3 | 2 | L1–4 baixa; L5–7 média; L8–10 alta |
| Divina | 5 | 3 | L1–4 baixa; L5–7 média; L8–10 alta |

(Seção 7.2 [proposta].)

## Exemplos

**Exemplo 1: Custos de fabricação com L variável**
- Peitoral L1 = 5 Ferro
- Peitoral L5 = 25 Ferro
- Peitoral L10 = 50 Aço (pois L ≥ 6, Ferro → Aço)

**Exemplo 2: Soma de defesa em combate**
Um guerreiro equipado com Peitoral L5, Capacete L5, Ombreiras L5, Luvas L5, Calças L5, Sapato L5:
- Defesa Peitoral L5 = 8 × 1,8 = 14,4
- Defesa Capacete L5 = 4 × 1,8 = 7,2
- Defesa Ombreiras L5 = 3 × 1,8 = 5,4
- Defesa Luvas L5 = 2 × 1,8 = 3,6
- Defesa Calças L5 = 5 × 1,8 = 9
- Defesa Sapato L5 = 2 × 1,8 = 3,6
- **Defesa total = 43,2** (exatamente o conjunto L5 da tabela acima)
- **Iniciativa extra do Sapato = +1**

**Exemplo 3: Efeito de Defesa em dano**
Guerreiro com DEF 43,2 recebe ataque de inimigo com ATQ 25:
- dano = max(1; round(25 × 100 ÷ (100 + 3 × 43,2) × U(0,9; 1,1)))
- dano = max(1; round(2500 ÷ 229,6 × U(0,9; 1,1))) ≈ 10–13 dano (com variação)

## Interações com outros domínios

- [itens.md](../v1-011-itens-e-fabricacao/itens.md) — armaduras são itens, com qualidade, níveis e bônus.
- [fabricacao.md](../v1-011-itens-e-fabricacao/fabricacao.md) — regras de fabricação aplicam-se a armaduras.
- [equipamento.md](../v1-011-itens-e-fabricacao/equipamento.md) — como equipar/trocar armaduras.
- [batalha.md](../v1-014-batalha/batalha.md) — defesa de armaduras reduz dano em combate (seção 10.1, 10.2).
- [construcoes.md](../v1-003-construcoes/construcoes.md) — Ferraria e Alfaiataria fabricam armaduras.
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — requisito de idade e PE Guerreiro para equipar.

## Modelo de dados (resumo)

Armaduras são persistidas via tabela `item` (seção 11.2):

| Campo | Tipo | Descrição |
|---|---|---|
| id | PK | Identificador único |
| vila_id | FK → vila | Pertence a uma vila |
| categoria | ENUM | Valor: "Armadura" |
| subtipo | VARCHAR | Uma de: Peitoral, Capacete, Ombreiras, Luvas, Calças, Sapato |
| qualidade | ENUM | Simples, Boa, Excelente, Divina |
| nivel | INT | 1–10 |
| bonus | JSONB | Array de bônus intrínsecos (ex.: [{tipo: "VIT", magnitude: 1}]) |
| cidadao_id | FK → cidadao | Pessoa que equipa esta armadura (NULL se no inventário) |
| slot | VARCHAR | Qual peça (Peitoral, Capacete, ...) — redundante com subtipo, mantido por normalização |

Não há tabela separada para armaduras; usam-se filtros `categoria = 'Armadura'` e `subtipo = ?`.

## Histórias

- [H-001 — Fabricar armaduras](historia/h-001-fabricar-armaduras.md)
- [H-002 — Defesa das armaduras na batalha](historia/h-002-defesa-das-armaduras-na-batalha.md)

## Questões em aberto

- Balanceamento de defesa: valor de 24 em L1 é apropriado para o início do jogo?
- Distribuição de defesa entre as 6 peças: proporções corretas para o gameplay?
