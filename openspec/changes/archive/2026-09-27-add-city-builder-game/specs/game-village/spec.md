# Spec Delta

## Purpose

Gerencia a vida útil, estado e consulta da vila do jogador, incluindo a criação automática, sincronização de produção em tempo real, conclusão de ordens, e acesso ao catálogo de regras.

## ADDED Requirements

### Requirement: Criação automática da vila
A vila do usuário SHALL ser criada automaticamente no primeiro acesso à API do jogo. Quando um usuário sem vila chama `GET /api/jogo/vila`, o sistema SHALL criar uma vila com os valores iniciais definidos em A.4 (nome, prédios, recursos, canteiro e produções).

#### Scenario: Primeiro acesso de um usuário novo
- **WHEN** um usuário autenticado sem vila chama `GET /api/jogo/vila`
- **THEN** uma vila é criada com nome "Vila de <nome do usuário>"
- **AND** os prédios, recursos e canteiros têm os valores iniciais de A.4
- **AND** a resposta 200 contém o estado da vila criada

#### Scenario: Segundo acesso do mesmo usuário
- **WHEN** o mesmo usuário chama `GET /api/jogo/vila` novamente
- **THEN** a mesma vila é retornada
- **AND** nenhuma nova vila é criada

### Requirement: Isolamento por usuário
Cada usuário MUST acessar apenas a sua própria vila. Uma consulta a uma batalha, ordem ou recurso de outro usuário MUST retornar 404 (não encontrado).

#### Scenario: Usuário tentando acessar batalha de outro
- **WHEN** o usuário A tenta consultar uma batalha criada pelo usuário B
- **THEN** a API retorna 404 `NAO_ENCONTRADO`

#### Scenario: Acesso à própria vila
- **WHEN** o usuário A consulta a sua própria vila
- **THEN** a API retorna 200 com o estado da vila

### Requirement: Recursos e capacidade
O sistema SHALL manter quatro recursos (COMIDA, MADEIRA, PEDRA, FERRO) armazenados em milésimos. A capacidade de cada recurso SHALL ser determinada pelo nível do Armazém: `500 × 2^(nível−1)` unidades (1000 milésimos).

#### Scenario: Armazém nível 2
- **WHEN** a vila tem Armazém nível 2
- **THEN** a capacidade é 1000 unidades por recurso (1.000.000 milésimos)

#### Scenario: Capacidade padrão
- **WHEN** a vila é criada com Armazém nível 1
- **THEN** a capacidade é 500 unidades por recurso

### Requirement: Produção em tempo real calculada sob demanda
O sistema SHALL calcular a produção acumulada desde a última sincronização, sem limite de tempo. Quando consultada, a vila SHALL ter seus recursos atualizados proporcionalmente ao tempo decorrido e às produções/h atuais.

#### Scenario: Produção acumulada após 2 horas
- **WHEN** a serraria está nível 1 (30 madeira/h) com velocidade 1 e passaram 2 horas sem requisições
- **THEN** a próxima consulta mostra +60 de madeira (30/h × 2 h)

#### Scenario: Produção respeitando a capacidade
- **WHEN** os recursos estão no máximo (500 unidades) e continua havendo produção
- **THEN** o recurso permanece no máximo, nenhum ganho além da capacidade

#### Scenario: Mudança de taxa no meio do intervalo
- **WHEN** a serraria é melhorada de nível 1 para 2 no meio de um período de 2 horas (1 h antes, 1 h depois)
- **THEN** o cálculo total de madeira inclui 30/h da primeira hora (taxa antiga) e 60/h da segunda hora (taxa nova)

### Requirement: Conclusão de ordens sob demanda
O sistema SHALL processar ordens cuja conclusão (`concluiEm`) seja no passado ou no presente. Ordens vencidas MUST ser aplicadas em ordem cronológica (por `concluiEm`, depois por `id`) no início de toda leitura ou comando.

#### Scenario: Construção concluída há 10 minutos
- **WHEN** uma ordem CONSTRUCAO tem `concluiEm` 10 minutos no passado e o jogador consulta a vila
- **THEN** a ordem é removida do banco
- **AND** o prédio está no nível alvo
- **AND** se era uma melhoria da fazenda, um novo canteiro TRIGO foi adicionado

#### Scenario: Ordem ainda não vencida
- **WHEN** uma ordem tem `concluiEm` 5 minutos no futuro e o jogador consulta a vila
- **THEN** a ordem permanece no banco
- **AND** o prédio/item/unidade não foi criado/modificado

### Requirement: Velocidade configurável
O sistema SHALL respeitar a propriedade `app.jogo.velocidade` (inteiro ≥ 1). As produções efetivas serão multiplicadas por esse valor; os tempos de conclusão serão divididos por esse valor.

#### Scenario: Velocidade 60
- **WHEN** `JOGO_VELOCIDADE=60`, a serraria está nível 1 e a vila é consultada
- **THEN** a serraria produz 1800 madeira/h (30/h × 60)
- **AND** um upgrade que normalmente leva 60 s leva 1 s (60 s ÷ 60)

#### Scenario: Velocidade padrão
- **WHEN** `JOGO_VELOCIDADE` não é definida (ou é 1)
- **THEN** as produções e tempos são os valores base

### Requirement: Consulta do estado da vila
A endpoint `GET /api/jogo/vila` SHALL retornar um objeto `VilaDto` contendo todas as informações necessárias para que o frontend exiba o estado completo: hora atual do servidor, recursos inteiros (milésimos ÷ 1000), capacidade, produção por hora, prédios, ordens, canteiros, sementes, itens, unidades, capacidade do exército, nível liberado da masmorra e ID da batalha ativa (se houver).

#### Scenario: Resposta com status 200
- **WHEN** um usuário autenticado chama `GET /api/jogo/vila`
- **THEN** a resposta é 200 OK
- **AND** o corpo contém todos os campos do `VilaDto`

### Requirement: Catálogo de regras
A endpoint `GET /api/jogo/catalogo` SHALL retornar um objeto `CatalogoDto` contendo as regras estáticas do jogo: custos e tempos de construção por nível (já com a velocidade aplicada), cultivos e suas produções, modelos de itens com atributos derivados por nível, receitas de forja, tipos de tropas e atributos, inimigos com seus atributos fixos, e masmorras com mapa, obstáculos e composições.

#### Scenario: Consulta do catálogo
- **WHEN** um usuário autenticado chama `GET /api/jogo/catalogo`
- **THEN** a resposta é 200 OK
- **AND** o corpo contém prédios, cultivos, modelos, receitas, tropas, inimigos e masmorras
- **AND** os custos já refletem a velocidade configurada

### Requirement: Operações serializadas por vila
O sistema SHALL aplicar lock pessimista por vila antes de toda leitura e comando, garantindo que duas requisições simultâneas sejam serializadas e não causem condições de corrida.

#### Scenario: Duas requisições de melhoria simultâneas
- **WHEN** dois clientes do mesmo usuário chamam a melhoria do mesmo prédio simultaneamente, e há recursos para apenas uma
- **THEN** uma operação é aceita
- **AND** a outra recebe um erro 422 `RECURSOS_INSUFICIENTES` e nenhum recurso é debitado

### Requirement: Erros de regra padronizados
Violações das regras do jogo MUST retornar HTTP 422 com um corpo `{codigo, mensagem}`. Erros de concorrência (conflito de versão, turno desatualizado) MUST retornar 409. Recursos não encontrados MUST retornar 404. Requisições malformadas MUST retornar 400.

#### Scenario: Recursos insuficientes
- **WHEN** um usuário tenta fazer uma melhoria sem recursos suficientes
- **THEN** a resposta é 422 `RECURSOS_INSUFICIENTES`
- **AND** o estado da vila não é alterado

#### Scenario: Requisição anônima à API
- **WHEN** um usuário não autenticado chama qualquer endpoint `/api/jogo/**`
- **THEN** a resposta é 401 `UNAUTHORIZED`
