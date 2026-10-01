# H-002 · Tarefa 002 — Barra do turno e relatório

**História:** [H-002 — Acompanhar relatório do turno](h-002-acompanhar-relatorio-do-turno.md) · **Domínio:** [Turnos](../turnos.md) · **Depende de:** [H-002 · Tarefa 001](h-002-tarefa-001-registro-de-eventos-do-turno.md) · **Camada:** Frontend

## Objetivo

Implementar os componentes Vue/PrimeVue para exibir a barra do turno (número + contagem regressiva) e o painel de relatório com eventos filtráveis.

## Contexto necessário

- Seção 11.4 — Telas frontend
  > 12. Barra do turno (número, contagem regressiva) e relatório do turno.

- Endpoints da tarefa 001:
  - `GET /api/jogo/turno`
  - `GET /api/jogo/turno/eventos`

## Backend

Não se aplica.

## Frontend

- **Componente `BarraTurno.vue`:**
  - Exibe número do turno grande e visível.
  - Contagem regressiva até o próximo turno (atualiza a cada segundo).
  - Estilo: fundo contrastante (ex.: azul escuro) com branco; fixo no topo da tela.
  - Chama `GET /api/jogo/turno` a cada 10 segundos ou ao abrir a tela.

- **Componente `RelatorioDaTurno.vue`:**
  - Lista de eventos em tabela ou cards.
  - Colunas/campos: tipo (com ícone), descrição, timestamp.
  - Filtro por tipo (dropdown: Todos, Nascimento, Morte, Produção, Masmorra, Ferido, Recuperado).
  - Botão "Carregar anteriores" para consultar turnos passados.
  - Chama `GET /api/jogo/turno/eventos` com filtro opcional.

- **Ícones por tipo de evento:**
  - NASCIMENTO → bebê/berço.
  - MORTE → caveira/lápide.
  - PRODUCAO → colheita/trigo.
  - MASMORRA → dragão/caverna.
  - FERIDO → bandagem.
  - RECUPERADO → coração.
  - (Usar PrimeVue Icons ou FontAwesome.)

- **Rota:**
  - Turno integrado em `/jogo` (parte do layout principal).
  - Relatório acessível via aba ou modal.

## Arquivos prováveis

- [/frontend/src/components/jogo/BarraTurno.vue](/frontend/src/components/jogo/BarraTurno.vue) (novo)
- [/frontend/src/components/jogo/RelatorioDaTurno.vue](/frontend/src/components/jogo/RelatorioDaTurno.vue) (novo)
- [/frontend/src/views/Jogo.vue](/frontend/src/views/Jogo.vue) (existente, integrar componentes)

## Testes

- Teste de componente: mock de API; verificar que número e contagem são exibidos.
- Teste de filtro: simular múltiplos tipos de evento; validar filtro.
- Teste de atualização: aguardar 10 segundos; verificar que turno é refetch.

## Definição de pronto

- Critérios de aceite CA1, CA2, CA3 (barra, lista, filtro).
- Componentes renderizam sem erros.
- Build (`npm run build`) sem avisos.
- Responsivo em mobile.

## Fora de escopo

- Animações complexas.
- Histórico de múltiplos turnos em uma única página (paginação básica OK).
