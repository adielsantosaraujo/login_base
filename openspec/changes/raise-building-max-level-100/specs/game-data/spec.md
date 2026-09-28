## MODIFIED Requirements

### Requirement: Integridade dos níveis e quantidades
O banco SHALL validar que os níveis dos prédios estejam entre 0 e 100, as posições dos canteiros entre 1 e 24, as quantidades de sementes sejam não-negativas, e os níveis dos itens entre 1 e 23. Toda tentativa de gravar valores fora dessas faixas MUST ser rejeitada.

#### Scenario: Prédio com nível inválido
- **WHEN** se tenta gravar um prédio com nível 101 (acima do máximo)
- **THEN** a gravação é rejeitada pela restrição de verificação

#### Scenario: Prédio no nível máximo
- **WHEN** se grava um prédio com nível 100
- **THEN** a gravação é aceita

#### Scenario: Semente com quantidade negativa
- **WHEN** se tenta gravar um registro de semente com quantidade −1
- **THEN** a gravação é rejeitada pela restrição de verificação

#### Scenario: Item com nível válido
- **WHEN** se grava um item com nível 23 (dentro da faixa 1–23)
- **THEN** a gravação é aceita

#### Scenario: Item com nível inválido
- **WHEN** se tenta gravar um item com nível 24
- **THEN** a gravação é rejeitada pela restrição de verificação

#### Scenario: Canteiro com posição inválida
- **WHEN** se tenta gravar um canteiro na posição 25
- **THEN** a gravação é rejeitada pela restrição de verificação

## ADDED Requirements

### Requirement: Migração V5 para níveis estendidos
A migração `V5__niveis_estendidos.sql` SHALL substituir as restrições de verificação `ck_jogo_predios_nivel` (nível 0–100), `ck_jogo_canteiros_posicao` (posição 1–24) e `ck_jogo_itens_nivel` (nível 1–23), mantendo os nomes das restrições. As migrações já versionadas (`V1` a `V4`) MUST NOT ser editadas; em particular, `V3__jogo.sql` MUST manter as faixas originais (0–5, 1–5, 1–5) que a V5 substitui.

#### Scenario: Banco migrado até a V4 recebe a V5
- **WHEN** a V5 é aplicada em um banco migrado até a V4
- **THEN** a migração conclui com sucesso
- **AND** um prédio com nível 100, um canteiro na posição 24 e um item com nível 23 passam a ser aceitos

#### Scenario: Banco novo
- **WHEN** as migrações V1 a V5 são aplicadas em um banco vazio
- **THEN** a validação do esquema pelo Hibernate (`ddl-auto=validate`) passa e as restrições têm as faixas novas
