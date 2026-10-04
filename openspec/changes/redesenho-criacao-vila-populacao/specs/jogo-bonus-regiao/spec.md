# Spec Delta

## Purpose

Define o cálculo e exposição de bônus de região na vila, o efeito dos bônus na produção, as restrições de construção por tipo de região, a exibição de tipo e bônus no mapa e na anexação de regiões.

## ADDED Requirements

### Requirement: Bônus de região da vila
O sistema SHALL calcular os bônus de região da vila como a soma, bônus a bônus, dos bônus de todas as regiões possuídas pela vila, com os 13 bônus sempre presentes (0 quando ausente), e MUST expô-los em `bonusRegiao` no resumo (`GET /api/jogo/vila`) e no mapa (`GET /api/jogo/vila/mapa`).

#### Scenario: Soma das regiões iniciais
- **WHEN** a vila possui as regiões 06 (Comércio 47, Indústria 30, Desenvolvimento 12), 07 (Salinas 38, Militar 20, Enxofre 6) e 10 (Criações 41, Floresta 25, Plantações 14)
- **THEN** `bonusRegiao` tem Comércio 47, Criações 41, Salinas 38, Indústria 30, Floresta 25, Militar 20, Plantações 14, Desenvolvimento 12, Enxofre 6 e os outros 4 bônus com 0

#### Scenario: Região anexada soma
- **WHEN** a vila anexa uma região Montanha com Ferro 44
- **THEN** o bônus Ferro da vila aumenta 44

### Requirement: Efeito dos bônus na produção
Cada ponto de bônus de região da vila SHALL aumentar em 1% a produção ligada a ele: Floresta (Acampamento de lenhadores e Cabana de caça), Barreiro (Barreiro), Plantações (Fazenda de plantio), Criações (Fazenda de criação), Rocha (Pedreira), Ferro (Mina de ferro), Carvão (Mina de carvão), Salinas (Salina), Enxofre (Mina de enxofre), Indústria (ciclos disponíveis das fábricas), Comércio (Ouro do imposto e da Estalagem), Desenvolvimento (PO gerada nas obras e upgrades) e Militar (XP de treino no Quartel). O fator MUST ser `1 + bônus ÷ 100`, aplicado depois da eficiência e do nível, com arredondamento em 2 casas; com bônus 0 a produção MUST ser igual à de antes.

#### Scenario: Madeira com bônus Floresta
- **WHEN** um Acampamento de lenhadores N1 ativo tem 2 trabalhadores de eficiência 1,0 e a vila tem Floresta 42
- **THEN** o turno produz 14,20 Madeira

#### Scenario: Sem bônus
- **WHEN** a vila tem Floresta 0
- **THEN** o mesmo acampamento produz 10 Madeira

#### Scenario: Indústria nas fábricas
- **WHEN** uma Serraria N1 tem 1 trabalhador de eficiência 1,0, há Madeira suficiente e a vila tem Indústria 30
- **THEN** a Serraria executa 3,90 ciclos

#### Scenario: Comércio no imposto
- **WHEN** a vila tem 16 adultos e Comércio 47
- **THEN** o imposto do turno credita 11,76 Ouro

#### Scenario: Desenvolvimento nas obras
- **WHEN** uma obra tem 1 Construtor de eficiência 1,0 e a vila tem Desenvolvimento 12
- **THEN** a obra acumula 1,12 PO no turno

#### Scenario: Militar no treino
- **WHEN** uma tropa está aquartelada num Quartel N1 com instrutor e a vila tem Militar 20
- **THEN** cada membro saudável recebe 0,60 XP

### Requirement: Construções permitidas pelo tipo da região
O sistema SHALL permitir construir um prédio com bônus associado apenas em região possuída cujo tipo tenha esse bônus (Fazenda de plantio: Floresta ou Planície; Fazenda de criação: Planície; Acampamento de lenhadores e Cabana de caça: Floresta ou Planície; Barreiro: Floresta; Pedreira, Mina de ferro e Mina de carvão: Montanha; Salina e Mina de enxofre: Litoral; fábricas: Urbana) e os demais prédios apenas em região Urbana. O catálogo (`GET /api/jogo/construcoes/catalogo`) MUST informar, para cada prédio, a lista de tipos permitidos (`regioes`) e o bônus associado.

#### Scenario: Mina de ferro em Montanha
- **WHEN** o jogador constrói uma Mina de ferro numa região Montanha possuída
- **THEN** a obra é criada

#### Scenario: Mina de ferro fora de Montanha
- **WHEN** o jogador constrói uma Mina de ferro numa região Floresta
- **THEN** recebe 400 com mensagem citando "Montanha"

#### Scenario: Fazenda de plantio em dois tipos
- **WHEN** o catálogo é consultado
- **THEN** a Fazenda de plantio tem `regioes` [FLORESTA, PLANICIE] e `bonusRegiao` PLANTACOES

### Requirement: Mapa e região exibem tipo e bônus
O mapa da vila SHALL mostrar o tipo e os 3 bônus de todas as 16 regiões, inclusive as não possuídas, e o detalhe da região MUST incluir o tipo e os bônus. Regiões não Urbanas sem ladrilhos gravados MUST exibir ladrilhos gerados pela semente.

#### Scenario: Região não possuída
- **WHEN** o jogador consulta o mapa
- **THEN** uma região não possuída aparece com tipo e bônus sorteados na criação

#### Scenario: Tela do mapa
- **WHEN** o jogador abre `/app/jogo/mapa`
- **THEN** cada região aparece com a cor e o nome do seu tipo e o total de bônus da vila é exibido

### Requirement: Anexação mantém tipo e bônus sorteados
A anexação de região SHALL manter o tipo e os bônus gravados na criação da vila, sem receber tipo no pedido, e MUST gerar os ladrilhos da região se ainda não existirem. A tela de anexação MUST NOT oferecer escolha de tipo e SHALL mostrar o tipo e os bônus da região.

#### Scenario: Anexar sem tipo
- **WHEN** o jogador anexa a região 3 com corpo vazio
- **THEN** a região 3 passa a ser possuída com o mesmo tipo e os mesmos bônus exibidos antes da anexação
