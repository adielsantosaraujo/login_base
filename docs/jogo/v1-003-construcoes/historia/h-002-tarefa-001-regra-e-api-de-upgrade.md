# H-002 · Tarefa 001 — Regra e API de upgrade

**História:** [H-002 — Melhorar prédio de nível](h-002-melhorar-predio-de-nivel.md) · **Domínio:** [../construcoes.md](../construcoes.md) · **Depende de:** [h-001-tarefa-002-api-de-construcao-e-posicionamento.md](h-001-tarefa-002-api-de-construcao-e-posicionamento.md) · **Camada:** Backend

## Objetivo

Implementar endpoint `POST /api/jogo/construcoes/{id}/upgrade` que altera prédio ATIVA para EM_UPGRADE com novo nível, validando espaço disponível e debitando custos.

## Contexto necessário

- [../construcoes.md](../construcoes.md) — Upgrade (seção 4.1)
  > Custo N1→N2 = 2,5×; N2→N3 = 5×. Área 2x2 ou 3x3 que contenha atual, ladrilhos livres.

## Backend

- **POST /api/jogo/construcoes/{id}/upgrade**
  - Request: `{ novoNivel: "N2", novaX: 0, novaY: 0 }`
  - Validações:
    - Construção deve estar ATIVA.
    - Novo nível deve ser N+1 (N1→N2 ou N2→N3).
    - Espaço 2×2 (N2) ou 3×3 (N3) a partir (novaX, novaY) deve ser livre.
    - Estoque deve ter recursos suficientes.
  - Alterações: muda estado para EM_UPGRADE, atualiza nível, x, y, tamanho, reset po_atual, debitamento.
  - Response (200): prédio atualizado.

- **Etapa de turno**: integra EtapaObras para processar upgrades como obras.

- **Testes**:
  - Upgrade N1→N2 com espaço livre → sucesso.
  - Upgrade com espaço ocupado → erro.
  - Upgrade N1→N3 (skip N2) → erro.
  - Múltiplos upgrades no mesmo turno → ambos processados.

## Frontend

Não se aplica (UI em tarefa h-002-tarefa-002).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoService.java](/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoService.java) (modificar método upgrade)
- [/src/test/java/com/example/loginbase/jogo/construcao/ConstrucaoServiceTest.java](/src/test/java/com/example/loginbase/jogo/construcao/ConstrucaoServiceTest.java) (adicionar testes)

## Testes

- `testUpgradeN1toN2_Sucesso`.
- `testUpgradeComEspacoOcupado_Erro`.
- `testUpgradeN1toN3_Erro` (skip de nível).
- `testUpgradeRecursoInsuficiente_Erro`.

## Definição de pronto

- Critérios CA1, CA2, CA3 cobertos.
- Testes passando.
- Integração com pipeline de turno.

## Fora de escopo

- Cancelamento de upgrade.
