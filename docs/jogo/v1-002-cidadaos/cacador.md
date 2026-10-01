# Caçador

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Caçador trabalha em Cabanas de caça, coletando carne e couro dos animais selvagens.

## Características ligadas

- **Vitalidade (VIT)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Caçador.
- **Velocidade (VEL)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Caçador.
- **Carisma (CAR)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Caçador.

## Onde trabalha

- Cabana de caça: coleta Carne e Couro de animais selvagens (seção 4.5).

## Ferramenta

- **Faca de caça** (seção 7.9): oficina Ferraria; receita 1 Ferro + 1 Couro curtido; efeito +L PE de Caçador.

## Como evolui

- Base: distribuição na criação.
- Até 18 anos: +1 profissão a cada 2 anos (máx. 9 pontos ao atingir 18).
- Após 18: +1 PE a cada 24 turnos trabalhando em Cabana de caça.
- XP de Guerreiro: não se aplica.

## Produção por coleta

**Cabana de caça** (seção 4.5):
- Produção base: **2 Carne + 1 Couro** por trabalhador/turno.

## Exemplo de PE efetivo e eficiência

**Cenário**: Caçador com VIT 10, VEL 12, CAR 10, PE base 3, com Faca de caça L2, em Cabana N2.
- Bônus VIT: floor(10 ÷ 5) = 2.
- Bônus VEL: floor(12 ÷ 5) = 2.
- Bônus CAR: floor(10 ÷ 5) = 2.
- Bônus ferramenta: +2 (Faca L2).
- PE efetivo: 3 + 2 + 2 + 2 + 2 = 11.
- Eficiência: 0,5 + 0,1 × 11 = 1,6.
- Multiplicador nível N2: ×1,2 (seção 4.3).

**Produção**:
- (2 Carne + 1 Couro) × 1,6 × 1,2 = 3,84 Carne + 1,92 Couro/turno.

## Diferença entre Caçador e Fazendeiro

| Aspecto | Caçador | Fazendeiro |
|---|---|---|
| Profissão | Caçador | Fazendeiro |
| Prédio | Cabana de caça | Fazenda de criação |
| Produto | Carne selvagem + Couro | Carne criada (Gado/Ovelhas) + Couro/Lã |
| Característica | VIT, VEL, CAR | INT |
| Ferramento | Faca de caça | Forcado |

Caçador fornece Couro "selvagem" enquanto Fazendeiro fornece produtos de criação controlada.

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — Cabana de caça
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — produção de Carne, Couro
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — usa Faca de caça
