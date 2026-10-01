# H-003 · Tarefa 003 — Imigração pela Estalagem

**História:** [H-003 — Nascimento e crescimento](h-003-nascimento-e-crescimento.md) · **Domínio:** [../familias.md](../familias.md) · **Depende de:** [h-003-tarefa-002-envelhecimento-crescimento-e-morte.md](h-003-tarefa-002-envelhecimento-crescimento-e-morte.md) · **Camada:** Backend

## Objetivo

Implementar etapa do turno que processa imigração pela Estalagem. Adultos aleatórios chegam e formam novos núcleos.

## Contexto necessário

- [familias.md](../familias.md) — imigração
  > Estalagem N1/N2/N3: 2%/4%/6% chance/turno. Imigrante 18–30 anos, 20 características, 10 profissão (aleatório).

## Backend

**Serviço** (`com.example.loginbase.jogo.cidadao.ImigrancaoService`):
- `processarImigracao(vila: Vila, turnoAtual: Int): List<Cidadao> imigrantes`
  - Para cada Estalagem ativa (construcao.estado == ATIVA) da vila:
    - Calcular chance = 0,02 × nível (N1: 2%, N2: 4%, N3: 6%).
    - Se aleatório < chance e existe núcleo livre em alguma casa:
      - Criar novo Cidadao imigrante:
        - Idade aleatória 18–30 anos (216–360 meses).
        - Sexo 50/50.
        - Características: 20 pontos distribuídos aleatoriamente (máx. 10 por característica).
        - PE profissão: 10 pontos distribuídos aleatoriamente (máx. 5 por profissão).
        - Estado: SAUDAVEL.
        - Vivo: true.
      - Criar nova Familia (núcleo) com sobrenome aleatório.
      - Alocar novo núcleo em casa com vaga livre.
      - Salvar em BD.
      - Adicionar à lista de imigrantes.
  - Retorna lista de imigrantes para relatório.

**Método auxiliar**:
- `criarImigrante(): Cidadao`
  - Idade: random(216, 360) meses.
  - Sexo: random 50/50.
  - Características: distribuir 20 aleatoriamente, máx. 10 cada.
  - PE profissão: distribuir 10 aleatoriamente, máx. 5 cada.

- `criarNovoNucleo(imigrante: Cidadao, casa: Casa): Familia`
  - Familia com sobrenome aleatório.
  - Alocar em casa.

**Integração**:
- Chamar em pipeline de turno, após passo 9 ou 10 (varia com design).

## Frontend

Não se aplica (backend apenas; relatório no painel de turno).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/cidadao/ImigrancaoService.java](/src/main/java/com/example/loginbase/jogo/cidadao/ImigrancaoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EtapaImigracao.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaImigracao.java) (novo)

## Testes

- Teste: Estalagem N1, turno processado 100 vezes, ~2 imigrantes (esperar 0–5).
- Teste: Estalagem N2, turno processado 100 vezes, ~4 imigrantes.
- Teste: Estalagem N3, turno processado 100 vezes, ~6 imigrantes.
- Teste: imigrante tem idade 18–30 anos.
- Teste: imigrante tem 20 características distribuídas aleatoriamente (máx. 10 cada).
- Teste: imigrante tem 10 PE profissão distribuídos aleatoriamente (máx. 5 cada).
- Teste: sem núcleo livre, nenhuma imigração.
- Teste: novo núcleo criado e alocado em casa.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA7.
- Build do backend (`./mvnw verify`) sem erros.
- Testes listados passando.
- Chance conforme seção 4.10.
- Distribuição aleatória de características/profissão sem bias.

## Fora de escopo

- Nomes de imigrante (gerados simples; pode ser melhorado).
- Seleção de profissão com peso (todas com chance igual).
