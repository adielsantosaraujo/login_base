# Fazendeiro

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Fazendeiro trabalha em Fazendas de criação, produzindo carnes, couros e lã conforme o rebanho escolhido.

## Características ligadas

- **Inteligência (INT)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Fazendeiro.

## Onde trabalha

- Fazenda de criação: cria Gado ou Ovelhas (seção 4.5).

## Ferramenta

- **Forcado** (seção 7.9): oficina Ferraria; receita 1 Ferro + 1 Tábua; efeito +L PE de Fazendeiro.

## Como evolui

- Base: distribuição na criação.
- Até 18 anos: +1 profissão a cada 2 anos (máx. 9 pontos ao atingir 18).
- Após 18: +1 PE a cada 24 turnos trabalhando em Fazenda de criação.
- XP de Guerreiro: não se aplica.

## Produção

**Base por trabalhador/turno** (seção 4.5):
- Gado: **3 Carne + 1 Couro**.
- Ovelhas: **2 Lã + 1 Carne**.

O rebanho é escolhido pelo jogador e não troca automaticamente.

## Exemplo de PE efetivo e eficiência

**Cenário**: Fazendeiro com INT 12, PE base 3, com Forcado L2, em Fazenda N2.
- Bônus INT: floor(12 ÷ 5) = 2.
- Bônus ferramenta: +2 (Forcado L2).
- PE efetivo: 3 + 2 + 2 = 7.
- Eficiência: 0,5 + 0,1 × 7 = 1,2.
- Multiplicador nível N2: ×1,2 (seção 4.3).

**Produção de Gado**:
- (3 Carne + 1 Couro) × 1,2 × 1,2 = 4,32 Carne + 1,44 Couro/turno.

**Produção de Ovelhas**:
- (2 Lã + 1 Carne) × 1,2 × 1,2 = 2,88 Lã + 1,44 Carne/turno.

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — Fazenda de criação
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — produção de Carne/Couro/Lã
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — usa Forcado
