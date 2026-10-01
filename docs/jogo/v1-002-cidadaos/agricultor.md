# Agricultor

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Agricultor trabalha em Fazendas de plantio, produzindo Grãos ou Fibra conforme a cultura escolhida pelo jogador.

## Características ligadas

- **Inteligência (INT)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Agricultor.

## Onde trabalha

- Fazenda de plantio: cultiva Grãos ou Fibra (seção 4.5).

## Ferramenta

- **Enxada** (seção 7.9): oficina Ferraria; receita 1 Ferro + 1 Tábua; efeito +L PE de Agricultor.

## Como evolui

- Base: distribuição na criação.
- Até 18 anos: +1 profissão a cada 2 anos (máx. 9 pontos ao atingir 18).
- Após 18: +1 PE a cada 24 turnos trabalhando em Fazenda de plantio.
- XP de Guerreiro: não se aplica.

## Produção

**Base por trabalhador/turno** (seção 4.5):
- Grãos: **6 por turno** (com eficiência 1,0, N1).
- Fibra: **4 por turno** (com eficiência 1,0, N1).

A troca de cultura leva 1 turno sem produção.

## Exemplo de PE efetivo e eficiência

**Cenário**: Agricultor com INT 14, PE base 4, com Enxada L1, em Fazenda N1.
- Bônus INT: floor(14 ÷ 5) = 2.
- Bônus ferramenta: +1 (Enxada L1).
- PE efetivo: 4 + 2 + 1 = 7.
- Eficiência: 0,5 + 0,1 × 7 = 1,2.
- Produção de Grãos: 6 × 1,2 × 1,0 (N1) = 7,2 Grãos/turno.

**Em Fazenda N2**:
- Multiplicador nível ×1,2 (seção 4.3).
- Produção: 6 × 1,2 × 1,2 = 8,64 Grãos/turno.

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — Fazenda de plantio
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — produção de Grãos/Fibra
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — usa Enxada
