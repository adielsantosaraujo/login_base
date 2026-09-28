## MODIFIED Requirements

### Requirement: Recursos e capacidade
O sistema SHALL manter quatro recursos (COMIDA, MADEIRA, PEDRA, FERRO) armazenados em milésimos. A capacidade de cada recurso SHALL ser determinada pelo nível N do Armazém: `500 × 2^(N−1)` unidades para N ≤ 5 e `round_half_up(8000 × (N/5)^p)` unidades para N > 5, onde `p` é o expoente configurado (ver Requirement: Expoente da curva de níveis configurável); cada unidade vale 1000 milésimos. Os valores dos níveis 1–5 MUST permanecer inalterados.

#### Scenario: Armazém nível 2
- **WHEN** a vila tem Armazém nível 2
- **THEN** a capacidade é 1000 unidades por recurso (1.000.000 milésimos)

#### Scenario: Capacidade padrão
- **WHEN** a vila é criada com Armazém nível 1
- **THEN** a capacidade é 500 unidades por recurso

#### Scenario: Armazém nível 6 com expoente padrão
- **WHEN** a vila tem Armazém nível 6 e o expoente é 1,5
- **THEN** a capacidade é 10516 unidades por recurso (round_half_up(8000 × 1,2^1,5))

#### Scenario: Armazém nível 100 com expoente padrão
- **WHEN** a vila tem Armazém nível 100 e o expoente é 1,5
- **THEN** a capacidade é 715542 unidades por recurso (round_half_up(8000 × 20^1,5))

### Requirement: Produção em tempo real calculada sob demanda
O sistema SHALL calcular a produção acumulada desde a última sincronização, sem limite de tempo. Quando consultada, a vila SHALL ter seus recursos atualizados proporcionalmente ao tempo decorrido e às produções/h atuais. O cálculo MUST NOT sofrer overflow silencioso: se o ganho de um intervalo não couber em um inteiro de 64 bits, o recurso MUST ser saturado na capacidade (nunca ficar negativo nem acima da capacidade).

#### Scenario: Produção acumulada após 2 horas
- **WHEN** a serraria está nível 1 (30 madeira/h) com velocidade 1 e passaram 2 horas sem requisições
- **THEN** a próxima consulta mostra +60 de madeira (30/h × 2 h)

#### Scenario: Produção respeitando a capacidade
- **WHEN** os recursos estão no máximo (500 unidades) e continua havendo produção
- **THEN** o recurso permanece no máximo, nenhum ganho além da capacidade

#### Scenario: Mudança de taxa no meio do intervalo
- **WHEN** a serraria é melhorada de nível 1 para 2 no meio de um período de 2 horas (1 h antes, 1 h depois)
- **THEN** o cálculo total de madeira inclui 30/h da primeira hora (taxa antiga) e 60/h da segunda hora (taxa nova)

#### Scenario: Intervalo gigantesco satura na capacidade
- **WHEN** a taxa de madeira é 3000/h, a velocidade é 1.000.000 e passaram 100 anos desde a última sincronização
- **THEN** a madeira fica exatamente na capacidade
- **AND** nenhum erro de overflow ocorre e o valor não fica negativo

### Requirement: Conclusão de ordens sob demanda
O sistema SHALL processar ordens cuja conclusão (`concluiEm`) seja no passado ou no presente. Ordens vencidas MUST ser aplicadas em ordem cronológica (por `concluiEm`, depois por `id`) no início de toda leitura ou comando.

#### Scenario: Construção concluída há 10 minutos
- **WHEN** uma ordem CONSTRUCAO tem `concluiEm` 10 minutos no passado e o jogador consulta a vila
- **THEN** a ordem é removida do banco
- **AND** o prédio está no nível alvo
- **AND** se era uma melhoria da fazenda que aumenta o número de canteiros, um novo canteiro TRIGO foi adicionado

#### Scenario: Ordem ainda não vencida
- **WHEN** uma ordem tem `concluiEm` 5 minutos no futuro e o jogador consulta a vila
- **THEN** a ordem permanece no banco
- **AND** o prédio/item/unidade não foi criado/modificado

### Requirement: Consulta do estado da vila
A endpoint `GET /api/jogo/vila` SHALL retornar um objeto `VilaDto` contendo todas as informações necessárias para que o frontend exiba o estado completo: hora atual do servidor, recursos inteiros (milésimos ÷ 1000), capacidade, produção por hora, prédios, ordens, canteiros, sementes, itens, unidades, capacidade do exército, nível máximo de item forjável (`nivelMaximoForjavel`, 0 quando a FORJA está no nível 0), nível liberado da masmorra e ID da batalha ativa (se houver).

#### Scenario: Resposta com status 200
- **WHEN** um usuário autenticado chama `GET /api/jogo/vila`
- **THEN** a resposta é 200 OK
- **AND** o corpo contém todos os campos do `VilaDto`

#### Scenario: Nível máximo forjável na resposta
- **WHEN** a vila tem FORJA nível 15 e o usuário chama `GET /api/jogo/vila`
- **THEN** o campo `nivelMaximoForjavel` vale 11

#### Scenario: Forja não construída
- **WHEN** a vila tem FORJA nível 0 e o usuário chama `GET /api/jogo/vila`
- **THEN** o campo `nivelMaximoForjavel` vale 0

### Requirement: Catálogo de regras
A endpoint `GET /api/jogo/catalogo` SHALL retornar um objeto `CatalogoDto` contendo as regras estáticas do jogo: custos e tempos de construção de cada nível 1–100 de cada prédio (já com a velocidade aplicada), cultivos e suas produções, modelos de itens com atributos derivados por nível, receitas de forja, tipos de tropas e atributos, inimigos com seus atributos fixos, e masmorras com mapa, obstáculos e composições. A montagem do catálogo MUST NOT falhar por overflow com qualquer expoente válido.

#### Scenario: Consulta do catálogo
- **WHEN** um usuário autenticado chama `GET /api/jogo/catalogo`
- **THEN** a resposta é 200 OK
- **AND** o corpo contém prédios, cultivos, modelos, receitas, tropas, inimigos e masmorras
- **AND** os custos já refletem a velocidade configurada

#### Scenario: Catálogo com os 100 níveis
- **WHEN** um usuário autenticado chama `GET /api/jogo/catalogo` com expoente 1,5 e velocidade 1
- **THEN** cada prédio tem 100 entradas de nível
- **AND** a entrada do nível 100 de CENTRO_VILA tem custo M67921 P67921 e tempo 38400 s

## ADDED Requirements

### Requirement: Expoente da curva de níveis configurável
O sistema SHALL ler o expoente `p` da curva de progressão acima do nível 5 da propriedade `app.jogo.expoente-curva` (variável de ambiente `JOGO_EXPOENTE_CURVA`), com padrão 1,5. O valor MUST estar entre 1,0 e 2,0 (inclusive) e ser múltiplo de 0,25. Um valor ausente do intervalo, que não seja múltiplo de 0,25 ou que faça algum custo de prédio ou capacidade de armazém até o nível 100 estourar um inteiro de 64 bits MUST impedir a inicialização da aplicação. O expoente afeta apenas custos de construção e capacidade do armazém acima do nível 5.

#### Scenario: Expoente padrão
- **WHEN** `JOGO_EXPOENTE_CURVA` não é definida
- **THEN** a aplicação inicia com expoente 1,5

#### Scenario: Expoente 2,0 aceito
- **WHEN** `JOGO_EXPOENTE_CURVA=2.0`
- **THEN** a aplicação inicia
- **AND** a capacidade do armazém nível 10 é 32000 e o custo do centro da vila nível 10 é M3038 P3038

#### Scenario: Expoente fora da faixa
- **WHEN** `JOGO_EXPOENTE_CURVA=2.5` ou `JOGO_EXPOENTE_CURVA=0.5`
- **THEN** a aplicação falha na inicialização

#### Scenario: Expoente que não é múltiplo de 0,25
- **WHEN** `JOGO_EXPOENTE_CURVA=1.3`
- **THEN** a aplicação falha na inicialização
