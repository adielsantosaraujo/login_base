# Spec Delta

## Purpose

Define o terreno associado a cada prédio, as regiões em que cada prédio pode ser construído, o bônus de um prédio pelo seu ladrilho-âncora, o efeito desse bônus na produção, os bônus de grupo (Comércio, Desenvolvimento, Militar) pela média das âncoras e a marcação de ladrilhos pelo terreno.

## ADDED Requirements

### Requirement: Terreno associado ao prédio
Cada prédio SHALL ter um terreno associado conforme a tabela: Acampamento de lenhadores e Cabana de caça → Floresta; Barreiro → Barreiro; Pedreira → Rocha; Mina de ferro → Ferro; Mina de carvão → Carvão; Salina → Salinas; Mina de enxofre → Enxofre; Fazenda de plantio → Plantações; Fazenda de criação → Criações; Serraria, Olaria, Fundição, Tecelagem, Curtume e Cozinha → Indústria; Mercado e Estalagem → Comércio; Casa → Desenvolvimento; Quartel → Militar. Armazém, Ferraria, Alfaiataria e Carpintaria MUST NOT ter terreno. O catálogo (`GET /api/jogo/construcoes/catalogo`) SHALL expor esse valor no campo `terreno` (nulo quando não houver), no lugar de `bonusRegiao`.

#### Scenario: Catálogo do Quartel
- **WHEN** o cliente consulta o catálogo
- **THEN** o item QUARTEL tem `terreno` = `MILITAR` e o item ARMAZEM tem `terreno` = `null`

### Requirement: Regiões permitidas e Quartel no Litoral
Um prédio sem terreno SHALL ser permitido só em região Urbana; um prédio com terreno SHALL ser permitido nos tipos de região que têm esse terreno; o Quartel SHALL ser permitido na Urbana e no Litoral. Dentro de uma região permitida, o prédio MAY ser construído em ladrilho de qualquer terreno; fora do terreno certo ele só não recebe bônus.

#### Scenario: Quartel no Litoral
- **WHEN** o jogador constrói um Quartel numa região Litoral possuída, em ladrilho livre
- **THEN** a obra é criada

#### Scenario: Quartel na Planície
- **WHEN** o jogador tenta construir um Quartel numa região Planície
- **THEN** recebe erro 400 com a mensagem "Quartel só pode ser construído em região Urbana ou Litoral"

#### Scenario: Casa fora de Desenvolvimento
- **WHEN** o jogador constrói uma Casa na Urbana em ladrilho Indústria
- **THEN** a obra é criada e o bônus da Casa é 0

### Requirement: Bônus do prédio pela âncora
O bônus de um prédio SHALL ser o `bonus_total` do seu ladrilho-âncora (x, y atuais do prédio) quando o terreno da âncora é o terreno do prédio, e 0 caso contrário (inclusive para prédios sem terreno). O fator do prédio SHALL ser `1 + bônus ÷ 100`.

#### Scenario: Âncora no terreno certo
- **WHEN** um Acampamento de lenhadores tem âncora em ladrilho Floresta com `bonus_total` 80
- **THEN** o bônus do prédio é 80 e o fator é 1,80

#### Scenario: Âncora em terreno errado
- **WHEN** um Acampamento de lenhadores tem âncora em ladrilho Barreiro com `bonus_total` 70
- **THEN** o bônus do prédio é 0 e o fator é 1,00

### Requirement: Bônus da âncora na produção
A produção dos prédios de coleta, das fazendas e das fábricas SHALL ser multiplicada pelo fator do próprio prédio, aplicado depois da eficiência e do nível, com arredondamento em 2 casas. O evento de produção SHALL registrar o bônus aplicado em `bonusTerreno` quando for maior que 0.

#### Scenario: Madeira com âncora Floresta
- **WHEN** um Acampamento de lenhadores N1 ativo tem 2 trabalhadores de eficiência 1,0, ladrilhos marcados suficientes e âncora Floresta com `bonus_total` 80
- **THEN** o turno produz 18,00 Madeira

#### Scenario: Madeira sem bônus
- **WHEN** o mesmo acampamento tem a âncora em ladrilho Barreiro
- **THEN** o turno produz 10,00 Madeira

#### Scenario: Fábrica com âncora Indústria
- **WHEN** uma Serraria N1 tem 1 trabalhador de eficiência 1,0, há Madeira suficiente e a âncora é Indústria com `bonus_total` 50
- **THEN** a Serraria executa 4,50 ciclos

### Requirement: Bônus de grupo pela média das âncoras
Os bônus de Comércio, Desenvolvimento e Militar SHALL valer para a vila inteira e ser a média aritmética dos `bonus_total` das âncoras dos prédios do grupo — Mercado e Estalagem (Comércio), Casas (Desenvolvimento), Quartéis (Militar) — em estado ATIVA ou EM_UPGRADE cuja âncora está no terreno do grupo. Prédios fora do terreno certo MUST NOT entrar na média; sem nenhum prédio no terreno certo a média MUST ser 0. O fator SHALL ser `1 + média ÷ 100`. Comércio multiplica o Ouro do imposto e da Estalagem; Desenvolvimento multiplica a PO gerada nas obras e aprimoramentos; Militar multiplica a XP do treino no Quartel.

#### Scenario: Comércio no imposto
- **WHEN** a vila tem 16 adultos, um Mercado com âncora Comércio de `bonus_total` 45 e uma Estalagem com âncora Comércio de `bonus_total` 55
- **THEN** a média de Comércio é 50 e o imposto do turno credita 12,00 Ouro

#### Scenario: Prédio fora do terreno não entra
- **WHEN** o Mercado tem âncora em ladrilho Indústria e a Estalagem tem âncora Comércio de `bonus_total` 55
- **THEN** a média de Comércio é 55

#### Scenario: Desenvolvimento nas obras
- **WHEN** as Casas ativas da vila têm âncoras Desenvolvimento com `bonus_total` 20, 40, 60 e 80 e uma obra tem 1 Construtor de eficiência 1,0
- **THEN** a obra ganha 1,5 PO no turno

#### Scenario: Militar no treino
- **WHEN** a vila tem 2 Quartéis no Litoral com âncoras Militar de `bonus_total` 45 e 35, um Quartel na Urbana (terreno Indústria) e uma tropa aquartelada num Quartel N1
- **THEN** a média de Militar é 40 e cada membro saudável ganha 0,70 XP

#### Scenario: Sem prédio no terreno certo
- **WHEN** nenhum Quartel da vila tem âncora em ladrilho Militar
- **THEN** a média de Militar é 0 e a XP do treino não muda

### Requirement: Comércio não altera o Mercado
O bônus de Comércio MUST NOT alterar o preço de venda nem de compra do Mercado; o Mercado SHALL apenas contribuir com a sua âncora para a média de Comércio.

#### Scenario: Venda com Comércio alto
- **WHEN** a média de Comércio da vila é 80 e o jogador vende um recurso no Mercado
- **THEN** o Ouro recebido é o mesmo que com média 0

### Requirement: Marcação exige o terreno do prédio
Um prédio de coleta SHALL marcar apenas ladrilhos cujo terreno é o terreno do prédio (antes, a jazida). As demais regras de marcação (limite por nível, conexão ortogonal, ladrilho livre) continuam.

#### Scenario: Ladrilho do terreno certo
- **WHEN** uma Pedreira tenta marcar um ladrilho Rocha livre e conectado
- **THEN** a marcação é criada

#### Scenario: Ladrilho de outro terreno
- **WHEN** uma Pedreira tenta marcar um ladrilho Ferro
- **THEN** recebe erro 400 com a mensagem "Ladrilho sem o terreno do prédio (Rocha)"