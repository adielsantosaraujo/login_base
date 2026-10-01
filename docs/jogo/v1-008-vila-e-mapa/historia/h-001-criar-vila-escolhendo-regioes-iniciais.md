# H-001 — Criar vila escolhendo regiões iniciais

**Épico:** [../vila.md](../vila.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md)

## História

Como novo jogador, quero escolher 3 regiões iniciais e seus tipos para começar meu jogo.

## Contexto

- Cada usuário autenticado tem exatamente 1 vila.
- Na criação, o jogador escolhe 3 regiões (índices 1-16 da grade 4×4).
- A 1ª região pode ser qualquer uma; a 2ª e 3ª devem ser adjacentes (ortogonalmente) a uma região já escolhida.
- Ao menos 1 das 3 deve ser **Urbana**.
- Cada região recebe um tipo (Rural, Urbana ou Coleta) que é definitivo na v1.
- A vila é criada com uma semente aleatória que determina as jazidas de todos os ladrilhos.
- A vila recebe 4 casas N1 iniciais na 1ª região Urbana escolhida.
- A vila recebe recursos iniciais: Madeira 200, Pedra 100, Argila 50, Tábua 20, Grãos 200, Carne 40, Ouro 200.

## Critérios de aceite

### CA1 — Escolha de 3 regiões adjacentes é aceita

- **Dado** um novo usuário autenticado sem vila
- **Quando** o usuário submete a escolha: região 6 (Urbana), região 7 (Urbana), região 3 (Rural)
- **Então** a vila é criada com essas 3 regiões; região 7 é adjacente a 6 (✓); região 3 é adjacente a 7 (✓); pelo menos 1 é Urbana (✓)

### CA2 — Terceira região não adjacente é rejeitada

- **Dado** um novo usuário com a tela de criação mostrando regiões 6 e 7 (ambas Urbanas) selecionadas
- **Quando** o usuário tenta selecionar região 1 (Rural) como 3ª
- **Então** a seleção é rejeitada (região 1 não é adjacente a 6 nem a 7); mensagem de erro: "Região deve ser adjacente a uma já selecionada"

### CA3 — Escolha sem região Urbana é rejeitada

- **Dado** um novo usuário tentando escolher regiões 5 (Rural), 6 (Rural), 9 (Coleta)
- **Quando** o usuário submete a escolha
- **Então** a vila não é criada; mensagem de erro: "Ao menos uma região deve ser Urbana"

### CA4 — Usuário com vila não cria outra

- **Dado** um usuário autenticado que já tem uma vila criada (id = 123)
- **Quando** o usuário tenta acessar novamente a tela de criação de vila
- **Então** a tela redireciona para o mapa da sua vila existente

### CA5 — Recursos iniciais e casas iniciais são criados

- **Dado** a vila foi criada com região 6 (Urbana), região 7 (Rural), região 2 (Coleta)
- **Quando** o sistema processa a criação
- **Então** o estoque da vila contém: Madeira 200, Pedra 100, Argila 50, Tábua 20, Grãos 200, Carne 40, Ouro 200; e 4 casas N1 existem na região 6, nos ladrilhos (0,0), (2,0), (4,0), (6,0)

### CA6 — Prévia das jazidas antes de escolher

- **Dado** um novo usuário na tela de criação de vila
- **Quando** o usuário vê a grade 4×4 com cores/ícones indicando jazidas
- **Então** as jazidas visíveis refletem a semente temporária (mesma para toda a exibição) e correspondem à geração determinística

## Tarefas

- [h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md)
- [h-001-tarefa-002-geracao-de-jazidas-por-semente.md](h-001-tarefa-002-geracao-de-jazidas-por-semente.md)
- [h-001-tarefa-003-api-de-criacao-da-vila.md](h-001-tarefa-003-api-de-criacao-da-vila.md)
- [h-001-tarefa-004-tela-de-criacao-da-vila.md](h-001-tarefa-004-tela-de-criacao-da-vila.md)

## Fora de escopo

- Não há limite de regiões a escolher durante a criação além das 3 iniciais obrigatórias.
- Edição/anulação de vila após criação.
- Migração de dados de vila para outro usuário.
