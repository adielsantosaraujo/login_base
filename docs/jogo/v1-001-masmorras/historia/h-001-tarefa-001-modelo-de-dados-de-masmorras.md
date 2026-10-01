# H-001 · Tarefa 001 — Modelo de dados de masmorras

**História:** [h-001-surgimento-e-evolucao-de-masmorras.md](h-001-surgimento-e-evolucao-de-masmorras.md) · **Domínio:** [../masmorras.md](../masmorras.md) ·
**Depende de:** [../../../v1-008-vila-e-mapa/historia/h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](../../v1-008-vila-e-mapa/historia/h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md) · **Camada:** Backend

## Objetivo

Criar a tabela de dados `masmorra` com os campos necessários para rastrear surgimento, evolução e estado de cada masmorra.

## Contexto necessário

- [../masmorras.md](../masmorras.md) — regras de surgimento (R1–R3), evolução (R4–R5), vitória/derrota.
  > Surgimento: 1% chance por região elegível; limite 3 por vila; vilas com <12 turnos não recebem.
  > Evolução: +1 nível a cada 18 turnos sem ataque; máximo nível 10.
  > Ataque zera contador; vitória remove masmorra; derrota restaura inimigos.

## Backend

- **Entidade** `Masmorra` (novo)
  - `id: UUID`
  - `vila_id: UUID` (FK para `vila`)
  - `regiao_indice: int` (1–16)
  - `nivel: int` (1–10)
  - `turno_surgimento: int` (turno do jogo em que surgiu)
  - `turnos_sem_ataque: int` (contador para evolução)
  - `ativa: boolean` (true = ainda existe; false = removida)

- **Repositório** `MasmorraRepository` (novo)
  - `findByVilaIdAndAtiva(vila_id, ativa)`: lista masmorras ativas/inativas da vila.
  - `findByVilaIdAndRegiao_indice(vila_id, regiao_indice)`: masmorra da região.
  - `countByVilaIdAndAtiva(vila_id, true)`: quantidade de masmorras ativas.

- **Migração Flyway** (novo): `V<N>__criar_tabela_masmorra.sql`
  ```sql
  CREATE TABLE masmorra (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vila_id UUID NOT NULL REFERENCES vila(id) ON DELETE CASCADE,
    regiao_indice INT NOT NULL CHECK (regiao_indice BETWEEN 1 AND 16),
    nivel INT NOT NULL CHECK (nivel BETWEEN 1 AND 10),
    turno_surgimento INT NOT NULL,
    turnos_sem_ataque INT NOT NULL DEFAULT 0,
    ativa BOOLEAN NOT NULL DEFAULT true,
    UNIQUE (vila_id, regiao_indice)
  );
  CREATE INDEX idx_masmorra_vila_ativa ON masmorra(vila_id, ativa);
  ```

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/masmorra/Masmorra.java](/src/main/java/com/example/loginbase/jogo/masmorra/Masmorra.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/masmorra/MasmorraRepository.java](/src/main/java/com/example/loginbase/jogo/masmorra/MasmorraRepository.java) (novo)
- [/src/main/resources/db/migration/V<N>__criar_tabela_masmorra.sql](/src/main/resources/db/migration/V<N>__criar_tabela_masmorra.sql) (novo)

## Testes

- Teste de criação de Masmorra com valores válidos (nível 1–10, regiao_indice 1–16).
- Teste de validação: rejeita nível 0 ou 11; regiao_indice fora de 1–16.
- Teste de repositório: `countByVilaIdAndAtiva` retorna valor correto.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1 (dados de surgimento), CA5 (turnos_sem_ataque resetado).
- Build do backend (`./mvnw verify`) sem erros.
- Migração executa sem erro em BD limpo.

## Fora de escopo

- Geradores de masmorras (tarefa 002).
- API de consulta/atualização (tarefa 002).
