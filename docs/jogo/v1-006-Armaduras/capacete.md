# Capacete

**Épico:** [armaduras.md](armaduras.md) · **Domínio:** [armaduras.md](armaduras.md)

## Resumo

O Capacete é uma peça de proteção para a cabeça, reduzindo dano frontal. Fabricado na Ferraria, oferece defesa moderada e é mais acessível que o Peitoral em termos de custo.

## Oficina

Ferraria (qualquer nível; L máximo: N1 até L3, N2 até L6, N3 até L10).

## Receita e defesa por nível

| Nível | Receita (×L) | Defesa | Defesa (×1,8) L5 | Defesa (×2,8) L10 |
|---|---|---|---|---|
| L1 | 3 Ferro | 4 | — | — |
| L5 | 15 Ferro | 7,2 | 7,2 | — |
| L6 | 18 Aço | 8,4 | — | — |
| L10 | 30 Aço | 11,2 | — | 11,2 |

**Fórmula:** Defesa(L) = 4 × (1 + 0,2 × (L − 1)) (seção 7.3).

## Bônus intrínsecos permitidos

Sorteados do subconjunto: **VIT, DEF, VIDA, VEL** (seção 7.2 [proposta]).

| Qualidade | Nº de bônus |
|---|---|
| Simples | 0 |
| Boa | 1 |
| Excelente | 2 |
| Divina | 3 |

Faixa por nível: L1–4 baixa; L5–7 média; L8–10 alta.

## Exemplo

Artesão da Ferraria com PE efetivo 10, oficina N2:
- Fabricar Capacete L3: PE mínimo = 2 × 3 − 2 = 4 ✓ (10 ≥ 4, pode fabricar)
- Custo: 9 Ferro (3 × 3, arredondado)
- PF: 1 + 3 = 4
- Progresso por turno: 1,0 × 1,2 (N2) = 1,2 PF
- Tempo: 4 ÷ 1,2 ≈ 3,33 turnos → 4 turnos (arredondado)
- Qualidade (margem = 10 − 4 = 6, faixa 5–9): 55% Simples, 33% Boa, 11% Excelente, 1% Divina
- Defesa final: 4 × (1 + 0,2 × (3 − 1)) = 4 × 1,4 = 5,6

## Interações

- [armaduras.md](armaduras.md) — contexto das 6 peças e conjunto completo.
- [construcoes.md](../v1-003-construcoes/construcoes.md) — Ferraria é onde se fabrica.
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — Ferreiro é o profissional que trabalha em Ferraria.
- [batalha.md](../v1-014-batalha/batalha.md) — defesa do capacete reduz dano.
