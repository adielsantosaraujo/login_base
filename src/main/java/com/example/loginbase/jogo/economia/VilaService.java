package com.example.loginbase.jogo.economia;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.RecursoNaoEncontradoException;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoRecurso;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.Canteiro;
import com.example.loginbase.jogo.dominio.CanteiroRepository;
import com.example.loginbase.jogo.dominio.EstoqueSementeRepository;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.ItemRepository;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.OrdemRepository;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.PredioRepository;
import com.example.loginbase.jogo.dominio.Unidade;
import com.example.loginbase.jogo.dominio.UnidadeRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.dominio.VilaRepository;

/**
 * Orquestra a vila do usuário: criação no primeiro acesso (com estado
 * inicial), carregamento com lock pessimista e sincronização — o "cálculo
 * preguiçoso do tempo" que avança recursos e conclui ordens vencidas antes de
 * qualquer leitura ou comando. É o serviço do qual todos os serviços de ação
 * (construção, fazenda, forja, quartel, masmorra) dependem para obter a vila
 * já em dia.
 */
@Service
@Transactional
public class VilaService {

	private final VilaRepository vilaRepository;
	private final PredioRepository predioRepository;
	private final CanteiroRepository canteiroRepository;
	private final EstoqueSementeRepository estoqueSementeRepository;
	private final ItemRepository itemRepository;
	private final UnidadeRepository unidadeRepository;
	private final OrdemRepository ordemRepository;
	private final UsuarioRepository usuarioRepository;
	private final AplicadorOrdens aplicadorOrdens;
	private final Clock clock;
	private final CalculadoraProducao calculadoraProducao;
	private final TransactionTemplate transacaoNova;

	public VilaService(VilaRepository vilaRepository, PredioRepository predioRepository,
			CanteiroRepository canteiroRepository, EstoqueSementeRepository estoqueSementeRepository,
			ItemRepository itemRepository, UnidadeRepository unidadeRepository, OrdemRepository ordemRepository,
			UsuarioRepository usuarioRepository, AplicadorOrdens aplicadorOrdens, JogoProperties jogoProperties,
			Clock clock, PlatformTransactionManager transactionManager) {
		this.vilaRepository = vilaRepository;
		this.predioRepository = predioRepository;
		this.canteiroRepository = canteiroRepository;
		this.estoqueSementeRepository = estoqueSementeRepository;
		this.itemRepository = itemRepository;
		this.unidadeRepository = unidadeRepository;
		this.ordemRepository = ordemRepository;
		this.usuarioRepository = usuarioRepository;
		this.aplicadorOrdens = aplicadorOrdens;
		this.clock = clock;
		this.calculadoraProducao = new CalculadoraProducao(jogoProperties.getVelocidade());
		this.transacaoNova = new TransactionTemplate(transactionManager);
		this.transacaoNova.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
	}

	/**
	 * Retorna a vila do usuário travada para atualização (lock pessimista de
	 * escrita), já sincronizada (recursos em dia e ordens vencidas
	 * aplicadas). Cria a vila com o estado inicial no primeiro acesso.
	 */
	public Vila obterParaAtualizacao(long usuarioId) {
		Vila vila = vilaRepository.findByUsuarioIdParaAtualizacao(usuarioId).orElse(null);
		if (vila == null) {
			criarSeNaoExiste(usuarioId);
			vila = vilaRepository.findByUsuarioIdParaAtualizacao(usuarioId)
					.orElseThrow(() -> new IllegalStateException(
							"Vila não encontrada para usuário " + usuarioId + " logo após sua criação"));
		}
		sincronizar(vila, clock.instant());
		return vila;
	}

	/** Snapshot somente leitura da vila (já sincronizada) para os mapeadores da API. */
	public EstadoVila consultar(long usuarioId) {
		Vila vila = obterParaAtualizacao(usuarioId);
		List<Predio> predios = predioRepository.findByVilaId(vila.getId());
		List<Canteiro> canteiros = canteiroRepository.findByVilaId(vila.getId());
		List<Item> itens = itemRepository.findByVilaId(vila.getId());
		List<Unidade> unidades = unidadeRepository.findByVilaId(vila.getId());
		List<Ordem> ordens = ordemRepository.findByVilaId(vila.getId());
		return new EstadoVila(vila, predios, canteiros, estoqueSementeRepository.findByVilaId(vila.getId()), itens,
				unidades, ordens);
	}

	/**
	 * Cálculo preguiçoso do tempo: aplica, em ordem cronológica, o efeito de
	 * cada ordem já vencida (produzindo recursos até o instante de conclusão
	 * de cada uma, aplicando seu efeito e removendo-a), e por fim produz
	 * recursos até {@code agora}. Persiste a vila ao final.
	 */
	void sincronizar(Vila vila, Instant agora) {
		List<Ordem> ordensVencidas = ordemRepository.findByVilaId(vila.getId()).stream()
				.filter(ordem -> !ordem.getConcluiEm().isAfter(agora))
				.sorted(Comparator.comparing(Ordem::getConcluiEm).thenComparing(Ordem::getId))
				.toList();

		for (Ordem ordem : ordensVencidas) {
			produzirAte(vila, ordem.getConcluiEm());
			aplicadorOrdens.aplicar(ordem, vila);
			ordemRepository.delete(ordem);
		}
		produzirAte(vila, agora);
		vilaRepository.save(vila);
	}

	private void produzirAte(Vila vila, Instant dataAlvo) {
		Instant anterior = vila.getRecursosAtualizadosEm();
		if (!dataAlvo.isAfter(anterior)) {
			return;
		}

		Map<TipoPredio, Integer> niveisPredio = predioRepository.findByVilaId(vila.getId()).stream()
				.collect(Collectors.toMap(Predio::getTipo, Predio::getNivel));
		List<Cultivo> cultivosCanteiros = canteiroRepository.findByVilaId(vila.getId()).stream()
				.map(Canteiro::getCultivo)
				.toList();

		Map<TipoRecurso, Long> taxasHoraPorRecurso = calculadoraProducao.taxaHoraPorRecurso(niveisPredio,
				cultivosCanteiros);
		int nivelArmazem = niveisPredio.getOrDefault(TipoPredio.ARMAZEM, 1);
		long capacidade = calculadoraProducao.capacidadeMaxima(nivelArmazem);

		Estoque estoqueAtual = new Estoque(Map.of(
				TipoRecurso.COMIDA, vila.getComida(),
				TipoRecurso.MADEIRA, vila.getMadeira(),
				TipoRecurso.PEDRA, vila.getPedra(),
				TipoRecurso.FERRO, vila.getFerro()));

		Estoque novoEstoque = calculadoraProducao.produzirAte(estoqueAtual, taxasHoraPorRecurso, capacidade, anterior,
				dataAlvo);

		vila.setComida(novoEstoque.get(TipoRecurso.COMIDA));
		vila.setMadeira(novoEstoque.get(TipoRecurso.MADEIRA));
		vila.setPedra(novoEstoque.get(TipoRecurso.PEDRA));
		vila.setFerro(novoEstoque.get(TipoRecurso.FERRO));
		vila.setRecursosAtualizadosEm(dataAlvo);
	}

	/**
	 * Cria a vila com o estado inicial em uma transação {@code REQUIRES_NEW}
	 * (independente da transação corrente, que ainda não obteve o lock por a
	 * vila não existir). Se outra requisição concorrente já criou a vila
	 * entre a busca e esta chamada, a violação da unique constraint
	 * {@code usuario_id} é engolida: quem chamou relê a vila já criada.
	 */
	private void criarSeNaoExiste(long usuarioId) {
		try {
			transacaoNova.executeWithoutResult(status -> criarVilaInicial(usuarioId));
		} catch (DataIntegrityViolationException ex) {
			// Vila criada concorrentemente por outra requisição: será relida a seguir.
		}
	}

	private void criarVilaInicial(long usuarioId) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + usuarioId));

		Instant agora = clock.instant();
		Vila vila = new Vila();
		vila.setUsuarioId(usuarioId);
		vila.setNome("Vila de " + usuario.getNome());
		vila.setComida(300_000L);
		vila.setMadeira(400_000L);
		vila.setPedra(300_000L);
		vila.setFerro(50_000L);
		vila.setRecursosAtualizadosEm(agora);
		vila = vilaRepository.save(vila);

		criarPredioInicial(vila.getId(), TipoPredio.CENTRO_VILA, 1);
		criarPredioInicial(vila.getId(), TipoPredio.ARMAZEM, 1);
		criarPredioInicial(vila.getId(), TipoPredio.FAZENDA, 1);
		criarPredioInicial(vila.getId(), TipoPredio.SERRARIA, 1);
		criarPredioInicial(vila.getId(), TipoPredio.PEDREIRA, 1);
		criarPredioInicial(vila.getId(), TipoPredio.MINA_FERRO, 0);
		criarPredioInicial(vila.getId(), TipoPredio.FORJA, 0);
		criarPredioInicial(vila.getId(), TipoPredio.QUARTEL, 0);

		Canteiro canteiro = new Canteiro();
		canteiro.setVilaId(vila.getId());
		canteiro.setPosicao(1);
		canteiro.setCultivo(Cultivo.TRIGO);
		canteiro.setPlantadoEm(agora);
		canteiroRepository.save(canteiro);
	}

	private void criarPredioInicial(Long vilaId, TipoPredio tipo, int nivel) {
		Predio predio = new Predio();
		predio.setVilaId(vilaId);
		predio.setTipo(tipo);
		predio.setNivel(nivel);
		predioRepository.save(predio);
	}

}
