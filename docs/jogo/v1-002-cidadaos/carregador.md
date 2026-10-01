# Carregador

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Carregador trabalha no Armazém para aumentar capacidade de armazenamento e também apoia obras de construção.

## Características ligadas

- **Força (FOR)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Carregador.
- **Velocidade (VEL)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Carregador.

## Onde trabalha

- Armazém: aumenta capacidade de armazenamento (seção 3.3).
- Apoio a obras: contribui com `eficiência × 0,5` PO por turno (seção 4.2).

## Ferramenta

- **Carrinho de mão** (seção 7.9): oficina Carpintaria; receita 3 Tábua + 1 Ferro; efeito +L PE de Carregador.

## Como evolui

- Base: distribuição na criação.
- Até 18 anos: +1 profissão a cada 2 anos (máx. 9 pontos ao atingir 18).
- Após 18: +1 PE a cada 24 turnos trabalhando em prédio de Carregador.
- XP de Guerreiro: não se aplica.

## Exemplo de PE efetivo e eficiência

**Cenário**: Carregador com FOR 15, VEL 12, PE base 5, com Carrinho de mão L3.
- Bônus FOR: floor(15 ÷ 5) = 3.
- Bônus VEL: floor(12 ÷ 5) = 2.
- Bônus ferramenta: +3 (Carrinho L3).
- PE efetivo: 5 + 3 + 2 + 3 = 13.
- Eficiência: 0,5 + 0,1 × 13 = 1,8.

**Alocação em Armazém N2**:
- Carregadores mínimos: 2 (seção 3.3).
- Com 2 Carregadores eficiência 1,8: capacidade extra = 1.500 × min(1,5; 1,8) = 1.500 × 1,5 = +2.250 por recurso.

**Apoio a obra**:
- Contribui com `1,8 × 0,5 = 0,9` PO por turno (seção 4.2).

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — Armazém e apoio a obras
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — aumenta capacidade de armazenamento
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — usa Carrinho de mão
