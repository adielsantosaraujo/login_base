# H-001 · Tarefa 001 — Modelo de estoque e capacidade

**História:** [h-001-consultar-estoque-de-recursos.md](h-001-consultar-estoque-de-recursos.md) · **Domínio:** [../recursos.md](../recursos.md) ·
**Depende de:** — · **Camada:** Backend

## Objetivo

Implementar a tabela de estoque, a lógica de cálculo de capacidade por nível de Armazém e eficiência de Carregadores, e a etapa do turno que aplica o limite de armazenamento.

## Contexto necessário

- [../recursos.md#números-e-tabelas](../recursos.md#números-e-tabelas) — tabela 3.1 com lista de 19 recursos
  > Recursos: Madeira, Pedra, Argila, Minério de ferro, Carvão, Sal, Enxofre, Grãos, Fibra, Carne, Couro, Lã, Tábua, Tijolo, Ferro, Aço, Tecido, Couro curtido, Refeição, Ouro.

- [../recursos.md#regras](../recursos.md#regras) — capacidade base 500, Ouro ilimitado; Armazém N1 +500, N2 +1.500, N3 +4.000 × `min(1,5; eficiência média Carregadores)`; mínimo de Carregadores (N1 1, N2 2, N3 4) obrigatório.

- Seção 2.2 da bíblia — passo 4 do turno: limite de armazenamento, excedente perdido.

## Backend

- **Entidades**
  - `Recurso` (enum): MADEIRA, PEDRA, ARGILA, MINÉRIO_DE_FERRO, CARVÃO, SAL, ENXOFRE, GRÃOS, FIBRA, CARNE, COURO, LÃ, TÁBUA, TIJOLO, FERRO, AÇO, TECIDO, COURO_CURTIDO, REFEIÇÃO, OURO.
  - `Estoque` (tabela): vila_id (FK), recurso (enum), quantidade (numeric 14,2).

- **Repositórios**
  - `EstoqueRepository`: findByVilaIdAndRecurso(), saveAll()

- **Serviços**
  - `EstoqueService.calcularCapacidadeTotal(vila)`: retorna Map<Recurso, Double> com capacidade máxima por recurso.
    - Base: 500 (OURO: ilimitado = Long.MAX_VALUE)
    - Para cada Armazém ativo (construcao.tipo == ARMAZÉM):
      - Contar Carregadores alocados (construcao_id, profissão CARREGADOR)
      - Se >= mínimo (N1 1, N2 2, N3 4): somar capacidade extra
      - Capacidade extra = (N1 500 | N2 1500 | N3 4000) × min(1,5; eficiência média Carregadores)
  - `EstoqueService.aplicarLimiteArmazenamento(vila)`: etapa do turno, passo 4.
    - Para cada recurso com quantidade > capacidade:
      - Perda = quantidade − capacidade
      - Gravar evento_turno com tipo ESTOQUE_PERDIDO
      - Atualizar estoque

- **Migração Flyway** (V4 ou após V3)
  ```sql
  CREATE TABLE estoque (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vila_id BIGINT NOT NULL,
    recurso VARCHAR(50) NOT NULL,
    quantidade NUMERIC(14,2) NOT NULL DEFAULT 0,
    UNIQUE(vila_id, recurso),
    FOREIGN KEY (vila_id) REFERENCES vila(id)
  );
  ```

## Frontend

Não se aplica (Backend only).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/recurso/Recurso.java](/src/main/java/com/example/loginbase/jogo/recurso/Recurso.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/recurso/Estoque.java](/src/main/java/com/example/loginbase/jogo/recurso/Estoque.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/recurso/EstoqueRepository.java](/src/main/java/com/example/loginbase/jogo/recurso/EstoqueRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/recurso/EstoqueService.java](/src/main/java/com/example/loginbase/jogo/recurso/EstoqueService.java) (novo)
- [/src/main/resources/db/migration/V4__estoque.sql](/src/main/resources/db/migration/V4__estoque.sql) (novo)

## Testes

- `EstoqueServiceTest.testCalcularCapacidadeSemArmazem()`: capacidade base = 500 para todos menos Ouro (ilimitado)
- `EstoqueServiceTest.testCalcularCapacidadeArmazemN1()`: +500 × eficiência 1,0 = 1.000
- `EstoqueServiceTest.testCalcularCapacidadeArmazemSemMinimoCarregadores()`: não soma (sem mínimo)
- `EstoqueServiceTest.testCalcularCapacidadeArmazemN2()`: +1.500 × min(1,5; eficiência)
- `EstoqueServiceTest.testAplicarLimiteArmazenamento()`: perda registrada, estoque limitado

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA2, CA3, CA4, CA5
- Build do backend (`./mvnw verify`) sem erros
- Testes passando
- Capacidade de Ouro não tem limite superior

## Fora de escopo

- Implicações de armazém destruído (sucesso futuro)
