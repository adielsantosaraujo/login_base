# Comerciante

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Comerciante trabalha em Mercados e Estalagens, negociando recursos com mercadores NPC e gerando renda passiva.

## Características ligadas

- **Carisma (CAR)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Comerciante.

## Onde trabalha

- Mercado: comércio de recursos (seção 4.9).
- Estalagem: serviço de viajantes (seção 4.10).

## Ferramenta

- **Balança** (seção 7.9): oficina Carpintaria; receita 2 Tábua + 1 Ferro; efeito +L PE de Comerciante.

## Como evolui

- Base: distribuição na criação.
- Até 18 anos: +1 profissão a cada 2 anos (máx. 9 pontos ao atingir 18).
- Após 18: +1 PE a cada 24 turnos trabalhando em Mercado ou Estalagem.
- XP de Guerreiro: não se aplica.

## Operações comerciais

**Mercado** (seção 4.9):
- Vende e compra recursos com um mercador NPC (comércio entre jogadores fica fora da v1).
- Volume máximo negociado por turno = `20 × Σ eficiência dos Comerciantes × mult. nível` unidades.
- Preço de venda = preço base × `min(1,0; 0,5 + 0,02 × PE Comerciante do melhor comerciante)`.
- Preço de compra = preço base × `max(1,0; 1,5 − 0,02 × PE Comerciante do melhor comerciante)`.
- Ordens executadas imediatamente, consumindo o volume do turno corrente.

**Estalagem** (seção 4.10):
- Vagas: Cozinheiro ou Comerciante (2/5/10 por nível).
- Serve Refeições: por turno consome até `5 × Σ eficiência × mult. nível` Refeições e gera **4 Ouro por Refeição** servida.

## Exemplo de PE efetivo e eficiência

**Cenário**: Comerciante com CAR 20, PE base 5, com Balança L2, em Mercado N2.
- Bônus CAR: floor(20 ÷ 5) = 4.
- Bônus ferramenta: +2 (Balança L2).
- PE efetivo: 5 + 4 + 2 = 11.
- Eficiência: 0,5 + 0,1 × 11 = 1,6.
- Multiplicador nível N2: ×1,2 (seção 4.3).

**Negociação em Mercado N2**:
- Volume disponível por turno: 20 × 1,6 × 1,2 = 38,4 unidades de qualquer recurso.
- Preço de venda: preço base × min(1,0; 0,5 + 0,02 × 11) = preço base × min(1,0; 0,72) = preço base × 0,72.
  - Ex.: Madeira (preço base 1) vende por 0,72 Ouro.
- Preço de compra: preço base × max(1,0; 1,5 − 0,02 × 11) = preço base × max(1,0; 1,28) = preço base × 1,28.
  - Ex.: Madeira compra por 1,28 Ouro.

**Serviço na Estalagem N2**:
- Capacidade de consumo: `5 × 1,6 × 1,2 = 9,6` Refeições/turno.
- Ouro gerado: 9,6 × 4 = 38,4 Ouro/turno.

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — Mercado, Estalagem
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — negociação de preços
- [cozinheiro.md](cozinheiro.md) — pode trabalhar na Estalagem (ocupam vagas juntos)
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — usa Balança

## Questões em aberto

- [proposta] Mercado com NPC apenas: adicionar jogador-a-jogador no futuro?
- [proposta] Limite de volume por turno: suficiente para dinâmica de comércio esperada?
