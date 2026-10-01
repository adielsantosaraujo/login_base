# Batalha por rodadas

**Épico:** [batalha.md](batalha.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Sistema de combate por rodadas entre tropas do jogador e inimigos de masmorras. Cálculo de atributos de combate, resolução de dano com críticos, ordem de iniciativa, condições de vitória/derrota e consequências (ferimentos, mortes, saque).

## Regras

- R1: Batalha é resolvida em **rodadas** independentes do turno do jogo; uma batalha inteira ocorre no passo 8 do turno de processamento [req].
- R2: **Máximo de 30 rodadas** por batalha; se nenhum lado for eliminado, a tropa recua (conta como derrota) [proposta].
- R3: Ordem de ação em cada rodada por **Iniciativa** (desempate: maior VEL, depois menor ID) [proposta].
- R4: **Corpo a corpo** atinge só a linha de frente inimiga enquanto houver alguém vivo nela; espada só ataca se o portador estiver na frente [proposta].
- R5: **Lança** ataca de qualquer linha; **ataques à distância** (arco, besta) atingem qualquer alvo [proposta].
- R6: Escolha de alvo: tropa do jogador mira inimigo com **menor PV atual**; inimigos miram alvo aleatório [proposta].
- R7: **Crítico**: dano ×1,5; chance = 5% + 0,5% × VEL + Σ CRIT [proposta].
- R8: **Ferido**: abatido com 0 PV fica 6 turnos sem trabalhar nem lutar [proposta].
- R9: Membro com 0 PV é "abatido"; após batalha: vitória → 80% ferido / 20% morto; derrota → 50% / 50% [proposta].
- R10: Itens de mortos voltam ao inventário na vitória; são perdidos na derrota [proposta].
- R11: Semente da batalha gravada para replay reprodutível [proposta].

## Números e tabelas

### Atributos de combate (seção 10.1)
G = PE efetivo de Guerreiro; características = totais (base + itens + pedras).

- **PV máx.** = `30 + 5 × VIT + 3 × G + Σ VIDA`
- **Ataque** = `[ATQarma(L) × (1 + 0,05 × atributo-chave) + 2 × G] × (1 + Σ ATK%)`
- **Defesa** = `[Σ DEFpeça(L) + VIT + G + 2 (se espada)] × (1 + Σ DEF%)`
- **Iniciativa** = `2 × VEL + G + mod. arma + Σ INI (+1 do sapato)`; a cada rodada soma 1d6
- **Crítico** = `5% + 0,5% × VEL + Σ CRIT`; dano crítico ×1,5

### Dano (seção 10.2)
```
dano = max(1; round(Ataque × 100 ÷ (100 + 3 × Defesa_efetiva) × U(0,9; 1,1)))
Defesa_efetiva = Defesa × 0,75 se o atacante for Besta ou Xamã orc
```

### Armas (seção 7.8)
| Arma | Alcance | Atributo-chave | Mod. iniciativa | Especial |
|---|---|---|---|---|
| Espada | Corpo a corpo, só frente | FOR | 0 | +2 Defesa ao portador |
| Lança | Corpo a corpo, frente ou retaguarda | média FOR e VEL | +1 | — |
| Arco | À distância, qualquer alvo | VEL | +2 | — |
| Besta | À distância, qualquer alvo | INT | −4 | Ignora 25% da defesa |

### Armaduras (seção 7.10)
Conjunto completo base = 24 de defesa; L5 ×1,8 = 43,2; L10 ×2,8 = 67,2.

### Joias (seção 7.11)
- Colar: +5 × L de Vida
- Anel: + característica escolhida ao fabricar; L1–3 +1; L4–6 +2; L7–9 +3; L10 +4

### Inimigos por nível (seção 8.3 — tabela resumida)
Multiplicador `M(N) = 1 + 0,15 × (N − 1)` (N1 1,0; N5 1,6; N10 2,35).
Composição: `min(8; 2 + N)` comuns, + 1 chefe a partir de N3. Atributos da tabela × M(N).

| Inimigo | PV | ATQ | DEF | INI | Linha |
|---|---|---|---|---|---|
| Rato gigante | 25 | 12 | 4 | 8 | Frente |
| Goblin | 40 | 16 | 8 | 10 | Frente |
| Goblin arqueiro | 30 | 15 | 5 | 11 | Retaguarda |
| Lobo | 35 | 18 | 6 | 14 | Frente |
| Esqueleto | 55 | 22 | 14 | 6 | Frente |
| Esqueleto arqueiro | 40 | 20 | 10 | 8 | Retaguarda |
| Orc | 85 | 28 | 18 | 7 | Frente |
| Xamã orc | 60 | 26 | 10 | 9 | Retaguarda |
| Troll | 150 | 38 | 24 | 3 | Frente |
| Chefe goblin (N3) | 120 | 24 | 12 | 10 | Frente |
| Senhor orc (N4–6) | 220 | 36 | 24 | 8 | Frente |
| Troll ancião (N7–9) | 380 | 48 | 30 | 4 | Frente |
| Dragão jovem (N10) | 600 | 60 | 40 | 12 | Frente |

## Exemplos

### Exemplo 10.4 — Guerreiro vs. Goblin
Guerreiro: FOR 6, VIT 5, VEL 5, PE Guerreiro base 5 → G = 5 + 1 + 1 + 1 = 8; Espada L1; sem armadura.
- Ataque = 10 × 1,30 + 16 = 29
- Defesa = 5 + 8 + 2 = 15
- PV = 30 + 25 + 24 = 79

Goblin: ATQ 16, DEF 8, PV 40.
- Dano em Goblin (DEF 8): 29 × 100 ÷ 124 ≈ 23 → Goblin (40 PV) cai em 2 golpes.
- Dano do Goblin (ATQ 16): 16 × 100 ÷ 145 ≈ 11 → guerreiro aguenta ~7 golpes.

## Interações com outros domínios

- [Quartel e tropas](../v1-013-quartel-e-tropas/tropas.md) — formação, posições, participantes, estados da tropa.
- [Masmorras](../v1-001-masmorras/masmorras.md) — geração de inimigos, recompensas, limpeza de masmorra.
- [Itens](../v1-011-itens-e-fabricacao/itens.md) — armas, armaduras, joias, bônus intrínsecos.
- [Pedras de bônus](../v1-012-pedras-de-bonus/pedras-de-bonus.md) — bônus em itens/pedras.
- [Cidadãos](../v1-002-cidadaos/cidadao.md) — características, pontos de profissão, morte, ferimentos.
- [Turnos](../v1-009-turnos/turnos.md) — batalha ocorre no passo 8 do turno (seção 2.3).

## Modelo de dados (resumo)

Tabela `batalha` (11.2):
- id, vila_id, tropa_id, masmorra_id, turno, semente, resultado, log (jsonb), recompensas (jsonb)

Log gravado: participantes, atributos iniciais, ações por rodada (atacante, alvo, dano, crítico, abatido).

## Histórias

- [H-001 — Resolver batalha por rodadas](historia/h-001-resolver-batalha-por-rodadas.md)
- [H-002 — Ver relatório de batalha](historia/h-002-ver-relatorio-de-batalha.md)

## Questões em aberto

- Múltiplos alvos do Dragão jovem (aumento de complexidade do motor).
- Bônus dinâmicos (PROD, ATK, DEF) aplicados por itens/pedras — centralização no catálogo.
