package com.example.loginbase.jogo.quartel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.ArrayList;
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
 * Testa {@link QuartelService}: validação do treino em lote (quartel abaixo
 * do mínimo, modelo de armadura de categoria errada, fila ocupada,
 * capacidade do exército, armas/armaduras insuficientes — inclusive
 * ignorando itens de outra vila — e comida insuficiente), reserva imediata
 * das N menores armas/armaduras e débito de comida ao aceitar o lote, e a
 * conclusão da ordem (via {@link AplicadorOrdens}, disparada por
 * {@link VilaService#sincronizar}) criando as N unidades do lote — mesma
 * premissa de {@code FazendaServiceTest} (Postgres real do
 * {@code docker-compose}, {@code @Transactional(propagation = NOT_SUPPORTED)}
 * porque {@code VilaService} usa uma transação {@code REQUIRES_NEW} genuína
 * para criar a vila).
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

	private Item criarItem(Long vilaId, ModeloItem modelo, int nivel, StatusItem status) {
		Item item = new Item();
		item.setVilaId(vilaId);
		item.setModelo(modelo);
		item.setNivel(nivel);
		item.setOrigem(OrigemItem.FORJA);
		item.setStatus(status);
		return itemRepository.saveAndFlush(item);
	}

	private List<Item> criarItens(Long vilaId, ModeloItem modelo, int nivel, StatusItem status, int quantidade) {
		List<Item> itens = new ArrayList<>();
		for (int i = 0; i < quantidade; i++) {
			itens.add(criarItem(vilaId, modelo, nivel, status));
		}
		return itens;
	}

	private Unidade criarUnidade(Long vilaId, TipoTropa tipo, Long armaItemId, Long armaduraItemId) {
		Unidade unidade = new Unidade();
		unidade.setVilaId(vilaId);
		unidade.setTipo(tipo);
		unidade.setArmaItemId(armaItemId);
		unidade.setArmaduraItemId(armaduraItemId);
		unidade.setStatus(StatusUnidade.DISPONIVEL);
		// nome/sobrenome únicos por chamada (derivados do id da arma, sempre novo
		// nesses testes) para não colidir com a unique constraint da vila.
		unidade.setNome("Soldado" + armaItemId);
		unidade.setSobrenome("Teste");
		unidade.setOrdinalNome(1);
		return unidadeRepository.saveAndFlush(unidade);
	}

	private RegraJogoException assertRegra(Runnable acao) {
		return assertThrows(RegraJogoException.class, acao::run);
	}

	@Test
	void quartelAbaixoDoMinimoRejeitaComRequisitoNaoAtendido() {
		Usuario usuario = criarUsuario("quartel-baixo", "Ana");
		Vila vila = criarVilaComQuartel(usuario, 1);
		criarItens(vila.getId(), ModeloItem.ARCO, 1, StatusItem.DISPONIVEL, 2);
		criarItens(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.DISPONIVEL, 2);

		// ARQUEIRO exige quartel nível 2.
		RegraJogoException ex = assertRegra(() -> quartelService.treinar(usuario.getId(), TipoTropa.ARQUEIRO, 1,
				ModeloItem.ARMADURA_COURO, 1, 2));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.REQUISITO_NAO_ATENDIDO);
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void armaduraModeloDeCategoriaErradaRejeitaComItemIndisponivel() {
		Usuario usuario = criarUsuario("armadura-categoria-errada", "Elisa");
		Vila vila = criarVilaComQuartel(usuario, 1);
		criarItens(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL, 2);

		// ESPADA é categoria ARMA, não ARMADURA.
		RegraJogoException ex = assertRegra(
				() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 1, ModeloItem.ESPADA, 1, 1));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void filaOcupadaRejeitaSegundaOrdemDeTreino() {
		Usuario usuario = criarUsuario("fila-ocupada", "Helena");
		Vila vila = criarVilaComQuartel(usuario, 1);
		criarItens(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL, 1);
		criarItens(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.DISPONIVEL, 1);
		quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 1, ModeloItem.ARMADURA_COURO, 1, 1);

		criarItens(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL, 1);
		criarItens(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.DISPONIVEL, 1);

		RegraJogoException ex = assertRegra(() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 1,
				ModeloItem.ARMADURA_COURO, 1, 1));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.FILA_OCUPADA);
		assertThat(ordemRepository.findByVilaId(vila.getId())).hasSize(1);
	}

	@Test
	void capacidadeExercitoExcedidaRejeitaLoteENadaMuda() {
		Usuario usuario = criarUsuario("capacidade-excedida", "Ines");
		// Quartel N1 -> capacidade 3. 3 unidades já ocupam todos os slots, sem
		// nenhuma ordem TREINO pendente.
		Vila vila = criarVilaComQuartel(usuario, 1);
		for (int i = 0; i < 3; i++) {
			Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.EQUIPADO);
			Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.EQUIPADO);
			criarUnidade(vila.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId());
		}

		List<Item> novasArmas = criarItens(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL, 1);
		criarItens(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.DISPONIVEL, 1);

		RegraJogoException ex = assertRegra(() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 1,
				ModeloItem.ARMADURA_COURO, 1, 1));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.CAPACIDADE_EXERCITO);
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
		assertThat(itemRepository.findById(novasArmas.get(0).getId()).orElseThrow().getStatus())
				.isEqualTo(StatusItem.DISPONIVEL);
	}

	@Test
	void quartelNivel1Com2UnidadesAceitaLoteDeUmAteCapacidade() {
		Usuario usuario = criarUsuario("capacidade-limite", "Julia");
		Vila vila = criarVilaComQuartel(usuario, 1);
		for (int i = 0; i < 2; i++) {
			Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.EQUIPADO);
			Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.EQUIPADO);
			criarUnidade(vila.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId());
		}
		criarItens(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL, 1);
		criarItens(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.DISPONIVEL, 1);

		quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 1, ModeloItem.ARMADURA_COURO, 1, 1);

		assertThat(ordemRepository.findByVilaId(vila.getId())).hasSize(1);
	}

	@Test
	void armasInsuficientesRejeitaComItemIndisponivelENadaMuda() {
		Usuario usuario = criarUsuario("armas-insuficientes", "Bruno");
		Vila vila = criarVilaComQuartel(usuario, 2);
		long comidaInicial = vila.getComida();
		List<Item> armas = criarItens(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL, 3);
		criarItens(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.DISPONIVEL, 4);

		RegraJogoException ex = assertRegra(() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 1,
				ModeloItem.ARMADURA_COURO, 1, 4));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
		for (Item arma : armas) {
			assertThat(itemRepository.findById(arma.getId()).orElseThrow().getStatus())
					.isEqualTo(StatusItem.DISPONIVEL);
		}
		assertThat(vilaService.obterParaAtualizacao(usuario.getId()).getComida()).isEqualTo(comidaInicial);
	}

	@Test
	void armadurasInsuficientesRejeitaComItemIndisponivelENadaMuda() {
		Usuario usuario = criarUsuario("armaduras-insuficientes", "Carla");
		Vila vila = criarVilaComQuartel(usuario, 2);
		criarItens(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL, 4);
		List<Item> armaduras = criarItens(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.DISPONIVEL, 3);

		RegraJogoException ex = assertRegra(() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 1,
				ModeloItem.ARMADURA_COURO, 1, 4));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
		for (Item armadura : armaduras) {
			assertThat(itemRepository.findById(armadura.getId()).orElseThrow().getStatus())
					.isEqualTo(StatusItem.DISPONIVEL);
		}
	}

	@Test
	void armaDeOutraVilaNaoContaParaDisponibilidade() {
		Usuario usuario = criarUsuario("arma-outra-vila", "Fabio");
		Vila vila = criarVilaComQuartel(usuario, 1);
		criarItens(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.DISPONIVEL, 1);

		Usuario outroUsuario = criarUsuario("arma-outra-vila-dono", "Gustavo");
		Vila outraVila = vilaService.obterParaAtualizacao(outroUsuario.getId());
		criarItens(outraVila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL, 5);

		RegraJogoException ex = assertRegra(() -> quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 1,
				ModeloItem.ARMADURA_COURO, 1, 1));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
	}

	@Test
	void comidaInsuficienteRejeitaOrdemENaoAlteraItens() {
		Usuario usuario = criarUsuario("comida-insuficiente", "Karla");
		Vila vila = criarVilaComQuartel(usuario, 3);
		vila.setComida(100_000L); // 100 comida; lote de 2 LANCEIRO custa 120 (60 cada).
		vilaRepository.saveAndFlush(vila);

		List<Item> lancas = criarItens(vila.getId(), ModeloItem.LANCA, 1, StatusItem.DISPONIVEL, 2);
		List<Item> armaduras = criarItens(vila.getId(), ModeloItem.ARMADURA_FERRO, 1, StatusItem.DISPONIVEL, 2);

		RegraJogoException ex = assertRegra(() -> quartelService.treinar(usuario.getId(), TipoTropa.LANCEIRO, 1,
				ModeloItem.ARMADURA_FERRO, 1, 2));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.RECURSOS_INSUFICIENTES);
		for (Item lanca : lancas) {
			assertThat(itemRepository.findById(lanca.getId()).orElseThrow().getStatus())
					.isEqualTo(StatusItem.DISPONIVEL);
		}
		for (Item armadura : armaduras) {
			assertThat(itemRepository.findById(armadura.getId()).orElseThrow().getStatus())
					.isEqualTo(StatusItem.DISPONIVEL);
		}
		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void treinarLoteDeTresReservaItensEDebitaComidaImediatamente() {
		Usuario usuario = criarUsuario("lote-valido", "Lucas");
		Vila vila = criarVilaComQuartel(usuario, 2);
		vila.setComida(200_000L); // 200 comida.
		vilaRepository.saveAndFlush(vila);

		List<Item> armas = criarItens(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL, 3);
		List<Item> armaduras = criarItens(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.DISPONIVEL, 4);

		Ordem ordem = quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 1, ModeloItem.ARMADURA_COURO, 1, 3);

		// SOLDADO custa 50 comida cada: 200 - 150 = 50.
		Vila atualizada = vilaService.obterParaAtualizacao(usuario.getId());
		assertThat(atualizada.getComida()).isEqualTo(50_000L);

		assertThat(ordem.getAlvo()).isEqualTo(TipoTropa.SOLDADO.name());
		assertThat(ordem.getQuantidade()).isEqualTo(3);
		// SOLDADO leva 60s cada: ceil(60 * 3 / 1) = 180s.
		assertThat(ordem.getConcluiEm()).isEqualTo(ordem.getIniciadaEm().plusSeconds(180));

		for (Item arma : armas) {
			Item recarregada = itemRepository.findById(arma.getId()).orElseThrow();
			assertThat(recarregada.getStatus()).isEqualTo(StatusItem.RESERVADO);
			assertThat(recarregada.getOrdemId()).isEqualTo(ordem.getId());
		}
		for (int i = 0; i < 3; i++) {
			Item recarregada = itemRepository.findById(armaduras.get(i).getId()).orElseThrow();
			assertThat(recarregada.getStatus()).isEqualTo(StatusItem.RESERVADO);
			assertThat(recarregada.getOrdemId()).isEqualTo(ordem.getId());
		}
		// A 4ª armadura (não usada) permanece intocada.
		Item armaduraSobra = itemRepository.findById(armaduras.get(3).getId()).orElseThrow();
		assertThat(armaduraSobra.getStatus()).isEqualTo(StatusItem.DISPONIVEL);
		assertThat(armaduraSobra.getOrdemId()).isNull();

		assertThat(unidadeRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void treinarLoteAoAvancarRelogioConcluiOrdemECriaTresUnidadesComNomesSorteados() {
		Usuario usuario = criarUsuario("lote-conclusao", "Marina");
		Vila vila = criarVilaComQuartel(usuario, 2);

		List<Item> armas = criarItens(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL, 3);
		List<Item> armaduras = criarItens(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.DISPONIVEL, 3);

		quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 1, ModeloItem.ARMADURA_COURO, 1, 3);

		relogio.avancar(Duration.ofSeconds(181));
		vilaService.obterParaAtualizacao(usuario.getId());

		assertThat(ordemRepository.findByVilaId(vila.getId())).isEmpty();

		List<Unidade> unidades = unidadeRepository.findByVilaId(vila.getId());
		assertThat(unidades).hasSize(3);
		assertThat(unidades).allSatisfy(unidade -> {
			assertThat(unidade.getTipo()).isEqualTo(TipoTropa.SOLDADO);
			assertThat(unidade.getStatus()).isEqualTo(StatusUnidade.DISPONIVEL);
			assertThat(unidade.getOrdinalNome()).isEqualTo(1);
			assertThat(unidade.getNome()).isNotBlank();
			assertThat(unidade.getSobrenome()).isNotBlank();
		});

		// Cada unidade recebe exatamente 1 arma + 1 armadura do lote reservado.
		List<Long> armaIdsEsperados = armas.stream().map(Item::getId).sorted().toList();
		List<Long> armaduraIdsEsperados = armaduras.stream().map(Item::getId).sorted().toList();
		List<Long> armaIdsObtidos = unidades.stream().map(Unidade::getArmaItemId).sorted().toList();
		List<Long> armaduraIdsObtidos = unidades.stream().map(Unidade::getArmaduraItemId).sorted().toList();
		assertThat(armaIdsObtidos).isEqualTo(armaIdsEsperados);
		assertThat(armaduraIdsObtidos).isEqualTo(armaduraIdsEsperados);

		for (Item arma : armas) {
			Item recarregada = itemRepository.findById(arma.getId()).orElseThrow();
			assertThat(recarregada.getStatus()).isEqualTo(StatusItem.EQUIPADO);
			assertThat(recarregada.getOrdemId()).isNull();
		}
		for (Item armadura : armaduras) {
			assertThat(itemRepository.findById(armadura.getId()).orElseThrow().getStatus())
					.isEqualTo(StatusItem.EQUIPADO);
		}

		// Fila liberada: nova ordem de treino é aceita.
		criarItens(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL, 1);
		criarItens(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.DISPONIVEL, 1);
		quartelService.treinar(usuario.getId(), TipoTropa.SOLDADO, 1, ModeloItem.ARMADURA_COURO, 1, 1);
		assertThat(ordemRepository.findByVilaId(vila.getId())).hasSize(1);
	}

}
