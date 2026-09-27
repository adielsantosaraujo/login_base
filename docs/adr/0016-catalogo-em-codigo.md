# 0016 — Catálogo de Regras em Enums/Records Java (Não YAML)

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-26 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-city-builder-game`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/) |

## Contexto e problema

Regras do jogo (prédios, tropas, itens, masmorras) precisam de definição centralizada: custos, tempos, atributos. Há dois caminhos: (1) YAML/JSON (flexível, sem compilação), (2) enums/records Java (tipado, compilável). YAML é tradicional para configs, mas Java oferece type safety e facilita testes determinísticos.

## Direcionadores da decisão

- Type safety: Java typado reduz erros em tempo de compilação
- Testabilidade: enums/records são fáceis de iterar em testes
- Performance: sem parsing YAML em startup
- Versionamento: mudanças em catálogo = código (revisão de código, não config)
- Exposição: expor via API REST (`GET /api/jogo/catalogo`) como JSON

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Enums/records em Java** | `TipoPredio`, `Custo`, `TipoTropa`, etc. como enums com dados. Jackson serializa para JSON. Tipado, testável. |
| YAML configs | `application.yml` com árvor de prédios/tropas. Rejeitada: tipo-inseguro; parsing custoso. |
| Banco de dados | Tabelas de configuração. Rejeitada: overkill (dados não mudam em runtime). |

## Resultado da decisão

Adotou-se **enums/records em Java**:

1. **Pacote `jogo.catalogo`**: 
   - `TipoRecurso` (enum): COMIDA, MADEIRA, PEDRA, FERRO
   - `Custo` (record): armazena quantidades por recurso
   - `TipoPredio` (enum): CENTRO_VILA, ARMAZEM, FAZENDA, SERRARIA, PEDREIRA, MINA_FERRO, FORJA, QUARTEL
     - Cada enum item tem `custosPorNivel[5]`, `temposPorNivel[5]`
   - `Cultivo` (enum): TRIGO, MILHO, BATATA, ABOBORA_DOURADA
   - `ModeloItem` (enum): ESPADA, LANCA, ARCO, ARMADURA_COURO, ARMADURA_FERRO
   - `TipoTropa` (enum): SOLDADO, ARQUEIRO, LANCEIRO
   - `TipoInimigo` (enum): GOBLIN, ESQUELETO_ARQUEIRO, ORC, TROLL
   - `MapaMasmorra` (record): posições, obstáculos (fixed para todos)
   - `CatalogoMasmorras` (class): níveis 1–5

2. **Endpoint** (`GET /api/jogo/catalogo`):
   ```java
   @GetMapping("/catalogo")
   public CatalogoDto catalogo() {
       return jogoMapper.toCatalogoDto(clock.instant());
   }
   ```
   Retorna JSON com todos os enums serializados

3. **Jackson**:
   - Enums serializam como string (nome da constante)
   - Records serializam com campos públicos

### Consequências positivas
- **Type safe**: compiler pega erros de digitação
- **Testável**: fácil mockar/iterar em testes
- **Versionado**: mudanças = commits de código (auditório)
- **Rápido**: sem parsing YAML
- **Flexível**: adicionar novos prédios = adicionar enum item

### Consequências negativas
- **Não dinâmico**: mudanças precisam recompile + restart (aceito: balanceamento é raro)
- **Mais verbose**: enums Java são mais longas que YAML
- **Sem hot reload**: YAML poderia ser reloadado sem restart (rejeitado por simplicidade)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Enums/records Java | Type safe; testável; versionado; rápido. | Não dinâmico; verbose. |
| YAML | Dinâmico; conciso; tradicional. | Tipo-inseguro; parsing custoso; sem auditória. |
| Banco de dados | Flexível em runtime. | Overkill; sem controle de versão. |

## Mais informações

- **Design**: [`add-city-builder-game/design.md` §16](../../openspec/changes/archive/2026-09-27-add-city-builder-game/design.md)
- **Código**: [`jogo/catalogo/`](../../src/main/java/com/example/loginbase/jogo/catalogo/)
  - `TipoPredio.java`, `Custo.java`, `Cultivo.java`, `ModeloItem.java`, `TipoTropa.java`
  - `CatalogoMasmorras.java` (níveis 1–5)
- **Endpoint**: [`jogo/api/VilaController.java#catalogo`](../../src/main/java/com/example/loginbase/jogo/api/VilaController.java)
- **Teste**: [`jogo/catalogo/CatalogoTest.java`](../../src/test/java/com/example/loginbase/jogo/catalogo/CatalogoTest.java)

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
