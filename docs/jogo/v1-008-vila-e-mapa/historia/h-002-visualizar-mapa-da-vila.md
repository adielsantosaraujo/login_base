# H-002 — Visualizar mapa da vila

**Épico:** [../vila.md](../vila.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md)

## História

Como jogador, quero visualizar a grade 4×4 das minhas regiões com indicação de posse, tipo, composição de terrenos e masmorras, e poder clicar em uma região para expandir e ver os 10×10 ladrilhos com terreno, endereço e construções de cada ladrilho.

## Contexto

- A grade principal mostra 16 regiões (4×4) com tipo e composição dos 3 terrenos (%) visíveis.
- Células possuídas mostram tipo (Floresta, Planície, Urbana, Litoral, Montanha) e ícone/cor.
- Células não possuídas também mostram tipo e composição de terrenos sorteados na criação.
- Células com masmorra ativa mostram ícone de masmorra + nível.
- Ao clicar numa região, expande-se uma subgrada 10×10 mostrando ladrilhos.
- Cada ladrilho exibe: terreno (sigla de 2 letras + cor), bonus_base/adjacente/total, construção, marcação de coleta.
- Ladrilhos de todas as regiões (inclusive Urbana) são mostrados com terreno, endereço e construções.
- Telas 1, 3, 4 do roadmap (11.4).

## Critérios de aceite

### CA1 — Grade 4×4 mostra regiões com tipo, composição de terrenos e cor

- **Dado** um jogador com vila contendo regiões 6 (Urbana), 7 (Floresta), 2 (Montanha)
- **Quando** acessa a tela de mapa
- **Então** a grade mostra 16 células (3 possuídas, 13 não possuídas) com cores/ícones para os tipos; tipo e composição dos 3 terrenos (%) visíveis em cada célula; clickable

### CA2 — Clique numa região mostra 10×10 ladrilhos com terreno e construções

- **Dado** a grade 4×4 já exibida, região 6 (Urbana) selecionada
- **Quando** o jogador clica em região 6
- **Então** a tela expande ou abre subgrada 10×10; mostra ladrilhos com sigla do terreno e cor; 4 casas N1 nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura e todas em terreno Desenvolvimento; tipo e composição de terrenos da região exibidos

### CA3 — Célula com masmorra mostra ícone e nível

- **Dado** região 10 (não possuída) com masmorra ativa nível 5
- **Quando** visualiza a grade
- **Então** célula 10 exibe tipo, composição de terrenos, ícone de masmorra + "N5" sobreposto

### CA4 — Navegação entre regiões sem recarregar

- **Dado** subgrada 10×10 de região 6 aberta
- **Quando** o jogador clica no ícone de região 7 (vizinha) no minicabeçalho
- **Então** subgrada muda para 10×10 de região 7, sem recarregar a página

### CA5 — Legenda de tipos e símbolos

- **Dado** a tela de mapa
- **Quando** o jogador vê o lado ou sobrepõe ícone
- **Então** legenda exibe: "Floresta" (cor verde), "Planície" (cor bege), "Urbana" (cor amarela), "Litoral" (cor azul), "Montanha" (cor cinza), "Masmorra" (ícone caverna), "Construção" (ícone prédio), "Siglas dos 13 terrenos com cores" (tabela de regioes.md), etc.

### CA6 — Cada ladrilho exibe endereço e informações de bônus

- **Dado** uma região aberta com 10×10 ladrilhos visíveis
- **Quando** o jogador observa um ladrilho ou sobrepõe o cursor
- **Então** endereço (A,1) aparece acima da sigla do terreno; tooltip mostra endereço, terreno, bonus_base, bonus_adjacente e bonus_total

## Tarefas

- [h-002-tarefa-001-api-do-mapa-da-vila.md](h-002-tarefa-001-api-do-mapa-da-vila.md)
- [h-002-tarefa-002-telas-do-mapa-e-da-regiao.md](h-002-tarefa-002-telas-do-mapa-e-da-regiao.md)

## Fora de escopo

- Zoom/pan interativo (básico com clique).
- Miniatura de región 10×10 em grade; só números e cores.
- Drag-and-drop de regiões.
