# Espadas

**Épico:** [armas.md](armas.md) · **Domínio:** [v1-003-construcoes/oficinas.md](../v1-003-construcoes/oficinas.md)

## Resumo
Espadas são armas de corpo a corpo fabricadas na Ferraria, focadas em ataque com Força como atributo-chave. Oferecem bônus de defesa ao portador e atacam apenas da linha de frente.

## Características técnicas
- **Oficina:** Ferraria (N1, N2 ou N3).
- **Receita base:** 3 Ferro, 1 Tábua (multiplicado por nível L).
- **Ataque base:** 10 (multiplicador por nível: ×1,0 em L1, ×1,8 em L5, ×2,8 em L10).
- **Atributo-chave:** Força (FOR) — ataque aumenta 5% por ponto de FOR.
- **Modificador de iniciativa:** 0 (sem alteração).
- **Alcance:** Corpo a corpo, só da linha de frente (veja seção 10.3 para detalhes de posicionamento).
- **Especial:** +2 de Defesa ao portador (stacks com armaduras).
- **Bônus intrínsecos permitidos (qualidades Boa+):** FOR, VEL, INT, ATK, CRIT, INI (faixa conforme nível: L1–4 baixa; L5–7 média; L8–10 alta).

## Tabela de custo e ataque por nível

| Nível | Receita (×nível) | Ataque | Notas |
|---|---|---|---|
| L1 | 3 Ferro, 1 Tábua | 10 | Até L5, receita em Ferro; L6+ troca para Aço |
| L3 | 9 Ferro, 3 Tábua | 15,6 |  |
| L5 | 15 Ferro, 5 Tábua | 18 |  |
| L6 | 18 **Aço**, 6 Tábua | 20,8 | Troca: Ferro → Aço a partir daqui |
| L10 | 30 **Aço**, 10 Tábua | 28 |  |

Fórmula: Ataque(L) = 10 × (1 + 0,2 × (L − 1)) → L1 ×1,0; L5 ×1,8; L10 ×2,8.

## Exemplo de ataque
**Guerreiro com Espada L5:**
- FOR 8, VIT 6, VEL 4, PE Guerreiro base 6 → G = 6 + 1 (FOR) + 1 (VIT) + 1 (VEL) = 9.
- Espada L5 ataque 18.
- Ataque = 18 × (1 + 0,05 × 8) + 2 × 9 = 18 × 1,40 + 18 = 25,2 + 18 = 43,2.
- Contra um Orc (PV 85, DEF 18 × M(5) ≈ 28,8): dano = 43,2 × 100 ÷ (100 + 3 × 28,8) ≈ 32 por golpe.

## Fabricação
Mesmo processo que demais armas (seção 7.4 da bíblia):
- Requisito: artesão em Ferraria com PE efetivo ≥ 2L − 2.
- Custo: receita base × L.
- Pontos de fabricação: 1 + L; progresso por turno = eficiência × mult. nível.
- Qualidade sorteada ao concluir, conforme margem de PE.

## Questões em aberto
- Nenhuma deste tipo de arma.
