# H-002 · Tarefa 001 — Regra e API de casamento

**História:** [H-002 — Casar cidadãos](h-002-casar-cidadaos.md) · **Domínio:** [../familias.md](../familias.md) · **Depende de:** [h-001-tarefa-003-tela-de-distribuicao-de-pontos-e-familia-lider.md](h-001-tarefa-003-tela-de-distribuicao-de-pontos-e-familia-lider.md) · **Camada:** Backend

## Objetivo

Implementar serviço de casamento com validações (idade, parentes, solteiro) e criar novo núcleo familiar em casa escolhida. Expor via POST `/api/jogo/casamento`.

## Contexto necessário

- [familias.md](../familias.md) — casamento e núcleo
  > Exige núcleo livre; ambos ≥18, solteiros, não parentes. Novo núcleo com sobrenome escolhido.

- [ciclo-de-vida.md](../ciclo-de-vida.md) — morte
  > Ambos devem estar vivos.

## Backend

**Serviço** (`com.example.loginbase.jogo.cidadao.CasamentoService`):
- `casar(vilaId: Long, cidadao1Id: Long, cidadao2Id: Long, casaId: Long, sobrenomeEscolhido: String): Familia`
  - Validações (CA1–CA2):
    - Ambos ≥18 anos.
    - Ambos solteiros (conjugeId nulo).
    - Ambos vivos.
    - Não parentes de 1º grau: pai/mãe/irmão/irmã (check genealogia).
    - Da mesma vila.
    - Casa tem núcleo livre.
  - Se validação falha, lança `CasamentoException` com mensagem descritiva.
  - Cria novo núcleo (Familia) com sobrenomeEscolhido.
  - Atualiza ambos: familiaId → novo núcleo; conjugeId recíproco.
  - Move ambos para casa (casaId).
  - Salva em BD.
  - Retorna nova Familia criada.

**Método auxiliar**:
- `temParente1Grau(cidadao1: Cidadao, cidadao2: Cidadao): Boolean`
  - Verifica se um é pai/mãe/irmão/irmã do outro.

**Endpoint** (`com.example.loginbase.jogo.cidadao.CasamentoController`):
- `POST /api/jogo/casamento`
  - Request: `{ cidadao1Id, cidadao2Id, casaId, sobrenomeEscolhido }`
  - Response: `{ id, vilaId, sobrenome, casaId, membros: [ { id, nome, idade_anos }, ... ] }`
  - Erros: 400 Bad Request com mensagem descritiva.

## Frontend

Não se aplica (controller apenas; UI em tarefa 002).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/cidadao/CasamentoService.java](/src/main/java/com/example/loginbase/jogo/cidadao/CasamentoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/cidadao/CasamentoException.java](/src/main/java/com/example/loginbase/jogo/cidadao/CasamentoException.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/cidadao/CasamentoController.java](/src/main/java/com/example/loginbase/jogo/cidadao/CasamentoController.java) (novo)

## Testes

- Teste: casar dois cidadãos elegíveis → novo núcleo criado.
- Teste: tentar casar menores (< 18) → exception com mensagem.
- Teste: tentar casar não-solteiro → exception.
- Teste: tentar casar irmãos → exception.
- Teste: tentar casar sem núcleo livre → exception.
- Teste: casar com novo sobrenome escolhido → Familia.sobrenome atualizado.
- Teste: ambos têm conjugeId recíproco após casamento.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA3.
- Build do backend (`./mvnw verify`) sem erros.
- Testes listados passando.
- Validações conforme seção 5.7.

## Fora de escopo

- UI de casamento (tarefa 002).
- Transações distribuídas (apenas uma vila por turno).
