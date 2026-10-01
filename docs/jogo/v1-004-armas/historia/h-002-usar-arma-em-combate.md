# H-002 — Usar Arma em Combate

**Épico:** [../armas.md](../armas.md) · **Domínio:** [../armas.md](../armas.md), [../../v1-014-batalha/batalha.md](../../v1-014-batalha/batalha.md)

## História
Como guerreiro em expedição, quero usar minha arma em combate contra inimigos, respeitando as restrições de alcance e posicionamento, para que o combate seja justo e tático.

## Contexto
Cada arma tem restrições de alcance (seção 10.3 da bíblia):
- **Espada:** Corpo a corpo, ataca apenas da linha de frente.
- **Lança:** Corpo a corpo, ataca da frente ou da retaguarda.
- **Arco:** À distância, qualquer alvo (frente ou retaguarda inimiga).
- **Besta:** À distância, qualquer alvo + ignora 25% de Defesa (seção 10.2).

O cálculo de dano já incorpora o ataque base da arma e seu atributo-chave (seção 10.1 e 10.2).

## Critérios de aceite

### CA1 — Espada só ataca da frente
- **Dado** guerreiro com Espada L1 na posição Retaguarda, inimigo Goblin na frente.
- **Quando** a batalha está em andamento e é a vez do guerreiro atacar (iniciativa).
- **Então** o alvo permitido é apenas entre os inimigos da linha de frente; retaguarda inimiga é bloqueada com mensagem "Espada ataca apenas corpo a corpo da frente".

### CA2 — Lança ataca da frente ou da retaguarda
- **Dado** guerreiro com Lança L3 na posição Retaguarda, inimigos Goblins.
- **Quando** é a vez do guerreiro atacar.
- **Então** a lança pode mirar qualquer inimigo vivo da frente, sem bloqueio por posição do portador.

### CA3 — Arco ataca qualquer alvo
- **Dado** guerreiro com Arco L1 na posição qualquer (frente ou retaguarda), inimigos em ambas as linhas.
- **Quando** é a vez do guerreiro atacar.
- **Então** o arco pode mirar qualquer inimigo vivo, frente ou retaguarda, sem restrição.

### CA4 — Besta ignora 25% da defesa
- **Dado** guerreiro com Besta L5 (INT 7, G 8) contra Esqueleto (DEF 14 × M = 22,4).
- **Quando** ataca e o dano é calculado (seção 10.2).
- **Então** Defesa_efetiva = 22,4 × 0,75 = 16,8; dano = ataque × 100 ÷ (100 + 3 × 16,8), resultado é ~33% maior do que seria sem o modificador.

### CA5 — Modificador de iniciativa é aplicado
- **Dado** guerreiro com Arco (mod +2) e outro com Espada (mod 0), ambos VEL 5.
- **Quando** iniciativa é sorteada no começo da batalha.
- **Então** Iniciativa_Arco = 2 × 5 + 2 = 12; Iniciativa_Espada = 10; arqueiro age primeiro.

### CA6 — Escolha de alvo correto para cada arma
- **Dado** tropa do jogador com Espada (frente) e Arco (qualquer posição) vs. inimigos em linha dupla.
- **Quando** a batalha resolve os ataques da tropa do jogador (estratégia: mirar inimigo de menor PV permitido).
- **Então** Espada só ataca inimigos da frente; Arco ataca o de menor PV de qualquer linha.

## Tarefas
- [h-002-tarefa-001-regras-de-alcance-e-modificadores-de-arma.md](h-002-tarefa-001-regras-de-alcance-e-modificadores-de-arma.md)

## Fora de escopo
- Knockback ou efeitos ambientais; fora desta versão.
- Armas especiais (mágicas); fora da v1.
- Manual de posicionamento tático; fica para o manual de ajuda do frontend.
