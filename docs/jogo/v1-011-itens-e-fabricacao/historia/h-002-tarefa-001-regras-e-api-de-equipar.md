# H-002 · Tarefa 001 — Regras e API de equipar

**História:** [H-002 — Equipar itens no painel da pessoa](h-002-equipar-itens-no-painel-da-pessoa.md) · **Domínio:** [../itens.md](../itens.md), [../equipamento.md](../equipamento.md) · **Depende de:** [h-001-tarefa-001-modelo-de-dados-de-itens.md](../historia/h-001-tarefa-001-modelo-de-dados-de-itens.md) · **Camada:** Backend

## Objetivo

Implementar serviço de validação e API REST para equipar/trocar itens no painel da pessoa, validando requisitos de idade, PE, limite de slots e estado de tropa.

## Contexto necessário

- [Requisitos para equipar (seção 7.5)](../equipamento.md#requisitos-para-equipar)
  > Idade ≥14 (ferramentas/joias); ≥16 (armas/armaduras)
  > PE efetivo: Guerreiro ≥ L−1 (armas/armaduras); PE base profissão ≥ L−1 (ferramentas)
  > Joias sem requisito

- [Limitações (seções 7.1, 7.7)](../equipamento.md#limitações-especiais)
  > 1 Colar máximo; 2 Anéis máximo
  > Não trocar equipamento se membro de tropa em expedição (estados EM_VIAGEM_IDA ou EM_VIAGEM_VOLTA)

- [CA1–CA6 de H-002](h-002-equipar-itens-no-painel-da-pessoa.md#critérios-de-aceite)
  > Validações de slot, limite de anéis, PE, idade, tropa em expedição

## Backend

- Serviço `EquipamentoService`:
  - Método `validarEquipamento(pessoa, item, slot): ValidationResult`
    - Verifica categoria × slot (arma → slot Arma, etc.)
    - Verifica idade mínima (slot.getIdadeMinima())
    - Verifica PE mínimo (se arma/armadura/ferramenta)
    - Retorna `ValidationResult { success, erro?: { codigo, mensagem } }`

  - Método `validarLimiteSlots(pessoa, slot): ValidationResult`
    - Se Anel: conta anéis já equipados; rejeita se >= 2
    - Se Colar: rejeita se já tem 1 equipado
    - Senão: sem limite

  - Método `validarTropaEmExpedicao(pessoa): ValidationResult`
    - Se pessoa em tropa com estado EM_VIAGEM_IDA ou EM_VIAGEM_VOLTA: rejeitar
    - Senão: ok

  - Método `equipar(pessoa, item, slot): Item`
    - Executa todas as validações (acima)
    - Se item anterior no slot: move para inventário
    - Persiste novo equipamento
    - Retorna item equipado

- Controller `EquipamentoController`:
  - `PUT /api/jogo/pessoas/{pessoaId}/equipamento/{slot}` (body: itemId)
    - Validar que pessoa pertence à vila do usuário
    - Chamar `equipar()`
    - Response: { success, item, pessoa, erro?: { codigo, mensagem } }

- Enums e validações centralizadas em `jogo.catalogo`

## Frontend

- Não se aplica nesta tarefa (regras de servidor)

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/item/EquipamentoService.java](/src/main/java/com/example/loginbase/jogo/item/EquipamentoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/item/EquipamentoController.java](/src/main/java/com/example/loginbase/jogo/item/EquipamentoController.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/item/ValidationResult.java](/src/main/java/com/example/loginbase/jogo/item/ValidationResult.java) (novo)

## Testes

- Teste de validação: idade 13 com ferramenta → rejeita
- Teste de validação: PE Guerreiro 2 com Espada L5 (requisito 4) → rejeita
- Teste de limite de anéis: 2 equipados + tentativa de 3º → rejeita
- Teste de trocar: item anterior move para inventário
- Teste de expedição: membro de tropa EM_VIAGEM_IDA → rejeita troca
- Teste de sucesso: equipamento validado e persistido

## Definição de pronto

- Critérios de aceite da história cobertos: CA1–CA6
- Build (`./mvnw verify`) sem erros
- Testes passando (cobertura >80%)
- Validações centralizadas em `catalogo`
- Mensagens de erro claras em português

## Fora de escopo

- Interface frontend (tarefa 002)
- Remoção de item (deixar slot vazio)
- Reordenação de anéis
