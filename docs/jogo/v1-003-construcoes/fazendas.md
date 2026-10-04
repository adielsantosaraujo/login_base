# Fazendas

**Épico:** [construcoes.md](construcoes.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Existem duas tipos de Fazenda: **Fazenda de plantio** (Agricultor) e **Fazenda de criação** (Fazendeiro). A primeira produz Grãos ou Fibra (cultura escolhida). A segunda produz Carne e Couro (Gado) ou Lã e Carne (Ovelhas, rebanho escolhido). Tamanho da fazenda determina produção: N1 = 1x1, N2 = 2x2, N3 = 3x3.

## Regras

- **R1**: Fazenda de plantio requer profissão Agricultor; Fazenda de criação requer profissão Fazendeiro (seção 4.5).
- **R2**: Fazenda de plantio produz 6 Grãos **ou** 4 Fibra por trabalhador/turno (eficiência 1,0, N1); cultura escolhida pelo jogador; troca leva 1 turno sem produção (seção 4.5) [proposta].
- **R3**: Fazenda de criação produz (Gado) 3 Carne + 1 Couro **ou** (Ovelhas) 2 Lã + 1 Carne; rebanho escolhido (seção 4.5) [proposta].
- **R4**: Fazenda de plantio ocupa 1x1 (N1), 2x2 (N2), 3x3 (N3) ladrilhos em região Floresta ou Planície; Fazenda de criação ocupa os mesmos tamanhos apenas em região Planície (seção 1.4) [req].
- **R5**: Produção = `Σ (eficiência do trabalhador) × base × multiplicador do nível` (seção 4.3) [proposta].

## Números e tabelas

### Produção base por trabalhador/turno (eficiência 1,0, N1) [proposta]

| Fazenda | Opção | Produção/trabalhador/turno |
|---|---|---|
| Plantio | Grãos | 6 Grãos |
| Plantio | Fibra | 4 Fibra |
| Criação | Gado | 3 Carne + 1 Couro |
| Criação | Ovelhas | 2 Lã + 1 Carne |

### Custos por nível [proposta]

| Nível | Tamanho | Madeira | Grãos | PO |
|---|---|---|---|---|
| N1 | 1x1 | 15 | — | 4 |
| N2 | 2x2 | 37,5 (2,5×) | — | 10 |
| N3 | 3x3 | 75 (5×) | — | 20 |

**Fazenda de criação N1 adicional**: 20 Grãos (alimentação inicial do rebanho).

### Multiplicadores de produção [proposta]

| Nível | Multiplicador |
|---|---|
| N1 | ×1,0 |
| N2 | ×1,2 |
| N3 | ×1,5 |

## Exemplos

**Exemplo 1: Produção de Fazenda de plantio N1**
- Fazenda N1 com 2 Agricultores, cada um com PE 5 (eficiência 1,0).
- Produção: 2 × 1,0 × 6 Grãos × 1,0 = 12 Grãos/turno.

**Exemplo 2: Upgrade para N2**
- Custo N1→N2: 37,5 Madeira (arredonda para 38), 0 Grãos, 10 PO.
- Produção N2 com 2 mesmos Agricultores: 2 × 1,0 × 6 Grãos × 1,2 = 14,4 Grãos/turno.

**Exemplo 3: Mudança de cultura**
- Fazenda de plantio produzia Grãos. Jogador escolhe Fibra.
- 1º turno: sem produção (troca em andamento).
- 2º turno: inicia produção de Fibra (4 por trabalhador × eficiência × 1,0).

**Exemplo 4: Produção de rebanho**
- Fazenda de criação N1 com 3 Fazendeiros, cada um PE 4 (eficiência 0,9).
- Rebanho: Gado. Produção: 3 × 0,9 × (3 Carne + 1 Couro) × 1,0 = 2,7 Carne, 0,9 Couro/turno.

## Interações com outros domínios

- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — produção de alimentos
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — profissões Agricultor e Fazendeiro
- [construcoes.md](construcoes.md) — custos, PO, upgrade

## Modelo de dados

Fazenda é um tipo de `construcao`; campo `configuracao` (JSON) armazena:
- `cultura` (Grãos ou Fibra, para Plantio)
- `rebanho` (Gado ou Ovelhas, para Criação)

## Questões em aberto

- Limite de Fazendas por vila: há limite ou quantas desejar? (suponho ilimitado, dentro do espaço de regiões Rurais)
- Produção quando faltar trabalhador no turno de troca de cultura: 0 ou parcial?
