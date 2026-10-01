# H-004 · Tarefa 001 — API de alocação

**História:** [H-004 — Alocar trabalhadores nos prédios](h-004-alocar-trabalhadores-nos-predios.md) · **Domínio:** [../construcoes.md](../construcoes.md) · **Depende de:** [h-001-tarefa-002-api-de-construcao-e-posicionamento.md](h-001-tarefa-002-api-de-construcao-e-posicionamento.md) · **Camada:** Backend

## Objetivo

Implementar endpoints `POST /api/jogo/construcoes/{id}/alocacoes` (alocar) e `DELETE /api/jogo/construcoes/{id}/alocacoes/{cidadaoId}` (desalocar) com validações de vagas, idade, tropa.

## Contexto necessário

- [../construcoes.md](../construcoes.md) — Vagas por nível (seção 4.3)
  > N1 2, N2 5, N3 10 (ou tipo específico).

- [../../v1-002-cidadaos/cidadao.md](../../v1-002-cidadaos/cidadao.md) — Eficiência (seção 5.3)
  > eficiência = (0,5 + 0,1 × PE efetivo) limitado a 3,0.

## Backend

- **POST /api/jogo/construcoes/{id}/alocacoes**
  - Request: `{ cidadaoId: 456 }`
  - Validações:
    - Construção deve estar ATIVA (não em obra).
    - Cidadão deve ter 14–64 anos.
    - Cidadão não pode estar em tropa.
    - Cidadão não pode estar já alocado em outro prédio.
    - Prédio deve ter vaga.
  - Response (201): alocação criada; eficiência calculada.

- **DELETE /api/jogo/construcoes/{id}/alocacoes/{cidadaoId}**
  - Remove alocação.
  - Response (204).

- **Entidade `ConstrucaoAlocacao`**: construcao_id, cidadao_id, profissao_trabalho (enum).

- **Tabela** (em migração anterior ou nova):
  ```sql
  CREATE TABLE construcao_alocacao (
    id BIGSERIAL PRIMARY KEY,
    construcao_id BIGINT NOT NULL,
    cidadao_id BIGINT NOT NULL,
    FOREIGN KEY (construcao_id) REFERENCES construcao(id),
    FOREIGN KEY (cidadao_id) REFERENCES cidadao(id)
  );
  ```

- **Repositório**: AlocacaoRepository.

- **Serviço**: AlocacaoService com validações e cálculo de eficiência.

- **Testes**:
  - Alocar cidadão válido.
  - Rejeitar por vaga cheia.
  - Rejeitar por idade.
  - Rejeitar por tropa.
  - Desalocar com sucesso.

## Frontend

Não se aplica (UI em tarefa h-004-tarefa-002).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoAlocacao.java](/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoAlocacao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/construcao/AlocacaoService.java](/src/main/java/com/example/loginbase/jogo/construcao/AlocacaoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/construcao/AlocacaoController.java](/src/main/java/com/example/loginbase/jogo/construcao/AlocacaoController.java) (novo)
- [/src/test/java/com/example/loginbase/jogo/construcao/AlocacaoServiceTest.java](/src/test/java/com/example/loginbase/jogo/construcao/AlocacaoServiceTest.java) (novo)

## Testes

- `testAlocarCidadaoValido_Sucesso`.
- `testAlocarSemVagas_Erro`.
- `testAlocarMenorDeIdade_Erro`.
- `testAlocarTropaEmMembro_Erro`.
- `testDesalocar_Sucesso`.

## Definição de pronto

- Critérios CA1, CA2, CA3, CA4, CA5 cobertos.
- Testes passando.

## Fora de escopo

- Sugestão automática de alocação.
