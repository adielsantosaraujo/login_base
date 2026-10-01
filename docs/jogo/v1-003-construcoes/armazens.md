# Armazéns

**Épico:** [construcoes.md](construcoes.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Armazéns aumentam a capacidade de armazenamento de recursos da vila. Cada nível oferece mais espaço, multiplicado pela eficiência dos Carregadores alocados. Sem Carregadores mínimos, o Armazém não contribui.

## Regras

- **R1**: Armazém requer profissão Carregador (seção 4.3, 4.4) [req].
- **R2**: Capacidade base da vila é 500 por recurso (Ouro ilimitado) (seção 3.3) [proposta].
- **R3**: Cada Armazém adiciona capacidade extra: N1 +500, N2 +1.500, N3 +4.000, multiplicada por `min(1,5; eficiência média dos Carregadores alocados)` (seção 3.3) [proposta].
- **R4**: Mínimo de Carregadores para Armazém funcionar: N1 = 1, N2 = 2, N3 = 4 (seção 3.3) [proposta].
- **R5**: Sem mínimo, o Armazém não soma nada à capacidade (seção 3.3) [proposta].
- **R6**: Itens e pedras ficam em inventário da vila, sem limite (seção 3.3) [proposta].
- **R7**: Armazém ocupa 1x1 (N1), 2x2 (N2), 3x3 (N3) em Região Urbana (seção 1.4) [req].

## Números e tabelas

### Custos por nível [proposta]

| Nível | Tamanho | Madeira | Pedra | PO |
|---|---|---|---|---|
| N1 | 1x1 | 30 | 20 | 6 |
| N2 | 2x2 | 75 (2,5×) | 50 (2,5×) | 15 |
| N3 | 3x3 | 150 (5×) | 100 (5×) | 30 |

### Capacidade adicional [proposta]

| Nível | Capacidade base | Com eficiência 1,0 | Com eficiência 1,5 (máx.) |
|---|---|---|---|
| N1 | +500 | +500 | +750 |
| N2 | +1.500 | +1.500 | +2.250 |
| N3 | +4.000 | +4.000 | +6.000 |

## Exemplos

**Exemplo 1: Armazém N1 com 1 Carregador**
- Armazém N1 com Carregador de eficiência 0,8.
- Capacidade adicional: `0,8 × 500 = 400` por recurso.
- Capacidade total da vila: 500 (base) + 400 = 900 por recurso.

**Exemplo 2: Armazém N2 com mínimo de Carregadores**
- Armazém N2 com 2 Carregadores: um com eficiência 0,7, outro com 0,9.
- Eficiência média: (0,7 + 0,9) / 2 = 0,8.
- Capacidade adicional: `min(1,5; 0,8) × 1.500 = 0,8 × 1.500 = 1.200`.
- Capacidade total: 500 + 1.200 = 1.700 por recurso.

**Exemplo 3: Armazém N3 eficiência máxima**
- Armazém N3 com 4 Carregadores, cada um eficiência 1,0 ou mais.
- Eficiência média ≥ 1,0, limitada a 1,5.
- Capacidade adicional: `1,5 × 4.000 = 6.000`.
- Capacidade total: 500 + 6.000 = 6.500 por recurso.

**Exemplo 4: Excedente perdido (passo 4 do turno)**
- Capacidade: 1.000 Madeira. Vila tem 900; produção gera +200.
- Resultado: 1.000 (máximo), 100 Madeira perdida (registrada no evento do turno).

## Interações com outros domínios

- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — armazenamento, capacidade, perda de excedente
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — profissão Carregador
- [construcoes.md](construcoes.md) — custos, PO

## Modelo de dados

Armazém é um tipo de `construcao`; campo `alocacoes` (relação com Carregadores) calcula eficiência média ao computar capacidade.

## Questões em aberto

- Armazém desativado (sem mínimo): conta como "EM_OBRA" ou "ATIVA com 0 efeito"?
- Limite de Armazéns por vila: há limite?
