# Proposal

## Why

Hoje cada região tem 3 "bônus de região" (faixas 35–50 / 16–34 / 5–15) que são **somados na vila inteira** e multiplicam a produção de todos os prédios ligados, enquanto os 100 ladrilhos têm "jazidas" de uma tabela fixa (25/20/15/10/10/7/5, Campo), sem relação com os bônus. O usuário decidiu trocar esse modelo por um modelo **local**: os 3 bônus viram **percentuais de tipos de terreno** da região, cada ladrilho ganha um terreno (na quantidade exata do percentual), um bônus base e um bônus de adjacência, e o bônus de um prédio passa a ser o do **seu ladrilho-âncora**. Isso dá peso à escolha de onde construir e elimina a soma da vila. O banco será recriado limpo.

## What Changes

- **BREAKING** Os 3 bônus de cada região passam a ser **percentuais de tipos de terreno**; os 13 bônus viram **13 tipos de terreno** (enum `TipoTerreno`, que substitui `BonusRegiao`). Cada região tem os 3 terrenos do seu tipo (Floresta → Floresta, Barreiro, Plantações; Planície → Plantações, Criações, Floresta; Urbana → Indústria, Comércio, Desenvolvimento; Litoral → Salinas, Enxofre, Militar; Montanha → Rocha, Ferro, Carvão).
- **BREAKING** Sorteio novo por região (mantém o sorteio da ordem): b1 inteiro de 20 a 60; b2 inteiro de 20 a (90 − b1); b3 = 100 − b1 − b2 (sempre ≥ 10). A soma é sempre 100%. Substitui as faixas 35–50 / 16–34 / 5–15.
- **BREAKING** Ladrilhos: os 100 ladrilhos de cada região têm só os 3 terrenos da região, na quantidade exata do percentual, em posições aleatórias determinísticas (semente da vila + índice da região), **inclusive na Urbana**. As jazidas (tabela fixa, Campo, garantias mínimas) deixam de existir.
- Bônus do ladrilho: `bonus_base` (0–100, sorteado), `bonus_adjacente` (+25 por vizinho ortogonal do mesmo terreno dentro do 10×10; 0–100) e `bonus_total` = base + adjacente (0–200). Produção = base × (1 + bonus_total ÷ 100).
- **BREAKING** O bônus de um prédio é o `bonus_total` do seu **ladrilho-âncora** (x, y) e só vale se o terreno da âncora for o terreno do prédio; senão é 0.
- **BREAKING** Comércio, Desenvolvimento e Militar passam a valer, na vila inteira, a **média do `bonus_total` das âncoras** dos prédios do grupo (Mercado+Estalagem; Casas; Quartéis) que estão no terreno certo; sem nenhum, 0. Comércio multiplica só o ouro passivo (imposto e Estalagem); **não** altera o preço do Mercado.
- **BREAKING** A marcação de ladrilhos dos prédios de coleta exige ladrilhos do **terreno do prédio** (antes, a jazida).
- O **Quartel** pode ser construído na Urbana **e no Litoral**.
- **BREAKING (API)** A soma de bônus da vila deixa de existir: `bonusRegiao` sai de `GET /api/jogo/vila` e de `GET /api/jogo/vila/mapa`. O campo `bonus` das regiões vira `terrenos` (`{terreno, posicao, percentual}`) na prévia, no resumo, no mapa, no detalhe da região e na anexação. `LadrilhoDTO.jazida` vira `terreno`, `bonusBase`, `bonusAdjacente`, `bonusTotal`. O catálogo de construções troca `bonusRegiao` por `terreno`.
- As 4 casas iniciais ficam nos 4 primeiros ladrilhos **Desenvolvimento** (varredura y = 0..9, x = 0..9) da 1ª região Urbana escolhida; as posições fixas (0,0), (2,0), (4,0), (6,0) deixam de existir.
- Frontend: cada terreno ganha sigla de 2 letras e cor (`--vl-terreno-*`); a grade de ladrilhos mostra o endereço `(A,1)` acima da sigla, com tooltip de endereço, terreno e bônus; criação e mapa mostram a composição da região ("Floresta 40% · Plantações 35% · Barreiro 25%"); o painel de seleção mostra o total de ladrilhos por terreno; o painel "Bônus total da vila" sai.
- **BREAKING (dados)** Migration `V18__terrenos_e_ladrilhos.sql`: apaga `regiao_bonus` e `ladrilho_jazida`, cria `regiao_terreno` e `ladrilho`. Banco limpo, sem conversão.
- Código legado removido: `BonusRegiao`, `FaixaBonusRegiao`, `RegiaoBonus`, `BonusRegiaoService`, `Jazida`, `LadrilhoJazida`, `GeradorJazidaService` e equivalentes no frontend.

## Capabilities

### New Capabilities

- `jogo-terrenos-regiao`: sorteio dos percentuais de terreno, geração e persistência dos ladrilhos (terreno, bonus_base, bonus_adjacente), composição de terrenos nos contratos da API (prévia, resumo, mapa, detalhe, anexação), fim da soma de bônus da vila e casas iniciais em ladrilhos Desenvolvimento.
- `jogo-bonus-terreno`: terreno associado a cada prédio, regiões permitidas (Quartel na Urbana e no Litoral), bônus do prédio pela âncora, efeito na produção, bônus de grupo pela média das âncoras (Comércio, Desenvolvimento, Militar) e marcação de ladrilhos pelo terreno.
- `jogo-ui-terrenos`: siglas e cores dos 13 terrenos, grade de ladrilhos com endereço (A,1) e tooltip, composição de terrenos na criação, no mapa e na anexação, total de ladrilhos por terreno no painel de seleção, marcação e seletor de construção pelo terreno.

### Modified Capabilities

- Nenhuma. As capabilities que este assunto toca (`jogo-bonus-regiao`, `jogo-criacao-vila`) só existem na change `redesenho-criacao-vila-populacao`, ainda não arquivada; não há spec principal em `openspec/specs/` para modificar. Ver design D19.

## Impact

- Backend (`com.example.loginbase.jogo`): `modelo` (TipoTerreno novo no lugar de BonusRegiao, TipoRegiao, RegiaoTerreno e Ladrilho novos; remove FaixaBonusRegiao, RegiaoBonus, Jazida, LadrilhoJazida), `repositorio` (RegiaoTerrenoRepository e LadrilhoRepository novos; remove RegiaoBonusRepository e LadrilhoJazidaRepository), `servico` (GeradorMapaService, GeradorLadrilhoService novo, TerrenoRegiaoService novo, BonusTerrenoService e GrupoBonusVila novos, VilaService, VilaPreviaService, MapaService, AnexacaoService; remove GeradorJazidaService e BonusRegiaoService), `dto` (RegiaoTerrenoDTO novo, RegiaoPreviaDTO, VilaResumoDTO, MapaDTO, RegiaoResumoDTO, RegiaoDetalheDTO, LadrilhoDTO, AnexacaoDTO; remove RegiaoBonusDTO), `construcao` (ConstrucaoCatalogo, CatalogoConstrucaoDTO, ConstrucaoService, MarcacaoService, ObraService), `recurso` (ProducaoService, OuroService), `quartel/TreinamentoQuartelService`.
- Migration: `src/main/resources/db/migration/V18__terrenos_e_ladrilhos.sql` (nova).
- Testes backend: GeradorMapaServiceTest, GeradorLadrilhoServiceTest (novo), TipoRegiaoTest, TipoTerrenoTest (novo), ModeloJogoBaseIntegrationTest, VilaPreviaIntegrationTest, VilaControladorIntegrationTest, MapaControladorIntegrationTest, RegiaoControladorIntegrationTest, CasamentoIntegrationTest, ConstrucaoCatalogoTest, ConstrucaoServiceIntegrationTest, MarcacaoIntegrationTest, BonusTerrenoServiceIntegrationTest (novo), ProducaoServiceIntegrationTest, ProducaoFabricasIntegrationTest, OuroServiceIntegrationTest, MercadoIntegrationTest, ObraServiceIntegrationTest, TreinamentoQuartelIntegrationTest; removidos GeradorJazidaServiceTest e BonusRegiaoServiceIntegrationTest.
- Frontend: `src/domain/terrenos.ts` (novo), `src/domain/regioes.ts`, `src/styles/tokens.css`, `src/components/GradeRegiao.vue`, `src/components/DialogoAnexacao.vue`, `src/components/criacao/*` (ListaTerrenos novo; BonusLista removido), `src/components/jogo/{PainelMarcacao,SeletorConstrucao,MasmorraIndicador}.vue`, `src/composables/{useMapa,useMarcacoes,useVila,useAnexacao,useConstrucoes}.ts`, `src/views/{Mapa,CriacaoVila,BatalhaDetalhe,JogoBatalhas}.vue` e specs correspondentes.
- Docs: `docs/jogo/v1-008-vila-e-mapa/{regioes,vila}.md` e `docs/jogo/v1-010-recursos-e-producao/producao.md` (ajustes de coerência com as regras).
- Banco: exige recriação (sem conversão de dados antigos).