# H-002 · Tarefa 001 — API de relatório de batalha

**História:** [H-002 — Ver relatório de batalha](h-002-ver-relatorio-de-batalha.md) · **Domínio:** [batalha.md](../batalha.md) ·
**Depende de:** [H-001 · Tarefa 003 — Consequências para abatidos](h-001-tarefa-003-consequencias-para-abatidos.md) · **Camada:** Backend

## Objetivo

Implementar endpoints REST para listar histórico de batalhas e detalhar replay rodada a rodada, incluindo recompensas obtidas.

## Contexto necessário

- Log da batalha (seção 10.5)
  > Estrutura: {participantes_iniciais, acoes: [{rodada, atacante, alvo, dano, critico, pv_alvo_apos}], resultado}

- Recompensas por nível (seção 8.4)
  > - Ouro: `40 × N + aleatório(0..20 × N)`
  > - Recursos: N sorteios de `10 × N` unidades (Madeira, Pedra, Ferro, Couro curtido, Tecido, Aço a partir de N6)
  > - Item: chance `min(80%; 10% × N)` de L = N (qualidade por margem 0–4 ou 15+; Divina permitida)
  > - XP: cada sobrevivente ganha N XP de Guerreiro
  > - Pedras: `1 + floor(N ÷ 3)` sorteios com probabilidades por nível

- Tabela `batalha` (11.2): vi_id, tropa_id, masmorra_id, turno, semente, resultado, log (jsonb), recompensas (jsonb)

## Backend

**Controller**: `BatalhaController`

Endpoints:

1. **GET /api/jogo/batalhas** — Listar histórico
   - Query params: nenhum (filtros futuros)
   - Response: `{ "batalhas": [ { "id": 1, "data_turno": 42, "masmorra_nivel": 5, "resultado": "VITORIA", "tropa_nome": "Guerreiros da vila" }, ... ] }`
   - Filtro: apenas batalhas da vila do usuário logado

2. **GET /api/jogo/batalhas/{id}** — Detalhar replay + recompensas
   - Response:
   ```json
   {
     "id": 1,
     "resultado": "VITORIA",
     "masmorra_nivel": 5,
     "rodadas": [
       {
         "numero": 1,
         "acoes": [
           {
             "atacante": { "tipo": "GUERREIRO", "id": 123, "nome": "João" },
             "alvo": { "tipo": "INIMIGO", "classe": "GOBLIN", "id": 0 },
             "dano": 23,
             "critico": false,
             "pv_alvo_antes": 40,
             "pv_alvo_depois": 17
           },
           ...
         ]
       },
       ...
     ],
     "recompensas": {
       "ouro": 250,
       "recursos": [ { "tipo": "MADEIRA", "quantidade": 50 }, ... ],
       "itens": [ { "id": 99, "nome": "Espada L5", "qualidade": "BOA" } ],
       "xp_guerreiro": [ { "cidadao_id": 123, "xp": 5 } ],
       "pedras": [ { "qualidade": "BOA", "bonus": [...] }, ... ]
     }
   }
   ```

**Serviço**: `BatalhaService`

Métodos:
- `listarBatadasVila(vila_id) → List<ResuemBatalha>`
  - Consulta tabela `batalha` filtrado por vila_id
  - Ordena por turno DESC

- `obterDetalheBatalha(batalha_id, vila_id) → DetalheBatalha`
  - Carrega batalha (verifica vila_id para segurança)
  - Parse JSON do log para estrutura tipada
  - Parse JSON de recompensas
  - Retorna DTO com todos os campos

- `distribuirRecompensas(batalha: Batalha, tropa: Tropa, vila: Vila) → void`
  - Baseado em resultado e nível da masmorra (já em batalha.recompensas)
  - Adiciona ouro ao estoque
  - Adiciona recursos ao estoque
  - Cria itens no inventário
  - Adiciona XP aos cidadãos guerreiros
  - Cria pedras no inventário da vila

**Modelo de dados**:

DTOs:
- `ResuemBatalha { id, data_turno, masmorra_nivel, resultado, tropa_nome }`
- `DetalheBatalha { id, resultado, masmorra_nivel, rodadas, recompensas }`
- `Rodada { numero, acoes: List<AcaoBatalha> }`
- `AcaoBatalha { atacante, alvo, dano, critico, pv_antes, pv_depois }`
- `Recompensas { ouro, recursos, itens, xp_guerreiro, pedras }`

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/batalha/BatalhaController.java](/src/main/java/com/example/loginbase/jogo/batalha/BatalhaController.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/BatalhaService.java](/src/main/java/com/example/loginbase/jogo/batalha/BatalhaService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/dto/ResuemBatalha.java](/src/main/java/com/example/loginbase/jogo/batalha/dto/ResuemBatalha.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/dto/DetalheBatalha.java](/src/main/java/com/example/loginbase/jogo/batalha/dto/DetalheBatalha.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/BatalhaRepository.java](/src/main/java/com/example/loginbase/jogo/batalha/BatalhaRepository.java) (atualização com findByVilaId)
- [/src/test/java/com/example/loginbase/jogo/batalha/BatalhaControllerTest.java](/src/test/java/com/example/loginbase/jogo/batalha/BatalhaControllerTest.java) (novo)

## Testes

1. **GET /api/jogo/batalhas**: vila com 3 batalhas
   - Esperado: status 200, array com 3 itens, ordenado por turno DESC

2. **GET /api/jogo/batalhas/{id}** (vitória N5):
   - Esperado: status 200, resultado "VITORIA", rodadas com ações, recompensas com ouro ~250

3. **GET /api/jogo/batalhas/{id}** (derrota):
   - Esperado: status 200, resultado "DERROTA", recompensas vazias ou nulas

4. **Segurança**: tentar acessar batalha de outra vila
   - Esperado: status 403 ou 404

5. **Parse de log**: verificar que acoes_rodada[0] correspondem ao JSON armazenado
   - Esperado: deserialização correta, danos e PVs coincidem

## Definição de pronto

- Critérios de aceite da história cobertos: CA1, CA2, CA3, CA4, CA5
- Build do backend sem erros
- Testes listados passando
- API documentada (ex.: Swagger ou comentários)
- Integração com [Consequências](h-001-tarefa-003-consequencias-para-abatidos.md)
- Segurança: filtro por vila do usuário logado

## Fora de escopo

- Filtros avançados
- Paginação de histórico (futura)
- Export (CSV, PDF)
