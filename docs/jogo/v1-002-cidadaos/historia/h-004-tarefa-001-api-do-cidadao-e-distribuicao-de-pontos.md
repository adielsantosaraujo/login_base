# H-004 · Tarefa 001 — API do cidadão e distribuição de pontos

**História:** [H-004 — Consultar painel do cidadão](h-004-consultar-painel-do-cidadao.md) · **Domínio:** [../cidadao.md](../cidadao.md) · **Depende de:** [h-003-tarefa-003-imigracao-pela-estalagem.md](h-003-tarefa-003-imigracao-pela-estalagem.md) · **Camada:** Backend

## Objetivo

Criar endpoints REST para ler dados de cidadão (incluindo características, profissões, itens) e distribuir pontos pendentes.

## Contexto necessário

- [cidadao.md](../cidadao.md) — PE efetivo, características, profissões
  > PE efetivo = PE base + Σ bônus características + bônus ferramenta + bônus itens/pedras.

- [itens.md](../../v1-011-itens-e-fabricacao/itens.md) — bônus de itens
  > Itens dão bônus PROF (profissão), PROD (eficiência), características.

## Backend

**Endpoints**:
- `GET /api/jogo/cidadao/{cidadaoId}`
  - Resposta: `{ id, nome, idade_anos, sexo, familia_id, familia_nome, estado, características: { vit: { base: 5, total: 8, itens: 1, pedras: 2 }, ... }, profissoes: { construtor: { base: 6, efetiva: 11, xp_guerreiro: 50 }, ... }, pontos_car_pendentes, pontos_prof_pendentes, itens_equipados: { arma, armaduras: [...], joias: [...], ferramenta }, atributos_combate (se guerreiro): { pv_max, ataque, defesa, iniciativa, critico } }`
  - Validação: cidadão deve estar na vila do usuário logado.

- `POST /api/jogo/cidadao/{cidadaoId}/distribuir-pontos`
  - Request: `{ caracteristicas: { vit: 1, for: 2, ... }, profissoes: { construtor: 1, agricultor: 0, ... } }`
  - Response: cidadão atualizado.
  - Validação:
    - Σ características = pontos_car_pendentes.
    - Σ profissões = pontos_prof_pendentes.
    - Nenhuma característica recebe > pontos pedidos.
    - Idempotent: se chamado 2x com mesmos pontos, resultado igual.

**Serviço** (`com.example.loginbase.jogo.cidadao.CidadaoService`):
- `getCidadaoComDetalhes(cidadaoId: Long): CidadaoDTO`
  - Carrega cidadão, família, características, profissões, itens equipados.
  - Calcula PE efetivo por profissão (base + bônus características + ferramenta + itens).
  - Calcula atributos de combate (se guerreiro).

- `distribuirPontos(cidadaoId: Long, caract: Map<String, Int>, prof: Map<String, Int>): Cidadao`
  - Valida totais.
  - Atualiza cidadao.vit/for/vel/int/car e cidadao_profissao.pontos_base.
  - Zera pontos_car_pendentes e pontos_prof_pendentes.
  - Salva em BD.

## Frontend

Não se aplica (backend apenas; UI em tarefa 002).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoDTO.java](/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoDTO.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoController.java](/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoController.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoService.java](/src/main/java/com/example/loginbase/jogo/cidadao/CidadaoService.java) (novo/atualizado)

## Testes

- Teste: GET `/api/jogo/cidadao/1` retorna cidadão com dados corretos.
- Teste: características base e total calculadas corretamente (com itens/pedras).
- Teste: PE efetivo por profissão calculado (base + bônus INT + ferramenta + itens).
- Teste: POST distribuir pontos com valid request → sucesso.
- Teste: POST distribuir > pontos disponíveis → erro 400.
- Teste: POST distribuir = pontos → zera pendentes.
- Teste: cidadão de outra vila → erro 403.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA4 (parcial).
- Build do backend (`./mvnw verify`) sem erros.
- Testes listados passando.
- PE efetivo conforme seção 5.2.
- Atributos de combate conforme seção 10.1 (se guerreiro).

## Fora de escopo

- Equipar/trocar itens (tarefa 002).
- Relatório de XP de guerreiro (será em troopa/batalha).
