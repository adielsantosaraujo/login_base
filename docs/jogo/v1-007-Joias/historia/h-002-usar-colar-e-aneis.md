# H-002 — Usar colar e anéis

**Épico:** [../joias.md](../joias.md) · **Domínio:** [../joias.md](../joias.md)

## História

Como jogador, quero equipar colares e anéis no painel da pessoa, para que seus atributos e Vida máxima aumentem conforme os efeitos das joias.

## Contexto

Joias equipadas somam seus efeitos às características e Vida máxima de um cidadão. Cada cidadão pode equipar no máximo 1 colar e 2 anéis simultaneamente (seção 7.1). Os efeitos intrínsecos das joias (se existirem) também somam. Requisitos para equipar: pessoa ≥14 anos (seção 7.5).

Consultar:
- [../joias.md](../joias.md) — slots máximos (1 colar, 2 anéis).
- [../../v1-011-itens-e-fabricacao/equipamento.md](../../v1-011-itens-e-fabricacao/equipamento.md) (seção Equipar/trocar)
  > No painel da pessoa: escolher item do inventário para um slot; o item anterior volta ao inventário.

- [../../v1-002-cidadaos/cidadao.md](../../v1-002-cidadaos/cidadao.md) (seção PE efetivo e Eficiência)
  > Característica total = base + bônus de itens/pedras.

## Critérios de aceite

### CA1 — Colar equipado soma +Vida máxima

- **Dado** um cidadão com Vida máxima base 50 PV; Colar L3 (+15 Vida) no inventário, já equipado.
- **Quando** sistema calcula PV máximo (fórmula 10.1: `30 + 5×VIT + 3×G + Σ VIDA`).
- **Então** Vida máxima total = 50 + 15 = 65 PV; o +15 é registrado como origem "item" (Colar).

### CA2 — Dois anéis equipados somam características

- **Dado** um cidadão com FOR base 5; Anel 1 L1 (FOR) +1 e Anel 2 L2 (FOR) +1, ambos equipados.
- **Quando** sistema calcula FOR total para PE efetivo.
- **Então** FOR total = 5 + 1 + 1 = 7; PE Guerreiro efetivo usa 7.

### CA3 — Bônus intrínseco de joia somado

- **Dado** Colar L5 Excelente com intrínseco +2 VIDA, equipado.
- **Quando** PV máximo calculado.
- **Então** Vida máxima aumenta de +25 (Colar L5) + 2 (intrínseco) = +27 total.

### CA4 — Máximo 1 colar equipado

- **Dado** um cidadão com Colar 1 equipado.
- **Quando** o jogador tenta equipar Colar 2 em um slot livre.
- **Então** rejeitado com mensagem "Já há 1 colar equipado; máximo de colares é 1".

### CA5 — Máximo 2 anéis equipados

- **Dado** um cidadão com Anel 1 e Anel 2 equipados.
- **Quando** o jogador tenta equipar Anel 3.
- **Então** rejeitado com mensagem "Já há 2 anéis equipados; máximo de anéis é 2".

### CA6 — Desequipar move joia ao inventário

- **Dado** um cidadão com Colar L1 equipado.
- **Quando** o jogador clica em "desequipar" no painel.
- **Então** Colar volta ao inventário da vila (sem dono atribuído ou com dono anterior); PV máximo diminui de volta.

### CA7 — Requisito de idade ≥14 anos

- **Dado** uma criança com 10 anos.
- **Quando** o jogador tenta equipar qualquer joia.
- **Então** rejeitado com mensagem "Mínimo 14 anos para equipar joias".

### CA8 — Membro de tropa em expedição: bloqueado

- **Dado** um guerreiro em tropa em expedição (estado EM_VIAGEM_IDA).
- **Quando** o jogador tenta desequipar joia.
- **Então** rejeitado com mensagem "Não é permitido trocar equipamento em expedição".

## Tarefas

- [h-002-tarefa-001-slots-de-joias-e-efeitos.md](h-002-tarefa-001-slots-de-joias-e-efeitos.md)

## Fora de escopo

- Interface de tela do painel (fica em tarefa do frontend).
- Cálculo de PE efetivo (já existe em cidadao).
- Fórmula de PV máximo (já existe em batalha).
