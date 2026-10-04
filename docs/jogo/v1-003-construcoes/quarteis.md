# Quartéis

**Épico:** [construcoes.md](construcoes.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Quartéis são instalações militares onde Guerreiros treinam, formam tropas e preparam expedições. Cada nível de quartel oferece mais posições de instrutor e capacidade de tropas. Instrutores (Guerreiros) treinam guerreiros aquartelados, aumentando sua experiência.

## Regras

- **R1**: Quartel requer profissão Guerreiro (instrutor) (seção 4.12) [req + proposta].
- **R2**: Vagas de instrutor: N1 = 1, N2 = 2, N3 = 3 (seção 4.12) [proposta].
- **R3**: Sem instrutor, quartel não forma tropas nem treina (seção 4.12) [proposta].
- **R4**: Capacidade de tropas: N1 = 1 tropa até 5 membros; N2 = 2 tropas até 8; N3 = 4 tropas até 10 (seção 4.12) [proposta].
- **R5**: Vários quartéis somam capacidades (seção 4.12) [proposta].
- **R6**: Treino: cada guerreiro de tropa aquartelada (não em expedição) ganha XP de Guerreiro por turno, multiplicado pelo fator de bônus Militar da vila (fator = 1 + bônus ÷ 100): N1 = 0,5, N2 = 1,0, N3 = 1,5, arredondado em 2 casas decimais (seção 4.12) [proposta].
- **R7**: Quartel ocupa 1x1 (N1), 2x2 (N2), 3x3 (N3) em Região Urbana (seção 1.4) [req].

## Números e tabelas

### Capacidade por nível [proposta]

| Nível | Vagas instrutor | Tropas | Membros por tropa | XP treino/guerreiro/turno |
|---|---|---|---|---|
| N1 | 1 | 1 | até 5 | 0,5 |
| N2 | 2 | 2 | até 8 | 1,0 |
| N3 | 3 | 4 | até 10 | 1,5 |

### Custos por nível [proposta]

| Nível | Tamanho | Tábua | Pedra | Ferro | PO |
|---|---|---|---|---|---|
| N1 | 1x1 | 30 | 40 | 10 | 8 |
| N2 | 2x2 | 75 (2,5×) | 100 (2,5×) | 25 (2,5×) | 20 |
| N3 | 3x3 | 150 (5×) | 200 (5×) | 50 (5×) | 40 |

## Exemplos

**Exemplo 1: Quartel N1 com 1 instrutor**
- Quartel N1 com 1 Guerreiro instrutor.
- Capacidade: 1 tropa de até 5 membros.
- Cada membro treina 0,5 XP/turno (quando no quartel, não em expedição).

**Exemplo 2: Soma de capacidades**
- Vila tem Quartel N1 (1 tropa/5 membros) + Quartel N2 (2 tropas/8 membros).
- Capacidade total: 3 tropas, 21 membros distribuídos (ex.: 5 + 8 + 8).

**Exemplo 3: Guerreiro sem arma ou PE insuficiente**
- Jogador tenta colocar Guerreiro sem arma no quartel.
- Rejeitado: requisito não atendido (seção 9.1).

**Exemplo 4: Treino e progressão de XP**
- Quartel N2 com 1 instrutor, 1 tropa de 3 Guerreiros aquartelados.
- Cada um ganha 1,0 XP/turno → 3 XP/turno total.
- 10 XP = +1 PE Guerreiro base (seção 9.4).

**Exemplo 5: Bônus Militar no treino**
- Quartel N1 com 1 guerreiro aquartelado
- XP base do Quartel N1: 0,5 por turno
- Vila tem Militar 20 → fator = 1 + 20 ÷ 100 = 1,20
- XP no turno: 0,5 × 1,20 = **0,60 XP**

## Interações com outros domínios

- [tropas.md](../v1-013-quartel-e-tropas/tropas.md) — formação, estados, expedições
- [expedicoes.md](../v1-013-quartel-e-tropas/expedicoes.md) — viagem de tropas
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — profissão Guerreiro, PE
- [batalha.md](../v1-014-batalha/batalha.md) — combate
- [construcoes.md](construcoes.md) — custos, PO

## Modelo de dados

Quartel é um tipo de `construcao`. Relação com tropas em tabela `tropa`: tropa_id, quartel_id, estado, masmorra_id.

## Questões em aberto

- Limite de quartéis por vila: há limite?
- Instrutor trabalha em outra profissão simultaneamente? (suponho não, é ocupação dedicada)
