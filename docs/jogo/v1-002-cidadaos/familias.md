# Famílias

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Cidadãos vivem em famílias (núcleos) dentro de casas. Uma família tem um sobrenome e pode ter múltiplos casal/adulto em suas gerações. Casal novo exige núcleo livre; reprodução gera filhos com herança de características e PE.

## Regras

**R1 — Núcleo familiar [proposta]** (seção 0): Casal (ou adulto solteiro chefe) + filhos solteiros que moram juntos em uma casa.

**R2 — Casamento [req + proposta]** (seção 5.7):
- Exige núcleo livre em uma casa.
- Ambos ≥18 anos, solteiros, vivos, da mesma vila, não parentes de 1º grau nem irmãos.
- Casal formado por homem e mulher pode reproduzir.
- Casal se muda para a casa escolhida e forma novo núcleo.
- Sobrenome do novo núcleo escolhido pelo jogador entre os dois.
- Viúvos podem casar novamente.

**R3 — Reprodução [proposta]** (seção 5.8):
- Exige vaga livre na casa do casal.
- Concepção: por turno, chance de **8%** para casal com mulher de 18–45 anos, ambos não famintos, com vaga livre; a vaga fica reservada.
- Gestação: **9 turnos**.
- Intervalo mínimo: 12 turnos entre um nascimento e a próxima concepção do casal.
- Sem gêmeos. Sexo 50/50.

**R4 — Herança [req + proposta]** (seção 5.8):
- Filho nasce com 25% das características e PE dos pais.
- Fórmula: para cada característica e cada profissão, `floor(0,25 × média dos dois pais)` (valores base, sem itens).

**R5 — Família líder [req + proposta]** (seção 5.6):
- Líder = o adulto mais velho da família líder.
- Bônus: +1% de eficiência em toda a vila a cada 2 pontos de CAR do líder, máx. +10%.

**R6 — Sucessão [proposta]** (seção 5.6):
- Morto o líder, assume o adulto mais velho da família líder (cônjuges inclusive).
- Se não houver adulto, o jogador escolhe nova família líder.

**R7 — Imigração [proposta]** (seção 4.10):
- A cada turno, chance de `2% × nível` (N1 2%, N2 4%, N3 6%) de chegar um viajante adulto (18–30 anos, 20 pontos de característica e 10 de profissão distribuídos aleatoriamente), se existir núcleo livre em alguma casa.
- Viajante vira um núcleo próprio (solteiro).

## Números e tabelas

### Casas — capacidade de núcleos e vagas (seção 4.7)

| Nível | Núcleos familiares | Vagas (pessoas) |
|---|---|---|
| N1 | 1 | 4 |
| N2 | 2 | 10 |
| N3 | 4 | 24 |

**Nota**: As 4 casas N1 iniciais já vêm construídas na 1ª região Urbana escolhida, nos ladrilhos (0,0), (2,0), (4,0), (6,0) (seção 4.7).

### Imigração pela Estalagem (seção 4.10)

| Nível da Estalagem | Chance por turno | Idade do imigrante | Pontos |
|---|---|---|---|
| N1 | 2% | 18–30 anos | 20 característica, 10 profissão |
| N2 | 4% | 18–30 anos | 20 característica, 10 profissão |
| N3 | 6% | 18–30 anos | 20 característica, 10 profissão |

## Exemplos

**Exemplo 1 — Herança de características**
- Pai: FOR 10, VIT 8, VEL 6, INT 12, CAR 4
- Mãe: FOR 8, VIT 10, VEL 8, INT 10, CAR 6
- Filho nasce com:
  - FOR: floor(0,25 × (10 + 8) ÷ 2) = floor(0,25 × 9) = 2
  - VIT: floor(0,25 × (8 + 10) ÷ 2) = floor(0,25 × 9) = 2
  - VEL: floor(0,25 × (6 + 8) ÷ 2) = floor(0,25 × 7) = 1
  - INT: floor(0,25 × (12 + 10) ÷ 2) = floor(0,25 × 11) = 2
  - CAR: floor(0,25 × (4 + 6) ÷ 2) = floor(0,25 × 5) = 1

**Exemplo 2 — Herança de profissão**
- Pai tem Construtor PE base 8, Olaria PE base 4
- Mãe tem Construtor PE base 6
- Filho nasce com:
  - Construtor: floor(0,25 × (8 + 6) ÷ 2) = floor(0,25 × 7) = 1
  - Olaria (neste caso, seria outra profissão — esta é só exemplo): 0 (mãe não tem)

**Exemplo 3 — Bônus da família líder**
- Líder com CAR 20: bônus = +1% × (20 ÷ 2) = +10% de eficiência em toda a vila.
- Líder com CAR 12: bônus = +1% × (12 ÷ 2) = +6% de eficiência.

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — casas fornecem núcleos e vagas
- [ciclo-de-vida.md](ciclo-de-vida.md) — morte e envelhecimento afetam família
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — alimentação afeta reprodução

## Questões em aberto

- [proposta] Herança fixa em 25%: ajustar com base em testes de balanceamento?
- [proposta] Limite de sobrenomes no imigrante: gerar aleatoriamente ou pedir ao jogador?
