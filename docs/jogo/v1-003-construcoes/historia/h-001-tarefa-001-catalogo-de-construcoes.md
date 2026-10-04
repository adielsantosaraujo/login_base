# H-001 · Tarefa 001 — Catálogo de construções

**História:** [H-001 — Construir prédio nível 1](h-001-construir-predio-nivel-1.md) · **Domínio:** [../construcoes.md](../construcoes.md) · **Depende de:** — · **Camada:** Backend

## Objetivo

Centralizar dados de todos os prédios (custos, PO, vagas, região, profissão) em um enum `ConstrucaoCatalogo` com regras de balanceamento, facilitando validação e reutilização no backend.

## Contexto necessário

- [construcoes.md](../construcoes.md) — Tabela de custos N1 (seção 4.4)
  > | Construção | Região | Profissão | Custo N1 | PO N1 |
  > | Casa | Urbana | — | 20 Mad, 10 Ped, 10 Arg | 4 |
  > | Armazém | Urbana | Carregador | 30 Mad, 20 Ped | 6 |
  > ... (24 prédios no total)

- [construcoes.md](../construcoes.md) — Bonificação por nível (seção 4.3)
  > | Nível | Vagas | Multiplicador |
  > | N1 | 2 | ×1,0 |
  > | N2 | 5 | ×1,2 |
  > | N3 | 10 | ×1,5 |

## Backend

- **Enum `ConstrucaoCatalogo`** em `/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoCatalogo.java`:
  - Tipos: CASA, ARMAZEM, SERRARIA, OLARIA, FUNDIÇÃO, TECELAGEM, CURTUME, COZINHA, FERRARIA, ALFAIATARIA, CARPINTARIA, MERCADO, ESTALAGEM, QUARTEL, FAZENDA_PLANTIO, FAZENDA_CRIACAO, ACAMPAMENTO_LENHADORES, PEDREIRA, BARREIRO, MINA_FERRO, MINA_CARVAO, SALINA, MINA_ENXOFRE, CABANA_CACA.
  - Campos: nome, bonusRegiao (BonusRegiao, nulo = urbano), profissaoPrincipal (enum ou null), custoN1 (Map<TipoRecurso, quantidade>), poN1, vagasN1.
  - Método `regioesPermitidas()` derivado de `TipoRegiao.bonus()`.
  - Método `getCusto(Nivel)` retorna custo multiplicado por 2,5 ou 5.
  - Método `getPO(Nivel)` retorna PO multiplicado.
  - Método `getVagas(Nivel)` retorna vagas multiplicadas (tabela seção 4.3).

- **Classe `ConstrucaoCusto`** (value object): recurso, quantidade.

- **Testes unitários**: validar custos de cada prédio, upgrade 2,5× e 5×, vagas por nível.

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoCatalogo.java](/src/main/java/com/example/loginbase/jogo/construcao/ConstrucaoCatalogo.java) (novo)
- [/src/test/java/com/example/loginbase/jogo/construcao/ConstrucaoCatalogoTest.java](/src/test/java/com/example/loginbase/jogo/construcao/ConstrucaoCatalogoTest.java) (novo)

## Testes

- `testCustoN1_Casa` → 20 Madeira, 10 Pedra, 10 Argila.
- `testCustoN2_Casa` → 50 Madeira, 25 Pedra, 25 Argila (2,5×).
- `testCustoN3_Casa` → 100 Madeira, 50 Pedra, 50 Argila (5×).
- `testPO_Serraria` → N1: 6, N2: 15, N3: 30.
- `testVagas_ArmazemN1` → 1 (Carregador), com mínimo.
- `testRegiaoPermitida_QuartelEmCampo` → erro (Quartel é Urbana).

## Definição de pronto

- Critérios de aceite cobertos: CA1, CA5 (validações de tipo e recurso usam o catálogo).
- Build sem erros (`./mvnw verify`).
- Testes passando.
- Custos [proposta] da seção 4.4 centralizados em `ConstrucaoCatalogo`.

## Fora de escopo

- Cálculo dinâmico de custos (ex.: por população ou turno).
- Balanceamento futuro (mudança de números).
