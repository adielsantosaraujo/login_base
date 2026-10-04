# População inicial — distribuição de pontos (proposta)

**Substitui (quando validado):** limites de criação em [cidadao.md](../../../jogo/v1-002-cidadaos/cidadao.md) e o fluxo em [h-001-tarefa-003](../../../jogo/v1-002-cidadaos/historia/h-001-tarefa-003-tela-de-distribuicao-de-pontos-e-familia-lider.md) · **Protótipo:** [Distribuir Populacao.dc.html](../prototipos/Distribuir%20Populacao.dc.html)

## Resumo

Depois de criar a vila, o jogador define os pontos dos 16 cidadãos iniciais (4 famílias × 4 membros). A tela **já abre com uma distribuição automática**, gerada a partir de um **plano inicial** (quantas pessoas terão cada profissão como principal). O jogador pode alterar tudo: pontos, profissão principal de cada pessoa e o próprio plano.

## Regras

### População
- R1: 4 famílias × 4 membros = 16 cidadãos: pai e mãe com 40 anos, filho e filha com 18 [req].
- R2: Famílias e nomes iniciais: Oliveira (Marcos, Fernanda, André, Renata), Lima (Felipe, Juliana, Bruno, Camila), Almeida (Vitor, Patrícia, Diego, Larissa), Pereira (Rafael, Sandra, Lucas, Beatriz) [proposta: podem ser gerados].

### Limites de pontos
- R3: Cada pessoa tem **até 20 pontos de característica** no total (VIT, FOR, VEL, INT, CAR) [req].
- R4: Cada pessoa tem **até 10 pontos de profissão** no total, entre as 12 profissões [req].
- R5: **Não há limite por atributo**: uma característica pode ir de 0 a 20 e uma profissão de 0 a 10, desde que o total não passe do limite [proposta — substitui "máx. 10 / máx. 5"].
- R6: Pontos não usados ficam **pendentes** (`pontos_car_pendentes` / `pontos_prof_pendentes`) e podem ser distribuídos depois. Pendências não bloqueiam a confirmação [req].

### Profissão principal
- R7: A profissão principal de uma pessoa é a de **maior PE**. Em caso de empate, vale a primeira na ordem da tabela de profissões [proposta].
- R8: O jogador pode trocar a profissão principal de uma pessoa. Isso **refaz a distribuição dessa pessoa** com o algoritmo automático (R13–R16) e atualiza o plano: −1 na profissão antiga e +1 na nova [proposta].

### Plano inicial
- R9: O plano define quantas pessoas terão cada profissão como principal. A soma é sempre **16** [proposta].
- R10: Mínimos obrigatórios: **Construtor ≥ 2** e **Carregador ≥ 2** [req].
- R11: Plano padrão:

| Profissão | Qtd. |
|---|---|
| Comerciante | 1 |
| Construtor | 2 |
| Carregador | 2 |
| Madeireiro | 2 |
| Mineiro | 2 |
| Agricultor | 2 |
| Fazendeiro | 1 |
| Cozinheiro | 1 |
| Guerreiro | 2 |
| Ferreiro | 1 |
| Costureiro | 0 |
| Caçador | 0 |
| **Total** | **16** |

- R12: "Redistribuir pelo plano" refaz a distribuição dos 16 cidadãos a partir do plano atual. Só funciona com total = 16 [proposta].

### Distribuição automática (algoritmo)
- R13: **Atribuição de papéis.** A lista de papéis é montada na ordem: Comerciante, Construtor, Carregador, Madeireiro, Mineiro, Agricultor, Fazendeiro, Cozinheiro, Guerreiro, Ferreiro, Costureiro, Caçador (cada um repetido pela quantidade do plano). Ela é aplicada em rodízio entre as famílias: pai da Oliveira, pai da Lima, pai da Almeida, pai da Pereira, depois as mães, os filhos e as filhas. Assim, o 1º papel (Comerciante) vai para o pai da 1ª família, um candidato natural a líder.
- R14: **Profissões (10 pontos):**
  - **5** na principal;
  - **3** na secundária (tabela "Secundária");
  - **2** na de apoio, que é a primeira de Carregador → Construtor → Madeireiro que não seja nem a principal nem a secundária.
- R15: **Características (20 pontos)**, com peso por característica:
  - base: VIT 0,5;
  - +3 para cada característica ligada à principal;
  - +1,5 para cada uma ligada à secundária;
  - +0,5 para cada uma ligada à de apoio.
  - Os 20 pontos são dados um a um, sempre para a característica com maior `peso ÷ (valor atual + 1)`.
- R16: A distribuição automática é **determinística**: o mesmo plano gera sempre o mesmo resultado.

### Família líder
- R17: O jogador escolhe 1 das 4 famílias como líder [req].
- R18: O líder é o adulto mais velho da família. Em caso de empate de idade, vale a ordem pai → mãe → filho → filha [req + proposta].
- R19: Bônus do líder = **+1% de eficiência a cada 2 CAR**, até +10%: `min(10, floor(CAR ÷ 2))` [req].
- R20: Ao abrir a tela, a família pré-selecionada é a que tem o líder com mais CAR [proposta].

### Confirmação
- R21: "Confirmar população" exige:
  - ao menos **2 cidadãos com Construtor como principal**;
  - ao menos **2 com Carregador como principal**;
  - uma família líder escolhida [proposta].
- R22: O backend revalida todas as regras (R3–R5, R10 sobre o resultado, R17) [req].

## Tabelas

### Profissão × características ligadas e secundária

| Profissão | Características ligadas | Secundária (automática) |
|---|---|---|
| Construtor | INT | Carregador |
| Carregador | FOR, VEL | Construtor |
| Agricultor | INT | Fazendeiro |
| Fazendeiro | INT | Agricultor |
| Mineiro | FOR | Madeireiro |
| Madeireiro | FOR, VIT | Mineiro |
| Ferreiro | INT | Mineiro |
| Cozinheiro | VEL, CAR | Comerciante |
| Costureiro | VEL, CAR | Cozinheiro |
| Caçador | VIT, VEL, CAR | Guerreiro |
| Guerreiro | FOR, VIT, VEL | Caçador |
| Comerciante | CAR | Cozinheiro |

## Exemplos

**Exemplo 1 — Construtor**
- Profissões:
  - Construtor 5 (principal)
  - Carregador 3 (secundária)
  - Madeireiro 2 (apoio: Carregador e Construtor já estão usados)
- Pesos das características:
  - INT: 3
  - FOR: 1,5 + 0,5 = 2
  - VEL: 1,5
  - VIT: 0,5 + 0,5 = 1
  - CAR: 0
- Resultado: o maior peso fica com INT e CAR fica em 0. Os valores exatos saem de `referencia/distribuicao-populacao.js`.

**Exemplo 2 — Bônus do líder**
- Marcos (Comerciante) com CAR 10: `floor(10 ÷ 2)` = +5%.
- Com CAR 20 (agora possível): +10%, que é o teto.

**Exemplo 3 — Troca de profissão**
- O plano tem Construtor 2. O jogador troca Felipe de Construtor para Guerreiro.
- O plano fica com Construtor 1 e Guerreiro 3.
- "Construtores principais" fica em 1 / 2, e "Confirmar" é bloqueado até o jogador corrigir.

## Questões em aberto

- Sem máximo por atributo, alguém pode ter VIT 20 ou Construtor 10. Isso desequilibra o jogo? Vale um teto mais alto (ex.: 15 / 8)?
- Os nomes das famílias são fixos ou gerados?
- Pontos pendentes: há prazo para distribuí-los?
- O plano padrão deveria variar conforme as regiões escolhidas (ex.: mais Mineiros com Montanha)?
