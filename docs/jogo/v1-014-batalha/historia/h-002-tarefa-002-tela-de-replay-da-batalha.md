# H-002 · Tarefa 002 — Tela de replay da batalha

**História:** [H-002 — Ver relatório de batalha](h-002-ver-relatorio-de-batalha.md) · **Domínio:** [batalha.md](../batalha.md) ·
**Depende de:** [H-002 · Tarefa 001 — API de relatório de batalha](h-002-tarefa-001-api-de-relatorio-de-batalha.md) · **Camada:** Frontend

## Objetivo

Implementar tela Vue + PrimeVue para visualizar histórico de batalhas e replay rodada a rodada, com recompensas resumidas.

## Contexto necessário

- Estrutura de dados: Batalha { resultado, masmorra_nivel, rodadas: [...], recompensas: {...} }
- API: GET /api/jogo/batalhas, GET /api/jogo/batalhas/{id}
- Recompensas (seção 8.4): ouro, recursos (lista), itens, XP, pedras

## Frontend

**Componente**: `BatalhaReplay.vue` (em `/frontend/src/jogo/BatalhaReplay.vue`)

Funcionalidades:
1. **Lista de batalhas**: tabela com colunas data (turno), masmorra (N), resultado (cor: verde VITORIA, vermelho DERROTA), ação (botão "Ver").

2. **Detalhe de uma batalha**:
   - Cabeçalho: resultado, masmorra nível, data
   - Abas:
     a. **Rodada a rodada** (tab selecionada por padrão)
        - Seletor de rodada (dropdown ou prev/next)
        - Para cada ação da rodada exibir:
          - "Atacante (AT, DEF, PV) ataca Alvo"
          - "Dano: XX" (crítico em vermelho se houver)
          - "Alvo (PV antes → PV depois)"
        - Indicador visual: "Alvo abatido" se PV = 0
        
     b. **Recompensas** (tab)
        - Ouro: XX
        - Recursos: lista (ícone + Recurso + quantidade)
        - Itens: lista (nome, L, qualidade, bônus)
        - XP: "N pontos para cada sobrevivente"
        - Pedras: lista (qualidade, bônus)
        - Se derrota: "Sem recompensas"

**Rutas**: 
- Integrar em menu principal (ex.: "Histórico de batalhas")
- Path: `/jogo/batalhas`

**API calls**:
- `GET /api/jogo/batalhas` → lista batalhas
- `GET /api/jogo/batalhas/{id}` → detalhe com rodadas e recompensas

## Arquivos prováveis

- [/frontend/src/jogo/BatalhaReplay.vue](/frontend/src/jogo/BatalhaReplay.vue) (novo)
- [/frontend/src/jogo/components/ListaBatalhas.vue](/frontend/src/jogo/components/ListaBatalhas.vue) (novo)
- [/frontend/src/jogo/components/RodadaReplay.vue](/frontend/src/jogo/components/RodadaReplay.vue) (novo)
- [/frontend/src/jogo/components/RecompensasBatalha.vue](/frontend/src/jogo/components/RecompensasBatalha.vue) (novo)
- [/frontend/src/services/batalhaService.ts](/frontend/src/services/batalhaService.ts) (novo)

## Testes

1. **Carga de lista**: GET /api/jogo/batalhas retorna 3 batalhas
   - Esperado: tabela exibe 3 linhas, resultado com cores corretas

2. **Navegação**: clica em "Ver" de uma batalha
   - Esperado: abre detalhe, aba "Rodada a rodada" selecionada, rodada 1 visível

3. **Replay rodada a rodada**: exibe rodada 2 (2 ações)
   - Esperado: "Guerreiro ataca Goblin por 23", "Goblin ataca Guerreiro por 11"
   - PVs atualizados corretamente

4. **Crítico visível**: ação com dano crítico
   - Esperado: texto em vermelho ou destaque especial "(crítico)"

5. **Recompensas**: aba "Recompensas" em vitória
   - Esperado: ouro, lista de recursos, itens, XP, pedras exibidos

6. **Derrota sem recompensas**: aba "Recompensas" em derrota
   - Esperado: mensagem "Sem recompensas nesta derrota"

7. **Navegação de rodadas**: botão prev/next entre rodadas
   - Esperado: rodada decresce/cresce; primeira rodada desabilita prev, última desabilita next

## Definição de pronto

- Critérios de aceite da história cobertos: CA1, CA2, CA3, CA4, CA5
- Build do frontend (`npm run build`) sem erros
- Testes listados passando
- Componentes reutilizáveis (ex.: RodadaReplay para futura integração em outras telas)
- Responsivo em mobile (abas, overflow de tabelas)
- Integração com [API de relatório](h-002-tarefa-001-api-de-relatorio-de-batalha.md)

## Fora de escopo

- Animações de dano/movimento
- Replay em tempo real
- Impressão de relatório
- Filtros avançados
