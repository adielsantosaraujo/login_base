# Alimentação

**Épico:** [recursos.md](recursos.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

A alimentação é crítica para a população. Pessoas consomem alimentos em ordem de preferência, e a falta gera fome que reduz eficiência e pode levar à morte. Uma vila bem alimentada com Refeições ganha bônus de eficiência.

## Regras

- R1: Consumo por turno: 1 alimento por pessoa ≥14 anos; 0,5 para menores de 14 (seção 3.4 da bíblia).
- R2: Alimentos válidos: Refeição, Grãos, Carne (1 unidade = 1 alimento).
- R3: Ordem de consumo: Refeição → Grãos → Carne.
- R4: **Bem alimentada**: se ≥50% do consumo do turno veio de Refeições, a vila tem +10% de eficiência no turno seguinte.
- R5: **Fome**: se faltar alimento, pessoas sem comida ficam "famintas": eficiência ×0,5 e não concebem.
- R6: A partir do 3º turno seguido de fome, pessoas perdem 1 de Vitalidade por turno.
- R7: Vitalidade 0 com fome = morte.
- R8: O consumo é resolvido no passo 3 da ordem de turno (seção 2.2 da bíblia).

## Números e tabelas

**Consumo de alimentos**

| Faixa etária | Consumo por turno |
|---|---|
| 0–13 anos | 0,5 alimento |
| 14+ anos | 1,0 alimento |

**Ordem de consumo**
1. Refeição (processada, mais nutritiva)
2. Grãos (bruto, básico)
3. Carne (bruto, backup)

**Bônus de eficiência**
- Se ≥50% de Refeições: +10% de eficiência em todas as profissões no turno seguinte
- Se <50%: sem bônus (mas sem penalidade se houver comida suficiente)

## Exemplos

**Exemplo 1: Vila bem alimentada**
- População: 10 adultos (10 alimentos) + 4 menores (2 alimentos) = 12 alimentos por turno
- Estoque: 8 Refeições + 6 Grãos
- Consumo: 8 Refeições (100% coberto) → **Bem alimentada** (8 de 12 ≈ 67% de Refeições)
- Sobra: 6 Grãos

**Exemplo 2: Fome parcial**
- População: 8 adultos + 2 menores = 9 alimentos
- Estoque: 3 Refeições + 2 Grãos + 2 Carne
- Consumo: 3 Refeições + 2 Grãos + 2 Carne = 7 alimentos (2 pessoas ficam famintas)
- 2 pessoas com eficiência ×0,5; não concebem
- Contador de fome iniciado para elas

**Exemplo 3: Morte por fome prolongada**
- Pessoa fica faminta; turno 1, 2: eficiência ×0,5
- Turno 3: perde 1 VIT (de 5 → 4)
- Turno 4: perde 1 VIT (4 → 3)
- Turno 5: perde 1 VIT (3 → 2)
- Se alcançar VIT 0 enquanto faminta: morte

## Interações com outros domínios

- [recursos.md](recursos.md) — estoque de Refeição, Grãos, Carne
- [../../v1-002-cidadaos/cidadao.md](../v1-002-cidadaos/cidadao.md) — população e Vitalidade
- [../../v1-003-construcoes/cozinhas.md](../v1-003-construcoes/construcoes.md) — produção de Refeição

## Modelo de dados (resumo)

**Tabela cidadao — campos de alimentação**
- faminto_turnos: int (contador de turnos seguidos em fome; reseta quando come bem)

**Tabela vila — flags de alimentação**
- bem_alimentada: boolean (ativa bônus de +10% de eficiência no próximo turno)

## Questões em aberto

- Pessoas que nascem no turno de fome herdam o estado faminto?
- Bônus de +10% de eficiência se aplica a qual passo do turno?
