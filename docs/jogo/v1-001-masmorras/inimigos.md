# Inimigos

**Épico:** [masmorras.md](masmorras.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Cada masmorra contém um grupo de inimigos gerado pelo seu nível, usando um multiplicador de atributos. Inimigos comuns são sorteados de grupos específicos; chefes aparecem a partir do nível 3.

## Regras

- **R1** — Multiplicador `M(N) = 1 + 0,15 × (N − 1)` [proposta]: todos os atributos dos inimigos (PV, ATQ, DEF, INI) são multiplicados por M(N) e arredondados (seção 8.3).
- **R2** — Composição: `min(8; 2 + N)` inimigos comuns sorteados do grupo do nível; **+ 1 chefe a partir de N3** [proposta] (seção 8.3).

## Números e tabelas

### Tabela de atributos base

| Inimigo | PV | ATQ | DEF | INI | Linha | Tipo de ataque |
|---|---|---|---|---|---|---|
| Rato gigante | 25 | 12 | 4 | 8 | Frente | Corpo a corpo |
| Goblin | 40 | 16 | 8 | 10 | Frente | Corpo a corpo |
| Goblin arqueiro | 30 | 15 | 5 | 11 | Retaguarda | À distância |
| Lobo | 35 | 18 | 6 | 14 | Frente | Corpo a corpo |
| Esqueleto | 55 | 22 | 14 | 6 | Frente | Corpo a corpo |
| Esqueleto arqueiro | 40 | 20 | 10 | 8 | Retaguarda | À distância |
| Orc | 85 | 28 | 18 | 7 | Frente | Corpo a corpo |
| Xamã orc | 60 | 26 | 10 | 9 | Retaguarda | À distância, ignora 25% da defesa |
| Troll | 150 | 38 | 24 | 3 | Frente | Corpo a corpo |
| Chefe goblin (chefe N3) | 120 | 24 | 12 | 10 | Frente | Corpo a corpo |
| Senhor orc (chefe N4–6) | 220 | 36 | 24 | 8 | Frente | Corpo a corpo |
| Troll ancião (chefe N7–9) | 380 | 48 | 30 | 4 | Frente | Corpo a corpo |
| Dragão jovem (chefe N10) | 600 | 60 | 40 | 12 | Frente | Atinge 2 alvos por ataque |

Fonte: seção 8.3 (bíblia).

### Grupos de comuns por nível

| Níveis | Grupo de comuns |
|---|---|
| N1–2 | Rato gigante, Goblin |
| N3–4 | Goblin, Goblin arqueiro, Lobo |
| N5–6 | Lobo, Esqueleto, Esqueleto arqueiro, Orc |
| N7–8 | Esqueleto arqueiro, Orc, Xamã orc, Troll |
| N9–10 | Orc, Xamã orc, Troll |

Fonte: seção 8.3 (bíblia).

### Chefe por faixa de nível

| Níveis | Chefe |
|---|---|
| N3 | Chefe goblin |
| N4–6 | Senhor orc |
| N7–9 | Troll ancião |
| N10 | Dragão jovem |

Fonte: seção 8.3 (bíblia).

### Multiplicador M(N) por nível

| Nível | M(N) | Descrição |
|---|---|---|
| N1 | 1,0 | Atributos base |
| N2 | 1,15 | +15% |
| N3 | 1,30 | +30% |
| N4 | 1,45 | +45% |
| N5 | 1,60 | +60% |
| N6 | 1,75 | +75% |
| N7 | 1,90 | +90% |
| N8 | 2,05 | +105% |
| N9 | 2,20 | +120% |
| N10 | 2,35 | +135% |

Fonte: seção 8.3 (bíblia).

## Exemplos

### Exemplo N5

Masmorra nível 5:
- **Multiplicador**: M(5) = 1 + 0,15 × (5 − 1) = 1,60.
- **Comuns**: `min(8; 2 + 5) = 7` inimigos do grupo N5–6.
- **Chefe**: Senhor orc (nível 1 com M(5)).
- **Composição típica**: 4 Lobos, 2 Esqueletos, 1 Orc (ou similar, total 7) + 1 Senhor orc.
- **Atributos Senhor orc com M(5)**: PV = 220 × 1,60 = 352, ATQ = 36 × 1,60 = 58, DEF = 24 × 1,60 = 38, INI = 8 × 1,60 = 13 (arredondados).

## Interações com outros domínios

- [batalha.md](../v1-014-batalha/batalha.md) — atributos de combate usados no motor de batalha.
- [masmorras.md](masmorras.md) — composição gerada pelo nível.
- [tropas.md](../v1-013-quartel-e-tropas/tropas.md) — guerreiros da tropa enfrentam esses inimigos.

## Questões em aberto

- Nenhuma [proposta] adicional neste domínio.
