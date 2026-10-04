# Contratos de API (proposta)

Todos os endpoints exigem usuário autenticado (Spring Security, padrão do projeto). Erros seguem o formato de erro já usado no projeto, com `codigo` e `mensagem`.

## 1. Gerar prévia do mapa

`POST /api/jogo/vila/previa`

Gera (ou regenera) a prévia do mapa para um usuário **sem vila**. Ela fica guardada no servidor (sessão ou tabela `vila_previa`) com uma `semente`. Chamar de novo substitui a prévia ("Gerar novo mapa").

**Resposta 200**
```json
{
  "previaId": "uuid",
  "rodada": 3,
  "regioes": [
    { "indice": 1, "tipo": "FLORESTA",
      "bonus": [ { "bonus": "FLORESTA", "posicao": 1, "valor": 44 },
                 { "bonus": "PLANTACOES", "posicao": 2, "valor": 21 },
                 { "bonus": "BARREIRO", "posicao": 3, "valor": 9 } ] }
  ]
}
```
- `regioes`: 16 itens, `indice` de 1 a 16.
- `tipo` ∈ `FLORESTA | PLANICIE | URBANA | LITORAL | MONTANHA`.
- `bonus` ∈ `FLORESTA | BARREIRO | PLANTACOES | CRIACOES | ROCHA | FERRO | CARVAO | SALINAS | ENXOFRE | MILITAR | INDUSTRIA | COMERCIO | DESENVOLVIMENTO`.

**Erros**
- `409 VILA_JA_EXISTE`

## 2. Obter prévia atual

`GET /api/jogo/vila/previa`: retorna a prévia vigente (mesmo formato do §1), ou `404` se não houver. A tela chama este endpoint ao abrir e, se receber 404, chama o §1.

## 3. Criar vila

`POST /api/jogo/vila`

```json
{ "previaId": "uuid", "indices": [6, 7, 10] }
```
- `indices`: ordem de seleção (a 1ª região Urbana da lista recebe as 4 casas N1).

**Validações**

| Regra | Erro |
|---|---|
| exatamente 3 índices, distintos, de 1 a 16 | `400 SELECAO_INVALIDA` |
| cada índice após o 1º é vizinho de algum anterior | `400 REGIAO_NAO_ADJACENTE` "Região deve ser adjacente a uma já selecionada" |
| ≥ 1 Urbana | `400 SEM_REGIAO_URBANA` "Ao menos uma região deve ser Urbana" |
| `previaId` é a prévia vigente do usuário | `409 PREVIA_EXPIRADA` |
| usuário sem vila | `409 VILA_JA_EXISTE` |

**Efeitos**
- Cria a Vila com a semente da prévia e grava as 16 regiões (tipo + bônus), marcando as 3 como possuídas.
- Calcula os bônus de região da vila (soma das 3).
- Cria o estoque inicial e as 4 casas N1.
- Gera os 16 cidadãos **sem pontos** e a distribuição sugerida (§4).

**Resposta 201** `{ "vilaId": "uuid", "proximaEtapa": "DISTRIBUIR_POPULACAO" }` → o frontend redireciona para `/app/jogo/distribuir-populacao`.

## 4. Obter população + sugestão

`GET /api/jogo/vila/populacao`

```json
{
  "plano": { "COMERCIANTE": 1, "CONSTRUTOR": 2, "CARREGADOR": 2, "MADEIREIRO": 2, "MINEIRO": 2,
             "AGRICULTOR": 2, "FAZENDEIRO": 1, "COZINHEIRO": 1, "GUERREIRO": 2, "FERREIRO": 1,
             "COSTUREIRO": 0, "CACADOR": 0 },
  "minimos": { "CONSTRUTOR": 2, "CARREGADOR": 2 },
  "limites": { "caracteristicasTotal": 20, "profissoesTotal": 10 },
  "familiaLiderSugerida": 0,
  "familias": [
    { "familiaId": "uuid", "sobrenome": "Oliveira",
      "cidadaos": [
        { "cidadaoId": "uuid", "nome": "Marcos", "sexo": "M", "idadeAnos": 40, "papel": "PAI",
          "caracteristicas": { "vit": 1, "for": 0, "vel": 4, "int": 0, "car": 15 },
          "profissoes": { "COMERCIANTE": 5, "COZINHEIRO": 3, "CARREGADOR": 2 } } ] } ]
}
```

O ajuste e a redistribuição podem ser feitos **no frontend**, com o mesmo algoritmo, sem chamar o servidor a cada clique. Se for melhor centralizar no servidor, use:

`POST /api/jogo/vila/populacao/sugestao` com o corpo `{ "plano": { ... } }`, que retorna `familias` no mesmo formato.

## 5. Confirmar população

`POST /api/jogo/vila/populacao`

```json
{
  "familiaLiderIndex": 0,
  "familias": [
    { "familiaId": "uuid",
      "cidadaos": [ { "cidadaoId": "uuid",
                      "caracteristicas": { "vit": 1, "for": 0, "vel": 4, "int": 0, "car": 15 },
                      "profissoes": { "COMERCIANTE": 5, "COZINHEIRO": 3, "CARREGADOR": 2 } } ] } ]
}
```

**Validações**

| Regra | Erro |
|---|---|
| Σ características ≤ 20 por cidadão; cada ≥ 0 | `400` "Máximo 20 pontos de característica" |
| Σ profissões ≤ 10 por cidadão; cada ≥ 0 | `400` "Máximo 10 pontos de profissão" |
| ≥ 2 cidadãos com principal = Construtor | `400 MINIMO_CONSTRUTORES` |
| ≥ 2 cidadãos com principal = Carregador | `400 MINIMO_CARREGADORES` |
| `familiaLiderIndex` ∈ 0..3 | `400` "Escolha uma família líder" |
| todos os 16 cidadãos presentes, pertencentes à vila | `400 POPULACAO_INCOMPLETA` |
| população ainda não confirmada | `409 POPULACAO_JA_CONFIRMADA` |

**Efeitos**
- Grava os pontos; o restante fica em `pontos_car_pendentes = 20 − Σcar` e `pontos_prof_pendentes = 10 − Σprof`.
- Define o líder (o adulto mais velho da família; em empate, a ordem do papel) e o bônus `min(10, floor(CAR ÷ 2))`%.

**Resposta 200** `{ "bonusLider": 5, "redirect": "/app/jogo/mapa" }`

## Guardas de rota (frontend)

| Situação | Rota |
|---|---|
| sem vila | `/app/jogo/criar-vila` |
| vila criada, população não confirmada | `/app/jogo/distribuir-populacao` |
| população confirmada | `/app/jogo/mapa` |
