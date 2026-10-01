# Ferreiro

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Ferreiro trabalha em Fundições e Ferrarias, produzindo minério processado (Ferro, Aço) e fabricando armas e ferramentas.

## Características ligadas

- **Inteligência (INT)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Ferreiro.

## Onde trabalha

- Fundição: processamento de minério em Ferro (e Aço a partir de N2).
- Ferraria (oficina): fabricação de armas e ferramentas metálicas.

## Ferramenta

- **Malho** (seção 7.9): oficina Ferraria; receita 2 Ferro; efeito +L PE de Ferreiro.

## Como evolui

- Base: distribuição na criação.
- Até 18 anos: +1 profissão a cada 2 anos (máx. 9 pontos ao atingir 18).
- Após 18: +1 PE a cada 24 turnos trabalhando em Fundição ou Ferraria.
- XP de Guerreiro: não se aplica.

## Produção/Fabricação

**Fundição** (seção 4.5):
- Receita Ferro: **2 Minério de ferro + 1 Carvão → 1 Ferro**.
  - Ciclos: **2** por trabalhador/turno.
- Receita Aço (N2+): **2 Ferro + 1 Carvão + 1 Enxofre → 1 Aço**.
  - Ciclos: **1** por trabalhador/turno.
  - Jogador escolhe Ferro ou Aço por trabalhador.

**Ferraria (oficina)** (seção 4.11):
- Nível máximo de item fabricável: N1 até L3; N2 até L6; N3 até L10.
- Vagas de artesão: 2/5/10.
- Fabrica armas (Espada, Lança, Besta), ferramentas (Martelo, Enxada, Forcado, Picareta, Machado, Malho, Cutelo, Faca de caça, Balança), armaduras (Peitoral, Capacete, Ombreiras) e joias (Colar, Anel).

## Exemplo de PE efetivo e eficiência

**Cenário**: Ferreiro com INT 20, PE base 7, com Malho L4, em Fundição N2.
- Bônus INT: floor(20 ÷ 5) = 4.
- Bônus ferramenta: +4 (Malho L4).
- PE efetivo: 7 + 4 + 4 = 15.
- Eficiência: 0,5 + 0,1 × 15 = 2,0.
- Multiplicador nível N2: ×1,2 (seção 4.3).

**Produção Ferro**:
- Ciclos: 2 × 2,0 × 1,2 = 4,8 ciclos/turno.
- Insumo: 9,6 Minério de ferro + 4,8 Carvão.
- Produção: 4,8 Ferro/turno.

**Fabricação em Ferraria N3**:
- Cria até L10 (nível máximo).
- Com eficiência 2,0, PE efetivo suficiente.
- Engaste de pedras: apenas em Ferraria.

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — Fundição, Ferraria
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — produção de Ferro, Aço
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — fabrica ferramentas; usa Malho
- [armas.md](../v1-004-armas/armas.md) — fabrica armas
- [armaduras.md](../v1-006-Armaduras/armaduras.md) — fabrica peças de armadura
- [joias.md](../v1-007-Joias/joias.md) — fabrica joias; engasta pedras
