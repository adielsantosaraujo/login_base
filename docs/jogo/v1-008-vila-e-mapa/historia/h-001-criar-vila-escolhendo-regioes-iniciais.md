# H-001 — Criar vila escolhendo regiões iniciais

**Épico:** [../vila.md](../vila.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md)

## História

Como novo jogador, quero ver a prévia do mapa (tipos e percentuais de terreno de cada região) e escolher 3 regiões vizinhas entre si para fundar minha vila.

## Contexto

- Cada usuário autenticado tem exatamente 1 vila.
- O servidor mantém uma **prévia persistida** por usuário (tabela `vila_previa`) com semente e rodada; `POST /previa` gera ou renova; `GET /previa` devolve vigente; "Gerar novo mapa" é ilimitado.
- Na criação, o jogador escolhe 3 regiões (índices 1-16 da grade 4×4) que formam um **conjunto conexo** (ortogonalmente vizinhas entre si).
- Os 5 tipos e 3 terrenos com percentuais de cada região são gerados deterministicamente da semente; o jogador vê a prévia antes de escolher.
- Ao menos 1 das 3 regiões escolhidas deve ser **Urbana**.
- A vila é criada com a semente da prévia; gravas-se as 16 regiões com tipo e percentuais de terreno, e marca-se as 3 como possuídas.
- O painel de seleção mostra a composição de cada região e o total de ladrilhos por terreno nas regiões escolhidas.
- A vila recebe 4 casas N1 iniciais na **1ª região Urbana** (na ordem de seleção), nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura (y = 0..9, x = 0..9).
- A vila recebe recursos iniciais: Madeira 200, Pedra 100, Argila 50, Tábua 20, Grãos 200, Carne 40, Ouro 200.
- Erros: `VILA_JA_EXISTE` (409), `PREVIA_EXPIRADA` (409), `SELECAO_INVALIDA` (400), `REGIAO_NAO_ADJACENTE` (400), `SEM_REGIAO_URBANA` (400).

## Critérios de aceite

### CA1 — Prévia do mapa é gerada e exibida

- **Dado** um novo usuário autenticado sem vila
- **Quando** o usuário acessa a tela de criação, ou clica "Gerar novo mapa"
- **Então** `POST /previa` (ou `GET /previa`) retorna os 5 tipos e 3 terrenos com percentuais de cada uma das 16 regiões, com `rodada` e `previaId`

### CA2 — Escolha de 3 regiões conexas é aceita

- **Dado** o servidor com a prévia vigente (região 6 Urbana, 7 Litoral, 10 Planície, vizinhas entre si)
- **Quando** o usuário submete a escolha: região 6, região 7, região 10
- **Então** `POST /api/jogo/vila` com `{ "previaId": "...", "indices": [6, 7, 10] }` retorna `201 { "vilaId": 42, "proximaEtapa": "DISTRIBUIR_POPULACAO" }`; região 7 é vizinha a 6, região 10 é vizinha a 6 e 7 (✓ conexo); ≥1 Urbana (✓)

### CA3 — Seleção não conexa é rejeitada

- **Dado** o usuário tentando escolher região 6, região 7, região 1
- **Quando** o usuário submete a escolha
- **Então** `POST /api/jogo/vila` retorna `400 REGIAO_NAO_ADJACENTE` "As regiões escolhidas precisam ser vizinhas entre si"; 1 não é vizinha de 6 nem 7 (grafo desconectado)

### CA4 — Escolha sem região Urbana é rejeitada

- **Dado** o usuário tentando escolher regiões 2 (Planície), 3 (Litoral), 7 (Floresta)
- **Quando** o usuário submete a escolha
- **Então** `POST /api/jogo/vila` retorna `400 SEM_REGIAO_URBANA` "Ao menos uma região deve ser Urbana"

### CA5 — Usuário com vila não cria outra

- **Dado** um usuário autenticado que já tem uma vila criada
- **Quando** o usuário tenta enviar `POST /api/jogo/vila`
- **Então** o servidor retorna `409 VILA_JA_EXISTE` "Usuário já possui uma vila"; ou o frontend redireciona para `/jogo/mapa` (guarda de rota)

### CA6 — Recursos iniciais e casas iniciais são criados

- **Dado** a vila foi criada com regiões 6 (Urbana), 7 (Litoral), 10 (Planície)
- **Quando** a criação completa
- **Então** o estoque contém: Madeira 200, Pedra 100, Argila 50, Tábua 20, Grãos 200, Carne 40, Ouro 200; 4 casas N1 **da região 6** (1ª Urbana na ordem [6, 7, 10]) nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura e todas em terreno Desenvolvimento; 16 cidadãos pendentes 20/10; prévia apagada

### CA7 — Gerar novo mapa incrementa rodada

- **Dado** a prévia com `rodada` = 1
- **Quando** o usuário clica "Gerar novo mapa" (chama `POST /previa`)
- **Então** a nova prévia retorna `rodada` = 2; `previaId` novo; `semente` nova; "Gerar novo mapa" sem limite ou custo

### CA8 — Prévia expirada força recarregamento

- **Dado** a prévia com `previaId` = "uuid-1" e o servidor apaga a linha (ou usuário aguarda >30 min)
- **Quando** o usuário tenta enviar `POST /api/jogo/vila` com `previaId` = "uuid-1"
- **Então** retorna `409 PREVIA_EXPIRADA` "O mapa mudou. Escolha as regiões novamente"; frontend recarrega a prévia (`GET /previa`)

### CA9 — Percentuais gravados e total de ladrilhos por terreno

- **Dado** região 6 com 3 percentuais de terrenos (posição 1º: INDÚSTRIA 42%, posição 2º: COMÉRCIO 28%, posição 3º: DESENVOLVIMENTO 30%), região 7 com (posição 1º: SALINAS 38%, posição 2º: ENXOFRE 22%, posição 3º: MILITAR 40%), região 10 com (posição 1º: PLANTAÇÕES 45%, posição 2º: CRIAÇÕES 26%, posição 3º: FLORESTA 29%)
- **Quando** a vila é criada com essas 3
- **Então** a vila grava as 16 regiões com 3 terrenos cada (posição 1..3; b1 20–60, b2 20–(90−b1), b3 = 100−b1−b2 ≥ 10; soma 100) e 100 ladrilhos por região na proporção exata; o painel exibe "Indústria 42% · Comércio 28% · Desenvolvimento 30%" etc. e o total de ladrilhos: In 42, Co 28, De 30, Sa 38, En 22, Mi 40, Pl 45, Cr 26, Fl 29

## Tarefas

- [h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md)
- [h-001-tarefa-002-geracao-do-mapa-por-semente.md](h-001-tarefa-002-geracao-do-mapa-por-semente.md)
- [h-001-tarefa-003-api-de-criacao-da-vila.md](h-001-tarefa-003-api-de-criacao-da-vila.md)
- [h-001-tarefa-004-tela-de-criacao-da-vila.md](h-001-tarefa-004-tela-de-criacao-da-vila.md)

## Fora de escopo

- Não há limite de regiões a escolher durante a criação além das 3 iniciais obrigatórias.
- Edição/anulação de vila após criação.
- Migração de dados de vila para outro usuário.
