# Regiões

**Épico:** [vila.md](vila.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

As regiões são as 16 células da grade 4×4 da vila. Cada região tem 10×10 ladrilhos, um de 5 tipos (Floresta, Planície, Urbana, Litoral, Montanha) e 3 bônus de região. O mapa é gerado de forma determinística a partir de uma semente. As 3 regiões iniciais são escolhidas pelo jogador em conjunto conexo; as demais podem ser anexadas pagando um custo crescente com o número de regiões já possuídas.

## Regras

### Grade e escolha inicial
- R1: A grade é 4×4, com 16 regiões numeradas de 01 a 16 [req].
- R2: Regiões são vizinhas só na horizontal ou na vertical (ortogonal); diagonal não conta [req].
- R3: O jogador escolhe 3 regiões que devem formar um conjunto conexo ortogonal (grafo ligado); a ordem de seleção não importa (ex.: [6, 7, 10] e [10, 6, 7] são aceitos igualmente) [req].
- R4: Pelo menos 1 das 3 regiões escolhidas deve ser do tipo Urbana [req].

### Tipos de região
- R5: O tipo de cada região é definido pela geração do mapa, não pelo jogador [proposta].
- R6: Existem 5 tipos: **Floresta, Planície, Urbana, Litoral, Montanha** [proposta].
- R7: Cada tipo tem exatamente 3 bônus de região, conforme a tabela "Bônus por tipo" [proposta].

### Geração do mapa — etapa 1: tipos
- R8: Cada um dos 5 tipos aparece em **2 a 4** regiões, com a quantidade sorteada [proposta].
- R9: A soma das quantidades é sempre 16 [proposta].
- R10: No máximo **2 tipos** podem ter 4 regiões. Se 2 tipos já têm 4, os outros têm no máximo 3 cada [proposta].
- R11: Definidas as quantidades, os tipos são espalhados de forma aleatória nas 16 posições da grade [proposta].

### Geração do mapa — etapa 2: bônus
- R12: Só depois que os tipos das 16 regiões estão definidos o jogo sorteia os bônus [proposta].
- R13: Em cada região, sorteia-se a **ordem** dos 3 bônus do tipo (qual será o 1º, o 2º e o 3º) [proposta].
- R14: Depois, sorteia-se o **valor** de cada bônus pela posição que ele ganhou [proposta]:
  - 1º bônus: **35 a 50**
  - 2º bônus: **16 a 34**
  - 3º bônus: **5 a 15**
- R15: Os valores são inteiros e os limites entram no sorteio [proposta].
- R16: Todos os bônus têm o mesmo peso; nenhum é especial ou separado dos outros [proposta].

### Bônus da vila
- R17: Os bônus de região da vila são a **soma** dos bônus das regiões possuídas, bônus por bônus [proposta].

### Gerar novo mapa
- R18: Antes de criar a vila, o jogador pode pedir um novo mapa. Isso repete as etapas 1 e 2 e limpa a seleção atual [proposta].
- R19: A geração deve ser feita no servidor (com semente), e o mapa exibido deve ser o mesmo que será gravado [proposta].

### Complementos: ladrilhos, jazidas e anexação
- **Ladrilhos:** Cada região tem 10×10 = 100 ladrilhos [req].
- **Distância:** Distância entre regiões = distância de Manhattan entre (linha, coluna) [req].
- **Jazidas (subsolo):** Cada ladrilho tem uma jazida gerada pela semente da vila. Distribuição percentual por região (ver tabela). Garantia: toda região tem pelo menos 10 ladrilhos de Floresta, 10 de Rocha e 8 de Barreiro. Renováveis na v1 (não se esgotam). Em regiões Urbanas, a jazida é ignorada (todo ladrilho é construível) [req / proposta].
- **Anexação:** Para anexar uma nova região, ela deve ser **adjacente (ortogonalmente) a uma região possuída**. Região anexada mantém o tipo e os 3 bônus sorteados na geração do mapa. Ladrilhos são gerados se ausentes para tipos ≠ Urbana [req / proposta].
- **Masmorra:** Região com masmorra ativa **não pode ser anexada**. Região com masmorra limpa fica **6 turnos sem surgir nova masmorra** [req].

## Números e tabelas

### Grade 4×4 e suas 16 regiões

```
|01|02|03|04|
|05|06|07|08|
|09|10|11|12|
|13|14|15|16|
```

Linha = (indice − 1) ÷ 4, Coluna = (indice − 1) mod 4.

### Adjacência ortogonal das 16 regiões

| Região | Vizinhos (ortogonais) |
|---|---|
| 01 | 02, 05 |
| 02 | 01, 03, 06 |
| 03 | 02, 04, 07 |
| 04 | 03, 08 |
| 05 | 01, 06, 09 |
| 06 | 02, 05, 07, 10 |
| 07 | 03, 06, 08, 11 |
| 08 | 04, 07, 12 |
| 09 | 05, 10, 13 |
| 10 | 06, 09, 11, 14 |
| 11 | 07, 10, 12, 15 |
| 12 | 08, 11, 16 |
| 13 | 09, 14 |
| 14 | 10, 13, 15 |
| 15 | 11, 14, 16 |
| 16 | 12, 15 |

### Bônus por tipo

| Tipo | Bônus 1 | Bônus 2 | Bônus 3 |
|---|---|---|---|
| Floresta | Floresta | Barreiro | Plantações |
| Planície | Plantações | Criações | Floresta |
| Urbana | Indústria | Comércio | Desenvolvimento |
| Litoral | Salinas | Enxofre | Militar |
| Montanha | Rocha | Ferro | Carvão |

A ordem da tabela é só a lista de bônus do tipo; a posição de cada um em cada região vem do sorteio (R13).

### Lista completa de bônus de região (13)

Floresta, Barreiro, Plantações, Criações, Rocha, Ferro, Carvão, Salinas, Enxofre, Militar, Indústria, Comércio, Desenvolvimento.

### Faixas de valor

| Posição | Mín. | Máx. |
|---|---|---|
| 1º | 35 | 50 |
| 2º | 16 | 34 |
| 3º | 5 | 15 |

- Por região: soma mínima 56, soma máxima 99.
- Um mesmo bônus na vila (3 regiões): máximo de 150.

### Distribuições válidas de quantidade por tipo

As quantidades possíveis, em qualquer ordem entre os tipos, são:
- 4, 4, 3, 3, 2
- 4, 3, 3, 3, 3

A distribuição 4, 4, 4, 2, 2 é inválida (3 tipos com 4).

### Construções permitidas por tipo

| Prédio | Bônus associado | Tipos permitidos |
|---|---|---|
| Casa, Armazém, Ferraria, Alfaiataria, Carpintaria, Mercado, Estalagem, Quartel | — | Urbana |
| Serraria, Olaria, Fundição, Tecelagem, Curtume, Cozinha (fábricas) | Indústria | Urbana |
| Fazenda de plantio | Plantações | Floresta, Planície |
| Fazenda de criação | Criações | Planície |
| Acampamento de lenhadores | Floresta | Floresta, Planície |
| Cabana de caça | Floresta | Floresta, Planície |
| Barreiro | Barreiro | Floresta |
| Pedreira | Rocha | Montanha |
| Mina de ferro | Ferro | Montanha |
| Mina de carvão | Carvão | Montanha |
| Salina | Salinas | Litoral |
| Mina de enxofre | Enxofre | Litoral |

**Regra:** Prédio com bônus associado só pode ser construído em região cujo tipo tenha esse bônus (ver tabela acima). Prédio sem bônus associado é urbano (só Urbana).

### Jazidas (subsolo das regiões)

| Jazida | % dos ladrilhos | Recurso | Prédio que coleta |
|---|---|---|---|
| Floresta | 25% | Madeira (e caça) | Acampamento de lenhadores; Cabana de caça |
| Rocha | 20% | Pedra | Pedreira |
| Barreiro | 15% | Argila | Barreiro |
| Veio de ferro | 10% | Minério de ferro | Mina de ferro |
| Veio de carvão | 10% | Carvão | Mina de carvão |
| Salina | 7% | Sal | Salina |
| Enxofre | 5% | Enxofre | Mina de enxofre |
| Campo (neutro) | 8% | — | (subsolo não construível) |

**Garantias**: toda região tem pelo menos 10 ladrilhos de Floresta, 10 de Rocha e 8 de Barreiro. Regiões Urbanas ignoram a jazida (todo ladrilho é construível).

### Anexação / expansão de regiões

Requisitos:
- Região adjacente (ortogonal) a uma região possuída.
- Sem masmorra ativa nela.

Custo (sendo k = nº de regiões já possuídas; k ≥ 3):
- Ouro = `round(150 × 1,35^(k−3))` (arredondamento meio para cima)
- Madeira = `50 × (k−2)`
- Pedra = `50 × (k−2)`

| k | Ouro | Madeira | Pedra |
|---|---|---|---|
| 3 | 150 | 50 | 50 |
| 4 | 203 | 100 | 100 |
| 5 | 274 | 150 | 150 |
| 6 | 369 | 200 | 200 |
| 7 | 497 | 250 | 250 |
| 8 | 671 | 300 | 300 |
| 9 | 905 | 350 | 350 |
| 10 | 1.226 | 400 | 400 |
| 15 | 5.497 | 650 | 650 |

A anexação é imediata ao pagar; a região mantém o tipo e os 3 bônus sorteados na geração do mapa. Ladrilhos são gerados se ausentes para tipos ≠ Urbana.

## Exemplos

**Exemplo 1: Sorteio de uma região Montanha**
- Ordem sorteada: Ferro (1º), Carvão (2º), Rocha (3º).
- Valores: Ferro 44, Carvão 21, Rocha 9.

**Exemplo 2: Bônus da vila**
- Região 06 (Urbana): Comércio 47, Indústria 30, Desenvolvimento 12.
- Região 07 (Litoral): Salinas 38, Militar 20, Enxofre 6.
- Região 10 (Planície): Criações 41, Floresta 25, Plantações 14.
- Total da vila: Comércio 47, Criações 41, Salinas 38, Indústria 30, Floresta 25, Militar 20, Plantações 14, Desenvolvimento 12, Enxofre 6. Os demais ficam em 0.

**Exemplo 3: Quantidades por tipo**
- Válido: Floresta 4, Planície 4, Urbana 3, Litoral 3, Montanha 2 (soma 16, dois tipos com 4).
- Inválido: Floresta 4, Planície 4, Montanha 4, Urbana 2, Litoral 2 (três tipos com 4).

**Exemplo 4: Distância de Manhattan entre regiões**
- Região 6 (linha 1, coluna 1) até região 16 (linha 3, coluna 3): |1−3| + |1−3| = 4 turnos de viagem.
- Região 1 (linha 0, coluna 0) até região 8 (linha 1, coluna 3): |0−1| + |0−3| = 4 turnos.

**Exemplo 5: Anexação com custo crescente**
- Vila com 3 regiões: Ouro 150, Madeira 50, Pedra 50.
- Após anexar a 4ª: próxima custará Ouro 203, Madeira 100, Pedra 100.
- Após a 6ª: Ouro 369, Madeira 200, Pedra 200.

## Interações com outros domínios

- [vila.md](vila.md): criação da vila com seleção de 3 regiões conexas, 4 casas N1 na 1ª região Urbana da ordem de seleção, geração determinística do mapa a partir de semente.
- [../v1-001-masmorras/masmorras.md](../v1-001-masmorras/masmorras.md): masmorras bloqueiam anexação.
- [../v1-003-construcoes/construcoes.md](../v1-003-construcoes/construcoes.md): construções permitidas por tipo de região e bônus associado (tabela Construções permitidas por tipo).
- [../v1-010-recursos-e-producao/producao.md](../v1-010-recursos-e-producao/producao.md): cada ponto de bônus aumenta a produção ligada em +1%; exemplos de produção com bônus.

## Modelo de dados

### Tabela `regiao`
- `id` (bigint, PK, identity)
- `vila_id` (bigint, FK)
- `indice` (int 1..16)
- `tipo` (varchar, enum: FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA)
- `possuida` (boolean)

### Tabela `regiao_bonus` (nova)
- `id` (bigint, PK, identity)
- `regiao_id` (bigint, FK para `regiao`)
- `bonus` (varchar, enum: FLORESTA, BARREIRO, PLANTACOES, CRIACOES, ROCHA, FERRO, CARVAO, SALINAS, ENXOFRE, MILITAR, INDUSTRIA, COMERCIO, DESENVOLVIMENTO)
- `posicao` (smallint 1..3)
- `valor` (int)
- Constraint: `(posicao, valor)` deve respeitar a faixa (R14)

### Tabela `vila_previa` (nova)
- `usuario_id` (bigint, PK, FK para `usuarios`)
- `previa_id` (uuid, unique)
- `semente` (bigint)
- `rodada` (int ≥ 1)
- `criado_em` (timestamptz, default current_timestamp)

A prévia guarda as 16 regiões geradas com a semente, podendo ser regenerada ou substituída a qualquer momento. Na criação da vila, as 16 regiões são persistidas em `regiao` com seus tipos e bônus, e a linha de `vila_previa` é apagada.

## Questões em aberto

- Teto para bônus acumulados (ex.: limitar o fator a +150%)? Hoje sem teto.
- Plano inicial variável pelas regiões escolhidas (ex.: +Mineiro com Montanha)? Fora desta change.
- Limite/custo para "Gerar novo mapa" no futuro (a `rodada` já é guardada para isso).
- Nomes fixos das famílias do handoff (Oliveira/Lima/Almeida/Pereira)? Mantidos gerados.
