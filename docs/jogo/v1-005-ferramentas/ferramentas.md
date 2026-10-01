# Ferramentas

**Épico:** [v1-005-ferramentas] · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Ferramentas especializadas equipáveis por cidadãos para aumentar sua eficiência no trabalho. Cada profissão não-guerreira tem uma ferramenta dedicada que oferece bônus de pontos de profissão durante o trabalho. O efeito é proporcional ao nível da ferramenta (L1–L10) e só se aplica quando a pessoa trabalha na sua profissão.

## Regras

- R1: Cada pessoa pode equipar 1 ferramenta (slot único) [seção 7.1].
- R2: Bônus de ferramenta: **+L PE na profissão** enquanto a pessoa trabalha nela (ex.: Enxada L3 → +3 PE de Agricultor) [seção 7.9].
- R3: PE efetivo = PE base + bônus de ferramenta + bônus de itens/pedras + bônus de característica [seção 5.2].
- R4: Eficiência = (0,5 + 0,1 × PE efetivo) limitada a **3,0**, multiplicada por multiplicadores da seção 5.3.
- R5: Ferramentas só funcionam na profissão para a qual foram criadas; bônus não se aplica em outras profissões.

## Números e tabelas

Tabela de ferramentas (seção 7.9 da bíblia):

| Ferramenta | Profissão | Oficina | Receita base (×L) |
|---|---|---|---|
| Martelo | Construtor | Ferraria | 1 Ferro, 1 Tábua |
| Carrinho de mão | Carregador | Carpintaria | 3 Tábua, 1 Ferro |
| Enxada | Agricultor | Ferraria | 1 Ferro, 1 Tábua |
| Forcado | Fazendeiro | Ferraria | 1 Ferro, 1 Tábua |
| Picareta | Mineiro | Ferraria | 2 Ferro, 1 Tábua |
| Machado | Madeireiro | Ferraria | 2 Ferro, 1 Tábua |
| Malho | Ferreiro | Ferraria | 2 Ferro |
| Cutelo | Cozinheiro | Ferraria | 1 Ferro |
| Kit de costura | Costureiro | Alfaiataria | 1 Ferro, 1 Tecido |
| Faca de caça | Caçador | Ferraria | 1 Ferro, 1 Couro curtido |
| Balança | Comerciante | Carpintaria | 2 Tábua, 1 Ferro |

Fórmula de atributo principal [seção 7.3]:
`atributo principal(L) = base × (1 + 0,2 × (L − 1))` → L1 ×1,0; L5 ×1,8; L10 ×2,8.

Para ferramentas: bônus de PE = **+L** na profissão.

## Exemplos

**Exemplo 1: Enxada L3 no trabalho de Agricultor**
- Agricultor tem PE base 3 na profissão, Enxada L3 (+3 PE).
- PE efetivo = 3 + 3 = 6.
- Eficiência = 0,5 + 0,1 × 6 = 1,1 (sem multiplicadores adicionais).
- Sem a ferramenta: eficiência = 0,5 + 0,1 × 3 = 0,8.
- Ganho: +0,3 de eficiência na Fazenda de plantio.

**Exemplo 2: Martelo L5 no trabalho de Construtor**
- Construtor tem PE base 2 na profissão, Martelo L5 (+5 PE).
- PE efetivo = 2 + 5 = 7.
- Eficiência = 0,5 + 0,1 × 7 = 1,2.
- Sem a ferramenta: eficiência = 0,5 + 0,1 × 2 = 0,7.
- Ganho: +0,5 de eficiência em obras e Olaria.

## Interações com outros domínios

- [cidadao.md](../v1-002-cidadaos/cidadao.md) — PE efetivo, eficiência no trabalho (5.2, 5.3).
- [construcoes.md](../v1-003-construcoes/construcoes.md) — alocação de trabalhadores, profissões, eficiência.
- [itens.md](../v1-011-itens-e-fabricacao/itens.md) — sistema comum de fabricação, níveis, qualidade (7.1–7.4).

## Modelo de dados (resumo)

Ferramentas são itens normais com categoria "Ferramenta" e subtipo um dos 11 nomes. Fabricação via oficinas (Ferraria ou Carpintaria) conforme a tabela 7.9. Equipamento em slot único via painel da pessoa.

## Histórias

- [H-001 — Fabricar ferramentas](historia/h-001-fabricar-ferramentas.md)
- [H-002 — Aplicar bônus da ferramenta no trabalho](historia/h-002-aplicar-bonus-da-ferramenta-no-trabalho.md)

## Questões em aberto

- Nenhuma neste épico.
