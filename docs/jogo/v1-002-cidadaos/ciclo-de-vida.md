# Ciclo de vida

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Cada cidadão nasce, cresce ao longo de 18 anos, trabalha, envelhece a partir de 50 anos com risco de morte, e pode morrer por idade, fome ou batalha.

## Regras

**R1 — Idade em meses [proposta]** (seção 5.5): Idade guardada em meses; +1 mês por turno; aniversário a cada 12 turnos.

**R2 — Faixas etárias**: 
- 0–13 meses = recém-nascido/criança (não trabalha)
- 14–17 anos = adolescente (trabalha com ×0,5 de eficiência)
- 18–64 anos = adulto (trabalha com eficiência normal)
- 65+ anos = idoso (não trabalha)

**R3 — Envelhecimento [proposta]** (seção 5.9):
- +1 ponto de **característica** por ano de idade até 18 (aniversários 1..18 → 18 pontos).
- +1 ponto de **profissão** a cada 2 anos de idade até 18 (aniversários 2, 4, ..., 18 → 9 pontos).
- Os pontos ficam "pendentes" e o jogador os distribui no painel da pessoa (sem limite por atributo após a criação). Pontos não distribuídos acumulam.
- Após 18 anos: só ganha PE por experiência — a cada 24 turnos trabalhando em prédio da mesma profissão, +1 PE base nela; Guerreiro ganha por XP (seção 9.4).

**R4 — Teste de morte anual [proposta]** (seção 5.5):
- A cada aniversário a partir de 50 anos: `chance = max(0; 1% × (idade − 49) − 0,2% × VIT)`.
- Aos 90 anos morre com certeza.

**R5 — Morte por fome [proposta]** (seção 3.4):
- A partir do 3º turno seguido de fome, pessoa perde 1 de Vitalidade por turno.
- Vitalidade 0 com fome = morte.

**R6 — Morte em batalha [proposta]** (seção 10.6):
- Membro abatido (0 PV): na vitória, 80% ferido / 20% morto; na derrota, 50% / 50%.
- Ferido fica 6 turnos sem trabalhar nem lutar.

**R7 — Itens equipados de mortos [proposta]** (seção 5.5): voltam ao inventário da vila.

**R8 — Bem alimentada [proposta]** (seção 3.4):
- Se ≥50% do consumo do turno veio de Refeições, a vila tem +10% de eficiência no turno seguinte.

## Números e tabelas

### Fases da vida

| Fase | Idade (anos) | Idade (meses) | Trabalha | Eficiência | Características | Profissões |
|---|---|---|---|---|---|---|
| Infância | 0–13 | 0–155 | Não | — | +1/ano até 13 | +1 a cada 2 anos até 13 |
| Adolescência | 14–17 | 168–203 | Sim, ×0,5 | base ×0,5 | +1/ano até 18 | +1 a cada 2 anos até 18 |
| Idade adulta | 18–49 | 216–587 | Sim | Normal | — | Por experiência |
| 3ª idade | 50–89 | 600–1.067 | Sim até 64 | Teste de morte | — | Por experiência |
| Morte certa | 90+ | 1.080+ | Não | — | — | — |

### Taxa de morte por idade (com exemplos de VIT)

| Idade | VIT = 3 | VIT = 5 | VIT = 8 | VIT = 10 |
|---|---|---|---|---|
| 50 | 1% − 0,6% = 0,4% | 1% − 1% = 0% | 1% − 1,6% = 0% | 1% − 2% = 0% |
| 60 | 11% − 0,6% = 10,4% | 11% − 1% = 10% | 11% − 1,6% = 9,4% | 11% − 2% = 9% |
| 70 | 21% − 0,6% = 20,4% | 21% − 1% = 20% | 21% − 1,6% = 19,4% | 21% − 2% = 19% |
| 80 | 31% − 0,6% = 30,4% | 31% − 1% = 30% | 31% − 1,6% = 29,4% | 31% − 2% = 29% |
| 90 | 100% | 100% | 100% | 100% |

## Exemplos

**Exemplo 1 — Crescimento até 18 anos**

Cidadão nasce com idade 0, características base todas 0, PE base em todas as profissões 0.

- Aniversário 1 (turno 12): +1 de cada característica; nenhum PE (não é ano par).
- Aniversário 2 (turno 24): +1 de cada característica; +1 de cada profissão.
- Aniversário 6 (turno 72): +1 de cada característica; +1 de cada profissão.
- ...
- Aniversário 18 (turno 216): +1 de cada característica; +1 de cada profissão.
- **Total aos 18 anos**: 18 pontos de característica, 9 de profissão, ambos como pendentes.

O jogador os distribui ao longo dos 18 anos (ou após) sem limite por atributo após a criação.

**Exemplo 2 — Morte por idade**

Cidadão com 60 anos (idade 720 meses), VIT 5:
- Chance de morte = max(0; 1% × (60 − 49) − 0,2% × 5) = max(0; 11% − 1%) = 10%.
- Se passar (90% de chance), envelhece para 61 anos.
- Aos 61 anos: chance = 12% − 1% = 11%.

**Exemplo 3 — Morte por fome prolongada**

- Turno 1: sem comida, faminto_turnos = 1.
- Turno 2: sem comida, faminto_turnos = 2.
- Turno 3: sem comida, faminto_turnos = 3, perde 1 VIT. Se VIT chega a 0, morre.

**Exemplo 4 — Adolescente vs. adulto**

- Agricultor com INT 10 (bônus +2) e PE base 4, idade 16.
  - PE efetivo: 4 + 2 = 6.
  - Eficiência base: 0,5 + 0,1 × 6 = 1,1.
  - Eficiência real (×0,5 adolescente): 1,1 × 0,5 = 0,55.

- Mesmo agricultor aos 18 anos:
  - PE efetivo: 4 + 2 = 6 (sem mudança).
  - Eficiência: 0,5 + 0,1 × 6 = 1,1 (sem multiplicador).

## Interações com outros domínios

- [cidadao.md](cidadao.md) — características e profissões
- [familias.md](familias.md) — família afetada por morte
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — alimentação afeta saúde
- [batalha.md](../v1-014-batalha/batalha.md) — morte em combate

## Questões em aberto

- [proposta] Taxa de morte: 1% × (idade − 49) desejável? Considerar ajuste para aceleração após 70.
- [proposta] Bem alimentada (+10%): vale a pena produzir Refeições vs. outros alimentos?
