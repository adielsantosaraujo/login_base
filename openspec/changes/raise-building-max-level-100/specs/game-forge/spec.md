## MODIFIED Requirements

### Requirement: Modelos e atributos por nível
O sistema SHALL reconhecer cinco modelos de itens: ESPADA (arma), LANCA (arma), ARCO (arma), ARMADURA_COURO (armadura) e ARMADURA_FERRO (armadura). Cada modelo tem atributos derivados do nível L (1–23) conforme a tabela em A.6, com as mesmas fórmulas lineares para todos os níveis: ESPADA ataque `6 + 2(L−1)`, LANCA ataque `5 + 2(L−1)`, ARCO ataque `4 + 2(L−1)` (alcances 1, 1 e 3), ARMADURA_COURO defesa `2 + (L−1)`, ARMADURA_FERRO defesa `3 + 2(L−1)`.

#### Scenario: ESPADA nível 3
- **WHEN** uma ESPADA tem nível 3
- **THEN** seus atributos são ataque 10 (6 + 2×(3−1)), alcance 1, categoria ARMA

#### Scenario: ARMADURA_FERRO nível 2
- **WHEN** uma ARMADURA_FERRO tem nível 2
- **THEN** seus atributos são defesa 7 (3 + 2×(2−1)), categoria ARMADURA

#### Scenario: ESPADA nível 23
- **WHEN** uma ESPADA tem nível 23
- **THEN** seus atributos são ataque 50 (6 + 2×22), alcance 1

#### Scenario: ARMADURA_COURO nível 23
- **WHEN** uma ARMADURA_COURO tem nível 23
- **THEN** sua defesa é 24 (2 + 22)

### Requirement: Nível limitado pela forja
A FORJA MUST estar em nível ≥ 1 para forjar. O nível solicitado do item MUST ser ≤ nível máximo forjável da FORJA de nível N, definido por faixas: N para N ≤ 10; `10 + ⌊(N−10)/5⌋` para 11 ≤ N ≤ 50; `18 + ⌊(N−50)/10⌋` para 51 ≤ N ≤ 100 (máximo 23 no nível 100). Violações MUST retornar 422 `REQUISITO_NAO_ATENDIDO`.

#### Scenario: Forja nível 0
- **WHEN** a vila tem FORJA nível 0 e tenta forjar qualquer item
- **THEN** a operação é rejeitada com 422 `REQUISITO_NAO_ATENDIDO`

#### Scenario: Item nível acima da forja
- **WHEN** a vila tem FORJA nível 1 e tenta forjar ESPADA nível 2
- **THEN** a operação é rejeitada com 422 `REQUISITO_NAO_ATENDIDO`

#### Scenario: Forja nível 11 ainda forja até 10
- **WHEN** a vila tem FORJA nível 11 e tenta forjar ESPADA nível 11
- **THEN** a operação é rejeitada com 422 `REQUISITO_NAO_ATENDIDO`

#### Scenario: Forja nível 15 forja nível 11
- **WHEN** a vila tem FORJA nível 15 e ordena 1 ESPADA nível 11 com recursos suficientes
- **THEN** a operação é aceita

#### Scenario: Forja nível 60
- **WHEN** a vila tem FORJA nível 60
- **THEN** o nível máximo forjável é 19 (18 + ⌊(60−50)/10⌋)

#### Scenario: Forja nível 100 forja nível 23
- **WHEN** a vila tem FORJA nível 100 e ordena 1 ESPADA nível 23 com recursos suficientes
- **THEN** a operação é aceita

### Requirement: Validação da ordem
O nível solicitado MUST estar entre 1 e 23; a quantidade MUST estar entre 1 e 5. Valores fora dessas faixas MUST retornar 400 `REQUISICAO_INVALIDA`.

#### Scenario: Nível válido
- **WHEN** o jogador ordena um item com nível 3
- **THEN** a operação é aceita

#### Scenario: Nível fora da faixa
- **WHEN** o jogador ordena um item com nível 0 ou nível 24
- **THEN** a operação é rejeitada com 400 `REQUISICAO_INVALIDA`

#### Scenario: Quantidade inválida
- **WHEN** o jogador ordena 0 ou 6 itens
- **THEN** a operação é rejeitada com 400 `REQUISICAO_INVALIDA`
