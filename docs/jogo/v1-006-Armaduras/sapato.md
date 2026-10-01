# Sapato

**Épico:** [armaduras.md](armaduras.md) · **Domínio:** [armaduras.md](armaduras.md)

## Resumo

Sapatos protegem os pés e oferecem agilidade em combate. Fabricados em Alfaiataria com Couro curtido, são a única peça de armadura que fornece um bônus adicional: +1 de Iniciativa, melhorando a ordem de ataque do guerreiro.

## Oficina

Alfaiataria (qualquer nível; L máximo: N1 até L3, N2 até L6, N3 até L10).

## Receita e defesa por nível

| Nível | Receita (×L) | Defesa | Defesa (×1,8) L5 | Defesa (×2,8) L10 | Iniciativa extra |
|---|---|---|---|---|---|
| L1 | 2 Couro curtido | 2 | — | — | +1 |
| L5 | 10 Couro curtido | 3,6 | 3,6 | — | +1 |
| L6 | 12 Couro curtido | 4,2 | — | — | +1 |
| L10 | 20 Couro curtido | 5,6 | — | 5,6 | +1 |

**Fórmula:** Defesa(L) = 2 × (1 + 0,2 × (L − 1)) (seção 7.3); Iniciativa = +1 (constante, seção 7.10).

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

Costureira da Alfaiataria com PE efetivo Costureiro de 9, oficina N1:
- Fabricar Sapato L2: PE mínimo = 2 × 2 − 2 = 2 ✓ (9 ≥ 2, pode fabricar)
- Custo: 4 Couro curtido
- PF: 1 + 2 = 3
- Progresso por turno: 1,0 × 1,0 (N1) = 1,0 PF
- Tempo: 3 ÷ 1,0 = 3 turnos
- Qualidade (margem = 9 − 2 = 7, faixa 5–9): 55% Simples, 33% Boa, 11% Excelente, 1% Divina
- Defesa final: 2 × (1 + 0,2 × (2 − 1)) = 2 × 1,2 = 2,4
- **Iniciativa ganho permanente: +1**

## Interações

- [armaduras.md](armaduras.md) — contexto das 6 peças, única com bônus extra de Iniciativa.
- [construcoes.md](../v1-003-construcoes/construcoes.md) — Alfaiataria é onde se fabrica.
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — Costureiro é o profissional que trabalha em Alfaiataria.
- [batalha.md](../v1-014-batalha/batalha.md) — defesa do sapato reduz dano; Iniciativa afeta ordem de ataque.
