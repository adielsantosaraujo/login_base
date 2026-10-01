# H-004 · Tarefa 001 — API de compra e venda

**História:** [h-004-negociar-recursos-no-mercado.md](h-004-negociar-recursos-no-mercado.md) · **Domínio:** [../comercio.md](../comercio.md) ·
**Depende de:** — · **Camada:** Backend

## Objetivo

Implementar os endpoints POST `/api/jogo/mercado/ordens` que processa compra e venda de recursos com cálculo dinâmico de preço baseado no PE do Comerciante, validação de volume e recursos.

## Contexto necessário

- [../comercio.md#números-e-tabelas](../comercio.md#números-e-tabelas) — preços base (tabela 3.1)
  > Madeira 1, Pedra 1, Aço 20, etc.

- [../comercio.md#regras](../comercio.md#regras) — preço venda = base × min(1,0; 0,5 + 0,02 × PE melhor), compra = base × max(1,0; 1,5 − 0,02 × PE).

- Volume máximo = `20 × Σ eficiência Comerciantes × mult. nível`.

## Backend

- **Endpoint**
  - `POST /api/jogo/mercado/ordens`
    - Request: `{ recurso: "MADEIRA", tipo: "VENDA|COMPRA", quantidade: 10 }`
    - Response: `{ sucesso: true, recurso_novo: 10, ouro_novo: 95.6, preco_unitario: 0.74 }` ou erro

- **Serviço**
  - `MercadoService.executarOrdem(vila, recurso, tipo, quantidade)`
    - Validar: Mercado ativo existe na vila
    - Calcular PE melhor Comerciante (max PE de todos alocados ao Mercado ou Estalagem)
    - Se tipo == VENDA:
      - Preço = basePrecio[recurso] × min(1,0; 0,5 + 0,02 × PE)
      - Validar: estoque suficiente
      - Validar: volume cabe no limite do turno
      - Debitar recurso, adicionar Ouro
    - Se tipo == COMPRA:
      - Preço = basePrecio[recurso] × max(1,0; 1,5 − 0,02 × PE)
      - Validar: Ouro suficiente
      - Validar: volume cabe no limite
      - Debitar Ouro, adicionar recurso
    - Gravar evento_turno com detalhes

- **Controller**
  - `MercadoController.executarOrdem()`: valida entrada, chama serviço, retorna resultado

- **Testes**
  - `testVenderMadeiraComPEAlto()`: PE 20 → preço 0,9 → venda lucrativa
  - `testComprarAcoComPEBaixo()`: PE 0 → preço 30 Ouro → compra cara
  - `testVenderSemRecurso()`: sem estoque → erro "Recurso insuficiente"
  - `testComprarSemOuro()`: sem Ouro → erro "Ouro insuficiente"
  - `testVolumeLimitado()`: excede 20 × eficiência → erro "Volume diário excedido"
  - `testSemMercado()`: vila sem Mercado → erro "Mercado não disponível"
  - `testEventoMercado()`: compra/venda registra evento_turno

## Frontend

Não se aplica (Backend only).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/comercio/MercadoService.java](/src/main/java/com/example/loginbase/jogo/comercio/MercadoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/comercio/MercadoController.java](/src/main/java/com/example/loginbase/jogo/comercio/MercadoController.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/catalogo/CatalogoPrecos.java](/src/main/java/com/example/loginbase/jogo/catalogo/CatalogoPrecos.java) (novo)

## Testes

Todos listados acima.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1, CA2, CA3, CA4
- Testes passando
- Build sem erros
- PE do Comerciante melhor consultado corretamente
- Volume não persiste entre turnos (reseta cada turno)

## Fora de escopo

- Histórico de preços
- Diferentes Mercados em regiões diferentes
