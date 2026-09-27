package com.example.loginbase.jogo.economia;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.Canteiro;
import com.example.loginbase.jogo.dominio.CanteiroRepository;
import com.example.loginbase.jogo.dominio.CategoriaOrdem;
import com.example.loginbase.jogo.dominio.EstoqueSementeRepository;
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
import com.example.loginbase.jogo.suporte.JogoTestConfig;
import com.example.loginbase.jogo.suporte.RelogioAjustavel;

/**
 * Testa {@link VilaService} e {@link AplicadorOrdens} contra o Postgres real
 * do {@code docker-compose} (mesma premissa de {@code RepositoriosJogoTest}):
 * criação da vila com estado inicial, idempotência, cálculo preguiçoso de
 * produção (inclusive em trechos com taxas diferentes) e aplicação dos
 * efeitos de ordens vencidas (construção, fazenda, forja e treino) na
 * sincronização.
 *
 * <p>{@code obterParaAtualizacao} cria a vila com o usuário travado (lock
 * pessimista) já coberto isoladamente por
 * {@code RepositoriosJogoTest.deveBuscarVilaPorUsuarioComLockPessimista}; aqui
 * validamos que chamadas repetidas nunca duplicam a vila, que é o
 * comportamento observável do lock combinado com a criação em
 * {@code REQUIRES_NEW}.
 *
 * <p>{@code @Transactional(propagation = NOT_SUPPORTED)} desliga a
 * transação única que {@code @DataJpaTest} normalmente aplica ao método de
 * teste (com rollback ao final): aqui cada chamada a repositório/serviço
 * gerencia sua própria transação, como em produção — necessário porque
 * {@code VilaService} usa uma transação {@code REQUIRES_NEW} genuína para
 * criar a vila, que não enxergaria um usuário de setup apenas "flushado" (e
 * não commitado) na transação do teste.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ AuditoriaConfig.class, UsuarioAuditorAware.class, JogoTestConfig.class, JogoProperties.class,
		VilaService.class, AplicadorOrdens.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class VilaServiceTest {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private VilaService vilaService;

	@Autowired
	private VilaRepository vilaRepository;

	@Autowired
	private PredioRepository predioRepository;

	@Autowired
	private CanteiroRepository canteiroRepository;

	@Autowired
	private EstoqueSementeRepository estoqueSementeRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	@Autowired
	private OrdemRepository ordemRepository;

	@Autowired
	private RelogioAjustavel relogio;

	/**
	 * @param prefixoEmail identificador curto do cenário (ex.: {@code "criacao"}); um sufixo
	 *                     único é anexado para que reexecuções do teste — cujos dados ficam
	 *                     commitados de verdade, sem rollback (ver Javadoc da classe) — nunca
	 *                     colidam com a unique constraint de e-mail
	 */
	private Usuario criarUsuario(String prefixoEmail, String nome) {
		Usuario usuario = new Usuario();
		usuario.setNome(nome);
		usuario.setEmail(prefixoEmail + "+" + java.util.UUID.randomUUID() + "@teste.local");
		usuario.setSenha("senha-hash");
		// saveAndFlush roda em sua própria transação (commitada), diferente de
		// TestEntityManager.persistFlushFind: precisa estar visível para a
		// transação REQUIRES_NEW de VilaService.criarSeNaoExiste.
		return usuarioRepository.saveAndFlush(usuario);
	}

	@Test
	void primeiraChamadaCriaVilaComEstadoInicial() {
		Usuario usuario = criarUsuario("criacao", "Ana");

		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());

		assertThat(vila.getNome()).isEqualTo("Vila de Ana");
		assertThat(vila.getComida()).isEqualTo(300_000L);
		assertThat(vila.getMadeira()).isEqualTo(400_000L);
		assertThat(vila.getPedra()).isEqualTo(300_000L);
		assertThat(vila.getFerro()).isEqualTo(50_000L);
		assertThat(vila.getMasmorraNivelLiberado()).isEqualTo(1);
		assertThat(vila.getRecursosAtualizadosEm()).isEqualTo(relogio.instant());

		Map<TipoPredio, Integer> niveis = predioRepository.findByVilaId(vila.getId()).stream()
				.collect(Collectors.toMap(Predio::getTipo, Predio::getNivel));
		assertThat(niveis)
				.containsEntry(TipoPredio.CENTRO_VILA, 1)
				.containsEntry(TipoPredio.ARMAZEM, 1)
				.containsEntry(TipoPredio.FAZENDA, 1)
				.containsEntry(TipoPredio.SERRARIA, 1)
				.containsEntry(TipoPredio.PEDREIRA, 1)
				.containsEntry(TipoPredio.MINA_FERRO, 0)
				.containsEntry(TipoPredio.FORJA, 0)
				.containsEntry(TipoPredio.QUARTEL, 0);

		List<Canteiro> canteiros = canteiroRepository.findByVilaId(vila.getId());
		assertThat(canteiros).hasSize(1);
		assertThat(canteiros.get(0).getPosicao()).isEqualTo(1);
		assertThat(canteiros.get(0).getCultivo()).isEqualTo(Cultivo.TRIGO);

		assertThat(estoqueSementeRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void segundaChamadaRetornaMesmaVilaSemDuplicar() {
		Usuario usuario = criarUsuario("idempotencia", "Bruno");

		Vila primeira = vilaService.obterParaAtualizacao(usuario.getId());
		Vila segunda = vilaService.obterParaAtualizacao(usuario.getId());

		assertThat(segunda.getId()).isEqualTo(primeira.getId());
		assertThat(predioRepository.findByVilaId(primeira.getId())).hasSize(8);
		assertThat(vilaRepository.findByUsuarioId(usuario.getId())).isPresent();
	}

	@Test
	void producaoAposDuasHorasSemRequisicoesAcumulaMadeiraDaSerraria() {
		Usuario usuario = criarUsuario("producao", "Carla");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		long madeiraInicial = vila.getMadeira();

		relogio.avancar(Duration.ofHours(2));
		Vila atualizada = vilaService.obterParaAtualizacao(usuario.getId());

		// Serraria N1 = 30/h; 2h sem requisições = +60 (em milésimos: +60_000).
		assertThat(atualizada.getMadeira()).isEqualTo(madeiraInicial + 60_000L);
		assertThat(atualizada.getRecursosAtualizadosEm()).isEqualTo(relogio.instant());
	}

	@Test
	void ordemDeConstrucaoVencidaEAplicadaNaSincronizacao() {
		Usuario usuario = criarUsuario("ordem-construcao", "Diego");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());

		Instant agora = relogio.instant();
		Ordem ordem = new Ordem();
		ordem.setVilaId(vila.getId());
		ordem.setCategoria(CategoriaOrdem.CONSTRUCAO);
		ordem.setAlvo(TipoPredio.ARMAZEM.name());
		ordem.setNivel(2);
		ordem.setQuantidade(1);
		ordem.setIniciadaEm(agora);
		ordem.setConcluiEm(agora.plusSeconds(60));
		ordemRepository.saveAndFlush(ordem);

		relogio.avancar(Duration.ofMinutes(2));
		Vila atualizada = vilaService.obterParaAtualizacao(usuario.getId());

		Map<TipoPredio, Integer> niveis = predioRepository.findByVilaId(atualizada.getId()).stream()
				.collect(Collectors.toMap(Predio::getTipo, Predio::getNivel));
		assertThat(niveis.get(TipoPredio.ARMAZEM)).isEqualTo(2);
		assertThat(ordemRepository.findByVilaId(atualizada.getId())).isEmpty();
	}

	@Test
	void ordemDeConstrucaoDeFazendaCriaNovoCanteiroComTrigo() {
		Usuario usuario = criarUsuario("ordem-fazenda", "Gabriela");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());

		Instant agora = relogio.instant();
		Ordem ordem = new Ordem();
		ordem.setVilaId(vila.getId());
		ordem.setCategoria(CategoriaOrdem.CONSTRUCAO);
		ordem.setAlvo(TipoPredio.FAZENDA.name());
		ordem.setNivel(2);
		ordem.setQuantidade(1);
		ordem.setIniciadaEm(agora);
		ordem.setConcluiEm(agora.plusSeconds(60));
		ordemRepository.saveAndFlush(ordem);

		relogio.avancar(Duration.ofMinutes(2));
		vilaService.obterParaAtualizacao(usuario.getId());

		List<Canteiro> canteiros = canteiroRepository.findByVilaId(vila.getId());
		assertThat(canteiros).hasSize(2);
		assertThat(canteiros).extracting(Canteiro::getPosicao).containsExactlyInAnyOrder(1, 2);
		assertThat(canteiros).extracting(Canteiro::getCultivo).containsOnly(Cultivo.TRIGO);
	}

	@Test
	void ordemDeForjaVencidaCriaItensDisponiveis() {
		Usuario usuario = criarUsuario("ordem-forja", "Hugo");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());

		Instant agora = relogio.instant();
		Ordem ordem = new Ordem();
		ordem.setVilaId(vila.getId());
		ordem.setCategoria(CategoriaOrdem.FORJA);
		ordem.setAlvo(ModeloItem.ESPADA.name());
		ordem.setNivel(1);
		ordem.setQuantidade(3);
		ordem.setIniciadaEm(agora);
		ordem.setConcluiEm(agora.plusSeconds(60));
		ordemRepository.saveAndFlush(ordem);

		relogio.avancar(Duration.ofMinutes(2));
		vilaService.obterParaAtualizacao(usuario.getId());

		List<Item> itens = itemRepository.findByVilaId(vila.getId());
		assertThat(itens).hasSize(3);
		assertThat(itens).allSatisfy(item -> {
			assertThat(item.getModelo()).isEqualTo(ModeloItem.ESPADA);
			assertThat(item.getOrigem()).isEqualTo(OrigemItem.FORJA);
			assertThat(item.getStatus()).isEqualTo(StatusItem.DISPONIVEL);
		});
	}

	@Test
	void ordemDeTreinoVencidaCriaUnidadeEEquipaArmaEArmadura() {
		Usuario usuario = criarUsuario("ordem-treino", "Ines");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());

		Item arma = new Item();
		arma.setVilaId(vila.getId());
		arma.setModelo(ModeloItem.ESPADA);
		arma.setNivel(1);
		arma.setOrigem(OrigemItem.FORJA);
		arma.setStatus(StatusItem.DISPONIVEL);
		arma = itemRepository.saveAndFlush(arma);

		Item armadura = new Item();
		armadura.setVilaId(vila.getId());
		armadura.setModelo(ModeloItem.ARMADURA_COURO);
		armadura.setNivel(1);
		armadura.setOrigem(OrigemItem.FORJA);
		armadura.setStatus(StatusItem.DISPONIVEL);
		armadura = itemRepository.saveAndFlush(armadura);

		Instant agora = relogio.instant();
		Ordem ordem = new Ordem();
		ordem.setVilaId(vila.getId());
		ordem.setCategoria(CategoriaOrdem.TREINO);
		ordem.setAlvo(TipoTropa.SOLDADO.name());
		ordem.setQuantidade(1);
		ordem.setArmaItemId(arma.getId());
		ordem.setArmaduraItemId(armadura.getId());
		ordem.setIniciadaEm(agora);
		ordem.setConcluiEm(agora.plusSeconds(60));
		ordemRepository.saveAndFlush(ordem);

		relogio.avancar(Duration.ofMinutes(2));
		vilaService.obterParaAtualizacao(usuario.getId());

		List<Unidade> unidades = unidadeRepository.findByVilaId(vila.getId());
		assertThat(unidades).hasSize(1);
		assertThat(unidades.get(0).getTipo()).isEqualTo(TipoTropa.SOLDADO);
		assertThat(unidades.get(0).getStatus()).isEqualTo(StatusUnidade.DISPONIVEL);

		assertThat(itemRepository.findById(arma.getId()).orElseThrow().getStatus()).isEqualTo(StatusItem.EQUIPADO);
		assertThat(itemRepository.findById(armadura.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusItem.EQUIPADO);
	}

	@Test
	void ordemEmTrechosMudaTaxaDeProducaoNoMeioDoIntervalo() {
		Usuario usuario = criarUsuario("trechos", "Elisa");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		long madeiraInicial = vila.getMadeira();

		Instant agora = relogio.instant();
		Ordem ordem = new Ordem();
		ordem.setVilaId(vila.getId());
		ordem.setCategoria(CategoriaOrdem.CONSTRUCAO);
		ordem.setAlvo(TipoPredio.SERRARIA.name());
		ordem.setNivel(2);
		ordem.setQuantidade(1);
		ordem.setIniciadaEm(agora);
		ordem.setConcluiEm(agora.plus(Duration.ofHours(1)));
		ordemRepository.saveAndFlush(ordem);

		relogio.avancar(Duration.ofHours(2));
		Vila atualizada = vilaService.obterParaAtualizacao(usuario.getId());

		// 1h com serraria N1 (30/h) + 1h com serraria N2 (60/h) = +90 (milésimos: +90_000).
		assertThat(atualizada.getMadeira()).isEqualTo(madeiraInicial + 30_000L + 60_000L);
	}

	@Test
	void consultarRetornaSnapshotComDadosAtuaisDaVila() {
		Usuario usuario = criarUsuario("consulta", "Julia");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());

		EstadoVila estado = vilaService.consultar(usuario.getId());

		assertThat(estado.vila().getId()).isEqualTo(vila.getId());
		assertThat(estado.predios()).hasSize(8);
		assertThat(estado.canteiros()).hasSize(1);
		assertThat(estado.sementes()).isEmpty();
		assertThat(estado.itens()).isEmpty();
		assertThat(estado.unidades()).isEmpty();
		assertThat(estado.ordens()).isEmpty();
	}

}
