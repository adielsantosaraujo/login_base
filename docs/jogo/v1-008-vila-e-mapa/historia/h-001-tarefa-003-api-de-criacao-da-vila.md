# H-001 · Tarefa 003 — API de criação da vila

**História:** [h-001-criar-vila-escolhendo-regioes-iniciais.md](h-001-criar-vila-escolhendo-regioes-iniciais.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md), [h-001-tarefa-002-geracao-de-jazidas-por-semente.md](h-001-tarefa-002-geracao-de-jazidas-por-semente.md) · **Camada:** Backend

## Objetivo

Implementar endpoint `POST /api/jogo/vila` que valida as 3 regiões escolhidas (adjacência, tipo Urbana obrigatório), cria a vila com recursos e casas iniciais, gera jazidas.

## Contexto necessário

- [../vila.md](../vila.md) — regras de criação (seção 1.3, 4.7) e recursos iniciais (3.2)
  > Escolha: 1ª qualquer, 2ª e 3ª adjacentes; ≥1 Urbana.
  > Recursos iniciais: Madeira 200, Pedra 100, Argila 50, Tábua 20, Grãos 200, Carne 40, Ouro 200.
  > Casas iniciais: 4 N1 nos ladrilhos (0,0), (2,0), (4,0), (6,0) da 1ª região Urbana.

- [../regioes.md](../regioes.md) — adjacência ortogonal (seção 1.2)
  > Região 6 é adjacente a 2, 5, 7, 10.

## Backend

**Controlador (novo):**
- [/src/main/java/com/example/loginbase/jogo/controlador/VilaControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/VilaControlador.java) (novo)
  - Endpoint: `POST /api/jogo/vila`
  - Corpo: `{ "regioesEscolhidas": [6, 7, 2], "tipos": { "6": "URBANA", "7": "RURAL", "2": "COLETA" } }`
  - Resposta sucesso (201): `{ "vilaId": "uuid...", "nome": "Vila do Jogador X", "regioes": [...], "estoque": {...} }`
  - Resposta erro (400/409): `{ "erro": "..." }`

**Serviço (novo):**
- [/src/main/java/com/example/loginbase/jogo/servico/VilaService.java](/src/main/java/com/example/loginbase/jogo/servico/VilaService.java) (novo)
  - Método: `Vila criarVila(Long usuarioId, List<Integer> indices, Map<Integer, TipoRegiao> tipos)` throws VilaJaExisteException, RegiaoNaoAdjacenteException, etc.
  - Validações:
    1. Usuário não tem vila (uniqueness username → usuario_id)
    2. indices.size() == 3
    3. 1ª região: qualquer (1-16)
    4. 2ª região: adjacente ortogonalmente à 1ª
    5. 3ª região: adjacente à 1ª ou 2ª
    6. ≥1 tipo == URBANA
    7. Indices válidos (1-16)
  - Criação: 
    - Gerar semente aleatória
    - Criar Vila (id=UUID, usuario_id, semente, turno_criacao=1, bem_alimentada=false)
    - Criar 3 regiões com tipos escolhidos (possuida=true)
    - Gerar jazidas para essas 3 regiões
    - Criar 13 regiões vazias (possuida=false)
    - Criar estoque com recursos iniciais (Madeira 200, ...)
    - **Criar 4 casas N1**: na 1ª região Urbana, ladrilhos (0,0), (2,0), (4,0), (6,0)
    - Retornar Vila

**Entidades auxiliares:**
- Exceções: `VilaJaExisteException`, `RegiaoNaoAdjacenteException`, `UrbanaObrigatoriaException`

## Frontend

Não se aplica (será chamado pelo frontend h-001-tarefa-004).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/controlador/VilaControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/VilaControlador.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/servico/VilaService.java](/src/main/java/com/example/loginbase/jogo/servico/VilaService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/excecao/VilaJaExisteException.java](/src/main/java/com/example/loginbase/jogo/excecao/VilaJaExisteException.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/excecao/RegiaoNaoAdjacenteException.java](/src/main/java/com/example/loginbase/jogo/excecao/RegiaoNaoAdjacenteException.java) (novo)

## Testes

- **Teste de integração**: POST com 3 regiões válidas (6, 7, 2; tipos URBANA, URBANA, COLETA) → 201, vila criada, recursos iniciais corretos, 4 casas em (0,0), (2,0), (4,0), (6,0).
- **Teste de validação**: POST com região 1 e 3 (não adjacentes) → 400, mensagem "segunda região não é adjacente".
- **Teste de validação**: POST sem Urbana (Rural, Rural, Coleta) → 400, "ao menos 1 Urbana obrigatória".
- **Teste de unicidade**: usuário com vila já criada tenta POST novamente → 409, "usuário já tem vila".

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA5
- Build do backend (`./mvnw verify`) sem erros
- Testes listados passando
- Endpoint documentado em Swagger/OpenAPI

## Fora de escopo

- Distribuição automática de pontos da família inicial (tarefa posterior).
- Batismo de vila (pode usar nome padrão ou deixar para tela).
