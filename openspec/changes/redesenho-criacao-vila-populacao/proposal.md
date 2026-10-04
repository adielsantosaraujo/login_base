# Proposal

## Why

O pacote do Claude Design em `docs/designe/handoff/` redesenha o início do jogo: o mapa passa a ser sorteado pelo servidor com 5 tipos de região e 3 bônus de região por região, a população abre pré-distribuída por um plano inicial e o visual muda para um tema escuro ("Vilarejo"). As regras atuais (3 tipos escolhidos pelo jogador, jazidas como "recursos" da região, máximo 10/5 por atributo, tela zerada, tema Aura claro) ficam incompatíveis. O usuário decidiu ainda que os bônus de região têm efeito já nesta change (% de produção), que as construções permitidas derivam do bônus do tipo e que o tema novo vale para o jogo inteiro. O banco será recriado limpo.

## What Changes

- **BREAKING** Tipos de região passam de `RURAL | URBANA | COLETA` para `FLORESTA | PLANICIE | URBANA | LITORAL | MONTANHA`, definidos pela geração do mapa (o jogador não escolhe tipo). Cada região tem 3 bônus de região (13 bônus possíveis) com valores 35–50 / 16–34 / 5–15.
- Novo gerador do mapa por semente (porte de `referencia/geracao-mapa.js`) e prévia persistida por usuário (`vila_previa`) com "Gerar novo mapa" ilimitado.
- **BREAKING** `GET /api/jogo/vila/preview` removido; novos `POST/GET /api/jogo/vila/previa`; `POST /api/jogo/vila` passa a receber `{previaId, indices}` e responde `{vilaId, proximaEtapa}`.
- **BREAKING** `GET/POST /api/jogo/vila/populacao` com contrato novo: sugestão pelo plano padrão (calculada, não persistida), papel derivado, `plano`/`minimos`/`limites`, `familiaLiderSugeridaId`; confirmação com valores absolutos dos 16 cidadãos, sem máximo por atributo (só totais 20/10), mínimos de 2 Construtores e 2 Carregadores principais e `409 POPULACAO_JA_CONFIRMADA`.
- Erros da API do jogo passam a ter `codigo` além de `erro`: `{ "erro": "...", "codigo": "..." }`.
- Bônus de região com efeito: bônus da vila = soma das regiões possuídas; cada ponto = +1% na produção ligada (coleta, fazendas, fábricas, ouro passivo, obras e treino).
- **BREAKING** Construções permitidas derivadas do bônus do tipo da região (catálogo expõe `regioes: []` em vez de `regiao`); prédios urbanos só em Urbana.
- **BREAKING** Anexação não recebe mais `tipo`: a região mantém o tipo e os bônus sorteados.
- Mapa e resumo da vila exibem tipo e bônus das 16 regiões e o total de bônus da vila.
- O turno deixa de processar vilas com população não confirmada.
- Frontend: telas "Criar minha vila" e "Distribuir a população" refeitas (alta fidelidade); rota `/jogo/distribuir-populacao` (redirect de `/jogo/populacao`); distribuição portada para TypeScript com fixture de paridade compartilhada com o teste Java; `ApiError` com `status` e `codigo`.
- Frontend: tema "Vilarejo" (tokens `--vl-*`, fontes Bricolage Grotesque / IBM Plex Sans / IBM Plex Mono, preset PrimeVue derivado do Aura em modo escuro, cabeçalho "Vilarejo") aplicado a **todas** as telas do jogo.
- **BREAKING (dados)** Migration V17 sem retrocompatibilidade de dados: exige banco limpo.
- Documentos de domínio em `docs/jogo/**` atualizados (regiões v2, vila, população, construções, produção, turnos).

## Capabilities

### New Capabilities

- `jogo-criacao-vila`: geração do mapa por semente, prévia por usuário, criação da vila a partir da prévia, erros com código, tela "Criar minha vila" e guardas de rota do início de jogo.
- `jogo-populacao-inicial`: distribuição automática pelo plano, consulta e confirmação da população inicial, turno que ignora vila pendente e tela "Distribuir a população".
- `jogo-bonus-regiao`: bônus de região da vila, efeito dos bônus na produção, construções permitidas pelo tipo da região, exibição de tipo/bônus no mapa e anexação que mantém tipo e bônus.
- `jogo-ui-tema`: tokens de design, preset PrimeVue "Vilarejo" escuro, cabeçalho "Vilarejo" e migração visual de todas as telas do jogo.

### Modified Capabilities

- `frontend-app`: o PrimeVue deixa de usar o Aura puro (passa ao preset "Vilarejo" derivado do Aura, modo escuro) e a aplicação deixa de ter "uma única página" (rotas do jogo).

## Impact

- Backend: `jogo/modelo` (TipoRegiao, BonusRegiao novo, FaixaBonusRegiao novo, Regiao, RegiaoBonus novo, VilaPrevia novo), `jogo/servico` (GeradorMapaService novo, VilaPreviaService novo, BonusRegiaoService novo, VilaService, MapaService, AnexacaoService), `jogo/controlador` (VilaControlador, RegiaoControlador), `jogo/dto/*`, `jogo/comum` (JogoException, ApiExceptionHandler), `jogo/excecao/*`, `jogo/cidadao` (DistribuicaoPopulacao novo, PopulacaoService, PopulacaoController, DTOs), `jogo/construcao` (ConstrucaoCatalogo, CatalogoConstrucaoDTO, ConstrucaoService, ObraService), `jogo/recurso` (ProducaoService, OuroService), `jogo/quartel/TreinamentoQuartelService`, `jogo/turno/TurnoProcessorPorVila`, `jogo/repositorio` (VilaRepository, novos repositórios); migration `V17__regioes_v2_bonus_e_previa.sql`; ~15 classes de teste.
- Frontend: `src/api/http.ts`, `src/main.ts`, `index.html`, `src/styles/tokens.css` (novo), `src/theme/` (novo), `src/domain/` (novo), `src/router/*`, `src/composables/{useVila,usePopulacao,useMapa,useAnexacao,useConstrucoes}.ts`, `src/views/*` e `src/components/**` (todas as telas do jogo), componentes novos em `components/vilarejo`, `components/criacao`, `components/populacao`; remoção de `CidadaoForm.vue`, `components/FamiliaLiderSelector.vue`, `components/jogo/BarraTurno.vue` e specs.
- Template servido pelo backend: `src/main/resources/templates/sistema/seguro/app/index.html` (link das fontes).
- Banco: exige recriação (sem conversão de dados antigos).
- Docs: `docs/jogo/v1-008-vila-e-mapa/**`, `v1-002-cidadaos/**`, `v1-003-construcoes/**`, `v1-010-recursos-e-producao/**`, `v1-009-turnos/turnos.md`, `v1-003-construcoes/quarteis.md`, `v1-001-masmorras/historia/h-001-tarefa-002-*.md`, `plano-de-construcao.md`.
- Specs principais: `openspec/specs/frontend-app/spec.md` (MODIFIED).
