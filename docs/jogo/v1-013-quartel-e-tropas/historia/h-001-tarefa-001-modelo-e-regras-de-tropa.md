# H-001 · Tarefa 001 — Modelo e regras de tropa

**História:** [h-001-formar-tropa-no-quartel.md](h-001-formar-tropa-no-quartel.md) · **Domínio:** [../tropas.md](../tropas.md) · **Depende de:** [h-001-tarefa-002 (opcional: ambas de forma paralela)](#) · **Camada:** Backend

## Objetivo

Implementar as entidades, validações e API REST para criar e gerenciar tropas de guerreiros. Garantir que cidadãos elegíveis (com arma, PE ≥ 1, 16–54 anos) sejam adicionados a tropas respeitando os limites de capacidade por quartel.

## Contexto necessário

- [../tropas.md](../tropas.md) — Formação (R1–R6), Capacidade do quartel (tabela), Estados
  > R1: Apenas cidadãos com PE Guerreiro ≥ 1 (base) e arma equipada podem ser membros de tropa; R2: 16–54 anos; R3: no máximo 1 tropa; R4: posição Frente ou Retaguarda.

- [../../v1-002-cidadaos/cidadao.md](../../v1-002-cidadaos/cidadao.md) — PE base, profissão Guerreiro
  > PE Guerreiro base é um dos atributos do cidadão; item arma ocupa slot fixo.

- [../../v1-003-construcoes/construcoes.md](../../v1-003-construcoes/construcoes.md) — Quartel N1/N2/N3 com instrutores
  > Quartel tem vagas de instrutor (Guerreiro): sem instrutor não forma tropas.

## Backend

### Entidades e repositórios

- **Tropa** (JPA)
  - `id` (Long)
  - `vila` (Vila) — referência
  - `quartel` (Construcao) — referência ao quartel que a formou
  - `nome` (String) — nome da tropa (ex.: "Guardiões do Vale")
  - `estado` (Enum AQUARTELADA, EM_VIAGEM_IDA, EM_VIAGEM_VOLTA)
  - `masmorraId` (Long, nullable) — masmorra alvo em expedição
  - `turnosRestantes` (Integer) — contador de turnos de viagem
  - `criadoEm` (LocalDateTime)

- **TropaRepository** (Spring Data JPA)
  - `findByVila(vila: Vila): List<Tropa>`
  - `findByQuartel(construcao: Construcao): List<Tropa>`
  - `findByVilaMembros`: opcional, para contar membros por vila

- **TropaMembro** (JPA)
  - `id` (TropaMemberId)
    - `tropaId` (Long)
    - `cidadaoId` (Long)
  - `posicao` (Enum FRENTE, RETAGUARDA)

- **TropaMembroRepository** (Spring Data JPA)
  - `findByTropa(tropa: Tropa): List<TropaMembro>`
  - `findByCidadao(cidadao: Cidadao): Optional<TropaMembro>` — para verificar se em tropa
  - `countByTropa(tropa: Tropa): long`

### Validações

**TropaValidator** ou métodos em serviço:
- `validarMembroElegivel(cidadao: Cidadao)`: verifica
  - PE Guerreiro base ≥ 1
  - Arma equipada (categoria ARMA não nula)
  - Idade 16–54 anos (idade em meses ÷ 12)
  - Não em outra tropa (TropaMembroRepository)
  - Vivo (estado != MORTO)
- `validarCapacidadeQuartel(quartel: Construcao, tropaAtual: Tropa, novoMembro: Cidadao)`: verifica
  - Soma de membros de todas as tropas do quartel ≤ capacidade (N1 5, N2 8, N3 10)
  - Número de tropas ≤ limite (N1 1, N2 2, N3 4)
- `validarInstrutor(quartel: Construcao)`: quartel tem ≥ 1 instrutor alocado

### Endpoints REST

- **POST** `/api/jogo/quartel/{quartelId}/tropa`
  - Body: `{ "nome": "...", "membros": [ { "cidadaoId": 123, "posicao": "FRENTE" }, ... ] }`
  - Response 201: `{ "id": 1, "nome": "...", "estado": "AQUARTELADA", "membros": [...] }`
  - Response 400: erro de validação (sem instrutor, membro inelegível, capacidade excedida)

- **GET** `/api/jogo/quartel/{quartelId}/tropas`
  - Response 200: lista de tropas do quartel com membros

- **POST** `/api/jogo/tropa/{tropaId}/membro`
  - Body: `{ "cidadaoId": 456, "posicao": "RETAGUARDA" }`
  - Response 201: membro adicionado; ou 400 se falhar validação

- **DELETE** `/api/jogo/tropa/{tropaId}` (apenas AQUARTELADA)
  - Response 204: tropa desfeita; ou 400 se em expedição

### Migração Flyway (parte de V3 ou nova)

```sql
CREATE TABLE tropa (
  id BIGSERIAL PRIMARY KEY,
  vila_id BIGINT NOT NULL REFERENCES vila(id),
  quartel_id BIGINT NOT NULL REFERENCES construcao(id),
  nome VARCHAR(100) NOT NULL,
  estado VARCHAR(50) NOT NULL DEFAULT 'AQUARTELADA',
  masmorra_id BIGINT REFERENCES masmorra(id),
  turnos_restantes INTEGER,
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(vila_id, nome)
);

CREATE TABLE tropa_membro (
  tropa_id BIGINT NOT NULL REFERENCES tropa(id) ON DELETE CASCADE,
  cidadao_id BIGINT NOT NULL REFERENCES cidadao(id),
  posicao VARCHAR(20) NOT NULL,
  PRIMARY KEY(tropa_id, cidadao_id),
  FOREIGN KEY(cidadao_id) REFERENCES cidadao(id)
);

CREATE INDEX idx_tropa_vila ON tropa(vila_id);
CREATE INDEX idx_tropa_quartel ON tropa(quartel_id);
CREATE INDEX idx_tropa_membro_cidadao ON tropa_membro(cidadao_id);
```

## Frontend

Não se aplica (tarefa de Backend). Relacionada a [h-001-tarefa-002-tela-do-quartel.md](h-001-tarefa-002-tela-do-quartel.md).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/quartel/Tropa.java](/src/main/java/com/example/loginbase/jogo/quartel/Tropa.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/quartel/TropaMembro.java](/src/main/java/com/example/loginbase/jogo/quartel/TropaMembro.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/quartel/TropaRepository.java](/src/main/java/com/example/loginbase/jogo/quartel/TropaRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/quartel/TropaMembroRepository.java](/src/main/java/com/example/loginbase/jogo/quartel/TropaMembroRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/quartel/TropaValidator.java](/src/main/java/com/example/loginbase/jogo/quartel/TropaValidator.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/quartel/TropaService.java](/src/main/java/com/example/loginbase/jogo/quartel/TropaService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/quartel/TropaController.java](/src/main/java/com/example/loginbase/jogo/quartel/TropaController.java) (novo)
- [/src/main/resources/db/migration/V<N>__criar_tabelas_tropa.sql](/src/main/resources/db/migration/V<N>__criar_tabelas_tropa.sql) (novo)

## Testes

- Testes unitários de **TropaValidator**:
  - cidadão sem arma → erro
  - cidadão com PE Guerreiro 0 → erro
  - cidadão com 15 anos → erro; 16 anos → OK
  - cidadão com 55 anos → erro; 54 anos → OK
  - cidadão em outra tropa → erro
  - cidadão morto → erro
  
- Testes de integração (TropaService):
  - criar tropa com 2 membros elegíveis → sucesso
  - adicionar membro ao limite (N1=5) → sucesso; 6º → erro
  - quartel sem instrutor → erro ao criar tropa
  - desfazer tropa em estado AQUARTELADA → sucesso; EM_VIAGEM_IDA → erro

- Teste numérico (exemplo CA5):
  - Quartel N1, 3 cidadãos com PE Guerreiro ≥ 1, arma equipada, 20 anos.
  - Criar tropa = 2 Frente + 1 Retaguarda.
  - Verificar: tropa criada, estado AQUARTELADA, membros nas posições.

## Definição de pronto

- Critérios de aceite da história cobertos: CA1–CA6
- Build Backend (`./mvnw verify`) sem erros
- Testes listados passando
- Validações de PE, arma, idade, estado de tropa centralizadas em TropaValidator
- Ambas as tables (tropa, tropa_membro) criadas via Flyway
- Endpoints `/api/jogo/quartel/{quartelId}/tropa*` funcionando

## Fora de escopo

- Permissões de edição (apenas o dono da vila pode formar suas tropas)
- Histórico de mudanças em tropas
- Renumeração automática de tropas
