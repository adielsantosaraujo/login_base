package com.example.loginbase.jogo.masmorra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.auditoria.AuditoriaConfig;
import com.example.loginbase.auditoria.UsuarioAuditorAware;
import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RecursoNaoEncontradoException;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.SlotEquipamento;
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.config.Aleatorio;
import com.example.loginbase.jogo.config.AleatorioNomes;
import com.example.loginbase.jogo.config.AleatorioPadrao;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.Batalha;
import com.example.loginbase.jogo.dominio.BatalhaRepository;
import com.example.loginbase.jogo.dominio.ContadorNomeRepository;
import com.example.loginbase.jogo.dominio.EstoqueSemente;
import com.example.loginbase.jogo.dominio.EstoqueSementeRepository;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.ItemRepository;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.StatusBatalha;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.dominio.StatusUnidade;
import com.example.loginbase.jogo.dominio.Unidade;
import com.example.loginbase.jogo.dominio.UnidadeRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.dominio.VilaRepository;
import com.example.loginbase.jogo.economia.AplicadorOrdens;
import com.example.loginbase.jogo.economia.VilaService;
import com.example.loginbase.jogo.masmorra.combate.AcaoCombate;
import com.example.loginbase.jogo.masmorra.combate.Combatente;
import com.example.loginbase.jogo.masmorra.combate.EstadoBatalha;
import com.example.loginbase.jogo.masmorra.combate.Posicao;
import com.example.loginbase.jogo.quartel.EquipamentoService;
import com.example.loginbase.jogo.quartel.GeradorNomes;
import com.example.loginbase.jogo.quartel.NumeradorNomes;
import com.example.loginbase.jogo.suporte.RelogioAjustavel;

/**
 * Testa {@link MasmorraService} contra o Postgres real do
 * {@code docker-compose} (mesma premissa de {@code VilaServiceTest}):
 * criação de batalha com estado tático inicial, persistência de estado/log
 * por ação, vitória com aplicação de loot (respeitando capacidade) e
 * liberação de nível, derrota excluindo unidades/itens, rejeição de ações
 * após o fim, isolamento por usuário (404) e lock otimista via
 * {@code @Version}.
 *
 * <p>
 * Para vitória/derrota, em vez de simular dezenas de turnos reais de
 * movimento/ataque através do {@link com.example.loginbase.jogo.masmorra.combate.MotorCombate},
 * o teste inicia a batalha de verdade (garantindo o mapeamento correto de
 * unidade → combatente) e substitui o {@code estado} persistido por uma
 * variação "a um golpe do fim" — só reposicionando/reduzindo HP dos mesmos
 * combatentes reais — antes de disparar a ação final via
 * {@link MasmorraService#agir}. Isso exercita o desfecho da batalha
 * (loot/liberação/limpeza) sem depender da lógica interna do motor de
 * combate, que já é testada isoladamente em {@code MotorCombateTest}.
 *
 * <p>
 * {@code @Transactional(propagation = NOT_SUPPORTED)}: mesma razão de
 * {@code VilaServiceTest} — {@code VilaService.obterParaAtualizacao} usa uma
 * transação {@code REQUIRES_NEW} genuína para criar a vila, que não
 * enxergaria um usuário apenas "flushado" (não commitado) na transação do
 * teste.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ AuditoriaConfig.class, UsuarioAuditorAware.class, MasmorraServiceTest.MasmorraTestConfig.class,
		JogoProperties.class, VilaService.class, AplicadorOrdens.class, MasmorraService.class,
		EquipamentoService.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MasmorraServiceTest {

	@TestConfiguration
	static class MasmorraTestConfig {

		@Bean
		RelogioAjustavel clock() {
			return new RelogioAjustavel(Instant.parse("2024-01-01T00:00:00Z"));
		}

		@Bean
		AleatorioControlavel aleatorio() {
			return new AleatorioControlavel();
		}

		// AplicadorOrdens (importado abaixo) depende de GeradorNomes desde a task
		// 2.1 e de NumeradorNomes desde a task 2.5; este teste não exercita a
		// conclusão de treino, mas o contexto Spring precisa dos beans para subir.
		@Bean
		AleatorioNomes aleatorioNomes() {
			return new AleatorioNomes(new AleatorioPadrao());
		}

		@Bean
		GeradorNomes geradorNomes(AleatorioNomes aleatorioNomes) {
			return new GeradorNomes(aleatorioNomes);
		}

		@Bean
		NumeradorNomes numeradorNomes(ContadorNomeRepository contadorNomeRepository) {
			return new NumeradorNomes(contadorNomeRepository);
		}

	}

	/**
	 * {@link Aleatorio} de teste cuja sequência é definida por cada teste,
	 * via {@link #usar(Integer...)}, imediatamente antes de disparar a ação
	 * que consome números aleatórios (geração de loot).
	 */
	static class AleatorioControlavel implements Aleatorio {

		private Iterator<Integer> valores = Collections.emptyIterator();

		void usar(Integer... sequencia) {
			this.valores = List.of(sequencia).iterator();
		}

		@Override
		public int proximoInt(int limite) {
			if (!valores.hasNext()) {
				throw new IllegalStateException("AleatorioControlavel esgotado: chame usar(...) antes de consumir.");
			}
			return valores.next();
		}

	}

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private MasmorraService masmorraService;

	@Autowired
	private EquipamentoService equipamentoService;

	@Autowired
	private VilaService vilaService;

	@Autowired
	private VilaRepository vilaRepository;

	@Autowired
	private BatalhaRepository batalhaRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private EstoqueSementeRepository estoqueSementeRepository;

	@Autowired
	private AleatorioControlavel aleatorioControlavel;

	private Usuario criarUsuario(String prefixoEmail, String nome) {
		Usuario usuario = new Usuario();
		usuario.setNome(nome);
		usuario.setEmail(prefixoEmail + "+" + UUID.randomUUID() + "@teste.local");
		usuario.setSenha("senha-hash");
		return usuarioRepository.saveAndFlush(usuario);
	}

	private Unidade criarUnidadeDisponivel(Long vilaId, TipoTropa tipo, ModeloItem modeloArma,
			ModeloItem modeloArmadura, int nivel) {
		Item arma = new Item();
		arma.setVilaId(vilaId);
		arma.setModelo(modeloArma);
		arma.setNivel(nivel);
		arma.setOrigem(OrigemItem.FORJA);
		arma.setStatus(StatusItem.EQUIPADO);
		arma = itemRepository.saveAndFlush(arma);

		Item armadura = new Item();
		armadura.setVilaId(vilaId);
		armadura.setModelo(modeloArmadura);
		armadura.setNivel(nivel);
		armadura.setOrigem(OrigemItem.FORJA);
		armadura.setStatus(StatusItem.EQUIPADO);
		armadura = itemRepository.saveAndFlush(armadura);

		Unidade unidade = new Unidade();
		unidade.setVilaId(vilaId);
		unidade.setTipo(tipo);
		unidade.setArmaItemId(arma.getId());
		unidade.setArmaduraItemId(armadura.getId());
		unidade.setStatus(StatusUnidade.DISPONIVEL);
		// nome/sobrenome únicos por chamada (derivados do id da arma, sempre novo
		// nesses testes) para não colidir com a unique constraint da vila.
		unidade.setNome("Soldado" + arma.getId());
		unidade.setSobrenome("Teste");
		unidade.setOrdinalNome(1);
		return unidadeRepository.saveAndFlush(unidade);
	}

	private EstadoBatalha lerEstado(Batalha batalha) {
		return OBJECT_MAPPER.readValue(batalha.getEstado(), EstadoBatalha.class);
	}

	private void definirEstado(Batalha batalha, EstadoBatalha estado) {
		batalha.setEstado(OBJECT_MAPPER.writeValueAsString(estado));
		batalhaRepository.saveAndFlush(batalha);
	}

	// ---- iniciar ----

	@Test
	void iniciarCriaBatalhaComEstadoInicialCorreto() {
		Usuario usuario = criarUsuario("iniciar", "Joana");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Unidade u1 = criarUnidadeDisponivel(vila.getId(), TipoTropa.SOLDADO, ModeloItem.ESPADA,
				ModeloItem.ARMADURA_COURO, 1);
		Unidade u2 = criarUnidadeDisponivel(vila.getId(), TipoTropa.ARQUEIRO, ModeloItem.ARCO,
				ModeloItem.ARMADURA_COURO, 1);

		Batalha batalha = masmorraService.iniciar(usuario.getId(), 1, List.of(u1.getId(), u2.getId()));

		assertThat(batalha.getId()).isNotNull();
		assertThat(batalha.getStatus()).isEqualTo(StatusBatalha.EM_ANDAMENTO);
		assertThat(batalha.getTurno()).isEqualTo(1);
		assertThat(batalha.getMasmorraNivel()).isEqualTo(1);
		assertThat(batalha.getVersion()).isZero();
		assertThat(batalha.getFinalizadaEm()).isNull();
		assertThat(batalha.getLoot()).isNull();
		assertThat(batalha.getLog()).contains("Turno 1 começou.");

		EstadoBatalha estado = lerEstado(batalha);
		assertThat(estado.turno()).isEqualTo(1);
		assertThat(estado.resultado()).isEqualTo(EstadoBatalha.Resultado.NULO);
		assertThat(estado.combatentes()).hasSize(5); // 2 do jogador + 3 goblins (nível 1)

		Combatente j1 = estado.buscar("J1").orElseThrow();
		assertThat(j1.posicao()).isEqualTo(new Posicao(2, 7));
		assertThat(j1.hp()).isEqualTo(TipoTropa.SOLDADO.hp());
		Combatente j2 = estado.buscar("J2").orElseThrow();
		assertThat(j2.posicao()).isEqualTo(new Posicao(3, 7));

		Combatente i1 = estado.buscar("I1").orElseThrow();
		assertThat(i1.posicao()).isEqualTo(new Posicao(3, 0));
		assertThat(i1.tipoOuUnidade()).isEqualTo("GOBLIN");

		assertThat(unidadeRepository.findById(u1.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusUnidade.EM_MASMORRA);
		assertThat(unidadeRepository.findById(u2.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusUnidade.EM_MASMORRA);
	}

	@Test
	void iniciarRejeitaMasmorraBloqueada() {
		Usuario usuario = criarUsuario("bloqueada", "Karla");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Unidade unidade = criarUnidadeDisponivel(vila.getId(), TipoTropa.SOLDADO, ModeloItem.ESPADA,
				ModeloItem.ARMADURA_COURO, 1);

		assertThatThrownBy(() -> masmorraService.iniciar(usuario.getId(), 2, List.of(unidade.getId())))
				.isInstanceOf(RegraJogoException.class)
				.extracting(e -> ((RegraJogoException) e).getCodigo())
				.isEqualTo(CodigoErro.MASMORRA_BLOQUEADA);
	}

	@Test
	void iniciarRejeitaEsquadraoVazioOuMaiorQueQuatro() {
		Usuario usuario = criarUsuario("esquadrao", "Lucas");
		vilaService.obterParaAtualizacao(usuario.getId());

		assertThatThrownBy(() -> masmorraService.iniciar(usuario.getId(), 1, List.of()))
				.isInstanceOf(RegraJogoException.class)
				.extracting(e -> ((RegraJogoException) e).getCodigo())
				.isEqualTo(CodigoErro.ESQUADRAO_INVALIDO);

		assertThatThrownBy(() -> masmorraService.iniciar(usuario.getId(), 1, List.of(1L, 2L, 3L, 4L, 5L)))
				.isInstanceOf(RegraJogoException.class)
				.extracting(e -> ((RegraJogoException) e).getCodigo())
				.isEqualTo(CodigoErro.ESQUADRAO_INVALIDO);
	}

	@Test
	void iniciarRejeitaUnidadeIndisponivel() {
		Usuario usuario = criarUsuario("unidade-indisponivel", "Marcia");
		vilaService.obterParaAtualizacao(usuario.getId());

		assertThatThrownBy(() -> masmorraService.iniciar(usuario.getId(), 1, List.of(999_999_999L)))
				.isInstanceOf(RegraJogoException.class)
				.extracting(e -> ((RegraJogoException) e).getCodigo())
				.isEqualTo(CodigoErro.UNIDADE_INDISPONIVEL);
	}

	@Test
	void iniciarRejeitaSegundaBatalhaEmAndamento() {
		Usuario usuario = criarUsuario("batalha-andamento", "Nadia");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Unidade u1 = criarUnidadeDisponivel(vila.getId(), TipoTropa.SOLDADO, ModeloItem.ESPADA,
				ModeloItem.ARMADURA_COURO, 1);
		Unidade u2 = criarUnidadeDisponivel(vila.getId(), TipoTropa.SOLDADO, ModeloItem.ESPADA,
				ModeloItem.ARMADURA_COURO, 1);

		masmorraService.iniciar(usuario.getId(), 1, List.of(u1.getId()));

		assertThatThrownBy(() -> masmorraService.iniciar(usuario.getId(), 1, List.of(u2.getId())))
				.isInstanceOf(RegraJogoException.class)
				.extracting(e -> ((RegraJogoException) e).getCodigo())
				.isEqualTo(CodigoErro.BATALHA_EM_ANDAMENTO);
	}

	// ---- agir: persistência de estado/log ----

	@Test
	void agirPersisteEstadoELogAposCadaAcaoEIncrementaVersion() {
		Usuario usuario = criarUsuario("persistencia", "Otavio");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Unidade unidade = criarUnidadeDisponivel(vila.getId(), TipoTropa.SOLDADO, ModeloItem.ESPADA,
				ModeloItem.ARMADURA_COURO, 1);

		Batalha batalha = masmorraService.iniciar(usuario.getId(), 1, List.of(unidade.getId()));

		Batalha depoisMover = masmorraService.agir(usuario.getId(), batalha.getId(),
				AcaoCombate.mover("J1", 2, 4, 1));

		assertThat(depoisMover.getVersion()).isEqualTo(1);
		assertThat(depoisMover.getLog()).contains("J1 moveu para (2,4).");
		EstadoBatalha estado = lerEstado(depoisMover);
		assertThat(estado.buscar("J1").orElseThrow().posicao()).isEqualTo(new Posicao(2, 4));

		Batalha consultada = masmorraService.consultar(usuario.getId(), batalha.getId());
		assertThat(consultada.getEstado()).isEqualTo(depoisMover.getEstado());
		assertThat(consultada.getLog()).isEqualTo(depoisMover.getLog());
	}

	// ---- agir: vitória com loot e liberação de nível ----

	@Test
	void agirNaVitoriaAplicaLootRespeitandoCapacidadeELiberaProximoNivel() {
		Usuario usuario = criarUsuario("vitoria", "Paula");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Unidade unidade = criarUnidadeDisponivel(vila.getId(), TipoTropa.SOLDADO, ModeloItem.ESPADA,
				ModeloItem.ARMADURA_COURO, 1);

		Batalha batalha = masmorraService.iniciar(usuario.getId(), 1, List.of(unidade.getId()));
		EstadoBatalha original = lerEstado(batalha);
		Combatente j1Original = original.buscar("J1").orElseThrow();
		Combatente i1Original = original.buscar("I1").orElseThrow();

		// Estado "a um golpe do fim": só resta I1, com 1 HP, adjacente a J1
		// (os demais combatentes originais são descartados, simulando que já
		// morreram em turnos anteriores).
		Combatente j1Pronto = new Combatente(j1Original.id(), j1Original.lado(), j1Original.tipoOuUnidade(),
				new Posicao(0, 0), j1Original.hpMax(), j1Original.hpMax(), j1Original.ataque(), j1Original.defesa(),
				j1Original.alcance(), j1Original.movimento(), false, false, false, true);
		Combatente i1QuaseMorto = new Combatente(i1Original.id(), i1Original.lado(), i1Original.tipoOuUnidade(),
				new Posicao(0, 1), 1, i1Original.hpMax(), i1Original.ataque(), i1Original.defesa(),
				i1Original.alcance(), i1Original.movimento(), false, false, false, true);
		EstadoBatalha estadoPronto = new EstadoBatalha(List.of(j1Pronto, i1QuaseMorto), 1, 30, original.mapa(),
				EstadoBatalha.Resultado.NULO, original.log());
		definirEstado(batalha, estadoPronto);

		// Comida perto da capacidade do armazém N1 (500 => 500_000 milésimos)
		// para verificar o corte por capacidade no loot garantido (+40_000).
		Vila vilaAntes = vilaRepository.findByUsuarioId(usuario.getId()).orElseThrow();
		vilaAntes.setComida(480_000L);
		vilaRepository.saveAndFlush(vilaAntes);

		// Sequência determinística (idêntica ao exemplo do design/spec de
		// loot): 1ª rolagem -> semente MILHO; 2ª rolagem -> item ESPADA nível 2.
		aleatorioControlavel.usar(10, 0, 70, 0, 1);

		Batalha resultado = masmorraService.agir(usuario.getId(), batalha.getId(),
				AcaoCombate.atacar("J1", "I1", 1));

		assertThat(resultado.getStatus()).isEqualTo(StatusBatalha.VITORIA);
		assertThat(resultado.getFinalizadaEm()).isNotNull();
		assertThat(resultado.getLoot()).isNotNull();
		assertThat(resultado.getVersion()).isEqualTo(2); // 0 -> 1 (definirEstado) -> 2 (agir)

		Vila vilaDepois = vilaRepository.findByUsuarioId(usuario.getId()).orElseThrow();
		assertThat(vilaDepois.getComida()).isEqualTo(500_000L); // 480_000 + 40_000 > cap: cortado em 500_000
		assertThat(vilaDepois.getMadeira()).isEqualTo(400_000L + 50_000L);
		assertThat(vilaDepois.getPedra()).isEqualTo(300_000L + 50_000L);
		assertThat(vilaDepois.getFerro()).isEqualTo(50_000L + 20_000L);
		assertThat(vilaDepois.getMasmorraNivelLiberado()).isEqualTo(2);

		List<EstoqueSemente> sementes = estoqueSementeRepository.findByVilaId(vilaDepois.getId());
		assertThat(sementes).hasSize(1);
		assertThat(sementes.get(0).getCultivo()).isEqualTo(Cultivo.MILHO);
		assertThat(sementes.get(0).getQuantidade()).isEqualTo(1);

		List<Item> itensDeLoot = itemRepository.findByVilaId(vilaDepois.getId()).stream()
				.filter(i -> i.getOrigem() == OrigemItem.MASMORRA)
				.toList();
		assertThat(itensDeLoot).hasSize(1);
		assertThat(itensDeLoot.get(0).getModelo()).isEqualTo(ModeloItem.ESPADA);
		assertThat(itensDeLoot.get(0).getNivel()).isEqualTo(2);
		assertThat(itensDeLoot.get(0).getStatus()).isEqualTo(StatusItem.DISPONIVEL);

		Unidade unidadeDepois = unidadeRepository.findById(unidade.getId()).orElseThrow();
		assertThat(unidadeDepois.getStatus()).isEqualTo(StatusUnidade.DISPONIVEL);
	}

	// ---- agir: derrota excluindo unidades/itens ----

	@Test
	void agirNaDerrotaExcluiUnidadesEItensERejeitaAcoesAposFim() {
		Usuario usuario = criarUsuario("derrota", "Rafael");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Unidade unidade = criarUnidadeDisponivel(vila.getId(), TipoTropa.SOLDADO, ModeloItem.ESPADA,
				ModeloItem.ARMADURA_COURO, 1);
		Long armaId = unidade.getArmaItemId();
		Long armaduraId = unidade.getArmaduraItemId();

		Batalha batalha = masmorraService.iniciar(usuario.getId(), 1, List.of(unidade.getId()));
		EstadoBatalha original = lerEstado(batalha);
		Combatente j1Original = original.buscar("J1").orElseThrow();
		Combatente i1Original = original.buscar("I1").orElseThrow();

		Combatente j1QuaseMorto = new Combatente(j1Original.id(), j1Original.lado(), j1Original.tipoOuUnidade(),
				new Posicao(0, 0), 1, j1Original.hpMax(), j1Original.ataque(), j1Original.defesa(),
				j1Original.alcance(), j1Original.movimento(), false, false, false, true);
		Combatente i1Pronto = new Combatente(i1Original.id(), i1Original.lado(), i1Original.tipoOuUnidade(),
				new Posicao(0, 1), i1Original.hpMax(), i1Original.hpMax(), i1Original.ataque(), i1Original.defesa(),
				i1Original.alcance(), i1Original.movimento(), false, false, false, true);
		EstadoBatalha estadoPronto = new EstadoBatalha(List.of(j1QuaseMorto, i1Pronto), 1, 30, original.mapa(),
				EstadoBatalha.Resultado.NULO, original.log());
		definirEstado(batalha, estadoPronto);

		Batalha resultado = masmorraService.agir(usuario.getId(), batalha.getId(), AcaoCombate.encerrarTurno(1));

		assertThat(resultado.getStatus()).isEqualTo(StatusBatalha.DERROTA);
		assertThat(resultado.getFinalizadaEm()).isNotNull();
		assertThat(resultado.getLoot()).isNull();

		assertThat(unidadeRepository.findById(unidade.getId())).isEmpty();
		assertThat(itemRepository.findById(armaId)).isEmpty();
		assertThat(itemRepository.findById(armaduraId)).isEmpty();

		Vila vilaDepois = vilaRepository.findByUsuarioId(usuario.getId()).orElseThrow();
		assertThat(vilaDepois.getMasmorraNivelLiberado()).isEqualTo(1);

		assertThatThrownBy(() -> masmorraService.agir(usuario.getId(), batalha.getId(), AcaoCombate.encerrarTurno(1)))
				.isInstanceOf(RegraJogoException.class)
				.extracting(e -> ((RegraJogoException) e).getCodigo())
				.isEqualTo(CodigoErro.BATALHA_ENCERRADA);
	}

	/**
	 * Cobre a iteração por {@link com.example.loginbase.jogo.catalogo.SlotEquipamento}
	 * (task 2.3): com duas unidades mortas, os itens dos dois slots com dados
	 * (arma e armadura) de cada uma são destruídos, e a passagem pelos 7
	 * slots futuros (ainda sem persistência) não lança erro.
	 */
	@Test
	void agirNaDerrotaDestroiItensDeTodosOsSlotsOcupadosDoEsquadraoSemErroNosSlotsFuturos() {
		Usuario usuario = criarUsuario("derrota-slots", "Sonia");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Unidade u1 = criarUnidadeDisponivel(vila.getId(), TipoTropa.SOLDADO, ModeloItem.ESPADA,
				ModeloItem.ARMADURA_COURO, 1);
		Unidade u2 = criarUnidadeDisponivel(vila.getId(), TipoTropa.ARQUEIRO, ModeloItem.ARCO,
				ModeloItem.ARMADURA_COURO, 1);
		Long armaId1 = u1.getArmaItemId();
		Long armaduraId1 = u1.getArmaduraItemId();
		Long armaId2 = u2.getArmaItemId();
		Long armaduraId2 = u2.getArmaduraItemId();

		Batalha batalha = masmorraService.iniciar(usuario.getId(), 1, List.of(u1.getId(), u2.getId()));
		EstadoBatalha original = lerEstado(batalha);
		Combatente j1Original = original.buscar("J1").orElseThrow();
		Combatente j2Original = original.buscar("J2").orElseThrow();
		Combatente i1Original = original.buscar("I1").orElseThrow();
		Combatente i2Original = original.buscar("I2").orElseThrow();

		// J1 e J2 a um golpe do fim (1 HP cada); I1 e I2 (goblins, alcance 1)
		// posicionados um adjacente a cada um, cada qual mirando o jogador
		// mais próximo (regra de escolherAlvo) — um único encerrarTurno mata
		// as duas unidades do jogador de uma vez.
		Combatente j1QuaseMorto = new Combatente(j1Original.id(), j1Original.lado(), j1Original.tipoOuUnidade(),
				new Posicao(0, 0), 1, j1Original.hpMax(), j1Original.ataque(), j1Original.defesa(),
				j1Original.alcance(), j1Original.movimento(), false, false, false, true);
		Combatente j2QuaseMorto = new Combatente(j2Original.id(), j2Original.lado(), j2Original.tipoOuUnidade(),
				new Posicao(0, 3), 1, j2Original.hpMax(), j2Original.ataque(), j2Original.defesa(),
				j2Original.alcance(), j2Original.movimento(), false, false, false, true);
		Combatente i1Pronto = new Combatente(i1Original.id(), i1Original.lado(), i1Original.tipoOuUnidade(),
				new Posicao(0, 1), i1Original.hpMax(), i1Original.hpMax(), i1Original.ataque(), i1Original.defesa(),
				i1Original.alcance(), i1Original.movimento(), false, false, false, true);
		Combatente i2Pronto = new Combatente(i2Original.id(), i2Original.lado(), i2Original.tipoOuUnidade(),
				new Posicao(0, 2), i2Original.hpMax(), i2Original.hpMax(), i2Original.ataque(), i2Original.defesa(),
				i2Original.alcance(), i2Original.movimento(), false, false, false, true);
		EstadoBatalha estadoPronto = new EstadoBatalha(List.of(j1QuaseMorto, j2QuaseMorto, i1Pronto, i2Pronto), 1, 30,
				original.mapa(), EstadoBatalha.Resultado.NULO, original.log());
		definirEstado(batalha, estadoPronto);

		Batalha resultado = masmorraService.agir(usuario.getId(), batalha.getId(), AcaoCombate.encerrarTurno(1));

		assertThat(resultado.getStatus()).isEqualTo(StatusBatalha.DERROTA);

		assertThat(unidadeRepository.findById(u1.getId())).isEmpty();
		assertThat(unidadeRepository.findById(u2.getId())).isEmpty();
		assertThat(itemRepository.findById(armaId1)).isEmpty();
		assertThat(itemRepository.findById(armaduraId1)).isEmpty();
		assertThat(itemRepository.findById(armaId2)).isEmpty();
		assertThat(itemRepository.findById(armaduraId2)).isEmpty();
	}

	/**
	 * Cobre a spec game-army — Requirement: Troca de equipamento, cenário
	 * "Morte após troca" (task 2.6): a unidade troca a arma antes de entrar na
	 * masmorra; ao morrer, a arma nova (agora equipada) é destruída, mas a
	 * arma antiga — já {@code DISPONIVEL} desde a troca — permanece intocada
	 * no inventário.
	 */
	@Test
	void agirNaDerrotaAposTrocaDeEquipamentoDestroiApenasOItemNovoEMantemOAntigoDisponivel() {
		Usuario usuario = criarUsuario("derrota-apos-troca", "Tania");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Unidade unidade = criarUnidadeDisponivel(vila.getId(), TipoTropa.SOLDADO, ModeloItem.ESPADA,
				ModeloItem.ARMADURA_COURO, 1);
		Long armaAntigaId = unidade.getArmaItemId();
		Long armaduraId = unidade.getArmaduraItemId();

		Item armaNova = new Item();
		armaNova.setVilaId(vila.getId());
		armaNova.setModelo(ModeloItem.ESPADA);
		armaNova.setNivel(2);
		armaNova.setOrigem(OrigemItem.FORJA);
		armaNova.setStatus(StatusItem.DISPONIVEL);
		armaNova = itemRepository.saveAndFlush(armaNova);
		Long armaNovaId = armaNova.getId();

		equipamentoService.trocar(usuario.getId(), unidade.getId(), SlotEquipamento.ARMA, armaNovaId);
		assertThat(itemRepository.findById(armaAntigaId).orElseThrow().getStatus()).isEqualTo(StatusItem.DISPONIVEL);
		assertThat(unidadeRepository.findById(unidade.getId()).orElseThrow().getArmaItemId()).isEqualTo(armaNovaId);

		Batalha batalha = masmorraService.iniciar(usuario.getId(), 1, List.of(unidade.getId()));
		EstadoBatalha original = lerEstado(batalha);
		Combatente j1Original = original.buscar("J1").orElseThrow();
		Combatente i1Original = original.buscar("I1").orElseThrow();

		Combatente j1QuaseMorto = new Combatente(j1Original.id(), j1Original.lado(), j1Original.tipoOuUnidade(),
				new Posicao(0, 0), 1, j1Original.hpMax(), j1Original.ataque(), j1Original.defesa(),
				j1Original.alcance(), j1Original.movimento(), false, false, false, true);
		Combatente i1Pronto = new Combatente(i1Original.id(), i1Original.lado(), i1Original.tipoOuUnidade(),
				new Posicao(0, 1), i1Original.hpMax(), i1Original.hpMax(), i1Original.ataque(), i1Original.defesa(),
				i1Original.alcance(), i1Original.movimento(), false, false, false, true);
		EstadoBatalha estadoPronto = new EstadoBatalha(List.of(j1QuaseMorto, i1Pronto), 1, 30, original.mapa(),
				EstadoBatalha.Resultado.NULO, original.log());
		definirEstado(batalha, estadoPronto);

		Batalha resultado = masmorraService.agir(usuario.getId(), batalha.getId(), AcaoCombate.encerrarTurno(1));

		assertThat(resultado.getStatus()).isEqualTo(StatusBatalha.DERROTA);
		assertThat(unidadeRepository.findById(unidade.getId())).isEmpty();
		assertThat(itemRepository.findById(armaNovaId)).isEmpty();
		assertThat(itemRepository.findById(armaduraId)).isEmpty();
		assertThat(itemRepository.findById(armaAntigaId).orElseThrow().getStatus()).isEqualTo(StatusItem.DISPONIVEL);
	}

	// ---- isolamento por usuário (404) ----

	@Test
	void consultarEAgirRejeitamBatalhaDeOutraVila() {
		Usuario dono = criarUsuario("dono", "Helena");
		Vila vilaDono = vilaService.obterParaAtualizacao(dono.getId());
		Unidade unidadeDono = criarUnidadeDisponivel(vilaDono.getId(), TipoTropa.SOLDADO, ModeloItem.ESPADA,
				ModeloItem.ARMADURA_COURO, 1);
		Batalha batalha = masmorraService.iniciar(dono.getId(), 1, List.of(unidadeDono.getId()));

		Usuario outro = criarUsuario("outro", "Igor");
		vilaService.obterParaAtualizacao(outro.getId());

		assertThatThrownBy(() -> masmorraService.consultar(outro.getId(), batalha.getId()))
				.isInstanceOf(RecursoNaoEncontradoException.class);

		assertThatThrownBy(
				() -> masmorraService.agir(outro.getId(), batalha.getId(), AcaoCombate.encerrarTurno(1)))
				.isInstanceOf(RecursoNaoEncontradoException.class);

		Batalha vistaPeloDono = masmorraService.consultar(dono.getId(), batalha.getId());
		assertThat(vistaPeloDono.getId()).isEqualTo(batalha.getId());
	}

	// ---- lock otimista ----

	@Test
	void salvarBatalhaComVersaoDesatualizadaLancaConflito() {
		Usuario usuario = criarUsuario("lock-otimista", "Simone");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Unidade unidade = criarUnidadeDisponivel(vila.getId(), TipoTropa.SOLDADO, ModeloItem.ESPADA,
				ModeloItem.ARMADURA_COURO, 1);
		Batalha batalha = masmorraService.iniciar(usuario.getId(), 1, List.of(unidade.getId()));

		Batalha copia1 = batalhaRepository.findById(batalha.getId()).orElseThrow();
		Batalha copia2 = batalhaRepository.findById(batalha.getId()).orElseThrow();

		copia1.setLog(copia1.getLog() + "\nEditado por copia1.");
		batalhaRepository.saveAndFlush(copia1);

		copia2.setLog(copia2.getLog() + "\nEditado por copia2 (desatualizada).");
		assertThatThrownBy(() -> batalhaRepository.saveAndFlush(copia2))
				.isInstanceOf(ObjectOptimisticLockingFailureException.class);
	}

}
