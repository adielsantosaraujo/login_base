# H-001 · Tarefa 001 — Modelo de dados de cidadãos e famílias

**História:** [H-001 — Gerar famílias...](h-001-gerar-familias-e-distribuir-pontos-iniciais.md) · **Domínio:** [../cidadao.md](../cidadao.md) · **Depende de:** — · **Camada:** Backend

## Objetivo

Criar as tabelas e entidades JPA para cidadãos e famílias. Executar migração Flyway V3 com schema completo (jogo_turno, vila, regiao, etc. conforme seção 11.2). Pronto para leitura via API.

## Contexto necessário

- [cidadao.md](../cidadao.md) — modelo de dados (resumo)
  > Tabelas: familia, cidadao, cidadao_profissao. Campos principais (seção 11.2).

- [familias.md](../familias.md) — núcleo e herança
  > Núcleo familiar: Casal + filhos solteiros na mesma casa.

## Backend

**Entidades JPA** (`com.example.loginbase.jogo.cidadao.*`):
- `Familia`: id, vilaId, sobrenome, casaId.
- `Cidadao`: id, vilaId, familiaId, nome, sexo, idadeMeses, vit/for/vel/int/car, pontosCarpendentes, pontosProffendentes, conjugeId, paiId, maeId, vivo, estado, feridoAteturno, famintoTurnos, gestacaoTurnos, construcaoId, tropaId, xpGuerreiro.
- `CidadaoProfissao`: cidadaoId (PK, FK), profissao (PK, string), pontosBase, turnosExperiencia.

**Repositórios**:
- `FamiliaRepository`: findByVilaId, save, delete.
- `CidadaoRepository`: findByVilaId, findByFamiliaId, findByConiugeId, findByVitro (genealogia).
- `CidadaoProfissaoRepository`: findByCidadaoId, findByCidadaoIdAndProfissao.

**Migração Flyway** (`V3__schema_jogo.sql`):
Criar tabelas conforme 11.2: jogo_turno, vila, regiao, familia, cidadao, cidadao_profissao, construcao, estoque, etc. com constraints FK e índices.

## Frontend

Não se aplica (backend apenas).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/cidadao/Familia.java](/src/main/java/com/example/loginbase/jogo/cidadao/Familia.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/cidadao/Cidadao.java](/src/main/java/com/example/loginbase/jogo/cidadao/Cidadao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoProfissao.java](/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoProfissao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/cidadao/FamiliaRepository.java](/src/main/java/com/example/loginbase/jogo/cidadao/FamiliaRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoRepository.java](/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoRepository.java) (novo)
- [/src/main/resources/db/migration/V3__schema_jogo.sql](/src/main/resources/db/migration/V3__schema_jogo.sql) (novo)

## Testes

- Teste unitário: criar Cidadao com características e profissões (todas 0 na geração).
- Teste de repositório: persistir Familia e Cidadao; recuperar por vilaId.
- Teste Flyway: executar V3, verificar tabelas e colunas existem.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1 (geração de estrutura).
- Build do backend (`./mvnw verify`) sem erros.
- Testes listados passando.
- Tabelas e constraints criadas conforme 11.2.

## Fora de escopo

- Distribuição de pontos (tarefa 003).
- Família líder (tarefa 003).
- API REST (será em tarefa 004 ou futura).
