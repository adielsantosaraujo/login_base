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
A tela do quartel MUST permitir treinar unidades. Selects para tipo de tropa (SOLDADO, ARQUEIRO, LANCEIRO), e selects/filtros para arma compatível (filtradas por tipo) e armadura (todas disponíveis). Um painel MUST mostrar a capacidade do exército (`3 × nível`, ex: "3/6 unidades"). Um botão "Treinar" MUST enviar `POST /api/jogo/quartel/ordens {tipo, armaId, armaduraId}`. Abaixo, uma tabela de unidades MUST mostrar: tipo, HP, ataque/defesa/alcance (derivados dos itens), movimento, comida (custo de treino), status.

#### Scenario: Seleção de arma filtrada
- **WHEN** o usuário seleciona SOLDADO
- **THEN** o select de arma mostra apenas ESPADA (arma exigida de Soldado)

#### Scenario: Exibição de capacidade
- **WHEN** há 2 unidades e capacidade máxima é 6
- **THEN** o painel mostra "Capacidade: 2/6"

#### Scenario: Treino de unidade
- **WHEN** o usuário seleciona SOLDADO, arma ID 5, armadura ID 6 e clica "Treinar"
- **THEN** POST `/api/jogo/quartel/ordens` é enviado com `{tipo: "SOLDADO", armaId: 5, armaduraId: 6}`

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

#### Scenario: Custo vem do catálogo
- **WHEN** o usuário abre a tela da forja
- **THEN** custos de ESPADA por nível vêm do `CatalogoDto.modelos[ESPADA].receita`, não são calculados no frontend

#### Scenario: Validação no backend
- **WHEN** o usuário tenta treinar excedendo a capacidade
- **THEN** o backend retorna 422 `CAPACIDADE_EXERCITO`, o frontend não prevê isso
