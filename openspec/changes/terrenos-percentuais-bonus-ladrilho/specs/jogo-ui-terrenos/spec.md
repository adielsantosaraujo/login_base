# Spec Delta

## Purpose

Define como o frontend exibe os tipos de terreno: siglas e cores, grade de ladrilhos com endereço e tooltip, composição de terrenos na criação, no mapa e na anexação, total de ladrilhos por terreno no painel de seleção, marcação e seletor de construção pelo terreno.

## ADDED Requirements

### Requirement: Siglas e cores dos terrenos
Cada um dos 13 terrenos SHALL ter uma sigla própria de 2 letras — Fl (Floresta), Ba (Barreiro), Pl (Plantações), Cr (Criações), Ro (Rocha), Fe (Ferro), Ca (Carvão), Sa (Salinas), En (Enxofre), Mi (Militar), In (Indústria), Co (Comércio), De (Desenvolvimento) — e uma cor dada pelo token CSS `--vl-terreno-<sigla em minúsculas>`. Os tokens `--vl-bonus-*` e `--vl-jazida-*` MUST deixar de existir.

#### Scenario: Siglas únicas
- **WHEN** o domínio de terrenos é carregado
- **THEN** as 13 siglas são distintas e cada terreno tem a cor `var(--vl-terreno-<sigla>)`

### Requirement: Ladrilho com endereço e sigla
Nas grades de ladrilhos (mapa, região e marcação), cada ladrilho SHALL mostrar o endereço `(A,1)` — coluna A–J para x = 0..9 e linha 1–10 para y = 0..9 — acima da sigla do terreno, com o fundo na cor do terreno. Ladrilho com construção SHALL manter endereço e sigla visíveis e indicar o prédio.

#### Scenario: Endereço do primeiro e do último
- **WHEN** a grade de uma região possuída é exibida
- **THEN** o ladrilho x = 0, y = 0 mostra "(A,1)" e o ladrilho x = 9, y = 9 mostra "(J,10)", cada um acima da sigla do seu terreno

#### Scenario: Ladrilho com casa
- **WHEN** o ladrilho (D,1) tem uma Casa em terreno Desenvolvimento
- **THEN** ele mostra "(D,1)", a sigla "De" e o indicador do prédio

### Requirement: Tooltip do ladrilho
O tooltip e o rótulo acessível de cada ladrilho SHALL informar o endereço, o terreno (nome e sigla), `bonus_base`, `bonus_adjacente` e `bonus_total`, além dos dados da construção quando houver.

#### Scenario: Tooltip de ladrilho livre
- **WHEN** o jogador passa o mouse no ladrilho (C,5) Floresta com base 30 e adjacente 50
- **THEN** o tooltip é "(C,5) - Floresta (Fl) - base 30 - adjacente +50 - total 80"

### Requirement: Composição de terrenos na criação e no mapa
As regiões da tela "Criar minha vila", o mapa da vila, o detalhe da região e o diálogo de anexação SHALL mostrar a composição de terrenos da região em ordem decrescente de percentual (ex.: "Floresta 40% · Plantações 35% · Barreiro 25%"). O painel "Bônus total da vila" MUST NOT existir.

#### Scenario: Tile da criação
- **WHEN** a prévia mostra a região 06 Urbana com Indústria 45%, Comércio 30% e Desenvolvimento 25%
- **THEN** o tile lista os 3 terrenos com esses percentuais e seu rótulo acessível é "Região 06 · Urbana · Indústria 45% · Comércio 30% · Desenvolvimento 25%"

#### Scenario: Mapa sem bônus total
- **WHEN** o jogador abre o mapa da vila
- **THEN** cada região mostra seus 3 terrenos com percentual e não há o painel "Bônus total da vila"

### Requirement: Ladrilhos por terreno no painel de seleção
O painel "Sua seleção" da criação SHALL mostrar, no lugar da soma de bônus, o total de ladrilhos por terreno das regiões escolhidas (soma dos percentuais), só dos terrenos com total maior que 0, em ordem decrescente.

#### Scenario: Três regiões escolhidas
- **WHEN** o jogador escolhe uma Urbana (Comércio 47, Indústria 30, Desenvolvimento 23), um Litoral (Salinas 38, Militar 40, Enxofre 22) e uma Planície (Plantações 45, Criações 26, Floresta 29)
- **THEN** o painel lista Comércio 47, Plantações 45, Militar 40, Salinas 38, Indústria 30, Floresta 29, Criações 26, Desenvolvimento 23 e Enxofre 22 ladrilhos

#### Scenario: Nada escolhido
- **WHEN** nenhuma região está escolhida
- **THEN** o painel mostra "Selecione regiões para ver os ladrilhos"

### Requirement: Marcação pelo terreno no frontend
O painel de marcação SHALL destacar como candidatos apenas os ladrilhos livres, conectados e do terreno do prédio, e SHALL informar o terreno exigido.

#### Scenario: Pedreira
- **WHEN** o jogador abre a marcação de uma Pedreira
- **THEN** só ladrilhos Rocha livres e conectados aparecem como candidatos e a dica começa com "Terreno: Rocha (Ro)"

### Requirement: Terreno do prédio no seletor de construção
O seletor de construção SHALL mostrar o terreno do prédio escolhido ("Terreno: <nome> (<sigla>)") ou "Sem terreno (sem bônus)" quando o prédio não tem terreno.

#### Scenario: Pedreira no seletor
- **WHEN** o jogador escolhe Pedreira no seletor
- **THEN** o seletor mostra "Terreno: Rocha (Ro)"