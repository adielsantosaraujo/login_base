# H-001 — Gerar famílias e distribuir pontos iniciais

**Épico:** [../cidadao.md](../cidadao.md) · **Domínio:** [../familias.md](../familias.md)

## História

Como jogador novo, quero definir as características e profissões das minhas 4 famílias iniciais para começar o jogo com a vila que desejo.

## Contexto

A vila nasce com 4 famílias × 4 membros cada (16 cidadãos). Para cada pessoa, o jogador distribui 20 pontos de característica (máx. 10 por característica) e 10 pontos de profissão (máx. 5 por profissão), começando com 0 em tudo. Depois escolhe a família líder, que representa o jogador (seção 5.6).

**População inicial** (seção 5.4):
- Idades: pai e mãe com 40 anos; filho e filha com 18 anos.
- Nomes e sobrenomes gerados de listas fixas (seção 5.4).

**Família líder** (seção 5.6):
- Líder = adulto mais velho da família.
- Bônus: +1% eficiência em toda a vila a cada 2 pontos de CAR do líder, máx. +10%.

## Critérios de aceite

### CA1 — Gerar 4 famílias com idades corretas

- **Dado** um novo jogador na tela de criação.
- **Quando** o sistema gera a população inicial.
- **Então**:
  - 4 famílias com 4 membros cada (16 cidadãos total).
  - Cada família: pai 40 anos, mãe 40 anos, filho 18 anos, filha 18 anos.
  - Nomes/sobrenomes aleatórios de listas.
  - Características todas 0; PE de profissão todos 0.

### CA2 — Distribuir pontos de característica com limites

- **Dado** um cidadão com 20 pontos pendentes de característica.
- **Quando** o jogador aloca `X` pontos a VIT, `Y` a FOR, etc.
- **Então**:
  - Soma X + Y + ... = máx. 20.
  - Cada característica recebe no máx. 10.
  - Botão "distribuir automaticamente" (opcional) sugere uma distribuição balanceada.
  - Limite reforçado: validação backend recusa >= 10 em uma característica ou >= 20 no total.

### CA3 — Distribuir pontos de profissão com limites

- **Dado** um cidadão com 10 pontos pendentes de profissão.
- **Quando** o jogador aloca `X` pontos a Construtor, `Y` a Agricultor, etc.
- **Então**:
  - Soma X + Y + ... = máx. 10.
  - Cada profissão recebe no máx. 5.
  - Limite reforçado: validação backend recusa >= 5 em uma profissão ou >= 10 no total.

### CA4 — Escolher família líder (obrigatória)

- **Dado** 4 famílias prontas com pontos distribuídos.
- **Quando** o jogador não escolhe família líder e tenta confirmar.
- **Então** a tela rejeita com mensagem de erro.
- **E** o sistema permite confirmar apenas após selecionar uma das 4 famílias.

### CA5 — Bônus líder refletido na criação

- **Dado** família líder com líder (adulto mais velho, ex.: pai 40 anos) com CAR 16.
- **Quando** a vila é criada.
- **Então** o bônus eficiência = +1% × (16 ÷ 2) = +8% já aparece no relatório de criação.
- **E** este bônus afeta eficiências de trabalho no turno 1 em diante.

## Tarefas

- [h-001-tarefa-001 — Modelo de dados de cidadãos e famílias](h-001-tarefa-001-modelo-de-dados-de-cidadaos-e-familias.md)
- [h-001-tarefa-002 — Geração das famílias iniciais](h-001-tarefa-002-geracao-das-familias-iniciais.md)
- [h-001-tarefa-003 — Tela de distribuição de pontos e família líder](h-001-tarefa-003-tela-de-distribuicao-de-pontos-e-familia-lider.md)

## Fora de escopo

- Botão "Editar" após confirmação (troca de família líder durante o jogo é outra história).
- Migração de dados de cidadãos entre versões do jogo.
