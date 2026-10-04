# H-002 · Tarefa 001 — XP de treinamento no turno

**História:** [h-002-treinar-guerreiros-no-quartel.md](h-002-treinar-guerreiros-no-quartel.md) · **Domínio:** [../tropas.md](../tropas.md) · **Depende de:** [h-001-tarefa-001-modelo-e-regras-de-tropa.md](h-001-tarefa-001-modelo-e-regras-de-tropa.md) · **Camada:** Backend

## Objetivo

Implementar a etapa 7 do processamento de turno (seção 2.2) que adiciona XP a guerreiros em tropas aquarteladas e converte XP em PE base quando atinge 10.

## Contexto necessário

- [../tropas.md](../tropas.md) — XP por nível de quartel (N1 0,5, N2 1,0, N3 1,5)
  > R7: Cada membro de tropa aquartelada ganha XP por turno conforme nível. R8: 10 XP = +1 PE base.

- [../../v1-009-turnos/turnos.md](../../v1-009-turnos/turnos.md) — Passo 7 da resolução
  > Etapa 7 executa treino dos guerreiros; transação por vila (idempotência).

- [../../v1-002-cidadaos/cidadao.md](../../v1-002-cidadaos/cidadao.md) — Campo `xp_guerreiro` na tabela cidadao
  > Almacena XP e PE base de Guerreiro.

## Backend

### Extensão de entidades

**Cidadao.java**
```java
@Column(name = "xp_guerreiro", columnDefinition = "DECIMAL(10,2)")
private BigDecimal xpGuerreiro = BigDecimal.ZERO;

// Método auxiliar
public void adicionarXpGuerreiro(BigDecimal xp) {
    this.xpGuerreiro = this.xpGuerreiro.add(xp);
    while (this.xpGuerreiro.compareTo(new BigDecimal("10")) >= 0) {
        // PE da profissão Guerreiro
        CidadaoProfissao profGuerreiro = this.profissoes.stream()
            .filter(p -> p.getProfissao() == Profissao.GUERREIRO)
            .findFirst()
            .orElseThrow();
        profGuerreiro.setPontosBase(profGuerreiro.getPontosBase() + 1);
        this.xpGuerreiro = this.xpGuerreiro.subtract(new BigDecimal("10"));
    }
}
```

### Serviço EtapaTurno

**TreinamentoGuerreirosEtapa.java** — implementa `EtapaTurno`

```java
@Component
public class TreinamentoGuerreirosEtapa implements EtapaTurno {
    
    @Autowired
    private TropaRepository tropaRepository;
    
    @Autowired
    private ConstrucaoRepository construcaoRepository;
    
    @Override
    public int getOrdem() {
        return 7; // Etapa 7 (1-indexed: Produção=1, ..., Treinamento=7, Movimentação=8, ...)
    }
    
    @Override
    public void executar(Vila vila, int numeroTurno) {
        // 1. Buscar todas as tropas aquarteladas da vila
        List<Tropa> tropas = tropaRepository.findByVilaAndEstado(vila, EstadoTropa.AQUARTELADA);
        
        for (Tropa tropa : tropas) {
            // 2. Buscar quartel da tropa
            Construcao quartel = tropa.getQuartel();
            if (quartel == null || quartel.getTipo() != TipoConstrucao.QUARTEL) {
                continue;
            }
            
            // 3. XP por nível do quartel
            BigDecimal xpPorTurno = switch (quartel.getNivel()) {
                case N1 -> new BigDecimal("0.5");
                case N2 -> new BigDecimal("1.0");
                case N3 -> new BigDecimal("1.5");
            };
            
            // 4. Adicionar XP a cada membro
            for (TropaMembro membro : tropa.getMembros()) {
                membro.getCidadao().adicionarXpGuerreiro(xpPorTurno);
            }
        }
    }
}
```

### Validações

- Apenas cidadãos com profissão Guerreiro recebem XP (validado no `adicionarXpGuerreiro`).
- Quartel deve ter nível válido; se mal configurado, não adiciona XP.
- Idempotência: se etapa rodar 2x no mesmo turno (falha + retry), ambas as adições ocorrem (comportamento aceitável, mas não ideal; melhor: verificar `processado` no banco).

### Banco de dados

**Migração Flyway** — parte de V3 ou posterior:

```sql
ALTER TABLE cidadao ADD COLUMN xp_guerreiro DECIMAL(10,2) DEFAULT 0;
```

(Assume-se que `cidadao_profissao.pontos_base` já existe para armazenar PE base por profissão.)

## Frontend

Não se aplica. Visualização de XP fica para tarefa futura.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/quartel/TreinamentoGuerreirosEtapa.java](/src/main/java/com/example/loginbase/jogo/quartel/TreinamentoGuerreirosEtapa.java) (novo)
- [/src/main/resources/db/migration/V<N>__adicionar_xp_guerreiro.sql](/src/main/resources/db/migration/V<N>__adicionar_xp_guerreiro.sql) (novo, se não for mesmo arquivo de V3)

## Testes

- Teste unitário de **adicionarXpGuerreiro**:
  - xp=0, add 0.5 → xp=0.5 (PE não muda)
  - xp=9.5, add 1.0 → xp=10 → xp retorna a 0, PE += 1
  - xp=9.5, add 1.5 → xp=11 → xp retorna a 1, PE += 1

- Teste de integração (TreinamentoGuerreirosEtapa):
  - Quartel N1, 1 tropa com 1 guerreiro aquartelado.
  - Executar etapa → XP guerreiro += 0.5
  - 20 turnos → 20 × 0.5 = 10 XP → PE += 1

- Teste numérico (CA3):
  - Guerreiro com PE 8, XP 7.
  - Turno 1 (N2, +1 XP) → XP 8, PE 8.
  - Turno 2 (+1 XP) → XP 9, PE 8.
  - Turno 3 (+1 XP) → XP 10 → PE 9, XP 0.

- Teste com bônus Militar:
  - Quartel N1 com âncora em ladrilho Militar, bonus_total 40.
  - Média Militar = 40 (único Quartel em ladrilho Militar).
  - Fator = 1 + 40 ÷ 100 = 1,40.
  - XP base N1: 0,5 × 1,40 = 0,70 XP por turno.
  - Guerreiro com XP 9 após turno: 9 + 0,70 = 9,70 XP (PE sem mudança).

- Teste de estado:
  - Tropa EM_VIAGEM_IDA → membros não ganham XP.
  - Tropa AQUARTELADA → membros ganham XP.

## Definição de pronto

- CA1–CA6 de H-002 cobertos.
- EtapaTurno integrada ao pipeline de processamento (ordem 7).
- Build Backend (`./mvnw verify`) sem erros.
- Testes listados passando.
- Coluna `xp_guerreiro` criada via Flyway; dados persistem.

## Fora de escopo

- Bônus de XP por vitória (incluso em evento de batalha).
- Penalidade de XP por derrota.
- UI de visualização de XP.
