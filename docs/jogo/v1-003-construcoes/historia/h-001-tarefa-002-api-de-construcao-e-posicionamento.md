# H-001 · Tarefa 002 — API de construção e posicionamento

**História:** [H-001 — Construir prédio nível 1](h-001-construir-predio-nivel-1.md) · **Domínio:** [../construcoes.md](../construcoes.md) · **Depende de:** [h-001-tarefa-001-catalogo-de-construcoes.md](h-001-tarefa-001-catalogo-de-construcoes.md) · **Camada:** Backend

## Objetivo

Implementar endpoint REST `POST /api/jogo/construcoes` para criar construção N1 em uma região, validando posição, tipo de região, recursos e debitando estoque. Criar entidade `Construcao` e migração Flyway para tabela `construcao`.

## Contexto necessário

- [../construcoes.md](../construcoes.md) — Regras gerais (seção 4.1)
  > Construção ocupa 1x1 (N1) em ladrilho sem ocupante. Recursos debitados ao iniciar obra. Prédio só em região correta.

- [../../v1-008-vila-e-mapa/regioes.md](../../v1-008-vila-e-mapa/regioes.md) — Tipos de região (seção 1.4)

## Backend

- **POST /api/jogo/construcoes**
  - Request body: `{ tipo: "CASA", regiaoIndice: 1, x: 0, y: 0 }`
  - Validações:
    - Região `regiaoIndice` deve ser possuída.
    - Tipo de prédio permitido em tipo de região.
    - Ladrilho (x, y) deve estar vazio (sem prédio, sem marcação de coleta).
    - Vila deve ter recursos suficientes (débito imediato).
  - Response (201):
    ```json
    {
      "id": 123,
      "tipo": "CASA",
      "nivel": "N1",
      "regiaoIndice": 1,
      "x": 0,
      "y": 0,
      "estado": "EM_OBRA",
      "poTotal": 4,
      "poAtual": 0
    }
    ```
  - Response (400): região errada, ladrilho ocupado, recursos insuficientes.

- **Entidade `Construcao`**: id, vila_id, tipo (enum), nivel, regiaoIndice, x, y, tamanho (1, 2 ou 3), estado (EM_OBRA, ATIVA, EM_UPGRADE), po_total, po_atual, configuracao (JSON para cultura/rebanho), criado_em, atualizado_em.

- **Tabela `construcao`** (migração V4):
  ```sql
  CREATE TABLE construcao (
    id BIGSERIAL PRIMARY KEY,
    vila_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    nivel VARCHAR(2) NOT NULL,
    regiao_indice INT NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    tamanho INT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    po_total INT NOT NULL,
    po_atual INT NOT NULL,
    configuracao JSONB,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vila_id) REFERENCES vila(id)
  );
  ```

- **Repositório `ConstrucaoRepository`**: findByVilaId, findByVilasIdAndRegiao.

- **Serviço `ConstrucaoService.criar(vila, tipo, regiaoIndice, x, y)`: validação, débito, criação, salvamento.

## Frontend

Não se aplica (frontend em tarefa h-001-tarefa-004).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/construcao/Construcao.java](/src/main/java/com/example/loginbase/jogo/construcao/Construcao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoRepository.java](/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoService.java](/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoController.java](/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoController.java) (novo)
- [/src/main/resources/db/migration/V4__construcao.sql](/src/main/resources/db/migration/V4__construcao.sql) (novo)
- [/src/test/java/com/example/loginbase/jogo/construcao/ConstrucaoServiceTest.java](/src/test/java/com/example/loginbase/jogo/construcao/ConstrucaoServiceTest.java) (novo)

## Testes

- `testCriarCasaN1_Sucesso` → 20 Mad/10 Ped/10 Arg debitados, Construcao criada com EM_OBRA.
- `testCriarQuartelEmRegiaoFloresta_Erro` → rejeitado.
- `testCriarQuartelEmLitoral_Sucesso` → Quartel criado com EM_OBRA em região Litoral.
- `testCriarEmLadrilhoOcupado_Erro` → rejeitado.
- `testCriarSemRecursosInsuficientes_Erro` → rejeitado.
- `testCriarEmRegiaoNaoPossuida_Erro` → rejeitado.

## Definição de pronto

- Critérios CA1, CA2, CA3 cobertos.
- Build sem erros.
- Testes passando.
- Transação atômica (débito + criação).

## Fora de escopo

- Cancelamento de obra.
- Histórico de custos.
