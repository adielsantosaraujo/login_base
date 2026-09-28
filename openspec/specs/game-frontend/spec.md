# game-frontend Specification

## Purpose

Define o frontend web do jogo: telas das aldeias, prédios, fazenda, forja, quartel, masmorras e batalha tática, com navegação por router, atualização de estado em tempo real via polling, e integração com a API REST.

## Requirements

### Requirement: Navegação do jogo
A aplicação MUST ter um menu (`Menubar`) com links para as principais telas do jogo: Vila (`/`), Fazenda (`/fazenda`), Forja (`/forja`), Quartel (`/quartel`), Masmorras (`/masmorras`). Cada link navega internamente sem recarregar a página. Um botão "Sair" envia um formulário POST para `/logout` (com CSRF) e redireciona para o login.

#### Scenario: Navegação entre telas
- **WHEN** o usuário clica em "Quartel" no menu
- **THEN** a URL muda para `/quartel` e a tela do quartel é exibida

#### Scenario: Botão Sair
- **WHEN** o usuário clica em "Sair"
- **THEN** um formulário POST `/logout` é enviado
- **AND** o navegador redireciona para `/login`

#### Scenario: Menu permanece visível
- **WHEN** o usuário está em qualquer tela do jogo
- **THEN** o menu está sempre visível no topo ou lateral

### Requirement: Redirecionamento ao login
Quando a API retorna HTTP 401 (não autenticado), o navegador MUST redirecionar para `/login` automaticamente. Após o usuário se autenticar na página Thymeleaf do backend, ele é redirecionado para `/` (página inicial do jogo).

#### Scenario: Sessão expirada redireciona
- **WHEN** a sessão HTTP expirou e o usuário tenta fazer uma ação (ex: `POST /api/jogo/predios/...`)
- **THEN** a API retorna 401
- **AND** o navegador redireciona para `/login`

#### Scenario: Retorno à vila após login
- **WHEN** o usuário faz login
- **THEN** ele é redirecionado para `/` (tela da vila)

### Requirement: Painel de recursos
Um painel MUST ser exibido permanentemente (no topo ou em barra fixa) mostrando os recursos atuais da vila: comida, madeira, pedra, ferro. Para cada recurso, exibe o valor atual e a capacidade (ex: "300/500"). A produção por hora de cada recurso também é exibida. Este painel MUST ser atualizado a cada 5 segundos automaticamente (polling) enquanto o usuário está na aplicação.

#### Scenario: Exibição dos recursos
- **WHEN** a tela da vila abre
- **THEN** o painel mostra, ex., "Comida: 300/500 · +20/h"

#### Scenario: Atualização automática
- **WHEN** 5 segundos passam
- **THEN** o painel refaz a requisição `GET /api/jogo/vila` e exibe valores novos

#### Scenario: Sem atualização quando sem componente montado
- **WHEN** o usuário sai da vila (vai para outra tela)
- **THEN** o polling para (economia de banda)

### Requirement: Prédios e fila de construção
A tela da vila MUST exibir todos os prédios em um grid (ex: 4 colunas). Para cada prédio, MUST mostrar seu tipo, nível atual, nível máximo (limitado pelo centro da vila), e quando há uma construção em andamento, uma barra de progresso com contagem regressiva. Um botão "Melhorar" MUST enviar `POST /api/jogo/predios/{tipo}/melhorar`. Se houver um bloqueio (ex: recursos insuficientes, fila ocupada, nível máximo), o botão fica desabilitado e mostra um tooltip com a razão.

#### Scenario: Exibição de prédio sem construção
- **WHEN** um prédio está no nível 2 sem construção ativa
- **THEN** o card mostra "Serraria" · "Nível 2" · "Próximo nível: M90 P60 · 120s" · botão "Melhorar" ativo

#### Scenario: Construção em andamento
- **WHEN** uma construção conclui em 30 segundos
- **THEN** a tela mostra uma barra de progresso que conta de 30 até 0

#### Scenario: Botão desabilitado por bloqueio
- **WHEN** o usuário não tem recursos suficientes
- **THEN** o botão "Melhorar" fica cinzento com tooltip "Recursos insuficientes"

#### Scenario: Clique em Melhorar
- **WHEN** o usuário clica o botão
- **THEN** a requisição POST é enviada e o painel de recursos é atualizado imediatamente

### Requirement: Fazenda
A tela da fazenda MUST exibir uma tabela com os canteiros da vila, cada linha mostrando: posição, cultivo atual, e produção por hora desse canteiro. Abaixo da tabela, um select de cultivo (ex: MILHO, BATATA, TRIGO) e um botão "Plantar". Um painel MUST mostrar o estoque de sementes (MILHO, BATATA, ABOBORA_DOURADA). Ao clicar em "Plantar", a aplicação MUST enviar `POST /api/jogo/canteiros/{posicao}/plantar {cultivo}`.

#### Scenario: Tabela de canteiros
- **WHEN** a tela da fazenda abre
- **THEN** há uma tabela com as colunas: Posição, Cultivo, Produção/h

#### Scenario: Seleção de cultivo
- **WHEN** o usuário seleciona "MILHO" e clica "Plantar"
- **THEN** a aplicação envia `POST /api/jogo/canteiros/1/plantar {cultivo: "MILHO"}`
- **AND** se bem-sucedido, a tabela é atualizada

#### Scenario: Estoque de sementes
- **WHEN** a tela abre
- **THEN** o painel mostra "MILHO: 2 · BATATA: 0 · ABOBORA_DOURADA: 0"

### Requirement: Forja
A tela da forja MUST exibir controles para criar uma ordem: selects/inputs para modelo (ESPADA, LANCA, ARCO, ARMADURA_COURO, ARMADURA_FERRO), nível (1–5), quantidade (1–5). Um painel MUST exibir o custo total calculado a partir do catálogo (custos já vêm do backend). Um botão "Forjar" MUST enviar `POST /api/jogo/forja/ordens {modelo, nivel, quantidade}`. Abaixo, uma tabela de itens do inventário MUST mostrar: modelo, nível, ataque/defesa/alcance (derivados), origem (FORJA/MASMORRA), status (DISPONIVEL/RESERVADO/EQUIPADO).

#### Scenario: Cálculo de custo
- **WHEN** o usuário seleciona ESPADA nível 2, quantidade 2
- **THEN** o painel mostra "Custo: Madeira 80 · Ferro 120 · Tempo: 240s"

#### Scenario: Envio de ordem
- **WHEN** o usuário clica "Forjar"
- **THEN** POST `/api/jogo/forja/ordens` é enviado

#### Scenario: Inventário de itens
- **WHEN** a tela abre
- **THEN** há uma tabela listando itens: Modelo, Nível, Ataque, Defesa, Alcance, Origem, Status

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

### Requirement: Masmorras
A tela de masmorras MUST exibir cards por nível (1–5). Cada card MUST mostrar: nível, bloqueio (desabilitado se não liberado), composição de inimigos (ex: "3 Goblins"). Um `MultiSelect` ou checkboxes MUST permitir selecionar de 1 a 4 unidades disponíveis. Um botão "Entrar" MUST enviar `POST /api/jogo/masmorras/{nivel}/batalhas {unidadeIds}`. Se há uma batalha ativa, um link MUST permitir retomar (`GET /batalhas/{id}`).

#### Scenario: Card de nível bloqueado
- **WHEN** apenas nível 1 está liberado
- **THEN** o card do nível 2 mostra "Bloqueado" e o botão "Entrar" está desabilitado

#### Scenario: Seleção de unidades
- **WHEN** o usuário marca 2 unidades no `MultiSelect`
- **THEN** o botão "Entrar" fica habilitado

#### Scenario: Retomada de batalha ativa
- **WHEN** há uma batalha ativa
- **THEN** há um link "Retomar batalha (Nível 2)" que navega para `/batalhas/{id}`

### Requirement: Batalha tática
A tela de batalha MUST exibir uma grade 8×8 renderizada em CSS grid ou canvas. A grade MUST mostrar: obstáculos (visuais, não clicáveis), combatentes do jogador e inimigos (com HP e ícone/cor), e posições livres. Um clique em um combatente MUST selecioná-lo; um clique em uma posição livre (se um combatente está selecionado) MUST enviar `MOVER`. Um botão "Atacar" com um select de alvo MUST enviar `ATACAR`. Botões "Defender" e "Encerrar turno" MUST enviar as ações respectivas. Um "Render" com `ConfirmDialog` MUST enviar `RENDER`. Um painel lateral DEVE mostrar o combatente selecionado (HP, ataque, defesa, alcance, movimento, status). Um log em `ScrollPanel` MUST acumular eventos. Ao fim da batalha, um diálogo DEVE mostrar resultado (Vitória/Derrota) e loot (recursos, sementes, itens).

#### Scenario: Seleção e movimento
- **WHEN** o usuário clica em um soldado e depois em uma posição 2 casas acima
- **THEN** um `MOVER` é enviado

#### Scenario: Ataque
- **WHEN** o usuário seleciona um combatente, clica "Atacar" e escolhe um inimigo adjacente
- **THEN** um `ATACAR` é enviado

#### Scenario: Log de eventos
- **WHEN** várias ações ocorrem
- **THEN** o log mostra, ex., "Turno 1 começou." · "Soldado se moveu para (2,5)." · "Goblin sofreu 7 de dano."

#### Scenario: Fim da batalha
- **WHEN** todos os inimigos morrem
- **THEN** um diálogo mostra "VITÓRIA" e lista recursos/itens ganhos
- **AND** um botão "Voltar" navega de volta para a tela de masmorras

### Requirement: Mensagens de erro
Erros da API (422 `RECURSOS_INSUFICIENTES`, 409 `TURNO_DESATUALIZADO`, etc.) MUST ser exibidos em um componente Toast (notificação) no canto da tela com a mensagem retornada pelo backend. Em batalhas, um erro 409 `TURNO_DESATUALIZADO` causa a recarga automática da batalha (sem exibir erro ao usuário).

#### Scenario: Erro 422 exibido
- **WHEN** o usuário tenta melhorar um prédio sem recursos
- **THEN** a resposta é 422 `RECURSOS_INSUFICIENTES`
- **AND** um Toast exibe "Recursos insuficientes." (mensagem do backend)

#### Scenario: Erro 409 em batalha
- **WHEN** o turno está desatualizado
- **THEN** a batalha é recarregada automaticamente

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
