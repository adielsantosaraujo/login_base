# H-003 · Tarefa 001 — Reprodução e nascimento no turno

**História:** [H-003 — Nascimento e crescimento](h-003-nascimento-e-crescimento.md) · **Domínio:** [../familias.md](../familias.md) · **Depende de:** [h-002-tarefa-002-tela-de-familias-e-casamento.md](h-002-tarefa-002-tela-de-familias-e-casamento.md) · **Camada:** Backend

## Objetivo

Implementar etapa do turno (passo 9, seção 2.2) que processa concepções, gestações e nascimentos. Filhos herdam 25% das características e PE dos pais.

## Contexto necessário

- [familias.md](../familias.md) — herança e reprodução
  > Concepção 8%/turno; gestação 9 turnos; herança 25% média dos pais.

- [ciclo-de-vida.md](../ciclo-de-vida.md) — idade
  > Mulher 18–45 para reproduzir; intervalo 12 turnos.

- [recursos.md](../../v1-010-recursos-e-producao/recursos.md) — fome
  > Ambos não famintos para conceber.

## Backend

**Serviço** (`com.example.loginbase.jogo.cidadao.ReproducaoService`):
- `procesarReproducao(vila: Vila, turnoAtual: Int): List<Cidadao> nascidos`
  - Para cada casal (conjugeId != nulo) da vila:
    - **Concepção**: se mulher 18–45, ambos não famintos (faminto_turnos < 3), vaga livre, e (gestacao_turnos nulo ou 0):
      - Aleatório < 0,08 (8%): marcar gestacao_turnos = 9.
      - Decrementar vaga da casa (reservada).
    - **Gestação**: se cidadão tem gestacao_turnos > 0, decrementar.
    - **Nascimento**: se gestacao_turnos chega a 0:
      - Criar novo Cidadao na familia do casal.
      - Idade 0 meses, sexo 50/50.
      - Características herdadas: `floor(0,25 × (pai.caract + mae.caract) ÷ 2)` para cada.
      - PE herdados: `floor(0,25 × (pai.PE + mae.PE) ÷ 2)` para profissões com PE > 0.
      - Marcar gestacao_turnos = nulo.
      - Liberar vaga reservada, mas ocupar nova vaga para o bebê.
      - Intervalo_proxima_concepcao = 12 turnos (implementar com campo ou lógica).
  - Retorna lista de cidadãos nascidos (para relatório do turno).

**Método auxiliar**:
- `calcularCaracteristicaHerdada(pai_valor: Int, mae_valor: Int): Int = floor(0,25 × (pai_valor + mae_valor) ÷ 2)`
- `podeConceberAgora(casal: Pair<Cidadao>): Boolean`
  - Mulher 18–45 anos.
  - Ambos não famintos.
  - Ambos não já gestando.
  - Vaga livre na casa.
  - Intervalo de 12 turnos desde último nascimento.

**Integração**:
- Chamar em pipeline de turno, passo 9 (seção 2.2).

## Frontend

Não se aplica (backend apenas; relatório vem em tarefa 004).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/cidadao/ReproducaoService.java](/src/main/java/com/example/loginbase/jogo/cidadao/ReproducaoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EtapaReproducao.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaReproducao.java) (novo)

## Testes

- Teste: casal elegível, turno processado 100 vezes, ~8 concepções (esperar entre 4–12 nascimentos em 100 turnos).
- Teste: gestação dura exatamente 9 turnos.
- Teste: filho nasce com características herdadas corretas (exemplo CA3).
- Teste: intervalo de 12 turnos após nascimento não permite nova concepção antes.
- Teste: casal famintos não concedem.
- Teste: mulher > 45 anos não concebe.
- Teste: sem vaga livre não concebe.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA4.
- Build do backend (`./mvnw verify`) sem erros.
- Testes listados passando.
- Herança de características exata conforme fórmula.
- Integração no pipeline de turno (passo 9).

## Fora de escopo

- Relatório do nascimento (tarefa 004).
- Nomes do bebê (gerados em gerador de nomes, pode ser simplificado).
