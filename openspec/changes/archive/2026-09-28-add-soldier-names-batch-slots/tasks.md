# Tasks

## 1. Backend: dados e nomes

- [x] 1.1 [Migração V4 e modelo de unidade/lote](tasks/1.1-migracao-v4-unidade-lote.md) — verificação: `./mvnw test -Dtest=RepositoriosJogoTest` passa
- [x] 1.2 [JSONs movidos e gerador de nomes](tasks/1.2-gerador-nomes-classpath.md) — verificação: `./mvnw test -Dtest=GeradorNomesTest` passa
- [x] 1.3 [Enum SlotEquipamento](tasks/1.3-slot-equipamento-enum.md) — verificação: `./mvnw test -Dtest=CatalogoTest` passa

## 2. Backend: regras e API

- [x] 2.1 [Treino em lote e serviço](tasks/2.1-treino-em-lote-servico.md) — verificação: `./mvnw test -Dtest=QuartelServiceTest` passa
- [x] 2.2 [API de treino e UnidadeDto](tasks/2.2-api-treino-e-unidade-dto.md) — verificação: `./mvnw test -Dtest=VilaControllerWebMvcTest` passa
- [x] 2.3 [Morte libera slots](tasks/2.3-morte-libera-slots.md) — verificação: `./mvnw test -Dtest=MasmorraServiceTest` passa
- [x] 2.4 [Rota detalhe unidade na SPA](tasks/2.4-rota-detalhe-unidade-spa.md) — verificação: `./mvnw test -Dtest=AutenticacaoWebMvcTest` passa
- [x] 2.5 [Sufixo de nomes duplicados](tasks/2.5-sufixo-nomes-duplicados.md) — verificação: `./mvnw test -Dtest=NumeradorNomesTest,VilaServiceTest` passa
- [x] 2.6 [Troca de equipamento na API](tasks/2.6-troca-equipamento-api.md) — verificação: `./mvnw test -Dtest=EquipamentoServiceTest,AcoesVilaControllerWebMvcTest,MasmorraServiceTest` passa

## 3. Frontend

- [x] 3.1 [Tipos e API de treino](tasks/3.1-tipos-e-api-treino.md) — verificação: `cd frontend && npm run build` sem erros
- [x] 3.2 [Quartel com lote e quantidade](tasks/3.2-quartel-lote-quantidade.md) — verificação: `cd frontend && npm run build` sem erros
- [x] 3.3 [Tela detalhe da unidade](tasks/3.3-tela-detalhe-unidade.md) — verificação: `cd frontend && npm run build` sem erros; rota `/quartel/unidades/{id}` navega
- [x] 3.4 [Troca de equipamento no detalhe](tasks/3.4-troca-equipamento-detalhe.md) — verificação: `cd frontend && npm run build` sem erros; troca pelo diálogo funciona

## 4. Documentação técnica

- [x] 4.1 [Docs técnicas do quartel](tasks/4.1-docs-tecnicas-quartel.md) — verificação: docs 05, 06, 02, 15, 13, 16 descrevem lote, nomes com sufixo e troca de equipamento; links válidos

## 5. Verificação final

- [x] 5.1 [Verificação ponta a ponta](tasks/5.1-verificacao-ponta-a-ponta.md) — verificação: `./mvnw test`, `cd frontend && npm run build` e teste manual de lote, nome repetido com sufixo, detalhe e troca de equipamento funcionam
