# Vila e mapa

**Épico:** [vila.md](vila.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Uma vila é o conjunto de regiões controladas por um jogador, com limite máximo de 16. Cada usuário autenticado possui exatamente uma vila. Na criação, o jogador escolhe 3 regiões iniciais (pelo menos uma Urbana) e recebe recursos iniciais e 4 casas iniciais.

## Regras

- R1: Cada usuário autenticado tem 0 ou 1 vila; a vila é única por usuário [req].
- R2: A grade é 4×4, totalizando 16 regiões possíveis, numeradas de 1 a 16 [req].
- R3: Coordenada da região: índice 1..16; linha = (i−1) ÷ 4, coluna = (i−1) mod 4 [req].
- R4: Adjacência conta **somente ortogonal** (cima, baixo, esquerda, direita); diagonal não conta [proposta].
- R5: Na criação, o jogador escolhe 3 regiões e o tipo de cada uma [req].
- R6: A 1ª região pode ser qualquer uma; a 2ª e 3ª devem ser adjacentes (ortogonalmente) a alguma região já escolhida [req].
- R7: Ao menos 1 das 3 regiões iniciais deve ser **Urbana** [proposta].
- R8: O tipo da região é definitivo na v1 (não há troca de tipo) [proposta].
- R9: Na criação, a vila recebe uma `semente` aleatória que gera, de forma determinística, as jazidas de todos os ladrilhos das 16 regiões [proposta].
- R10: O jogador vê a prévia das jazidas antes de escolher [proposta].
- R11: Recursos iniciais: Madeira 200, Pedra 100, Argila 50, Tábua 20, Grãos 200, Carne 40, Ouro 200 [proposta].
- R12: 4 casas N1 iniciais já vêm construídas na 1ª região Urbana escolhida, nos ladrilhos (0,0), (2,0), (4,0), (6,0) [proposta].

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

**Exemplo 1: Escolha das 3 regiões iniciais**
- Jogador escolhe região 6 (Urbana).
- Regiões adjacentes a 6: 2, 5, 7, 10 (ortogonalmente).
- Jogador escolhe região 7 (Urbana também). ✓ Adjacente a 6.
- Regiões adjacentes a 6 ou 7: 2, 3, 5, 8, 10, 11.
- Jogador tenta escolher região 1 (Rural). ✗ Não adjacente a nenhuma já escolhida. Rejeitado.
- Jogador escolhe região 2 (Rural). ✓ Adjacente a 6.
- Vilas aceita com 3 regiões (2 Urbanas, 1 Rural).

**Exemplo 2: Casas iniciais**
- Primeira região Urbana escolhida: região 7.
- 4 casas N1 construídas em (0,0), (2,0), (4,0), (6,0) da região 7.
- Cada casa tem capacidade para 4 pessoas, totalizando 16 vagas nas casas iniciais.

## Interações com outros domínios

- [regioes.md](regioes.md) — tipos de região, jazidas, anexação.
- [../../v1-002-cidadaos/cidadao.md](../v1-002-cidadaos/cidadao.md) — 4 famílias iniciais com 4 membros cada.
- [../../v1-010-recursos-e-producao/recursos.md](../v1-010-recursos-e-producao/recursos.md) — recursos iniciais e armazenamento.

## Modelo de dados (resumo)

### Tabela: jogo_turno

| Campo | Tipo | Descrição |
|---|---|---|
| numero | INT | Número sequencial global do turno |
| iniciado_em | TIMESTAMP | Quando o turno foi iniciado |
| concluido_em | TIMESTAMP | Quando o turno foi concluído |

### Tabela: vila

| Campo | Tipo | Descrição |
|---|---|---|
| id | UUID/BIGINT | Chave primária |
| usuario_id | UUID/BIGINT | FK único para o usuário; cada usuário tem 0 ou 1 vila |
| nome | VARCHAR | Nome da vila (definido pelo usuário ou gerado) |
| semente | BIGINT | Semente aleatória para gerar jazidas deterministicamente |
| turno_criacao | INT | Turno em que a vila foi criada |
| familia_lider_id | FK | Referência à família que lidera a vila |
| bem_alimentada | BOOLEAN | Indica se a vila está bem alimentada neste turno |
| version | INT | Controle de concorrência (lock otimista @Version) |

### Tabela: regiao

| Campo | Tipo | Descrição |
|---|---|---|
| id | UUID/BIGINT | Chave primária |
| vila_id | FK | Referência à vila |
| indice | INT | 1–16; posição na grade 4×4 |
| tipo | ENUM | RURAL, URBANA, COLETA (nulo se não possuída) |
| possuida | BOOLEAN | Se a região foi anexada pela vila |
| limpa_ate_turno | INT | Até qual turno a masmorra não pode surgir (após limpeza) |

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
