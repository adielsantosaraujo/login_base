## MODIFIED Requirements

### Requirement: Treino em lote
O treinamento de unidades MUST suportar pedidos de múltiplas unidades (lote) em uma única ordem. A configuração de ordem especifica: tipo de tropa (SOLDADO, ARQUEIRO, LANCEIRO), nível da arma (1–23, modelo determinado pelo tipo), modelo da armadura (ARMADURA_COURO ou ARMADURA_FERRO), nível da armadura (1–23), e quantidade de unidades 1–15 (limitada pelo máximo treinável). O servidor MUST validar estritamente: se a quantidade solicitada exceder qualquer limite (armas disponíveis do modelo/nível exigidos, armaduras disponíveis do modelo/nível escolhidos, comida, capacidade do exército), a ordem é rejeitada com um código de erro apropriado (ITEM_INDISPONIVEL, RECURSOS_INSUFICIENTES, CAPACIDADE_EXERCITO) e nenhuma mudança ocorre no estado. Na aceitação da ordem, a comida é debitada imediatamente (comida_tipo × quantidade) e N armas e N armaduras são reservadas (menores ids primeiro) com status RESERVADO. O tempo de conclusão é calculado como `ceil(tempo_tipo × quantidade / velocidade)` (padrão da forja). Quando a ordem conclui, as N unidades surgem juntas, cada uma com 1 arma + 1 armadura do lote reservado; os itens passam a status EQUIPADO.

#### Scenario: Lote de 3 Soldados debita comida e reserva itens
- **WHEN** uma vila tem 200 comida e pede um lote de 3 Soldados (custo 50 comida cada, 3 Espadas N1, 4 Armaduras de couro N1 disponíveis)
- **THEN** a ordem é aceita, a comida passa para 50 (200 − 150), 3 Espadas N1 e 3 Armaduras de couro N1 passam a RESERVADO
- **AND** o tempo é calculado (ex: ceil(60 × 3 / 1) = 180 s)

#### Scenario: Conclusão entrega 3 unidades juntas
- **WHEN** a ordem de lote de 3 conclui
- **THEN** 3 unidades DISPONIVEL surgem na vila, cada uma com 1 Espada N1 + 1 Armadura de couro N1 (EQUIPADO)

#### Scenario: Itens insuficientes rejeita com 422 ITEM_INDISPONIVEL
- **WHEN** a ordem pede 4 Espadas N1 mas há apenas 3 disponíveis
- **THEN** a requisição retorna 422 ITEM_INDISPONIVEL
- **AND** nenhuma mudança ocorre (comida preservada, nenhum item reservado)

#### Scenario: Quantidade 0 ou acima do limite rejeita com 400
- **WHEN** a quantidade é 0 ou 16
- **THEN** a requisição retorna 400 (invalid request)

#### Scenario: Nível de arma ou armadura fora da faixa rejeita com 400
- **WHEN** o nível da arma ou da armadura é 0 ou 24
- **THEN** a requisição retorna 400 `REQUISICAO_INVALIDA`

#### Scenario: Treino com itens de nível alto
- **WHEN** a vila tem 1 Espada N23 e 1 Armadura de ferro N23 disponíveis, comida e capacidade suficientes, e pede 1 Soldado com arma nível 23 e Armadura de ferro nível 23
- **THEN** a ordem é aceita e os dois itens passam a RESERVADO
