package com.example.loginbase.jogo.quartel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
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
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.ItemRepository;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.OrdemRepository;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.PredioRepository;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.dominio.StatusUnidade;
import com.example.loginbase.jogo.dominio.Unidade;
import com.example.loginbase.jogo.dominio.UnidadeRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.dominio.VilaRepository;
import com.example.loginbase.jogo.economia.AplicadorOrdens;
import com.example.loginbase.jogo.economia.VilaService;
import com.example.loginbase.jogo.suporte.JogoTestConfig;
import com.example.loginbase.jogo.suporte.RelogioAjustavel;

/**
 * Testa {@link QuartelService}: validação de ordem de treino (quartel abaixo
 * do mínimo, arma/armadura inexistente, de modelo/categoria errada,
 * reservada ou de outra vila, fila ocupada, capacidade do exército e comida
 * insuficiente), reserva imediata de itens e débito de comida, e a conclusão
 * da ordem (via {@link AplicadorOrdens}, disparada por
 * {@link VilaService#sincronizar}) — mesma premissa de
 * {@code FazendaServiceTest} (Postgres real do {@code docker-compose},
 * {@code @Transactional(propagation = NOT_SUPPORTED)} porque
 * {@code VilaService} usa uma transação {@code REQUIRES_NEW} genuína para
 * criar a vila).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ AuditoriaConfig.class, UsuarioAuditorAware.class, JogoTestConfig.class, JogoProperties.class,
		VilaService.class, AplicadorOrdens.class, QuartelService.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class QuartelServiceTest {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private VilaService vilaService;

	@Autowired
	private QuartelService quartelService;

	@Autowired
	private VilaRepository vilaRepository;

	@Autowired
	private PredioRepository predioRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	@Autowired
	private OrdemRepository ordemRepository;

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

	/** Cria a vila do usuário e ajusta o nível do quartel (prédio já existe, criado em nível 0). */
	private Vila criarVilaComQuartel(Usuario usuario, int nivelQuartel) {
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Predio quartel = predioRepository.findByVilaId(vila.getId()).stream()
				.filter(p -> p.getTipo() == TipoPredio.QUARTEL)
				.findFirst()
				.orElseThrow();
		quartel.setNivel(nivelQuartel);
		predioRepository.saveAndFlush(quartel);
		return vila;
	}

	private Item criarItem(Long vilaId, ModeloItem modelo, StatusItem status) {
		Item item = new Item();
		item.setVilaId(vilaId);
		item.setModelo(modelo);
		item.setNivel(1);
		item.setOrigem(OrigemItem.FORJA);
		item.setStatus(status);
		return itemRepository.saveAndFlush(item);
	}

	private Unidade criarUnidade(Long vilaId, TipoTropa tipo, Long armaItemId, Long armaduraItemId) {
		Unidade unidade = new Unidade();
		unidade.setVilaId(vilaId);
		unidade.setTipo(tipo);
		unidade.setArmaItemId(armaItemId);
		unidade.setArmaduraItemId(armaduraItemId);
		unidade.setStatus(StatusUnidade.DISPONIVEL);
		return unidadeRepository.saveAndFlush(unidade);
	}

	private RegraJogoException assertRegra(Runnable acao) {
		return assertThrows(RegraJogoException.class, acao::run);
	}

	@Test
	void quartelAbaixoDoMinimoRejeitaComRequisitoNaoAtendido() {
		Usuario usuario = criarUsuario("quartel-baixo", "Ana");
		Vila vila = criarVilaComQuartel(usuario, 1);
		Item arma = criarItem(vila.getId(), ModeloItem.ARCO, StatusItem.DISPONIVEL);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(
				() -> quartelService.treinar(usuario.getId(), TipoTropa.ARQUEIRO, arma.getId(), armadura.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.REQUISITO_NAO_ATENDIDO);
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void armaInexistenteRejeitaComItemIndisponivel() {
		Usuario usuario = criarUsuario("arma-inexistente", "Bruno");
		Vila vila = criarVilaComQuartel(usuario, 1);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(
				() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 999_999L, armadura.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
	}

	@Test
	void armaDeModeloErradoRejeitaComItemIndisponivel() {
		Usuario usuario = criarUsuario("arma-modelo-errado", "Carla");
		Vila vila = criarVilaComQuartel(usuario, 1);
		Item arco = criarItem(vila.getId(), ModeloItem.ARCO, StatusItem.DISPONIVEL);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);

		// SOLDADO exige ESPADA, não ARCO.
		RegraJogoException ex = assertRegra(
				() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, arco.getId(), armadura.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
	}

	@Test
	void armaReservadaRejeitaComItemIndisponivel() {
		Usuario usuario = criarUsuario("arma-reservada", "Diego");
		Vila vila = criarVilaComQuartel(usuario, 1);
		Item espada = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.RESERVADO);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(
				() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, espada.getId(), armadura.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
	}

	@Test
	void armaduraDeCategoriaErradaRejeitaComItemIndisponivel() {
		Usuario usuario = criarUsuario("armadura-categoria-errada", "Elisa");
		Vila vila = criarVilaComQuartel(usuario, 1);
		Item espada = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.DISPONIVEL);
		Item outraEspada = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO,
				espada.getId(), outraEspada.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
	}

	@Test
	void armaduraDeOutraVilaRejeitaComItemIndisponivel() {
		Usuario usuario = criarUsuario("armadura-outra-vila", "Fabio");
		Vila vila = criarVilaComQuartel(usuario, 1);
		Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.DISPONIVEL);

		Usuario outroUsuario = criarUsuario("armadura-outra-vila-dono", "Gustavo");
		Vila outraVila = vilaService.obterParaAtualizacao(outroUsuario.getId());
		Item armaduraDeOutraVila = criarItem(outraVila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO,
				arma.getId(), armaduraDeOutraVila.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
	}

	@Test
	void filaOcupadaRejeitaSegundaOrdemDeTreino() {
		Usuario usuario = criarUsuario("fila-ocupada", "Helena");
		Vila vila = criarVilaComQuartel(usuario, 1);
		Item arma1 = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.DISPONIVEL);
		Item armadura1 = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);
		quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, arma1.getId(), armadura1.getId());

		Item arma2 = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.DISPONIVEL);
		Item armadura2 = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(
				() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, arma2.getId(), armadura2.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.FILA_OCUPADA);
		assertThat(ordemRepository.findByVilaId(vila.getId())).hasSize(1);
	}

	@Test
	void capacidadeExercitoExcedidaRejeitaOrdem() {
		Usuario usuario = criarUsuario("capacidade-excedida", "Ines");
		// Quartel N1 -> capacidade 3. 3 unidades já ocupam todos os slots (as duas
		// primeiras diretamente e a terceira simulando uma que veio de um treino já
		// concluído), sem nenhuma ordem TREINO pendente.
		Vila vila = criarVilaComQuartel(usuario, 1);
		for (int i = 0; i < 3; i++) {
			Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.EQUIPADO);
			Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.EQUIPADO);
			criarUnidade(vila.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId());
		}

		Item novaArma = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.DISPONIVEL);
		Item novaArmadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO,
				novaArma.getId(), novaArmadura.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.CAPACIDADE_EXERCITO);
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
		assertThat(itemRepository.findById(novaArma.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusItem.DISPONIVEL);
	}

	@Test
	void quartelNivel1Com2UnidadesAceitaTerceiroTreino() {
		Usuario usuario = criarUsuario("capacidade-limite", "Julia");
		Vila vila = criarVilaComQuartel(usuario, 1);
		for (int i = 0; i < 2; i++) {
			Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.EQUIPADO);
			Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.EQUIPADO);
			criarUnidade(vila.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId());
		}

		Item novaArma = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.DISPONIVEL);
		Item novaArmadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);

		quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, novaArma.getId(), novaArmadura.getId());

		assertThat(ordemRepository.findByVilaId(vila.getId())).hasSize(1);
	}

	@Test
	void comidaInsuficienteRejeitaOrdemENaoAlteraItens() {
		Usuario usuario = criarUsuario("comida-insuficiente", "Karla");
		Vila vila = criarVilaComQuartel(usuario, 3);
		vila.setComida(30_000L); // 30 comida (em milésimos); LANCEIRO exige 60.
		vilaRepository.saveAndFlush(vila);

		Item lanca = criarItem(vila.getId(), ModeloItem.LANCA, StatusItem.DISPONIVEL);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_FERRO, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(
				() -> quartelService.treinar(usuario.getId(), TipoTropa.LANCEIRO, lanca.getId(), armadura.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.RECURSOS_INSUFICIENTES);
		assertThat(itemRepository.findById(lanca.getId()).orElseThrow().getStatus()).isEqualTo(StatusItem.DISPONIVEL);
		assertThat(itemRepository.findById(armadura.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusItem.DISPONIVEL);
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void treinarReservaArmaEArmaduraEDebitaComidaImediatamente() {
		Usuario usuario = criarUsuario("reserva-debito", "Lucas");
		Vila vila = criarVilaComQuartel(usuario, 1);
		long comidaInicial = vila.getComida();
		Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.DISPONIVEL);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);

		quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId());

		assertThat(itemRepository.findById(arma.getId()).orElseThrow().getStatus()).isEqualTo(StatusItem.RESERVADO);
		assertThat(itemRepository.findById(armadura.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusItem.RESERVADO);

		Vila atualizada = vilaService.obterParaAtualizacao(usuario.getId());
		// SOLDADO custa 50 comida (em milésimos: 50_000).
		assertThat(atualizada.getComida()).isEqualTo(comidaInicial - 50_000L);

		List<Ordem> ordens = ordemRepository.findByVilaId(vila.getId());
		assertThat(ordens).hasSize(1);
		Ordem ordem = ordens.get(0);
		assertThat(ordem.getAlvo()).isEqualTo(TipoTropa.SOLDADO.name());
		assertThat(ordem.getArmaItemId()).isEqualTo(arma.getId());
		assertThat(ordem.getArmaduraItemId()).isEqualTo(armadura.getId());
		assertThat(ordem.getConcluiEm()).isEqualTo(ordem.getIniciadaEm().plusSeconds(60));
		assertThat(unidadeRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void treinarUnidadeAoAvancarRelogioConcluiOrdemEEquipaItens() {
		Usuario usuario = criarUsuario("conclusao-treino", "Marina");
		Vila vila = criarVilaComQuartel(usuario, 1);
		Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.DISPONIVEL);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);

		quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId());

		relogio.avancar(Duration.ofSeconds(61));
		vilaService.obterParaAtualizacao(usuario.getId());

		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();

		List<Unidade> unidades = unidadeRepository.findByVilaId(vila.getId());
		assertThat(unidades).hasSize(1);
		assertThat(unidades.get(0).getTipo()).isEqualTo(TipoTropa.SOLDADO);
		assertThat(unidades.get(0).getStatus()).isEqualTo(StatusUnidade.DISPONIVEL);
		assertThat(unidades.get(0).getArmaItemId()).isEqualTo(arma.getId());
		assertThat(unidades.get(0).getArmaduraItemId()).isEqualTo(armadura.getId());

		assertThat(itemRepository.findById(arma.getId()).orElseThrow().getStatus()).isEqualTo(StatusItem.EQUIPADO);
		assertThat(itemRepository.findById(armadura.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusItem.EQUIPADO);

		// Fila liberada: nova ordem de treino é aceita.
		Item novaArma = criarItem(vila.getId(), ModeloItem.ESPADA, StatusItem.DISPONIVEL);
		Item novaArmadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, StatusItem.DISPONIVEL);
		quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, novaArma.getId(), novaArmadura.getId());
		assertThat(ordemRepository.findByVilaId(vila.getId())).hasSize(1);
	}

}
