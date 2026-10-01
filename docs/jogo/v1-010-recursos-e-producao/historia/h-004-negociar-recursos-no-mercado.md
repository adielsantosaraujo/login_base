# H-004 — Negociar recursos no mercado

**Épico:** [../recursos.md](../recursos.md) · **Domínio:** [../comercio.md](../comercio.md)

## História

Como comerciante, quero comprar e vender recursos no Mercado com preços que variam conforme meu PE de Comerciante, para ganhar ouro e obter recursos raros.

## Contexto

Mercado NPC com preços dinâmicos. Preço de venda = base × min(1,0; 0,5 + 0,02 × PE Comerciante melhor). Preço de compra = base × max(1,0; 1,5 − 0,02 × PE Comerciante melhor). Volume máximo por turno = 20 × eficiência × mult. nível.

**Referência:** [../comercio.md#regras](../comercio.md#regras) — Mercado R1-R5.

No passo 2 do turno (ouro passivo), a vila também recebe Ouro sem negociar: imposto de 0,5 Ouro por cidadão com 18 anos ou mais ([../recursos.md#regras](../recursos.md#regras), R8) e receita da Estalagem, que serve até `5 × Σ eficiência × mult. nível` Refeições por turno a 4 Ouro cada, com multiplicador ×1,0 / ×1,2 / ×1,5 para N1 / N2 / N3 ([../comercio.md#regras](../comercio.md#regras), R6–R7; [../../v1-003-construcoes/estalagem.md](../../v1-003-construcoes/estalagem.md)). A imigração pela Estalagem é tratada em [../../v1-002-cidadaos/historia/h-003-nascimento-e-crescimento.md](../../v1-002-cidadaos/historia/h-003-nascimento-e-crescimento.md) (CA7).

## Critérios de aceite

### CA1 — Vender recurso pelo Mercado
- **Dado** um Mercado N1 com 1 Comerciante (PE 12, eficiência 1,0), 20 Madeira em estoque
- **Quando** jogador vende 10 Madeira
- **Então** preço = 1 × min(1,0; 0,5 + 0,02 × 12) = 0,74 Ouro/unit; recebe 7,4 Ouro; estoque cai para 10 Madeira

### CA2 — Comprar recurso pelo Mercado
- **Dado** mesmo Mercado, 100 Ouro em estoque
- **Quando** jogador compra 4 Aço
- **Então** preço = 20 × max(1,0; 1,5 − 0,02 × 12) = 25,2 Ouro/unit; paga 100,8 Ouro; estoque cai para -0,8 Ouro (erro: recursos insuficientes)

### CA3 — Limite de volume por turno
- **Dado** Mercado N1 sem Comerciante (0 eficiência)
- **Quando** jogador tenta vender
- **Então** volume máximo = 20 × 0 × 1,0 = 0; operação rejeitada

### CA4 — Sem Mercado, venda rejeitada
- **Dado** vila sem nenhum Mercado
- **Quando** jogador tenta vender
- **Então** erro "Mercado não disponível"

### CA5 — Imposto cobrado dos adultos
- **Dado** uma vila com 10 cidadãos vivos com 18 anos ou mais e 4 cidadãos menores de 18 anos
- **Quando** o passo 2 do turno (ouro passivo) for processado
- **Então** a vila recebe 10 × 0,5 = 5 Ouro; menores não pagam imposto; um evento `IMPOSTO_COBRADO` é registrado no relatório do turno

### CA6 — Estalagem serve Refeições e gera Ouro
- **Dado** uma Estalagem N1 ativa com 2 Cozinheiros alocados (eficiências 0,9 e 1,1) e 30 Refeições em estoque
- **Quando** o passo 2 do turno for processado
- **Então** capacidade = 5 × (0,9 + 1,1) × 1,0 = 10 Refeições; são consumidas 10 Refeições (estoque cai para 20) e a vila recebe 10 × 4 = 40 Ouro; um evento `ESTALAGEM_RECEITA` é registrado

### CA7 — Multiplicador de nível da Estalagem
- **Dado** uma Estalagem N2 ativa (×1,2) com 2 Cozinheiros de eficiência 1,0 e 30 Refeições em estoque
- **Quando** o passo 2 do turno for processado
- **Então** capacidade = 5 × 2,0 × 1,2 = 12 Refeições; são consumidas 12 Refeições e a vila recebe 48 Ouro

### CA8 — Refeições insuficientes limitam a receita
- **Dado** uma Estalagem N1 com capacidade de 10 Refeições e apenas 3 Refeições em estoque
- **Quando** o passo 2 do turno for processado
- **Então** são servidas 3 Refeições (estoque cai para 0) e a vila recebe 12 Ouro; com 0 Refeições, nenhuma é servida e nenhum Ouro é gerado pela Estalagem

### CA9 — Estalagem ausente ou sem trabalhadores
- **Dado** uma vila sem Estalagem ativa, ou com Estalagem sem Cozinheiros/Comerciantes alocados
- **Quando** o passo 2 do turno for processado
- **Então** nenhuma Refeição é consumida e a Estalagem não gera Ouro; o imposto (CA5) continua sendo cobrado normalmente

## Tarefas

- [h-004-tarefa-001-api-de-compra-e-venda.md](h-004-tarefa-001-api-de-compra-e-venda.md)
- [h-004-tarefa-002-tela-do-mercado.md](h-004-tarefa-002-tela-do-mercado.md)
- [h-004-tarefa-003-ouro-passivo-imposto-e-estalagem.md](h-004-tarefa-003-ouro-passivo-imposto-e-estalagem.md)

## Fora de escopo

- Comércio entre jogadores (v2)
- Limites de volume globais entre múltiplos Mercados (cada um independente)
