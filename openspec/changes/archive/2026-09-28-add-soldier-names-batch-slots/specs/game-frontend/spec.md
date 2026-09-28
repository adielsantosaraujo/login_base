# Spec Delta: game-frontend

## ADDED Requirements

### Requirement: Detalhe da unidade
A tela do quartel MUST permitir abrir uma sub-tela de detalhe ao clicar em uma unidade listada. A rota é `/quartel/unidades/:id` (ex: `/quartel/unidades/42`). Se a unidade não existir ou pertencer a outra vila, a tela exibe "Unidade não encontrada" com um botão "Voltar" que retorna à lista do quartel. O detalhe exibe:
- Cabeçalho: nome de exibição (`nomeExibicao`: nome + sobrenome + sufixo ordinal quando houver, ex.: "Ana Silva (2)"), tipo de tropa (SOLDADO/ARQUEIRO/LANCEIRO), status (DISPONIVEL/EM_MASMORRA)
- Atributos finais: HP, ataque (derivado de arma + tipo), defesa (derivado de armadura + tipo), alcance (derivado de arma), movimento (do tipo)
- Grade de 9 slots de equipamento, em ordem: Arma, Armadura, Capacete/chapéu, Bota, Luva, Colar, Anel 1, Anel 2, Anel 3. Cada slot é renderizado em um Card com rótulo e conteúdo: se ocupado, mostra "Modelo · Nível · Atributos (ex: +2 ataque)" e origem (FORJA/MASMORRA); se vazio, mostra "Vazio".
- Nos Cards Arma e Armadura, um botão "Trocar" abre um diálogo com os itens `DISPONIVEL` compatíveis da vila (Arma: modelo igual a `CatalogoDto.tropas[tipo].armaExigida`; Armadura: categoria ARMADURA), exibindo modelo, nível, atributos e origem, ou "Nenhum item compatível disponível". Escolher um item envia `POST /api/jogo/unidades/{id}/equipamento {slot, itemId}` e atualiza a tela com o `VilaDto` retornado; erros aparecem no Toast. Com a unidade `EM_MASMORRA`, o botão fica desabilitado com a dica "Indisponível durante a masmorra". Slots futuros não têm botão.

#### Scenario: Unidade encontrada exibe detalhe completo
- **WHEN** o usuário clica em "Ana Silva" (tipo Soldado) na lista do quartel
- **THEN** a URL muda para `/quartel/unidades/42`
- **AND** a tela exibe nome "Ana Silva", tipo "Soldado", status "DISPONIVEL"
- **AND** os atributos finais aparecem (ex: HP 30, Ataque 6, Defesa 3, Alcance 1, Movimento 3)
- **AND** 9 slots aparecem: Arma com "Espada N1 · Ataque 6", Armadura com "Armadura de couro N1 · Defesa 2", Capacete/chapéu até Anel 3 vazios ("Vazio")

#### Scenario: Unidade não encontrada
- **WHEN** a URL é `/quartel/unidades/999` e a unidade 999 não existe
- **THEN** a tela exibe "Unidade não encontrada"
- **AND** um botão "Voltar" retorna para `/quartel`

#### Scenario: Clique na lista navega ao detalhe
- **WHEN** o usuário clica na linha "Ana Silva" (tipo Soldado) da tabela de unidades
- **THEN** a URL muda para `/quartel/unidades/42` e o detalhe é exibido sem recarregar a página

#### Scenario: Nome repetido no cabeçalho
- **WHEN** o usuário abre o detalhe de uma unidade com ordinal 2
- **THEN** o cabeçalho exibe "Ana Silva (2)"

#### Scenario: Trocar arma pelo detalhe
- **WHEN** o usuário clica "Trocar" no slot Arma de um Soldado e escolhe "Espada N2" no diálogo
- **THEN** é enviado `POST /api/jogo/unidades/42/equipamento` com `{slot: "ARMA", itemId: 17}`
- **AND** o slot Arma passa a exibir "Espada N2" e o ataque atualizado

#### Scenario: Troca bloqueada em masmorra
- **WHEN** a unidade exibida está `EM_MASMORRA`
- **THEN** os botões "Trocar" estão desabilitados com a dica "Indisponível durante a masmorra"

## MODIFIED Requirements

### Requirement: Quartel
A tela do quartel MUST permitir treinar unidades em lotes. O formulário exibe:
- Select de tipo de tropa (SOLDADO, ARQUEIRO, LANCEIRO)
- Select de nível da arma (1–5), filtrado pelo tipo escolhido (ex: Soldado aceita apenas ESPADA)
- Select de modelo da armadura (ARMADURA_COURO, ARMADURA_FERRO)
- Select de nível da armadura (1–5)
- InputNumber de quantidade (mín 1, máx exibido como dica pelo cliente; a regra fica no backend)
- Botão "Máx." que preenche a quantidade com o valor máximo calculado
- Um painel MUST exibir a capacidade do exército (`3 × nível`, ex: "2/6 unidades")

Abaixo, uma tabela de unidades MUST mostrar colunas: Nome (`nomeExibicao`, ex: "Ana Silva" ou "Ana Silva (2)"), tipo, HP, ataque/defesa/alcance (derivados dos itens), movimento, comida (custo de treino), status. A coluna "Nome" é clicável e navega para `/quartel/unidades/:id`.

Um painel MUST exibir ordens em andamento com progresso "Treinando N unidades" (onde N é a `quantidade` da ordem ativa).

Um botão "Treinar" MUST enviar `POST /api/jogo/quartel/ordens {tipo, armaNivel, armaduraModelo, armaduraNivel, quantidade}` (novo formato do corpo, sem armaId/armaduraId).

#### Scenario: Seleção de arma filtrada
- **WHEN** o usuário seleciona SOLDADO
- **THEN** o select de nível da arma lista só níveis de ESPADA com a quantidade disponível

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

### Requirement: Sem cálculo de regras no frontend
O frontend MUST exibir dados como recebidos do backend, sem calcular custos, tempos, atributos ou validações de regra. Custos e tempos de construção/forja/treino são obtidos do `CatalogoDto` ou do `VilaDto.proximoNivel`. Atributos derivados de itens/unidades vêm na resposta da API. Todas as validações de negócio (capacidade, bloqueios, fila) são realizadas pelo backend e retornadas em erros 422.

**Exceção para UX no Quartel:** O frontend PODE exibir uma dica de quantidade máxima calculada localmente (para guiar o usuário ao preencher o formulário), desde que o backend valide e rejeite qualquer quantidade acima dos limites reais com 422 (ITEM_INDISPONIVEL, RECURSOS_INSUFICIENTES, CAPACIDADE_EXERCITO).

**Exceção para UX na troca de equipamento:** o frontend PODE filtrar a lista de itens oferecidos no diálogo "Trocar" (status `DISPONIVEL`, modelo exigido do `CatalogoDto` ou categoria ARMADURA), desde que o backend valide e rejeite itens inválidos com 422 (`ITEM_INDISPONIVEL`, `UNIDADE_EM_MASMORRA`). O nome de exibição com sufixo vem pronto da API (`nomeExibicao`).

#### Scenario: Custo vem do catálogo
- **WHEN** o usuário abre a tela da forja
- **THEN** custos de ESPADA por nível vêm do `CatalogoDto.modelos[ESPADA].receita`, não são calculados no frontend

#### Scenario: Dica de máximo no cliente para o quartel
- **WHEN** o usuário abre o formulário de treino no quartel e já tem dados da vila
- **THEN** o campo de quantidade PODE exibir uma dica "Máx: 3" baseada no cliente (ex: calcular min de itens/comida/capacidade visivelmente)

#### Scenario: Validação no backend
- **WHEN** o usuário tenta treinar quantidade acima do máximo
- **THEN** o backend retorna 422 `CAPACIDADE_EXERCITO`, o frontend não prevê isso
