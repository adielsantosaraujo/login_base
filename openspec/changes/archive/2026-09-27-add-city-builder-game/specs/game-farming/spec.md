# Spec Delta

## Purpose

Define o cultivo de alimentos na fazenda através de canteiros, sementes e a produção contínua de comida baseada no cultivo ativo.

## ADDED Requirements

### Requirement: Canteiros da fazenda
O número de canteiros disponíveis MUST ser igual ao nível da FAZENDA. Quando a FAZENDA é construída ou melhorada (nível 0→1, 1→2, etc.), cada novo canteiro é criado automaticamente com o cultivo TRIGO.

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

### Requirement: Cultivos e produção
O sistema SHALL reconhecer quatro cultivos: TRIGO (20 comida/h, sem semente), MILHO (30 comida/h, requer semente), BATATA (45 comida/h, requer semente) e ABOBORA_DOURADA (70 comida/h, requer semente). A produção total de comida da vila MUST ser a soma das produções de todos os canteiros.

#### Scenario: Dois canteiros com cultivos diferentes
- **WHEN** a vila tem 1 canteiro TRIGO e 1 canteiro MILHO
- **THEN** a produção total de comida é 50/h (20 + 30)

#### Scenario: Cinco canteiros com cultivos diversos
- **WHEN** a vila tem 5 canteiros: TRIGO (20), MILHO (30), BATATA (45), ABOBORA_DOURADA (70), TRIGO (20)
- **THEN** a produção total de comida é 185/h

### Requirement: Plantio consome semente
O ato de plantar um cultivo em um canteiro é instantâneo e consome 1 semente do tipo respectivo (exceto TRIGO, que não exige semente). Erros MUST retornar 422: canteiro inexistente → `CANTEIRO_INEXISTENTE`, sem semente → `SEMENTE_INDISPONIVEL`.

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

### Requirement: Troca de cultivo preserva a produção anterior
Quando o cultivo de um canteiro é alterado, a produção acumulada até o instante do plantio MUST usar o cultivo anterior. A sincronização SHALL produzir com a taxa antiga, depois o canteiro passa a produzir com a nova taxa.

#### Scenario: Troca de cultivo após 1 hora
- **WHEN** um canteiro tem TRIGO por 1 hora completa e então é alterado para MILHO
- **THEN** a produção acumulada desse canteiro até a troca foi 20 comida (20/h × 1 h)
- **AND** a partir do plantio, o canteiro passa a produzir 30/h de MILHO
