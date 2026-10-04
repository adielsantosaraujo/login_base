# Spec Delta

## Purpose

Define a geração determinística de mapas 4×4, a manutenção de prévias de mapa para o usuário, a criação de vila a partir de uma prévia, erros estruturados da API, e o fluxo de criação no frontend com validações de vizinhança e tipo de região.

## ADDED Requirements

### Requirement: Geração do mapa por semente
O sistema SHALL gerar, a partir de uma semente, uma grade 4×4 de 16 regiões em que cada um dos 5 tipos (FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA) aparece em 2 a 4 regiões, com soma 16 e no máximo 2 tipos com 4 regiões, distribuídos aleatoriamente nas posições. Depois dos tipos, cada região MUST receber os 3 bônus do seu tipo em ordem sorteada, com valores inteiros sorteados pela posição: 35 a 50 (1º), 16 a 34 (2º) e 5 a 15 (3º), limites inclusos. A mesma semente MUST gerar sempre o mesmo mapa.

#### Scenario: Quantidades válidas
- **WHEN** o mapa é gerado para qualquer semente
- **THEN** cada tipo aparece em 2 a 4 regiões, a soma é 16 e no máximo 2 tipos têm 4 regiões

#### Scenario: Bônus do tipo com faixa por posição
- **WHEN** uma região Montanha recebe a ordem Ferro, Carvão, Rocha
- **THEN** Ferro tem valor entre 35 e 50, Carvão entre 16 e 34 e Rocha entre 5 e 15

#### Scenario: Determinismo
- **WHEN** o mapa é gerado duas vezes com a mesma semente
- **THEN** os dois mapas têm os mesmos tipos, bônus e valores em todas as regiões

### Requirement: Prévia do mapa por usuário
O sistema SHALL manter uma única prévia vigente por usuário sem vila, guardada no servidor com semente, identificador (`previaId`) e número da rodada. `POST /api/jogo/vila/previa` MUST gerar uma nova prévia (substituindo a anterior e incrementando a rodada) e `GET /api/jogo/vila/previa` MUST devolver a vigente com as 16 regiões, cada uma com tipo e 3 bônus. Gerar um novo mapa é ilimitado.

#### Scenario: Gerar novo mapa
- **WHEN** o usuário com prévia na rodada 2 chama `POST /api/jogo/vila/previa`
- **THEN** recebe 200 com outro `previaId`, `rodada` 3 e 16 regiões

#### Scenario: Sem prévia
- **WHEN** o usuário sem prévia chama `GET /api/jogo/vila/previa`
- **THEN** recebe 404 com `codigo` `PREVIA_NAO_ENCONTRADA`

#### Scenario: Usuário que já tem vila
- **WHEN** o usuário com vila chama `POST /api/jogo/vila/previa`
- **THEN** recebe 409 com `codigo` `VILA_JA_EXISTE`

#### Scenario: Prévia exibida é a gravada
- **WHEN** o usuário chama `GET /api/jogo/vila/previa` duas vezes sem gerar outra
- **THEN** as duas respostas são idênticas

### Requirement: Criação da vila a partir da prévia
`POST /api/jogo/vila` SHALL receber `previaId` e `indices` (ordem de seleção) e criar a vila com a semente da prévia, gravando as 16 regiões com tipo e bônus, marcando as 3 escolhidas como possuídas, criando o estoque inicial, 4 casas N1 na 1ª região Urbana da ordem de seleção e 16 cidadãos sem pontos distribuídos, apagando a prévia e respondendo 201 com `vilaId` e `proximaEtapa` = `DISTRIBUIR_POPULACAO`. O sistema MUST validar, nesta ordem: vila existente (409 `VILA_JA_EXISTE`), prévia vigente (409 `PREVIA_EXPIRADA`), 3 índices distintos de 1 a 16 (400 `SELECAO_INVALIDA`), regiões formando um conjunto conexo por vizinhança ortogonal (400 `REGIAO_NAO_ADJACENTE`) e ao menos 1 Urbana (400 `SEM_REGIAO_URBANA`). O tipo de cada região MUST vir da prévia, nunca do pedido.

#### Scenario: Criação válida
- **WHEN** a prévia vigente tem a região 06 Urbana e o usuário envia `indices` [6, 7, 10]
- **THEN** recebe 201 com `proximaEtapa` `DISTRIBUIR_POPULACAO`, as regiões 6, 7 e 10 ficam possuídas com o tipo e os bônus da prévia, as 4 casas ficam na região 6 e a prévia deixa de existir

#### Scenario: Ordem de clique conexa aceita
- **WHEN** o usuário envia `indices` [1, 3, 2] e a região 1, 2 ou 3 é Urbana
- **THEN** a vila é criada, porque as 3 regiões formam um conjunto conexo

#### Scenario: Regiões não vizinhas
- **WHEN** o usuário envia `indices` [1, 3, 6]
- **THEN** recebe 400 com `codigo` `REGIAO_NAO_ADJACENTE`

#### Scenario: Sem região Urbana
- **WHEN** nenhuma das 3 regiões enviadas é Urbana
- **THEN** recebe 400 com `codigo` `SEM_REGIAO_URBANA` e a mensagem "Ao menos uma região deve ser Urbana"

#### Scenario: Seleção inválida
- **WHEN** o usuário envia 2 índices, índices repetidos ou o índice 17
- **THEN** recebe 400 com `codigo` `SELECAO_INVALIDA`

#### Scenario: Prévia expirada
- **WHEN** o usuário envia um `previaId` de uma prévia já substituída
- **THEN** recebe 409 com `codigo` `PREVIA_EXPIRADA` e nenhuma vila é criada

### Requirement: Erros da API do jogo com código
As respostas de erro da API do jogo SHALL ter o corpo `{ "erro": "<mensagem>", "codigo": "<CODIGO>" }`, com `codigo` presente sempre que a regra violada tiver código definido; erros sem código MUST manter o corpo `{ "erro": "<mensagem>" }`.

#### Scenario: Erro com código
- **WHEN** uma requisição viola uma regra com código, como `VILA_JA_EXISTE`
- **THEN** a resposta tem `erro` com a mensagem e `codigo` `VILA_JA_EXISTE`

#### Scenario: Erro sem código
- **WHEN** uma requisição viola uma regra antiga sem código
- **THEN** a resposta tem apenas `erro`

### Requirement: Mapa e seleção na tela Criar minha vila
A tela `/app/jogo/criar-vila` SHALL carregar a prévia com `GET /api/jogo/vila/previa` e, se receber 404, MUST gerar uma com `POST /api/jogo/vila/previa`. A grade 4×4 SHALL mostrar em cada região o número (01–16), o chip do tipo na cor do tipo e os 3 bônus em ordem decrescente de valor, com barra proporcional a `valor ÷ 50`. O usuário SHALL selecionar até 3 regiões na ordem de clique; a partir da segunda, só regiões ortogonalmente vizinhas de alguma selecionada ficam disponíveis e as demais MUST aparecer esmaecidas e ignorar o clique; clicar numa selecionada a remove. "Gerar novo mapa" SHALL chamar `POST /api/jogo/vila/previa`, mostrar carregamento, limpar a seleção e exibir "Mapa nº <rodada>".

#### Scenario: Abrir sem prévia
- **WHEN** `GET /api/jogo/vila/previa` responde 404
- **THEN** a tela chama `POST /api/jogo/vila/previa` e exibe as 16 regiões e "Mapa nº 1"

#### Scenario: Região não vizinha indisponível
- **WHEN** só a região 01 está selecionada
- **THEN** as regiões 02 e 05 estão disponíveis e a região 03 aparece esmaecida e não é selecionada ao clicar

#### Scenario: Gerar novo mapa
- **WHEN** o usuário com 2 regiões selecionadas clica em "Gerar novo mapa"
- **THEN** a seleção é limpa e o novo mapa é exibido com a rodada seguinte

### Requirement: Painel Sua seleção e criação pela tela
O painel SHALL mostrar 3 slots (vazio ou número e tipo), o checklist (3 regiões n/3, vizinhas entre si, ao menos 1 Urbana), a soma de cada um dos 13 bônus das regiões selecionadas (barra `soma ÷ 150`, esmaecido quando 0), o botão "Limpar" e o card "Em foco" com a região sob o ponteiro ou foco do teclado, ou a última selecionada. O botão SHALL mostrar "Criar vila" somente com seleção válida (3 regiões conexas e ao menos 1 Urbana) e, caso contrário, "Selecione 3 regiões válidas" desabilitado. Ao criar, a tela SHALL enviar `previaId` e `indices`, exibir carregamento e navegar para `/app/jogo/distribuir-populacao`; em erro MUST mostrar aviso com a mensagem do backend e, em `PREVIA_EXPIRADA`, recarregar a prévia e limpar a seleção.

#### Scenario: Soma dos bônus
- **WHEN** estão selecionadas as regiões 06 (Comércio 47, Indústria 30, Desenvolvimento 12), 07 (Salinas 38, Militar 20, Enxofre 6) e 10 (Criações 41, Floresta 25, Plantações 14)
- **THEN** o painel mostra Comércio 47, Criações 41, Salinas 38, Indústria 30, Floresta 25, Militar 20, Plantações 14, Desenvolvimento 12, Enxofre 6 e os demais bônus com 0 esmaecidos

#### Scenario: Seleção sem Urbana
- **WHEN** 3 regiões vizinhas sem nenhuma Urbana estão selecionadas
- **THEN** a dica mostra "Inclua ao menos uma região Urbana.", o item "Ao menos 1 Urbana" fica não cumprido e o botão mostra "Selecione 3 regiões válidas" desabilitado

#### Scenario: Criação com sucesso
- **WHEN** o usuário com seleção válida clica em "Criar vila" e o backend responde 201
- **THEN** a aplicação navega para `/app/jogo/distribuir-populacao`

#### Scenario: Prévia expirada na tela
- **WHEN** o backend responde 409 `PREVIA_EXPIRADA`
- **THEN** a tela mostra o aviso, recarrega a prévia vigente e limpa a seleção

### Requirement: Guardas de rota do início de jogo
A aplicação SHALL redirecionar o usuário sem vila para `/app/jogo/criar-vila`, o usuário com vila e população não confirmada para `/app/jogo/distribuir-populacao` e o usuário com população confirmada que acessa a criação para `/app/jogo/mapa`. O caminho `/app/jogo/populacao` SHALL redirecionar para `/app/jogo/distribuir-populacao`.

#### Scenario: População pendente
- **WHEN** o usuário com vila e população não confirmada acessa `/app/jogo/mapa`
- **THEN** é redirecionado para `/app/jogo/distribuir-populacao`

#### Scenario: Caminho antigo
- **WHEN** o usuário acessa `/app/jogo/populacao`
- **THEN** é redirecionado para `/app/jogo/distribuir-populacao`

#### Scenario: Vila pronta na criação
- **WHEN** o usuário com população confirmada acessa `/app/jogo/criar-vila`
- **THEN** é redirecionado para `/app/jogo/mapa`
