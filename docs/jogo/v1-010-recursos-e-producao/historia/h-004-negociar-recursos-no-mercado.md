# H-004 — Negociar recursos no mercado

**Épico:** [../recursos.md](../recursos.md) · **Domínio:** [../comercio.md](../comercio.md)

## História

Como comerciante, quero comprar e vender recursos no Mercado com preços que variam conforme meu PE de Comerciante, para ganhar ouro e obter recursos raros.

## Contexto

Mercado NPC com preços dinâmicos. Preço de venda = base × min(1,0; 0,5 + 0,02 × PE Comerciante melhor). Preço de compra = base × max(1,0; 1,5 − 0,02 × PE Comerciante melhor). Volume máximo por turno = 20 × eficiência × mult. nível.

**Referência:** [../comercio.md#regras](../comercio.md#regras) — Mercado R1-R5.

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

## Tarefas

- [h-004-tarefa-001-api-de-compra-e-venda.md](h-004-tarefa-001-api-de-compra-e-venda.md)
- [h-004-tarefa-002-tela-do-mercado.md](h-004-tarefa-002-tela-do-mercado.md)
- [h-004-tarefa-003-ouro-passivo-imposto-e-estalagem.md](h-004-tarefa-003-ouro-passivo-imposto-e-estalagem.md)

## Fora de escopo

- Comércio entre jogadores (v2)
- Limites de volume globais entre múltiplos Mercados (cada um independente)
