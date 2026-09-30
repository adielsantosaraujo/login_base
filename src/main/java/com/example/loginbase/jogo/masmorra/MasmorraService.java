package com.example.loginbase.jogo.masmorra;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RecursoNaoEncontradoException;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.CatalogoMasmorras;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.MapaMasmorra;
import com.example.loginbase.jogo.catalogo.Masmorra;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.SlotEquipamento;
import com.example.loginbase.jogo.catalogo.TipoInimigo;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoRecurso;
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.config.Aleatorio;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.Batalha;
import com.example.loginbase.jogo.dominio.BatalhaRepository;
import com.example.loginbase.jogo.dominio.EstoqueSemente;
import com.example.loginbase.jogo.dominio.EstoqueSementeRepository;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.ItemRepository;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.PredioRepository;
import com.example.loginbase.jogo.dominio.StatusBatalha;
import com.example.loginbase.jogo.dominio.StatusUnidade;
import com.example.loginbase.jogo.dominio.Unidade;
import com.example.loginbase.jogo.dominio.UnidadeRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.dominio.VilaRepository;
import com.example.loginbase.jogo.economia.VilaService;
import com.example.loginbase.jogo.masmorra.combate.AcaoCombate;
import com.example.loginbase.jogo.masmorra.combate.Combatente;
import com.example.loginbase.jogo.masmorra.combate.EstadoBatalha;
import com.example.loginbase.jogo.masmorra.combate.Lado;
import com.example.loginbase.jogo.masmorra.combate.MotorCombate;
import com.example.loginbase.jogo.masmorra.combate.Posicao;

/**
 * Orquestra o ciclo completo de uma batalha de masmorra: criação (validação
 * de nível/esquadrão/fila, montagem do estado tático inicial), processamento
 * de ações via {@link MotorCombate} com persistência do estado/log após cada
 * uma, e o desfecho da batalha (aplicação de loot, liberação do próximo
 * nível, limpeza de unidades mortas e retorno das sobreviventes a
 * {@code DISPONIVEL}).
 *
 * <p>
 * {@code Batalha.estado} e {@code Batalha.loot} continuam mapeados como
 * {@code String} em {@link Batalha} (ver nota em {@code JsonConverter}): este
 * serviço é quem serializa/desserializa {@link EstadoBatalha} e {@link Loot}
 * via um {@code ObjectMapper} próprio, no mesmo estilo do conversor genérico
 * — mais simples do que introduzir dois conversores JPA concretos agora que
 * outros pacotes (ex.: task 6.1, DTOs da API) ainda vão decidir como expor
 * esses campos.
 *
 * <p>
 * Combatentes do jogador usam o rótulo livre {@link Combatente#tipoOuUnidade()}
 * (sem uso nas regras do motor) para guardar {@code "TIPO:idDaUnidade"} — a
 * única forma de, ao final da batalha, mapear de volta um combatente
 * sobrevivente/morto (ids {@code J1..J4}) para a {@link Unidade} real da vila,
 * já que {@link EstadoBatalha} (pacote {@code jogo.masmorra.combate}, task
 * 5.1) não conhece ids de entidades JPA.
 */
@Service
@Transactional
public class MasmorraService {

	private static final int TAMANHO_MAXIMO_ESQUADRAO = 4;
	private static final int TURNO_MAXIMO = 30;

	private final VilaService vilaService;
	private final VilaRepository vilaRepository;
	private final BatalhaRepository batalhaRepository;
	private final UnidadeRepository unidadeRepository;
	private final ItemRepository itemRepository;
	private final PredioRepository predioRepository;
	private final EstoqueSementeRepository estoqueSementeRepository;
	private final Clock clock;
	private final Aleatorio aleatorio;
	private final JogoProperties jogoProperties;
	private final GeradorLoot geradorLoot = new GeradorLoot();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public MasmorraService(VilaService vilaService, VilaRepository vilaRepository, BatalhaRepository batalhaRepository,
			UnidadeRepository unidadeRepository, ItemRepository itemRepository, PredioRepository predioRepository,
			EstoqueSementeRepository estoqueSementeRepository, Clock clock, Aleatorio aleatorio,
			JogoProperties jogoProperties) {
		this.vilaService = vilaService;
		this.vilaRepository = vilaRepository;
		this.batalhaRepository = batalhaRepository;
		this.unidadeRepository = unidadeRepository;
		this.itemRepository = itemRepository;
		this.predioRepository = predioRepository;
		this.estoqueSementeRepository = estoqueSementeRepository;
		this.clock = clock;
		this.aleatorio = aleatorio;
		this.jogoProperties = jogoProperties;
	}

	/**
	 * Inicia uma batalha na masmorra {@code nivel} com o esquadrão informado
	 * (1 a 4 unidades distintas, {@code DISPONIVEL}, da vila do usuário).
	 * Marca as unidades como {@code EM_MASMORRA} e persiste o estado tático
	 * inicial (mapa, combatentes do jogador e inimigos nas posições
	 * iniciais, turno 1).
	 */
	public Batalha iniciar(long usuarioId, int nivel, List<Long> unidadeIds) {
		Vila vila = vilaService.obterParaAtualizacao(usuarioId);

		if (nivel < 1 || nivel > CatalogoMasmorras.NIVEL_MAXIMO || nivel > vila.getMasmorraNivelLiberado()) {
			throw new RegraJogoException(CodigoErro.MASMORRA_BLOQUEADA,
					"Masmorra de nível " + nivel + " não está liberada.");
		}

		validarTamanhoEsquadrao(unidadeIds);
		List<Unidade> unidades = carregarEsquadrao(vila, unidadeIds);

		if (batalhaRepository.findByVilaIdAndStatus(vila.getId(), StatusBatalha.EM_ANDAMENTO).isPresent()) {
			throw new RegraJogoException(CodigoErro.BATALHA_EM_ANDAMENTO,
					"Já existe uma batalha em andamento para esta vila.");
		}

		EstadoBatalha estadoInicial = construirEstadoInicial(nivel, unidades);

		Batalha batalha = new Batalha();
		batalha.setVilaId(vila.getId());
		batalha.setMasmorraNivel(nivel);
		batalha.setStatus(StatusBatalha.EM_ANDAMENTO);
		batalha.setTurno(estadoInicial.turno());
		batalha.setEstado(serializar(estadoInicial));
		batalha.setLog(String.join("\n", estadoInicial.log()));
		batalha.setIniciadaEm(clock.instant());
		batalha = batalhaRepository.save(batalha);

		for (Unidade unidade : unidades) {
			unidade.setStatus(StatusUnidade.EM_MASMORRA);
			unidadeRepository.save(unidade);
		}

		return batalha;
	}

	/** Consulta somente leitura de uma batalha, restrita ao dono da vila. */
	@Transactional(readOnly = true)
	public Batalha consultar(long usuarioId, long batalhaId) {
		Batalha batalha = buscarBatalha(batalhaId);
		Vila vila = vilaRepository.findByUsuarioId(usuarioId)
				.orElseThrow(() -> batalhaNaoEncontrada(batalhaId));
		if (!batalha.getVilaId().equals(vila.getId())) {
			throw batalhaNaoEncontrada(batalhaId);
		}
		return batalha;
	}

	/**
	 * Processa uma ação de combate sobre a batalha, persistindo o novo
	 * estado/log. Se a ação encerra a batalha, aplica o desfecho: loot e
	 * liberação de nível (vitória), ou apenas o fim (derrota); em ambos os
	 * casos, unidades mortas são excluídas (com seus itens) e sobreviventes
	 * voltam a {@code DISPONIVEL}.
	 */
	public Batalha agir(long usuarioId, long batalhaId, AcaoCombate acao) {
		Vila vila = vilaService.obterParaAtualizacao(usuarioId);
		Batalha batalha = buscarBatalha(batalhaId);
		if (!batalha.getVilaId().equals(vila.getId())) {
			throw batalhaNaoEncontrada(batalhaId);
		}
		if (batalha.getStatus() != StatusBatalha.EM_ANDAMENTO) {
			throw new RegraJogoException(CodigoErro.BATALHA_ENCERRADA, "A batalha já terminou.");
		}

		EstadoBatalha estadoAtual = desserializarEstado(batalha.getEstado());
		EstadoBatalha novoEstado = MotorCombate.processar(estadoAtual, acao);

		batalha.setTurno(novoEstado.turno());
		batalha.setLog(String.join("\n", novoEstado.log()));

		if (MotorCombate.terminou(novoEstado)) {
			finalizarBatalha(vila, batalha, novoEstado);
		} else {
			batalha.setEstado(serializar(novoEstado));
		}

		return batalhaRepository.save(batalha);
	}

	// ---- iniciar: validação e montagem do estado inicial ----

	private void validarTamanhoEsquadrao(List<Long> unidadeIds) {
		if (unidadeIds == null || unidadeIds.isEmpty() || unidadeIds.size() > TAMANHO_MAXIMO_ESQUADRAO
				|| unidadeIds.size() != Set.copyOf(unidadeIds).size()) {
			throw new RegraJogoException(CodigoErro.ESQUADRAO_INVALIDO,
					"O esquadrão deve ter de 1 a " + TAMANHO_MAXIMO_ESQUADRAO + " unidades distintas.");
		}
	}

	private List<Unidade> carregarEsquadrao(Vila vila, List<Long> unidadeIds) {
		Map<Long, Unidade> encontradas = unidadeRepository.findAllById(unidadeIds).stream()
				.collect(Collectors.toMap(Unidade::getId, Function.identity()));
		List<Unidade> unidades = new ArrayList<>();
		for (Long id : unidadeIds) {
			Unidade unidade = encontradas.get(id);
			if (unidade == null || !unidade.getVilaId().equals(vila.getId())
					|| unidade.getStatus() != StatusUnidade.DISPONIVEL) {
				throw new RegraJogoException(CodigoErro.UNIDADE_INDISPONIVEL, "Unidade indisponível: " + id);
			}
			unidades.add(unidade);
		}
		return unidades;
	}

	private EstadoBatalha construirEstadoInicial(int nivel, List<Unidade> unidades) {
		Masmorra masmorra = CatalogoMasmorras.porNivel(nivel);
		MapaMasmorra mapa = masmorra.mapa();
		List<Combatente> combatentes = new ArrayList<>();

		List<MapaMasmorra.Posicao> posicoesJogador = mapa.posicoesJogador();
		for (int i = 0; i < unidades.size(); i++) {
			Unidade unidade = unidades.get(i);
			Item arma = buscarItem(unidade.getArmaItemId());
			Item armadura = buscarItem(unidade.getArmaduraItemId());
			Posicao posicao = paraPosicao(posicoesJogador.get(i));
			combatentes.add(criarCombatenteJogador("J" + (i + 1), posicao, unidade, arma, armadura));
		}

		List<TipoInimigo> composicao = masmorra.composicaoInimigos();
		for (int i = 0; i < composicao.size(); i++) {
			MapaMasmorra.Posicao spawn = mapa.spawnsInimigos().get("S" + (i + 1));
			combatentes.add(criarCombatenteInimigo("I" + (i + 1), paraPosicao(spawn), composicao.get(i)));
		}

		List<String> log = List.of("Batalha iniciada na masmorra nível " + nivel + ".", "Turno 1 começou.");
		return new EstadoBatalha(combatentes, 1, TURNO_MAXIMO, mapa, EstadoBatalha.Resultado.NULO, log);
	}

	private Item buscarItem(Long itemId) {
		return itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalStateException("Item não encontrado: " + itemId));
	}

	private static Combatente criarCombatenteJogador(String id, Posicao posicao, Unidade unidade, Item arma,
			Item armadura) {
		TipoTropa tipo = unidade.getTipo();
		ModeloItem.AtributosItem atributosArma = arma.getModelo().atributos(arma.getNivel());
		ModeloItem.AtributosItem atributosArmadura = armadura.getModelo().atributos(armadura.getNivel());
		int hp = tipo.hp();
		int ataque = atributosArma.ataque();
		int defesa = tipo.defesaBase() + atributosArmadura.defesa();
		int alcance = atributosArma.alcance();
		int movimento = tipo.movimento();
		String tipoOuUnidade = tipo.name() + ":" + unidade.getId();
		return new Combatente(id, Lado.JOGADOR, tipoOuUnidade, posicao, hp, hp, ataque, defesa, alcance, movimento,
				false, false, false, true);
	}

	private static Combatente criarCombatenteInimigo(String id, Posicao posicao, TipoInimigo tipo) {
		return new Combatente(id, Lado.INIMIGO, tipo.name(), posicao, tipo.hp(), tipo.hp(), tipo.ataque(),
				tipo.defesa(), tipo.alcance(), tipo.movimento(), false, false, false, true);
	}

	private static Posicao paraPosicao(MapaMasmorra.Posicao posicao) {
		return new Posicao(posicao.x(), posicao.y());
	}

	/** Extrai o id da {@link Unidade} codificado em {@code "TIPO:id"} por {@link #criarCombatenteJogador}. */
	private static Long extrairUnidadeId(String tipoOuUnidade) {
		int separador = tipoOuUnidade.lastIndexOf(':');
		return Long.valueOf(tipoOuUnidade.substring(separador + 1));
	}

	// ---- agir: desfecho da batalha ----

	private void finalizarBatalha(Vila vila, Batalha batalha, EstadoBatalha novoEstado) {
		batalha.setEstado(serializar(novoEstado));
		batalha.setFinalizadaEm(clock.instant());

		boolean vitoria = novoEstado.resultado() == EstadoBatalha.Resultado.VITORIA;
		batalha.setStatus(vitoria ? StatusBatalha.VITORIA : StatusBatalha.DERROTA);

		atualizarEsquadraoAoFinal(vila, novoEstado);

		if (vitoria) {
			aplicarLoot(vila, batalha);
			liberarProximoNivel(vila, batalha.getMasmorraNivel());
		}
	}

	/**
	 * Unidades do esquadrão (todas as {@code EM_MASMORRA} da vila — só pode
	 * haver uma batalha ativa por vez) que ainda aparecem vivas no estado
	 * final voltam a {@code DISPONIVEL} (HP cheio na próxima batalha, sem
	 * persistência própria); as demais morreram e são excluídas com os itens
	 * que ocupam seus slots de equipamento no momento da morte (na prática,
	 * arma e armadura atuais).
	 */
	private void atualizarEsquadraoAoFinal(Vila vila, EstadoBatalha novoEstado) {
		Set<Long> sobreviventesIds = novoEstado.combatentesDoLado(Lado.JOGADOR).stream()
				.map(c -> extrairUnidadeId(c.tipoOuUnidade()))
				.collect(Collectors.toSet());

		List<Unidade> esquadrao = unidadeRepository.findByVilaId(vila.getId()).stream()
				.filter(u -> u.getStatus() == StatusUnidade.EM_MASMORRA)
				.toList();

		for (Unidade unidade : esquadrao) {
			if (sobreviventesIds.contains(unidade.getId())) {
				unidade.setStatus(StatusUnidade.DISPONIVEL);
				unidadeRepository.save(unidade);
			} else {
				unidadeRepository.delete(unidade);
				destruirItensEquipados(unidade);
			}
		}
	}

	/**
	 * Apaga os itens que ocupam os slots de equipamento da unidade morta,
	 * iterando {@link SlotEquipamento} — hoje só ARMA e ARMADURA têm
	 * persistência ({@code armaItemId}/{@code armaduraItemId}, já refletindo
	 * trocas feitas antes da morte); os demais slots (futuros) ficam sempre
	 * vazios e são ignorados sem erro.
	 */
	private void destruirItensEquipados(Unidade unidade) {
		for (SlotEquipamento slot : SlotEquipamento.values()) {
			Long itemId = switch (slot) {
				case ARMA -> unidade.getArmaItemId();
				case ARMADURA -> unidade.getArmaduraItemId();
				default -> null;
			};
			if (itemId != null) {
				itemRepository.deleteById(itemId);
			}
		}
	}

	private void aplicarLoot(Vila vila, Batalha batalha) {
		Loot loot = geradorLoot.gerar(batalha.getMasmorraNivel(), aleatorio);

		int nivelArmazem = predioRepository.findByVilaId(vila.getId()).stream()
				.filter(p -> p.getTipo() == TipoPredio.ARMAZEM)
				.findFirst()
				.map(Predio::getNivel)
				.orElse(1);
		long capacidadeMilesimos = Math.multiplyExact(TipoRecurso.capacidadeArmazem(nivelArmazem, jogoProperties.curvaNiveis()), 1000L);

		vila.setComida(creditarComCapacidade(vila.getComida(),
				loot.recursos().getOrDefault(TipoRecurso.COMIDA, 0L), capacidadeMilesimos));
		vila.setMadeira(creditarComCapacidade(vila.getMadeira(),
				loot.recursos().getOrDefault(TipoRecurso.MADEIRA, 0L), capacidadeMilesimos));
		vila.setPedra(creditarComCapacidade(vila.getPedra(),
				loot.recursos().getOrDefault(TipoRecurso.PEDRA, 0L), capacidadeMilesimos));
		vila.setFerro(creditarComCapacidade(vila.getFerro(),
				loot.recursos().getOrDefault(TipoRecurso.FERRO, 0L), capacidadeMilesimos));
		vilaRepository.save(vila);

		for (Map.Entry<Cultivo, Integer> entrada : loot.sementes().entrySet()) {
			if (entrada.getValue() > 0) {
				creditarSemente(vila, entrada.getKey(), entrada.getValue());
			}
		}

		for (Item item : loot.itens()) {
			item.setVilaId(vila.getId());
			itemRepository.save(item);
		}

		// O loot gravado na batalha reflete os ganhos "tentados" (antes do
		// corte por capacidade), conforme spec game-dungeon-loot.
		batalha.setLoot(serializar(loot));
	}

	private static long creditarComCapacidade(long atual, long ganho, long capacidadeMilesimos) {
		if (atual >= capacidadeMilesimos) {
			return atual;
		}
		return Math.min(capacidadeMilesimos, atual + ganho);
	}

	private void creditarSemente(Vila vila, Cultivo cultivo, int quantidade) {
		EstoqueSemente estoque = estoqueSementeRepository.findByVilaId(vila.getId()).stream()
				.filter(e -> e.getCultivo() == cultivo)
				.findFirst()
				.orElseGet(() -> {
					EstoqueSemente novo = new EstoqueSemente();
					novo.setVilaId(vila.getId());
					novo.setCultivo(cultivo);
					novo.setQuantidade(0);
					return novo;
				});
		estoque.setQuantidade(estoque.getQuantidade() + quantidade);
		estoqueSementeRepository.save(estoque);
	}

	private void liberarProximoNivel(Vila vila, int nivelBatalha) {
		int novoNivel = Math.max(vila.getMasmorraNivelLiberado(),
				Math.min(CatalogoMasmorras.NIVEL_MAXIMO, nivelBatalha + 1));
		if (novoNivel > vila.getMasmorraNivelLiberado()) {
			vila.setMasmorraNivelLiberado(novoNivel);
			vilaRepository.save(vila);
		}
	}

	// ---- auxiliares ----

	private Batalha buscarBatalha(long batalhaId) {
		return batalhaRepository.findById(batalhaId)
				.orElseThrow(() -> batalhaNaoEncontrada(batalhaId));
	}

	private static RecursoNaoEncontradoException batalhaNaoEncontrada(long batalhaId) {
		return new RecursoNaoEncontradoException("Batalha não encontrada: " + batalhaId);
	}

	private String serializar(Object valor) {
		try {
			return objectMapper.writeValueAsString(valor);
		} catch (JacksonException e) {
			throw new IllegalStateException("Falha ao serializar " + valor.getClass().getSimpleName() + " para JSON",
					e);
		}
	}

	private EstadoBatalha desserializarEstado(String json) {
		try {
			return objectMapper.readValue(json, EstadoBatalha.class);
		} catch (JacksonException e) {
			throw new IllegalStateException("Falha ao desserializar EstadoBatalha de JSON", e);
		}
	}

}
