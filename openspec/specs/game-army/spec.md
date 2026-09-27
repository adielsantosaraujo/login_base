# game-army Specification

## Purpose

Define o recrutamento de tropas no quartel: tipos de unidades, armas e armaduras necessárias, treinamento com custo em comida e tempo, e limites de capacidade.

## Requirements

### Requirement: Tipos de tropa e atributos derivados
A aplicação SHALL suportar três tipos de unidades: `SOLDADO`, `ARQUEIRO` e `LANCEIRO`, cada uma com atributos fixos de HP, defesa base, movimento e consumo de comida por treino. Os atributos finais da unidade combinam estatísticas do tipo com as do item de arma (ataque e alcance) e da armadura (defesa adicional). As armas exigidas por tipo são `ESPADA` (Soldado), `ARCO` (Arqueiro) e `LANCA` (Lanceiro); armaduras podem ser de qualquer modelo.

#### Scenario: Atributos do Soldado
- **WHEN** um soldado é treinado com uma `ESPADA` nível 2 (ataque 8) e `ARMADURA_COURO` nível 1 (defesa 1)
- **THEN** a unidade tem ataque 8, alcance 1 (da espada), defesa 3 (base 1 + armadura 1), HP 30, movimento 3

#### Scenario: Atributos do Arqueiro
- **WHEN** um arqueiro é treinado com um `ARCO` nível 3 (ataque 8) e `ARMADURA_FERRO` nível 2 (defesa 4)
- **THEN** a unidade tem ataque 8, alcance 3 (do arco), defesa 4, HP 22, movimento 3

#### Scenario: Arma incorreta rejeitada
- **WHEN** se tenta treinar um soldado com um `ARCO`
- **THEN** a requisição retorna 422 `ITEM_INDISPONIVEL`

### Requirement: Treino consome arma e armadura
O treinamento de uma unidade é uma ordem que MUST debitar comida da vila, reservar uma arma e uma armadura com status `DISPONIVEL` e consumir tempo do quartel. Quando a ordem conclui, a unidade é criada com status `DISPONIVEL` e os itens mudam de status para `EQUIPADO`, ficando presos à unidade. Se a ordem falhar ou for cancelada, os itens retornam ao status anterior (implementação a cargo do executor).

#### Scenario: Débito de comida no início
- **WHEN** um usuário ordena treinar um `SOLDADO` (custo 50 comida, tempo 60 s) com 200 comida em estoque
- **THEN** a requisição é aceita, a comida passa para 150 imediatamente e a ordem começa

#### Scenario: Status dos itens
- **WHEN** a ordem conclui
- **THEN** a unidade surge com status `DISPONIVEL`, a arma e armadura têm status `EQUIPADO`

#### Scenario: Comida insuficiente
- **WHEN** um usuário com 30 comida tenta treinar (custo 50)
- **THEN** a requisição retorna 422 `RECURSOS_INSUFICIENTES` e nenhuma mudança ocorre

### Requirement: Validação dos itens
Armas e armaduras usados no treino MUST estar no inventário da vila, ter status `DISPONIVEL`, ser do tipo esperado (arma do modelo certo para o tipo de tropa) e não ser de outra vila.

#### Scenario: Item reservado não pode ser usado
- **WHEN** uma arma tem status `RESERVADO` (presa a outra ordem de treino)
- **THEN** ao tentar usá-la em um novo treino, a requisição retorna 422 `ITEM_INDISPONIVEL`

#### Scenario: Item equipado não pode ser usado
- **WHEN** uma arma tem status `EQUIPADO` (presa a uma unidade em batalha)
- **THEN** a requisição retorna 422 `ITEM_INDISPONIVEL`

#### Scenario: Item inexistente ou de outra vila
- **WHEN** o ID da arma não existe, ou pertence a outra vila, ou é nulo
- **THEN** a requisição retorna 422 `ITEM_INDISPONIVEL`

### Requirement: Tropas liberadas pelo nível do quartel
Cada tipo de tropa exige um nível mínimo do quartel para ser treinado: `SOLDADO` nível 1, `ARQUEIRO` nível 2, `LANCEIRO` nível 3. Tentar treinar uma tropa acima do nível do quartel MUST ser rejeitado.

#### Scenario: Soldado no quartel nível 1
- **WHEN** um quartel nível 1 tenta treinar um `SOLDADO`
- **THEN** o treino é aceito

#### Scenario: Arqueiro bloqueado no quartel nível 1
- **WHEN** um quartel nível 1 tenta treinar um `ARQUEIRO`
- **THEN** a requisição retorna 422 `REQUISITO_NAO_ATENDIDO`

#### Scenario: Lanceiro liberado no quartel nível 3
- **WHEN** um quartel nível 3 tenta treinar um `LANCEIRO`
- **THEN** o treino é aceito

### Requirement: Capacidade do exército
O quartel limita o número de unidades vivas em até `3 × nível do quartel`. O cálculo inclui unidades existentes mais uma ordem de treino em andamento (cada uma consome 1 slot de capacidade). Exceder a capacidade MUST ser rejeitado.

#### Scenario: Dentro da capacidade
- **WHEN** um quartel nível 2 tem 2 unidades e nenhuma ordem, e se treina uma 3ª
- **THEN** a requisição é aceita (2 + 1 = 3, capacidade 6)

#### Scenario: Acima da capacidade
- **WHEN** um quartel nível 1 tem 2 unidades e 1 ordem em andamento, e se tenta treinar outra
- **THEN** a requisição retorna 422 `CAPACIDADE_EXERCITO` (2 + 1 + 1 > 3)

#### Scenario: Aumento de capacidade ao subir nível
- **WHEN** um quartel sobe de nível 1 (cap 3) para nível 2 (cap 6) enquanto há 3 unidades
- **THEN** a capacidade aumenta para 6 e novos treinos tornam-se possíveis

### Requirement: Uma ordem de treino por vez
O quartel MUST manter no máximo uma ordem de treino ativa por vila. Tentar iniciar uma segunda ordem enquanto a primeira ainda está em andamento MUST ser rejeitado com 422 `FILA_OCUPADA`.

#### Scenario: Duas ordens simultâneas rejeitadas
- **WHEN** há uma ordem de treino ativa e o usuário tenta treinar outra unidade
- **THEN** a requisição retorna 422 `FILA_OCUPADA`

#### Scenario: Nova ordem após conclusão
- **WHEN** a ordem anterior conclui ou a unidade é obtida
- **THEN** uma nova ordem de treino é aceita
