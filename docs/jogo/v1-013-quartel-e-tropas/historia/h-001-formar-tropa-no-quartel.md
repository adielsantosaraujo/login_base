# H-001 — Formar tropa no quartel

**Épico:** [../tropas.md](../tropas.md) · **Domínio:** [../tropas.md](../tropas.md), [../../v1-002-cidadaos/cidadao.md](../../v1-002-cidadaos/cidadao.md)

## História

Como jogador, quero formar uma tropa escolhendo guerreiros e suas posições, para depois enviá-la em expedições.

## Contexto

O quartel é um prédio urbano onde tropas são formadas. Para um cidadão fazer parte de uma tropa, precisa atender critérios rigorosos: ter arma equipada, PE de Guerreiro base ≥ 1, estar vivo, ter 16–54 anos e não estar em outra tropa (seção 9.1).

Cada membro tem uma **posição**: Frente (pode atacar à distância ou corpo a corpo se não houver retaguarda) ou Retaguarda (ataques à distância, usa besta ou arco). A formação é flexível: o jogador organiza a tropa dinamicamente.

A capacidade total de tropas depende do nível do quartel (seção 4.12, tabela em domínio tropas.md R1–R6).

## Critérios de aceite

### CA1 — Cidadão sem arma é rejeitado
- **Dado** um quartel N1 com 1 instrutor alocado, e um guerreiro sem arma equipada.
- **Quando** o jogador tenta adicioná-lo à tropa em formação.
- **Então** a API retorna erro 400 "Cidadão não tem arma equipada" e o guerreiro não é adicionado.

### CA2 — Cidadão sem PE Guerreiro base ≥ 1 é rejeitado
- **Dado** um cidadão com PE base de Guerreiro = 0, arma equipada, idade 20 anos.
- **Quando** o jogador tenta adicioná-lo à tropa.
- **Então** a API retorna erro 400 "PE Guerreiro insuficiente" (exige ≥ 1 base).

### CA3 — Quartel sem instrutor bloqueia formação
- **Dado** um quartel N1 sem nenhum instrutor alocado.
- **Quando** o jogador tenta criar uma tropa.
- **Então** a tela mostra mensagem "Quartel sem instrutor" e bloqueia a ação.

### CA4 — Limite de capacidade é respeitado
- **Dado** um quartel N1 (capacidade 5 membros, 1 tropa), 5 guerreiros já na tropa.
- **Quando** o jogador tenta adicionar um 6º guerreiro.
- **Então** a API retorna erro 400 "Capacidade do quartel excedida" e a tropa permanece com 5 membros.

### CA5 — Tropa com posições definidas é criada com sucesso
- **Dado** um quartel N1 com instrutor, 3 guerreiros elegíveis (arma, PE ≥ 1, 16–54 anos, não em tropa).
- **Quando** o jogador forma tropa com 2 em Frente e 1 em Retaguarda.
- **Então** a API retorna sucesso 200, tropa criada com estado AQUARTELADA, 3 membros nas posições informadas.

### CA6 — Cidadão em outra tropa é rejeitado
- **Dado** um guerreiro já membro de Tropa A.
- **Quando** o jogador tenta adicioná-lo a Tropa B.
- **Então** a API retorna erro 400 "Cidadão já está em tropa" e não é adicionado.

## Tarefas

- [h-001-tarefa-001-modelo-e-regras-de-tropa.md](h-001-tarefa-001-modelo-e-regras-de-tropa.md)
- [h-001-tarefa-002-tela-do-quartel.md](h-001-tarefa-002-tela-do-quartel.md)

## Fora de escopo

- Renomear troupa
- Desfazer tropa após enviada (só aquartelada)
- Transferência de membros entre tropas
