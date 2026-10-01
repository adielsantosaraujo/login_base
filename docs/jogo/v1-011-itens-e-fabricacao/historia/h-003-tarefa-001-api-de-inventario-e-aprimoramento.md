# H-003 · Tarefa 001 — API de inventário e aprimoramento

**História:** [H-003 — Gerenciar inventário e aprimorar itens](h-003-gerenciar-inventario-e-aprimorar-itens.md) · **Domínio:** [../itens.md](../itens.md), [../fabricacao.md](../fabricacao.md), [../equipamento.md](../equipamento.md) · **Depende de:** [h-001-tarefa-001-modelo-de-dados-de-itens.md](../historia/h-001-tarefa-001-modelo-de-dados-de-itens.md) · **Camada:** Backend

## Objetivo

Implementar endpoints REST para listar inventário com filtros e aplicar aprimoramento (L→L+1) de itens com validação de requisitos e custos.

## Contexto necessário

- [Inventário (seção 7.6)](../equipamento.md#inventário-da-vila)
  > Itens não equipados no inventário da vila; capacidade ilimitada em v1

- [Aprimoramento (seção 7.4)](../fabricacao.md#aprimoramento-de-item)
  > Custa 50% do custo de fabricar L+1
  > Requisitos: mesmos de fabricar L+1 (PE efetivo ≥ 2(L+1)−2)
  > Mantém qualidade, bônus, pedras
  > Máximo L10

- [CA1–CA6 de H-003](h-003-gerenciar-inventario-e-aprimorar-itens.md#critérios-de-aceite)
  > Filtros: categoria
  > Validações: PE, recursos, máximo L10

## Backend

- Controller `InventarioController`:
  - `GET /api/jogo/inventario` (query params: categoria?, page?, size?)
    - Retorna itens com cidadao_id == null, paginados
    - Opcional: filtrar por categoria (ARMA/ARMADURA/etc.)
    - Response: { items: [...], total, page, pageSize }

  - `GET /api/jogo/inventario/{itemId}`
    - Retorna detalhes completo do item (categoria, subtipo, nível, qualidade, bônus, pedras engastadas)
    - Response: { id, categoria, subtipo, nivel, qualidade, bonus: [...], pedras: [...] }

- Controller `AprimoramentoController`:
  - `POST /api/jogo/inventario/{itemId}/aprimorar` (body: vazio ou { oficina_id? })
    - Validar que item pertence à vila do usuário
    - Validar PE mínimo ≥ 2(L+1)−2
    - Validar recursos: 50% × custo L+1
    - Criar registro Fabricacao com nivel = L+1, pf_total = 1+(L+1)
    - Mover item para fabricação (cidadao_id = null, mas "em aprimoramento")
    - Response: { success, fabricacao, item_novo?, erro?: { codigo, mensagem } }

  - Alternativa: usar POST `/api/jogo/oficinas/{oficina_id}/aprimoramentos` (body: itemId)
    - Mesma lógica, mas especificar oficina (validar que é compatível)

- Serviço `AprimoramentoService`:
  - Método `validarAprimoramento(item, artesao, oficina): ValidationResult`
    - PE mínimo ≥ 2(L+1)−2
    - Nível L < 10
    - Oficina correta (Ferraria, Alfaiataria ou Carpintaria conforme item)
    - Recursos suficientes

  - Método `calcularCustoAprimoramento(item, catalog): ResourceCost`
    - Retorna recursos = 50% × custo L+1 × (receita_base)

- Repositório `ItemRepository`:
  - Query `findByVila(vilaId, categoria, pageable)`
  - Query `findInventarioByVila(vilaId, pageable)`

## Frontend

- Não se aplica nesta tarefa (API pura)

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/item/InventarioController.java](/src/main/java/com/example/loginbase/jogo/item/InventarioController.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/item/AprimoramentoController.java](/src/main/java/com/example/loginbase/jogo/item/AprimoramentoController.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/item/AprimoramentoService.java](/src/main/java/com/example/loginbase/jogo/item/AprimoramentoService.java) (novo)

## Testes

- Teste de listagem: GET retorna itens pagos, com cidadao_id == null
- Teste de filtro: categoria ARMA retorna apenas armas
- Teste de aprimoramento: L1 → L2 custa 50% do custo L2
- Teste de validação: PE insuficiente → erro `pe`
- Teste de validação: recursos insuficientes → erro `recursos`
- Teste de validação: L10 → L11 → erro `limite`
- Teste de manutenção: qualidade, bônus, pedras preservados após aprimoramento

## Definição de pronto

- Critérios de aceite da história cobertos: CA1–CA6
- Build (`./mvnw verify`) sem erros
- Testes passando (cobertura >80%)
- Paginação funcional
- Mensagens de erro claras

## Fora de escopo

- Frontend (tarefa 002)
- Cancelamento de aprimoramento
