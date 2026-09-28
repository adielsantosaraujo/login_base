## MODIFIED Requirements

### Requirement: Forja
A tela da forja MUST exibir controles para criar uma ordem: selects/inputs para modelo (ESPADA, LANCA, ARCO, ARMADURA_COURO, ARMADURA_FERRO), nível (1 até `VilaDto.nivelMaximoForjavel`), quantidade (1–5). Um painel MUST exibir o custo total calculado a partir do catálogo (custos já vêm do backend). Um botão "Forjar" MUST enviar `POST /api/jogo/forja/ordens {modelo, nivel, quantidade}`; ele fica desabilitado quando `nivelMaximoForjavel` é 0 ou o nível escolhido o excede. Abaixo, uma tabela de itens do inventário MUST mostrar: modelo, nível, ataque/defesa/alcance (derivados), origem (FORJA/MASMORRA), status (DISPONIVEL/RESERVADO/EQUIPADO).

#### Scenario: Cálculo de custo
- **WHEN** o usuário seleciona ESPADA nível 2, quantidade 2
- **THEN** o painel mostra "Custo: Madeira 80 · Ferro 120 · Tempo: 240s"

#### Scenario: Envio de ordem
- **WHEN** o usuário clica "Forjar"
- **THEN** POST `/api/jogo/forja/ordens` é enviado

#### Scenario: Inventário de itens
- **WHEN** a tela abre
- **THEN** há uma tabela listando itens: Modelo, Nível, Ataque, Defesa, Alcance, Origem, Status

#### Scenario: Nível limitado pelo nível máximo forjável
- **WHEN** a vila tem FORJA nível 15 (`nivelMaximoForjavel` = 11)
- **THEN** o campo de nível aceita valores de 1 a 11
- **AND** a tela indica que o nível máximo forjável é 11

### Requirement: Quartel
A tela do quartel MUST permitir treinar unidades em lotes. O formulário exibe:
- Select de tipo de tropa (SOLDADO, ARQUEIRO, LANCEIRO)
- Select de nível da arma (1–23), filtrado pelo tipo escolhido (ex: Soldado aceita apenas ESPADA), listando apenas os níveis com ao menos 1 item `DISPONIVEL` do modelo, com a quantidade disponível
- Select de modelo da armadura (ARMADURA_COURO, ARMADURA_FERRO)
- Select de nível da armadura (1–23), listando apenas os níveis com ao menos 1 armadura `DISPONIVEL` do modelo, com a quantidade disponível
- InputNumber de quantidade (mín 1, máx exibido como dica pelo cliente; a regra fica no backend)
- Botão "Máx." que preenche a quantidade com o valor máximo calculado
- Um painel MUST exibir a capacidade do exército (`3 × nível`, ex: "2/6 unidades")

Abaixo, uma tabela de unidades MUST mostrar colunas: Nome (`nomeExibicao`, ex: "Ana Silva" ou "Ana Silva (2)"), tipo, HP, ataque/defesa/alcance (derivados dos itens), movimento, comida (custo de treino), status. A coluna "Nome" é clicável e navega para `/quartel/unidades/:id`.

Um painel MUST exibir ordens em andamento com progresso "Treinando N unidades" (onde N é a `quantidade` da ordem ativa).

Um botão "Treinar" MUST enviar `POST /api/jogo/quartel/ordens {tipo, armaNivel, armaduraModelo, armaduraNivel, quantidade}` (novo formato do corpo, sem armaId/armaduraId).

#### Scenario: Seleção de arma filtrada
- **WHEN** o usuário seleciona SOLDADO
- **THEN** o select de nível da arma lista só níveis de ESPADA com a quantidade disponível

#### Scenario: Níveis listados conforme o inventário
- **WHEN** a vila tem 2 Espadas N1 e 1 Espada N12 `DISPONIVEL` e o usuário seleciona SOLDADO
- **THEN** o select de nível da arma lista apenas "Nível 1 (2 disponível)" e "Nível 12 (1 disponível)"

#### Scenario: Exibição de capacidade
- **WHEN** há 2 unidades e capacidade máxima é 6
- **THEN** o painel mostra "Capacidade: 2/6"

#### Scenario: Botão Máx. preenche quantidade
- **WHEN** o usuário clica o botão "Máx."
- **THEN** o campo de quantidade é preenchido com o valor máximo calculado (ex: 3, limitado por itens, comida e capacidade)

#### Scenario: Treino de unidade
- **WHEN** o usuário seleciona SOLDADO, nível de arma 1, Armadura de couro, nível 2, quantidade 3 e clica "Treinar"
- **THEN** POST `/api/jogo/quartel/ordens` é enviado com `{tipo: "SOLDADO", armaNivel: 1, armaduraModelo: "ARMADURA_COURO", armaduraNivel: 2, quantidade: 3}`

#### Scenario: Progresso de lote em andamento
- **WHEN** uma ordem de lote de 3 está em andamento
- **THEN** o painel exibe "Treinando 3 unidades" com barra de progresso

#### Scenario: Nome é clicável e navega ao detalhe
- **WHEN** o usuário clica em "Ana Silva" na tabela de unidades
- **THEN** a URL muda para `/quartel/unidades/42` e o detalhe é exibido

#### Scenario: Nome repetido exibido com sufixo
- **WHEN** a vila tem duas unidades "Ana Silva" com ordinais 1 e 2
- **THEN** a tabela exibe "Ana Silva" e "Ana Silva (2)"
