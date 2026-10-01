# H-003 — Enviar tropa em expedição

**Épico:** [../tropas.md](../tropas.md) · **Domínio:** [../tropas.md](../tropas.md), [../expedicoes.md](../expedicoes.md)

## História

Como jogador, quero enviar uma tropa em expedição para atacar uma masmorra ativa, calculando o tempo de viagem, consumindo comida e resolvendo a batalha ao chegar.

## Contexto

Expedição é a missão de uma tropa para atacar masmorra. Ao enviar a tropa:
1. Sistema calcula turnos de viagem (distância de Manhattan mínima até masmorra, máx. N regiões possuídas).
2. Debita comida necessária (nº membros × turnos ida+volta).
3. Tropa muda estado para EM_VIAGEM_IDA.
4. No turno que chega (após turnos_restantes decrescente a 0), batalha é resolvida (seção 2.2, passo 8).
5. Após batalha, tropa entra em EM_VIAGEM_VOLTA.
6. Ao retornar, tropa volta a AQUARTELADA, saque entregue ao estoque.

## Critérios de aceite

### CA1 — Expedição calcula turnos de viagem corretamente
- **Dado** vila com regiões 01, 02, 05, 06; masmorra em região 16 (linha 3, col 3).
- **Quando** jogador envia tropa desde região 06 (linha 1, col 1).
- **Então** distância = 2+2 = 4; turnos_viagem = 4 cada sentido (ida e volta); `turnos_restantes = 4` (ida).

### CA2 — Débito de comida é verificado
- **Dado** tropa com 8 membros, 4 turnos viagem (ida+volta = 8); estoque com 63 alimentos (Refeição).
- **Quando** jogador envia expedição (64 necessários).
- **Então** a API retorna erro 400 "Comida insuficiente" e expedição não parte.

### CA3 — Comida é debitada ao partir (não ao retornar)
- **Dado** vila com 80 alimentos, tropa 8 membros × 8 turnos = 64 alimentos necessários.
- **Quando** expedição é enviada.
- **Então** estoque fica 80−64 = 16 alimentos; tropa em EM_VIAGEM_IDA.

### CA4 — Tropa muda estado para EM_VIAGEM_IDA
- **Dado** tropa AQUARTELADA enviada em expedição.
- **Quando** POST `/api/jogo/tropa/{tropaId}/expedicao` sucesso 201.
- **Então** tropa.estado = EM_VIAGEM_IDA; `turnos_restantes = turnos_viagem`.

### CA5 — Ao chegar (turnos_restantes = 0), batalha é resolvida
- **Dado** tropa em EM_VIAGEM_IDA com `turnos_restantes = 1`.
- **Quando** turno é processado (passo 8: Movimentação de tropas).
- **Então** `turnos_restantes` decrementa a 0; **batalha é executada** no mesmo turno.

### CA6 — Após batalha, tropa entra em EM_VIAGEM_VOLTA
- **Dado** tropa que chegou à masmorra e resolveu batalha.
- **Quando** batalha conclui (vitória ou derrota).
- **Então** tropa.estado = EM_VIAGEM_VOLTA; `turnos_restantes = turnos_viagem_volta`.

### CA7 — Tropa retorna e saque é entregue
- **Dado** tropa em EM_VIAGEM_VOLTA com `turnos_restantes = 1`.
- **Quando** turno é processado (decrementa a 0).
- **Então** tropa retorna a AQUARTELADA; saque (ouro, recursos, itens, pedras) é adicionado ao estoque da vila.

### CA8 — Se masmorra sumir antes de chegar, tropa retorna vazia
- **Dado** tropa A e tropa B; ambas enviadas para mesma masmorra N5.
- **Quando** tropa A chega primeiro e vence, masmorra é removida.
- **Então** tropa B, ao chegar, encontra masmorra inexistente; muda para EM_VIAGEM_VOLTA sem batalha; retorna com saque vazio.

## Tarefas

- [h-003-tarefa-001-viagem-de-tropas-no-turno.md](h-003-tarefa-001-viagem-de-tropas-no-turno.md)
- [h-003-tarefa-002-tela-de-expedicao.md](h-003-tarefa-002-tela-de-expedicao.md)

## Fora de escopo

- Cancelar expedição em andamento (só se aquartelada).
- Rota alternativa se masmorra se move.
- Comida perecível / podre.
