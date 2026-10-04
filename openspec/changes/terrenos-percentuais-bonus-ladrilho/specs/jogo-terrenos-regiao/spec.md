# Spec Delta

## Purpose

Define os percentuais de tipos de terreno de cada região, a geração e a persistência dos ladrilhos com terreno e bônus, a exposição da composição de terrenos na API, o fim da soma de bônus da vila e a posição das casas iniciais.

## ADDED Requirements

### Requirement: Percentuais de terreno por região
Cada região do mapa SHALL ter exatamente os 3 tipos de terreno do seu tipo de região (Floresta: Floresta, Barreiro, Plantações; Planície: Plantações, Criações, Floresta; Urbana: Indústria, Comércio, Desenvolvimento; Litoral: Salinas, Enxofre, Militar; Montanha: Rocha, Ferro, Carvão), em ordem sorteada (posições 1, 2 e 3), com percentuais sorteados pela semente: b1 inteiro de 20 a 60, b2 inteiro de 20 a (90 − b1) e b3 = 100 − (b1 + b2). A soma MUST ser sempre 100 e b3 MUST ser sempre ≥ 10. A mesma semente MUST gerar os mesmos terrenos, ordens e percentuais.

#### Scenario: Região Floresta
- **WHEN** a geração sorteia para uma região Floresta a ordem Floresta, Plantações, Barreiro com b1 = 40 e b2 = 35
- **THEN** a região tem Floresta 40% (posição 1), Plantações 35% (posição 2) e Barreiro 25% (posição 3)

#### Scenario: Limites ao longo de muitas sementes
- **WHEN** o mapa é gerado para as sementes 0 a 999
- **THEN** em toda região b1 está entre 20 e 60, b2 entre 20 e 90 − b1, b3 = 100 − b1 − b2 ≥ 10, os 3 terrenos são distintos e pertencem ao tipo da região, e b1 assume os valores 20 e 60 e b3 o valor 10 em alguma região

#### Scenario: Determinismo
- **WHEN** o mapa é gerado duas vezes com a mesma semente
- **THEN** as 16 regiões têm os mesmos tipos, terrenos, posições e percentuais

### Requirement: Terreno dos ladrilhos pelo percentual
Os 100 ladrilhos (10×10) de uma região SHALL ter apenas os 3 terrenos da região, cada um na quantidade exata do seu percentual, em posições embaralhadas de forma determinística pela semente da vila e pelo índice da região. Isso MUST valer para todos os tipos de região, inclusive Urbana. Jazidas MUST NOT existir.

#### Scenario: Quantidades pelo percentual
- **WHEN** uma região Floresta tem Floresta 40%, Plantações 35% e Barreiro 25%
- **THEN** seus ladrilhos são 40 Floresta, 35 Plantações e 25 Barreiro

#### Scenario: Urbana com terrenos
- **WHEN** uma região Urbana possuída tem Indústria 45%, Comércio 30% e Desenvolvimento 25%
- **THEN** ela tem 100 ladrilhos: 45 Indústria, 30 Comércio e 25 Desenvolvimento

#### Scenario: Mesma semente e região
- **WHEN** os ladrilhos da região 6 são gerados duas vezes com a mesma semente e os mesmos percentuais
- **THEN** o terreno e o bônus base de cada (x, y) são iguais nas duas gerações

### Requirement: Bônus do ladrilho
Cada ladrilho SHALL ter `bonus_base` inteiro sorteado de 0 a 100, `bonus_adjacente` igual a 25 vezes o número de vizinhos ortogonais (acima, abaixo, esquerda, direita), dentro do 10×10, com o mesmo terreno (0 a 100), e `bonus_total` = `bonus_base` + `bonus_adjacente` (0 a 200).

#### Scenario: Dois vizinhos iguais
- **WHEN** o ladrilho (C,5) (x = 2, y = 4) é Floresta com `bonus_base` 30, os vizinhos (2,3) e (1,4) são Floresta e os vizinhos (3,4) e (2,5) não são
- **THEN** `bonus_adjacente` é 50 e `bonus_total` é 80

#### Scenario: Canto da região
- **WHEN** o ladrilho (A,1) (x = 0, y = 0) tem os dois vizinhos (1,0) e (0,1) do mesmo terreno
- **THEN** `bonus_adjacente` é 50 (vizinhos fora do 10×10 não contam)

#### Scenario: Faixas
- **WHEN** os ladrilhos de qualquer região são gerados
- **THEN** todo `bonus_base` está entre 0 e 100 e todo `bonus_adjacente` é 0, 25, 50, 75 ou 100

### Requirement: Persistência de terrenos e ladrilhos
O sistema SHALL gravar os terrenos de cada uma das 16 regiões da vila em `regiao_terreno` (terreno, posição, percentual) na criação da vila e os 100 ladrilhos (x, y, terreno, `bonus_base`, `bonus_adjacente`) em `ladrilho` quando a região passa a ser possuída (criação e anexação). As tabelas `regiao_bonus` e `ladrilho_jazida` MUST deixar de existir (migration V18, banco limpo, sem conversão de dados).

#### Scenario: Vila criada
- **WHEN** uma vila é criada com 3 regiões
- **THEN** existem 48 linhas em `regiao_terreno` da vila (3 por região) e 300 linhas em `ladrilho` (100 por região possuída, inclusive a Urbana)

#### Scenario: Restrições do banco
- **WHEN** se tenta gravar um terreno na posição 1 com percentual 61, ou um ladrilho com `bonus_adjacente` 30, ou dois ladrilhos na mesma (região, x, y)
- **THEN** o banco rejeita a gravação

### Requirement: Composição de terrenos na API
A prévia (`POST/GET /api/jogo/vila/previa`), o resumo (`GET /api/jogo/vila`), o mapa (`GET /api/jogo/vila/mapa`), o detalhe da região (`GET /api/jogo/regioes/{indice}`) e a anexação (`POST /api/jogo/regioes/{indice}/anexar`) SHALL expor em cada região a lista `terrenos` com `{terreno, posicao, percentual}` ordenada por posição, no lugar de `bonus`. O detalhe de região possuída SHALL listar os 100 ladrilhos com `{x, y, terreno, bonusBase, bonusAdjacente, bonusTotal, construcao}`; região não possuída MUST devolver `possuida: false`, seus `terrenos` e `ladrilhos` vazio.

#### Scenario: Prévia
- **WHEN** o usuário gera uma prévia
- **THEN** cada uma das 16 regiões traz `terrenos` com 3 itens cuja soma de `percentual` é 100

#### Scenario: Detalhe com ladrilhos
- **WHEN** o usuário consulta uma região possuída
- **THEN** recebe 100 ladrilhos, cada um com `terreno`, `bonusBase`, `bonusAdjacente` e `bonusTotal = bonusBase + bonusAdjacente`, e a contagem de ladrilhos por terreno é igual ao `percentual` de cada terreno

#### Scenario: Região não possuída
- **WHEN** o usuário consulta uma região não possuída
- **THEN** recebe `possuida: false`, os 3 `terrenos` e `ladrilhos: []`

### Requirement: Sem soma de bônus da vila
O sistema MUST NOT calcular nem expor uma soma de bônus da vila: o campo `bonusRegiao` MUST NOT aparecer no resumo nem no mapa da vila.

#### Scenario: Mapa sem soma
- **WHEN** o usuário consulta `GET /api/jogo/vila/mapa`
- **THEN** `vila` traz apenas `id` e `nome`, sem `bonusRegiao`

#### Scenario: Resumo sem soma
- **WHEN** o usuário consulta `GET /api/jogo/vila`
- **THEN** a resposta não tem `bonusRegiao`

### Requirement: Ladrilhos na anexação
Ao anexar uma região, o sistema SHALL manter o tipo e os terrenos sorteados na geração do mapa e SHALL gerar e gravar os 100 ladrilhos da região se ainda não existirem, para qualquer tipo de região, inclusive Urbana.

#### Scenario: Anexar Urbana
- **WHEN** a vila anexa uma região Urbana vizinha
- **THEN** a região passa a ter 100 ladrilhos com os terrenos Indústria, Comércio e Desenvolvimento nas quantidades dos seus percentuais

#### Scenario: Mesmos terrenos da prévia
- **WHEN** a vila anexa a região 7, que na geração tinha Salinas 50%, Militar 30% e Enxofre 20%
- **THEN** a resposta da anexação traz esses mesmos 3 terrenos e percentuais

### Requirement: Casas iniciais em ladrilhos Desenvolvimento
Na criação da vila, as 4 casas N1 iniciais SHALL ser construídas na 1ª região Urbana da ordem de seleção, nos 4 primeiros ladrilhos de terreno Desenvolvimento em ordem de varredura (y = 0..9 e, dentro da linha, x = 0..9). As posições fixas antigas MUST NOT ser usadas.

#### Scenario: Primeiros ladrilhos De
- **WHEN** os ladrilhos Desenvolvimento da Urbana inicial, em ordem de varredura, começam por (3,0), (7,0), (1,1), (4,1), (8,2)
- **THEN** as 4 casas ficam em (3,0), (7,0), (1,1) e (4,1)

#### Scenario: Sempre há lugar
- **WHEN** a Urbana inicial tem Desenvolvimento com o percentual mínimo (10%)
- **THEN** as 4 casas são criadas em ladrilhos Desenvolvimento