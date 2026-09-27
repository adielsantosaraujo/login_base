# Tasks

## 1. Base: segurança, esquema, catálogo e infraestrutura

- [x] 1.1 [Segurança da API REST](tasks/1.1-seguranca-api-rest.md) — verificação: 401 anônimo em `/api/**`, 200 com CSRF, request cache ignora `/api`
- [x] 1.2 [Migração V3 do jogo](tasks/1.2-migracao-v3-jogo.md) — verificação: tabelas criadas, constraints e índices aplicados
- [x] 1.3 [Catálogo estático do jogo](tasks/1.3-catalogo-estatico-jogo.md) — verificação: tabela de custos, atributos e tropas validadas
- [x] 1.4 [Infra do jogo: relógio, aleatório, erros](tasks/1.4-infra-relogio-aleatorio-erros.md) — verificação: velocidade padrão 1, variações rejeitadas

## 2. Persistência

- [x] 2.1 [Entidades e repositórios do jogo](tasks/2.1-entidades-repositorios-jogo.md) — verificação: 8 entidades com lock pessimista, unique e índices

## 3. Economia e tempo

- [x] 3.1 [Calculadora de produção](tasks/3.1-calculadora-producao.md) — verificação: taxas, capacidade, milésimos e velocidade
- [x] 3.2 [Serviço da vila e sincronização](tasks/3.2-servico-vila-sincronizacao.md) — verificação: criação com estado inicial, produção sob demanda, ordens sincronizadas

## 4. Ações da vila

- [x] 4.1 [Serviço de construção](tasks/4.1-servico-construcao.md) — verificação: 5 erros validados, débito inicial, canteiro novo em fazenda
- [x] 4.2 [Serviço da fazenda](tasks/4.2-servico-fazenda.md) — verificação: plantio instantâneo com taxa de comida
- [x] 4.3 [Serviço da forja](tasks/4.3-servico-forja.md) — verificação: receitas e fila de forja
- [x] 4.4 [Serviço do quartel](tasks/4.4-servico-quartel.md) — verificação: reserva de itens, capacidade, conclusão com equipamento

## 5. Masmorras

- [x] 5.1 [Motor de combate tático](tasks/5.1-motor-combate-tatico.md) — verificação: movimento, ataque, dano, IA, turnos
- [x] 5.2 [Gerador de loot](tasks/5.2-gerador-loot.md) — verificação: rolagens com sementes, materiais, itens por nível
- [x] 5.3 [Serviço de masmorra](tasks/5.3-servico-masmorra.md) — verificação: início, persistência, vitória com loot e liberação

## 6. API REST

- [x] 6.1 [API da vila e do catálogo](tasks/6.1-api-vila-catalogo.md) — verificação: JSON de exemplo, mapeamento de erros
- [x] 6.2 [API das ações da vila](tasks/6.2-api-acoes-vila.md) — verificação: 4 POSTs, CSRF, validação 400
- [x] 6.3 [API de masmorras e batalhas](tasks/6.3-api-masmorras-batalhas.md) — verificação: 201 início, ações, 409 turno desatualizado

## 7. Frontend

- [x] 7.1 [Infra do frontend e rotas](tasks/7.1-frontend-infra-rotas.md) — verificação: build sem erros, proxy configurado, rotas e composables
- [x] 7.2 [Tela da vila e prédios](tasks/7.2-tela-vila-predios.md) — verificação: painel de recursos, grade de prédios, ordens
- [x] 7.3 [Tela da fazenda](tasks/7.3-tela-fazenda.md) — verificação: canteiros, cultivos, estoque de sementes
- [x] 7.4 [Tela da forja e inventário](tasks/7.4-tela-forja-inventario.md) — verificação: seleção de modelo/nível, custo, inventário
- [x] 7.5 [Tela do quartel](tasks/7.5-tela-quartel.md) — verificação: seleção de tipo, arma e armadura, lista de unidades
- [x] 7.6 [Telas de masmorras e batalha](tasks/7.6-telas-masmorra-batalha.md) — verificação: grid 8×8, combatentes, log, resultado com loot

## 8. Documentação e verificação

- [x] 8.1 [README do jogo](tasks/8.1-readme-jogo.md) — verificação: comandos e variáveis alinhados com compose e .env
- [x] 8.2 [Verificação integrada](tasks/8.2-verificacao-integrada.md) — verificação: todos os testes passam, validação aberta, roteiro manual
