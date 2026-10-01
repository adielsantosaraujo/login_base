# H-002 · Tarefa 002 — Produção de coleta e rural

**História:** [h-002-produzir-recursos-nos-predios.md](h-002-produzir-recursos-nos-predios.md) · **Domínio:** [../producao.md](../producao.md) ·
**Depende de:** [h-002-tarefa-001-calculo-de-eficiencia-do-trabalhador.md](h-002-tarefa-001-calculo-de-eficiencia-do-trabalhador.md) · **Camada:** Backend

## Objetivo

Implementar a etapa 1 do turno (passo de produção de coleta e rural) que calcula recurso produzido por cada prédio de coleta e Fazenda, respeita limite de ladrilhos marcados (2 ladrilhos por trabalhador produtivo), e soma ao estoque.

## Contexto necessário

- [../producao.md#números-e-tabelas](../producao.md#números-e-tabelas) — tabela 4.5, produção base por prédio (ex.: Acampamento 5 Madeira)
  > Fórmula: produção = Σ eficiência × base × multiplicador do nível.

- [../producao.md#regras](../producao.md#regras) — R5: trabalhadores produtivos = `min(alocados, floor(marcados ÷ 2))`

- Seção 4.6 da bíblia — marcação de ladrilhos; máximo por nível (N1 4, N2 10, N3 20).

- Seção 4.3 da bíblia — multiplicador por nível (N1 ×1,0, N2 ×1,2, N3 ×1,5).

## Backend

- **Serviço**
  - `ProducaoService.processarProducaoColataRural(vila, turno)`: etapa 1
    - Iterar sobre construções da vila com tipo em {ACAMPAMENTO_LENHADORES, PEDREIRA, BARREIRO, MINA_FERRO, MINA_CARVÃO, SALINA, MINA_ENXOFRE, CABANA_CAÇA, FAZENDA_PLANTIO, FAZENDA_CRIAÇÃO}
    - Para cada prédio ativo (estado == ATIVA):
      - Contar cidadãos alocados à construção_id (construcao_id da tabela cidadao)
      - Calcular trabalhadores produtivos: `min(alocados.size(), floor(marcados.size() ÷ 2))`
      - Para cada trabalhador produtivo:
        - Calcular eficiência via EficienciaService.calcularEficiencia()
        - Acumular: produção += eficiência × base × mult. nível
      - Adicionar produção ao estoque (recurso específico do prédio via catálogo)
    - Gravar eventos_turno para cada prédio com produção > 0

- **Repositórios**
  - Usar `ConstrucaoRepository` para listar construções ativas por vila e tipo
  - Usar `EstoqueService.adicionarRecurso()` para somar ao estoque

- **Catálogo (enum)**
  - `CatalogoPrediosProducao` com: tipo → (recurso, produção_base)

- **Testes**
  - `testProducaoAcampamentoLenhadores()`: 2 Madeireiros eficiência 1,0, 4 marcados, N1 → 2 × 1,0 × 5 × 1,0 = 10 Madeira
  - `testProducaoFazendaPlantio()`: 1 Agricultor eficiência 1,0, N2 → 1 × 1,0 × 6 × 1,2 = 7,2 Grãos
  - `testProducaoSemLadrilhosMarcados()`: Acampamento com 0 marcados → 0 produtivos → sem produção
  - `testProducaoMenorQueAlocados()`: 4 Madeireiros, 2 marcados → 2 produtivos (não 4)
  - `testEventoProducao()`: cada prédio com produção gera evento_turno tipo PRODUÇÃO_COLETA

## Frontend

Não se aplica (Backend only).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/recurso/ProducaoService.java](/src/main/java/com/example/loginbase/jogo/recurso/ProducaoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/construcao/CatalogoPrediosProducao.java](/src/main/java/com/example/loginbase/jogo/construcao/CatalogoPrediosProducao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EtapaTurnoProducao.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaTurnoProducao.java) (novo)

## Testes

Todos listados acima.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1
- Testes passando
- Build sem erros
- Etapa integrada ao pipeline de turno (EtapaTurno)
- Eventos registrados no relatório do turno

## Fora de escopo

- Marcar/desmarcar ladrilhos (fica para história de UI)
- Mudança de cultura/rebanho (fica para história separada)
