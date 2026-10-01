# Joias

**Épico:** [joias.md](joias.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Joias são itens de adorno que concedem bônus de características ou efeitos especiais. Cada cidadão pode equipar até 1 colar e 2 anéis, fabricadas nas oficinas ou obtidas como recompensa de masmorras. Os efeitos das joias equipadas somam-se aos atributos do cidadão.

## Regras

- R1: Cada cidadão pode equipar no máximo **1 colar** e **2 anéis** simultaneamente.
- R2: As joias são fabricadas na **Ferraria** (não há oficina específica para joias no v1).
- R3: Colar e Anel têm receitas base com custo em **Ferro + Ouro** (seção 7.11 da bíblia).
- R4: Anel exige que o jogador **escolha uma característica** (VIT, FOR, VEL, INT ou CAR) ao fabricar; o efeito do anel aplica-se apenas à característica escolhida.
- R5: Colar aplica +5 × L de Vida (PV máximo).
- R6: Efeitos de joias equipadas somam-se em Vida e na característica correspondente; não há limite para a soma.
- R7: Requisitos para equipar joias: pessoa ≥14 anos (seção 7.5); sem requisito de PE.
- R8: Joias podem ser aprimoradas (L→L+1) como outros itens (seção 7.4).
- R9: Joias participam do sistema de qualidade e bônus intrínsecos igual a outros itens (seção 7.2).

## Números e tabelas

Tabela de joias (seção 7.11):

| Joia | Oficina | Receita base (×L) | Efeito principal |
|---|---|---|---|
| Colar (1 por pessoa) | Ferraria | 1 Ferro, 20 Ouro | +5 × L de Vida |
| Anel (2 por pessoa) | Ferraria | 1 Ferro, 15 Ouro | + característica escolhida ao fabricar: L1–3 +1, L4–6 +2, L7–9 +3, L10 +4 |

Faixa de bônus intrínsecos permitidos em joias (seção 7.2):
- Bônus de joias: VIT, FOR, VEL, INT, CAR, VIDA, CRIT, PROD.

Faixa dos intrínsecos pelo nível (seção 7.2):
- L1–4 → faixa baixa
- L5–7 → faixa média
- L8–10 → faixa alta

Catálogo de magnitudes de bônus (seção 6.2):

| Código | Baixa | Média | Alta |
|---|---|---|---|
| VIT / FOR / VEL / INT / CAR | +1 | +2 | +3 |
| VIDA | +8 | +15 | +25 |
| CRIT | +2 | +3 | +5 |
| PROD | +3% | +5% | +8% |

## Exemplos

**Colar L1:**
- Receita: 1 Ferro, 20 Ouro.
- Efeito: +5 de Vida.
- Nível máximo de fabricação: N3 (seção 4.11).

**Colar L10:**
- Receita: 10 Aço (pois L≥6 vira Aço), 200 Ouro.
- Efeito: +50 de Vida.

**Anel L1 com característica FOR escolhida:**
- Receita: 1 Ferro, 15 Ouro.
- Efeito: +1 FOR.
- Intrínseco possível (faixa baixa): +1 VIT, +1 FOR, etc.

**Anel L5 com característica VIT escolhida:**
- Receita: 5 Ferro, 75 Ouro.
- Efeito: +2 VIT.
- Intrínseco possível (faixa média): +2 VIDA, +2 VEL, etc.

**Exemplo com dois anéis e um colar:**
- Anel 1 (L3, FOR): +1 FOR.
- Anel 2 (L3, INT): +1 INT.
- Colar L3: +15 Vida.
- Soma: +1 FOR, +1 INT, +15 Vida máx. (antes de intrínsecos).

## Interações com outros domínios

- [../../v1-011-itens-e-fabricacao/itens.md](../v1-011-itens-e-fabricacao/itens.md) — sistema comum de qualidade, níveis, aprimoramento.
- [../../v1-011-itens-e-fabricacao/fabricacao.md](../v1-011-itens-e-fabricacao/fabricacao.md) — processo de fabricação, requisitos de artesão, custo ×L.
- [../../v1-011-itens-e-fabricacao/equipamento.md](../v1-011-itens-e-fabricacao/equipamento.md) — sistema de equipar/trocar itens no painel da pessoa.
- [../../v1-003-construcoes/oficinas.md](../v1-003-construcoes/oficinas.md) — Ferraria onde joias são fabricadas.
- [../../v1-002-cidadaos/cidadao.md](../v1-002-cidadaos/cidadao.md) — cidadãos equipam joias; influem em características e PV máx.

## Modelo de dados (resumo)

A tabela `item` do jogo já cobre joias; não há tabela específica:

- `item.categoria = 'JOIA'`
- `item.subtipo` = 'COLAR' ou 'ANEL'
- `item.nivel` = L1..L10
- `item.qualidade` = Simples/Boa/Excelente/Divina
- `item.bonus` = jsonb com intrínsecos e efeito principal
- `item.atributo_escolhido` = para Anel, a característica escolhida (VIT, FOR, VEL, INT, CAR)
- `item.cidadao_id` = pessoa que equipa (opcional)
- `item.slot` = 'COLAR' ou 'ANEL1' / 'ANEL2' (ou similar)

Validações em banco/backend:
- Máximo 1 item.slot = 'COLAR' por cidadão.
- Máximo 2 itens.slot = 'ANEL' por cidadão.

## Histórias

- [historia/h-001-fabricar-joias.md](historia/h-001-fabricar-joias.md) — fabricar colar e anel com custo em Ferro e Ouro.
- [historia/h-002-usar-colar-e-aneis.md](historia/h-002-usar-colar-e-aneis.md) — equipar joias no painel e ver efeitos somados.

## Questões em aberto

- Nenhuma prevista neste épico (ver seção 6 do plano para propostas gerais).
