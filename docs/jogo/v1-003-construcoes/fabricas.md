# Fábricas

**Épico:** [construcoes.md](construcoes.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Fábricas processam recursos brutos em produtos acabados ou intermediários através de receitas fixas. Cada fábrica tem profissão específica, ciclos de produção e recursos consumidos. A vila começa com a possibilidade de construir 6 tipos de fábrica em regiões Urbanas: Serraria, Olaria, Fundição, Tecelagem, Curtume, Cozinha.

## Regras

- **R1**: Cada fábrica tem receita fixa (insumos → produtos) executada em ciclos (seção 4.5) [proposta].
- **R2**: Ciclos por trabalhador/turno (eficiência 1,0, N1) listados na tabela (seção 4.5) [proposta].
- **R3**: Se faltar insumo, executa ciclos possíveis (seção 4.5) [proposta].
- **R4**: Produção = `Σ (eficiência) × ciclos × multiplicador do nível` (seção 4.3, 4.5) [proposta].
- **R5**: Fundição N2+ permite fabricar Aço (seção 4.5) [proposta].
- **R6**: Fábrica ocupa 1x1 (N1), 2x2 (N2), 3x3 (N3) em Região Urbana (seção 1.4) [req].
- **R7**: Fábricas precisam de trabalhadores especializados para funcionar (seção 4.1) [req].

## Números e tabelas

### Receitas e ciclos [proposta]

| Fábrica | Profissão | Receita | Ciclos/trabalhador/turno |
|---|---|---|---|
| Serraria | Madeireiro | 2 Madeira → 1 Tábua | 3 |
| Olaria | Construtor | 2 Argila + 1 Madeira → 2 Tijolo | 2 |
| Fundição (Ferro) | Ferreiro | 2 Minério de ferro + 1 Carvão → 1 Ferro | 2 |
| Fundição (Aço, N2+) | Ferreiro | 2 Ferro + 1 Carvão + 1 Enxofre → 1 Aço | 1 |
| Tecelagem | Costureiro | 2 Fibra **ou** 2 Lã → 1 Tecido | 2 |
| Curtume | Costureiro | 2 Couro + 1 Sal → 1 Couro curtido | 2 |
| Cozinha | Cozinheiro | 2 Grãos + 1 Carne → 5 Refeição | 2 |

### Custos por nível [proposta]

| Fábrica | Nível | Madeira | Pedra | Tábua | Argila | Tijolo | Ferro | PO |
|---|---|---|---|---|---|---|---|---|
| Serraria | N1 | 30 | 10 | — | — | — | — | 6 |
| Olaria | N1 | 20 | 20 | — | 10 | — | — | 6 |
| Fundição | N1 | — | 30 | 20 | — | 20 | — | 8 |
| Tecelagem | N1 | — | 10 | 20 | — | — | — | 6 |
| Curtume | N1 | — | — | 20 | — | 10 | — | 6 |
| Cozinha | N1 | — | — | 15 | — | 15 | — | 6 |

Custos N2 = 2,5×, N3 = 5×.

## Exemplos

**Exemplo 1: Serraria com 2 Madeireiros**
- Serraria N1 com 2 Madeireiros: um PE 3 (eficiência 0,8), outro PE 5 (eficiência 1,0).
- Ciclos: 0,8 × 3 + 1,0 × 3 = 5,4 ciclos/turno.
- Produção: 5,4 × 1 Tábua × 1,0 = 5,4 Tábuas/turno.
- Consumo: 5,4 × 2 Madeira = 10,8 Madeira/turno (arredonda em UI).

**Exemplo 2: Fundição N1 Ferro com insumo faltando**
- Fundição N1 com 1 Ferreiro (eficiência 1,0): 2 ciclos possíveis/turno.
- Insumo: 3 Minério de ferro, 2 Carvão (suficiente para 1 ciclo apenas).
- Resultado: 1 Ferro produzido (Minério: 2 consumido, Carvão: 1 consumido).
- Restam: 1 Minério, 1 Carvão não utilizados.

**Exemplo 3: Fundição N2+ Aço**
- Fundição N2 com 2 Ferreiros (eficiência média 1,0).
- Ciclos Aço: 1 ciclo × (2 × 1,0) × 1,2 = 2,4 ciclos/turno (arredonda para 2 ou 2,4?).
- Produção: 2,4 × 1 Aço = 2,4 Aço/turno.
- Consumo: 2,4 × (2 Ferro + 1 Carvão + 1 Enxofre).

**Exemplo 4: Cozinha com trabalhadores insuficientes**
- Cozinha N1 com 1 Cozinheiro (eficiência 0,6).
- Ciclos: 0,6 × 2 = 1,2/turno.
- Produção: 1,2 × 5 Refeição = 6 Refeições.
- Consumo: 1,2 × (2 Grãos + 1 Carne) = 2,4 Grãos, 1,2 Carne.

## Interações com outros domínios

- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — produção, receitas, recursos
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — profissões especializadas
- [construcoes.md](construcoes.md) — custos, PO, alocação
- [turnos.md](../v1-009-turnos/turnos.md) — produção resolvida no passo 1 do turno

## Modelo de dados

Fábrica é um tipo de `construcao`; campo `configuracao` (JSON) armazena `receita` selecionada (Ferro ou Aço em Fundição; Fibra ou Lã em Tecelagem).

## Questões em aberto

- Arredondamento de ciclos fracionários para UI: sempre piso, ou apresentar decimal?
- Troca de receita em Fundição e Tecelagem: custa turno sem produção como em Fazenda?
