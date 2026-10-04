# Regiões

**Épico:** [vila.md](vila.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

As regiões são as 16 células da grade 4×4 da vila. Cada região tem 10×10 ladrilhos, um de 5 tipos (Floresta, Planície, Urbana, Litoral, Montanha) e 3 tipos de terreno com percentuais sorteados. Cada ladrilho tem um tipo de terreno, um bônus base e um bônus por adjacência de terreno igual; o bônus total do ladrilho afeta a produção dos prédios nele construídos. O mapa é gerado de forma determinística a partir de uma semente. As 3 regiões iniciais são escolhidas pelo jogador em conjunto conexo; as demais podem ser anexadas pagando um custo crescente com o número de regiões já possuídas.

## Regras

### Grade e escolha inicial
- R1: A grade é 4×4, com 16 regiões numeradas de 01 a 16 [req].
- R2: Regiões são vizinhas só na horizontal ou na vertical (ortogonal); diagonal não conta [req].
- R3: O jogador escolhe 3 regiões que devem formar um conjunto conexo ortogonal (grafo ligado); a ordem de seleção não importa (ex.: [6, 7, 10] e [10, 6, 7] são aceitos igualmente) [req].
- R4: Pelo menos 1 das 3 regiões escolhidas deve ser do tipo Urbana [req].

### Tipos de região
- R5: O tipo de cada região é definido pela geração do mapa, não pelo jogador [proposta].
- R6: Existem 5 tipos: **Floresta, Planície, Urbana, Litoral, Montanha** [proposta].
- R7: Cada tipo tem exatamente 3 tipos de terreno associados, com percentuais de distribuição dos ladrilhos, conforme a tabela "Terrenos por tipo" [req].

### Geração do mapa — etapa 1: tipos
- R8: Cada um dos 5 tipos aparece em **2 a 4** regiões, com a quantidade sorteada [proposta].
- R9: A soma das quantidades é sempre 16 [proposta].
- R10: No máximo **2 tipos** podem ter 4 regiões. Se 2 tipos já têm 4, os outros têm no máximo 3 cada [proposta].
- R11: Definidas as quantidades, os tipos são espalhados de forma aleatória nas 16 posições da grade [proposta].

### Geração do mapa — etapa 2: percentuais de terreno
- R12: Só depois que os tipos das 16 regiões estão definidos o jogo sorteia os percentuais dos terrenos [req].
- R13: Em cada região, sorteia-se a **ordem** dos 3 tipos de terreno do tipo de região (qual será o 1º, o 2º e o 3º) [req].
- R14: Depois, sorteia-se o **percentual** de cada terreno pela posição que ele ganhou [req]:
  - 1º percentual (b1): inteiro de **20 a 60**
  - 2º percentual (b2): inteiro de **20 a (90 − b1)**
  - 3º percentual (b3): **100 − (b1 + b2)**, que fica sempre ≥ 10
- R15: A soma sempre é 100%; nenhum terreno é especial ou separado dos outros [req].
- R16: Exemplo: região Floresta com ordem Floresta (1º), Plantações (2º), Barreiro (3º) e percentuais 40%, 35%, 25% [req].

### Bônus do ladrilho-âncora
- R17: O bônus de um prédio é o **bonus_total do seu ladrilho-âncora** (x,y), e só vale se o terreno da âncora for o terreno do prédio. Se não for, o bônus é 0 [req].
- R18: Correspondência entre terreno e prédios:
  - Floresta → Acampamento de lenhadores, Cabana de caça
  - Barreiro → Barreiro
  - Rocha → Pedreira
  - Ferro → Mina de ferro
  - Carvão → Mina de carvão
  - Salinas → Salina
  - Enxofre → Mina de enxofre
  - Plantações → Fazenda de plantio
  - Criações → Fazenda de criação
  - Indústria → Fábricas (Serraria, Olaria, Fundição, Tecelagem, Curtume, Cozinha)
  - Comércio → Mercado, Estalagem
  - Desenvolvimento → Casas
  - Militar → Quartel

### Efeito na vila inteira
- R19: O bônus de Comércio, Desenvolvimento e Militar na vila é a **média do bonus_total das âncoras dos prédios daquele grupo** (Mercado+Estalagem; Casas; Quartéis) **que estão em ladrilho do terreno certo**. Prédios fora do terreno certo não entram na média. Se nenhum estiver, o bônus é 0. Fator = 1 + média/100. Entram na média os prédios em estado Ativa ou Em aprimoramento; obras ainda não concluídas não entram. A média é arredondada em 2 casas [req].
- Comércio afeta: ouro do imposto e renda da Estalagem. O Mercado só contribui com a sua âncora para a média; o preço de venda e de compra do Mercado não muda.
- Desenvolvimento afeta: PO (pontos de obra) das obras (construções e aprimoramentos).
- Militar afeta: XP do treino no Quartel.

### Gerar novo mapa
- R20: Antes de criar a vila, o jogador pode pedir um novo mapa. Isso repete as etapas 1 e 2 e limpa a seleção atual [proposta].
- R21: A geração deve ser feita no servidor (com semente), e o mapa exibido deve ser o mesmo que será gravado [proposta].

### Complementos: ladrilhos, terrenos e anexação
- **Ladrilhos:** Cada região tem 10×10 = 100 ladrilhos [req].
- **Distância:** Distância entre regiões = distância de Manhattan entre (linha, coluna) [req].
- **Terrenos dos ladrilhos:** Cada ladrilho tem um tipo de terreno gerado determinística pela semente da vila, respeitando os percentuais da região. Cada ladrilho também tem bonus_base (0-100) e bonus_adjacente (+25 por vizinho ortogonal do mesmo tipo, até 100). Em regiões Urbanas, os ladrilhos também seguem os 3 terrenos da região (Indústria, Comércio, Desenvolvimento) nos percentuais sorteados; todos continuam construíveis por qualquer prédio urbano [req].
- **Anexação:** Para anexar uma nova região, ela deve ser **adjacente (ortogonalmente) a uma região possuída**. A região mantém o tipo e os 3 percentuais de terreno sorteados na geração do mapa. Os ladrilhos existem para todos os tipos, inclusive Urbana [req].
- **Masmorra:** Região com masmorra ativa **não pode ser anexada**. Região com masmorra limpa fica **6 turnos sem surgir nova masmorra** [req].
- R22: Quartel pode ser construído na Urbana **e no Litoral**, porque o terreno Militar só existe no Litoral [req].

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

### Terrenos por tipo

| Tipo | Terreno 1 | Terreno 2 | Terreno 3 |
|---|---|---|---|
| Floresta | Floresta | Barreiro | Plantações |
| Planície | Plantações | Criações | Floresta |
| Urbana | Indústria | Comércio | Desenvolvimento |
| Litoral | Salinas | Enxofre | Militar |
| Montanha | Rocha | Ferro | Carvão |

A ordem da tabela é só a lista de terrenos do tipo; a posição de cada um em cada região vem do sorteio (R13). Os percentuais de cada terreno em cada região vêm do sorteio (R14).

### Lista completa de tipos de terreno (13)

Floresta (Fl), Barreiro (Ba), Plantações (Pl), Criações (Cr), Rocha (Ro), Ferro (Fe), Carvão (Ca), Salinas (Sa), Enxofre (En), Militar (Mi), Indústria (In), Comércio (Co), Desenvolvimento (De).

### Cores dos terrenos

| Sigla | Terreno | Token CSS |
|---|---|---|
| Fl | Floresta | `--vl-terreno-fl` |
| Ba | Barreiro | `--vl-terreno-ba` |
| Pl | Plantações | `--vl-terreno-pl` |
| Cr | Criações | `--vl-terreno-cr` |
| Ro | Rocha | `--vl-terreno-ro` |
| Fe | Ferro | `--vl-terreno-fe` |
| Ca | Carvão | `--vl-terreno-ca` |
| Sa | Salinas | `--vl-terreno-sa` |
| En | Enxofre | `--vl-terreno-en` |
| Mi | Militar | `--vl-terreno-mi` |
| In | Indústria | `--vl-terreno-in` |
| Co | Comércio | `--vl-terreno-co` |
| De | Desenvolvimento | `--vl-terreno-de` |

Os valores das cores ficam a cargo do design.

### Faixas de percentual

| Posição | Mín. | Máx. | Nota |
|---|---|---|---|
| 1º (b1) | 20 | 60 | % |
| 2º (b2) | 20 | 90 − b1 | % |
| 3º (b3) | 10 | 60 | % (calculado como 100 − b1 − b2) |

- Por região: soma sempre 100%.
- Exemplo: b1=40, b2=35 → b3=25. Todos entre 10 e 60.

### Distribuições válidas de quantidade por tipo

As quantidades possíveis, em qualquer ordem entre os tipos, são:
- 4, 4, 3, 3, 2
- 4, 3, 3, 3, 3

A distribuição 4, 4, 4, 2, 2 é inválida (3 tipos com 4).

### Terrenos dos ladrilhos

Cada ladrilho de uma região tem:

1. **Tipo de terreno:** Gerado determinística pela semente da vila, respeitando os percentuais da região. Exemplo: região Floresta com Floresta 40%, Plantações 35%, Barreiro 25% → 40 ladrilhos Floresta, 35 Plantações, 25 Barreiro, em posições aleatórias (embaralhamento determinístico).

2. **Bonus base:** Inteiro sorteado de 0 a 100 para cada ladrilho.

3. **Bonus adjacente:** +25 para cada ladrilho vizinho (ortogonal: acima, abaixo, esquerda, direita) do mesmo tipo de terreno, só dentro dos 10×10. Máximo 4 vizinhos = até +100.

4. **Bonus total:** bonus_base + bonus_adjacente (0 a 200).

5. **Fórmula de produção:** Produção do prédio = base × (1 + bonus_total/100).
   - Exemplo: Prédio com produção base 100; ladrilho com bonus_total 80 → produção = 100 × 1,8 = 180.

6. **Endereço (A,1):** Cada ladrilho é identificado por coluna (A–J, x = 0..9) e linha (1–10, y = 0..9).
   - Exemplo: ladrilho na coluna 0, linha 0 = (A,1); coluna 9, linha 9 = (J,10).

7. **Siglas de 2 letras por terreno:** Fl (Floresta), Ba (Barreiro), Pl (Plantações), Cr (Criações), Ro (Rocha), Fe (Ferro), Ca (Carvão), Sa (Salinas), En (Enxofre), Mi (Militar), In (Indústria), Co (Comércio), De (Desenvolvimento).

**Exemplos numéricos:**

- Região Floresta com Floresta 40%, Plantações 35%, Barreiro 25%.
- Ladrilho Floresta no endereço (C,5) com bonus_base 30. Tem 2 vizinhos Floresta (acima e esquerda).
  - bonus_adjacente = 2 × 25 = 50.
  - bonus_total = 30 + 50 = 80.
  - Prédio com base 100 neste ladrilho → produção = 100 × (1 + 80/100) = 180.

- Região Urbana com Indústria 45%, Comércio 30%, Desenvolvimento 25% → 45 ladrilhos In, 30 Co, 25 De em posições aleatórias; qualquer prédio urbano pode ser construído em qualquer um deles, mas só recebe bônus se o terreno da âncora for o do prédio.

### Construções permitidas por terreno

| Prédio | Terreno associado | Regiões permitidas |
|---|---|---|
| Casa | Desenvolvimento | Urbana |
| Armazém, Ferraria, Alfaiataria, Carpintaria | — (sem terreno, sem bônus) | Urbana |
| Mercado, Estalagem | Comércio | Urbana |
| Quartel | Militar | Urbana, Litoral |
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

**Regra:** Prédio com terreno associado só recebe bônus se construído em ladrilho do terreno certo. Prédios urbanos (sem terreno específico) só podem ser construídos em região Urbana.

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

A anexação é imediata ao pagar; a região mantém o tipo e os 3 percentuais de terreno sorteados na geração do mapa. Os 100 ladrilhos são gerados se ainda não existirem, para todos os tipos, inclusive Urbana.

## Exemplos

**Exemplo 1: Sorteio de percentuais de uma região Montanha**
- Ordem sorteada: Ferro (1º), Carvão (2º), Rocha (3º).
- Percentuais: Ferro 45%, Carvão 28%, Rocha 27%.
- Os 100 ladrilhos da região serão 45 Ferro, 28 Carvão, 27 Rocha em posições aleatórias.

**Exemplo 2: Bônus de prédios por terreno**
- Região 06 (Urbana): tem Mercado no ladrilho (D,3) (Comércio, bonus_total 45) e Estalagem em (E,4) (Comércio, bonus_total 55).
  - Média de Comércio = (45 + 55) / 2 = 50. Fator = 1 + 50/100 = 1,5.
  - Ouro do imposto: × 1,5.
- Região 07 (Litoral): tem dois Quartéis: um com âncora em (A,2) (Militar, bonus_total 60) e outro em (A,3) (Militar, bonus_total 40).
  - Média de Militar = (60 + 40) / 2 = 50. Fator = 1,5.
  - XP do treino: × 1,5.

**Exemplo 3: Quantidades por tipo**
- Válido: Floresta 4, Planície 4, Urbana 3, Litoral 3, Montanha 2 (soma 16, dois tipos com 4).
- Inválido: Floresta 4, Planície 4, Montanha 4, Urbana 2, Litoral 2 (três tipos com 4).

**Exemplo 4: Bônus do ladrilho-âncora com terreno errado**
- Prédio Acampamento de lenhadores (terreno Floresta) construído em ladrilho Ba (Barreiro).
  - O ladrilho tem bonus_total 70, mas o terreno não bate (Floresta ≠ Barreiro).
  - Bônus do prédio = 0. Produção base sem modificação.

**Exemplo 5: Distância de Manhattan entre regiões**
- Região 6 (linha 1, coluna 1) até região 16 (linha 3, coluna 3): |1−3| + |1−3| = 4 turnos de viagem.
- Região 1 (linha 0, coluna 0) até região 8 (linha 1, coluna 3): |0−1| + |0−3| = 4 turnos.

**Exemplo 6: Anexação com custo crescente**
- Vila com 3 regiões: Ouro 150, Madeira 50, Pedra 50.
- Após anexar a 4ª: próxima custará Ouro 203, Madeira 100, Pedra 100.
- Após a 6ª: Ouro 369, Madeira 200, Pedra 200.

## Interações com outros domínios

- [vila.md](vila.md): criação da vila com seleção de 3 regiões conexas, 4 casas N1 nos 4 primeiros ladrilhos Desenvolvimento (De) em ordem de varredura da 1ª região Urbana da ordem de seleção, geração determinística do mapa a partir de semente.
- [../v1-001-masmorras/masmorras.md](../v1-001-masmorras/masmorras.md): masmorras bloqueiam anexação.
- [../v1-003-construcoes/construcoes.md](../v1-003-construcoes/construcoes.md): construções permitidas por tipo de região e terreno associado (tabela Construções permitidas por terreno); cada prédio marcado em um ladrilho-âncora recebe o bônus do terreno certo.
- [../v1-010-recursos-e-producao/producao.md](../v1-010-recursos-e-producao/producao.md): produção = base × (1 + bonus_total/100); bônus de Comércio, Desenvolvimento e Militar são a média das âncoras dos prédios daquele grupo.

## Modelo de dados

### Tabela `regiao`
- `id` (bigint, PK, identity)
- `vila_id` (bigint, FK)
- `indice` (int 1..16)
- `tipo` (varchar, enum: FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA)
- `possuida` (boolean)

### Tabela `regiao_terreno` (nova)
- `id` (bigint, PK, identity)
- `regiao_id` (bigint, FK para `regiao`)
- `terreno` (varchar, enum: FLORESTA, BARREIRO, PLANTACOES, CRIACOES, ROCHA, FERRO, CARVAO, SALINAS, ENXOFRE, MILITAR, INDUSTRIA, COMERCIO, DESENVOLVIMENTO)
- `posicao` (smallint 1..3) — ordem dos 3 terrenos da região
- `percentual` (smallint) — b1, b2, b3 conforme R14
- CHECK por posição: 1 → 20..60, 2 → 20..70, 3 → 10..60; únicos (regiao_id, terreno) e (regiao_id, posicao). A soma 100 e o limite b2 ≤ 90 − b1 são garantidos pela geração (não cabem em CHECK de linha).

### Tabela `ladrilho` (nova)
- `id` (bigint, PK, identity)
- `regiao_id` (bigint, FK para `regiao`)
- `x` (smallint 0..9) — coluna
- `y` (smallint 0..9) — linha
- `terreno` (varchar, enum idem `regiao_terreno.terreno`)
- `bonus_base` (smallint 0..100) — gravado na geração
- `bonus_adjacente` (smallint com CHECK em (0, 25, 50, 75, 100)) — gravado na geração
- Único `(regiao_id, x, y)`; cada região tem 100 ladrilhos (10×10).

**bonus_total** é calculado como `bonus_base + bonus_adjacente` (máx 200), não é gravado na tabela.

### Tabela `vila_previa` (existente, sem mudança)
- `usuario_id` (bigint, PK, FK para `usuarios`)
- `previa_id` (uuid, unique)
- `semente` (bigint)
- `rodada` (int ≥ 1)
- `criado_em` (timestamptz, default current_timestamp)

A prévia guarda as 16 regiões geradas com a semente, podendo ser regenerada ou substituída a qualquer momento. Na criação da vila, as 16 regiões são persistidas em `regiao` com seus tipos e terrenos, os terrenos em `regiao_terreno`, e os ladrilhos em `ladrilho`. A linha de `vila_previa` é apagada.

**Migration:** `V18__terrenos_e_ladrilhos.sql` (apaga `regiao_bonus` e `ladrilho_jazida`; cria `regiao_terreno` e `ladrilho`; banco limpo).

## Questões em aberto

- Teto para bônus acumulados na vila (ex.: limitar o fator a +200%)? Hoje sem teto (até +200 por ladrilho).
- Plano inicial variável pelas regiões escolhidas (ex.: +Mineiro com Montanha)? Fora desta change.
- Limite/custo para "Gerar novo mapa" no futuro (a `rodada` já é guardada para isso).
- Nomes fixos das famílias do handoff (Oliveira/Lima/Almeida/Pereira)? Mantidos gerados.
