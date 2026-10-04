# H-003 · Tarefa 001 — API de marcação de ladrilhos

**História:** [H-003 — Marcar ladrilhos de coleta](h-003-marcar-ladrilhos-de-coleta.md) · **Domínio:** [../construcoes.md](../construcoes.md) · **Depende de:** [h-001-tarefa-002-api-de-construcao-e-posicionamento.md](h-001-tarefa-002-api-de-construcao-e-posicionamento.md) · **Camada:** Backend

## Objetivo

Implementar endpoints `POST /api/jogo/construcoes/{id}/marcacoes` (marcar) e `DELETE /api/jogo/construcoes/{id}/marcacoes/{x}/{y}` (desmarcar) com validações de terreno, conectividade e limite.

## Contexto necessário

- [../predios-de-coleta.md](../predios-de-coleta.md) — Prédios de coleta (seção 4.6)
  > Máximo: N1 4, N2 10, N3 20. Terreno compatível, conectado ortogonalmente.

- [../construcoes.md](../construcoes.md) — Tabela de produção (seção 4.5)

## Backend

- **POST /api/jogo/construcoes/{id}/marcacoes**
  - Request: `{ x: 5, y: 6 }`
  - Validações:
    - Construção deve ser de coleta.
    - Ladrilho deve ter terreno compatível com o prédio.
    - Não deve estar marcado.
    - Conectividade ortogonal ao prédio.
    - Não exceder limite de marcados.
  - Response (201): marcação criada.

- **DELETE /api/jogo/construcoes/{id}/marcacoes/{x}/{y}**
  - Remove marcação.
  - Response (204).

- **Entidade `ConstrucaoMarcacao`**: construcao_id, x, y.

- **Tabela `construcao_marcacao`** (em migração anterior ou nova):
  ```sql
  CREATE TABLE construcao_marcacao (
    id BIGSERIAL PRIMARY KEY,
    construcao_id BIGINT NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    FOREIGN KEY (construcao_id) REFERENCES construcao(id)
  );
  ```

- **Repositório**: MarcacaoRepository.

- **Serviço**: validações em MarcacaoService.

- **Testes**:
  - Marcar ladrilho do terreno do prédio.
  - Rejeitar terreno diferente.
  - Rejeitar sem conectividade.
  - Rejeitar após limite.
  - Desmarcar com sucesso.

## Frontend

Não se aplica (UI em tarefa h-003-tarefa-002).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoMarcacao.java](/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoMarcacao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/construcao/MarcacaoService.java](/src/main/java/com/example/loginbase/jogo/construcao/MarcacaoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/construcao/MarcacaoController.java](/src/main/java/com/example/loginbase/jogo/construcao/MarcacaoController.java) (novo)
- [/src/test/java/com/example/loginbase/jogo/construcao/MarcacaoServiceTest.java](/src/test/java/com/example/loginbase/jogo/construcao/MarcacaoServiceTest.java) (novo)

## Testes

- `testMarcarLadrilhoDoTerrenoDoPredio_Sucesso`.
- `testMarcarTerreno Diferente_Erro`.
- `testMarcacoesAposLimite_Erro`.
- `testDesmarcar_Sucesso`.

## Definição de pronto

- Critérios CA1, CA2, CA3, CA4 cobertos.
- Testes passando.

## Fora de escopo

- Cache de terrenos.
