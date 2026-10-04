# Spec Delta

## Purpose

Define a distribuição determinística de pontos dos 16 cidadãos iniciais a partir de um plano, a consulta e confirmação da população com validações de mínimos, o tratamento de vilas com população pendente no turno, e o fluxo de interface para ajuste e confirmação de população.

## ADDED Requirements

### Requirement: Distribuição automática pelo plano
O sistema SHALL distribuir os pontos dos 16 cidadãos iniciais de forma determinística a partir de um plano (quantidade de cidadãos por profissão principal, soma 16): os papéis são listados na ordem Comerciante, Construtor, Carregador, Madeireiro, Mineiro, Agricultor, Fazendeiro, Cozinheiro, Guerreiro, Ferreiro, Costureiro, Caçador e atribuídos em rodízio aos pais das 4 famílias, depois às mães, filhos e filhas; cada pessoa recebe 5 pontos na profissão principal, 3 na secundária e 2 na de apoio, e 20 pontos de característica pelo algoritmo de pesos. O backend (Java) e o frontend (TypeScript) MUST produzir o mesmo resultado que o algoritmo de referência para os planos da fixture de paridade. A profissão principal SHALL ser a de maior PE, com empate decidido pela ordem da tabela de profissões.

#### Scenario: Plano padrão
- **WHEN** o plano padrão é aplicado às 4 famílias
- **THEN** o pai da 1ª família fica Comerciante com VIT 1, FOR 1, VEL 5, INT 0, CAR 13 e Comerciante 5, Cozinheiro 3, Carregador 2

#### Scenario: Paridade com a referência
- **WHEN** cada plano da fixture é distribuído no Java e no TypeScript
- **THEN** os pontos de cada cidadão são iguais aos da fixture gerada pelo algoritmo de referência

#### Scenario: Determinismo
- **WHEN** o mesmo plano é distribuído duas vezes
- **THEN** os resultados são idênticos

### Requirement: Consulta da população inicial
`GET /api/jogo/vila/populacao` SHALL devolver o plano padrão, os mínimos (Construtor 2, Carregador 2), os limites (20 de característica e 10 de profissão por cidadão), a família líder sugerida (`familiaLiderSugeridaId`, a do líder com mais CAR) e as 4 famílias ordenadas, com os cidadãos na ordem pai, mãe, filho, filha e o papel de cada um. Enquanto a população não estiver confirmada, os pontos MUST ser a distribuição sugerida pelo plano padrão, calculada sem persistir; depois de confirmada, MUST ser os valores gravados.

#### Scenario: População pendente
- **WHEN** o usuário com vila recém-criada consulta a população
- **THEN** recebe `populacaoConfirmada` false, os 16 cidadãos com a distribuição do plano padrão e `familiaLiderSugeridaId` da 1ª família

#### Scenario: Consulta repetida
- **WHEN** a população pendente é consultada duas vezes
- **THEN** as duas respostas são idênticas

### Requirement: Confirmação da população
`POST /api/jogo/vila/populacao` SHALL receber `familiaLiderId` e as famílias com os 16 cidadãos e seus valores absolutos de características e profissões, gravar os pontos, deixar como pendentes `20 − Σ características` e `10 − Σ profissões`, definir a família líder, marcar a população como confirmada e responder `bonusLider` = `min(10, floor(CAR ÷ 2))` do líder (adulto mais velho; empate pai, mãe, filho, filha) e `proximaEtapa` `MAPA`. O sistema MUST NOT impor máximo por característica ou profissão além dos totais e MUST recusar: população já confirmada (409 `POPULACAO_JA_CONFIRMADA`), cidadãos faltando, repetidos ou de outra vila (400 `POPULACAO_INCOMPLETA`), valores negativos ou chaves desconhecidas (400 `PONTOS_INVALIDOS`), mais de 20 pontos de característica (400 `LIMITE_CARACTERISTICAS`) ou de 10 de profissão (400 `LIMITE_PROFISSOES`) num cidadão, família líder ausente ou de outra vila (400 `FAMILIA_LIDER_OBRIGATORIA`) e menos de 2 Construtores (400 `MINIMO_CONSTRUTORES`) ou 2 Carregadores (400 `MINIMO_CARREGADORES`) principais. Pontos pendentes MUST NOT bloquear a confirmação.

#### Scenario: Atributo concentrado aceito
- **WHEN** um cidadão é enviado com VIT 20 e Construtor 10
- **THEN** a confirmação é aceita

#### Scenario: Limite total excedido
- **WHEN** um cidadão é enviado com 21 pontos de característica
- **THEN** recebe 400 com `codigo` `LIMITE_CARACTERISTICAS` e nada é gravado

#### Scenario: Mínimo de Construtores
- **WHEN** só 1 cidadão tem Construtor como profissão principal
- **THEN** recebe 400 com `codigo` `MINIMO_CONSTRUTORES`

#### Scenario: Confirmação com pendências
- **WHEN** os mínimos são atendidos, um cidadão usa 15 de 20 pontos de característica e o líder escolhido tem CAR 13
- **THEN** recebe 200 com `bonusLider` 6, o cidadão fica com 5 pontos de característica pendentes e a população fica confirmada

#### Scenario: Já confirmada
- **WHEN** a população já confirmada é enviada de novo
- **THEN** recebe 409 com `codigo` `POPULACAO_JA_CONFIRMADA`

### Requirement: Vila com população pendente fora do turno
O processamento de turno SHALL ignorar vilas cuja população ainda não foi confirmada, sem alterar cidadãos, estoque ou construções dessas vilas.

#### Scenario: Vila pendente não é processada
- **WHEN** um turno é processado e existe uma vila com população não confirmada
- **THEN** essa vila não tem eventos nem mudanças no turno, e as vilas confirmadas são processadas normalmente

### Requirement: Tela Distribuir a população
A tela `/app/jogo/distribuir-populacao` SHALL carregar `GET /api/jogo/vila/populacao` e exibir as 4 famílias em abas (com "X pontos pendentes" ou "Todos os pontos usados" e o chip "LÍDER") e os cidadãos da família ativa já com os pontos sugeridos. Cada cidadão SHALL ter seletor de profissão principal e steppers para 5 características e 12 profissões; o "+" MUST ficar desabilitado quando o total do cidadão atingir o limite (20 ou 10) e o "−" em 0, sem limite por atributo. Cada profissão SHALL mostrar o bônus R3 (`Σ floor(característica ÷ 5)` das características ligadas) com explicação no `title`. A tela MUST NOT chamar o servidor durante os ajustes.

#### Scenario: Limite total
- **WHEN** um cidadão tem 20 pontos de característica
- **THEN** os "+" de característica desse cidadão ficam desabilitados e o contador mostra "20 / 20" em destaque

#### Scenario: Atributo acima de 10
- **WHEN** o usuário aumenta VIT de um cidadão que tem VIT 10 e 12 pontos de característica no total
- **THEN** VIT passa para 11

#### Scenario: Bônus R3
- **WHEN** um cidadão tem FOR 8 e VEL 7
- **THEN** Carregador mostra "+2" com o title "Bônus R3: FOR 8÷5 + VEL 7÷5"

### Requirement: Plano inicial e troca de profissão principal na tela
O painel "Plano inicial" SHALL listar as 12 profissões na ordem do plano com quantidade (stepper que não desce abaixo do mínimo nem passa o total 16) e "Atual" (cidadãos com a profissão como principal, em destaque de erro abaixo do mínimo e de aviso quando diferente do plano). Trocar a profissão principal de um cidadão SHALL refazer a distribuição dele pelo algoritmo e ajustar o plano (−1 na antiga, +1 na nova). "Redistribuir pelo plano" SHALL refazer os 16 cidadãos de forma determinística e MUST ficar desabilitado quando o plano não somar 16. "Zerar tudo" SHALL zerar os pontos sem mudar plano nem família líder.

#### Scenario: Troca de principal
- **WHEN** o plano tem Construtor 2 e Guerreiro 2 e o usuário troca Felipe de Construtor para Guerreiro
- **THEN** o plano fica Construtor 1 e Guerreiro 3, Felipe fica com Guerreiro 5 e "Construtores principais" mostra 1 / 2

#### Scenario: Plano diferente de 16
- **WHEN** a soma do plano é 15
- **THEN** o total mostra "15 / 16" em aviso e "Redistribuir pelo plano" fica desabilitado

### Requirement: Família líder e confirmação na tela
A tela SHALL pré-selecionar a família sugerida e listar as 4 famílias com o líder, o CAR e o bônus `+N%`. O botão "Confirmar população" SHALL ficar habilitado somente com ao menos 2 Construtores e 2 Carregadores principais e uma família líder escolhida, mostrando "Ajuste os mínimos" caso contrário; pontos pendentes aparecem como aviso e MUST NOT bloquear. Ao confirmar, a tela SHALL enviar os 16 cidadãos e `familiaLiderId` e navegar para `/app/jogo/mapa`.

#### Scenario: Mínimos não atendidos
- **WHEN** há só 1 Carregador principal
- **THEN** o botão mostra "Ajuste os mínimos" e fica desabilitado

#### Scenario: Confirmação com pendências na tela
- **WHEN** os mínimos estão atendidos, há 5 pontos pendentes e o usuário clica em "Confirmar população"
- **THEN** o checklist mostra "Pontos pendentes 5" como aviso, a requisição é enviada e a aplicação navega para `/app/jogo/mapa`
