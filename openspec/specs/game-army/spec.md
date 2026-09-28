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
O treinamento de N unidades (lote) é uma ordem que MUST debitar comida da vila (comida_tipo × N), reservar N armas e N armaduras com status `DISPONIVEL` e consumir tempo do quartel. Quando a ordem conclui, as N unidades são criadas com status `DISPONIVEL` e os N+N itens mudam de status para `EQUIPADO`, ficando equipados nas respectivas unidades (trocáveis depois, ver Requirement: Troca de equipamento). Se a ordem falhar ou for cancelada, os itens retornam ao status anterior (implementação a cargo do executor).

#### Scenario: Débito de comida no início
- **WHEN** um usuário ordena treinar um lote de 3 `SOLDADO` (custo 50 comida cada = 150 total, tempo 60 s base) com 200 comida em estoque
- **THEN** a requisição é aceita, a comida passa para 50 imediatamente e a ordem começa

#### Scenario: Status dos itens
- **WHEN** a ordem de lote conclui
- **THEN** cada uma das N unidades surge com status `DISPONIVEL`
- **AND** os N+N itens (N armas + N armaduras) têm status `EQUIPADO`

#### Scenario: Comida insuficiente
- **WHEN** um usuário com 100 comida tenta treinar um lote de 3 Soldados (custo 150)
- **THEN** a requisição retorna 422 `RECURSOS_INSUFICIENTES` e nenhuma mudança ocorre

### Requirement: Validação dos itens
Armas e armaduras usados no treino MUST estar no inventário da vila, ter status `DISPONIVEL` em quantidade suficiente, ser do tipo esperado (arma do modelo certo para o tipo de tropa, armadura do modelo/nível escolhidos), ser da configuração modelo/nível (não IDs específicos), e não ser de outra vila. O servidor SELECT os N menores IDs de armas e N menores IDs de armaduras que satisfazem a configuração.

#### Scenario: Item reservado não pode ser usado
- **WHEN** uma arma tem status `RESERVADO` (presa a outra ordem de treino em lote)
- **THEN** ao tentar usá-la em um novo treino, a requisição retorna 422 `ITEM_INDISPONIVEL`

#### Scenario: Item equipado não pode ser usado
- **WHEN** uma arma tem status `EQUIPADO` (presa a uma unidade)
- **THEN** a requisição retorna 422 `ITEM_INDISPONIVEL`

#### Scenario: Item inexistente ou de outra vila
- **WHEN** a quantidade solicitada de armas/armaduras do modelo/nível exigido não existe, ou os itens pertencem a outra vila
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
O quartel limita o número de unidades vivas e em treino em até `3 × nível do quartel`. O cálculo inclui unidades existentes mais a soma das quantidades de todas as ordens de treino em andamento. Exceder a capacidade MUST ser rejeitado com 422 `CAPACIDADE_EXERCITO`.

#### Scenario: Dentro da capacidade
- **WHEN** um quartel nível 2 tem 2 unidades, nenhuma ordem, e se treina um lote de 2
- **THEN** a requisição é aceita (2 + 2 = 4 ≤ 6)

#### Scenario: Acima da capacidade
- **WHEN** um quartel nível 1 tem 1 unidade, 1 ordem de treino em andamento (quantidade 1), e se tenta treinar um lote de 2
- **THEN** a requisição retorna 422 `CAPACIDADE_EXERCITO` (1 + 1 + 2 > 3)

#### Scenario: Aumento de capacidade ao subir nível
- **WHEN** um quartel sobe de nível 1 (cap 3) para nível 2 (cap 6) com 3 unidades e nenhuma ordem
- **THEN** a capacidade aumenta para 6 e um lote de 3 é aceito (3 + 3 = 6)

### Requirement: Uma ordem de treino por vez
O quartel MUST manter no máximo uma ordem de treino ativa por vila. Tentar iniciar uma segunda ordem enquanto a primeira ainda está em andamento MUST ser rejeitado com 422 `FILA_OCUPADA`.

#### Scenario: Duas ordens simultâneas rejeitadas
- **WHEN** há uma ordem de treino ativa e o usuário tenta treinar outra unidade
- **THEN** a requisição retorna 422 `FILA_OCUPADA`

#### Scenario: Nova ordem após conclusão
- **WHEN** a ordem anterior conclui ou a unidade é obtida
- **THEN** uma nova ordem de treino é aceita

### Requirement: Nome e sobrenome da unidade
Toda unidade treinada SHALL receber um nome e um sobrenome, sorteados independentemente de listas de pessoas carregadas na inicialização. Os sorteios ocorrem no momento da conclusão do treino (quando a unidade é criada), e cada unidade do lote recebe seus próprios sorteios. As listas de nomes e sobrenomes estão em `src/main/resources/jogo/nomes/nome_pessoas.json` e `src/main/resources/jogo/nomes/sobrenome_pessoas.json`, cada uma contendo um array JSON de strings. Os sorteios MUST ser uniformes e independentes; o sorteio pode repetir um par nome-sobrenome já usado, e a distinção na exibição é feita pelo sufixo ordinal (ver Requirement: Numeração de nomes duplicados). A aplicação MUST falhar na inicialização se qualquer lista estiver ausente ou vazia. Unidades pré-existentes no banco de dados (antes desta migração) MUST receber um nome atribuído automaticamente pela migração.

#### Scenario: Unidade concluída tem nome e sobrenome
- **WHEN** uma ordem de treino conclui
- **THEN** a unidade criada tem um nome da lista A e um sobrenome da lista B, sorteados uniformemente

#### Scenario: Lote gera sorteios independentes
- **WHEN** um lote de 3 Soldados é treinado em paralelo
- **THEN** cada um dos 3 recebe seu próprio par nome-sobrenome (pares repetidos são permitidos e distinguidos pelo sufixo ordinal)

#### Scenario: Lista ausente ou vazia impede inicialização
- **WHEN** a aplicação tenta iniciar e o arquivo `nome_pessoas.json` está ausente ou vazio
- **THEN** a aplicação falha na inicialização e não prossegue
- **AND** uma mensagem de erro identifica qual lista falta

### Requirement: Numeração de nomes duplicados
Cada unidade SHALL ter um ordinal de nome, calculado por vila (o exército do jogador) sobre o par exato nome-sobrenome: a primeira unidade da vila com o par recebe ordinal 1 e é exibida sem sufixo ("Ana Silva"); cada unidade seguinte com o mesmo par recebe o próximo ordinal e é exibida com sufixo "(N)" ("Ana Silva (2)", "Ana Silva (3)"). A contagem MUST ser histórica: inclui todas as unidades que já tiveram o par na vila, inclusive as mortas, e um ordinal nunca é reaproveitado. Quando um mesmo lote gera o mesmo par mais de uma vez, os ordinais MUST ser atribuídos na ordem de criação das unidades do lote (arma de menor id primeiro). Vilas diferentes têm contagens independentes. O ordinal é atribuído na criação da unidade e é imutável; a API MUST expor o nome de exibição já formatado.

#### Scenario: Primeira ocorrência sem sufixo
- **WHEN** uma unidade conclui o treino com o par "Ana Silva" e a vila nunca teve uma "Ana Silva"
- **THEN** a unidade tem ordinal 1 e é exibida como "Ana Silva"

#### Scenario: Repetição recebe sufixo
- **WHEN** a vila já teve uma "Ana Silva" e uma nova unidade sorteia "Ana Silva"
- **THEN** a nova unidade tem ordinal 2 e é exibida como "Ana Silva (2)"

#### Scenario: Unidades mortas continuam contando
- **WHEN** "Ana Silva" e "Ana Silva (2)" morreram em masmorra e uma nova unidade sorteia "Ana Silva"
- **THEN** a nova unidade é exibida como "Ana Silva (3)"

#### Scenario: Mesmo nome duas vezes no mesmo lote
- **WHEN** um lote de 2 conclui, a vila nunca teve "João Souza" e ambas as unidades sorteiam "João Souza"
- **THEN** a unidade com a arma de menor id é "João Souza" e a outra é "João Souza (2)"

#### Scenario: Contagem independente por vila
- **WHEN** a vila A já teve 3 unidades "Ana Silva" e a vila B nunca teve nenhuma
- **THEN** a primeira "Ana Silva" da vila B é exibida sem sufixo

### Requirement: Troca de equipamento
O jogador SHALL poder trocar o item de um slot ocupado (Arma ou Armadura) de uma unidade da sua vila por outro item compatível com status `DISPONIVEL` do inventário da mesma vila, por `POST /api/jogo/unidades/{id}/equipamento` com corpo `{slot, itemId}`. Compatibilidade: no slot Arma, somente armas do modelo exigido pelo tipo da unidade (ESPADA/ARCO/LANCA); no slot Armadura, qualquer armadura (ARMADURA_COURO ou ARMADURA_FERRO); qualquer nível e origem. Na troca, o item retirado MUST voltar ao status `DISPONIVEL` e o novo item MUST passar a `EQUIPADO`; os atributos finais da unidade passam a refletir o novo item. Arma e Armadura MUST permanecer sempre ocupados (não há desequipar). A troca MUST ser rejeitada com 422 `UNIDADE_EM_MASMORRA` se a unidade estiver `EM_MASMORRA`; com 422 `ITEM_INDISPONIVEL` se o slot for um slot futuro ou se o item não existir, for de outra vila, não estiver `DISPONIVEL` ou for incompatível; com 404 `NAO_ENCONTRADO` se a unidade não existir ou for de outra vila; e com 400 se o corpo for inválido. Em qualquer rejeição, nenhuma mudança ocorre. A resposta de sucesso é o `VilaDto` atualizado.

#### Scenario: Troca de arma por outra compatível
- **WHEN** um Soldado `DISPONIVEL` com Espada N1 (ataque 6) troca a Arma por uma Espada N2 `DISPONIVEL` (ataque 8)
- **THEN** a resposta é 200 com o `VilaDto` atualizado
- **AND** a Espada N2 fica `EQUIPADO` no slot Arma e a Espada N1 volta a `DISPONIVEL` no inventário
- **AND** o ataque da unidade passa a 8

#### Scenario: Troca de armadura entre modelos
- **WHEN** a unidade troca a Armadura de couro N1 por uma Armadura de ferro N1 `DISPONIVEL`
- **THEN** a troca é aceita e a defesa da unidade passa a refletir a armadura de ferro

#### Scenario: Arma de modelo errado rejeitada
- **WHEN** se tenta equipar um `ARCO` no slot Arma de um Soldado
- **THEN** a requisição retorna 422 `ITEM_INDISPONIVEL` e nada muda

#### Scenario: Item não disponível rejeitado
- **WHEN** o item escolhido está `RESERVADO` (ordem de treino) ou `EQUIPADO` (em outra unidade)
- **THEN** a requisição retorna 422 `ITEM_INDISPONIVEL`

#### Scenario: Unidade em masmorra não troca
- **WHEN** a unidade está `EM_MASMORRA` e se tenta trocar sua Arma
- **THEN** a requisição retorna 422 `UNIDADE_EM_MASMORRA` e nada muda

#### Scenario: Slot futuro não aceita item
- **WHEN** se tenta equipar qualquer item no slot `CABECA`
- **THEN** a requisição retorna 422 `ITEM_INDISPONIVEL`

#### Scenario: Unidade inexistente ou de outra vila
- **WHEN** o id da unidade não existe ou pertence a outra vila
- **THEN** a requisição retorna 404 `NAO_ENCONTRADO`

#### Scenario: Morte após troca
- **WHEN** uma unidade trocou a Espada N1 pela Espada N2 e depois morre em combate
- **THEN** a Espada N2 e a armadura equipada são apagadas
- **AND** a Espada N1 permanece `DISPONIVEL` no inventário

### Requirement: Treino em lote
O treinamento de unidades MUST suportar pedidos de múltiplas unidades (lote) em uma única ordem. A configuração de ordem especifica: tipo de tropa (SOLDADO, ARQUEIRO, LANCEIRO), nível da arma (1–5, modelo determinado pelo tipo), modelo da armadura (ARMADURA_COURO ou ARMADURA_FERRO), nível da armadura (1–5), e quantidade de unidades 1–15 (limitada pelo máximo treinável). O servidor MUST validar estritamente: se a quantidade solicitada exceder qualquer limite (armas disponíveis do modelo/nível exigidos, armaduras disponíveis do modelo/nível escolhidos, comida, capacidade do exército), a ordem é rejeitada com um código de erro apropriado (ITEM_INDISPONIVEL, RECURSOS_INSUFICIENTES, CAPACIDADE_EXERCITO) e nenhuma mudança ocorre no estado. Na aceitação da ordem, a comida é debitada imediatamente (comida_tipo × quantidade) e N armas e N armaduras são reservadas (menores ids primeiro) com status RESERVADO. O tempo de conclusão é calculado como `ceil(tempo_tipo × quantidade / velocidade)` (padrão da forja). Quando a ordem conclui, as N unidades surgem juntas, cada uma com 1 arma + 1 armadura do lote reservado; os itens passam a status EQUIPADO.

#### Scenario: Lote de 3 Soldados debita comida e reserva itens
- **WHEN** uma vila tem 200 comida e pede um lote de 3 Soldados (custo 50 comida cada, 3 Espadas N1, 4 Armaduras de couro N1 disponíveis)
- **THEN** a ordem é aceita, a comida passa para 50 (200 − 150), 3 Espadas N1 e 3 Armaduras de couro N1 passam a RESERVADO
- **AND** o tempo é calculado (ex: ceil(60 × 3 / 1) = 180 s)

#### Scenario: Conclusão entrega 3 unidades juntas
- **WHEN** a ordem de lote de 3 conclui
- **THEN** 3 unidades DISPONIVEL surgem na vila, cada uma com 1 Espada N1 + 1 Armadura de couro N1 (EQUIPADO)

#### Scenario: Itens insuficientes rejeita com 422 ITEM_INDISPONIVEL
- **WHEN** a ordem pede 4 Espadas N1 mas há apenas 3 disponíveis
- **THEN** a requisição retorna 422 ITEM_INDISPONIVEL
- **AND** nenhuma mudança ocorre (comida preservada, nenhum item reservado)

#### Scenario: Quantidade 0 ou acima do limite rejeita com 400
- **WHEN** a quantidade é 0 ou 16
- **THEN** a requisição retorna 400 (invalid request)

### Requirement: Slots de equipamento
Toda unidade treinada MUST ter 9 slots de equipamento distribuídos em uma tela de detalhe. Os slots aceitam itens em categorias específicas: Arma (ARMA), Armadura (ARMADURA), Capacete/chapéu (CABECA, futuro), Bota (BOTA, futuro), Luva (LUVA, futuro), Colar (COLAR, futuro), Anel 1, Anel 2, Anel 3 (ANEL, futuro; os 3 aceitam a mesma categoria). Quando a unidade é criada pelo treino, Arma e Armadura MUST ser preenchidos obrigatoriamente com os itens do lote; os demais 7 slots permanecem vazios. Num slot ocupado, a aplicação exibe modelo, nível, atributos do item e origem (FORJA/MASMORRA); num slot vazio, exibe "Vazio". Arma e Armadura MUST permanecer sempre ocupados (não há desequipar); seu item pode ser trocado conforme o Requirement: Troca de equipamento. Os 7 slots futuros não aceitam itens nesta versão. Na morte da unidade, os itens que ocupam seus slots no momento da morte (na prática, a Arma e a Armadura atuais) MUST ser removidos junto.

#### Scenario: Unidade recém-treinada tem Arma e Armadura preenchidos
- **WHEN** uma unidade é criada pelo treino em lote
- **THEN** o slot Arma mostra o modelo, nível e atributos da arma (ex: "Espada N1 · Ataque 6")
- **AND** o slot Armadura mostra modelo, nível e atributos da armadura (ex: "Armadura de couro N1 · Defesa 2")
- **AND** os 7 demais slots mostram "Vazio"

#### Scenario: Morte destrói itens dos slots ocupados
- **WHEN** uma unidade morre em combate
- **THEN** a unidade é removida do banco de dados
- **AND** a arma e a armadura equipadas no momento da morte são removidas (apagadas, não devolvidas ao inventário)
