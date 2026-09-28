# 0024 — Listas de Nomes como Recurso de Classpath

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-28 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [add-soldier-names-batch-slots](../../openspec/changes/add-soldier-names-batch-slots/) |

## Contexto e Problema

Atualmente, unidades (tropas) treináveis no quartel são anônimas. A mudança proposta pela change `add-soldier-names-batch-slots` demanda que cada unidade receba um nome e sobrenome sorteados de listas. Existem múltiplas estratégias de armazenamento: (a) JSON em `docs/`, copiado manualmente em build; (b) JSON em `src/main/resources/jogo/nomes/` como recurso de classpath, carregado via `ClassPathResource` na inicialização; (c) tabelas no banco de dados; (d) listas hardcoded em código Java. A escolha impacta portabilidade, manutenibilidade do build e falha rápida em caso de dados faltantes.

**Requisito associado**: RF-EXE-007 (nomes e sufixo para unidades); design `add-soldier-names-batch-slots` §D1.

## Direcionadores da Decisão

- **Portabilidade**: Recurso incluído no classpath funciona em qualquer ambiente (IDE, Docker, bare metal) sem configuração extra.
- **Build simplificado**: Dockerfiles e pom.xml já copiam `src/` completo; sem necessidade de volumes ou `<resources>` adicionais.
- **Falha rápida**: Ausência ou vazio do arquivo deve impedir inicialização da aplicação (early detection).
- **Manutenibilidade**: Uma única fonte de verdade; sem duplicação (docs + recursos).
- **Testabilidade**: Testes carregam dos mesmos arquivos em produção.

## Opções Consideradas

### Opção (a): JSON em `docs/` com cópia em build — **DESCARTADA**

- Arquivos `docs/nome_pessoas.json`, `docs/sobrenome_pessoas.json`.
- Build via `git mv` copia para `src/main/resources/jogo/nomes/` (ou via `<resources>` em pom.xml).
- Dockerfile precisa copiar dados antes de classes compiladas.

**Prós**: Documentação separada de código.  
**Contras**: Duas fontes de verdade; maior complexidade no build; Dockerfile deve referenciar `docs/`; risco de divergência.

### Opção (b): JSON em `src/main/resources/jogo/nomes/` — **ESCOLHIDA**

- Via `git mv` move `docs/nome_pessoas.json` e `docs/sobrenome_pessoas.json` para `src/main/resources/jogo/nomes/`.
- Carregamento via `ClassPathResource` no bean `GeradorNomes` (Spring component).
- Jackson deserializa em `List<String>` durante inicialização.
- Construtor falha (throws `IllegalStateException`) se lista vazia ou arquivo ausente.

**Prós**: Único ponto de verdade; Dockerfile/pom.xml sem ajustes; classpath padrão; falha rápida.  
**Contras**: Nenhum arquivo `docs/` específico para nomes; decisão trade-off aceitável.

### Opção (c): Tabelas no banco de dados

- Tabelas `jogo_nomes_pessoas`, `jogo_nomes_sobrenomes` com dados semeados em V4.
- Carregamento via repository em boot.

**Prós**: Flexibilidade (editar em runtime via admin).  
**Contras**: Overhead de banco; falha se migrations ausentes; mais complexo.

### Opção (d): Hardcoded em código Java

- Listas em `public static final String[] NOMES = { ... }` em `GeradorNomes`.

**Prós**: Sem arquivo externo.  
**Contras**: Difícil manutenção; código inchado; sem flexibilidade para atualizar.

## Resultado da Decisão

**Decidimos pela Opção (b)**: JSON em `src/main/resources/jogo/nomes/` via `git mv`, carregado na inicialização como recurso de classpath.

**Racional**:
1. **Simplicidade**: Arquivo movido uma única vez com `git mv`; Dockerfile não precisa de ajustes.
2. **Segurança**: Construtor de `GeradorNomes` valida tamanho e estrutura JSON; falha na inicialização se inválido.
3. **Consistência**: Alinha-se com padrão Spring Boot de recursos em `src/main/resources/`.
4. **Testabilidade**: Testes rodam contra os mesmos arquivos de produção via classpath de teste.
5. **Portabilidade**: Funciona em IDE (via Maven), Docker (volume bind ou COPY de `src/`) e bare metal (sem dependência de volume).

## Decisões Complementares (do Design)

### 1. Arquivo e Localização

- **Caminho**: `src/main/resources/jogo/nomes/nome_pessoas.json` e `src/main/resources/jogo/nomes/sobrenome_pessoas.json`.
- **Formato**: JSON array de strings (ex.: `["Ana", "Pedro", "Maria", ...]`).
- **Origem**: Movido de `docs/nome_pessoas.json` e `docs/sobrenome_pessoas.json` via `git mv` em migration V4.

### 2. Carregamento e Validação

Classe `GeradorNomes` (pacote `jogo/quartel`), anotada com `@Component`:

```java
@Component
public class GeradorNomes {
    private final List<String> nomes;
    private final List<String> sobrenomes;

    public GeradorNomes() throws IOException {
        ClassPathResource nomeResource = new ClassPathResource("jogo/nomes/nome_pessoas.json");
        ClassPathResource sobrenomeResource = new ClassPathResource("jogo/nomes/sobrenome_pessoas.json");
        
        ObjectMapper mapper = new ObjectMapper();
        nomes = mapper.readValue(nomeResource.getInputStream(), new TypeReference<List<String>>(){});
        sobrenomes = mapper.readValue(sobrenomeResource.getInputStream(), new TypeReference<List<String>>(){});
        
        if (nomes.isEmpty() || sobrenomes.isEmpty()) {
            throw new IllegalStateException("Listas de nomes vazio(a)s em classpath");
        }
    }

    public String sortearNome(Aleatorio aleatorio) { ... }
    public String sortearSobrenome(Aleatorio aleatorio) { ... }
}
```

### 3. Falha Rápida em Inicialização

- Exceção na construção bloqueia `ApplicationContext`.
- Logs claros: "Erro ao carregar `nome_pessoas.json` do classpath: `FileNotFoundException`".
- Deploy falha imediatamente sem máquina em estado corrompido.

### 4. Testabilidade

- Testes unitários mocam ou usam `ClassPathResource` de `src/test/resources/jogo/nomes/` (cópia ou subset).
- Integração carrega dos recursos reais de `src/main/resources/` durante `@SpringBootTest`.

## Consequências Positivas

- **Portabilidade garantida**: Recurso sempre disponível no classpath de build e em runtime.
- **Manutenção centralizada**: Um arquivo por tipo de nome (não duplicado em docs + outras pastas).
- **Falha detectada rápido**: Aplicação não inicia se dados ausentes; evita "nomes nulos" em produção.
- **Docker simplificado**: Sem volume bind obrigatório de `docs/`.
- **Alinhamento com padrões Spring**: Segue convenção `src/main/resources/` para dados configuráveis.

## Consequências Negativas

- **Sem edição em runtime**: Mudanças em listas exigem rebuild e restart (trade-off aceitável).
- **Arquivo não rastreável em docs/**: Nomes agora em `src/main/resources/`, não em docs; documentação deve referenciar o novo caminho.
- **Migração V4 necessária**: Unidades pré-existentes herdam nomes determinísticos; backfill aceitável para sandbox de testes.

## Mais Informações

- **Spec relacionada**: [add-soldier-names-batch-slots proposal](../../openspec/changes/add-soldier-names-batch-slots/proposal.md)
- **Design relacionado**: [add-soldier-names-batch-slots design](../../openspec/changes/add-soldier-names-batch-slots/design.md) — §D1 (decisão original)
- **Código principal**: 
  - `src/main/java/com/example/loginbase/jogo/quartel/GeradorNomes.java`
  - `src/main/java/com/example/loginbase/jogo/quartel/NumeradorNomes.java` (sufixo ordinal)
  - `src/main/resources/jogo/nomes/nome_pessoas.json`
  - `src/main/resources/jogo/nomes/sobrenome_pessoas.json`
- **Migration**: `src/main/resources/db/migration/V4__unidade_nome_e_lote_treino.sql`
- **Testes**: 
  - `src/test/java/com/example/loginbase/jogo/quartel/GeradorNomesTest.java`
  - `src/test/java/com/example/loginbase/jogo/quartel/NumeradorNomesTest.java`

---

**Histórico de Revisões:**

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-28 | Versão inicial, implementada pela change `add-soldier-names-batch-slots` | Adiel, com apoio de agentes Claude |
