# H-001 · Tarefa 002 — Geração de jazidas por semente

**História:** [h-001-criar-vila-escolhendo-regioes-iniciais.md](h-001-criar-vila-escolhendo-regioes-iniciais.md) · **Domínio:** [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md) · **Camada:** Backend

## Objetivo

Implementar gerador determinístico de jazidas baseado em semente (pseudo-random com seed determinística), garantindo distribuição dentro dos percentuais, respeitar garantias mínimas (10 Floresta, 10 Rocha, 8 Barreiro por região).

## Contexto necessário

- [../regioes.md](../regioes.md) — jazidas (seção 1.5)
  > Distribuição: Floresta 25%, Rocha 20%, Barreiro 15%, Veio de ferro 10%, Veio de carvão 10%, Salina 7%, Enxofre 5%, Campo 8%.
  > Garantias: ≥10 Floresta, ≥10 Rocha, ≥8 Barreiro por região.

## Backend

**Serviço (novo):**
- [/src/main/java/com/example/loginbase/jogo/servico/GeradorJazidaService.java](/src/main/java/com/example/loginbase/jogo/servico/GeradorJazidaService.java) (novo)
  - Método: `Map<String, Jazida> gerarJazidadasPorRegiao(long semente, int indiceRegiao)` — retorna mapa de 100 pares (x, y) → Jazida
  - Implementação: usar Random(seed) ou equivalente, distribuir com respeito aos percentuais, pós-processar para garantir minimais
  - Determinístico: mesma semente + indiceRegiao → sempre mesmo resultado

**Método auxiliar em Vila ou repositório:**
- Persistir ou gerar dinamicamente: `JogoService.gerarOuObterJazidas(regiao)` — verificar se já estão persistidas; se não, gerar da semente

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/servico/GeradorJazidaService.java](/src/main/java/com/example/loginbase/jogo/servico/GeradorJazidaService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/modelo/Jazida.java](/src/main/java/com/example/loginbase/jogo/modelo/Jazida.java) (novo, enum)

## Testes

- **Teste unitário (determinístico)**: chamar `gerarJazidadasPorRegiao(12345, 1)` duas vezes → mesma distribuição exata.
- **Teste de distribuição**: gerar 16 regiões com semente X, contar % de cada jazida em cada região, verificar se próximas dos esperados (tolerância ±5%).
- **Teste de garantias**: gerar 100 regiões, verificar se cada uma tem ≥10 Floresta, ≥10 Rocha, ≥8 Barreiro.
- **Teste de determinismo cruzado**: semente 12345, regiões 1, 2, 16 → comparar com semente 99999 + mesmas regiões → Jazida de (1,0) da região 1 deve ser igual em ambas.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA6 (prévia de jazidas)
- Build do backend (`./mvnw verify`) sem erros
- Testes de distribuição e garantias passando
- Reprodutibilidade confirmada em testes

## Fora de escopo

- Persistência de jazidas (pode ser em tempo real ou cacheada).
- Renovação de jazidas (na v1, não se esgotam).
