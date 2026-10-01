# H-002 · Tarefa 001 — API de engaste

**História:** [H-002 — Engastar pedra em item](h-002-engastar-pedra-em-item.md) · **Domínio:** [../pedras-de-bonus.md](../pedras-de-bonus.md) ·
**Depende de:** [h-001-tarefa-001-modelo-e-gerador-de-pedras.md](h-001-tarefa-001-modelo-e-gerador-de-pedras.md), [../../v1-011-itens-e-fabricacao/historia/h-001-tarefa-001-modelo-de-dados-de-itens.md](../../v1-011-itens-e-fabricacao/historia/h-001-tarefa-001-modelo-de-dados-de-itens.md) · **Camada:** Backend

## Objetivo

Implementar endpoints REST para engaste e remoção de pedras em itens, com validações de slot, tipo de item, custo e permissões.

## Contexto necessário

- [Pedras de bônus — tipos e custos](../pedras-de-bonus.md)
  > Simples: 1 bônus, 10 Ouro. Boa: 2 bônus, 25 Ouro. Excelente: 3 bônus, 60 Ouro. Divina: 4 bônus, 150 Ouro.

- [Qualidade de item e slots](../../v1-011-itens-e-fabricacao/itens.md)
  > Simples: 0 slots. Boa: 1 slot. Excelente: 3 slots. Divina: 5 slots.

- [Regra de engaste](../pedras-de-bonus.md)
  > Ferraria qualquer nível, imediato, permanente, custa Ouro.

## Backend

### Endpoints REST

#### **POST /api/jogo/ferraria/engaste** [proposta]

Engasta uma pedra em um item na Ferraria.

**Corpo de requisição (JSON):**
```json
{
  "pedra_id": 12345,
  "item_id": 67890
}
```

**Respostas de sucesso (200 OK):**
```json
{
  "pedra_id": 12345,
  "item_id": 67890,
  "custo_ouro": 25,
  "qualidade_pedra": "BOA",
  "bonus": [
    {"codigo": "VIT", "magnitude": "BAIXA"},
    {"codigo": "ATK", "magnitude": "BAIXA"}
  ],
  "slots_livres_apos": 0
}
```

**Respostas de erro:**

- **400 Bad Request** — item Simples ou sem slot livre:
  ```json
  {
    "erro": "Itens Simples não aceitam pedras.",
    "codigo": "ITEM_SIMPLES_SEM_SLOT"
  }
  ```

- **400 Bad Request** — pedra já engastada:
  ```json
  {
    "erro": "A pedra já está engastada em outro item.",
    "codigo": "PEDRA_JA_ENGASTADA"
  }
  ```

- **400 Bad Request** — ouro insuficiente:
  ```json
  {
    "erro": "Ouro insuficiente. Necessário 25, disponível 15.",
    "codigo": "OURO_INSUFICIENTE"
  }
  ```

- **404 Not Found** — pedra ou item não existe:
  ```json
  {
    "erro": "Pedra ou item não encontrado.",
    "codigo": "RECURSO_NAO_ENCONTRADO"
  }
  ```

- **403 Forbidden** — pedra ou item não pertence à vila do usuário:
  ```json
  {
    "erro": "Acesso negado.",
    "codigo": "ACESSO_NEGADO"
  }
  ```

**Lógica:**
1. Validar que item e pedra existem e pertencem à vila do usuário autenticado.
2. Validar que item tem qualidade ≠ Simples e slot livre.
3. Validar que pedra não está já engastada.
4. Obter custo de engaste conforme tipo de pedra.
5. Validar estoque de Ouro.
6. Debitar ouro.
7. Atualizar pedra: `item_id = <id do item>`.
8. Registrar no evento do turno (opcional).

#### **POST /api/jogo/ferraria/remover-pedra** [proposta]

Remove uma pedra engastada de um item (e destrói a pedra permanentemente).

**Corpo de requisição (JSON):**
```json
{
  "item_id": 67890,
  "slot_index": 0
}
```

**Respostas de sucesso (200 OK):**
```json
{
  "item_id": 67890,
  "pedra_removida_id": 12345,
  "slots_livres_apos": 1,
  "pedra_destruida": true
}
```

**Respostas de erro:**

- **400 Bad Request** — slot não contém pedra:
  ```json
  {
    "erro": "O slot especificado não contém uma pedra.",
    "codigo": "SLOT_VAZIO"
  }
  ```

- **404 Not Found** — item não existe:
  ```json
  {
    "erro": "Item não encontrado.",
    "codigo": "RECURSO_NAO_ENCONTRADO"
  }
  ```

- **403 Forbidden** — item não pertence à vila:
  ```json
  {
    "erro": "Acesso negado.",
    "codigo": "ACESSO_NEGADO"
  }
  ```

**Lógica:**
1. Validar que item existe e pertence à vila do usuário.
2. Validar que slot contém uma pedra.
3. Remover a ligação pedra-item (`pedra.item_id = null`).
4. Deletar a pedra da base de dados (destruição permanente).
5. Retornar confirmação.

### Serviço de engaste

**Classe `EngasteService`**:

```java
public class EngasteService {
  
  // Validações e execução de engaste
  public void engastar(Long vilaId, Long pedraId, Long itemId) throws EngasteException;
  
  // Remoção de pedra
  public void removerPedra(Long vilaId, Long itemId, int slotIndex) throws EngasteException;
  
  // Validação de slot livre
  boolean temSlotLivre(Item item);
  
  // Custo conforme tipo
  int obterCustoEngaste(Pedra pedra);
}
```

## Frontend

Não se aplica nesta tarefa. (Frontend será em H-002-tarefa-002.)

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/ferraria/EngasteController.java](/src/main/java/com/example/loginbase/jogo/ferraria/EngasteController.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/ferraria/EngasteService.java](/src/main/java/com/example/loginbase/jogo/ferraria/EngasteService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/ferraria/EngasteException.java](/src/main/java/com/example/loginbase/jogo/ferraria/EngasteException.java) (novo)
- [/src/test/java/com/example/loginbase/jogo/ferraria/EngasteServiceTest.java](/src/test/java/com/example/loginbase/jogo/ferraria/EngasteServiceTest.java) (novo)

## Testes

- **Teste de integração: `testeEngasteComOuroSuficiente()`**
  - Cria vila, item Boa, pedra Simples.
  - Chama `POST /api/jogo/ferraria/engaste`.
  - Valida: pedra engastada, ouro debitado, resposta 200.

- **Teste de integração: `testeEngasteRejeitadoItemSimples()`**
  - Item de qualidade Simples.
  - Tenta engastar.
  - Valida: erro 400, código `ITEM_SIMPLES_SEM_SLOT`.

- **Teste de integração: `testeEngasteRejeitadoOuroInsuficiente()`**
  - Pedra Divina (150 Ouro), estoque com 100.
  - Tenta engastar.
  - Valida: erro 400, código `OURO_INSUFICIENTE`.

- **Teste de integração: `testeRemocaoPermanente()`**
  - Engasta pedra com sucesso.
  - Remove via `POST /api/jogo/ferraria/remover-pedra`.
  - Valida: pedra não existe no banco; slot fica livre; resposta 200.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1, CA2, CA3, CA4, CA5.
- Build do backend (`./mvnw verify`) sem erros.
- Testes de integração listados acima passando.
- Endpoints seguem padrão REST (`/api/jogo/...`).
- Validações de acesso (usuário autenticado, vila do usuário).

## Fora de escopo

- Interface visual (tarefa 002).
- Desconto de custo por nível de Ferreiro.
- Histórico de engastes.
