# H-001 · Tarefa 001 — Catálogo de armaduras

**História:** [H-001 — Fabricar armaduras](h-001-fabricar-armaduras.md) · **Domínio:** [../armaduras.md](../armaduras.md) · **Depende de:** — · **Camada:** Backend

## Objetivo

Criar um catálogo centralizado (enum/constantes) de todas as 6 peças de armadura com receitas, níveis máximos, custos e defesa. Este catálogo alimentará o sistema de fabricação e equipamento.

## Contexto necessário

- [../armaduras.md](../armaduras.md) — definição de todas as 6 peças, receita base, defesa base
  > Peitoral: 5 Ferro, 8 DEF; Capacete: 3 Ferro, 4 DEF; Ombreiras: 3 Ferro, 3 DEF; Luvas: 2 Couro curtido, 2 DEF; Calças: 3 Couro curtido + 1 Tecido, 5 DEF; Sapato: 2 Couro curtido, 2 DEF (+ 1 INI).

- [../../v1-003-construcoes/construcoes.md](../../v1-003-construcoes/construcoes.md) — limites por nível de oficina
  > Ferraria: N1 L3, N2 L6, N3 L10; Alfaiataria: N1 L3, N2 L6, N3 L10.

- [../../v1-011-itens-e-fabricacao/fabricacao.md](../../v1-011-itens-e-fabricacao/fabricacao.md) — regras gerais de PF e custo
  > PF = 1 + L; Custo = receita base × L; para L ≥ 6, Ferro → Aço.

## Backend

**Entidades e enums:**
- Enum `CategoriaArmadura` (ou `TipoArmadura`): PEITORAL, CAPACETE, OMBREIRAS, LUVAS, CALCAS, SAPATO.
- Enum `OficinaArmadura`: FERRARIA (Peitoral, Capacete, Ombreiras), ALFAIATARIA (Luvas, Calças, Sapato).
- Classe `CatalogoArmadura` (ou `ArmadurasData`) no módulo `catalogo`:
  - Método `getReceita(armadura, nivel)`: retorna Map<Recurso, Integer> com multiplicação de base × L.
  - Método `getDefesaBase(armadura)`: retorna número para defesa base.
  - Método `getOficina(armadura)`: retorna OficinaArmadura.
  - Método `getNivelMaximoOficina(oficina, nivelOficina)`: retorna L máximo (N1→3, N2→6, N3→10).
  - Método `getPF(nivel)`: retorna 1 + nivel (comum a todos os itens).

**Conversão de Ferro para Aço:**
- Helper `converteFerroPara Aço(receita, nivel)`: para cada entrada com Recurso.FERRO onde nivel ≥ 6, troca para Recurso.ACO.

## Frontend

Não se aplica nesta tarefa. (Tela de fabricação é tarefa separada em H-001.)

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/catalogo/CatalogoArmaduras.java](/src/main/java/com/example/loginbase/jogo/catalogo/CatalogoArmaduras.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/item/CategoriaArmadura.java](/src/main/java/com/example/loginbase/jogo/item/CategoriaArmadura.java) (novo)
- [/src/test/java/com/example/loginbase/jogo/catalogo/CatalogoArmadurasTest.java](/src/test/java/com/example/loginbase/jogo/catalogo/CatalogoArmadurasTest.java) (novo)

## Testes

1. **Receita de Peitoral**: L1 = 5 Ferro; L5 = 25 Ferro; L6 = 30 Aço; L10 = 50 Aço.
2. **Receita de Luvas**: L1 = 2 Couro curtido; L5 = 10 Couro curtido; L6 = 12 Couro curtido.
3. **PF**: L1 = 2, L5 = 6, L10 = 11.
4. **Defesa base**: Peitoral 8, Capacete 4, Ombreiras 3, Luvas 2, Calças 5, Sapato 2.
5. **Nível máximo por oficina**: Ferraria N1 até L3, N2 até L6, N3 até L10; idem Alfaiataria.
6. **Conversão automática**: verificar que Peitoral L6 retorna Aço, não Ferro.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1, CA2, CA3, CA4, CA5.
- Build do backend (`./mvnw verify`) sem erros.
- Testes todos passando; cobertura ≥ 90% no catálogo.
- Catálogo centralizado em módulo `jogo.catalogo`.

## Fora de escopo

- Persistência de armaduras em banco (fica em H-001 tarefa 2 ou em v1-011).
- Cálculo de atributos de combate com armadura (fica em H-002).
- Interface gráfica da oficina.
