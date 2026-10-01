# H-002 · Tarefa 1 — Slots de joias e efeitos

**História:** [h-002-usar-colar-e-aneis.md](h-002-usar-colar-e-aneis.md) · **Domínio:** [../joias.md](../joias.md) ·
**Depende de:** [../../v1-011-itens-e-fabricacao/historia/h-002-tarefa-001-regras-e-api-de-equipar.md](../../v1-011-itens-e-fabricacao/historia/h-002-tarefa-001-regras-e-api-de-equipar.md) · **Camada:** Backend

## Objetivo

Implementar validação de slots máximos de joias (1 colar, 2 anéis) por cidadão e garantir que efeitos de joias equipadas (bônus principal + intrínsecos) sejam somados ao calcular características e PV máximo.

## Contexto necessário

- [../../v1-002-cidadaos/cidadao.md](../../v1-002-cidadaos/cidadao.md) (seção PE efetivo)
  > Característica total = base + bônus de itens/pedras.

- [../../v1-011-itens-e-fabricacao/equipamento.md](../../v1-011-itens-e-fabricacao/equipamento.md)
  > Pessoa ≥14 anos; no painel: escolher item do inventário para um slot; item anterior volta ao inventário.

- [../joias.md](../joias.md) (seção Regras)
  > 1 colar máximo, 2 anéis máximo, efeitos somados.

## Backend

**Modelo de dados — extensão de `Cidadao`:**

Campo existente `item.slot` deve suportar valores:
- `COLAR` (máximo 1 por cidadão)
- `ANEL_1` ou `ANEL_2` (máximo 2 por cidadão)

Banco/JPA: validação única em `Cidadao` ou consulta em repositório.

**Serviço `EquiparService` (existente) — extensão:**

Adicionar validação em `equiparItem(Cidadao cidadao, Item item, Slot slot)`:

```
1. Verificar idade cidadao >= 14 anos (seção 7.5).
2. Se cidadao está em tropa em expedição (estado EM_VIAGEM_IDA/EM_VIAGEM_VOLTA):
   → rejeitar com "Não é permitido trocar equipamento em expedição".
3. Se slot = COLAR:
   → contar quantos itens.slot = 'COLAR' já tem cidadao.
   → se >= 1, rejeitar "Já há 1 colar equipado; máximo de colares é 1".
4. Se slot = ANEL_1 ou ANEL_2:
   → contar quantos itens.slot IN ('ANEL_1', 'ANEL_2') já tem cidadao.
   → se >= 2, rejeitar "Já há 2 anéis equipados".
5. Se há item atual em slot, devolver ao inventário (cidadao_id = null).
6. Equipar novo item (cidadao_id = cidadao.id, slot = slot).
```

**Serviço `CaracteristicaService` (existente) — extensão para cálculo de totais:**

Método `calcularCaracteristicaTotal(Cidadao cidadao, Caracteristica car)`:

```
total = cidadao.getCaracteristicaBase(car)
      + somarBonusDeItensEquipados(cidadao, car)
      + somarBonusDeItensNaoEquipados(cidadao, car) // se houver regra
      + somarBonusDePergrasMestre(cidadao) // de pedras engastadas
      
return total;

somarBonusDeItensEquipados(cidadao, car):
  items = SELECT * FROM item WHERE cidadao_id = cidadao.id AND categoria IN ('JOIA', 'ARMA', 'ARMADURA', 'FERRAMENTA')
  soma = 0
  PARA cada item:
    se item.categoria = 'JOIA':
      se item.subtipo = 'COLAR': 
        // Colar não afeta características; só Vida. Passar.
      se item.subtipo = 'ANEL':
        se item.atributo_escolhido = car:
          soma += efeitoPrincipalAnel(item.nivel) // +1/+2/+3/+4
        // intrínsecos somados abaixo
    // outros itens continuam (armas, armaduras, ferramentas)
    soma += somarBonusIntrinsecosItem(item, car)
  return soma;
```

**Serviço `VidaMaximaService` (novo ou extensão de `AtributoBatalhaService`):**

Método `calcularVidaMaxima(Cidadao cidadao)`:

```
fórmula (seção 10.1):
PV_max = 30 + 5*VIT + 3*G + Σ VIDA

onde:
  VIT = caracteristica total já calculada (inclui itens)
  G = PE efetivo Guerreiro
  Σ VIDA = soma de todos os bônus VIDA de itens equipados + pedras
  
vidaDosColar = SELECT SUM(efeitoPrincipal) FROM item WHERE cidadao_id = cidadao.id AND subtipo = 'COLAR'
vidaDasArmaduras = SELECT SUM(efeito_vida) FROM item WHERE cidadao_id = cidadao.id AND categoria = 'ARMADURA'
bonusIntrinsecosVida = SELECT SUM(valor) FROM item...bonus WHERE tipo = 'VIDA' AND cidadao_id = cidadao.id

Σ VIDA = vidaDosColar + vidaDasArmaduras + bonusIntrinsecosVida

return 30 + 5*VIT + 3*G + Σ VIDA;
```

**Validação em banco (opcional, mas recomendado):**

Adicionar constraint unique ou trigger em `item`:
```sql
-- Máximo 1 colar por cidadão
UNIQUE (cidadao_id, slot) WHERE slot = 'COLAR'

-- Máximo 2 anéis por cidadão
ALTER TABLE item ADD CONSTRAINT check_anel_max_2 
  CHECK (
    (SELECT COUNT(*) FROM item i2 WHERE i2.cidadao_id = item.cidadao_id 
     AND i2.slot IN ('ANEL_1', 'ANEL_2')) <= 2
  );
```

## Frontend

Não se aplica (interface do painel fica em tarefa de frontend).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/item/JoiaEquipamentoService.java](/src/main/java/com/example/loginbase/jogo/item/JoiaEquipamentoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/cidadao/CaracteristicaService.java](/src/main/java/com/example/loginbase/jogo/cidadao/CaracteristicaService.java) (modificação)
- [/src/main/java/com/example/loginbase/jogo/batalha/VidaMaximaCalculadora.java](/src/main/java/com/example/loginbase/jogo/batalha/VidaMaximaCalculadora.java) (novo)
- [/src/main/resources/db/migration/VN__add_joia_slots.sql](/src/main/resources/db/migration/VN__add_joia_slots.sql) (novo)

## Testes

- `JoiaEquipamentoServiceTest.testEquiparColarMaximo()` — tentar equipar 2º colar, deve falhar.
- `JoiaEquipamentoServiceTest.testEquiparAnelMaximo()` — tentar equipar 3º anel, deve falhar.
- `JoiaEquipamentoServiceTest.testDesequiparColar()` — desequipar colar, volta ao inventário; PV recalculado.
- `CaracteristicaServiceTest.testCaracteristicaTotalComAneis()` — cidadão base FOR 5, 2 anéis L2 +1 FOR cada = 7 total.
- `VidaMaximaCalculadoraTest.testColarSomaVida()` — PV base 50, Colar L3 +15 = 65 PV.
- `VidaMaximaCalculadoraTest.testBolusIntrinsecosVida()` — Colar L5 +25 + intrínseco +2 VIDA = +27 total.
- `JoiaEquipamentoServiceTest.testIdadeMinima()` — criança 10 anos, rejeitar equipar joia.
- `JoiaEquipamentoServiceTest.testTropaEmExpedicao()` — guerreiro em expedição, rejeitar trocar equipamento.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA8.
- Build do backend (`./mvnw verify`) sem erros.
- Testes listados passando (incluindo margem da validação de slots).
- Características totais recalculadas ao equipar/desequipar joias.
- PV máximo recalculado ao equipar/desequipar joias ou colares.

## Fora de escopo

- Geração de efeitos ao fabricar (fica em tarefa de fabricação).
- Interface de painel (fica em tarefa de frontend).
- Engaste de pedras (fica em tarefa de pedras).
