# H-003 — Nascimento e crescimento

**Épico:** [../cidadao.md](../cidadao.md) · **Domínio:** [../ciclo-de-vida.md](../ciclo-de-vida.md), [../familias.md](../familias.md)

## História

Como jogador, quero ver minha população crescer naturalmente através de concepção, gestação e nascimento. Filhos herdam características e PE dos pais.

## Contexto

**Concepcão** (seção 5.8):
- Por turno, chance de 8% para casal com mulher 18–45 anos, ambos não famintos, com vaga livre.
- Gestação: 9 turnos.
- Intervalo mínimo: 12 turnos entre nascimento e próxima concepção.
- Sem gêmeos. Sexo 50/50.

**Herança** (seção 5.8):
- Filho nasce com 25% das características e PE dos pais.
- Fórmula: para cada atributo, `floor(0,25 × média dos dois pais)`.

**Crescimento** (seção 5.9):
- +1 característica/ano até 18.
- +1 profissão a cada 2 anos até 18.
- Pontos ficam pendentes.

**Envelhecimento e morte** (seção 5.5):
- +1 mês por turno; aniversário a cada 12 turnos.
- A partir de 50 anos, teste de morte: `chance = max(0; 1% × (idade − 49) − 0,2% × VIT)`.
- Aos 90 anos morre com certeza.

**Imigração** (seção 4.10):
- Estalagem N1/N2/N3: 2%/4%/6% de imigrante (18–30 anos) por turno, se núcleo livre.

## Critérios de aceite

### CA1 — Concepção com validações

- **Dado** casal elegível (mulher 18–45, ambos não famintos, vaga livre) no turno T.
- **Quando** o turno é processado.
- **Então**:
  - Chance de 8% de concepção registrada.
  - Vaga na casa fica reservada (para o futuro filho).
  - Gestação_turnos = 9.

### CA2 — Gestação e nascimento

- **Dado** casal com gestação em curso (gestacao_turnos = 9 → 8 → ... → 1).
- **Quando** gestacao_turnos chega a 0 (9 turnos depois).
- **Então**:
  - Novo cidadão criado na família do casal.
  - Idade = 0 meses.
  - Sexo: 50/50 aleatório.
  - Características herdadas: `floor(0,25 × (PAI + MAE) ÷ 2)` para cada uma.
  - PE profissão herdados: `floor(0,25 × (PAI + MAE) ÷ 2)` para cada profissão com PE > 0 nos pais.
  - Família = mesma do casal.

### CA3 — Exemplo numérico de herança

- **Dado** pai FOR 12, VIT 10; mãe FOR 8, VIT 12.
- **Quando** filho nasce.
- **Então**:
  - Filho FOR = floor(0,25 × (12 + 8) ÷ 2) = floor(0,25 × 10) = 2.
  - Filho VIT = floor(0,25 × (10 + 12) ÷ 2) = floor(0,25 × 11) = 2.

### CA4 — Intervalo mínimo de 12 turnos

- **Dado** casal que concebeu no turno T.
- **Quando** bebê nasce no turno T+9.
- **Então** nova concepção não ocorre antes do turno T+9+12 = T+21.

### CA5 — Envelhecimento anual

- **Dado** cidadão com idade 5 anos (60 meses).
- **Quando** turno 1 de um ano novo passa (aniversário no turno 12k).
- **Então**:
  - Idade += 1 ano (72 meses).
  - +1 ponto de característica pendente (até ano 18).
  - +1 ponto de profissão pendente a cada 2 anos (anos 2, 4, ..., 18).

### CA6 — Morte por idade

- **Dado** cidadão com 60 anos e VIT 5.
- **Quando** turno processa aniversário.
- **Então**:
  - Chance de morte = 1% × (60 − 49) − 0,2% × 5 = 10%.
  - Se aleatório < 10%, cidadão morre.
  - Itens equipados voltam ao inventário.

### CA7 — Imigração pela Estalagem

- **Dado** Estalagem N2 (4% chance) ativa com núcleo livre.
- **Quando** turno processa.
- **Então**:
  - 4% de chance de imigrante (18–30 anos) chegar.
  - Novo cidadão: adulto, 20 características, 10 profissão (aleatório).
  - Novo núcleo na casa (solteiro).
  - Sobrenome gerado ou aleatório.

## Tarefas

- [h-003-tarefa-001 — Reprodução e nascimento no turno](h-003-tarefa-001-reproducao-e-nascimento-no-turno.md)
- [h-003-tarefa-002 — Envelhecimento, crescimento e morte](h-003-tarefa-002-envelhecimento-crescimento-e-morte.md)
- [h-003-tarefa-003 — Imigração pela Estalagem](h-003-tarefa-003-imigracao-pela-estalagem.md)

## Fora de escopo

- Esterilidade ou infertilidade.
- Adoção.
- Maldições que aumentam taxa de morte.
