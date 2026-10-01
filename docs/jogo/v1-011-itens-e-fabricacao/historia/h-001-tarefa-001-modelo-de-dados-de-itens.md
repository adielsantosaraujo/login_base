# H-001 · Tarefa 001 — Modelo de dados de itens

**História:** [H-001 — Fabricar item na oficina](h-001-fabricar-item-na-oficina.md) · **Domínio:** [../itens.md](../itens.md) · **Depende de:** — · **Camada:** Backend

## Objetivo

Criar as tabelas `item` e `fabricacao` no banco de dados, com suas entidades JPA correspondentes e repositórios, para armazenar e gerenciar itens fabricados e filas de fabricação nas oficinas.

## Contexto necessário

- [Tabelas no modelo de dados (seção 11.2)](../itens.md#modelo-de-dados-resumo)
  > `item`: id, vila_id, categoria, subtipo, qualidade, nivel, bonus (jsonb), atributo_escolhido, cidadao_id, slot
  > `fabricacao`: id, construcao_id, artesao_id, subtipo, nivel, pf_total, pf_atual

- [Qualidades e bônus (seção 7.2)](../itens.md#qualidades)
  > Simples (0 slots), Boa (1 slot), Excelente (3 slots), Divina (5 slots)

## Backend

- Entidades:
  - `Item`: categoria (ARMA/FERRAMENTA/ARMADURA/JOIA), subtipo (ESPADA/LANCA/...), qualidade (enum), nivel (1–10), bonus (JSON), atributo_escolhido (para Anéis: FOR/VIT/VEL/INT/CAR), cidadao_id (null se no inventário), slot (null se não equipado)
  - `Fabricacao`: construcao_id (oficina), artesao_id (cidadão), subtipo, nivel, pf_total, pf_atual
  - Enums: ItemCategoria (ARMA, FERRAMENTA, ARMADURA, JOIA), ItemSubtipo (ESPADA, LANCA, ARCO, BESTA, MARTELO, ...), Qualidade (SIMPLES, BOA, EXCELENTE, DIVINA)

- Repositórios:
  - `ItemRepository` com queries: por vila, por cidadão, por categoria, por inventário (cidadao_id == null)
  - `FabricacaoRepository` com queries: por construção, por artesão, por vila

- Migração Flyway `V5__criar_tabelas_item_e_fabricacao.sql`:
  - Tabelas `item` (com FK vila_id, construcao_id opcional, cidadao_id opcional)
  - Tabela `fabricacao` (com FK construcao_id, artesao_id)
  - Índices em vila_id, categoria, cidadao_id para performance

## Frontend

- Não se aplica nesta tarefa (tabelas internas)

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/item/Item.java](/src/main/java/com/example/loginbase/jogo/item/Item.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/item/ItemRepository.java](/src/main/java/com/example/loginbase/jogo/item/ItemRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/item/Fabricacao.java](/src/main/java/com/example/loginbase/jogo/item/Fabricacao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/item/FabricacaoRepository.java](/src/main/java/com/example/loginbase/jogo/item/FabricacaoRepository.java) (novo)
- [/src/main/resources/db/migration/V5__criar_tabelas_item_e_fabricacao.sql](/src/main/resources/db/migration/V5__criar_tabelas_item_e_fabricacao.sql) (novo)

## Testes

- Teste de criação de Item com bonus (JSON)
- Teste de persistência e leitura de Fabricacao
- Query de inventário: listar itens com cidadao_id == null
- Query de fabricação: listar itens em fila por construção

## Definição de pronto

- Critérios de aceite da história cobertos: CA4, CA5, CA6 (que verificam dados criados)
- Build (`./mvnw verify`) sem erros
- Testes unitários de repositórios passando
- Modelo de dados consistente com seções 7.1–7.2 e 11.2 da bíblia

## Fora de escopo

- Geração de bônus (tarefa 002)
- API REST (tarefa 003)
- Frontend (tarefa 004)
