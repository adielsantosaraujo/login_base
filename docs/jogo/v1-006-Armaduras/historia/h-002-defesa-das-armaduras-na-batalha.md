# H-002 — Defesa das armaduras na batalha

**Épico:** [../armaduras.md](../armaduras.md) · **Domínio:** [../armaduras.md](../armaduras.md)

## História

Como guerreiro equipado com armaduras, quero que a defesa de minhas peças reduza o dano que recebo em combate, proporcionalmente ao nível e quantidade de peças.

## Contexto

A defesa de um guerreiro é calculada como a soma das defesas individuais de cada peça de armadura equipada. A fórmula de dano (seção 10.2) usa a defesa total para reduzir o dano do atacante. Algumas armas especiais (Besta, Xamã orc) ignoram 25% da defesa (seção 10.2) [proposta].

Defesa total = Σ DEFpeça(L) para cada peça equipada (seção 10.1) [proposta].

Fórmula de dano: `dano = max(1; round(Ataque × 100 ÷ (100 + 3 × Defesa_efetiva) × U(0,9; 1,1)))` (seção 10.2) [proposta].
Se atacante é Besta ou Xamã orc: Defesa_efetiva = Defesa × 0,75.

## Critérios de aceite

### CA1 — Soma de defesa com conjunto completo L1

- **Dado** um guerreiro equipado com Peitoral L1, Capacete L1, Ombreiras L1, Luvas L1, Calças L1, Sapato L1
- **Quando** a defesa é calculada
- **Então** defesa total = 8 + 4 + 3 + 2 + 5 + 2 = 24

### CA2 — Defesa com nível maior (L5)

- **Dado** o mesmo guerreiro com todas as peças L5 (fórmula: defesa(L) = base × 1,8 para L5)
- **Quando** a defesa é calculada
- **Então** defesa total = (8+4+3+2+5+2) × 1,8 = 24 × 1,8 = 43,2

### CA3 — Dano reduzido por defesa

- **Dado** um guerreiro com Defesa 24 (conjunto L1) recebendo ataque de inimigo com Ataque 25
- **Quando** o dano é calculado: `dano = max(1; round(25 × 100 ÷ (100 + 3 × 24) × U(0,9; 1,1)))`
- **Então** dano ≈ 8–11 (com variação aleatória), significativamente reduzido vs. sem armadura (~23 dano)

### CA4 — Defesa parcial com peças faltando

- **Dado** um guerreiro com apenas Peitoral L1 (8 DEF), sem as demais peças
- **Quando** a defesa é calculada
- **Então** defesa total = 8 (só a soma das equipadas)

### CA5 — Besta ignora 25% de defesa

- **Dado** um guerreiro com Defesa 40, atacado por Besta com Ataque 28
- **Quando** a defesa efetiva é calculada para dano: Defesa_efetiva = 40 × 0,75 = 30
- **Então** dano = max(1; round(28 × 100 ÷ (100 + 3 × 30) × U(0,9; 1,1))) ≈ 11–15 (maior que se fosse ataque normal)

### CA6 — Xamã orc também ignora 25%

- **Dado** um guerreiro com Defesa 30, atacado por Xamã orc (ataque à distância, ignora 25% DEF)
- **Quando** a defesa efetiva é usada
- **Então** Defesa_efetiva = 30 × 0,75 = 22,5

## Tarefas

- [H-002 · Tarefa 001 — Cálculo de defesa com armaduras](h-002-tarefa-001-calculo-de-defesa-com-armaduras.md)

## Fora de escopo

- Outros atributos de combate (Ataque, Iniciativa, Crítico) — ficam no Epic de Batalha.
- Efeitos de bônus intrínsecos de armaduras (VIT, DEF% extra) — ficam em v1-011.
- Interface visual de combate — fica em tarefa de relatório/replay de batalha.
