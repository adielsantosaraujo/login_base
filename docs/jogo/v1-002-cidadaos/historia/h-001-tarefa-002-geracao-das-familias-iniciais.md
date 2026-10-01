# H-001 · Tarefa 002 — Geração das famílias iniciais

**História:** [H-001 — Gerar famílias...](h-001-gerar-familias-e-distribuir-pontos-iniciais.md) · **Domínio:** [../cidadao.md](../cidadao.md) · **Depende de:** [h-001-tarefa-001-modelo-de-dados-de-cidadaos-e-familias.md](h-001-tarefa-001-modelo-de-dados-de-cidadaos-e-familias.md) · **Camada:** Backend

## Objetivo

Criar serviço que gera 4 famílias × 4 membros com nomes aleatórios, idades corretas (pai/mãe 40, filhos 18) e características/PE base todos em 0. Integrado à criação de vila.

## Contexto necessário

- [familias.md](../familias.md) — população inicial
  > 4 famílias × 4 membros: nomes de listas fixas; características todas 0; PE profissão todos 0.

- [h-001-tarefa-001](h-001-tarefa-001-modelo-de-dados-de-cidadaos-e-familias.md) — modelo de dados
  > Tabelas familia, cidadao, cidadao_profissao prontas.

## Backend

**Serviço** (`com.example.loginbase.jogo.cidadao.FamiliaService`):
- `gerarFamiliasIniciais(vila: Vila): List<Familia>`
  - Cria 4 famílias com sobrenomes aleatórios de lista.
  - Para cada família:
    - Pai: nome M aleatório, 40 anos (480 meses), sexo M.
    - Mãe: nome F aleatório, 40 anos (480 meses), sexo F.
    - Filho: nome M aleatório, 18 anos (216 meses), sexo M.
    - Filha: nome F aleatório, 18 anos (216 meses), sexo F.
  - Cada cidadão: vit=0, for=0, vel=0, int=0, car=0; pontosCarpendentes=0, pontosProffendentes=0.
  - Liga casal (conjuge_id recíproco).
  - Salva famílias e cidadãos em BD.
  - Retorna lista de 4 famílias.

**Listas de nomes**:
- Sobrenomes: lista fixa (ex.: Silva, Santos, Oliveira, Pereira, Souza, ...).
- Nomes masculinos: lista fixa (ex.: João, Pedro, Paulo, Carlos, ...).
- Nomes femininos: lista fixa (ex.: Maria, Ana, Paula, Carolina, ...).

**Método auxiliar**:
- `gerarCidadao(familia: Familia, nome: String, sexo: Char, idadeMeses: Int): Cidadao`
  - Cria cidadão com características 0, pendentes 0.

## Frontend

Não se aplica (backend apenas; a tela vem em tarefa 003).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/cidadao/FamiliaService.java](/src/main/java/com/example/loginbase/jogo/cidadao/FamiliaService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/cidadao/NomesFixos.java](/src/main/java/com/example/loginbase/jogo/cidadao/NomesFixos.java) (novo; listas de nomes)

## Testes

- Teste unitário: `gerarFamiliasIniciais` retorna 4 famílias com 4 membros cada (16 cidadãos).
- Teste de estrutura: pai/mãe 40 anos (480 meses), filhos 18 (216 meses).
- Teste de nomes: todos têm nome não vazio; nenhum duplicado na família.
- Teste de casamento: casal tem conjuge_id recíproco e não nulo.
- Teste de características: todos começam com 0 em todas.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1 (estrutura e idades).
- Build do backend (`./mvnw verify`) sem erros.
- Testes listados passando.
- Nenhum número inventado (idades, nomes de listas).

## Fora de escopo

- Distribuição de pontos (será tarefa 003).
- Integração com criação de vila (será em história h-001 ou nova tarefa).
