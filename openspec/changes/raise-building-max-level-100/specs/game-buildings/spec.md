## MODIFIED Requirements

### Requirement: Tipos de prédio e efeitos
O sistema SHALL reconhecer oito tipos de prédio (CENTRO_VILA, ARMAZEM, FAZENDA, SERRARIA, PEDREIRA, MINA_FERRO, FORJA, QUARTEL), cada um com um efeito específico no nível N definido em A.3. O efeito MUST estar ativo imediatamente quando a ordem de construção é concluída. Os efeitos que dependem de faixa de nível são: ARMAZEM — capacidade por recurso `500 × 2^(N−1)` para N ≤ 5 e `round_half_up(8000 × (N/5)^p)` para N > 5 (ver game-village — Recursos e capacidade); FAZENDA — N canteiros para N ≤ 5 e `5 + ⌊(N−5)/5⌋` para N > 5 (ver game-farming — Canteiros da fazenda); FORJA — nível máximo de item forjável N para N ≤ 10, `10 + ⌊(N−10)/5⌋` para 11 ≤ N ≤ 50 e `18 + ⌊(N−50)/10⌋` para 51 ≤ N ≤ 100 (ver game-forge — Nível limitado pela forja). SERRARIA, PEDREIRA, MINA_FERRO e QUARTEL mantêm efeitos lineares em N (30×N, 20×N, 10×N por hora e capacidade do exército 3×N).

#### Scenario: Pedreira nível 3 produz pedra
- **WHEN** a pedreira da vila está nível 3
- **THEN** a produção de pedra aumenta em 60/h (20 × 3)

#### Scenario: Centro da vila nível 1
- **WHEN** o centro da vila está nível 1
- **THEN** nenhum outro prédio pode ir além do nível 1

#### Scenario: Armazém nível 5
- **WHEN** o armazém está nível 5
- **THEN** a capacidade de cada recurso é 8000 (500 × 2^(5−1))

#### Scenario: Armazém nível 10 com expoente padrão
- **WHEN** o armazém está nível 10 e o expoente configurado é 1,5
- **THEN** a capacidade de cada recurso é 22627 (round_half_up(8000 × 2^1,5))

#### Scenario: Fazenda nível 10
- **WHEN** a fazenda está nível 10
- **THEN** a vila tem 6 canteiros (5 + ⌊(10−5)/5⌋)

#### Scenario: Forja nível 15
- **WHEN** a forja está nível 15
- **THEN** o nível máximo de item forjável é 11 (10 + ⌊(15−10)/5⌋)

### Requirement: Custo e tempo por nível
Para cada tipo de prédio e cada nível alvo N (1–100), o custo por recurso SHALL ser `round_half_up(base × 1,5^(N−1))` para N ≤ 5 e `round_half_up(base × 1,5^4 × (N/5)^p)` para N > 5, onde `p` é o expoente configurado (padrão 1,5; ver game-village — Expoente da curva de níveis configurável). O tempo SHALL ser `tempoBase × 2^(N−1)` segundos para N ≤ 5 e `ceil(tempoBase × 16 × N / 5)` segundos para N > 5. Os valores dos níveis 1–5 MUST ser idênticos aos da tabela em A.3. O cálculo MUST ser determinístico e MUST NOT falhar por overflow para nenhum N entre 1 e 100. Esses valores MUST ser aplicados com a velocidade configurada (custos em recurso, tempos divididos).

#### Scenario: Serraria 1→2
- **WHEN** a serraria está nível 1 e o jogador tenta melhorá-la para nível 2
- **THEN** o custo é M90 P60 (M150×0,6=90 arredondado, P60×1=60)
- **AND** o tempo de conclusão é 120 s com velocidade 1

#### Scenario: Forja nível 1
- **WHEN** a forja está nível 0 e o jogador tenta construir (atingir nível 1)
- **THEN** o custo é M120 P100 F40
- **AND** o tempo de conclusão é 120 s

#### Scenario: Centro da vila nível 5 inalterado
- **WHEN** o jogador melhora o centro da vila para o nível 5
- **THEN** o custo é M759 P759 e o tempo é 1920 s com velocidade 1

#### Scenario: Centro da vila nível 6 com expoente padrão
- **WHEN** o jogador melhora o centro da vila para o nível 6 e o expoente é 1,5
- **THEN** o custo é M998 P998 (round_half_up(150 × 5,0625 × 1,2^1,5))
- **AND** o tempo é 2304 s (ceil(120 × 16 × 6 / 5)) com velocidade 1

#### Scenario: Centro da vila nível 100 sem overflow
- **WHEN** o custo e o tempo do centro da vila no nível 100 são calculados com expoente 1,5
- **THEN** o custo é M67921 P67921 e o tempo é 38400 s
- **AND** nenhum erro de overflow ocorre

### Requirement: Débito no início e efeito na conclusão
Os recursos MUST ser debitados no instante em que a ordem é criada. O nível do prédio permanece inalterado até que a ordem seja concluída. No instante de conclusão, o nível passa para o alvo e o efeito torna-se ativo (incluindo, se for a FAZENDA, a criação de novo canteiro TRIGO somente quando o número de canteiros do novo nível for maior que o número de canteiros existentes).

#### Scenario: Recursos debitados imediatamente
- **WHEN** o jogador inicia a melhoria de uma serraria e tem exatamente M90 P60
- **THEN** os recursos são debitados imediatamente (agora tem 0)
- **AND** a serraria permanece nível 1 até `concluiEm`

#### Scenario: Nível alvo alcançado na conclusão
- **WHEN** uma ordem de melhoria é aplicada (ordem vencida)
- **THEN** o nível do prédio muda para o alvo
- **AND** o efeito do novo nível passa a valer

#### Scenario: Novo canteiro ao melhorar a fazenda
- **WHEN** a fazenda nível 1 é melhorada para nível 2 e a ordem é concluída
- **THEN** um novo canteiro (posição 2) é criado automaticamente com cultivo TRIGO

#### Scenario: Melhoria da fazenda sem novo canteiro
- **WHEN** a fazenda nível 5 (5 canteiros) é melhorada para nível 6 e a ordem é concluída
- **THEN** nenhum canteiro é criado e a vila continua com 5 canteiros

#### Scenario: Fazenda nível 10 ganha o sexto canteiro
- **WHEN** a fazenda nível 9 (5 canteiros) é melhorada para nível 10 e a ordem é concluída
- **THEN** um novo canteiro (posição 6) é criado com cultivo TRIGO

## REMOVED Requirements

### Requirement: Nível máximo 5
**Reason**: o nível máximo de prédio passa a ser 100 (ver Requirement: Nível máximo 100).
**Migration**: nenhuma ação do cliente; a regra continua retornando 422 `NIVEL_MAXIMO`, agora no nível 100.

## ADDED Requirements

### Requirement: Nível máximo 100
Os prédios SHALL ter níveis de 0 (não construído) a 100. Se um prédio já está no nível 100, qualquer tentativa de melhoria MUST ser rejeitada com 422 `NIVEL_MAXIMO`, e o `PredioDto` MUST informar `nivelMaximo` = 100.

#### Scenario: Prédio no nível máximo
- **WHEN** um prédio está nível 100 e o jogador tenta melhorá-lo
- **THEN** a operação é rejeitada com 422 `NIVEL_MAXIMO`

#### Scenario: Prédio acima do antigo limite pode ser melhorado
- **WHEN** o centro da vila está nível 5 e o jogador, com recursos suficientes e fila livre, tenta melhorá-lo
- **THEN** a operação é aceita e a ordem tem nível alvo 6
