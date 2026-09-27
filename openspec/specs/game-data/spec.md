# game-data Specification

## Purpose

Define o modelo persistido do jogo — vilas, prédios, canteiros, recursos, itens, unidades, ordens e batalhas — com as tabelas estruturadas no banco e validações de integridade nos níveis, quantidades e fila de operações.

## Requirements

### Requirement: Tabelas do jogo
O banco de dados SHALL ter as tabelas `jogo_vilas`, `jogo_predios`, `jogo_canteiros`, `jogo_sementes`, `jogo_itens`, `jogo_unidades`, `jogo_ordens` e `jogo_batalhas`, cada uma com as colunas especificadas em B.2 do plano. A aplicação MUST criar essas tabelas por migração versionada (`V3__jogo.sql`) na inicialização e MUST falhar se o esquema não corresponder ao mapeamento das entidades.

#### Scenario: Aplicação iniciando com banco vazio
- **WHEN** a aplicação inicia apontando para um banco sem as tabelas do jogo
- **THEN** a migração V3 é aplicada automaticamente
- **AND** todas as 8 tabelas são criadas com as colunas corretas
- **AND** a aplicação conclui a inicialização com sucesso

#### Scenario: Reinício com banco já migrado
- **WHEN** a aplicação é reiniciada sobre um banco já migrado com V3 aplicada
- **THEN** nenhuma migração é reaplicada
- **AND** os dados existentes são preservados

### Requirement: Uma vila por usuário
A tabela `jogo_vilas` SHALL ter uma restrição de unicidade na coluna `usuario_id`, garantindo que cada usuário tenha exatamente uma vila.

#### Scenario: Segunda vila para o mesmo usuário
- **WHEN** já existe uma vila criada para o usuário A e se tenta gravar outra vila para o mesmo usuário
- **THEN** a gravação é rejeitada por violar a restrição de unicidade

### Requirement: Integridade dos níveis e quantidades
O banco SHALL validar que os níveis dos prédios estejam entre 0 e 5, as posições dos canteiros entre 1 e 5, as quantidades de sementes sejam não-negativas, e os níveis dos itens entre 1 e 5. Toda tentativa de gravar valores fora dessas faixas MUST ser rejeitada.

#### Scenario: Prédio com nível inválido
- **WHEN** se tenta gravar um prédio com nível 6 (acima do máximo)
- **THEN** a gravação é rejeitada pela restrição de verificação

#### Scenario: Semente com quantidade negativa
- **WHEN** se tenta gravar um registro de semente com quantidade −1
- **THEN** a gravação é rejeitada pela restrição de verificação

#### Scenario: Item com nível válido
- **WHEN** se grava um item com nível 3 (dentro da faixa 1–5)
- **THEN** a gravação é aceita

### Requirement: Fila de uma ordem por categoria
A tabela `jogo_ordens` SHALL ter uma restrição de unicidade no par (`vila_id`, `categoria`), garantindo que exista no máximo uma ordem por categoria (CONSTRUCAO, FORJA, TREINO) por vila.

#### Scenario: Segunda ordem de construção na mesma vila
- **WHEN** já existe uma ordem CONSTRUCAO na vila e se tenta gravar outra ordem CONSTRUCAO na mesma vila
- **THEN** a gravação é rejeitada pela restrição de unicidade

#### Scenario: Ordem de forja enquanto há construção
- **WHEN** já existe uma ordem CONSTRUCAO na vila e se grava uma ordem FORJA
- **THEN** a gravação é aceita (categorias diferentes)

### Requirement: Uma batalha em andamento por vila
A tabela `jogo_batalhas` SHALL ter um índice parcial único na coluna `vila_id` com a condição `status = 'EM_ANDAMENTO'`, garantindo que exista no máximo uma batalha em andamento por vila a qualquer momento.

#### Scenario: Segunda batalha em andamento na mesma vila
- **WHEN** já existe uma batalha com `status = 'EM_ANDAMENTO'` na vila e se tenta criar outra batalha
- **THEN** a gravação é rejeitada pelo índice parcial único

#### Scenario: Nova batalha quando a anterior foi finalizada
- **WHEN** existia uma batalha com `status = 'EM_ANDAMENTO'` que foi finalizada (agora `status = 'VITORIA'` ou `'DERROTA'`) e se tenta criar nova batalha
- **THEN** a gravação é aceita (não há batalha EM_ANDAMENTO)

### Requirement: Auditoria nas tabelas do jogo
Todas as tabelas (`jogo_vilas`, `jogo_predios`, `jogo_canteiros`, `jogo_sementes`, `jogo_itens`, `jogo_unidades`, `jogo_ordens`, `jogo_batalhas`) SHALL ter as colunas `criado_em`, `criado_por`, `alterado_em` e `alterado_por`. Na inclusão, `criado_em` e `criado_por` MUST ser preenchidos automaticamente; em toda inclusão e alteração, `alterado_em` e `alterado_por` MUST ser preenchidos automaticamente. `criado_por` e `alterado_por` MUST conter o e-mail do usuário autenticado ou `sistema` quando não há usuário autenticado.

#### Scenario: Inclusão pela aplicação como usuário autenticado
- **WHEN** um usuário autenticado com e-mail `ana@exemplo.com` cria uma vila pela API
- **THEN** o registro é criado com `criado_por = 'ana@exemplo.com'` e `alterado_por = 'ana@exemplo.com'`
- **AND** `criado_em` e `alterado_em` têm a data e hora da criação

#### Scenario: Alteração preserva valores originais de criação
- **WHEN** o usuário `ana@exemplo.com` altera um prédio que foi criado anteriormente por `sistema`
- **THEN** `criado_por` e `criado_em` permanecem com os valores originais
- **AND** `alterado_por` passa a ser `ana@exemplo.com` e `alterado_em` a data e hora da alteração
