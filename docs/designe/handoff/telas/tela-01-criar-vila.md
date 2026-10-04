# Tela 01 — Criar minha vila

**Rota:** `/app/jogo/criar-vila` · **Protótipo:** `../prototipos/Criar Vila.dc.html` · **Regras:** `../regras/regras-regioes-v2.md` · **API:** `../api/contratos-api.md` §1–§3 · **Tokens:** `design-tokens.md`

## Layout
- Header comum (aba "Mapa" ativa).
- `main`: max-width 1180px, centralizado, padding 36px 28px 48px, gap 28px.
  - Sobretítulo "INÍCIO DE JOGO", o H1 "Criar minha vila" e o parágrafo: "Escolha 3 regiões vizinhas — ao menos 1 Urbana — para fundar sua vila. Os bônus das regiões escolhidas definem o ponto de partida da vila."
  - Linha flex com quebra e gap de 28px:
    - **Mapa** (flex 1 1 560px)
    - **Painel** (flex 1 1 320px, max 400px, sticky com top 20px). Em telas estreitas, desce para baixo do mapa.

## Mapa
- **Legenda**: os 5 tipos, cada um com um quadrado de 8px na cor do tipo.
- **Grade**: 4 colunas, gap 10px, padding 10px, raio 16px, fundo `--surface-1`.
- **Tile** (botão): raio 12px, padding 12px, altura mínima 150px.
  - Topo: número "01"–"16" (Mono 13px) e chip do tipo (11px 600, maiúsculas, fundo na cor do tipo).
  - Base: os **3 bônus** da região, do maior para o menor. Cada linha tem o nome (12px), uma barra de 3px (largura = valor ÷ 50) e o valor em Mono.
  - Estados:
    - Normal: fundo `--surface-3` e borda 1px `--border`.
    - Hover: `translateY(-2px)`.
    - Selecionado: borda 2px `--accent`, fundo `--accent-bg` e um selo de 24px com a ordem (1–3) no canto superior direito.
    - Indisponível (não vizinho, ou já há 3 selecionados): opacidade .4 e cursor `not-allowed`.
- **Dica** (13px, centralizada), conforme o estado:
  - Nenhuma selecionada: "Clique em uma região para começar."
  - Parcial: "Regiões destacadas são vizinhas da sua seleção."
  - Válida: "Tudo certo — crie sua vila."
  - Sem Urbana: "Inclua ao menos uma região Urbana."
  - Desconectadas: "As regiões precisam ser vizinhas."
- **Botão "↻ Gerar novo mapa"**: pílula de 42px, centralizada. Abaixo, "MAPA Nº X" (Mono 11px). Ao clicar: chama `POST /previa`, limpa a seleção e mostra estado de carregamento enquanto aguarda.

## Painel "Sua seleção"
1. Título e o botão de texto "Limpar".
2. **3 slots** (64px):
   - Vazio: borda tracejada, com "—" e "VAZIO".
   - Preenchido: número e tipo, na cor do tipo.
3. **Checklist**:
   - "3 regiões" (n/3)
   - "Vizinhas entre si"
   - "Ao menos 1 Urbana"
4. **Bônus de região** ("soma das regiões"): os 13 bônus. Cada um tem uma barra de 6px (largura = soma ÷ 150) e o valor. Bônus com valor 0 ficam esmaecidos.
5. **CTA**:
   - Válido: "Criar vila".
   - Inválido: "Selecione 3 regiões válidas".
   - Ao clicar: `POST /api/jogo/vila`, com spinner, e depois redireciona para `/app/jogo/distribuir-populacao`. Se der erro, mostra um toast com a mensagem do backend.

## Card "Em foco"
- Mostra a região em hover (ou a última selecionada) com o título "Região 06 · Urbana" e os 3 bônus com valores.
- Sem região em foco: "Passe o mouse numa região".

## Estado (frontend)
- `previa` (resposta da API) e `rodada`
- `selecionadas: number[]` (na ordem de seleção) e `hover: number | null`
- Derivados:
  - `podeSelecionar(i)`: há menos de 3 selecionadas, e a seleção está vazia ou `i` é vizinha de alguma selecionada.
  - `conectado`: as selecionadas formam um grupo ligado (busca em largura, BFS, ortogonal).
  - `temUrbana`, `valido`
  - `totaisBonus`: soma de cada bônus nas selecionadas.
- Clicar numa selecionada a remove.

## Componentes Vue sugeridos
`CriarVilaView.vue`, `MapaGrade.vue`, `RegiaoTile.vue`, `SelecaoPainel.vue`, `BonusLista.vue`, `RegiaoFoco.vue`.
