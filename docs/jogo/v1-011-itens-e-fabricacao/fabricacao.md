# Fabricação

**Épico:** [itens.md](itens.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Itens são fabricados nas oficinas (Ferraria, Alfaiataria, Carpintaria) por artesãos. Cada item tem nível máximo permitido pela oficina, requisitos de PE, custo que aumenta com o nível, qualidade sorteada e tempo de conclusão em pontos de fabricação.

## Regras

### Requisitos de fabricação

- R1: Requisitos para fabricar item [proposta]
  - Oficina correta para a categoria (tabelas 7.8–7.11)
  - Nível máximo da oficina permite L: N1 até L3; N2 até L6; N3 até L10 (seção 4.11)
  - Artesão alocado com **PE efetivo ≥ 2L − 2** na profissão da oficina
  - Recursos suficientes no estoque da vila

### Custo de fabricação

- R2: Custo = receita base × L, arredondado para cima [proposta]
- R3: Para L ≥ 6, todo "Ferro" na receita vira "Aço" [proposta]
- R4: Exemplo — Espada L1 = 3 Ferro + 1 Tábua; Espada L6 = 30 Aço + 10 Tábua (seção 7.8)

### Pontos de fabricação e tempo

- R5: Pontos de fabricação (PF) = 1 + L [proposta]
- R6: Progresso por turno = eficiência do artesão × multiplicador do nível da oficina [proposta]
  - Exemplo: L1 com eficiência 1,0 em N1 → 2 PF por turno → 2 turnos para concluir
  - N3 triplicaria o progresso → ~0,67 turnos (~1 turno com arredondamento)

### Qualidade sorteada

- R7: Qualidade determinada por margem `m = PE efetivo − (2L − 2)` [proposta]
- R8: Divina só em oficina N3; fora dela a chance de Divina passa para Excelente [proposta]

| Margem | Simples | Boa | Excelente | Divina |
|---|---|---|---|---|
| 0–4 | 70% | 25% | 5% | 0% |
| 5–9 | 55% | 33% | 11% | 1% |
| 10–14 | 40% | 38% | 19% | 3% |
| 15+ | 30% | 40% | 25% | 5% |

(Seção 7.4 da bíblia)

### Aprimoramento de item

- R9: Aprimorar L→L+1 (upgrade) na mesma oficina [proposta]
- R10: Custa 50% do custo de fabricar L+1; mesmos requisitos de L+1 [proposta]
- R11: Mantém qualidade, bônus intrínsecos e pedras engastadas [proposta]
- R12: Máximo L10 [proposta]

## Números e tabelas

### Tabela de custo por nível (exemplo — Espada)

| Nível | Multiplicador | Custo (Ferro/Aço + Tábua) | PF | Turnos ref. (Efic. 1,0, N1) |
|---|---|---|---|---|
| L1 | 1,0 | 3 Ferro + 1 Tábua | 2 | ~2 |
| L3 | 1,4 | 4 Ferro + 1 Tábua | 4 | ~4 |
| L5 | 1,8 | 9 Ferro + 5 Tábua | 6 | ~6 |
| L6 | 2,0 | 12 Aço + 6 Tábua | 7 | ~7 |
| L10 | 2,8 | 30 Aço + 10 Tábua | 11 | ~11 |

(Seção 7.8 da bíblia; arredondamento para cima em custo)

## Exemplos

### Exemplo completo: Espada L5

**Setup:**
- Artesão (Ferreiro) com PE efetivo 10 na profissão Ferreiro
- Oficina (Ferraria) nível N2 (mult. 1,2)
- Recursos: 9 Ferro + 5 Tábua disponíveis

**Cálculo:**
- Requisito: PE efetivo ≥ 2×5−2 = 8 ✓
- Custo: receita (3 Ferro + 1 Tábua) × 5 = 15 Ferro + 5 Tábua (arredondado, fica 15+5)
- Margem: m = 10 − 8 = 2 (faixa 0–4)
- Qualidade esperada: 70% Simples, 25% Boa, 5% Excelente
- PF: 1 + 5 = 6
- Progresso/turno: 1,0 × 1,2 = 1,2 PF/turno → ~5 turnos para concluir

Se Ferro → Aço (L ≥ 6), não se aplica aqui (L5 < 6).

### Exemplo completo: Aprimoramento Espada L5 → L6

**Setup:**
- Mesmo Ferreiro e Ferraria N2
- Item Espada L5 Boa com 1 pedra já engastada

**Cálculo:**
- Costo do aprimoramento: 50% × (receita L6 × 6) = 50% × (18 Aço + 6 Tábua) = 9 Aço + 3 Tábua
- Requisito: PE efetivo ≥ 2×6−2 = 10 ✓
- PF: 1 + 6 = 7
- Tempo: ~6 turnos (1,2 PF/turno em N2)
- Resultado: Espada L6 Boa com pedra mantida

## Interações com outros domínios

- [itens.md](itens.md) — categorias, qualidades, níveis, bônus
- [../../v1-003-construcoes/oficinas.md](../v1-003-construcoes/oficinas.md) — oficinas disponíveis
- [../../v1-002-cidadaos/cidadao.md](../v1-002-cidadaos/cidadao.md) — cidadãos (artesãos com PE)
- [../../v1-010-recursos-e-producao/recursos.md](../v1-010-recursos-e-producao/recursos.md) — estoque de recursos
- [../v1-012-pedras-de-bonus/pedras-de-bonus.md](../v1-012-pedras-de-bonus/pedras-de-bonus.md) — pedras engastáveis após fabricação

## Questões em aberto

- [proposta] Aprimoramento pode mudar de qualidade? (spec diz "mantém qualidade")
- [proposta] Há limite de itens em fabricação simultânea? (não mencionado)
