# H-002 — Visualizar mapa da vila

**Épico:** [../vila.md](../vila.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md)

## História

Como jogador, quero visualizar a grade 4×4 das minha regiões com indicação de posse, tipo, masmorras, e poder clicar em uma região para expandir e ver os 10×10 ladrilhos com construções e jazidas.

## Contexto

- A grade principal mostra 16 regiões (4×4).
- Células possuídas mostram tipo (Rural/Urbana/Coleta) e ícone/cor.
- Células vazias mostram como disponíveis para anexação.
- Células com masmorra ativa mostram ícone de masmorra + nível.
- Ao clicar numa região, expande-se uma subgrada 10×10 mostrando ladrilhos.
- Cada ladrilho pode ter: jazida, construção, recurso (marcação de coleta).
- Telas 1, 3, 4 do roadmap (11.4).

## Critérios de aceite

### CA1 — Grade 4×4 mostra regiões possuídas com tipo e cor

- **Dado** um jogador com vila contendo regiões 6 (Urbana), 7 (Rural), 2 (Coleta)
- **Quando** acessa a tela de mapa
- **Então** a grade mostra 3 células preenchidas (índices 6, 7, 2) com cores/ícones para os tipos; 13 células vazias (índice visível); clickable

### CA2 — Clique numa região mostra 10×10 ladrilhos com jazidas e construções

- **Dado** a grade 4×4 já exibida, região 6 (Urbana) selecionada
- **Quando** o jogador clica em região 6
- **Então** a tela expande ou abre subgrada 10×10; mostra ladrilhos com ícones (Floresta, Pedra, etc. se Coleta; prédios se construído); 4 casas N1 nos ladrilhos (0,0), (2,0), (4,0), (6,0)

### CA3 — Célula com masmorra mostra ícone e nível

- **Dado** região 10 (não possuída) com masmorra ativa nível 5
- **Quando** visualiza a grade
- **Então** célula 10 exibe ícone de masmorra + "N5" sobreposto

### CA4 — Navegação entre regiões sem recarregar

- **Dado** subgrada 10×10 de região 6 aberta
- **Quando** o jogador clica no ícone de região 7 (vizinha) no minicabeçalho
- **Então** subgrada muda para 10×10 de região 7, sem recarregar a página

### CA5 — Legenda de tipos e símbolos

- **Dado** a tela de mapa
- **Quando** o jogador vê o lado ou sobrepõe ícone
- **Então** legenda exibe: "Rural" (cor verde), "Urbana" (cor azul), "Coleta" (cor marrom), "Masmorra" (ícone caverna), "Construção" (ícone prédio), etc.

## Tarefas

- [h-002-tarefa-001-api-do-mapa-da-vila.md](h-002-tarefa-001-api-do-mapa-da-vila.md)
- [h-002-tarefa-002-telas-do-mapa-e-da-regiao.md](h-002-tarefa-002-telas-do-mapa-e-da-regiao.md)

## Fora de escopo

- Zoom/pan interativo (básico com clique).
- Miniatura de región 10×10 em grade; só números e cores.
- Drag-and-drop de regiões.
