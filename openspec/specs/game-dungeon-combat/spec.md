# game-dungeon-combat Specification

## Purpose

Define o sistema de masmorras: níveis, liberação de acesso, criação de batalhas táticas com mapa 8×8, combate por turnos, movimento, ataque, defesa, IA dos inimigos, desempates e vitória/derrota.

## Requirements

### Requirement: Níveis de masmorra e liberação
As masmorras têm 5 níveis. O usuário começa com apenas o nível 1 liberado (`masmorra_nivel_liberado = 1`). Ao vencer uma masmorra de nível N, o nível N+1 é liberado (até o máximo de 5). Tentar entrar em um nível não liberado MUST retornar 422 `MASMORRA_BLOQUEADA`.

#### Scenario: Acesso ao nível 1
- **WHEN** um usuário novo tenta entrar na masmorra nível 1
- **THEN** a batalha é criada (nível está liberado)

#### Scenario: Acesso bloqueado ao nível 2
- **WHEN** o mesmo usuário tenta entrar na masmorra nível 2 sem ter vencido nível 1
- **THEN** a requisição retorna 422 `MASMORRA_BLOQUEADA`

#### Scenario: Liberação ao vencer
- **WHEN** o usuário vence a masmorra nível 2 (tendo já vencido nível 1)
- **THEN** `masmorra_nivel_liberado` muda para 3 e ele pode entrar no nível 3

#### Scenario: Nível 5 é o máximo
- **WHEN** o usuário vence o nível 5
- **THEN** `masmorra_nivel_liberado` permanece 5

### Requirement: Início da batalha
Uma batalha é iniciada com `POST /api/jogo/masmorras/{nivel}/batalhas` e um corpo contendo uma lista de 1 a 4 IDs de unidades distintas. As unidades MUST ser da vila do usuário e ter status `DISPONIVEL`. Uma única batalha por vila pode estar `EM_ANDAMENTO` por vez. A resposta MUST ser HTTP 201 com um objeto `BatalhaDto` contendo o estado inicial: turno 1, combatentes do jogador e inimigos nas posições iniciais, HP cheio.

#### Scenario: Batalha bem iniciada
- **WHEN** um usuário com 3 soldados `DISPONIVEL` inicia a masmorra nível 1 com IDs `[3, 4, 5]`
- **THEN** a resposta é 201, `turno` é 1, há 3 combatentes do jogador nas posições iniciais (2,7), (3,7), (4,7), os goblins estão nos slots de inimigos, unidades têm status `EM_MASMORRA`

#### Scenario: Esquadrão vazio
- **WHEN** a lista de unidades é vazia
- **THEN** a requisição retorna 422 `ESQUADRAO_INVALIDO`

#### Scenario: Esquadrão acima de 4 unidades
- **WHEN** a lista contém 5 unidades
- **THEN** a requisição retorna 422 `ESQUADRAO_INVALIDO`

#### Scenario: Unidade não disponível
- **WHEN** uma unidade tem status `EM_MASMORRA` ou `RESERVADO` (em ordem de treino)
- **THEN** a requisição retorna 422 `UNIDADE_INDISPONIVEL`

#### Scenario: Unidade de outra vila
- **WHEN** o ID refere a uma unidade que pertence a outro usuário
- **THEN** a requisição retorna 422 `UNIDADE_INDISPONIVEL`

#### Scenario: Batalha já em andamento
- **WHEN** há uma batalha `EM_ANDAMENTO` nessa vila e o usuário tenta iniciar outra
- **THEN** a requisição retorna 422 `BATALHA_EM_ANDAMENTO`

### Requirement: Mapa da masmorra
O mapa da masmorra SHALL ser uma grade 8×8 (colunas `x` 0–7, linhas `y` 0–7, sendo `y=0` o topo). O mapa MUST conter obstáculos em posições fixas (independentes do nível), posições iniciais do jogador e slots de spawn de inimigos. A composição de inimigos varia por nível (nível 1 = 3 Goblins, nível 2 = 1 Esqueleto Arqueiro + 3 Goblins, etc.), preenchendo os slots de inimigos em ordem.

#### Scenario: Mapa do nível 1
- **WHEN** uma batalha de nível 1 é iniciada
- **THEN** a resposta inclui `largura: 8`, `altura: 8`, `obstaculos: [{x:3,y:2}, {x:4,y:2}, ...]` (8 obstáculos total) e 3 combatentes inimigos (Goblins)

#### Scenario: Composição diferente por nível
- **WHEN** uma batalha de nível 2 é iniciada
- **THEN** há 4 combatentes inimigos (1 Esqueleto Arqueiro + 3 Goblins em ordem)

### Requirement: Movimento
Um combatente do jogador MUST usar a ação `MOVER {combatenteId, x, y}` para se mover. O movimento é válido se: o combatente ainda não moveu neste turno; o destino está dentro da grade (0–7 em ambas as dimensões); o destino é livre (sem obstáculo nem combatente vivo); o caminho é ortogonal (sem diagonais) com comprimento ≤ movimento do combatente; o combatente ainda não agiu (atacado ou defendido) neste turno.

#### Scenario: Movimento válido
- **WHEN** um soldado (movimento 3) em (2,7) move para (2,4) (3 casas acima, caminho livre)
- **THEN** a ação é aceita, a posição muda para (2,4)

#### Scenario: Movimento acima do limite
- **WHEN** o mesmo soldado (movimento 3) tenta mover para (2,1) (6 casas)
- **THEN** a requisição retorna 422 `ACAO_INVALIDA`

#### Scenario: Obstáculo bloqueia movimento
- **WHEN** um combatente tenta mover através de um obstáculo
- **THEN** a requisição retorna 422 `ACAO_INVALIDA`

#### Scenario: Já agiu neste turno
- **WHEN** um combatente já atacou e tenta mover
- **THEN** a requisição retorna 422 `ACAO_INVALIDA`

### Requirement: Ataque e dano
Um combatente MUST usar a ação `ATACAR {combatenteId, alvoId}` para atacar. O alvo MUST ser um combatente inimigo vivo, estar à distância Manhattan ≤ alcance da arma (sem exigência de linha de visão), e o atacante ainda não deve ter agido neste turno. O dano é determinístico: `max(1, ataque − defesa_efetiva)`, onde `defesa_efetiva` é a defesa base se o alvo não está defendendo, ou o dobro se está defendendo.

#### Scenario: Ataque bem-sucedido
- **WHEN** um soldado (ataque 8) adjacente a um goblin (defesa 1) ataca
- **THEN** o goblin sofre 7 de dano

#### Scenario: Dano mínimo de 1
- **WHEN** um arqueiro (ataque 4) ataca um orc (defesa 5)
- **THEN** o orc sofre 1 de dano

#### Scenario: Alvo fora do alcance
- **WHEN** um soldado (alcance 1) tenta atacar um inimigo a 2 casas de distância Manhattan
- **THEN** a requisição retorna 422 `ACAO_INVALIDA`

#### Scenario: Morte do inimigo
- **WHEN** um goblin com 1 HP é atingido por um dano ≥ 1
- **THEN** o goblin morre e sai do mapa

### Requirement: Defesa
Um combatente MUST usar a ação `DEFENDER {combatenteId}` para entrar em posição defensiva, aumentando sua defesa em 100% (multiplicada por 2) até o início do próximo turno do jogador. O combatente ainda não deve ter agido neste turno para usar essa ação.

#### Scenario: Defesa dobrada
- **WHEN** um soldado (defesa 3) executa `DEFENDER` e sofre um ataque de 9 de dano
- **THEN** o dano é `max(1, 9 − 6)` = 3 HP

#### Scenario: Defesa expira após turno
- **WHEN** a ação `ENCERRAR_TURNO` é executada
- **THEN** o status "defendendo" é removido de todos os combatentes do jogador

### Requirement: IA dos inimigos
Ao executar `ENCERRAR_TURNO`, a IA MUST controlar cada inimigo vivo na ordem (I1, I2, …, I5). Para cada um: escolhe o alvo (combatente do jogador vivo com menor distância Manhattan; em caso de empate, menor HP; em novo empate, ordem lexicográfica do ID); se o alvo está no alcance, ataca; caso contrário, move para a casa alcançável (BFS ≤ movimento) que minimiza distância Manhattan (desempate: menor y, depois menor x), e se o alvo ficou no alcance após a mudança, ataca. Inimigos MUST nunca defender.

#### Scenario: Ataque após BFS
- **WHEN** um goblin a 2 casas de um soldado executa sua IA (BFS encontra casa a 1 casa de distância)
- **THEN** o goblin se move e ataca o soldado no mesmo turno

#### Scenario: Escolha de alvo
- **WHEN** há um soldado a 2 casas e um arqueiro a 2 casas (mesma distância), mas o arqueiro tem 15 HP e o soldado 30 HP
- **THEN** o inimigo ataca o arqueiro

### Requirement: Turnos e controle de concorrência
Toda ação (movimento, ataque, defesa, encerrar turno, render) MUST incluir o número do turno atual. Se o turno incluído não corresponder ao turno atual da batalha, a requisição MUST retornar 409 `TURNO_DESATUALIZADO` sem alterar o estado. Após `ENCERRAR_TURNO`, o número do turno é incrementado.

#### Scenario: Turno desatualizado
- **WHEN** um cliente envia `ATACAR {..., turno: 1}` mas a batalha está no turno 2
- **THEN** a resposta é 409 `TURNO_DESATUALIZADO`, nenhuma mudança ocorre

#### Scenario: Turno correto aceito
- **WHEN** a mesma ação é enviada com `turno: 2`
- **THEN** o ataque é processado

### Requirement: Fim da batalha
A batalha termina quando: todos os inimigos morrem (`VITORIA`), todas as unidades do jogador morrem (`DERROTA`), o jogador executa `RENDER` (`DERROTA` imediatamente), ou o turno 30 é encerrado com inimigos ainda vivos (`DERROTA`). Ao terminar, a batalha recebe `status` final e `finalizada_em` preenchido. Ações após o fim MUST retornar 422 `BATALHA_ENCERRADA`. Unidades mortas são excluídas da vila com seus itens; sobreviventes retornam ao status `DISPONIVEL` (HP cheio na próxima batalha).

#### Scenario: Vitória
- **WHEN** o último inimigo morre
- **THEN** `status` muda para `VITORIA`, `finalizada_em` é preenchido, sobreviventes retornam `DISPONIVEL`

#### Scenario: Derrota por morte
- **WHEN** o último combatente do jogador morre
- **THEN** `status` muda para `DERROTA`, `finalizada_em` é preenchido, unidades são excluídas

#### Scenario: Derrota por Render
- **WHEN** o jogador executa `RENDER`
- **THEN** `status` muda para `DERROTA` imediatamente, `finalizada_em` é preenchido, sobreviventes retornam `DISPONIVEL`

#### Scenario: Derrota por timeout
- **WHEN** o turno 30 é encerrado e há inimigos vivos
- **THEN** `status` muda para `DERROTA`, `finalizada_em` é preenchido

#### Scenario: Ação após fim da batalha
- **WHEN** a batalha já terminou e o cliente tenta `ATACAR`
- **THEN** a requisição retorna 422 `BATALHA_ENCERRADA`

### Requirement: Batalha persistida por ação
O estado completo da batalha (posições, HP, flags de movimento/ação, turno, resultado) MUST ser serializado em JSON e gravado na base de dados após cada ação do jogador. Um log textual de eventos MUST acumular uma linha por evento. Ao chamar `GET /api/jogo/batalhas/{id}`, o estado gravado SHALL ser retornado integralmente.

#### Scenario: Recuperar estado após fechar navegador
- **WHEN** o jogador inicia uma batalha, move um combatente, fecha o navegador e volta 30 minutos depois
- **THEN** `GET /api/jogo/batalhas/{id}` devolve o estado salvo: mesma posição do combatente, mesmo HP, mesmo turno, mesmo log com a ação anterior

#### Scenario: Log acumulado
- **WHEN** há 5 ações em uma batalha
- **THEN** `log` contém 5 linhas descrevendo as ações
