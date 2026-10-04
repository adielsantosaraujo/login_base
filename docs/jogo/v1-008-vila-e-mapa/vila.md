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
- R9: Na criação, a semente gera os 5 tipos + 3 bônus de cada região, determinísticos; jazidas também são geradas pela semente [req].
- R10: O jogador vê a prévia do mapa (tipos e bônus) antes de escolher as regiões; prévia regenerável no servidor sem limite [req].
- R11: Recursos iniciais: Madeira 200, Pedra 100, Argila 50, Tábua 20, Grãos 200, Carne 40, Ouro 200 [proposta].
- R12: 4 casas N1 iniciais já vêm construídas na 1ª região Urbana escolhida, nos ladrilhos (0,0), (2,0), (4,0), (6,0) [proposta].
- R13: Bônus da vila = soma dos bônus de todas as regiões possuídas (na criação, as 3 regiões iniciais) [req]; ver [regioes.md](regioes.md) R17 e [design.md § D10](../../openspec/changes/redesenho-criacao-vila-populacao/design.md).

## Números e tabelas

### Regiões iniciais

| Regiões | Qtde | Adjacência necessária |
|---|---|---|
| 1ª escolhida | 1 | — (qualquer) |
| 2ª escolhida | 1 | Adjacente à 1ª |
| 3ª escolhida | 1 | Adjacente à 1ª ou 2ª |

### Casas iniciais

| Casa | Ladrilho | Nível | Núcleos | Vagas |
|---|---|---|---|---|
| 1 | (0, 0) | N1 | 1 | 4 |
| 2 | (2, 0) | N1 | 1 | 4 |
| 3 | (4, 0) | N1 | 1 | 4 |
| 4 | (6, 0) | N1 | 1 | 4 |

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

**Exemplo 2: Escolha das 3 regiões iniciais e bônus**
- Jogador escolhe região 06 (Urbana): bônus COMÉRCIO 47, INDÚSTRIA 30, DESENVOLVIMENTO 12.
- Regiões adjacentes a 06: 02, 05, 07, 10 (ortogonalmente).
- Jogador escolhe região 07 (Litoral). ✓ Adjacente a 06; bônus SALINAS 38, MILITAR 20, ENXOFRE 6.
- Regiões adjacentes a 06 ou 07: 02, 03, 05, 08, 10, 11.
- Jogador escolhe região 10 (Planície). ✓ Adjacente a 06 e a 07; bônus CRIAÇÕES 41, FLORESTA 25, PLANTAÇÕES 14.
- Vila aceita: 1 Urbana, 1 Litoral, 1 Planície (≥1 Urbana ✓). Bônus da vila: COMÉRCIO 47, CRIAÇÕES 41, SALINAS 38, INDÚSTRIA 30, FLORESTA 25, MILITAR 20, PLANTAÇÕES 14, DESENVOLVIMENTO 12, ENXOFRE 6.

**Exemplo 3: Casas iniciais**
- Primeira região Urbana escolhida: região 06.
- 4 casas N1 construídas em (0,0), (2,0), (4,0), (6,0) da região 06.
- Cada casa tem capacidade para 4 pessoas, totalizando 16 vagas nas casas iniciais.

## Interações com outros domínios

- [regioes.md](regioes.md) — tipos de região, jazidas, anexação, bônus de região.
- [../v1-002-cidadaos/cidadao.md](../v1-002-cidadaos/cidadao.md) — 4 famílias iniciais com 4 membros cada.
- [../v1-010-recursos-e-producao/recursos.md](../v1-010-recursos-e-producao/recursos.md) — recursos iniciais e armazenamento.

## Modelo de dados (resumo)

**Migração:** [V17__regioes_v2_bonus_e_previa.sql](/src/main/resources/db/migration/V17__regioes_v2_bonus_e_previa.sql) (nova; remove RURAL/COLETA do check da tabela `regiao`; cria `regiao_bonus` e `vila_previa`).

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
| semente | BIGINT | Semente para gerar o mapa (tipos e bônus) e as jazidas |
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

### Tabela: regiao_bonus

| Campo | Tipo | Descrição |
|---|---|---|
| id | BIGINT | Chave primária |
| regiao_id | FK | Referência à região |
| bonus | ENUM | FLORESTA, BARREIRO, PLANTACOES, CRIACOES, ROCHA, FERRO, CARVAO, SALINAS, ENXOFRE, MILITAR, INDUSTRIA, COMERCIO, DESENVOLVIMENTO |
| posicao | INT | 1, 2 ou 3 (ordem do bônus na região) |
| valor | INT | Valor percentual do bônus (35–50 para posição 1, 16–34 para posição 2, 5–15 para posição 3) |

### Tabela: vila_previa

| Campo | Tipo | Descrição |
|---|---|---|
| usuario_id | BIGINT | FK único para o usuário; PK |
| previa_id | UUID | Identificador único da prévia |
| semente | BIGINT | Semente aleatória para gerar o mapa |
| rodada | INT | Número da rodada de geração (incrementa a cada novo mapa) |
| criado_em | TIMESTAMPTZ | Quando a prévia foi criada |

### Tabela: ladrilho_jazida

| Campo | Tipo | Descrição |
|---|---|---|
| regiao_id | FK | Referência à região |
| x | INT | Coordenada x (0–9) |
| y | INT | Coordenada y (0–9) |
| jazida | ENUM | Floresta, Rocha, Barreiro, etc. (pode ser gerada da semente e não persistida) |

## Histórias

- [historia/h-001-criar-vila-escolhendo-regioes-iniciais.md](historia/h-001-criar-vila-escolhendo-regioes-iniciais.md)
- [historia/h-002-visualizar-mapa-da-vila.md](historia/h-002-visualizar-mapa-da-vila.md)
- [historia/h-003-anexar-nova-regiao.md](historia/h-003-anexar-nova-regiao.md)

## Questões em aberto

- Nome padrão da vila (gerado ou escolhido pelo jogador).
- Modo de exibição do mapa na tela: com ou sem números; zoom interativo.
