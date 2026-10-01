# H-001 · Tarefa 002 — Motor de batalha

**História:** [H-001 — Resolver batalha por rodadas](h-001-resolver-batalha-por-rodadas.md) · **Domínio:** [batalha.md](../batalha.md) ·
**Depende de:** [H-001 · Tarefa 001 — Cálculo de atributos de combate](h-001-tarefa-001-calculo-de-atributos-de-combate.md) · **Camada:** Backend

## Objetivo

Implementar mecanismo central de resolução de batalha: rodadas sequenciais, ordem de iniciativa, seleção de alvo, cálculo de dano, críticos, e gravação de log. Terminar quando um lado é eliminado ou 30 rodadas são atingidas.

## Contexto necessário

- Fluxo de batalha (seção 10.3)
  > - Máximo de **30 rodadas**
  > - Ordem por **Iniciativa** (desempate: VEL, depois ID)
  > - Alvos: corpo a corpo atinge só frente; lança de qualquer linha; distância qualquer alvo
  > - Escolha: tropa mira inimigo com **menor PV**; inimigos miram **aleatório**
  > - Fim: um lado sem vivos; após 30 rodadas, tropa recua (derrota)

- Crítico (seção 10.1, 6.2)
  > Chance = 5% + 0,5% × VEL + Σ CRIT; dano crítico ×1,5

- Dano e defesa efetiva (seção 10.2)
  > dano = max(1; round(Ataque × 100 ÷ (100 + 3 × Defesa_efetiva) × U(0,9; 1,1)))
  > Defesa_efetiva = Defesa × 0,75 se atacante é Besta ou Xamã orc

- Alcance de armas (seção 7.8)
  > Espada: corpo a corpo, só frente
  > Lança: corpo a corpo, frente ou retaguarda
  > Arco/Besta: à distância, qualquer alvo

## Backend

**Serviço**: `MotorBatalhaService`

Métodos:
- `resolverBatalha(participantes_jogador: List<Guerreiro>, inimigos: List<Inimigo>, semente: long) → ResultadoBatalha`
  - Inicializa RNG com semente
  - Loop de rodadas (máx. 30):
    1. Calcula Iniciativa + 1d6 de cada participante
    2. Ordena por Iniciativa (desempate: VEL, ID)
    3. Cada ator vivo:
       - Seleciona alvo válido (regra de alcance)
       - Calcula dano (com sorteio de crítico)
       - Aplica dano ao alvo
       - Grava ação no log
    4. Remove mortos de 0 PV
    5. Checa vitória (um lado sem vivos)
  - Retorna: {resultado: VITORIA/DERROTA, rodadas, log, recompensas}

- `selecionarAlvo(ator, aliados, inimigos, arma) → Inimigo`
  - Se jogador: retorna inimigo com menor PV atual e válido por alcance
  - Se inimigo: retorna alvo permitido aleatório

- `validarAlcance(atacante_linha, alvo_linha, tipo_arma) → boolean`
  - Espada: só ataca frente se estiver na frente
  - Lança: qualquer linha ataca qualquer linha
  - Distância (arco/besta): qualquer linha ataca qualquer linha

**Modelo de dados**:

Tabela `batalha`:
- id, vila_id, tropa_id, masmorra_id, turno, semente, resultado (VITORIA/DERROTA), log (jsonb), recompensas (jsonb)

Log JSON:
```json
{
  "rodada": 1,
  "participantes_iniciais": [
    {"tipo": "GUERREIRO", "id": 123, "pv_max": 79, ...},
    {"tipo": "INIMIGO", "classe": "GOBLIN", "pv_max": 40, ...}
  ],
  "acoes": [
    {
      "rodada": 1,
      "atacante": {"tipo": "GUERREIRO", "id": 123},
      "alvo": {"tipo": "INIMIGO", "classe": "GOBLIN", "id": 0},
      "dano": 23,
      "critico": false,
      "pv_alvo_apos": 17
    },
    ...
  ]
}
```

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/batalha/MotorBatalhaService.java](/src/main/java/com/example/loginbase/jogo/batalha/MotorBatalhaService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/dto/ResultadoBatalha.java](/src/main/java/com/example/loginbase/jogo/batalha/dto/ResultadoBatalha.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/dto/AcaoBatalha.java](/src/main/java/com/example/loginbase/jogo/batalha/dto/AcaoBatalha.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/Batalha.java](/src/main/java/com/example/loginbase/jogo/batalha/Batalha.java) (novo, entidade JPA)
- [/src/test/java/com/example/loginbase/jogo/batalha/MotorBatalhaServiceTest.java](/src/test/java/com/example/loginbase/jogo/batalha/MotorBatalhaServiceTest.java) (novo)

## Testes

1. **Reprodução do exemplo 10.4**: 1 guerreiro (Ataque 29, DEF 15, PV 79) vs. 1 Goblin (ATQ 16, DEF 8, PV 40)
   - Entrada: semente fixa
   - Esperado: Goblin cai em 2 rodadas (2 × 23 dano); resultado VITORIA; log com exatamente 2 ações

2. **Limite de 30 rodadas**: combate duradouro sem eliminação
   - Esperado: batalha termina na rodada 30 com resultado DERROTA

3. **Validação de alcance (Espada)**: guerreiro com Espada na retaguarda vs. inimigo
   - Esperado: ataque é inválido (sem alvo válido); trata como passe de rodada

4. **Validação de alcance (Lança)**: guerreiro com Lança na retaguarda vs. inimigo na frente
   - Esperado: ataque é válido

5. **Crítico com VEL 5**: chance 5% + 0,5% × 5 = 7,5%; múltiplas rodadas verificam ~7,5% de críticos

6. **Defesa efetiva de Besta**: atacante com Besta, alvo com DEF 100
   - Esperado: usa Defesa_efetiva = 75 no cálculo de dano

7. **Seleção de alvo (jogador)**: múltiplos inimigos com PVs 40, 30, 50
   - Esperado: jogador sempre ataca o com 30 PV (menor)

## Definição de pronto

- Critérios de aceite da história cobertos: CA1, CA3, CA4, CA5, CA6, CA7
- Build do backend sem erros
- Todos os testes listados passando
- Log reprodutível com mesma semente
- Integração com [Cálculo de atributos](h-001-tarefa-001-calculo-de-atributos-de-combate.md)

## Fora de escopo

- Persistência em banco de dados (feita em tarefa 003)
- Replay visual
- Consequências (ferimentos/mortes) — tarefa 003
