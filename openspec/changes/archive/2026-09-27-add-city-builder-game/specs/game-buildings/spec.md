# Spec Delta

## Purpose

Define a construção e melhoria de prédios da vila, incluindo custos, tempos, pré-requisitos, limites de nível e efeitos de cada tipo de prédio.

## ADDED Requirements

### Requirement: Tipos de prédio e efeitos
O sistema SHALL reconhecer oito tipos de prédio (CENTRO_VILA, ARMAZEM, FAZENDA, SERRARIA, PEDREIRA, MINA_FERRO, FORJA, QUARTEL), cada um com um efeito específico no nível N definido em A.3. O efeito MUST estar ativo imediatamente quando a ordem de construção é concluída.

#### Scenario: Pedreira nível 3 produz pedra
- **WHEN** a pedreira da vila está nível 3
- **THEN** a produção de pedra aumenta em 60/h (20 × 3)

#### Scenario: Centro da vila nível 1
- **WHEN** o centro da vila está nível 1
- **THEN** nenhum outro prédio pode ir além do nível 1

#### Scenario: Armazém nível 5
- **WHEN** o armazém está nível 5
- **THEN** a capacidade de cada recurso é 8000 (500 × 2^(5−1))

### Requirement: Custo e tempo por nível
Para cada tipo de prédio e cada nível alvo N (1–5), o custo SHALL ser calculado como `round_half_up(base × 1,5^(N−1))` e o tempo como `tempoBase × 2^(N−1)` segundos, conforme a tabela em A.3. Esses valores MUST ser aplicados com a velocidade configurada (custos em recurso, tempos divididos).

#### Scenario: Serraria 1→2
- **WHEN** a serraria está nível 1 e o jogador tenta melhorá-la para nível 2
- **THEN** o custo é M90 P60 (M150×0,6=90 arredondado, P60×1=60)
- **AND** o tempo de conclusão é 120 s com velocidade 1

#### Scenario: Forja nível 1
- **WHEN** a forja está nível 0 e o jogador tenta construir (atingir nível 1)
- **THEN** o custo é M120 P100 F40
- **AND** o tempo de conclusão é 120 s

### Requirement: Limite pelo centro da vila
Se um prédio diferente de CENTRO_VILA tiver nível menor que o alvo, a melhoria MUST ser rejeitada com 422 `REQUISITO_NAO_ATENDIDO` a menos que o alvo seja ≤ nível do CENTRO_VILA.

#### Scenario: Centro nível 1, armazém nível 1
- **WHEN** a vila tem centro nível 1 e armazém nível 1, e o jogador tenta melhorar o armazém para nível 2
- **THEN** a operação é rejeitada com 422 `REQUISITO_NAO_ATENDIDO`

#### Scenario: Centro nível 2, qualquer prédio pode ir a 2
- **WHEN** a vila tem centro nível 2 e tenta melhorar a fazenda para nível 2
- **THEN** a operação é aceita (2 ≤ 2)

### Requirement: Pré-requisitos de forja e quartel
Para construir a FORJA, a MINA_FERRO MUST estar em nível ≥ 1. Para construir o QUARTEL, a FORJA MUST estar em nível ≥ 1. Violações MUST rejeitar com 422 `REQUISITO_NAO_ATENDIDO`.

#### Scenario: Construir forja sem mina
- **WHEN** a mina de ferro está nível 0 e o jogador tenta construir a forja (nível 0→1)
- **THEN** a operação é rejeitada com 422 `REQUISITO_NAO_ATENDIDO`

#### Scenario: Construir quartel com forja nível 1
- **WHEN** a forja está nível 1 e o jogador tenta construir o quartel
- **THEN** a operação é aceita

### Requirement: Fila de construção única
A vila MUST ter no máximo uma ordem de construção ativa de cada vez. Se houver uma ordem CONSTRUCAO não vencida, nova melhoria MUST ser rejeitada com 422 `FILA_OCUPADA`.

#### Scenario: Segunda construção enquanto há uma ativa
- **WHEN** há uma ordem CONSTRUCAO em andamento (não vencida) e o jogador tenta iniciar outra melhoria
- **THEN** a operação é rejeitada com 422 `FILA_OCUPADA`

#### Scenario: Construção anterior concluiu
- **WHEN** a ordem CONSTRUCAO anterior foi concluída e removida, e o jogador tenta iniciar nova melhoria
- **THEN** a operação é aceita

### Requirement: Nível máximo 5
Se um prédio já está no nível 5, qualquer tentativa de melhoria MUST ser rejeitada com 422 `NIVEL_MAXIMO`.

#### Scenario: Prédio no nível máximo
- **WHEN** um prédio está nível 5 e o jogador tenta melhorá-lo
- **THEN** a operação é rejeitada com 422 `NIVEL_MAXIMO`

### Requirement: Débito no início e efeito na conclusão
Os recursos MUST ser debitados no instante em que a ordem é criada. O nível do prédio permanece inalterado até que a ordem seja concluída. No instante de conclusão, o nível passa para o alvo e o efeito torna-se ativo (incluindo a criação de novo canteiro TRIGO se for a FAZENDA).

#### Scenario: Recursos debitados imediatamente
- **WHEN** o jogador inicia a melhoria de uma serraria e tem exatamente M90 P60
- **THEN** os recursos são debitados imediatamente (agora tem 0)
- **AND** a serraria permanece nível 1 até `concluiEm`

#### Scenario: Nível alvo alcançado na conclusão
- **WHEN** uma ordem de melhoria é aplicada (ordem vencida)
- **THEN** o nível do prédio muda para o alvo
- **AND** o efeito do novo nível passa a valer

#### Scenario: Novo canteiro ao melhorar a fazenda
- **WHEN** a fazenda nível 1 é melhorada para nível 2 e a ordem é concluída
- **THEN** um novo canteiro (posição 2) é criado automaticamente com cultivo TRIGO
