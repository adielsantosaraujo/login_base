# Regiões

**Épico:** [vila.md](vila.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

As regiões são as 16 células da grade 4×4 da vila. Cada região tem 10×10 ladrilhos e um tipo (Rural, Urbana ou Coleta). As regiões iniciais são escolhidas pelo jogador; as demais podem ser anexadas pagando um custo crescente com o número de regiões já possuídas.

## Regras

- R1: Grade é 4×4, totalizando 16 regiões [req].
- R2: Cada região tem 10×10 = 100 ladrilhos [req].
- R3: Adjacência **só ortogonal** (cima, baixo, esquerda, direita); diagonal não conta [proposta].
- R4: Distância entre regiões = distância de Manhattan entre (linha, coluna) [proposta].
- R5: Existem 3 tipos de região: Rural, Urbana, Coleta [req].
- R6: Cada tipo limita as construções permitidas (ver tabela 1.4 de game-design.md) [req].
- R7: Cada ladrilho tem uma jazida gerada pela semente da vila [proposta].
- R8: As jazidas seguem uma distribuição percentual por região (ver tabela) [proposta].
- R9: Garantia: toda região tem pelo menos 10 ladrilhos de Floresta, 10 de Rocha e 8 de Barreiro [proposta].
- R10: Jazidas são renováveis na v1 (não se esgotam) [proposta].
- R11: Em regiões Rurais e Urbanas, a jazida é ignorada (todo ladrilho é construível) [proposta].
- R12: Para anexar uma nova região, ela deve ser **adjacente (ortogonalmente) a uma região possuída** [proposta].
- R13: Região com masmorra ativa **não pode ser anexada** [proposta].
- R14: Região com masmorra limpa fica **6 turnos sem surgir nova masmorra** [proposta].

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

### Tipos de região

| Tipo | Construções permitidas | Descrição |
|---|---|---|
| Rural | Fazenda de plantio, Fazenda de criação | Agricultura e criação de animais |
| Urbana | Casa, Armazém, Fábricas (Serraria, Olaria, Fundição, Tecelagem, Curtume, Cozinha), Oficinas (Ferraria, Alfaiataria, Carpintaria) [proposta], Quartel, Mercado, Estalagem | Centro urbano com comércio e produção |
| Coleta | Acampamento de lenhadores, Pedreira, Barreiro, Mina de ferro, Mina de carvão, Salina, Mina de enxofre, Cabana de caça [proposta: carvão e caça adicionados como "entre outros"] | Exploração de recursos naturais |

### Jazidas (regiões de Coleta)

| Jazida | % dos ladrilhos | Recurso | Prédio que coleta |
|---|---|---|---|
| Floresta | 25% | Madeira (e caça) | Acampamento de lenhadores; Cabana de caça |
| Rocha | 20% | Pedra | Pedreira |
| Barreiro | 15% | Argila | Barreiro |
| Veio de ferro | 10% | Minério de ferro | Mina de ferro |
| Veio de carvão | 10% | Carvão | Mina de carvão |
| Salina | 7% | Sal | Salina |
| Enxofre | 5% | Enxofre | Mina de enxofre |
| Campo (neutro) | 8% | — | (só para posicionar prédios) |

**Garantias**: toda região tem pelo menos 10 ladrilhos de Floresta, 10 de Rocha e 8 de Barreiro.

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

A anexação é imediata ao pagar; o jogador escolhe o tipo da região no ato.

## Exemplos

**Exemplo 1: Distância de Manhattan entre regiões**
- Região 6 (linha 1, coluna 1) até região 16 (linha 3, coluna 3): |1−3| + |1−3| = 4 turnos de viagem.
- Região 1 (linha 0, coluna 0) até região 8 (linha 1, coluna 3): |0−1| + |0−3| = 4 turnos.

**Exemplo 2: Anexação com custo crescente**
- Vila com 3 regiões: Ouro 150, Madeira 50, Pedra 50.
- Após anexar a 4ª: próxima custará Ouro 203, Madeira 100, Pedra 100.
- Após a 6ª: Ouro 369, Madeira 200, Pedra 200.

## Interações com outros domínios

- [vila.md](vila.md) — vila e suas regiões iniciais.
- [../../v1-001-masmorras/masmorras.md](../v1-001-masmorras/masmorras.md) — masmorras bloqueiam anexação.

## Questões em aberto

- Ordem de apresentação das 16 regiões na tela (numérica, geográfica ou visual).
- Ícones/cores por tipo de região para visibilidade.
