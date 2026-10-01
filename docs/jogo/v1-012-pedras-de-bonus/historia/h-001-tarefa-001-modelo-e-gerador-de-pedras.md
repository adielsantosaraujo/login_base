# H-001 · Tarefa 001 — Modelo e gerador de pedras

**História:** [H-001 — Obter pedras nas masmorras](h-001-obter-pedras-nas-masmorras.md) · **Domínio:** [../pedras-de-bonus.md](../pedras-de-bonus.md) ·
**Depende de:** H-001-tarefa-001 (masmorra e batalha) | — · **Camada:** Backend

## Objetivo

Implementar a entidade `Pedra`, o repositório e um serviço gerador determinístico de pedras baseado em semente (reprodutível em testes).

## Contexto necessário

- [Catálogo de bônus](../bonus.md) — códigos, magnitudes e aplicabilidade
  > Códigos: VIT, FOR, VEL, INT, CAR, ATK, DEF, VIDA, INI, CRIT, PROF, PROD. Magnitudes: Baixa, Média, Alta. Tabela de restrição por categoria de item.

- [Recompensas de masmorra](../../v1-001-masmorras/masmorras.md) — fórmula de sorteios e distribuição de tipo
  > Sorteios = `1 + floor(N ÷ 3)`. Tabela de drop: N1–3 (50% Nada, 45% Simples, 5% Boa); N4–6 (30% Nada, 45% Simples, 20% Boa, 5% Excelente); N7–9 (20% Nada, 30% Simples, 30% Boa, 17% Excelente, 3% Divina); N10 (10% Nada, 20% Simples, 35% Boa, 27% Excelente, 8% Divina).

## Backend

### Entidades e repositórios

- **Entidade `Pedra`** (JPA, table `pedra`):
  - `id`: UUID/Long, PK.
  - `vila_id`: FK, referência não nula à vila.
  - `qualidade`: ENUM (SIMPLES, BOA, EXCELENTE, DIVINA).
  - `bonus`: JSONB, lista de objetos com campos `codigo` (String) e `magnitude` (ENUM: BAIXA, MÉDIA, ALTA).
  - `item_id`: FK (nullable), referência opcional ao item que contém a pedra (nulo se no inventário).
  - `criada_em`: Timestamp.

  Exemplo JSONB de bonus:
  ```json
  [
    {"codigo": "VIT", "magnitude": "MÉDIA"},
    {"codigo": "ATK", "magnitude": "MÉDIA"}
  ]
  ```

- **Repositório `PedraRepository`**: extends `JpaRepository<Pedra, Long>`.
  - `findByVilaIdAndItemIdIsNull(vilaId)`: pedras do inventário.
  - `findByVilaIdAndItemIdIsNotNull(vilaId)`: pedras engastadas.

### Serviço de geração de pedras

**Classe `GeradorPedras`** (serviço puro, sem injeções de dependência além de `Random` ou `java.util.Random`):

**Método público:**
```
Pedra gerar(int nivelMasmorra, Random semente)
```

Retorna uma única pedra sorteada conforme a tabela de drop do nível, ou `null` se o sorteio resultar em "Nada".

**Lógica:**
1. Determinar número de bônus da pedra:
   - Nada (50%, 30%, 20%, 10% conforme nível): retorna `null`.
   - Simples (45%, 45%, 30%, 20%): 1 bônus.
   - Boa (5%, 20%, 30%, 35%): 2 bônus.
   - Excelente (0%, 5%, 17%, 27%): 3 bônus.
   - Divina (0%, 0%, 3%, 8%): 4 bônus.

2. Determinar faixa de magnitude:
   - N1–3: Baixa.
   - N4–6: Baixa.
   - N7–9: Média.
   - N10: Alta.

3. Sortear bônus distintos do catálogo:
   - Lista todos os códigos disponíveis (VIT, FOR, ..., PROD).
   - Sorteia sem reposição quantos forem necessários.
   - Cada bônus recebe a magnitude determinada no passo 2.

**Método de teste (testes unitários):**
```
void testeSorteioN3ComSementes()
```
Executa 1000 sorteios para N3 com semente fixa, valida que:
- 50% retorna `null`.
- 45% retorna Simples.
- 5% retorna Boa.
- Nenhum Excelente/Divina.

Usa `Random(12345)` para reprodutibilidade.

## Frontend

Não se aplica. (Pedras são apenas resultado da batalha, exibidas no relatório gerado pelo backend.)

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/pedra/Pedra.java](/src/main/java/com/example/loginbase/jogo/pedra/Pedra.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/pedra/PedraRepository.java](/src/main/java/com/example/loginbase/jogo/pedra/PedraRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/pedra/GeradorPedras.java](/src/main/java/com/example/loginbase/jogo/pedra/GeradorPedras.java) (novo)
- [/src/test/java/com/example/loginbase/jogo/pedra/GeradorPedrasTest.java](/src/test/java/com/example/loginbase/jogo/pedra/GeradorPedrasTest.java) (novo)

## Testes

- **Teste unitário: `testeSorteioN3ComSementes()`**
  - Executa 1000 sorteios com semente fixa.
  - Valida distribuição: ~50% Nada, ~45% Simples, ~5% Boa.
  - Margem de erro: ±2%.

- **Teste unitário: `testeDistribuicaoN10()`**
  - 1000 sorteios para N10 com semente fixa.
  - Valida: 10% Nada, 20% Simples, 35% Boa, 27% Excelente, 8% Divina.

- **Teste unitário: `testeBonusDistintos()`**
  - Gera 100 pedras Divina (N10).
  - Para cada uma, valida que nenhum código de bônus se repete.

- **Teste unitário: `testeFaixaMagnitudeN7()`**
  - Gera pedras para N7.
  - Valida que todos os bônus têm magnitude MÉDIA.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1, CA2, CA3, CA4.
- Build do backend (`./mvnw verify`) sem erros.
- Testes listados acima passando (cobertura ≥80% da classe `GeradorPedras`).
- Entidade `Pedra` com migração Flyway correspondente (V<N>__criar_tabela_pedra.sql).

## Fora de escopo

- Integração com batalha (será feita em H-001-tarefa-002, quando Batalha chamar o gerador).
- Engaste e inventário de pedras (H-002).
- Relatório visual no frontend (v1-014-batalha).
