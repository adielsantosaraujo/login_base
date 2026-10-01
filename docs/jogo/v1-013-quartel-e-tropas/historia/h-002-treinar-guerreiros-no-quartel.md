# H-002 — Treinar guerreiros no quartel

**Épico:** [../tropas.md](../tropas.md) · **Domínio:** [../tropas.md](../tropas.md)

## História

Como jogador, quero que meus guerreiros ganhem experiência (XP) ao serem treinados no quartel, aumentando seu PE de Guerreiro e tornando-os mais fortes.

## Contexto

Guerreiros em tropa no estado AQUARTELADA recebem treinamento automático a cada turno. O XP ganho depende do nível do quartel: N1 0,5 XP/turno, N2 1,0 XP/turno, N3 1,5 XP/turno (seção 4.12). Cada 10 XP acumulados geram +1 PE base de Guerreiro, aumentando atributos de combate (Ataque, Defesa, PV máximo) (seção 9.4).

XP também é ganho em vitórias em masmorras (8.4), o que complementa o treinamento.

## Critérios de aceite

### CA1 — Guerreiro aquartelado ganha XP a cada turno
- **Dado** um quartel N2 com 1 guerreiro alocado, tropa com 1 membro aquartelado.
- **Quando** um turno é processado.
- **Então** o guerreiro recebe 1,0 XP (conforme nível do quartel).

### CA2 — XP não é ganho se em expedição
- **Dado** um guerreiro em tropa EM_VIAGEM_IDA.
- **Quando** um turno é processado.
- **Então** o guerreiro não recebe XP de treinamento.

### CA3 — Cada 10 XP = +1 PE base
- **Dado** um guerreiro com 8 PE base, 7 XP acumulado.
- **Quando** o turno avança (recebe 1 XP em N2 = 8 total; no seguinte: 1 XP = 9; no seguinte: 1 XP = 10).
- **Então** no terceiro turno, XP atinge 10 e PE base sobe para 9; XP retorna a 0.

### CA4 — XP acumula por guerreiro
- **Dado** 2 guerreiros na mesma tropa aquartelada em N1.
- **Quando** 10 turnos passam (10 × 0,5 = 5 XP cada).
- **Então** cada um tem 5 XP; nenhum chega a 10 ainda (PE não aumenta).

### CA5 — Múltiplas tropas no mesmo quartel ganham XP
- **Dado** um quartel N2 com 2 tropas, 3 membros em cada, todas AQUARTELADA.
- **Quando** um turno é processado.
- **Então** todos os 6 guerreiros recebem 1,0 XP (etapa 7 do turno, seção 2.2).

### CA6 — XP é persistido entre turnos
- **Dado** um guerreiro com 9 XP após turno T.
- **Quando** o turno T+1 é processado (recebe +1 XP = 10 total).
- **Então** PE base sobe para N+1, XP retorna a 0, persistido no banco.

## Tarefas

- [h-002-tarefa-001-xp-de-treinamento-no-turno.md](h-002-tarefa-001-xp-de-treinamento-no-turno.md)

## Fora de escopo

- Visualização de XP em tempo real no frontend (abordado em task posterior).
- Bônus de XP por bater metas.
- Penalidade de XP por derrota.
