package com.example.loginbase.jogo.api;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import com.example.loginbase.jogo.api.CatalogoDto.CultivoCatalogoDto;
import com.example.loginbase.jogo.api.CatalogoDto.InimigoCatalogoDto;
import com.example.loginbase.jogo.api.CatalogoDto.MapaMasmorraDto;
import com.example.loginbase.jogo.api.CatalogoDto.MasmorraCatalogoDto;
import com.example.loginbase.jogo.api.CatalogoDto.ModeloItemCatalogoDto;
import com.example.loginbase.jogo.api.CatalogoDto.PosicaoDto;
import com.example.loginbase.jogo.api.CatalogoDto.TropaCatalogoDto;
import com.example.loginbase.jogo.api.VilaDto.CanteiroDto;
import com.example.loginbase.jogo.api.VilaDto.CustoDto;
import com.example.loginbase.jogo.api.VilaDto.ItemDto;
import com.example.loginbase.jogo.api.VilaDto.OrdemDto;
import com.example.loginbase.jogo.api.VilaDto.OrdemEmAndamentoDto;
import com.example.loginbase.jogo.api.VilaDto.PredioDto;
import com.example.loginbase.jogo.api.VilaDto.ProximoNivelDto;
import com.example.loginbase.jogo.api.VilaDto.UnidadeDto;
import com.example.loginbase.jogo.catalogo.CatalogoMasmorras;
import com.example.loginbase.jogo.catalogo.CategoriaItem;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.MapaMasmorra;
import com.example.loginbase.jogo.catalogo.MapaMasmorra.Posicao;
import com.example.loginbase.jogo.catalogo.Masmorra;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.ModeloItem.AtributosItem;
import com.example.loginbase.jogo.catalogo.SlotEquipamento;
import com.example.loginbase.jogo.catalogo.TipoInimigo;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoRecurso;
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.Batalha;
import com.example.loginbase.jogo.dominio.BatalhaRepository;
import com.example.loginbase.jogo.dominio.Canteiro;
import com.example.loginbase.jogo.dominio.CategoriaOrdem;
import com.example.loginbase.jogo.dominio.EstoqueSemente;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.StatusBatalha;
import com.example.loginbase.jogo.dominio.Unidade;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.economia.CalculadoraProducao;
import com.example.loginbase.jogo.economia.EstadoVila;
import com.example.loginbase.jogo.masmorra.Loot;
import com.example.loginbase.jogo.masmorra.combate.Combatente;
import com.example.loginbase.jogo.masmorra.combate.EstadoBatalha;
import com.example.loginbase.jogo.masmorra.combate.Lado;

/**
 * Converte o modelo interno do jogo ({@link EstadoVila}, entidades de
 * domínio, catálogo em código) para os DTOs JSON da API ({@link VilaDto},
 * {@link CatalogoDto}).
 */
@Component
public class JogoMapper {

	private static final long MILESIMOS_POR_UNIDADE = 1000L;

	private final JogoProperties jogoProperties;
	private final BatalhaRepository batalhaRepository;

	/**
	 * {@code Batalha.estado}/{@code Batalha.loot} são {@code String} JSON (ver
	 * nota em {@code JsonConverter}): este mapper desserializa com uma
	 * instância própria de {@code ObjectMapper}, no mesmo estilo usado por
	 * {@code MasmorraService} para serializar.
	 */
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JogoMapper(JogoProperties jogoProperties, BatalhaRepository batalhaRepository) {
		this.jogoProperties = jogoProperties;
		this.batalhaRepository = batalhaRepository;
	}

	// -------------------------------------------------------------- Vila --

	public VilaDto toVilaDto(EstadoVila estado, Clock clock) {
		Vila vila = estado.vila();
		Instant agora = clock.instant();
		int velocidade = jogoProperties.getVelocidade();

		Map<TipoPredio, Integer> niveisPredio = estado.predios().stream()
				.collect(Collectors.toMap(Predio::getTipo, Predio::getNivel));
		List<Cultivo> cultivosCanteiros = estado.canteiros().stream().map(Canteiro::getCultivo).toList();

		CalculadoraProducao calculadora = new CalculadoraProducao(velocidade, jogoProperties.curvaNiveis());
		Map<TipoRecurso, Long> taxaBasePorRecurso = calculadora.taxaHoraPorRecurso(niveisPredio, cultivosCanteiros);
		Map<TipoRecurso, Long> producaoPorHora = new EnumMap<>(TipoRecurso.class);
		for (TipoRecurso recurso : TipoRecurso.values()) {
			producaoPorHora.put(recurso, taxaBasePorRecurso.getOrDefault(recurso, 0L) * velocidade);
		}

		int nivelArmazem = niveisPredio.getOrDefault(TipoPredio.ARMAZEM, 1);
		long capacidadeUnidade = TipoRecurso.capacidadeArmazem(Math.max(1, nivelArmazem), jogoProperties.curvaNiveis());
		Map<TipoRecurso, Long> capacidade = new EnumMap<>(TipoRecurso.class);
		for (TipoRecurso recurso : TipoRecurso.values()) {
			capacidade.put(recurso, capacidadeUnidade);
		}

		Map<TipoRecurso, Long> recursos = new EnumMap<>(TipoRecurso.class);
		recursos.put(TipoRecurso.COMIDA, vila.getComida() / MILESIMOS_POR_UNIDADE);
		recursos.put(TipoRecurso.MADEIRA, vila.getMadeira() / MILESIMOS_POR_UNIDADE);
		recursos.put(TipoRecurso.PEDRA, vila.getPedra() / MILESIMOS_POR_UNIDADE);
		recursos.put(TipoRecurso.FERRO, vila.getFerro() / MILESIMOS_POR_UNIDADE);

		List<PredioDto> predios = estado.predios().stream()
				.map(predio -> toPredioDto(predio, estado.ordens(), agora, velocidade))
				.toList();

		List<CanteiroDto> canteiros = estado.canteiros().stream()
				.map(canteiro -> new CanteiroDto(canteiro.getPosicao(), canteiro.getCultivo(),
						canteiro.getCultivo().producaoComidaPorHora() * velocidade))
				.toList();

		Map<Cultivo, Integer> sementes = estado.sementes().stream()
				.collect(Collectors.toMap(EstoqueSemente::getCultivo, EstoqueSemente::getQuantidade));

		List<ItemDto> itens = estado.itens().stream().map(this::toItemDto).toList();

		Map<Long, Item> itensPorId = estado.itens().stream()
				.collect(Collectors.toMap(Item::getId, item -> item));
		List<UnidadeDto> unidades = estado.unidades().stream()
				.map(unidade -> toUnidadeDto(unidade, itensPorId))
				.toList();

		int nivelQuartel = niveisPredio.getOrDefault(TipoPredio.QUARTEL, 0);
		int capacidadeExercito = nivelQuartel <= 0 ? 0 : TipoPredio.QUARTEL.capacidadeExercito(nivelQuartel);

		int nivelForja = niveisPredio.getOrDefault(TipoPredio.FORJA, 0);
		int nivelMaximoForjavel = nivelForja <= 0 ? 0 : TipoPredio.FORJA.nivelMaximoForjavel(nivelForja);

		List<OrdemDto> ordens = estado.ordens().stream()
				.map(ordem -> new OrdemDto(ordem.getId(), ordem.getCategoria(), ordem.getAlvo(), ordem.getNivel(),
						ordem.getQuantidade(), ordem.getIniciadaEm(), ordem.getConcluiEm()))
				.toList();

		Long batalhaAtivaId = batalhaRepository.findByVilaId(vila.getId()).stream()
				.filter(batalha -> batalha.getStatus() == StatusBatalha.EM_ANDAMENTO)
				.map(Batalha::getId)
				.findFirst()
				.orElse(null);

		return new VilaDto(agora, vila.getNome(), recursos, capacidade, producaoPorHora,
				vila.getMasmorraNivelLiberado(), batalhaAtivaId, predios, canteiros, sementes, itens, unidades,
				capacidadeExercito, nivelMaximoForjavel, ordens);
	}

	private PredioDto toPredioDto(Predio predio, List<Ordem> ordens, Instant agora, int velocidade) {
		TipoPredio tipo = predio.getTipo();
		int nivel = predio.getNivel();

		ProximoNivelDto proximoNivel = null;
		if (nivel < TipoPredio.NIVEL_MAXIMO) {
			int proximoNivelNumero = nivel + 1;
			proximoNivel = new ProximoNivelDto(proximoNivelNumero, toCustoDto(tipo.custo(proximoNivelNumero, jogoProperties.curvaNiveis())),
					aplicarVelocidade(tipo.tempoSegundos(proximoNivelNumero), velocidade));
		}

		OrdemEmAndamentoDto ordemEmAndamento = ordens.stream()
				.filter(ordem -> ordem.getCategoria() == CategoriaOrdem.CONSTRUCAO && tipo.name().equals(ordem.getAlvo()))
				.findFirst()
				.map(ordem -> new OrdemEmAndamentoDto(ordem.getConcluiEm(),
						Math.max(0, Duration.between(agora, ordem.getConcluiEm()).getSeconds())))
				.orElse(null);

		return new PredioDto(tipo, nivel, TipoPredio.NIVEL_MAXIMO, proximoNivel, ordemEmAndamento);
	}

	private ItemDto toItemDto(Item item) {
		AtributosItem atributos = item.getModelo().atributos(item.getNivel());
		Integer alcance = item.getModelo().categoria() == CategoriaItem.ARMA ? atributos.alcance() : null;
		return new ItemDto(item.getId(), item.getModelo(), item.getNivel(), item.getOrigem(), item.getStatus(),
				atributos.ataque(), atributos.defesa(), alcance);
	}

	private UnidadeDto toUnidadeDto(Unidade unidade, Map<Long, Item> itensPorId) {
		TipoTropa tipo = unidade.getTipo();
		Item arma = itensPorId.get(unidade.getArmaItemId());
		Item armadura = itensPorId.get(unidade.getArmaduraItemId());
		AtributosItem atributosArma = arma.getModelo().atributos(arma.getNivel());
		AtributosItem atributosArmadura = armadura.getModelo().atributos(armadura.getNivel());

		var equipamento = new LinkedHashMap<SlotEquipamento, ItemDto>();
		for (SlotEquipamento slot : SlotEquipamento.values()) {
			ItemDto item = null;
			if (slot == SlotEquipamento.ARMA && unidade.getArmaItemId() != null) {
				item = toItemDto(itensPorId.get(unidade.getArmaItemId()));
			} else if (slot == SlotEquipamento.ARMADURA && unidade.getArmaduraItemId() != null) {
				item = toItemDto(itensPorId.get(unidade.getArmaduraItemId()));
			}
			equipamento.put(slot, item);
		}

		return new UnidadeDto(unidade.getId(), unidade.getNome(), unidade.getSobrenome(), unidade.getOrdinalNome(),
				unidade.nomeExibicao(), tipo, unidade.getStatus(), tipo.hp(), atributosArma.ataque(),
				tipo.defesaBase() + atributosArmadura.defesa(), atributosArma.alcance(), tipo.movimento(),
				equipamento);
	}

	private List<CustoDto> toCustoDto(com.example.loginbase.jogo.catalogo.Custo custo) {
		List<CustoDto> resultado = new ArrayList<>();
		for (TipoRecurso recurso : TipoRecurso.values()) {
			long quantidade = custo.quantidade(recurso);
			if (quantidade > 0) {
				resultado.add(new CustoDto(recurso, quantidade));
			}
		}
		return resultado;
	}

	private long aplicarVelocidade(long tempoBaseSegundos, int velocidade) {
		return (tempoBaseSegundos + velocidade - 1) / velocidade;
	}

	// -------------------------------------------------------------- Batalha --

	/** Desserializa {@code Batalha.estado} (JSON) e monta o {@link BatalhaDto}. */
	public BatalhaDto toBatalhaDto(Batalha batalha) {
		EstadoBatalha estado = desserializarEstado(batalha.getEstado());
		return toBatalhaDto(estado, batalha);
	}

	/** Versão que aceita um {@link EstadoBatalha} já desserializado (evita reserializar em testes). */
	public BatalhaDto toBatalhaDto(EstadoBatalha estado, Batalha batalha) {
		MapaMasmorra mapa = estado.mapa();

		List<BatalhaDto.ObstaculoDto> obstaculos = mapa.obstaculos().stream()
				.map(posicao -> new BatalhaDto.ObstaculoDto(posicao.x(), posicao.y()))
				.toList();

		List<BatalhaDto.CombatenteDto> combatentes = estado.combatentes().stream()
				.map(this::toCombatenteDto)
				.toList();

		List<String> log = Arrays.stream(batalha.getLog().split("\n"))
				.filter(linha -> !linha.isBlank())
				.toList();

		LootDto loot = batalha.getStatus() == StatusBatalha.VITORIA ? toLootDto(batalha.getLoot()) : null;

		return new BatalhaDto(batalha.getId(), batalha.getMasmorraNivel(), batalha.getStatus(), estado.turno(),
				estado.turnoMaximo(), mapa.largura(), mapa.altura(), obstaculos, combatentes, log, loot);
	}

	/**
	 * Combatentes do jogador guardam {@code "TIPO:idDaUnidade"} em
	 * {@code tipoOuUnidade()} (ver {@code MasmorraService}); aqui expomos o
	 * tipo "limpo" (sem o id) e o id da unidade separadamente. Inimigos não
	 * têm esse prefixo — {@code tipoOuUnidade()} já é só o nome do
	 * {@code TipoInimigo}.
	 */
	private BatalhaDto.CombatenteDto toCombatenteDto(Combatente combatente) {
		String tipo;
		Long unidadeId;
		if (combatente.lado() == Lado.JOGADOR) {
			int separador = combatente.tipoOuUnidade().lastIndexOf(':');
			tipo = combatente.tipoOuUnidade().substring(0, separador);
			unidadeId = Long.valueOf(combatente.tipoOuUnidade().substring(separador + 1));
		} else {
			tipo = combatente.tipoOuUnidade();
			unidadeId = null;
		}
		return new BatalhaDto.CombatenteDto(combatente.id(), combatente.lado(), unidadeId, tipo,
				combatente.posicao().x(), combatente.posicao().y(), combatente.hp(), combatente.hpMax(),
				combatente.ataque(), combatente.defesa(), combatente.alcance(), combatente.movimento(),
				combatente.defendendo(), combatente.moveu(), combatente.agiu(), combatente.vivo());
	}

	/** Recursos do {@link Loot} vêm em milésimos (mesma convenção de {@code Vila}); o DTO expõe inteiros. */
	private LootDto toLootDto(String lootJson) {
		Loot loot = desserializarLoot(lootJson);
		Map<TipoRecurso, Long> recursos = new EnumMap<>(TipoRecurso.class);
		loot.recursos().forEach((recurso, milesimos) -> recursos.put(recurso, milesimos / MILESIMOS_POR_UNIDADE));
		List<com.example.loginbase.jogo.api.ItemDto> itens = loot.itens().stream()
				.map(this::toLootItemDto)
				.toList();
		return new LootDto(recursos, loot.sementes(), itens);
	}

	private com.example.loginbase.jogo.api.ItemDto toLootItemDto(Item item) {
		AtributosItem atributos = item.getModelo().atributos(item.getNivel());
		return new com.example.loginbase.jogo.api.ItemDto(item.getId(), item.getModelo(), item.getModelo().categoria(),
				item.getNivel(), atributos.ataque(), atributos.defesa(), atributos.alcance(), item.getOrigem(),
				item.getStatus());
	}

	private EstadoBatalha desserializarEstado(String json) {
		try {
			return objectMapper.readValue(json, EstadoBatalha.class);
		} catch (JacksonException e) {
			throw new IllegalStateException("Falha ao desserializar EstadoBatalha de JSON", e);
		}
	}

	private Loot desserializarLoot(String json) {
		try {
			return objectMapper.readValue(json, Loot.class);
		} catch (JacksonException e) {
			throw new IllegalStateException("Falha ao desserializar Loot de JSON", e);
		}
	}

	// ---------------------------------------------------------- Catálogo --

	public CatalogoDto toCatalogoDto(JogoProperties jogoProperties) {
		int velocidade = jogoProperties.getVelocidade();

		Map<TipoPredio, List<ProximoNivelDto>> predios = new EnumMap<>(TipoPredio.class);
		for (TipoPredio tipo : TipoPredio.values()) {
			List<ProximoNivelDto> niveis = new ArrayList<>();
			for (int nivel = 1; nivel <= TipoPredio.NIVEL_MAXIMO; nivel++) {
				niveis.add(new ProximoNivelDto(nivel, toCustoDto(tipo.custo(nivel, jogoProperties.curvaNiveis())),
						aplicarVelocidade(tipo.tempoSegundos(nivel), velocidade)));
			}
			predios.put(tipo, niveis);
		}

		Map<Cultivo, CultivoCatalogoDto> cultivos = new EnumMap<>(Cultivo.class);
		for (Cultivo cultivo : Cultivo.values()) {
			cultivos.put(cultivo, new CultivoCatalogoDto(cultivo.producaoComidaPorHora(), cultivo.exigeSemente(),
					cultivo.nivelMasmorraParaSemente()));
		}

		Map<ModeloItem, ModeloItemCatalogoDto> modelosItem = new EnumMap<>(ModeloItem.class);
		for (ModeloItem modelo : ModeloItem.values()) {
			// custoTotal(1, 1) == custo-base (nível 1, quantidade 1): equivalente ao
			// custo por unidade no nível L (custoBase × L) com L = 1.
			modelosItem.put(modelo, new ModeloItemCatalogoDto(modelo.categoria(), toCustoDto(modelo.custoTotal(1, 1)),
					modelo.tempoTotalSegundos(1, 1, 1)));
		}

		Map<TipoTropa, TropaCatalogoDto> tropas = new EnumMap<>(TipoTropa.class);
		for (TipoTropa tipo : TipoTropa.values()) {
			tropas.put(tipo, new TropaCatalogoDto(tipo.armaExigida(), tipo.hp(), tipo.defesaBase(), tipo.movimento(),
					tipo.comida(), tipo.tempoTreinoSegundos(), tipo.nivelMinimoQuartel()));
		}

		Map<TipoInimigo, InimigoCatalogoDto> inimigos = new EnumMap<>(TipoInimigo.class);
		for (TipoInimigo tipo : TipoInimigo.values()) {
			inimigos.put(tipo,
					new InimigoCatalogoDto(tipo.hp(), tipo.ataque(), tipo.defesa(), tipo.alcance(), tipo.movimento()));
		}

		Map<Integer, MasmorraCatalogoDto> masmorras = new HashMap<>();
		for (int nivel = 1; nivel <= CatalogoMasmorras.NIVEL_MAXIMO; nivel++) {
			Masmorra masmorra = CatalogoMasmorras.porNivel(nivel);
			masmorras.put(nivel, new MasmorraCatalogoDto(nivel, toMapaDto(masmorra.mapa()), masmorra.composicaoInimigos()));
		}

		return new CatalogoDto(predios, cultivos, modelosItem, tropas, inimigos, masmorras);
	}

	private MapaMasmorraDto toMapaDto(MapaMasmorra mapa) {
		List<PosicaoDto> obstaculos = mapa.obstaculos().stream().map(this::toPosicaoDto).toList();
		List<PosicaoDto> posicoesJogador = mapa.posicoesJogador().stream().map(this::toPosicaoDto).toList();
		Map<String, PosicaoDto> spawns = new HashMap<>();
		mapa.spawnsInimigos().forEach((chave, posicao) -> spawns.put(chave, toPosicaoDto(posicao)));
		return new MapaMasmorraDto(mapa.largura(), mapa.altura(), obstaculos, posicoesJogador, spawns);
	}

	private PosicaoDto toPosicaoDto(Posicao posicao) {
		return new PosicaoDto(posicao.x(), posicao.y());
	}

}
