# H-001 · Tarefa 001 — Modelo de dados da vila e regiões

**História:** [h-001-criar-vila-escolhendo-regioes-iniciais.md](h-001-criar-vila-escolhendo-regioes-iniciais.md) · **Domínio:** [../vila.md](../vila.md) ·
**Depende de:** — · **Camada:** Backend

## Objetivo

Criar as tabelas base do jogo (jogo_turno, vila, regiao, ladrilho_jazida) e suas entidades JPA correspondentes, com chaves estrangeiras e constraints de unicidade.

## Contexto necessário

- [../vila.md](../vila.md) — modelo de dados resumo (seções 11.1, 11.2)
  > Tabelas: jogo_turno (numero, iniciado_em, concluido_em), vila (id, usuario_id único, nome, semente, turno_criacao, familia_lider_id, bem_alimentada, version), regiao (id, vila_id, indice 1–16, tipo, possuida, limpa_ate_turno), ladrilho_jazida (regiao_id, x, y, jazida).

## Backend

**Entidades (novos):**
- `/src/main/java/com/example/loginbase/jogo/modelo/JogoTurno.java` — @Entity, @Table(name = "jogo_turno")
- `/src/main/java/com/example/loginbase/jogo/modelo/Vila.java` — @Entity, @Table(name = "vila"), @Version version, FK usuario_id (UNIQUE)
- `/src/main/java/com/example/loginbase/jogo/modelo/Regiao.java` — @Entity, @Table(name = "regiao"), FK vila_id
- `/src/main/java/com/example/loginbase/jogo/modelo/LadrilhoJazida.java` — @Entity, @Table(name = "ladrilho_jazida"), @Id(regiao_id, x, y) ou @IdClass

**Repositórios (novos):**
- [/src/main/java/com/example/loginbase/jogo/repositorio/JogoTurnoRepository.java](/src/main/java/com/example/loginbase/jogo/repositorio/JogoTurnoRepository.java) (novo) — finder: findByNumero, existsByNumero
- [/src/main/java/com/example/loginbase/jogo/repositorio/VilaRepository.java](/src/main/java/com/example/loginbase/jogo/repositorio/VilaRepository.java) (novo) — finder: findByUsuarioId, existsByUsuarioId
- [/src/main/java/com/example/loginbase/jogo/repositorio/RegiaoRepository.java](/src/main/java/com/example/loginbase/jogo/repositorio/RegiaoRepository.java) (novo) — finder: findByVilaIdAndIndice, findAllByVilaId

**Observações:**
- `V3__Criacao_tabelas_jogo_base.sql` já existe (fase anterior).
- `V17__regioes_v2_bonus_e_previa.sql` cria `regiao_bonus` e `vila_previa` (esta task atualiza modelos para V17); ids continuam BIGINT.

**Migração Flyway (V17, novo):**
- [/src/main/resources/db/migration/V17__regioes_v2_bonus_e_previa.sql](/src/main/resources/db/migration/V17__regioes_v2_bonus_e_previa.sql) (novo)
  ```sql
  CREATE TABLE jogo_turno (
    numero INT PRIMARY KEY,
    iniciado_em TIMESTAMP NOT NULL,
    concluido_em TIMESTAMP
  );

  CREATE TABLE vila (
    id UUID PRIMARY KEY,
    usuario_id UUID UNIQUE NOT NULL,
    nome VARCHAR(255),
    semente BIGINT NOT NULL,
    turno_criacao INT NOT NULL,
    familia_lider_id BIGINT,
    bem_alimentada BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 0,
    FOREIGN KEY (usuario_id) REFERENCES users(id)
  );

  CREATE TABLE regiao (
    id UUID PRIMARY KEY,
    vila_id UUID NOT NULL,
    indice INT NOT NULL CHECK (indice BETWEEN 1 AND 16),
    tipo VARCHAR(50),
    possuida BOOLEAN DEFAULT FALSE,
    limpa_ate_turno INT,
    FOREIGN KEY (vila_id) REFERENCES vila(id),
    UNIQUE (vila_id, indice)
  );

  CREATE TABLE ladrilho_jazida (
    regiao_id UUID NOT NULL,
    x INT NOT NULL CHECK (x BETWEEN 0 AND 9),
    y INT NOT NULL CHECK (y BETWEEN 0 AND 9),
    jazida VARCHAR(50),
    PRIMARY KEY (regiao_id, x, y),
    FOREIGN KEY (regiao_id) REFERENCES regiao(id)
  );
  ```

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/modelo/JogoTurno.java](/src/main/java/com/example/loginbase/jogo/modelo/JogoTurno.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/modelo/Vila.java](/src/main/java/com/example/loginbase/jogo/modelo/Vila.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/modelo/Regiao.java](/src/main/java/com/example/loginbase/jogo/modelo/Regiao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/modelo/LadrilhoJazida.java](/src/main/java/com/example/loginbase/jogo/modelo/LadrilhoJazida.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/repositorio/JogoTurnoRepository.java](/src/main/java/com/example/loginbase/jogo/repositorio/JogoTurnoRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/repositorio/VilaRepository.java](/src/main/java/com/example/loginbase/jogo/repositorio/VilaRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/repositorio/RegiaoRepository.java](/src/main/java/com/example/loginbase/jogo/repositorio/RegiaoRepository.java) (novo)
- [/src/main/resources/db/migration/V3__Criacao_tabelas_jogo_base.sql](/src/main/resources/db/migration/V3__Criacao_tabelas_jogo_base.sql) (novo)

## Testes

- Teste unitário: criar entidade Vila com usuario_id e verificar se version inicia em 0.
- Teste de integração: inserir vila, recuperá-la por usuario_id, garantir unicidade.
- Teste de integração: criar vila com 16 regiões, verificar constraint CHECK na coluna indice.
- Teste de integração: inserir ladrilho_jazida e verificar chave primária composta (regiao_id, x, y).

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA6 (estrutura base)
- Build do backend (`./mvnw verify`) sem erros
- Testes listados passando
- Tabelas criadas, constraints aplicadas, FKs funcionando

## Fora de escopo

- Dados iniciais (seed) de jogo_turno.
- Geração de jazidas (tarefa 002).
