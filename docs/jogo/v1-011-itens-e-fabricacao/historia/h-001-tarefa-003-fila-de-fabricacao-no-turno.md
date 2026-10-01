# H-001 · Tarefa 003 — Fila de fabricação no turno

**História:** [H-001 — Fabricar item na oficina](h-001-fabricar-item-na-oficina.md) · **Domínio:** [../itens.md](../itens.md), [../fabricacao.md](../fabricacao.md) · **Depende de:** [h-001-tarefa-001-modelo-de-dados-de-itens.md](h-001-tarefa-001-modelo-de-dados-de-itens.md), [h-001-tarefa-002-gerador-de-itens-qualidade-e-bonus.md](h-001-tarefa-002-gerador-de-itens-qualidade-e-bonus.md) · **Camada:** Backend

## Objetivo

Implementar a etapa 6 do turno ("Fabricação", seção 2.2): avançar a fila de fabricação em cada oficina por vila, resolver conclusões de itens, e movê-los para o inventário.

## Contexto necessário

- [Etapa 6 do turno (seção 2.2)](../../../roadmap.md#ordem-de-resolução-por-vila)
  > Etapa 6: **Fabricação**: progresso das filas das oficinas; conclusão de itens.

- [Tabela de PF (seção 7.4)](../fabricacao.md#pontos-de-fabricação-e-tempo)
  > PF = 1 + L
  > Progresso por turno = eficiência do artesão × mult. do nível da oficina

## Backend

- Etapa `FabricacaoEtapaTurno` (implementa `EtapaTurno`, precedência 6):
  - Por vila, carregar todas as filas de fabricação (tabela `fabricacao`)
  - Para cada item em fabricação:
    - Carregar eficiência do artesão (cidadão_id)
    - Carregar multiplicador da oficina (construcao_id → nivel → mult)
    - Progresso += eficiência × mult
    - Se progresso >= pf_total:
      - Item concluído: sortear qualidade e bônus (serviço ItemBonusGerador)
      - Criar registro Item (categoria, subtipo, nivel, qualidade, bonus)
      - Adicionar ao inventário (cidadao_id = null)
      - Remover de Fabricacao
      - Registrar evento de conclusão no relatório do turno
    - Senão: atualizar pf_atual na tabela Fabricacao

- Injetar dependências:
  - `FabricacaoRepository`
  - `ItemRepository`
  - `CidadaoRepository` (para eficiência)
  - `ConstrucaoRepository` (para multiplicador)
  - `ItemBonusGerador` (para sorteio)
  - `EventoTurnoRepository` (para registrar eventos)

- Tratamento de erro: se artesão não encontrado (morreu?), pausar fila com evento de erro

## Frontend

- Não se aplica (lógica de servidor)

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/item/FabricacaoEtapaTurno.java](/src/main/java/com/example/loginbase/jogo/item/FabricacaoEtapaTurno.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EtapaTurno.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaTurno.java) (existente, adicionar interface)

## Testes

- Teste de progressão de fila: 1 turno, eficiência 1,0, nível da oficina 1,0 → PF avança conforme L1 (2), L5 (6), L10 (11)
- Teste de conclusão: fila com PF=2 e progresso 1,0 por turno → item concluído após 2 turnos
- Teste de sorteio de qualidade: verificar se ItemBonusGerador é chamado com margem correta
- Teste de artesão morto: pausar fabricação com evento de erro
- Teste de múltiplas filas: 2 oficinas em paralelo, ambas avançam no mesmo turno

## Definição de pronto

- Critérios de aceite da história cobertos: CA6 (conclusão após PF)
- Build (`./mvnw verify`) sem erros
- Testes de integração passando
- Etapa integrada ao pipeline de turno (EtapaTurno #6)
- Eventos de conclusão registrados no relatório

## Fora de escopo

- API REST (tarefa 004)
- Frontend (tarefa 004)
- Aprimoramento (tarefa h-003-tarefa-001)
