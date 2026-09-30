# Tasks

## 1. Backend: banco e curva

- [x] 1.1 [Migração V5 e reversão do V3](tasks/1.1-migracao-v5-niveis.md) — verificação: `./mvnw test -Dtest=RepositoriosJogoTest` passa
- [x] 1.2 [Curva de níveis e expoente configurável](tasks/1.2-curva-niveis-expoente.md) — verificação: `./mvnw test -Dtest=CurvaNiveisTest,JogoPropertiesTest` passa

## 2. Backend: regras

- [x] 2.1 [Fórmulas de prédios e itens com a curva](tasks/2.1-formulas-predios-itens.md) — verificação: `./mvnw test -Dtest=CatalogoTest,CurvaNiveisTest,JogoPropertiesTest,CalculadoraProducaoTest,ConstrucaoServiceTest,GeradorLootTest,ForjaServiceTest,MasmorraServiceTest,VilaControllerWebMvcTest` passa
- [x] 2.2 [Produção sem overflow](tasks/2.2-producao-sem-overflow.md) — verificação: `./mvnw test -Dtest=CalculadoraProducaoTest` passa
- [x] 2.3 [Canteiros da fazenda por faixa](tasks/2.3-canteiros-fazenda-faixas.md) — verificação: `./mvnw test -Dtest=FazendaServiceTest,ConstrucaoServiceTest,VilaServiceTest` passa
- [x] 2.4 [Nível forjável e VilaDto](tasks/2.4-forja-nivel-forjavel.md) — verificação: `./mvnw test -Dtest=ForjaServiceTest,VilaControllerWebMvcTest,AcoesVilaControllerWebMvcTest,QuartelServiceTest` passa

## 3. Frontend

- [x] 3.1 [Forja e quartel com níveis estendidos](tasks/3.1-frontend-forja-quartel.md) — verificação: `cd frontend && npm run build` sem erros

## 4. Documentação

- [x] 4.1 [ADR 0025 e índices de ADR](tasks/4.1-adr-0025-indices.md) — verificação: ADR 0025 criado, listado em `docs/adr/README.md` e `docs/04`; links válidos
- [x] 4.2 [GDD com níveis estendidos](tasks/4.2-gdd-niveis-estendidos.md) — verificação: `grep` não encontra "máx. 5"/"Nível máximo 5" nos arquivos do GDD tocados; tabela de valores presente em 12.14
- [x] 4.3 [Requisitos, casos de uso e rastreabilidade](tasks/4.3-requisitos-casos-uso.md) — verificação: docs 01, 02, 03, 15 citam nível 100, canteiros 24 e itens 1–23
- [x] 4.4 [Dados, API, testes e operação](tasks/4.4-dados-api-operacao.md) — verificação: docs 05, 06, 08, 09, 10 citam V5, `nivelMaximoForjavel` e `JOGO_EXPOENTE_CURVA`
- [x] 4.5 [Manual, glossário, histórico e README](tasks/4.5-manual-glossario-historico.md) — verificação: docs 13, 14, 16, 17 e `README.md` atualizados; links válidos

## 5. Verificação final

- [ ] 5.1 [Verificação ponta a ponta](tasks/5.1-verificacao-ponta-a-ponta.md) — verificação: `./mvnw test`, `cd frontend && npm run build`, `openspec validate raise-building-max-level-100 --strict` e teste manual do catálogo, da forja e da fazenda passam
