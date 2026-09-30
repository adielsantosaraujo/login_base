package com.example.loginbase.jogo.forja;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.auditoria.AuditoriaConfig;
import com.example.loginbase.auditoria.UsuarioAuditorAware;
import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.CategoriaOrdem;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.ItemRepository;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.OrdemRepository;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.PredioRepository;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.dominio.VilaRepository;
import com.example.loginbase.jogo.economia.AplicadorOrdens;
import com.example.loginbase.jogo.economia.VilaService;
import com.example.loginbase.jogo.suporte.JogoTestConfig;
import com.example.loginbase.jogo.suporte.RelogioAjustavel;

/**
 * Testa {@link ForjaService}: validações de nível/quantidade da ordem e da
 * capacidade da FORJA, fila de 1 por vila, débito do custo total (tabela
 * A.7), cálculo do tempo total (incluindo o efeito da velocidade do jogo) e —
 * via {@link VilaService#sincronizar} — a entrega dos itens na conclusão.
 * Mesma premissa de {@code VilaServiceTest}/{@code FazendaServiceTest}
 * (Postgres real do {@code docker-compose}, {@code @Transactional(propagation
 * = NOT_SUPPORTED)} porque {@code VilaService} usa uma transação
 * {@code REQUIRES_NEW} genuína para criar a vila).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ AuditoriaConfig.class, UsuarioAuditorAware.class, JogoTestConfig.class, JogoProperties.class,
		VilaService.class, AplicadorOrdens.class, ForjaService.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ForjaServiceTest {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private VilaService vilaService;

	@Autowired
	private ForjaService forjaService;

	@Autowired
	private VilaRepository vilaRepository;

	@Autowired
	private PredioRepository predioRepository;

	@Autowired
	private OrdemRepository ordemRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private JogoProperties jogoProperties;

	@Autowired
	private RelogioAjustavel relogio;

	/**
	 * @param prefixoEmail identificador curto do cenário; um sufixo único é anexado para que
	 *                     reexecuções do teste — cujos dados ficam commitados de verdade, sem
	 *                     rollback (ver Javadoc da classe) — nunca colidam com a unique
	 *                     constraint de e-mail
	 */
	private Usuario criarUsuario(String prefixoEmail, String nome) {
		Usuario usuario = new Usuario();
		usuario.setNome(nome);
		usuario.setEmail(prefixoEmail + "+" + java.util.UUID.randomUUID() + "@teste.local");
		usuario.setSenha("senha-hash");
		return usuarioRepository.saveAndFlush(usuario);
	}

	/** Cria a vila do usuário e ajusta o nível da FORJA (0 = não construída). */
	private Vila prepararVilaComForja(String prefixoEmail, int nivelForja) {
		Usuario usuario = criarUsuario(prefixoEmail, "Ferreiro");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Predio forja = predioRepository.findByVilaId(vila.getId()).stream()
				.filter(predio -> predio.getTipo() == TipoPredio.FORJA)
				.findFirst()
				.orElseThrow();
		forja.setNivel(nivelForja);
		predioRepository.saveAndFlush(forja);
		return vila;
	}

	/** Igual a {@link #prepararVilaComForja}, mas com madeira e ferro fartos para cobrir o custo das ordens. */
	private Vila prepararVilaComForjaERecursosFartos(String prefixoEmail, int nivelForja) {
		Vila vila = prepararVilaComForja(prefixoEmail, nivelForja);
		vila.setMadeira(1_000_000L);
		vila.setFerro(1_000_000L);
		return vilaRepository.saveAndFlush(vila);
	}

	@Test
	void forjaNivelZeroRejeitaComRequisitoNaoAtendido() {
		Vila vila = prepararVilaComForja("forja-zero", 0);

		assertThatThrownBy(() -> forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 1, 1))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.REQUISITO_NAO_ATENDIDO));
	}

	@Test
	void nivelDoItemAcimaDoNivelDaForjaRejeitaComRequisitoNaoAtendido() {
		Vila vila = prepararVilaComForja("forja-nivel", 1);

		assertThatThrownBy(() -> forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 2, 1))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.REQUISITO_NAO_ATENDIDO));
	}

	@Test
	void forjaNivel11RejeitaItemNivel11PorAcimaDoMaximoForjavel() {
		Vila vila = prepararVilaComForja("forja-11-item-11", 11);

		assertThatThrownBy(() -> forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 11, 1))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.REQUISITO_NAO_ATENDIDO));
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void forjaNivel15AceitaItemNivel11() {
		Vila vila = prepararVilaComForja("forja-15-item-11", 15);
		definirNivelArmazemERecursos(vila, 2, 500_000L);

		forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 11, 1);

		assertThat(ordemRepository.findByVilaId(vila.getId())).hasSize(1);
	}

	@Test
	void forjaNivel100AceitaItemNivel23() {
		Vila vila = prepararVilaComForja("forja-100-item-23", 100);
		definirNivelArmazemERecursos(vila, 3, 1_000_000L);

		forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 23, 1);

		Ordem ordem = ordemRepository.findByVilaId(vila.getId()).stream()
				.filter(o -> o.getCategoria() == CategoriaOrdem.FORJA)
				.findFirst()
				.orElseThrow();
		assertThat(ordem.getNivel()).isEqualTo(23);
	}

	/** Ajusta o ARMAZEM e dá madeira/ferro (em milésimos) dentro da capacidade do nível. */
	private void definirNivelArmazemERecursos(Vila vila, int nivelArmazem, long milesimos) {
		Predio armazem = predioRepository.findByVilaId(vila.getId()).stream()
				.filter(predio -> predio.getTipo() == TipoPredio.ARMAZEM)
				.findFirst()
				.orElseThrow();
		armazem.setNivel(nivelArmazem);
		predioRepository.saveAndFlush(armazem);
		Vila atual = vilaRepository.findById(vila.getId()).orElseThrow();
		atual.setMadeira(milesimos);
		atual.setFerro(milesimos);
		vilaRepository.saveAndFlush(atual);
	}

	@Test
	void nivelOuQuantidadeForaDaFaixaRejeitaComRequisicaoInvalida() {
		Vila vila = prepararVilaComForja("faixa-invalida", 5);

		assertThatThrownBy(() -> forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 0, 1))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.REQUISICAO_INVALIDA));
		assertThatThrownBy(() -> forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 24, 1))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.REQUISICAO_INVALIDA));
		assertThatThrownBy(() -> forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 1, 0))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.REQUISICAO_INVALIDA));
		assertThatThrownBy(() -> forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 1, 6))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.REQUISICAO_INVALIDA));
	}

	@Test
	void segundaOrdemDeForjaEnquantoHaUmaAtivaRejeitaComFilaOcupada() {
		Vila vila = prepararVilaComForja("fila-ocupada", 3);
		forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 1, 1);

		assertThatThrownBy(() -> forjaService.forjar(vila.getUsuarioId(), ModeloItem.LANCA, 1, 1))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.FILA_OCUPADA));
	}

	@Test
	void recursosInsuficientesRejeitaAOrdemENaoDebitaNada() {
		Vila vila = prepararVilaComForja("recursos-insuficientes", 3);
		long ferroAntes = vilaRepository.findById(vila.getId()).orElseThrow().getFerro();

		// Ferro inicial da vila é 50 unidades; 5 ESPADAS N1 exigem F150 (base F30 × nível 1 × qtd 5).
		assertThatThrownBy(() -> forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 1, 5))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.RECURSOS_INSUFICIENTES));

		assertThat(vilaRepository.findById(vila.getId()).orElseThrow().getFerro()).isEqualTo(ferroAntes);
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void custoTotalDebitadoConformeTabelaA7() {
		Vila vila = prepararVilaComForjaERecursosFartos("custo", 3);
		long madeiraAntes = vilaRepository.findById(vila.getId()).orElseThrow().getMadeira();
		long ferroAntes = vilaRepository.findById(vila.getId()).orElseThrow().getFerro();

		// 2 ESPADAS nível 2: custo M80 F120 (base M20 F30 × nível 2 × quantidade 2).
		forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 2, 2);

		Vila atualizada = vilaRepository.findById(vila.getId()).orElseThrow();
		assertThat(madeiraAntes - atualizada.getMadeira()).isEqualTo(80_000L);
		assertThat(ferroAntes - atualizada.getFerro()).isEqualTo(120_000L);
	}

	@Test
	void tempoTotalCalculadoConformeTabelaA7() {
		Vila vila = prepararVilaComForjaERecursosFartos("tempo", 3);
		Instant agora = relogio.instant();

		// 2 ESPADAS nível 2: tempo ceil(60 × 2 × 2 / 1) = 240s.
		forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 2, 2);

		Ordem ordem = ordemRepository.findByVilaId(vila.getId()).stream()
				.filter(o -> o.getCategoria() == CategoriaOrdem.FORJA)
				.findFirst()
				.orElseThrow();
		assertThat(ordem.getAlvo()).isEqualTo(ModeloItem.ESPADA.name());
		assertThat(ordem.getNivel()).isEqualTo(2);
		assertThat(ordem.getQuantidade()).isEqualTo(2);
		assertThat(ordem.getIniciadaEm()).isEqualTo(agora);
		assertThat(Duration.between(ordem.getIniciadaEm(), ordem.getConcluiEm())).isEqualTo(Duration.ofSeconds(240));
	}

	@Test
	void velocidadeDoJogoDivideOTempoTotalDaOrdem() {
		Vila vila = prepararVilaComForjaERecursosFartos("velocidade", 3);

		jogoProperties.setVelocidade(2);
		try {
			// Mesma ordem de tempoTotalCalculadoConformeTabelaA7 (240s com velocidade 1):
			// com velocidade 2, o tempo cai pela metade.
			forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 2, 2);
		} finally {
			jogoProperties.setVelocidade(1);
		}

		Ordem ordem = ordemRepository.findByVilaId(vila.getId()).stream()
				.filter(o -> o.getCategoria() == CategoriaOrdem.FORJA)
				.findFirst()
				.orElseThrow();
		assertThat(Duration.between(ordem.getIniciadaEm(), ordem.getConcluiEm())).isEqualTo(Duration.ofSeconds(120));
	}

	@Test
	void ordemDeForjaConcluidaEntregaTodosOsItensDisponiveisJuntos() {
		Vila vila = prepararVilaComForjaERecursosFartos("entrega", 3);
		forjaService.forjar(vila.getUsuarioId(), ModeloItem.ESPADA, 2, 2);

		relogio.avancar(Duration.ofSeconds(241));
		vilaService.obterParaAtualizacao(vila.getUsuarioId());

		List<Item> itens = itemRepository.findByVilaId(vila.getId());
		assertThat(itens).hasSize(2);
		assertThat(itens).allSatisfy(item -> {
			assertThat(item.getModelo()).isEqualTo(ModeloItem.ESPADA);
			assertThat(item.getNivel()).isEqualTo(2);
			assertThat(item.getOrigem()).isEqualTo(OrigemItem.FORJA);
			assertThat(item.getStatus()).isEqualTo(StatusItem.DISPONIVEL);
		});
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
	}

}
