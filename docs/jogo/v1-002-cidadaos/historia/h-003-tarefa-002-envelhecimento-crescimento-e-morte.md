# H-003 · Tarefa 002 — Envelhecimento, crescimento e morte

**História:** [H-003 — Nascimento e crescimento](h-003-nascimento-e-crescimento.md) · **Domínio:** [../ciclo-de-vida.md](../ciclo-de-vida.md) · **Depende de:** [h-003-tarefa-001-reproducao-e-nascimento-no-turno.md](h-003-tarefa-001-reproducao-e-nascimento-no-turno.md) · **Camada:** Backend

## Objetivo

Implementar etapa do turno (passo 10, seção 2.2) que processa envelhecimento (+1 mês), aniversários (a cada 12 turnos), distribuição de pontos de crescimento e teste de morte.

## Contexto necessário

- [ciclo-de-vida.md](../ciclo-de-vida.md) — idade, morte, crescimento
  > +1 característica/ano até 18; +1 profissão a cada 2 anos; morte com idade.

## Backend

**Serviço** (`com.example.loginbase.jogo.cidadao.EnvelhecimentoService`):
- `procesarEnvelhecimento(vila: Vila, turnoAtual: Int): List<CidadaoEvento> eventos`
  - Para cada cidadão vivo da vila:
    - **Envelhecimento**: idadeMeses += 1.
    - **Aniversário** (se idadeMeses % 12 == 0):
      - Chamar `processarAniversario(cidadao, idadeAnos, turnoAtual)`.
    - **Morte por idade** (seção 5.5):
      - Se idadeAnos >= 50: chance = max(0; 1% × (idadeAnos − 49) − 0,2% × VIT).
      - Se aleatório < chance: cidadao.vivo = false; itens equipados voltam ao inventário.
      - Se idadeAnos >= 90: morte com certeza.
  - Retorna lista de eventos (nascimentos, aniversários, mortes) para relatório.

**Método auxiliar**:
- `processarAniversario(cidadao: Cidadao, idadeAnos: Int, turnoAtual: Int): void`
  - Se idadeAnos >= 1 e idadeAnos <= 18:
    - pontosCarpendentes += 1.
  - Se idadeAnos >= 2, idadeAnos <= 18, e idadeAnos par:
    - pontosProffendentes += 1.
  - Registrar evento "aniversário" com idade e pontos pendentes.

- `calcularChanceMorte(idadeAnos: Int, vit: Int): Double`
  - Se idadeAnos >= 90: return 1,0 (morte certa).
  - Se idadeAnos >= 50: return max(0,0; 0,01 × (idadeAnos − 49) − 0,002 × vit).
  - Senão: return 0,0.

- `processarMorte(cidadao: Cidadao): void`
  - cidadao.vivo = false.
  - Mover todos os itens equipados para inventário da vila.
  - Se era líder da família líder da vila: transferir liderança (sucessão).

**Integração**:
- Chamar em pipeline de turno, passo 10 (seção 2.2).

## Frontend

Não se aplica (backend apenas; relatório vem em painel de cidadão).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/cidadao/EnvelhecimentoService.java](/src/main/java/com/example/loginbase/jogo/cidadao/EnvelhecimentoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EtapaEnvelhecimento.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaEnvelhecimento.java) (novo)

## Testes

- Teste: todos envelhecem +1 mês/turno.
- Teste: a cada 12 turnos (aniversário), +1 característica até 18 anos (máx. 18).
- Teste: a cada 24 turnos (aniversários 2, 4, ..., 18), +1 profissão até 18 anos (máx. 9).
- Teste: cidadão com 60 anos e VIT 5 tem ~10% de morte (100 testes → ~10 mortes).
- Teste: cidadão com 90 anos morre com certeza.
- Teste: morte preserva itens no inventário.
- Teste: morte líder de família → sucessão automática.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA5–CA6.
- Build do backend (`./mvnw verify`) sem erros.
- Testes listados passando.
- Fórmula de morte conforme seção 5.5.
- Integração no pipeline de turno (passo 10).

## Fora de escopo

- Casamento após morte (divórcio não existe).
- Famine-related death details (ver historia h-003 de recursos).
