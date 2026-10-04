# Handoff — Criação da vila (Regiões + População)

Pacote para implementar o fluxo inicial do jogo no `login_base` (frontend Vue 3 + PrimeVue, backend Spring Boot).

## Sobre os arquivos

Os arquivos em `prototipos/` são **referências de design em HTML**: protótipos que mostram a aparência e o comportamento esperados. Não são código de produção. A tarefa é **recriar as telas no frontend existente**, com os padrões e os componentes do projeto, e implementar a lógica no backend.

Para abrir um protótipo, use o navegador. O `support.js` precisa estar na mesma pasta.

**Fidelidade: alta.** Cores, tipografia, espaçamentos e interações são finais. Os dados dos protótipos são gerados localmente; no jogo, eles vêm da API.

## Conteúdo

| Arquivo | Para quê |
|---|---|
| `README.md` | Este índice, o fluxo e a ordem de implementação |
| `PROMPT-CLAUDE-CLI.md` | Prompts prontos para colar no Claude CLI, uma etapa por vez |
| `regras/regras-regioes-v2.md` | Regras do mapa: tipos, sorteio de bônus, escolha das 3 regiões |
| `regras/regras-populacao-v1.md` | Regras da distribuição de pontos, do plano inicial e da família líder |
| `telas/tela-01-criar-vila.md` | Especificação visual e de comportamento da tela 1 |
| `telas/tela-02-distribuir-populacao.md` | Especificação visual e de comportamento da tela 2 |
| `telas/design-tokens.md` | Cores, fontes, raios e componentes comuns |
| `api/contratos-api.md` | Endpoints, payloads, validações e erros |
| `referencia/geracao-mapa.js` | Algoritmo de referência da geração do mapa (para portar para Java) |
| `referencia/distribuicao-populacao.js` | Algoritmo de referência da distribuição automática (para portar para Java) |
| `prototipos/Criar Vila.dc.html` | Protótipo da tela 1 |
| `prototipos/Distribuir Populacao.dc.html` | Protótipo da tela 2 |
| `prototipos/support.js` | Runtime dos protótipos |

## Fluxo

```
/app/jogo/criar-vila                    /app/jogo/distribuir-populacao          /app/jogo/mapa
┌──────────────────────┐   POST vila   ┌──────────────────────────────┐  POST  ┌──────────┐
│ 1. Mapa 4×4 sorteado │ ────────────▶ │ 2. 16 cidadãos pré-distribuí-│ ─────▶ │  Mapa    │
│    escolher 3 regiões│               │    dos; ajustar; família líder│        │          │
│    [Gerar novo mapa] │               └──────────────────────────────┘        └──────────┘
└──────────────────────┘
```

1. **Criar vila:** o servidor gera uma prévia do mapa (tipos e bônus). O jogador pode gerar outra quantas vezes quiser e depois escolhe 3 regiões vizinhas, com pelo menos 1 Urbana.
2. **Distribuir população:** o servidor gera os 16 cidadãos já distribuídos conforme o plano padrão. O jogador ajusta pontos, profissão principal e plano, escolhe a família líder e confirma.
3. Ao confirmar, o jogador vai para o mapa.

## Ordem sugerida de implementação

1. Backend: geração do mapa (porte de `geracao-mapa.js`), com testes das regras R8–R15.
2. Backend: endpoints de prévia e de criação da vila (`contratos-api.md` §1–§3).
3. Frontend: tela 1.
4. Backend: distribuição automática (porte de `distribuicao-populacao.js`), endpoints da população (§4–§5).
5. Frontend: tela 2.
6. Atualizar os documentos de domínio em `docs/jogo/` (lista abaixo).

## Documentos do projeto que precisam ser atualizados

As regras deste pacote **substituem** pontos dos documentos atuais:
- `docs/jogo/v1-008-vila-e-mapa/regioes.md`: tipos de região, jazidas → bônus de região, geração do mapa.
- `docs/jogo/v1-008-vila-e-mapa/historia/h-001-*.md`: o tipo da região passa a ser sorteado; inclui o botão "Gerar novo mapa".
- `docs/jogo/v1-002-cidadaos/cidadao.md`: os limites na criação passam a ser só os totais (20 / 10), sem máximo por atributo.
- `docs/jogo/v1-002-cidadaos/historia/h-001-tarefa-003-*.md`: a tela abre pré-distribuída (não zerada), com plano inicial e troca de profissão principal; os limites mudam.

## Questões em aberto (decidir antes ou durante)

Estão no fim de cada arquivo em `regras/`. As principais:
- O que cada bônus de região faz no jogo (% de produção? quantidade por turno?).
- Plantações × Fazendas (nome final).
- As construções permitidas em cada um dos 5 tipos de região.
- Se haverá limite ou custo para "Gerar novo mapa".
