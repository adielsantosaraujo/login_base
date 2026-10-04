# H-001 · Tarefa 003 — Tela de distribuição de pontos e família líder

**História:** [H-001 — Gerar famílias...](h-001-gerar-familias-e-distribuir-pontos-iniciais.md) · **Domínio:** [../cidadao.md](../cidadao.md) · **Depende de:** [h-001-tarefa-002-geracao-das-familias-iniciais.md](h-001-tarefa-002-geracao-das-familias-iniciais.md) · **Camada:** Frontend

## Objetivo

Criar tela Vue + PrimeVue para a distribuição dos 16 cidadãos iniciais (4 famílias × 4 membros) com sugestão automática pelo plano padrão. Permitir ajuste de características, profissões e plano; selecionar família líder; confirmar com validações de mínimos; enviar dados ao backend com nova rota `/jogo/distribuir-populacao` e contrato GET/POST com `familiaLiderId` e chaves em MAIÚSCULAS.

## Contexto necessário

- [cidadao.md](../cidadao.md) — características, profissões, família líder
  > Características: VIT, FOR, VEL, INT, CAR. Profissões: 12 tipos.

- [familias.md](../familias.md) — bônus líder
  > Líder = adulto mais velho da família escolhida. Bônus +1% eficiência a cada 2 CAR, máx. +10%.

## Backend

**Endpoints** (novos):

### GET /api/jogo/vila/populacao → 200 (Consultar distribuição)

```json
{
  "populacaoConfirmada": false,
  "plano": { "COMERCIANTE": 1, "CONSTRUTOR": 2, "CARREGADOR": 2, "MADEIREIRO": 2, "MINEIRO": 2, 
             "AGRICULTOR": 2, "FAZENDEIRO": 1, "COZINHEIRO": 1, "GUERREIRO": 2, "FERREIRO": 1, "COSTUREIRO": 0, "CACADOR": 0 },
  "minimos": { "CONSTRUTOR": 2, "CARREGADOR": 2 },
  "limites": { "caracteristicasTotal": 20, "profissoesTotal": 10 },
  "familiaLiderSugeridaId": 11,
  "familiaLiderId": null,
  "familias": [
    { "familiaId": 11, "sobrenome": "Oliveira",
      "cidadaos": [
        { "cidadaoId": 101, "nome": "Marcos", "sexo": "M", "idadeAnos": 40, "papel": "PAI",
          "caracteristicas": { "VIT": 1, "FOR": 1, "VEL": 5, "INT": 0, "CAR": 13 },
          "profissoes": { "CONSTRUTOR": 0, "CARREGADOR": 2, "AGRICULTOR": 0, "FAZENDEIRO": 0, "MINEIRO": 0,
                          "MADEIREIRO": 0, "FERREIRO": 0, "COZINHEIRO": 3, "COSTUREIRO": 0, "CACADOR": 0,
                          "GUERREIRO": 0, "COMERCIANTE": 5 } } ] } ]
}
```

- Enquanto `populacaoConfirmada = false`: características/profissões são sugestão (calculadas, não persistidas); recarregar dá o mesmo resultado.
- Papel derivado de pai/mãe nulos: PAI (sexo M) ou MAE (F); senão FILHO (M) ou FILHA (F).
- Erros: 404 `VILA_NAO_ENCONTRADA` se sem vila.

### POST /api/jogo/vila/populacao → 200 (Confirmar distribuição)

Corpo:
```json
{ "familiaLiderId": 11,
  "familias": [ { "familiaId": 11, "cidadaos": [ { "cidadaoId": 101,
      "caracteristicas": { "VIT": 1, "FOR": 1, "VEL": 5, "INT": 0, "CAR": 13 },
      "profissoes": { "COMERCIANTE": 5, "COZINHEIRO": 3, "CARREGADOR": 2 } } ] } ] }
```

Resposta 200:
```json
{ "bonusLider": 6, "familiaLiderId": 11, "proximaEtapa": "MAPA" }
```

**Chaves de características e profissões**: MAIÚSCULAS (VIT, FOR, VEL, INT, CAR); entrada aceita qualquer caixa. Profissões sempre com as 12 chaves (zeros incluídos). Familias ordenadas por ID; cidadãos na ordem PAI, MAE, FILHO, FILHA.

**Validações backend** (ordem de execução):
1. População já confirmada → 409 `POPULACAO_JA_CONFIRMADA` "A população já foi confirmada".
2. Cidadãos incompletos (faltando, repetidos, de outra vila) → 400 `POPULACAO_INCOMPLETA` "Informe os 16 cidadãos da vila".
3. Valor nulo, negativo ou chave desconhecida → 400 `PONTOS_INVALIDOS` "Pontos inválidos para <nome>".
4. Σ características > 20 em um cidadão → 400 `LIMITE_CARACTERISTICAS` "Máximo 20 pontos de característica (<nome>)".
5. Σ profissões > 10 em um cidadão → 400 `LIMITE_PROFISSOES` "Máximo 10 pontos de profissão (<nome>)".
6. `familiaLiderId` nulo ou de outra vila → 400 `FAMILIA_LIDER_OBRIGATORIA` "Escolha uma família líder".
7. < 2 cidadãos com Construtor principal → 400 `MINIMO_CONSTRUTORES` "São necessários ao menos 2 Construtores principais".
8. < 2 cidadãos com Carregador principal → 400 `MINIMO_CARREGADORES` "São necessários ao menos 2 Carregadores principais".

**Efeitos** (após aprovação):
- Grava valores **absolutos** em `cidadao` (VIT, FOR, VEL, INT, CAR) e `cidadao_profissao` (só para valor > 0).
- Calcula pendentes: `pontos_car_pendentes = 20 − Σcar`, `pontos_prof_pendentes = 10 − Σprof`.
- Define `vila.familiaLiderId` (bigint).
- Marca `vila.populacaoConfirmada = true`.
- Sem máximo por atributo (VIT 20 e Construtor 10 são aceitos).
- Pendências não bloqueiam confirmação.
- `bonusLider` = `min(10, floor(CAR ÷ 2))` do líder (adulto mais velho; empate PAI → MAE → FILHO → FILHA).

## Frontend

**Rota e layout**:
- Rota: `/jogo/distribuir-populacao` (name `distribuir-populacao`, meta `etapaInicial: true`, componente `DistribuicaoPopulacao.vue`).
- Layout: cabeçalho `CabecalhoJogo` com abas inativas, painel principal com 4 seções (famílias, plano, população, líder).
- Guarda: se população pendente, direciona para esta rota; se confirmada, redireciona para `/jogo/mapa`.

**Seção Famílias** (abas com cidadãos):
- TabView com 4 abas: "Família Oliveira · N pontos pendentes", "Família Lima · ...", etc.
- Cada aba lista 4 cidadãos (pai, mãe, filho, filha) com:
  - Nome, papel, idade (read-only).
  - Seletor de profissão principal (dropdown).
  - Steppers (+ / −) para 5 características (VIT, FOR, VEL, INT, CAR); máx. 20 total por cidadão.
  - Steppers (+ / −) para 12 profissões; máx. 10 total por cidadão.
  - Bônus R3 por profissão: `Σ floor(característica ÷ 5)` das características ligadas, com tooltip.
  - Contador de pontos usados / limite em destaque (vermelho se = limite).

**Painel Plano inicial** (stepper, mínimos em coluna):
- Lista ordenada das 12 profissões com stepper de quantidade (min: mínimo obrigatório; máx: 16 − outras).
- Coluna "Atual" mostra cidadãos com a profissão como principal (em vermelho se < mínimo, em aviso se ≠ plano).
- Total em proeminência: "10 / 16" (aviso) ou "16 / 16" (ok).
- "Redistribuir pelo plano" (desabilitado se total ≠ 16): refaz os 16 cidadãos.
- "Zerar tudo": limpa pontos sem mudar plano nem líder.

**Painel População**:
- Checklist com status:
  - "Mínimos: 2 Construtores, 2 Carregadores" (vermelho se não atendido).
  - "Pontos pendentes" (aviso se > 0, não bloqueia).

**Painel Família líder** (RadioButton):
- 4 opções com nome da família, líder (nome, idade, papel), CAR e bônus +N%.
- Pré-selecionada: família com líder de maior CAR.

**Botão Confirmar população**:
- Habilitado: mínimos atendidos + família líder escolhida.
- Desabilitado: mostra "Ajuste os mínimos" (tooltip).
- Clique: POST `/api/jogo/vila/populacao` com familiaLiderId e 16 cidadãos (valores absolutos).
- Spinner durante requisição.
- Sucesso: navega para `/jogo/mapa`.
- Erro: exibe toast com mensagem e código (MINIMO_CONSTRUTORES, LIMITE_CARACTERISTICAS, etc.).

**Fluxo**:
1. GET `/api/jogo/vila/populacao` carrega a distribuição sugerida.
2. Tela exibe 4 abas com cidadãos, plano padrão e família sugerida.
3. Jogador pode ajustar características, profissões, plano e líder.
4. Trocar profissão principal de um cidadão refaz a distribuição dele.
5. "Redistribuir" refaz todos (exigindo plano = 16).
6. Clica "Confirmar" → valida mínimos → POST → sucesso → navega para mapa.

## Arquivos prováveis

**Backend:**
- [/src/main/java/com/example/loginbase/jogo/cidadao/PopulacaoService.java](/src/main/java/com/example/loginbase/jogo/cidadao/PopulacaoService.java) (novo/atualizado)
- [/src/main/java/com/example/loginbase/jogo/cidadao/PopulacaoController.java](/src/main/java/com/example/loginbase/jogo/cidadao/PopulacaoController.java) (novo)
- DTOs: PopulacaoDTO, FamiliaDTO, CidadaoDTO, etc. (novos, em `jogo/dto`)

**Frontend:**
- [/frontend/src/views/DistribuicaoPopulacao.vue](/frontend/src/views/DistribuicaoPopulacao.vue) (novo; tela principal)
- [/frontend/src/components/populacao/FamiliaTabs.vue](/frontend/src/components/populacao/FamiliaTabs.vue) (novo; abas)
- [/frontend/src/components/populacao/CidadaoCard.vue](/frontend/src/components/populacao/CidadaoCard.vue) (novo; características e profissões)
- [/frontend/src/components/populacao/PlanoInicialPainel.vue](/frontend/src/components/populacao/PlanoInicialPainel.vue) (novo; stepper do plano)
- [/frontend/src/components/populacao/FamiliaLiderSelector.vue](/frontend/src/components/populacao/FamiliaLiderSelector.vue) (novo; escolha de líder)
- [/frontend/src/components/populacao/ConfirmacaoPainel.vue](/frontend/src/components/populacao/ConfirmacaoPainel.vue) (novo; painel de confirmação)
- [/frontend/src/domain/populacao.ts](/frontend/src/domain/populacao.ts) (novo; porte TS do algoritmo)
- [/frontend/src/composables/usePopulacao.ts](/frontend/src/composables/usePopulacao.ts) (novo; reexporta constantes)

## Testes

**Backend:**
- Teste: GET retorna distribuição sugerida (plano padrão) com caractéristicas e profissões.
- Teste: POST com valores válidos confirma população e retorna bonusLider correto.
- Teste: POST recusa < 2 Construtores (erro `MINIMO_CONSTRUTORES`).
- Teste: POST recusa < 2 Carregadores (erro `MINIMO_CARREGADORES`).
- Teste: POST recusa Σ características > 20 (erro `LIMITE_CARACTERISTICAS`).
- Teste: POST recusa Σ profissões > 10 (erro `LIMITE_PROFISSOES`).
- Teste: POST recusa sem familiaLiderId (erro `FAMILIA_LIDER_OBRIGATORIA`).
- Teste: POST recusa população já confirmada (erro `POPULACAO_JA_CONFIRMADA`).

**Frontend:**
- Teste: Tela abre com GET `/api/jogo/vila/populacao` e carrega distribuição.
- Teste: Steppers incrementam/decrementam pontos sem exceder limites totais.
- Teste: Trocar profissão principal refaz distribuição do cidadão.
- Teste: "Redistribuir pelo plano" refaz os 16 cidadãos.
- Teste: Plano ≠ 16 desabilita "Redistribuir".
- Teste: Mínimos < 2 desabilitam "Confirmar".
- Teste: Clicar "Confirmar" POST com sucesso → navega para `/jogo/mapa`.
- Teste: Erro 400 exibe toast com mensagem e código.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA2 a CA11.
- Build do backend (`./mvnw verify`) sem erros.
- Build do frontend (`npm run build`) sem erros.
- Testes backend (unitários e de integração) passando.
- Testes frontend (unitários e E2E) passando.
- GET e POST em conformidade com o contrato (D8), chaves em MAIÚSCULAS, papel derivado.
- Erros com códigos conforme D5 tabela de população.
- Validações backend reforçadas (sem máximo por atributo, apenas totais; pendências não bloqueiam).

## Fora de escopo

- Editar distribuição após confirmação.
- Múltiplas famílias líderes (apenas 1).
