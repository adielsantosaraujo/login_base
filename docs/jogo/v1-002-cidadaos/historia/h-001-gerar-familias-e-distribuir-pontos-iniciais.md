# H-001 — Gerar famílias e distribuir pontos iniciais

**Épico:** [../cidadao.md](../cidadao.md) · **Domínio:** [../familias.md](../familias.md)

## História

Como jogador novo, quero definir as características e profissões das minhas 4 famílias iniciais para começar o jogo com a vila que desejo.

## Contexto

A vila nasce com 4 famílias × 4 membros cada (16 cidadãos): pai, mãe, filho e filha. A tela abre com uma **distribuição automática** a partir do **plano padrão** (quantos cidadãos por profissão principal). O jogador pode alterar:
- Características e profissões de cada cidadão (20 pontos de característica, 10 de profissão, sem máximo por atributo);
- Profissão principal (refaz a distribuição desse cidadão); plano inicial (soma sempre 16);
- Família líder (escolha única).

**População inicial** (cidadao.md §5.4):
- 4 famílias × 4 membros: papéis PAI, MAE, FILHO, FILHA.
- Idades: pai e mãe com 40 anos; filho e filha com 18 anos.
- Nomes e sobrenomes gerados de listas fixas (seção 5.4).
- Distribuição automática pelo plano: ordem de papéis Comerciante → Construtor → ... em rodízio nas famílias (pai da 1ª, pai da 2ª, ..., depois mães, filhos, filhas).

**Profissão principal e secundária** (cidadao.md §5.2):
- Principal: profissão com maior PE; desempate pela ordem da tabela (Comerciante, Construtor, ...).
- Secundária: associada automaticamente à principal (ex.: Construtor → Carregador); 3 pontos na inicial.
- Profissão de apoio (2 pontos): primeira de [Carregador, Construtor, Madeireiro] que não seja principal nem secundária.

**Mínimos obrigatórios** (cidadao.md §5.4):
- Ao menos 2 cidadãos com Construtor principal.
- Ao menos 2 cidadãos com Carregador principal.

**Família líder** (cidadao.md §5.6):
- Líder = adulto mais velho da família. Empate: ordem pai → mãe → filho → filha.
- Bônus: +1% eficiência em toda a vila a cada 2 pontos de CAR do líder, máx. +10%.
- Sugestão automática: família cujo líder tem maior CAR.

## Critérios de aceite

### CA1 — Gerar 4 famílias com idades e papéis corretos

- **Dado** um novo jogador na tela de criação.
- **Quando** o sistema gera a população inicial.
- **Então**:
  - 4 famílias com 4 membros cada (16 cidadãos total), papéis PAI, MAE, FILHO, FILHA.
  - Cada família: pai 40 anos, mãe 40 anos, filho 18 anos, filha 18 anos.
  - Nomes/sobrenomes aleatórios de listas (gerados com a semente da vila).
  - Características todas 0 em base; PE de profissão todos 0 (para distribuição inicial).

### CA2 — Distribuição inicial automática pelo plano padrão

- **Dado** as 4 famílias geradas.
- **Quando** a tela abre.
- **Então**:
  - A distribuição segue o plano padrão (Comerciante 1, Construtor 2, ..., soma 16).
  - Profissões principais distribuídas em rodízio: pai 1ª fam., pai 2ª fam., ..., depois mães, filhos, filhas.
  - Cada cidadão recebe 5 pontos na profissão principal, 3 na secundária, 2 na de apoio, e 20 pontos de característica pelo algoritmo de pesos.
  - A distribuição é determinística (mesmo plano sempre gera o mesmo resultado).

### CA3 — Distribuir pontos de característica sem máximo por atributo

- **Dado** um cidadão com 20 pontos pendentes de característica.
- **Quando** o jogador aloca `X` pontos a VIT, `Y` a FOR, etc., até 20 no total.
- **Então**:
  - A soma total pode usar todos os 20 pontos, sem limite por atributo individual (ex.: VIT pode ir de 0 a 20).
  - Validação backend recusa soma > 20 ou valores negativos.

### CA4 — Distribuir pontos de profissão sem máximo por profissão

- **Dado** um cidadão com 10 pontos pendentes de profissão.
- **Quando** o jogador aloca `X` pontos a Construtor, `Y` a Agricultor, etc., até 10 no total.
- **Então**:
  - A soma total pode usar todos os 10 pontos, sem limite por profissão individual (ex.: Construtor pode ir de 0 a 10).
  - Validação backend recusa soma > 10 ou valores negativos.

### CA5 — Trocar profissão principal refaz a distribuição

- **Dado** um cidadão com Construtor (5) como principal.
- **Quando** o jogador o muda para Guerreiro.
- **Então**:
  - O cidadão é redistribuído com Guerreiro como principal (5 pontos, secundária Caçador 3, apoio 2).
  - O plano se atualiza: −1 Construtor, +1 Guerreiro.
  - Contadores de mínimos (Construtor e Carregador) refletem a mudança.

### CA6 — Plano inicial pode ser alterado; mínimos e soma 16

- **Dado** o plano padrão (soma 16).
- **Quando** o jogador altera a quantidade de uma ou mais profissões.
- **Então**:
  - O plano deve manter soma = 16.
  - Mínimos obrigatórios: Construtor ≥ 2, Carregador ≥ 2.
  - "Redistribuir pelo plano" só funciona quando soma = 16 e refaz os 16 cidadãos.

### CA7 — Família líder sugerida e escolha obrigatória

- **Dado** as 4 famílias com distribuição pronta.
- **Quando** a tela abre.
- **Então**:
  - A família cuja líder tem maior CAR é pré-selecionada como sugestão.
  - O botão "Confirmar" só fica habilitado após o jogador escolher uma família líder.

### CA8 — Líder é adulto mais velho com desempate

- **Dado** uma família com pai 40, mãe 40, filho 18, filha 18 anos.
- **Quando** a tela calcula o líder.
- **Então**:
  - O líder é o adulto (≥18) mais velho. Se houver empate de idade, vale a ordem: pai → mãe → filho → filha.

### CA9 — Bônus líder calculado e exibido

- **Dado** família líder com líder tendo CAR 13.
- **Quando** a tela exibe a família.
- **Então**:
  - Bônus = `floor(13 ÷ 2)` = +6% de eficiência em toda a vila.
  - Este bônus aparece no resumo e é enviado ao backend na confirmação.

### CA10 — Mínimos obrigatórios

- **Dado** as 4 famílias distribuídas.
- **Quando** o jogador tenta confirmar.
- **Então**:
  - Backend recusa se há < 2 Construtores principais (erro `MINIMO_CONSTRUTORES`).
  - Backend recusa se há < 2 Carregadores principais (erro `MINIMO_CARREGADORES`).

### CA11 — Pontos pendentes não bloqueiam confirmação

- **Dado** um cidadão com 15 pontos de característica alocados (5 pendentes).
- **Quando** o jogador tenta confirmar a população.
- **Então**:
  - A confirmação é aceita.
  - Os 5 pontos pendentes ficam registrados em `pontos_car_pendentes`.
  - Aparecem como aviso na tela, mas não bloqueiam.

## Tarefas

- [h-001-tarefa-001 — Modelo de dados de cidadãos e famílias](h-001-tarefa-001-modelo-de-dados-de-cidadaos-e-familias.md)
- [h-001-tarefa-002 — Geração das famílias iniciais](h-001-tarefa-002-geracao-das-familias-iniciais.md)
- [h-001-tarefa-003 — Tela de distribuição de pontos e família líder](h-001-tarefa-003-tela-de-distribuicao-de-pontos-e-familia-lider.md)

## Fora de escopo

- Botão "Editar" após confirmação (troca de família líder durante o jogo é outra história).
- Migração de dados de cidadãos entre versões do jogo.
