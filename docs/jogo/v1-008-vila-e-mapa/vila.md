# Vila e mapa

**Épico:** [vila.md](vila.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Uma vila é o conjunto de regiões controladas por um jogador, com limite máximo de 16. Cada usuário autenticado possui exatamente uma vila. Na criação, o jogador escolhe 3 regiões iniciais (pelo menos uma Urbana) e recebe recursos iniciais e 4 casas iniciais.

## Regras

- R1: Cada usuário autenticado tem 0 ou 1 vila; a vila é única por usuário [req].
- R2: A grade é 4×4, totalizando 16 regiões possíveis, numeradas de 1 a 16 [req].
- R3: Coordenada da região: índice 1..16; linha = (i−1) ÷ 4, coluna = (i−1) mod 4 [req].
- R4: Adjacência conta **somente ortogonal** (cima, baixo, esquerda, direita); diagonal não conta [proposta].
- R5: Na criação, a vila é criada escolhendo 3 regiões vizinhas entre si (conjunto conexo, ortogonalmente) [req].
- R6: As 3 regiões devem formar um conjunto conexo ortogonal (grafo ligado); a ordem de seleção não importa. A tela só habilita regiões vizinhas de alguma região já selecionada (ex.: [6, 7, 10] e [10, 6, 7] são aceitos igualmente) [req].
- R7: ≥1 das 3 regiões iniciais deve ser **Urbana** [requirement].
- R8: O tipo da região é definitivo na v1 (não há troca de tipo) [proposta].
- R9: Na criação, a semente gera os 5 tipos, a ordem e os percentuais dos 3 terrenos de cada região e, por região, o terreno e o bonus_base de cada um dos 100 ladrilhos (embaralhamento determinístico por semente + índice da região); o bonus_adjacente é calculado pelos vizinhos [req].
- R10: O jogador vê a prévia do mapa (tipos e composição de terrenos de cada região) antes de escolher as regiões; prévia regenerável no servidor sem limite [req].
- R11: Recursos iniciais: Madeira 200, Pedra 100, Argila 50, Tábua 20, Grãos 200, Carne 40, Ouro 200 [proposta].
- R12: 4 casas N1 iniciais já vêm construídas na 1ª região Urbana escolhida, nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura (y = 0..9, x = 0..9) [req].
- R13: Não existe bônus somado da vila. Cada região mostra sua composição (ex.: 'Floresta 40% · Plantações 35% · Barreiro 25%') e o painel de criação mostra o total de ladrilhos por terreno nas regiões escolhidas [req].

## Números e tabelas

### Regiões iniciais

| Regiões | Qtde | Adjacência necessária |
|---|---|---|
| 1ª escolhida | 1 | — (qualquer) |
| 2ª escolhida | 1 | Adjacente à 1ª |
| 3ª escolhida | 1 | Adjacente à 1ª ou 2ª |

### Casas iniciais

| Casa | Ladrilho | Endereço exibido | Nível | Núcleos | Vagas |
|---|---|---|---|---|---|
| 1 | 1º De em varredura | conforme coordenada | N1 | 1 | 4 |
| 2 | 2º De em varredura | conforme coordenada | N1 | 1 | 4 |
| 3 | 3º De em varredura | conforme coordenada | N1 | 1 | 4 |
| 4 | 4º De em varredura | conforme coordenada | N1 | 1 | 4 |

### Recursos iniciais

| Recurso | Quantidade |
|---|---|
| Madeira | 200 |
| Pedra | 100 |
| Argila | 50 |
| Tábua | 20 |
| Grãos | 200 |
| Carne | 40 |
| Ouro | 200 |

## Exemplos

**Exemplo 2: Escolha das 3 regiões iniciais e composição de terrenos**
- Jogador escolhe região 06 (Urbana): Comércio 47%, Indústria 30%, Desenvolvimento 23%.
- Regiões adjacentes a 06: 02, 05, 07, 10 (ortogonalmente).
- Jogador escolhe região 07 (Litoral). ✓ Adjacente a 06; Salinas 38%, Enxofre 22%, Militar 40%.
- Regiões adjacentes a 06 ou 07: 02, 03, 05, 08, 10, 11.
- Jogador escolhe região 10 (Planície). ✓ Adjacente a 06 e a 07; Plantações 45%, Criações 26%, Floresta 29%.
- Vila aceita: 1 Urbana, 1 Litoral, 1 Planície (≥1 Urbana ✓). Total de ladrilhos por terreno nas 3 regiões: Comércio 47, Indústria 30, Desenvolvimento 23, Salinas 38, Enxofre 22, Militar 40, Plantações 45, Criações 26, Floresta 29 (soma 300).

**Exemplo 3: Casas iniciais**
- Primeira região Urbana escolhida: região 06 (Urbana com Indústria 45%, Comércio 30%, Desenvolvimento 25% = 25 ladrilhos Desenvolvimento).
- Os 4 primeiros ladrilhos Desenvolvimento (De) em ordem de varredura (y = 0..9, x = 0..9): exemplo (0,0), (1,0), (2,0), (3,0).
- 4 casas N1 construídas nesses 4 primeiros ladrilhos De em ordem de varredura.
- Cada casa tem capacidade para 4 pessoas, totalizando 16 vagas nas casas iniciais.

## Interações com outros domínios

- [regioes.md](regioes.md) — tipos de região, terrenos e percentuais, ladrilhos (bonus_base/adjacente/total), anexação.
- [../v1-002-cidadaos/cidadao.md](../v1-002-cidadaos/cidadao.md) — 4 famílias iniciais com 4 membros cada.
- [../v1-010-recursos-e-producao/recursos.md](../v1-010-recursos-e-producao/recursos.md) — recursos iniciais e armazenamento.

## Modelo de dados (resumo)

**Migração:** [V17__regioes_v2_bonus_e_previa.sql](/src/main/resources/db/migration/V17__regioes_v2_bonus_e_previa.sql) (remove RURAL/COLETA do check da tabela `regiao`; cria `vila_previa`). [V18__terrenos_e_ladrilhos.sql](/src/main/resources/db/migration/V18__terrenos_e_ladrilhos.sql) (nova; apaga `regiao_bonus` e `ladrilho_jazida`; cria `regiao_terreno` e `ladrilho`).

### Tabela: jogo_turno

| Campo | Tipo | Descrição |
|---|---|---|
| numero | INT | Número sequencial global do turno |
| iniciado_em | TIMESTAMP | Quando o turno foi iniciado |
| concluido_em | TIMESTAMP | Quando o turno foi concluído |

### Tabela: vila

| Campo | Tipo | Descrição |
|---|---|---|
| id | BIGINT | Chave primária |
| usuario_id | BIGINT | FK único para o usuário; cada usuário tem 0 ou 1 vila |
| nome | VARCHAR | Nome da vila (definido pelo usuário ou gerado) |
| semente | BIGINT | Semente para gerar o mapa (tipos, percentuais de terreno e ladrilhos) |
| turno_criacao | INT | Turno em que a vila foi criada |
| familia_lider_id | FK | Referência à família que lidera a vila |
| bem_alimentada | BOOLEAN | Indica se a vila está bem alimentada neste turno |
| populacao_confirmada | BOOLEAN | Indica se a população foi confirmada |
| version | INT | Controle de concorrência (lock otimista @Version) |

### Tabela: regiao

| Campo | Tipo | Descrição |
|---|---|---|
| id | BIGINT | Chave primária |
| vila_id | FK | Referência à vila |
| indice | INT | 1–16; posição na grade 4×4 |
| tipo | ENUM | FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA (gravado nas 16 regiões na criação) |
| possuida | BOOLEAN | Se a região foi anexada pela vila |
| limpa_ate_turno | INT | Até qual turno a masmorra não pode surgir (após limpeza) |

### Tabela: regiao_terreno

| Campo | Tipo | Descrição |
|---|---|---|
| id | BIGINT | Chave primária |
| regiao_id | FK | Referência à região |
| terreno | ENUM | FLORESTA, BARREIRO, PLANTACOES, CRIACOES, ROCHA, FERRO, CARVAO, SALINAS, ENXOFRE, MILITAR, INDUSTRIA, COMERCIO, DESENVOLVIMENTO |
| posicao | INT | 1, 2 ou 3 (ordem do terreno na região) |
| percentual | INT | Percentual do terreno (b1 em 20–60, b2 em 20–(90−b1), b3 = 100−b1−b2 ≥ 10) |

### Tabela: vila_previa

| Campo | Tipo | Descrição |
|---|---|---|
| usuario_id | BIGINT | FK único para o usuário; PK |
| previa_id | UUID | Identificador único da prévia |
| semente | BIGINT | Semente aleatória para gerar o mapa |
| rodada | INT | Número da rodada de geração (incrementa a cada novo mapa) |
| criado_em | TIMESTAMPTZ | Quando a prévia foi criada |

### Tabela: ladrilho

| Campo | Tipo | Descrição |
|---|---|---|
| id | BIGINT | Chave primária |
| regiao_id | FK | Referência à região |
| x | INT | Coordenada x (0–9) |
| y | INT | Coordenada y (0–9) |
| terreno | ENUM | Floresta, Rocha, Barreiro, etc., conforme percentuais da região |
| bonus_base | INT | Bônus base do ladrilho (0–100) |
| bonus_adjacente | INT | Bônus por adjacência (0–100, calculado a partir de vizinhos do mesmo terreno) |

## Histórias

- [historia/h-001-criar-vila-escolhendo-regioes-iniciais.md](historia/h-001-criar-vila-escolhendo-regioes-iniciais.md)
- [historia/h-002-visualizar-mapa-da-vila.md](historia/h-002-visualizar-mapa-da-vila.md)
- [historia/h-003-anexar-nova-regiao.md](historia/h-003-anexar-nova-regiao.md)

## Questões em aberto

- Nome padrão da vila (gerado ou escolhido pelo jogador).
- Modo de exibição do mapa na tela: com ou sem números; zoom interativo.
