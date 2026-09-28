# game-data Specification

## Purpose

Define o modelo persistido do jogo — vilas, prédios, canteiros, recursos, itens, unidades, ordens e batalhas — com as tabelas estruturadas no banco e validações de integridade nos níveis, quantidades e fila de operações.

## Requirements

### Requirement: Tabelas do jogo
O banco de dados SHALL ter as tabelas `jogo_vilas`, `jogo_predios`, `jogo_canteiros`, `jogo_sementes`, `jogo_itens`, `jogo_unidades`, `jogo_ordens`, `jogo_batalhas` e `jogo_contadores_nome`, cada uma com as colunas especificadas nas migrações versionadas. A aplicação MUST criar essas tabelas por migração versionada (`V3__jogo.sql` e `V4__unidade_nome_e_lote_treino.sql`) na inicialização e MUST falhar se o esquema não corresponder ao mapeamento das entidades.

**Principais mudanças em V4:**
- `jogo_unidades`: ADD `nome` varchar(60) NOT NULL, `sobrenome` varchar(60) NOT NULL, `ordinal_nome` int NOT NULL (≥ 1), única `(vila_id, nome, sobrenome, ordinal_nome)`
- `jogo_contadores_nome`: nova tabela (contador histórico de nomes por vila)
- `jogo_itens`: ADD `ordem_id` bigint nullable com FK para `jogo_ordens(id)` + índice `ix_jogo_itens_ordem`
- `jogo_ordens`: REMOVE `arma_item_id`, `armadura_item_id` (as FKs correspondentes); coluna `alvo` passa a guardar apenas o tipo de tropa (SOLDADO/ARQUEIRO/LANCEIRO); coluna `nivel` passa a guardar o nível da arma; coluna `quantidade` (existente) passa a guardar o tamanho do lote

#### Scenario: Aplicação iniciando com banco vazio
- **WHEN** a aplicação inicia apontando para um banco sem as tabelas do jogo
- **THEN** a migração V3 é aplicada automaticamente
- **AND** a migração V4 é aplicada automaticamente
- **AND** todas as 9 tabelas são criadas com as colunas corretas (incluindo nome/sobrenome/ordinal_nome em unidades, `jogo_contadores_nome`, ordem_id em itens, sem arma_item_id/armadura_item_id em ordens)
- **AND** a aplicação conclui a inicialização com sucesso

#### Scenario: Reinício com banco já migrado
- **WHEN** a aplicação é reiniciada sobre um banco já migrado com V3 e V4 aplicadas
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

### Requirement: Nome persistido da unidade
A tabela `jogo_unidades` SHALL ter as colunas `nome` (varchar(60)), `sobrenome` (varchar(60)) e `ordinal_nome` (int, `CHECK ordinal_nome >= 1`), todas NOT NULL, com restrição única `(vila_id, nome, sobrenome, ordinal_nome)`. Os valores são atribuídos no momento da criação da unidade (conclusão do treino) e não podem ser alterados durante a vida da unidade. A migração V4 SHALL preencher `nome`/`sobrenome` das unidades pré-existentes com algoritmo determinístico (10 primeiros nomes/sobrenomes do JSON, indexados por id) e `ordinal_nome` com a posição da unidade entre as unidades da mesma vila com o mesmo par, na ordem de id.

#### Scenario: Unidade nova tem nome persistido
- **WHEN** uma unidade é criada pela conclusão de um treino
- **THEN** as colunas `nome` e `sobrenome` são preenchidas com os valores sorteados (ex: "Ana" e "Silva")
- **AND** os valores permanecem imutáveis durante a vida da unidade

#### Scenario: Unidades pré-existentes recebem nome por migração
- **WHEN** a migração V4 é executada em um banco com unidades já existentes
- **THEN** cada unidade pré-existente recebe um par nome-sobrenome determinístico baseado em seu id
- **AND** cada unidade recebe `ordinal_nome` = sua posição (por id) entre as unidades da mesma vila com o mesmo par
- **AND** as colunas passam a NOT NULL sem erro

### Requirement: Contador histórico de nomes por vila
O banco SHALL ter a tabela `jogo_contadores_nome` com `id`, `vila_id` (FK para `jogo_vilas`), `nome` varchar(60), `sobrenome` varchar(60), `ultimo_ordinal` int (`CHECK >= 1`) e colunas de auditoria, com restrição única `(vila_id, nome, sobrenome)`. Cada linha guarda o maior ordinal já atribuído ao par na vila; o valor MUST ser incrementado a cada nova unidade com o par e MUST NOT ser decrementado quando unidades morrem (a exclusão da unidade não altera o contador). A migração V4 SHALL criar a tabela e semeá-la com a contagem das unidades existentes por `(vila_id, nome, sobrenome)`.

#### Scenario: Contador criado na primeira ocorrência
- **WHEN** uma unidade é criada com um par inédito na vila
- **THEN** uma linha é inserida com `ultimo_ordinal = 1`

#### Scenario: Contador preservado após morte
- **WHEN** a única unidade "Ana Silva" (ordinal 1) morre e é excluída
- **THEN** a linha do contador de "Ana Silva" permanece com `ultimo_ordinal = 1`

#### Scenario: Semeadura na migração
- **WHEN** a V4 roda sobre uma vila com 2 unidades que recebem o par "Ana Silva" no backfill
- **THEN** o contador de "Ana Silva" nessa vila é criado com `ultimo_ordinal = 2`

### Requirement: Itens reservados vinculados à ordem
A tabela `jogo_itens` SHALL ter uma coluna `ordem_id` (bigint, nullable) que referencia a FK para `jogo_ordens(id)`. Essa coluna MUST ser preenchida quando um item é reservado para uma ordem de treino em lote (status RESERVADO), permitindo rastrear quais itens pertencem a qual ordem durante a execução do lote. Um índice `ix_jogo_itens_ordem` MUST ser criado na coluna `ordem_id` para suportar buscas rápidas. Quando a ordem conclui e os itens são equipados (status EQUIPADO), a coluna `ordem_id` é limpa (setada a NULL), pois o item agora pertence à unidade em vez de à ordem.

#### Scenario: Item reservado liga à ordem
- **WHEN** um item é reservado para uma ordem de lote (status RESERVADO)
- **THEN** a coluna `ordem_id` é preenchida com o id da ordem
- **AND** a consulta `SELECT * FROM jogo_itens WHERE ordem_id = ?` retorna todos os itens daquela ordem

#### Scenario: Item equipado desliga da ordem
- **WHEN** a ordem conclui e o item passa a EQUIPADO
- **THEN** a coluna `ordem_id` é setada a NULL
- **AND** o item agora é rastreado pela coluna `unidade_id` (futuro) ou pelo atributo `armaItemId`/`armaduraItemId` da unidade

### Requirement: Migração V4 para treino em lote
A migração `V4__unidade_nome_e_lote_treino.sql` SHALL executar as seguintes transformações:
1. Adicionar colunas `nome` varchar(60) e `sobrenome` varchar(60) à tabela `jogo_unidades`; preencher com valores determinísticos (10 primeiros nomes/sobrenomes do JSON, indexados por `id`); aplicar `NOT NULL`.
2. Adicionar `ordinal_nome` int a `jogo_unidades`; preencher com `row_number() over (partition by vila_id, nome, sobrenome order by id)`; aplicar `NOT NULL`, `CHECK >= 1` e única `(vila_id, nome, sobrenome, ordinal_nome)`.
3. Criar `jogo_contadores_nome` e semear com `count(*)` por `(vila_id, nome, sobrenome)` (auditoria `sistema`).
4. Adicionar coluna `ordem_id` bigint nullable à tabela `jogo_itens`; criar FK para `jogo_ordens(id)`; criar índice `ix_jogo_itens_ordem`.
5. Para ordens TREINO em andamento, migrar os itens já alocados: `UPDATE jogo_itens i SET ordem_id = o.id FROM jogo_ordens o WHERE o.categoria='TREINO' AND i.id IN (o.arma_item_id, o.armadura_item_id)`.
6. Remover as colunas `arma_item_id` e `armadura_item_id` da tabela `jogo_ordens` e as FKs correspondentes.

#### Scenario: Banco com unidades pré-existentes é migrado
- **WHEN** a migração V4 é executada em um banco com unidades sem nome
- **THEN** as colunas `nome` e `sobrenome` são adicionadas e preenchidas
- **AND** `ordinal_nome` e `jogo_contadores_nome` refletem as repetições existentes por vila
- **AND** a migração conclui com sucesso

#### Scenario: Itens de ordens ativas são migrados
- **WHEN** há uma ordem TREINO em andamento com itens já alocados
- **THEN** a migração liga esses itens à ordem via `ordem_id`
- **AND** as colunas `arma_item_id`/`armadura_item_id` são removidas
- **AND** a ordem de treino segue em pé sem corrupção

### Requirement: Auditoria nas tabelas do jogo
Todas as tabelas (`jogo_vilas`, `jogo_predios`, `jogo_canteiros`, `jogo_sementes`, `jogo_itens`, `jogo_unidades`, `jogo_ordens`, `jogo_batalhas`, `jogo_contadores_nome`) SHALL ter as colunas `criado_em`, `criado_por`, `alterado_em` e `alterado_por`. Na inclusão, `criado_em` e `criado_por` MUST ser preenchidos automaticamente; em toda inclusão e alteração, `alterado_em` e `alterado_por` MUST ser preenchidos automaticamente. `criado_por` e `alterado_por` MUST conter o e-mail do usuário autenticado ou `sistema` quando não há usuário autenticado.

#### Scenario: Inclusão pela aplicação como usuário autenticado
- **WHEN** um usuário autenticado com e-mail `ana@exemplo.com` cria uma vila pela API
- **THEN** o registro é criado com `criado_por = 'ana@exemplo.com'` e `alterado_por = 'ana@exemplo.com'`
- **AND** `criado_em` e `alterado_em` têm a data e hora da criação

#### Scenario: Alteração preserva valores originais de criação
- **WHEN** o usuário `ana@exemplo.com` altera um prédio que foi criado anteriormente por `sistema`
- **THEN** `criado_por` e `criado_em` permanecem com os valores originais
- **AND** `alterado_por` passa a ser `ana@exemplo.com` e `alterado_em` a data e hora da alteração
