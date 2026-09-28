## MODIFIED Requirements

### Requirement: Canteiros da fazenda
O número de canteiros disponíveis MUST ser N para a FAZENDA no nível N ≤ 5 e `5 + ⌊(N−5)/5⌋` para N > 5 (máximo de 24 canteiros no nível 100). Quando a FAZENDA é construída ou melhorada e o número de canteiros do novo nível é maior que o número de canteiros existentes, cada novo canteiro é criado automaticamente com o cultivo TRIGO, na próxima posição livre; quando o número não muda, nenhum canteiro é criado.

#### Scenario: Fazenda nível 1
- **WHEN** a fazenda está nível 1
- **THEN** existe exatamente 1 canteiro
- **AND** esse canteiro tem cultivo TRIGO

#### Scenario: Melhoria da fazenda cria canteiro
- **WHEN** a fazenda é melhorada de nível 1 para 2 e a ordem é concluída
- **THEN** um novo canteiro (posição 2) é criado com cultivo TRIGO

#### Scenario: Fazenda nível 5
- **WHEN** a fazenda está nível 5
- **THEN** existem 5 canteiros

#### Scenario: Fazenda nível 6 não ganha canteiro
- **WHEN** a fazenda é melhorada de nível 5 para 6 e a ordem é concluída
- **THEN** continuam existindo 5 canteiros

#### Scenario: Fazenda nível 10
- **WHEN** a fazenda é melhorada de nível 9 para 10 e a ordem é concluída
- **THEN** existem 6 canteiros e o canteiro da posição 6 tem cultivo TRIGO

#### Scenario: Fazenda nível 100
- **WHEN** a fazenda está nível 100
- **THEN** o número de canteiros é 24

### Requirement: Plantio consome semente
O ato de plantar um cultivo em um canteiro é instantâneo e consome 1 semente do tipo respectivo (exceto TRIGO, que não exige semente). A posição do canteiro MUST estar entre 1 e o número de canteiros do nível atual da FAZENDA (ver Requirement: Canteiros da fazenda). Erros MUST retornar 422: canteiro inexistente → `CANTEIRO_INEXISTENTE`, sem semente → `SEMENTE_INDISPONIVEL`.

#### Scenario: Plantio de MILHO com semente disponível
- **WHEN** a vila tem 2 sementes de MILHO e o jogador planta MILHO no canteiro 1
- **THEN** o canteiro 1 passa a ser MILHO desde agora
- **AND** a vila fica com 1 semente de MILHO

#### Scenario: Plantio de TRIGO não consome semente
- **WHEN** o jogador planta TRIGO (sem semente no inventário)
- **THEN** o plantio é aceito
- **AND** nenhuma semente é consumida

#### Scenario: Sem semente disponível
- **WHEN** o jogador tenta plantar MILHO sem sementes disponíveis
- **THEN** a operação é rejeitada com 422 `SEMENTE_INDISPONIVEL`
- **AND** nenhuma semente é consumida

#### Scenario: Canteiro inexistente
- **WHEN** a fazenda está nível 2 (2 canteiros) e o jogador tenta plantar na posição 3
- **THEN** a operação é rejeitada com 422 `CANTEIRO_INEXISTENTE`

#### Scenario: Posição acima do número de canteiros em nível alto
- **WHEN** a fazenda está nível 9 (5 canteiros) e o jogador tenta plantar na posição 6
- **THEN** a operação é rejeitada com 422 `CANTEIRO_INEXISTENTE`

#### Scenario: Sexto canteiro disponível no nível 10
- **WHEN** a fazenda está nível 10 (6 canteiros) e o jogador planta TRIGO na posição 6
- **THEN** o plantio é aceito
