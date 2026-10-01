# H-003 · Tarefa 001 — Viagem de tropas no turno

**História:** [h-003-enviar-tropa-em-expedicao.md](h-003-enviar-tropa-em-expedicao.md) · **Domínio:** [../tropas.md](../tropas.md), [../expedicoes.md](../expedicoes.md) · **Depende de:** [h-003-tarefa-002 (opcional: paralelo)](#) | [h-001-tarefa-001-modelo-e-regras-de-tropa.md](h-001-tarefa-001-modelo-e-regras-de-tropa.md) · **Camada:** Backend

## Objetivo

Implementar a lógica de expedição: cálculo de distância até masmorra, validação de comida, transição de estados (AQUARTELADA → EM_VIAGEM_IDA → EM_VIAGEM_VOLTA → AQUARTELADA) e integração com o passo 8 do processamento de turno (seção 2.2).

## Contexto necessário

- [../expedicoes.md](../expedicoes.md) — Turnos de viagem (distância de Manhattan), consumo de comida, saque
  > R2: Turnos = max(1; menor distância de Manhattan). R3: Débito de alimentos = nº membros × (ida+volta).

- [../../v1-010-recursos-e-producao/recursos.md](../../v1-010-recursos-e-producao/recursos.md) — Consumo de alimentos (seção 3.4)
  > Ordem: Refeição → Grãos → Carne; quantidade é inteira no turno.

- [../../v1-014-batalha/batalha.md](../../v1-014-batalha/batalha.md) — Motor de batalha
  > Ao `turnos_restantes = 0` na ida, chamar motor de batalha (seção 10.3).

- [../../v1-001-masmorras/masmorras.md](../../v1-001-masmorras/masmorras.md) — Masmorra e recompensas
  > Vitória remove masmorra; derrota restaura inimigos. Recompensas dadas em 8.4.

## Backend

### Extensão de entidades

**Tropa.java**

```java
@Column(name = "masmorra_id")
private Long masmorraId; // null se aquartelada

@Column(name = "turnos_restantes")
private Integer turnosRestantes; // null se não viajando
```

(Já foram criadas em h-001-tarefa-001.)

### Serviço de expedição

**ExpedicaoService.java**

```java
@Service
public class ExpedicaoService {
    
    @Autowired
    private TropaRepository tropaRepository;
    
    @Autowired
    private EstoqueService estoqueService;
    
    @Autowired
    private RegiaoRepository regiaoRepository;
    
    @Autowired
    private MasmorraRepository masmorraRepository;
    
    /**
     * Enviar tropa em expedição para uma masmorra.
     * Calcula distância, valida comida, debita e muda estado.
     */
    public void enviarExpedicao(Tropa tropa, Long masmorraId) throws ExpedicaoException {
        Masmorra masmorra = masmorraRepository.findById(masmorraId)
            .orElseThrow(() -> new ExpedicaoException("Masmorra não encontrada"));
        
        if (!masmorra.isAtiva()) {
            throw new ExpedicaoException("Masmorra não está ativa");
        }
        
        if (tropa.getEstado() != EstadoTropa.AQUARTELADA) {
            throw new ExpedicaoException("Tropa deve estar aquartelada");
        }
        
        // Calcular turnos de viagem
        int turnosViajem = calcularTurnosViajem(tropa.getVila(), masmorra.getRegiaoIndice());
        
        // Calcular comida necessária
        int nMembros = tropa.getMembros().size();
        BigDecimal alimentosNecessarios = new BigDecimal(nMembros * (turnosViajem * 2))
            .setScale(2, RoundingMode.HALF_UP);
        
        // Validar e debitar comida
        estoqueService.validarAlimentosDisponiveis(tropa.getVila(), alimentosNecessarios);
        estoqueService.debitarAlimentos(tropa.getVila(), alimentosNecessarios);
        
        // Atualizar tropa
        tropa.setEstado(EstadoTropa.EM_VIAGEM_IDA);
        tropa.setMasmorraId(masmorraId);
        tropa.setTurnosRestantes(turnosViajem);
        tropaRepository.save(tropa);
    }
    
    /**
     * Calcular turnos de viagem = max(1; distância de Manhattan mínima)
     */
    private int calcularTurnosViajem(Vila vila, int regiaoMasmorraIndice) {
        List<Regiao> regioesPossuidas = regiaoRepository.findByVilaAndPossuida(vila, true);
        
        int distanciaMinima = Integer.MAX_VALUE;
        for (Regiao regiao : regioesPossuidas) {
            int dist = distanciasManhattan(regiao.getIndice(), regiaoMasmorraIndice);
            distanciaMinima = Math.min(distanciaMinima, dist);
        }
        
        return Math.max(1, distanciaMinima);
    }
    
    private int distanciasManhattan(int indice1, int indice2) {
        int linha1 = (indice1 - 1) / 4;
        int coluna1 = (indice1 - 1) % 4;
        int linha2 = (indice2 - 1) / 4;
        int coluna2 = (indice2 - 1) % 4;
        return Math.abs(linha1 - linha2) + Math.abs(coluna1 - coluna2);
    }
}
```

### Etapa 8 do turno — Movimentação de tropas

**MovimentacaoTropasEtapa.java** — implementa `EtapaTurno`

```java
@Component
public class MovimentacaoTropasEtapa implements EtapaTurno {
    
    @Autowired
    private TropaRepository tropaRepository;
    
    @Autowired
    private MasmorraRepository masmorraRepository;
    
    @Autowired
    private BatalhaService batalhaService;
    
    @Override
    public int getOrdem() {
        return 8; // Etapa 8 (seção 2.2, passo 8)
    }
    
    @Override
    public void executar(Vila vila, int numeroTurno) {
        List<Tropa> tropas = tropaRepository.findByVila(vila);
        
        for (Tropa tropa : tropas) {
            if (tropa.getEstado() == EstadoTropa.EM_VIAGEM_IDA) {
                processarViagem(tropa, numeroTurno);
            } else if (tropa.getEstado() == EstadoTropa.EM_VIAGEM_VOLTA) {
                processarRetorno(tropa);
            }
        }
    }
    
    private void processarViagem(Tropa tropa, int numeroTurno) {
        tropa.setTurnosRestantes(tropa.getTurnosRestantes() - 1);
        
        if (tropa.getTurnosRestantes() == 0) {
            // Chegou ao destino: resolver batalha
            Masmorra masmorra = masmorraRepository.findById(tropa.getMasmorraId())
                .orElse(null);
            
            if (masmorra != null && masmorra.isAtiva()) {
                // Batalha ocorre (implementado em BatalhaService)
                Batalha batalha = batalhaService.resolverBatalha(tropa, masmorra, numeroTurno);
                
                // Transição para retorno
                int turnosRetorno = calcularTurnosRetorno(tropa);
                tropa.setEstado(EstadoTropa.EM_VIAGEM_VOLTA);
                tropa.setTurnosRestantes(turnosRetorno);
            } else {
                // Masmorra já foi eliminada por outra tropa
                // Retornar sem batalha
                int turnosRetorno = calcularTurnosRetorno(tropa);
                tropa.setEstado(EstadoTropa.EM_VIAGEM_VOLTA);
                tropa.setTurnosRestantes(turnosRetorno);
            }
        }
        
        tropaRepository.save(tropa);
    }
    
    private void processarRetorno(Tropa tropa) {
        tropa.setTurnosRestantes(tropa.getTurnosRestantes() - 1);
        
        if (tropa.getTurnosRestantes() == 0) {
            // Retornou ao quartel
            tropa.setEstado(EstadoTropa.AQUARTELADA);
            tropa.setMasmorraId(null);
            // Saque já foi entregue em BatalhaService (ou é nulo se sem batalha)
        }
        
        tropaRepository.save(tropa);
    }
    
    private int calcularTurnosRetorno(Tropa tropa) {
        // Mesmo número que ida (simplificado; pode variar em versão futura)
        Masmorra masmorra = masmorraRepository.findById(tropa.getMasmorraId()).orElse(null);
        if (masmorra == null) return 1;
        
        List<Regiao> regioesPossuidas = regiaoRepository.findByVilaAndPossuida(tropa.getVila(), true);
        int distanciaMinima = Integer.MAX_VALUE;
        for (Regiao regiao : regioesPossuidas) {
            int dist = distanciasManhattan(regiao.getIndice(), masmorra.getRegiaoIndice());
            distanciaMinima = Math.min(distanciaMinima, dist);
        }
        
        return Math.max(1, distanciaMinima);
    }
    
    private int distanciasManhattan(int indice1, int indice2) {
        int linha1 = (indice1 - 1) / 4;
        int coluna1 = (indice1 - 1) % 4;
        int linha2 = (indice2 - 1) / 4;
        int coluna2 = (indice2 - 1) % 4;
        return Math.abs(linha1 - linha2) + Math.abs(coluna1 - coluna2);
    }
}
```

### Endpoint REST

- **POST** `/api/jogo/tropa/{tropaId}/expedicao`
  - Body: `{ "masmorraId": 123 }`
  - Response 201: `{ "id": 1, "estado": "EM_VIAGEM_IDA", "turnosRestantes": 4 }`
  - Response 400: "Comida insuficiente", "Masmorra não está ativa", "Tropa não está aquartelada"

### Banco de dados

(Colunas `masmorra_id` e `turnos_restantes` já criadas na tarefa h-001-tarefa-001.)

## Frontend

Não se aplica. Interface em [h-003-tarefa-002-tela-de-expedicao.md](h-003-tarefa-002-tela-de-expedicao.md).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/quartel/ExpedicaoService.java](/src/main/java/com/example/loginbase/jogo/quartel/ExpedicaoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/quartel/MovimentacaoTropasEtapa.java](/src/main/java/com/example/loginbase/jogo/quartel/MovimentacaoTropasEtapa.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/quartel/TropaController.java](/src/main/java/com/example/loginbase/jogo/quartel/TropaController.java) (atualizar endpoint `/expedicao`)

## Testes

- Teste de cálculo de distância:
  - Região 01 (0,0) → região 16 (3,3) = 3+3 = 6
  - Região 06 (1,1) → região 16 (3,3) = 2+2 = 4

- Teste de consumo de comida:
  - 5 membros, 3 turnos viagem = 5 × (3+3) = 30 alimentos
  - Estoque 25 → erro; estoque 30 → sucesso, débito imediato

- Teste de transição de estado:
  - Enviar expedição AQUARTELADA → EM_VIAGEM_IDA, turnosRestantes = 4
  - 4 turnos passam → turnosRestantes 3, 2, 1, 0
  - turnosRestantes = 0 → EM_VIAGEM_VOLTA, batalha resolvida

- Teste de retorno (CA7):
  - EM_VIAGEM_VOLTA com turnosRestantes = 1
  - Turno processa → turnosRestantes = 0 → AQUARTELADA, saque entregue

- Teste numérico (CA1):
  - Vila regiões 01, 02, 05, 06; masmorra região 16.
  - Distância mínima = 4 (de 06).
  - Expedição: turnosViajem = 4, turnosRestantes = 4 ida.

## Definição de pronto

- CA1–CA8 de H-003 cobertos.
- Endpoints POST `/api/jogo/tropa/{tropaId}/expedicao` funcionando.
- EtapaTurno #8 integrada, processa viagem/retorno.
- Build Backend (`./mvnw verify`) sem erros.
- Testes listados passando.
- Distância, comida, transições persistidas no banco.

## Fora de escopo

- Visualização de expedição em tempo real.
- Cancelamento de expedição.
- Rota alternativa se masmorra desaparece durante viagem.
