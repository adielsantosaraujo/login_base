# H-004 · Tarefa 003 — Ouro passivo, imposto e estalagem

**História:** [h-004-negociar-recursos-no-mercado.md](h-004-negociar-recursos-no-mercado.md) · **Domínio:** [../comercio.md](../comercio.md) ·
**Depende de:** — · **Camada:** Backend

## Objetivo

Implementar a etapa 2 do turno (ouro passivo) que coleta imposto de cidadãos e gera ouro pela Estalagem (servindo Refeições e processando imigração).

## Contexto necessário

- [../comercio.md#regras](../comercio.md#regras) — Imposto 0,5 Ouro por ≥18 anos; Estalagem 4 Ouro por Refeição servida (até 5/10/15 × eficiência × mult. nível); imigração chance 2/4/6%.
  > Imigrante: adulto 18–30 anos, 20 car + 10 prof aleatórios, se houver núcleo livre.

- Seção 4.10 da bíblia — Estalagem vagas Cozinheiro/Comerciante (2/5/10).

- Seção 2.2 da bíblia — passo 2 do turno.

## Backend

- **Serviço**
  - `OuroService.processarOuroPassivo(vila, turno)`: etapa 2
    - **Imposto**
      - Contar cidadãos vivos com idade ≥ 18 × 12 meses
      - Ouro += 0,5 × quantidade
      - Gravar evento IMPOSTO_COBRADO
    - **Estalagem**
      - Se não houver Estalagem ativa: skip
      - Contar Cozinheiros/Comerciantes alocados
      - Calcular capacidade: `5 × Σ eficiência × mult. nível` Refeições
      - Debitar do estoque, adicionar Ouro (4 por Refeição)
      - Gravar evento ESTALAGEM_RECEITA
      - **Imigração**
        - Chance = 2% × nível (N1 2%, N2 4%, N3 6%)
        - Se sortear sucesso E houver núcleo familiar livre:
          - Criar novo cidadão: adulto (idade 18–30 anos aleatório)
          - 20 pontos de característica + 10 de profissão distribuídos aleatoriamente
          - Núcleo próprio (solteiro), família nova
          - Gravar evento IMIGRANTE_CHEGOU

- **Testes**
  - `testImpostoBasico()`: 10 adultos → +5 Ouro
  - `testImpostoSemAdultos()`: população só menores → +0 Ouro
  - `testEstalagemN1()`: 1 Cozinheiro eficiência 1,0, 5 Refeições → consume 5, gera 20 Ouro
  - `testEstalagemSemRefeicao()`: 0 Refeição → gera 0 Ouro
  - `testImigraçãoSemNucleo()`: chance ativa mas sem núcleo livre → nada acontece
  - `testImigracaoComNucleo()`: chance ativa, núcleo livre → novo cidadão criado
  - `testImigracaoN1Chance()`: N1 2% chance (sorteio em 100 testes deve ter ~2)
  - `testEventosRegistrados()`: cada passo gera evento_turno

## Frontend

Não se aplica (Backend only).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/recurso/OuroService.java](/src/main/java/com/example/loginbase/jogo/recurso/OuroService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EtapaTurnoOuroPassivo.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaTurnoOuroPassivo.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/cidadao/GeraçãoImigranteCitizen.java](/src/main/java/com/example/loginbase/jogo/cidadao/GeradorImigrante.java) (novo)

## Testes

Todos listados acima.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1, CA2, CA3, CA4 (indireto)
- Testes passando (incluindo probabilidade de imigração)
- Build sem erros
- Etapa integrada ao pipeline de turno (passo 2, após Produção)
- Eventos registrados no relatório
- Imigrante gerado com distribuição aleatória válida

## Fora de escopo

- Gerenciamento manual de Refeições na Estalagem (automático)
- Nomes/sobrenomes customizáveis de imigrantes (gerados aleatoriamente de listas fixas)
