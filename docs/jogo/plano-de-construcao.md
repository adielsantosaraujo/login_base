# Plano de construção — Jogo de Vila v1

Checklist de histórias e tarefas **na ordem de construção**. Marque `[x]` em cada tarefa ao concluí-la e marque a história quando todas as suas tarefas estiverem prontas.

A ordem segue as fases do [roadmap](../roadmap.md) e as dependências declaradas em cada tarefa ("Depende de"), com alguns ajustes por dependências implícitas:

- **Produção, alimentação e ouro passivo** (v1-010 H-002, H-003 e H-004 tarefa 003) ficam na Fase 2, porque precisam de prédios, trabalhadores alocados, cidadãos e Estalagem.
- **Construções H-001** vem antes de **Cidadãos H-003**, porque a imigração usa a Estalagem.
- **Armas H-002** e **Armaduras H-002** ficam na Fase 4, porque dependem do motor de batalha.
- **Pedras de bônus H-001** abre a Fase 5, porque o drop das masmorras (Masmorras H-003) gera pedras.

Legenda: `[B]` backend · `[F]` frontend.

## Fase 1 — Fundação

_Vila, mapa, ciclo de turnos, estoque e mercado._

- [x] **[v1-008 · H-001 — Criar vila escolhendo regiões iniciais](v1-008-vila-e-mapa/historia/h-001-criar-vila-escolhendo-regioes-iniciais.md)**
  - [x] [B] [Tarefa 001 — Modelo de dados da vila e regiões](v1-008-vila-e-mapa/historia/h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md)
  - [x] [B] [Tarefa 002 — Geração de jazidas por semente](v1-008-vila-e-mapa/historia/h-001-tarefa-002-geracao-de-jazidas-por-semente.md)
  - [x] [B] [Tarefa 003 — API de criação da vila](v1-008-vila-e-mapa/historia/h-001-tarefa-003-api-de-criacao-da-vila.md)
  - [x] [F] [Tarefa 004 — Tela de criação da vila](v1-008-vila-e-mapa/historia/h-001-tarefa-004-tela-de-criacao-da-vila.md)

- [x] **[v1-008 · H-002 — Visualizar mapa da vila](v1-008-vila-e-mapa/historia/h-002-visualizar-mapa-da-vila.md)**
  - [x] [B] [Tarefa 001 — API do mapa da vila](v1-008-vila-e-mapa/historia/h-002-tarefa-001-api-do-mapa-da-vila.md)
  - [x] [F] [Tarefa 002 — Telas do mapa e da região](v1-008-vila-e-mapa/historia/h-002-tarefa-002-telas-do-mapa-e-da-regiao.md)

- [x] **[v1-008 · H-003 — Anexar nova região](v1-008-vila-e-mapa/historia/h-003-anexar-nova-regiao.md)**
  - [x] [B] [Tarefa 001 — Regra e API de anexação](v1-008-vila-e-mapa/historia/h-003-tarefa-001-regra-e-api-de-anexacao.md)
  - [x] [F] [Tarefa 002 — Interface de anexação](v1-008-vila-e-mapa/historia/h-003-tarefa-002-interface-de-anexacao.md)

- [x] **[v1-009 · H-001 — Processar turno global](v1-009-turnos/historia/h-001-processar-turno-global.md)**
  - [x] [B] [Tarefa 001 — Agendador do turno global](v1-009-turnos/historia/h-001-tarefa-001-agendador-do-turno-global.md)
  - [x] [B] [Tarefa 002 — Pipeline de resolução por vila](v1-009-turnos/historia/h-001-tarefa-002-pipeline-de-resolucao-por-vila.md)

- [x] **[v1-009 · H-002 — Acompanhar relatório do turno](v1-009-turnos/historia/h-002-acompanhar-relatorio-do-turno.md)**
  - [x] [B] [Tarefa 001 — Registro de eventos do turno](v1-009-turnos/historia/h-002-tarefa-001-registro-de-eventos-do-turno.md)
  - [x] [F] [Tarefa 002 — Barra do turno e relatório](v1-009-turnos/historia/h-002-tarefa-002-barra-do-turno-e-relatorio.md)

- [x] **[v1-010 · H-001 — Consultar estoque de recursos](v1-010-recursos-e-producao/historia/h-001-consultar-estoque-de-recursos.md)**
  - [x] [B] [Tarefa 001 — Modelo de estoque e capacidade](v1-010-recursos-e-producao/historia/h-001-tarefa-001-modelo-de-estoque-e-capacidade.md)
  - [x] [F] [Tarefa 002 — Painel de estoque](v1-010-recursos-e-producao/historia/h-001-tarefa-002-painel-de-estoque.md)

- **[v1-010 · H-004 — Negociar recursos no mercado](v1-010-recursos-e-producao/historia/h-004-negociar-recursos-no-mercado.md)** _(parte 1 — a história fecha na Fase 2)_
  - [x] [B] [Tarefa 001 — API de compra e venda](v1-010-recursos-e-producao/historia/h-004-tarefa-001-api-de-compra-e-venda.md)
  - [x] [F] [Tarefa 002 — Tela do mercado](v1-010-recursos-e-producao/historia/h-004-tarefa-002-tela-do-mercado.md)

## Fase 2 — População e construções

_Famílias, prédios, trabalhadores, produção, alimentação e ouro passivo._

- [x] **[v1-002 · H-001 — Gerar famílias e distribuir pontos iniciais](v1-002-cidadaos/historia/h-001-gerar-familias-e-distribuir-pontos-iniciais.md)**
  - [x] [B] [Tarefa 001 — Modelo de dados de cidadãos e famílias](v1-002-cidadaos/historia/h-001-tarefa-001-modelo-de-dados-de-cidadaos-e-familias.md)
  - [x] [B] [Tarefa 002 — Geração das famílias iniciais](v1-002-cidadaos/historia/h-001-tarefa-002-geracao-das-familias-iniciais.md)
  - [x] [F] [Tarefa 003 — Tela de distribuição de pontos e família líder](v1-002-cidadaos/historia/h-001-tarefa-003-tela-de-distribuicao-de-pontos-e-familia-lider.md)

- [x] **[v1-003 · H-001 — Construir prédio nível 1](v1-003-construcoes/historia/h-001-construir-predio-nivel-1.md)**
  - [x] [B] [Tarefa 001 — Catálogo de construções](v1-003-construcoes/historia/h-001-tarefa-001-catalogo-de-construcoes.md)
  - [x] [B] [Tarefa 002 — API de construção e posicionamento](v1-003-construcoes/historia/h-001-tarefa-002-api-de-construcao-e-posicionamento.md)
  - [x] [B] [Tarefa 003 — Progresso de obra no turno](v1-003-construcoes/historia/h-001-tarefa-003-progresso-de-obra-no-turno.md)
  - [x] [F] [Tarefa 004 — Tela de construção na região](v1-003-construcoes/historia/h-001-tarefa-004-tela-de-construcao-na-regiao.md)

- [x] **[v1-003 · H-002 — Melhorar prédio de nível](v1-003-construcoes/historia/h-002-melhorar-predio-de-nivel.md)**
  - [x] [B] [Tarefa 001 — Regra e API de upgrade](v1-003-construcoes/historia/h-002-tarefa-001-regra-e-api-de-upgrade.md)
  - [x] [F] [Tarefa 002 — Interface de upgrade](v1-003-construcoes/historia/h-002-tarefa-002-interface-de-upgrade.md)

- [x] **[v1-003 · H-003 — Marcar ladrilhos de coleta](v1-003-construcoes/historia/h-003-marcar-ladrilhos-de-coleta.md)**
  - [x] [B] [Tarefa 001 — API de marcação de ladrilhos](v1-003-construcoes/historia/h-003-tarefa-001-api-de-marcacao-de-ladrilhos.md)
  - [x] [F] [Tarefa 002 — Interface de marcação](v1-003-construcoes/historia/h-003-tarefa-002-interface-de-marcacao.md)

- [x] **[v1-002 · H-002 — Casar cidadãos](v1-002-cidadaos/historia/h-002-casar-cidadaos.md)**
  - [x] [B] [Tarefa 001 — Regra e API de casamento](v1-002-cidadaos/historia/h-002-tarefa-001-regra-e-api-de-casamento.md)
  - [x] [F] [Tarefa 002 — Tela de famílias e casamento](v1-002-cidadaos/historia/h-002-tarefa-002-tela-de-familias-e-casamento.md)

- [x] **[v1-002 · H-003 — Nascimento e crescimento](v1-002-cidadaos/historia/h-003-nascimento-e-crescimento.md)**
  - [x] [B] [Tarefa 001 — Reprodução e nascimento no turno](v1-002-cidadaos/historia/h-003-tarefa-001-reproducao-e-nascimento-no-turno.md)
  - [x] [B] [Tarefa 002 — Envelhecimento, crescimento e morte](v1-002-cidadaos/historia/h-003-tarefa-002-envelhecimento-crescimento-e-morte.md)
  - [x] [B] [Tarefa 003 — Imigração pela Estalagem](v1-002-cidadaos/historia/h-003-tarefa-003-imigracao-pela-estalagem.md)

- [x] **[v1-002 · H-004 — Consultar painel do cidadão](v1-002-cidadaos/historia/h-004-consultar-painel-do-cidadao.md)**
  - [x] [B] [Tarefa 001 — API do cidadão e distribuição de pontos](v1-002-cidadaos/historia/h-004-tarefa-001-api-do-cidadao-e-distribuicao-de-pontos.md)
  - [x] [F] [Tarefa 002 — Tela do painel do cidadão](v1-002-cidadaos/historia/h-004-tarefa-002-tela-do-painel-do-cidadao.md)

- [x] **[v1-003 · H-004 — Alocar trabalhadores nos prédios](v1-003-construcoes/historia/h-004-alocar-trabalhadores-nos-predios.md)**
  - [x] [B] [Tarefa 001 — API de alocação](v1-003-construcoes/historia/h-004-tarefa-001-api-de-alocacao.md)
  - [x] [F] [Tarefa 002 — Painel do prédio](v1-003-construcoes/historia/h-004-tarefa-002-painel-do-predio.md)

- [x] **[v1-010 · H-002 — Produzir recursos nos prédios](v1-010-recursos-e-producao/historia/h-002-produzir-recursos-nos-predios.md)**
  - [x] [B] [Tarefa 001 — Cálculo de eficiência do trabalhador](v1-010-recursos-e-producao/historia/h-002-tarefa-001-calculo-de-eficiencia-do-trabalhador.md)
  - [x] [B] [Tarefa 002 — Produção de coleta e rural](v1-010-recursos-e-producao/historia/h-002-tarefa-002-producao-de-coleta-e-rural.md)
  - [x] [B] [Tarefa 003 — Produção das fábricas](v1-010-recursos-e-producao/historia/h-002-tarefa-003-producao-das-fabricas.md)

- [x] **[v1-010 · H-003 — Alimentar a população](v1-010-recursos-e-producao/historia/h-003-alimentar-a-populacao.md)**
  - [x] [B] [Tarefa 001 — Consumo de comida e fome](v1-010-recursos-e-producao/historia/h-003-tarefa-001-consumo-de-comida-e-fome.md)

- [x] **[v1-010 · H-004 — Negociar recursos no mercado (continuação)](v1-010-recursos-e-producao/historia/h-004-negociar-recursos-no-mercado.md)**
  - [x] [B] [Tarefa 003 — Ouro passivo, imposto e estalagem](v1-010-recursos-e-producao/historia/h-004-tarefa-003-ouro-passivo-imposto-e-estalagem.md)

## Fase 3 — Itens e ofícios

_Fabricação, catálogos de equipamentos, equipar e inventário._

- [x] **[v1-011 · H-001 — Fabricar item na oficina](v1-011-itens-e-fabricacao/historia/h-001-fabricar-item-na-oficina.md)**
  - [x] [B] [Tarefa 001 — Modelo de dados de itens](v1-011-itens-e-fabricacao/historia/h-001-tarefa-001-modelo-de-dados-de-itens.md)
  - [x] [B] [Tarefa 002 — Gerador de itens, qualidade e bônus](v1-011-itens-e-fabricacao/historia/h-001-tarefa-002-gerador-de-itens-qualidade-e-bonus.md)
  - [x] [B] [Tarefa 003 — Fila de fabricação no turno](v1-011-itens-e-fabricacao/historia/h-001-tarefa-003-fila-de-fabricacao-no-turno.md)
  - [x] [F] [Tarefa 004 — Tela da oficina](v1-011-itens-e-fabricacao/historia/h-001-tarefa-004-tela-da-oficina.md)

- [x] **[v1-005 · H-001 — Fabricar ferramentas](v1-005-ferramentas/historia/h-001-fabricar-ferramentas.md)**
  - [x] [B] [Tarefa 001 — Catálogo de ferramentas](v1-005-ferramentas/historia/h-001-tarefa-001-catalogo-de-ferramentas.md)

- [x] **[v1-005 · H-002 — Aplicar bônus da ferramenta no trabalho](v1-005-ferramentas/historia/h-002-aplicar-bonus-da-ferramenta-no-trabalho.md)**
  - [x] [B] [Tarefa 001 — Bônus de ferramenta na eficiência](v1-005-ferramentas/historia/h-002-tarefa-001-bonus-de-ferramenta-na-eficiencia.md)

- [x] **[v1-004 · H-001 — Fabricar Armas](v1-004-armas/historia/h-001-fabricar-armas.md)**
  - [x] [B] [Tarefa 001 — Catálogo de Armas](v1-004-armas/historia/h-001-tarefa-001-catalogo-de-armas.md)

- [x] **[v1-006 · H-001 — Fabricar armaduras](v1-006-Armaduras/historia/h-001-fabricar-armaduras.md)**
  - [x] [B] [Tarefa 001 — Catálogo de armaduras](v1-006-Armaduras/historia/h-001-tarefa-001-catalogo-de-armaduras.md)

- [x] **[v1-007 · H-001 — Fabricar joias](v1-007-Joias/historia/h-001-fabricar-joias.md)**
  - [x] [B] [Tarefa 001 — Catálogo de joias](v1-007-Joias/historia/h-001-tarefa-001-catalogo-de-joias.md)

- [x] **[v1-011 · H-002 — Equipar itens no painel da pessoa](v1-011-itens-e-fabricacao/historia/h-002-equipar-itens-no-painel-da-pessoa.md)**
  - [x] [B] [Tarefa 001 — Regras e API de equipar](v1-011-itens-e-fabricacao/historia/h-002-tarefa-001-regras-e-api-de-equipar.md)
  - [x] [F] [Tarefa 002 — Interface de equipamento](v1-011-itens-e-fabricacao/historia/h-002-tarefa-002-interface-de-equipamento.md)

- [x] **[v1-007 · H-002 — Usar colar e anéis](v1-007-Joias/historia/h-002-usar-colar-e-aneis.md)**
  - [x] [B] [Tarefa 001 — Slots de joias e efeitos](v1-007-Joias/historia/h-002-tarefa-001-slots-de-joias-e-efeitos.md)

- [x] **[v1-011 · H-003 — Gerenciar inventário e aprimorar itens](v1-011-itens-e-fabricacao/historia/h-003-gerenciar-inventario-e-aprimorar-itens.md)**
  - [x] [B] [Tarefa 001 — API de inventário e aprimoramento](v1-011-itens-e-fabricacao/historia/h-003-tarefa-001-api-de-inventario-e-aprimoramento.md)
  - [x] [F] [Tarefa 002 — Tela de inventário](v1-011-itens-e-fabricacao/historia/h-003-tarefa-002-tela-de-inventario.md)

## Fase 4 — Militar

_Tropas, expedições e motor de batalha._

- [ ] **[v1-013 · H-001 — Formar tropa no quartel](v1-013-quartel-e-tropas/historia/h-001-formar-tropa-no-quartel.md)**
  - [ ] [B] [Tarefa 001 — Modelo e regras de tropa](v1-013-quartel-e-tropas/historia/h-001-tarefa-001-modelo-e-regras-de-tropa.md)
  - [ ] [F] [Tarefa 002 — Tela do quartel](v1-013-quartel-e-tropas/historia/h-001-tarefa-002-tela-do-quartel.md)

- [ ] **[v1-013 · H-002 — Treinar guerreiros no quartel](v1-013-quartel-e-tropas/historia/h-002-treinar-guerreiros-no-quartel.md)**
  - [ ] [B] [Tarefa 001 — XP de treinamento no turno](v1-013-quartel-e-tropas/historia/h-002-tarefa-001-xp-de-treinamento-no-turno.md)

- [ ] **[v1-013 · H-003 — Enviar tropa em expedição](v1-013-quartel-e-tropas/historia/h-003-enviar-tropa-em-expedicao.md)**
  - [ ] [B] [Tarefa 001 — Viagem de tropas no turno](v1-013-quartel-e-tropas/historia/h-003-tarefa-001-viagem-de-tropas-no-turno.md)
  - [ ] [F] [Tarefa 002 — Tela de expedição](v1-013-quartel-e-tropas/historia/h-003-tarefa-002-tela-de-expedicao.md)

- [ ] **[v1-014 · H-001 — Resolver batalha por rodadas](v1-014-batalha/historia/h-001-resolver-batalha-por-rodadas.md)**
  - [ ] [B] [Tarefa 001 — Cálculo de atributos de combate](v1-014-batalha/historia/h-001-tarefa-001-calculo-de-atributos-de-combate.md)
  - [ ] [B] [Tarefa 002 — Motor de batalha](v1-014-batalha/historia/h-001-tarefa-002-motor-de-batalha.md)
  - [ ] [B] [Tarefa 003 — Consequências para abatidos](v1-014-batalha/historia/h-001-tarefa-003-consequencias-para-abatidos.md)

- [ ] **[v1-004 · H-002 — Usar Arma em Combate](v1-004-armas/historia/h-002-usar-arma-em-combate.md)**
  - [ ] [B] [Tarefa 001 — Regras de Alcance e Modificadores de Arma](v1-004-armas/historia/h-002-tarefa-001-regras-de-alcance-e-modificadores-de-arma.md)

- [ ] **[v1-006 · H-002 — Defesa das armaduras na batalha](v1-006-Armaduras/historia/h-002-defesa-das-armaduras-na-batalha.md)**
  - [ ] [B] [Tarefa 001 — Cálculo de defesa com armaduras](v1-006-Armaduras/historia/h-002-tarefa-001-calculo-de-defesa-com-armaduras.md)

- [ ] **[v1-014 · H-002 — Ver relatório de batalha](v1-014-batalha/historia/h-002-ver-relatorio-de-batalha.md)**
  - [ ] [B] [Tarefa 001 — API de relatório de batalha](v1-014-batalha/historia/h-002-tarefa-001-api-de-relatorio-de-batalha.md)
  - [ ] [F] [Tarefa 002 — Tela de replay da batalha](v1-014-batalha/historia/h-002-tarefa-002-tela-de-replay-da-batalha.md)

## Fase 5 — Masmorras

_Masmorras, recompensas e pedras de bônus._

- [ ] **[v1-012 · H-001 — Obter pedras nas masmorras](v1-012-pedras-de-bonus/historia/h-001-obter-pedras-nas-masmorras.md)**
  - [ ] [B] [Tarefa 001 — Modelo e gerador de pedras](v1-012-pedras-de-bonus/historia/h-001-tarefa-001-modelo-e-gerador-de-pedras.md)

- [ ] **[v1-001 · H-001 — Surgimento e evolução de masmorras](v1-001-masmorras/historia/h-001-surgimento-e-evolucao-de-masmorras.md)**
  - [ ] [B] [Tarefa 001 — Modelo de dados de masmorras](v1-001-masmorras/historia/h-001-tarefa-001-modelo-de-dados-de-masmorras.md)
  - [ ] [B] [Tarefa 002 — Surgimento e evolução no turno](v1-001-masmorras/historia/h-001-tarefa-002-surgimento-e-evolucao-no-turno.md)
  - [ ] [F] [Tarefa 003 — Masmorras no mapa](v1-001-masmorras/historia/h-001-tarefa-003-masmorras-no-mapa.md)

- [ ] **[v1-001 · H-002 — Atacar masmorra](v1-001-masmorras/historia/h-002-atacar-masmorra.md)**
  - [ ] [B] [Tarefa 001 — Geração dos inimigos por nível](v1-001-masmorras/historia/h-002-tarefa-001-geracao-dos-inimigos-por-nivel.md)
  - [ ] [B] [Tarefa 002 — Integração expedição e batalha](v1-001-masmorras/historia/h-002-tarefa-002-integracao-expedicao-e-batalha.md)

- [ ] **[v1-001 · H-003 — Receber recompensas da masmorra](v1-001-masmorras/historia/h-003-receber-recompensas-da-masmorra.md)**
  - [ ] [B] [Tarefa 001 — Tabela de recompensas e drop](v1-001-masmorras/historia/h-003-tarefa-001-tabela-de-recompensas-e-drop.md)

- [ ] **[v1-012 · H-002 — Engastar pedra em item](v1-012-pedras-de-bonus/historia/h-002-engastar-pedra-em-item.md)**
  - [ ] [B] [Tarefa 001 — API de engaste](v1-012-pedras-de-bonus/historia/h-002-tarefa-001-api-de-engaste.md)
  - [ ] [F] [Tarefa 002 — Interface de engaste](v1-012-pedras-de-bonus/historia/h-002-tarefa-002-interface-de-engaste.md)

---

**Total:** 38 histórias · 76 tarefas.
