# Spec Delta

## Purpose

Define a forja de itens de combate (armas e armaduras) com receitas e tempos específicos, controle de nível pela capacidade da forja, e armazenamento dos itens forjados.

## ADDED Requirements

### Requirement: Modelos e atributos por nível
O sistema SHALL reconhecer cinco modelos de itens: ESPADA (arma), LANCA (arma), ARCO (arma), ARMADURA_COURO (armadura) e ARMADURA_FERRO (armadura). Cada modelo tem atributos derivados do nível (1–5) conforme a tabela em A.6: ataque e alcance para armas, defesa para armaduras.

#### Scenario: ESPADA nível 3
- **WHEN** uma ESPADA tem nível 3
- **THEN** seus atributos são ataque 10 (6 + 2×(3−1)), alcance 1, categoria ARMA

#### Scenario: ARMADURA_FERRO nível 2
- **WHEN** uma ARMADURA_FERRO tem nível 2
- **THEN** seus atributos são defesa 7 (3 + 2×(2−1)), categoria ARMADURA

### Requirement: Receitas e ordem de forja
O sistema SHALL processar ordens de forja com base na receita do modelo. O custo total = custo base × nível × quantidade; o tempo total = `ceil(tempoBase × nível × quantidade / velocidade)` segundos. Todos os itens de uma ordem são entregues juntos no instante de conclusão.

#### Scenario: Dois ESPADAS nível 2 com forja nível 2
- **WHEN** a vila tem FORJA nível 2 e o jogador ordena 2 ESPADAS nível 2
- **THEN** o custo é M80 F120 (base M20 F30 × nível 2 × qtd 2)
- **AND** o tempo de conclusão é 240 s (60 s base × 2 × 2 ÷ velocidade 1)

#### Scenario: Uma ARMADURA_COURO nível 1
- **WHEN** a vila tem FORJA nível 1 e o jogador ordena 1 ARMADURA_COURO nível 1
- **THEN** o custo é C20 M10 F5
- **AND** o tempo de conclusão é 45 s

### Requirement: Nível limitado pela forja
A FORJA MUST estar em nível ≥ 1 para forjar. O nível solicitado do item MUST ser ≤ nível da FORJA. Violações MUST retornar 422 `REQUISITO_NAO_ATENDIDO`.

#### Scenario: Forja nível 0
- **WHEN** a vila tem FORJA nível 0 e tenta forjar qualquer item
- **THEN** a operação é rejeitada com 422 `REQUISITO_NAO_ATENDIDO`

#### Scenario: Item nível acima da forja
- **WHEN** a vila tem FORJA nível 1 e tenta forjar ESPADA nível 2
- **THEN** a operação é rejeitada com 422 `REQUISITO_NAO_ATENDIDO`

### Requirement: Uma ordem por vez
A vila MUST ter no máximo uma ordem de forja ativa. Se houver uma ordem FORJA não vencida, nova ordem MUST ser rejeitada com 422 `FILA_OCUPADA`.

#### Scenario: Segunda ordem de forja enquanto há uma ativa
- **WHEN** há uma ordem FORJA em andamento (não vencida) e o jogador tenta criar outra ordem de forja
- **THEN** a operação é rejeitada com 422 `FILA_OCUPADA`

### Requirement: Entrega na conclusão
Quando uma ordem de forja é concluída, o sistema SHALL criar todos os itens solicitados com status `DISPONIVEL` e origem `FORJA`. Os itens passam imediatamente a estar disponíveis no inventário.

#### Scenario: Ordem de forja concluída
- **WHEN** uma ordem de forja é vencida (ordem aplicada)
- **THEN** todos os itens solicitados aparecem no inventário com status `DISPONIVEL`
- **AND** a origem de cada item é `FORJA`

#### Scenario: Itens aparecem juntos
- **WHEN** uma ordem de 3 ESPADAS nível 2 é concluída
- **THEN** as 3 ESPADAS aparecem no mesmo instante (não há ESPADA 1 antes de ESPADA 2)

### Requirement: Validação da ordem
O nível solicitado MUST estar entre 1 e 5; a quantidade MUST estar entre 1 e 5. Valores fora dessas faixas MUST retornar 400 `REQUISICAO_INVALIDA`.

#### Scenario: Nível válido
- **WHEN** o jogador ordena um item com nível 3
- **THEN** a operação é aceita

#### Scenario: Nível fora da faixa
- **WHEN** o jogador ordena um item com nível 0 ou nível 6
- **THEN** a operação é rejeitada com 400 `REQUISICAO_INVALIDA`

#### Scenario: Quantidade inválida
- **WHEN** o jogador ordena 0 ou 6 itens
- **THEN** a operação é rejeitada com 400 `REQUISICAO_INVALIDA`
