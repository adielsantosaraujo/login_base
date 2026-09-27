# game-dungeon-loot Specification

## Purpose

Define o sistema de recompensas em masmorras: recursos garantidos por nível, rolagens de itens e sementes com probabilidades, e registros de loot na batalha.

## Requirements

### Requirement: Recursos garantidos
Ao vencer uma masmorra de nível N, o jogador MUST receber um ganho garantido de: `40N` de comida, `50N` de madeira, `50N` de pedra, `20N` de ferro (valores em inteiros de recursos). Os ganhos MUST respeitar a capacidade do armazém — recursos que ultrapassassem a capacidade são perdidos.

#### Scenario: Loot do nível 1
- **WHEN** o jogador vence a masmorra nível 1
- **THEN** ganha 40 de comida, 50 de madeira, 50 de pedra, 20 de ferro

#### Scenario: Loot do nível 2
- **WHEN** o jogador vence a masmorra nível 2
- **THEN** ganha 80 de comida, 100 de madeira, 100 de pedra, 40 de ferro

#### Scenario: Respeito à capacidade
- **WHEN** o armazém já tem 450 comida (capacidade 500) e o ganho é 40
- **THEN** a comida vai para 490 (não 500, mas sim min(cap, atual + ganho) = min(500, 490))
- **AND** o ganho registrado no loot é 40, mas efetivamente 40 foram creditados

#### Scenario: Recurso cheio não perde mais
- **WHEN** o armazém tem 500 de ferro (capacidade 500) e o ganho é 20
- **THEN** o ferro permanece 500, o ganho não ocorre, mas o loot registra os 20 "tentados"

### Requirement: Rolagens de loot
Além dos recursos garantidos, o jogador MUST fazer um número de rolagens baseado no nível: N1 = 2, N2 = 2, N3 = 3, N4 = 3, N5 = 4 rolagens. Cada rolagem MUST seguir um gerador de números aleatórios interno (determinístico, sequencial) e classificar o resultado em três categorias: semente (faixas 0–34), material extra (faixas 35–59), item forjável (faixas 60–99).

#### Scenario: 2 rolagens no nível 1
- **WHEN** o gerador retorna [10, 0, 70, 0, 1, …]
- **THEN** primeira rolagem 10 (semente), segunda rolagem 0 (semente), terceira rolagem 70 (item)
- **AND** o loot total inclui 2 sementes e 0 itens da rolagem (pois são apenas 2 rolagens no nível 1)

#### Scenario: 3 rolagens no nível 3
- **WHEN** o nível é 3 (3 rolagens) e o gerador retorna [10, 50, 75, …]
- **THEN** primeira rolagem 10 (semente), segunda rolagem 50 (material), terceira rolagem 75 (item)

### Requirement: Sementes por nível
As sementes liberadas SHALL variar por nível de masmorra: `MILHO` em N≥1, `BATATA` em N≥2, `ABOBORA_DOURADA` em N≥4. Na rolagem de semente, o gerador MUST escolher entre as liberadas com pesos (MILHO 60, BATATA 30, ABOBORA_DOURADA 10). Tentar obter uma semente não liberada (ex.: BATATA no nível 1) MUST retornar nenhuma semente nessa rolagem.

#### Scenario: Sementes do nível 1
- **WHEN** uma rolagem resulta em semente no nível 1
- **THEN** apenas MILHO é possível

#### Scenario: Sementes do nível 2
- **WHEN** uma rolagem resulta em semente no nível 2
- **THEN** MILHO ou BATATA são possíveis

#### Scenario: Sementes do nível 4
- **WHEN** uma rolagem resulta em semente no nível 4
- **THEN** MILHO, BATATA ou ABOBORA_DOURADA são possíveis

### Requirement: Itens com nível próprio
Ao rolar um item (faixa 60–99), o gerador MUST escolher o modelo (ESPADA, LANCA, ARCO, ARMADURA_COURO, ARMADURA_FERRO, pesos iguais) e o nível MUST ser calculado como `min(5, N + d)`, onde `d` é uma rolagem binária (0 ou 1). O item MUST ser criado com origem `MASMORRA` e status `DISPONIVEL`.

#### Scenario: Nível do item — caso base
- **WHEN** uma rolagem de item resulta em ESPADA no nível 1 com `d = 0`
- **THEN** o item é ESPADA nível 1

#### Scenario: Nível do item — com bônus
- **WHEN** uma rolagem de item resulta em ARMADURA_FERRO no nível 3 com `d = 1`
- **THEN** o item é ARMADURA_FERRO nível 4

#### Scenario: Nível do item — cap em 5
- **WHEN** uma rolagem no nível 5 com `d = 1` resultaria em nível 6
- **THEN** o item é criado com nível 5

### Requirement: Nenhum loot na derrota
Se a batalha termina em `DERROTA`, nenhum loot MUST ser aplicado — nem recursos garantidos nem itens/sementes de rolagens. O objeto `BatalhaDto.loot` MUST permanecer nulo.

#### Scenario: Derrota não gera loot
- **WHEN** o jogador é derrotado na masmorra
- **THEN** `loot` é nulo e nenhum recurso/item é adicionado à vila

#### Scenario: Vitória gera loot
- **WHEN** o jogador vence
- **THEN** `loot` contém `recursos`, `sementes` e `itens` com os ganhos

### Requirement: Loot registrado na batalha
O loot completo (recursos, sementes, itens com modelo, nível e origem) MUST ser registrado na batalha como JSON após a vitória. A resposta `BatalhaDto` MUST incluir este `loot` quando a batalha termina em `VITORIA`.

#### Scenario: Loot exibido após vitória
- **WHEN** a batalha termina em `VITORIA`
- **THEN** `BatalhaDto.loot` contém:
  ```json
  {
    "recursos": {"COMIDA": 40, "MADEIRA": 50, "PEDRA": 50, "FERRO": 20},
    "sementes": {"MILHO": 1, "BATATA": 0, "ABOBORA_DOURADA": 0},
    "itens": [
      {"modelo": "ESPADA", "nivel": 2, "origem": "MASMORRA"},
      {"modelo": "ARMADURA_COURO", "nivel": 1, "origem": "MASMORRA"}
    ]
  }
  ```

#### Scenario: Sem loot na derrota
- **WHEN** a batalha termina em `DERROTA`
- **THEN** `BatalhaDto.loot` é nulo
