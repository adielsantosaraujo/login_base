# Lanças

**Épico:** [armas.md](armas.md) · **Domínio:** [v1-003-construcoes/oficinas.md](../v1-003-construcoes/oficinas.md)

## Resumo
Lanças são armas de corpo a corpo fabricadas na Ferraria, únicas por poderem atacar tanto da frente quanto da retaguarda. Seu atributo-chave é a média entre Força e Velocidade, oferecendo versatilidade tática.

## Características técnicas
- **Oficina:** Ferraria (N1, N2 ou N3).
- **Receita base:** 2 Ferro, 2 Tábua (multiplicado por nível L).
- **Ataque base:** 9 (multiplicador por nível: ×1,0 em L1, ×1,8 em L5, ×2,8 em L10).
- **Atributo-chave:** Média de Força (FOR) e Velocidade (VEL) — ataque aumenta 5% por ponto (bônus aplicado à média).
- **Modificador de iniciativa:** +1.
- **Alcance:** Corpo a corpo, pode atacar da frente ou da retaguarda (veja seção 10.3).
- **Especial:** Nenhum bônus especial ao portador.
- **Bônus intrínsecos permitidos (qualidades Boa+):** FOR, VEL, INT, ATK, CRIT, INI (faixa conforme nível: L1–4 baixa; L5–7 média; L8–10 alta).

## Tabela de custo e ataque por nível

| Nível | Receita (×nível) | Ataque | Notas |
|---|---|---|---|
| L1 | 2 Ferro, 2 Tábua | 9 | Até L5, receita em Ferro; L6+ troca para Aço |
| L3 | 6 Ferro, 6 Tábua | 14,04 |  |
| L5 | 10 Ferro, 10 Tábua | 16,2 |  |
| L6 | 12 **Aço**, 12 Tábua | 18,72 | Troca: Ferro → Aço a partir daqui |
| L10 | 20 **Aço**, 20 Tábua | 25,2 |  |

Fórmula: Ataque(L) = 9 × (1 + 0,2 × (L − 1)) → L1 ×1,0; L5 ×1,8; L10 ×2,8.

## Exemplo de ataque
**Guerreiro com Lança L5 (FOR 7, VEL 6):**
- VIT 5, PE Guerreiro base 5 → G = 5 + 1 (FOR) + 1 (VEL) + 1 (VIT) = 8.
- Média de FOR e VEL = (7 + 6) / 2 = 6,5.
- Lança L5 ataque 16,2.
- Ataque = 16,2 × (1 + 0,05 × 6,5) + 2 × 8 = 16,2 × 1,325 + 16 = 21,465 + 16 = 37,465 ≈ 37,5.
- Contra um Lobo (PV 35, DEF 6 × M(5) ≈ 9,6): dano = 37,5 × 100 ÷ (100 + 3 × 9,6) ≈ 35 por golpe → Lobo cai em 1 golpe.

## Posicionamento tático
Diferente de espadas e arcos, a lança permite ataque tanto da frente quanto da retaguarda, oferecendo flexibilidade na formação da tropa. Um portador de lança na retaguarda ainda contribui com dano.

## Fabricação
Mesmo processo que demais armas (seção 7.4 da bíblia):
- Requisito: artesão em Ferraria com PE efetivo ≥ 2L − 2.
- Custo: receita base × L.
- Pontos de fabricação: 1 + L; progresso por turno = eficiência × mult. nível.
- Qualidade sorteada ao concluir, conforme margem de PE.

## Questões em aberto
- Nenhuma deste tipo de arma.
