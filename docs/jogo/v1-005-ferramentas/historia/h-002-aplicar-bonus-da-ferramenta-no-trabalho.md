# H-002 — Aplicar bônus da ferramenta no trabalho

**Épico:** [ferramentas.md](../ferramentas.md) · **Domínio:** [cidadao.md](../../v1-002-cidadaos/cidadao.md), [construcoes.md](../../v1-003-construcoes/construcoes.md)

## História

Como um jogador, quero que ferramentas equipadas aumentem a eficiência de meus cidadãos, para que trabalhadores com boas ferramentas sejam mais produtivos.

## Contexto

Quando um cidadão trabalha em uma profissão, sua eficiência é calculada como `0,5 + 0,1 × PE efetivo` (seção 5.3). PE efetivo inclui bônus de ferramenta: se a ferramenta equipada é da profissão do prédio, soma +L PE. O bônus **só vale durante o trabalho na profissão correta**.

Referência: [cidadao.md](../../v1-002-cidadaos/cidadao.md) — PE efetivo e eficiência (5.2, 5.3).

## Critérios de aceite

### CA1 — Bônus de ferramenta só aplica na profissão correta
- **Dado** um Agricultor com PE base 2, Enxada L3 equipada, trabalhando em Fazenda de plantio.
- **Quando** o turno processa a produção.
- **Então** PE efetivo = 2 + 3 = 5, eficiência = 0,5 + 0,1 × 5 = 1,0. Produção = 6 Grãos (base) × 1,0 × 1,0 = 6 Grãos.

### CA2 — Bônus não aplica em outra profissão
- **Dado** o mesmo Agricultor com Enxada L3, mas alocado em Armazém (como Carregador).
- **Quando** o turno processa.
- **Então** PE efetivo de Carregador = 1 (base Carregador) + bônus de características (sem Enxada), eficiência reduzida. Enxada não soma.

### CA3 — Eficiência máx. 3,0 com ferramenta
- **Dado** um Agricultor com PE base 10, características INT 15 → +3 bônus, Enxada L10 (+10).
- **Quando** calcula eficiência.
- **Então** PE efetivo bruto = 10 + 3 + 10 = 23, eficiência = 0,5 + 0,1 × 23 = 2,8, limitada a **3,0**. Produção máx.

### CA4 — Ferramenta de nível maior funciona
- **Dado** um Mineiro com PE base 2, Picareta L10 (+10).
- **Quando** trabalha em Mina de ferro.
- **Então** PE efetivo = 2 + 10 = 12, eficiência = 0,5 + 0,1 × 12 = 1,7. Produção = 3 (base) × 1,7 × 1,0 = 5,1 Minério de ferro por trabalhador.

### CA5 — Sem ferramenta, bônus = 0
- **Dado** um Agricultor com PE base 2, sem ferramenta equipada.
- **Quando** trabalha em Fazenda de plantio.
- **Então** PE efetivo = 2 (sem bônus), eficiência = 0,5 + 0,1 × 2 = 0,7. Produção = 6 × 0,7 × 1,0 = 4,2 Grãos.

## Tarefas

- [H-002 · Tarefa 001 — Bônus de ferramenta na eficiência](h-002-tarefa-001-bonus-de-ferramenta-na-eficiencia.md)

## Fora de escopo

- Equipamento de ferramenta (é em cidadao, H-004 de cidadaos).
- Ferramentas de batalha (guerreiros usam armas, não ferramentas).
