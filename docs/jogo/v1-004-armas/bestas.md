# Bestas

**Épico:** [armas.md](armas.md) · **Domínio:** [v1-003-construcoes/oficinas.md](../v1-003-construcoes/oficinas.md)

## Resumo
Bestas são armas à distância fabricadas na Carpintaria, focadas em Inteligência e capazes de ignorar parte da defesa do inimigo. Oferecem o maior ataque base entre armas à distância, com penalidade de iniciativa.

## Características técnicas
- **Oficina:** Carpintaria (N1, N2 ou N3).
- **Receita base:** 2 Ferro, 3 Tábua, 1 Tecido (multiplicado por nível L).
- **Ataque base:** 13 (multiplicador por nível: ×1,0 em L1, ×1,8 em L5, ×2,8 em L10).
- **Atributo-chave:** Inteligência (INT) — ataque aumenta 5% por ponto de INT.
- **Modificador de iniciativa:** −4 (penaliza ordem de ação, refletindo tempo de recarga).
- **Alcance:** À distância, qualquer alvo (pode atacar frente ou retaguarda inimiga, veja seção 10.3).
- **Especial:** Ignora 25% da Defesa do alvo (Defesa_efetiva = Defesa × 0,75 no cálculo de dano).
- **Bônus intrínsecos permitidos (qualidades Boa+):** FOR, VEL, INT, ATK, CRIT, INI (faixa conforme nível: L1–4 baixa; L5–7 média; L8–10 alta).

## Tabela de custo e ataque por nível

| Nível | Receita (×nível) | Ataque | Notas |
|---|---|---|---|
| L1 | 2 Ferro, 3 Tábua, 1 Tecido | 13 | Até L5, receita em Ferro; L6+ troca para Aço |
| L3 | 6 Ferro, 9 Tábua, 3 Tecido | 20,28 |  |
| L5 | 10 Ferro, 15 Tábua, 5 Tecido | 23,4 |  |
| L6 | 12 **Aço**, 18 Tábua, 6 Tecido | 26,52 | Troca: Ferro → Aço a partir daqui |
| L10 | 20 **Aço**, 30 Tábua, 10 Tecido | 36,4 |  |

Fórmula: Ataque(L) = 13 × (1 + 0,2 × (L − 1)) → L1 ×1,0; L5 ×1,8; L10 ×2,8.

## Exemplo de ataque
**Guerreiro com Besta L5 (INT 7):**
- FOR 5, VIT 5, VEL 4, PE Guerreiro base 6 → G = 6 + 1 (FOR) + 1 (VIT) + 1 (INT) + 1 (VEL) = 10.
- Besta L5 ataque 23,4.
- Ataque = 23,4 × (1 + 0,05 × 7) + 2 × 10 = 23,4 × 1,35 + 20 = 31,59 + 20 = 51,59 ≈ 51,6.
- Contra um Esqueleto (PV 55, DEF 14 × M(5) ≈ 22,4):
  - Defesa_efetiva (com especial de besta) = 22,4 × 0,75 = 16,8.
  - Dano = 51,6 × 100 ÷ (100 + 3 × 16,8) ≈ 51,6 × 100 ÷ 150,4 ≈ 34,3 por golpe → Esqueleto cai em 2 golpes.

## Vantagem tática
A besta oferece o maior ataque base e ignora 25% da defesa, compensando sua penalidade de iniciativa (−4) com dano massivo. Ideal contra inimigos fortemente blindados. A penalidade de iniciativa reflete o tempo necessário para recarregar entre disparos.

## Fabricação
Mesmo processo que demais armas (seção 7.4 da bíblia):
- Requisito: artesão em Carpintaria com PE efetivo ≥ 2L − 2.
- Custo: receita base × L.
- Pontos de fabricação: 1 + L; progresso por turno = eficiência × mult. nível.
- Qualidade sorteada ao concluir, conforme margem de PE.

## Questões em aberto
- Nenhuma deste tipo de arma.
