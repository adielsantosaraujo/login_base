# Comércios

**Épico:** [construcoes.md](construcoes.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

O Mercado é a instalação comercial onde o jogador negocia recursos com um mercador NPC. Comerciantes especializados determinam o volume máximo negociado por turno e influenciam preços de venda e compra. Comércio entre jogadores fica fora da v1.

## Regras

- **R1**: Mercado vende e compra recursos com mercador NPC (seção 4.9) [proposta].
- **R2**: Comércio entre jogadores fica fora da v1 (seção 4.9) [proposta].
- **R3**: Vagas: N1 = 2, N2 = 5, N3 = 10 Comerciantes (seção 4.9, tabela de vagas) [proposta].
- **R4**: Volume máximo por turno = `20 × Σ eficiência dos Comerciantes × mult. nível` unidades (seção 4.9) [proposta].
- **R5**: Preço de venda = `preço base × min(1,0; 0,5 + 0,02 × PE Comerciante do melhor)` (seção 4.9) [proposta].
- **R6**: Preço de compra = `preço base × max(1,0; 1,5 − 0,02 × PE Comerciante do melhor)` (seção 4.9) [proposta].
- **R7**: Ordens executadas imediatamente, consumindo volume do turno corrente (seção 4.9) [proposta].
- **R8**: Sem Mercado ativo, negociação é rejeitada (seção 4.9) [proposta].
- **R9**: Mercado ocupa 1x1 (N1), 2x2 (N2), 3x3 (N3) em Região Urbana (seção 1.4) [req].

## Números e tabelas

### Custos por nível [proposta]

| Nível | Tamanho | Tábua | Pedra | PO |
|---|---|---|---|---|
| N1 | 1x1 | 30 | 20 | 6 |
| N2 | 2x2 | 75 (2,5×) | 50 (2,5×) | 15 |
| N3 | 3x3 | 150 (5×) | 100 (5×) | 30 |

### Preços base [proposta]

Ver [comercio.md](../v1-010-recursos-e-producao/comercio.md) para lista completa. Resumo:
- Bruto: 1-4 Ouro
- Processado: 3-20 Ouro
- Alimento: 1 Ouro

## Exemplos

**Exemplo 1: Volume disponível**
- Mercado N1 com 2 Comerciantes: um PE 3 (eficiência 0,8), outro PE 5 (eficiência 1,0).
- Volume: `20 × (0,8 + 1,0) × 1,0 = 20 × 1,8 = 36` unidades/turno.

**Exemplo 2: Preço de venda com PE bom**
- Melhor Comerciante: PE 10 (diferença +2).
- Preço base Madeira: 1 Ouro.
- Preço venda: `1 × min(1,0; 0,5 + 0,02 × 10) = 1 × min(1,0; 0,7) = 0,7 Ouro` por Madeira.

**Exemplo 3: Preço de compra com PE bom**
- Melhor Comerciante: PE 10 (diferença +2).
- Preço base Ferro: 6 Ouro.
- Preço compra: `6 × max(1,0; 1,5 − 0,02 × 10) = 6 × max(1,0; 1,3) = 6 × 1,3 = 7,8 Ouro` por Ferro.

**Exemplo 4: Volume insuficiente**
- Mercado N1 (36 unidades/turno). Jogador quer vender 50 Madeira.
- Rejeitado: sobrepassa volume. Aceita até 36 Madeira este turno; resto fila para próximo turno (ou rejeita tudo?).

## Interações com outros domínios

- [comercio.md](../v1-010-recursos-e-producao/comercio.md) — preços, exemplos de negociação
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — profissão Comerciante, PE
- [construcoes.md](construcoes.md) — custos, PO

## Modelo de dados

Mercado é um tipo de `construcao`. Ordens de venda/compra em tabela `ordem_comercio`: vila_id, tipo (VENDA/COMPRA), recurso, quantidade, ouro_ofertado/recebido, turno, status.

## Questões em aberto

- Volume insuficiente: fila ordem para próximo turno ou rejeita imediatamente?
- Limite de Mercados por vila: há limite?
- Comissão ou imposto em transação: zero na v1?
