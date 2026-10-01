# Mineiro

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Mineiro trabalha em Pedreiras, Barreiros, Minas (Ferro, Carvão, Enxofre) e Salinas, coletando matérias-primas.

## Características ligadas

- **Força (FOR)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Mineiro.

## Onde trabalha

- Pedreira: Pedra.
- Barreiro: Argila.
- Mina de ferro: Minério de ferro.
- Mina de carvão: Carvão.
- Salina: Sal.
- Mina de enxofre: Enxofre.

## Ferramenta

- **Picareta** (seção 7.9): oficina Ferraria; receita 2 Ferro + 1 Tábua; efeito +L PE de Mineiro.

## Como evolui

- Base: distribuição na criação.
- Até 18 anos: +1 profissão a cada 2 anos (máx. 9 pontos ao atingir 18).
- Após 18: +1 PE a cada 24 turnos trabalhando em prédio de coleta de Mineiro.
- XP de Guerreiro: não se aplica.

## Produção por coleta

**Base por trabalhador/turno** (seção 4.5):

| Prédio | Produção base |
|---|---|
| Pedreira | 4 Pedra |
| Barreiro | 4 Argila |
| Mina de ferro | 3 Minério de ferro |
| Mina de carvão | 3 Carvão |
| Salina | 3 Sal |
| Mina de enxofre | 2 Enxofre |

## Exemplo de PE efetivo e eficiência

**Cenário**: Mineiro com FOR 16, PE base 5, com Picareta L3, em Mina de ferro N2.
- Bônus FOR: floor(16 ÷ 5) = 3.
- Bônus ferramenta: +3 (Picareta L3).
- PE efetivo: 5 + 3 + 3 = 11.
- Eficiência: 0,5 + 0,1 × 11 = 1,6.
- Multiplicador nível N2: ×1,2 (seção 4.3).
- Produção: 3 × 1,6 × 1,2 = 5,76 Minério de ferro/turno.

## Marcação de ladrilhos

Os prédios de coleta precisam de ladrilhos marcados compatíveis com a jazida (seção 4.6):
- Pedreira → Rocha.
- Barreiro → Barreiro.
- Mina de ferro → Veio de ferro.
- Mina de carvão → Veio de carvão.
- Salina → Salina.
- Mina de enxofre → Enxofre.

Máximo de ladrilhos marcados por nível: N1 4, N2 10, N3 20 (seção 4.6).

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — prédios de coleta
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — produção de minérios
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — usa Picareta
