# Tasks

## 1. Fundação (onda 1)

- [x] 1.1 [Coerência dos docs de terrenos](tasks/1.1-coerencia-docs-terrenos.md) — verificação: grep sem "vil_previa", "≠ Urbana" e "V17" em regioes/vila.md
- [x] 1.2 [Enum TipoTerreno](tasks/1.2-enum-tipo-terreno.md) — verificação: mvnw test-compile + TipoTerrenoTest,TipoRegiaoTest
- [x] 1.3 [Domínio TS de terrenos e tokens](tasks/1.3-dominio-ts-terrenos.md) — verificação: vitest src/domain/terrenos.spec.ts

## 2. Base de dados, gerador e telas (onda 2)

- [x] 2.1 [Migration V18 e entidades](tasks/2.1-migration-v18-entidades.md) — verificação: mvnw test ModeloJogoBaseIntegrationTest
- [x] 2.2 [Gerador de ladrilhos por terreno](tasks/2.2-gerador-ladrilhos-terreno.md) — verificação: mvnw test GeradorLadrilhoServiceTest
- [x] 2.3 [Grade de ladrilhos com endereço](tasks/2.3-grade-ladrilhos-endereco.md) — verificação: vitest GradeRegiao, useMapa, PainelMarcacao, RegiaoVila
- [x] 2.4 [Criação com composição de terrenos](tasks/2.4-criacao-composicao-terrenos.md) — verificação: vitest domain/regioes, components/criacao, useVila, CriacaoVila

## 3. Mapa, criação da vila e catálogo (onda 3)

- [x] 3.1 [Percentuais, prévia e criação da vila](tasks/3.1-percentuais-criacao-vila.md) — verificação: mvnw test GeradorMapa, VilaPrevia, VilaControlador, Casamento
- [x] 3.2 [Catálogo com terreno e Quartel](tasks/3.2-catalogo-terreno-quartel.md) — verificação: mvnw test ConstrucaoCatalogoTest,ConstrucaoServiceIntegrationTest
- [x] 3.3 [Mapa, anexação e catálogo no front](tasks/3.3-mapa-anexacao-catalogo-front.md) — verificação: vitest Mapa, DialogoAnexacao, useAnexacao, useConstrucoes, SeletorConstrucao

## 4. Leitura na API, marcação e serviço de bônus (onda 4)

- [x] 4.1 [Mapa, detalhe e anexação na API](tasks/4.1-mapa-detalhe-anexacao-api.md) — verificação: mvnw test MapaControlador,RegiaoControlador
- [x] 4.2 [Marcação por terreno](tasks/4.2-marcacao-por-terreno.md) — verificação: mvnw test MarcacaoIntegrationTest
- [x] 4.3 [Serviço de bônus por âncora](tasks/4.3-servico-bonus-ancora.md) — verificação: mvnw test BonusTerrenoServiceIntegrationTest

## 5. Consumidores do bônus (onda 5)

- [x] 5.1 [Produção com bônus da âncora](tasks/5.1-producao-bonus-ancora.md) — verificação: mvnw test ProducaoServiceIntegrationTest,ProducaoFabricasIntegrationTest
- [x] 5.2 [Comércio no ouro passivo](tasks/5.2-comercio-ouro-passivo.md) — verificação: mvnw test OuroServiceIntegrationTest,MercadoIntegrationTest
- [x] 5.3 [Desenvolvimento nas obras](tasks/5.3-desenvolvimento-obras.md) — verificação: mvnw test ObraServiceIntegrationTest
- [x] 5.4 [Militar no treino](tasks/5.4-militar-treino.md) — verificação: mvnw test TreinamentoQuartelIntegrationTest

## 6. Remoção do legado (onda 6)

- [x] 6.1 [Remover legado do backend](tasks/6.1-remover-legado-backend.md) — verificação: grep vazio + ./mvnw verify
- [x] 6.2 [Remover legado do frontend](tasks/6.2-remover-legado-frontend.md) — verificação: grep vazio + npm test + npm run build

## 7. Verificação final (onda 7)

- [x] 7.1 [Verificação final integrada](tasks/7.1-verificacao-final-integrada.md) — verificação: mvnw verify, npm test, npm run build e smoke