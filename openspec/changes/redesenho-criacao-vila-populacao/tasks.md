# Tasks

## 1. Documentos docs/jogo

- [x] 1.1 [Reescrever regras de regiões v2](tasks/1.1-reescrever-regioes-v2.md) — verificação: R1–R19 v2 e tabelas em regioes.md
- [x] 1.2 [Atualizar vila e H-001 da vila](tasks/1.2-atualizar-vila-h001.md) — verificação: grep sem "jazidas-por-semente"
- [x] 1.3 [Atualizar docs de mapa e anexação](tasks/1.3-atualizar-docs-mapa-anexacao.md) — verificação: grep sem tipos Rural/Coleta em v1-008
- [x] 1.4 [Atualizar docs de construções](tasks/1.4-atualizar-docs-construcoes.md) — verificação: coluna Região com tipos v2
- [x] 1.5 [Atualizar docs da população inicial](tasks/1.5-atualizar-docs-populacao.md) — verificação: grep sem "máx. 10 por característica"
- [x] 1.6 [Documentar bônus na produção e no turno](tasks/1.6-documentar-bonus-producao.md) — verificação: tabela dos 13 bônus em producao.md

## 2. Backend base

- [x] 2.1 [Enums de tipo e bônus de região](tasks/2.1-enums-tipo-bonus-regiao.md) — verificação: mvnw test GradeRegioesTest + novo teste dos enums
- [x] 2.2 [Gerador do mapa por semente](tasks/2.2-gerador-mapa-semente.md) — verificação: mvnw test GeradorMapaServiceTest
- [x] 2.3 [Erros da API com código](tasks/2.3-erros-api-com-codigo.md) — verificação: mvnw test ApiInfraWebMvcTest
- [x] 2.4 [Migration V17 e entidades](tasks/2.4-migration-v17-entidades.md) — verificação: mvnw test ModeloJogoBaseIntegrationTest
- [x] 2.5 [Serviço de bônus da vila](tasks/2.5-servico-bonus-vila.md) — verificação: mvnw test BonusRegiaoServiceIntegrationTest

## 3. Backend endpoints da vila

- [x] 3.1 [Endpoints da prévia do mapa](tasks/3.1-endpoints-previa-mapa.md) — verificação: mvnw test VilaPreviaIntegrationTest
- [x] 3.2 [Criação da vila pela prévia](tasks/3.2-criacao-vila-previa.md) — verificação: mvnw test VilaControladorIntegrationTest,CasamentoIntegrationTest
- [x] 3.3 [Mapa, resumo e anexação v2](tasks/3.3-mapa-resumo-anexacao-v2.md) — verificação: mvnw test Mapa/Regiao/VilaControladorIntegrationTest

## 4. Backend população

- [x] 4.1 [Fixture de paridade da distribuição](tasks/4.1-fixture-paridade-distribuicao.md) — verificação: JSON gerado bate com o JS de referência
- [x] 4.2 [Porte Java da distribuição](tasks/4.2-porte-java-distribuicao.md) — verificação: mvnw test DistribuicaoPopulacaoTest
- [x] 4.3 [Consulta da população inicial](tasks/4.3-get-populacao-inicial.md) — verificação: mvnw test PopulacaoControllerIntegrationTest
- [x] 4.4 [Confirmação da população](tasks/4.4-confirmacao-populacao.md) — verificação: mvnw test PopulacaoControllerIntegrationTest
- [x] 4.5 [Turno ignora população pendente](tasks/4.5-turno-ignora-pendente.md) — verificação: mvnw test TurnoProcessorPorVilaIntegrationTest,AgendadorTurnoIntegrationTest

## 5. Backend bônus, construções e produção

- [x] 5.1 [Construções permitidas por tipo](tasks/5.1-construcoes-por-tipo.md) — verificação: mvnw test Construcao*Test,MarcacaoIntegrationTest
- [x] 5.2 [Bônus na produção dos prédios](tasks/5.2-bonus-producao-predios.md) — verificação: mvnw test Producao*IntegrationTest
- [x] 5.3 [Bônus Comércio no ouro passivo](tasks/5.3-bonus-comercio-ouro.md) — verificação: mvnw test OuroServiceIntegrationTest
- [x] 5.4 [Bônus Desenvolvimento nas obras](tasks/5.4-bonus-desenvolvimento-obras.md) — verificação: mvnw test ObraServiceIntegrationTest
- [x] 5.5 [Bônus Militar no treino](tasks/5.5-bonus-militar-treino.md) — verificação: mvnw test TreinamentoQuartelIntegrationTest
- [x] 5.6 [Remover tipos legados e verificar backend](tasks/5.6-remover-tipos-legados.md) — verificação: ./mvnw verify

## 6. Frontend base

- [x] 6.1 [ApiError no cliente HTTP](tasks/6.1-api-error-http.md) — verificação: npm test src/api
- [x] 6.2 [Tokens, fontes e preset PrimeVue](tasks/6.2-tokens-fontes-preset.md) — verificação: npm run build
- [x] 6.3 [Domínio TS de regiões](tasks/6.3-dominio-ts-regioes.md) — verificação: npm test src/domain/regioes.spec.ts
- [x] 6.4 [Domínio TS da população](tasks/6.4-dominio-ts-populacao.md) — verificação: npm test src/domain/populacao.spec.ts
- [x] 6.5 [Rotas e guarda do início de jogo](tasks/6.5-rotas-guarda-inicio.md) — verificação: npm test src/router
- [x] 6.6 [Componentes base Vilarejo](tasks/6.6-componentes-base-vilarejo.md) — verificação: npm test src/components/vilarejo
- [x] 6.7 [Layout do jogo com cabeçalho Vilarejo](tasks/6.7-layout-jogo-cabecalho.md) — verificação: npm test + npm run build

## 7. Frontend telas de criação e tipos de região

- [x] 7.1 [Composable de criação da vila](tasks/7.1-composable-criacao-vila.md) — verificação: npm test src/composables/useVila.spec.ts
- [x] 7.2 [Tela Criar minha vila](tasks/7.2-tela-criar-vila.md) — verificação: npm test CriacaoVila + components/criacao
- [x] 7.3 [Composable da população](tasks/7.3-composable-populacao.md) — verificação: npm test src/composables/usePopulacao.spec.ts
- [x] 7.4 [Tela Distribuir a população](tasks/7.4-tela-distribuir-populacao.md) — verificação: npm test DistribuicaoPopulacao + components/populacao
- [x] 7.5 [Mapa com tipos e bônus](tasks/7.5-mapa-tipos-bonus.md) — verificação: npm test Mapa.spec + useMapa.spec
- [x] 7.6 [Anexação sem escolha de tipo](tasks/7.6-anexacao-sem-tipo.md) — verificação: npm test useAnexacao.spec + DialogoAnexacao
- [x] 7.7 [Seletor de construção por tipo](tasks/7.7-seletor-construcao-tipos.md) — verificação: npm test SeletorConstrucao + useConstrucoes

## 8. Frontend migração visual do jogo

- [x] 8.1 [Visual: região e construção](tasks/8.1-visual-regiao-construcao.md) — verificação: npm test + npm run build
- [x] 8.2 [Visual: estoque e mercado](tasks/8.2-visual-estoque-mercado.md) — verificação: npm test + npm run build
- [x] 8.3 [Visual: famílias e cidadão](tasks/8.3-visual-familias-cidadao.md) — verificação: npm test + npm run build
- [x] 8.4 [Visual: inventário, oficina e itens](tasks/8.4-visual-inventario-oficina.md) — verificação: npm test + npm run build
- [x] 8.5 [Visual: quartel e batalhas](tasks/8.5-visual-quartel-batalhas.md) — verificação: npm test + npm run build

## 9. Verificação final

- [ ] 9.1 [Verificação final integrada](tasks/9.1-verificacao-final-integrada.md) — verificação: mvnw verify, npm test, npm run build e smoke
