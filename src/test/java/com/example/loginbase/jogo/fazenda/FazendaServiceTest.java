package com.example.loginbase.jogo.fazenda;

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
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.Canteiro;
import com.example.loginbase.jogo.dominio.CanteiroRepository;
import com.example.loginbase.jogo.dominio.CategoriaOrdem;
import com.example.loginbase.jogo.dominio.EstoqueSemente;
import com.example.loginbase.jogo.dominio.EstoqueSementeRepository;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.OrdemRepository;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.PredioRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.economia.AplicadorOrdens;
import com.example.loginbase.jogo.economia.VilaService;
import com.example.loginbase.jogo.suporte.JogoTestConfig;
import com.example.loginbase.jogo.suporte.RelogioAjustavel;

/**
 * Testa {@link FazendaService}: plantio instantâneo, consumo de sementes
 * (exceto TRIGO), validação de canteiro inexistente e de semente
 * indisponível, e o efeito da troca de cultivo sobre a produção de comida —
 * mesma premissa de {@code VilaServiceTest} (Postgres real do
 * {@code docker-compose}, {@code @Transactional(propagation = NOT_SUPPORTED)}
 * porque {@code VilaService} usa uma transação {@code REQUIRES_NEW} genuína
 * para criar a vila).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ AuditoriaConfig.class, UsuarioAuditorAware.class, JogoTestConfig.class, JogoProperties.class,
		VilaService.class, AplicadorOrdens.class, FazendaService.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class FazendaServiceTest {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private VilaService vilaService;

	@Autowired
	private FazendaService fazendaService;

	@Autowired
	private CanteiroRepository canteiroRepository;

	@Autowired
	private EstoqueSementeRepository estoqueSementeRepository;

	@Autowired
	private OrdemRepository ordemRepository;

	@Autowired
	private PredioRepository predioRepository;

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

	private void darSemente(Long vilaId, Cultivo cultivo, int quantidade) {
		EstoqueSemente estoque = new EstoqueSemente();
		estoque.setVilaId(vilaId);
		estoque.setCultivo(cultivo);
		estoque.setQuantidade(quantidade);
		estoqueSementeRepository.saveAndFlush(estoque);
	}

	/** Define o nível da FAZENDA e deixa exatamente {@code canteiros} canteiros (TRIGO) na vila. */
	private void prepararFazenda(Long vilaId, int nivel, int canteiros) {
		Predio fazenda = predioRepository.findByVilaId(vilaId).stream()
				.filter(p -> p.getTipo() == TipoPredio.FAZENDA)
				.findFirst()
				.orElseGet(() -> {
					Predio novo = new Predio();
					novo.setVilaId(vilaId);
					novo.setTipo(TipoPredio.FAZENDA);
					return novo;
				});
		fazenda.setNivel(nivel);
		predioRepository.saveAndFlush(fazenda);

		canteiroRepository.deleteAll(canteiroRepository.findByVilaId(vilaId));
		canteiroRepository.flush();
		for (int posicao = 1; posicao <= canteiros; posicao++) {
			Canteiro canteiro = new Canteiro();
			canteiro.setVilaId(vilaId);
			canteiro.setPosicao(posicao);
			canteiro.setCultivo(Cultivo.TRIGO);
			canteiro.setPlantadoEm(relogio.instant());
			canteiroRepository.save(canteiro);
		}
		canteiroRepository.flush();
	}

	/** Cria uma ordem de melhoria da FAZENDA já vencida e sincroniza a vila. */
	private void concluirMelhoriaDaFazenda(Long usuarioId, Long vilaId, int novoNivel) {
		Instant agora = relogio.instant();
		Ordem ordem = new Ordem();
		ordem.setVilaId(vilaId);
		ordem.setCategoria(CategoriaOrdem.CONSTRUCAO);
		ordem.setAlvo(TipoPredio.FAZENDA.name());
		ordem.setNivel(novoNivel);
		ordem.setQuantidade(1);
		ordem.setIniciadaEm(agora);
		ordem.setConcluiEm(agora.plusSeconds(60));
		ordemRepository.saveAndFlush(ordem);

		relogio.avancar(Duration.ofMinutes(2));
		vilaService.obterParaAtualizacao(usuarioId);
	}

	@Test
	void plantioDeTrigoNaoConsomeSemente() {
		Usuario usuario = criarUsuario("trigo", "Ana");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());

		fazendaService.plantar(usuario.getId(), 1, Cultivo.TRIGO);

		List<Canteiro> canteiros = canteiroRepository.findByVilaId(vila.getId());
		assertThat(canteiros).hasSize(1);
		assertThat(canteiros.get(0).getCultivo()).isEqualTo(Cultivo.TRIGO);
		assertThat(estoqueSementeRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void plantioDeMilhoComSementeDisponivelDecrementaEstoque() {
		Usuario usuario = criarUsuario("milho", "Bruno");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		darSemente(vila.getId(), Cultivo.MILHO, 2);

		fazendaService.plantar(usuario.getId(), 1, Cultivo.MILHO);

		List<Canteiro> canteiros = canteiroRepository.findByVilaId(vila.getId());
		assertThat(canteiros.get(0).getCultivo()).isEqualTo(Cultivo.MILHO);

		List<EstoqueSemente> sementes = estoqueSementeRepository.findByVilaId(vila.getId());
		assertThat(sementes).hasSize(1);
		assertThat(sementes.get(0).getCultivo()).isEqualTo(Cultivo.MILHO);
		assertThat(sementes.get(0).getQuantidade()).isEqualTo(1);
	}

	@Test
	void plantioEmPosicaoAlemDoNivelDaFazendaFalhaComCanteiroInexistente() {
		Usuario usuario = criarUsuario("canteiro-inexistente", "Carla");
		vilaService.obterParaAtualizacao(usuario.getId());

		assertThatThrownBy(() -> fazendaService.plantar(usuario.getId(), 2, Cultivo.TRIGO))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.CANTEIRO_INEXISTENTE));
	}

	@Test
	void plantioDeMilhoSemSementeFalhaComSementeIndisponivelENaoConsomeNada() {
		Usuario usuario = criarUsuario("sem-semente", "Diego");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());

		assertThatThrownBy(() -> fazendaService.plantar(usuario.getId(), 1, Cultivo.MILHO))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.SEMENTE_INDISPONIVEL));

		assertThat(canteiroRepository.findByVilaId(vila.getId()).get(0).getCultivo()).isEqualTo(Cultivo.TRIGO);
		assertThat(estoqueSementeRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void producaoDeComidaReflitoNovoCultivoAposPlantio() {
		Usuario usuario = criarUsuario("producao", "Elisa");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		darSemente(vila.getId(), Cultivo.MILHO, 1);

		fazendaService.plantar(usuario.getId(), 1, Cultivo.MILHO);

		long comidaAntes = vilaService.obterParaAtualizacao(usuario.getId()).getComida();
		relogio.avancar(Duration.ofHours(1));
		Vila atualizada = vilaService.obterParaAtualizacao(usuario.getId());

		// MILHO = 30 comida/h; 1h = +30 (em milésimos: +30_000).
		assertThat(atualizada.getComida()).isEqualTo(comidaAntes + 30_000L);
	}

	@Test
	void trocaDeCultivoContaProducaoAnteriorAteOInstanteDaTroca() {
		Usuario usuario = criarUsuario("troca-cultivo", "Fabio");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		darSemente(vila.getId(), Cultivo.MILHO, 1);
		long comidaInicial = vila.getComida();

		// Canteiro 1 fica com TRIGO (20/h) por 1h completa.
		relogio.avancar(Duration.ofHours(1));

		// obterParaAtualizacao (dentro de plantar) produz com TRIGO até agora, só
		// então o canteiro passa a MILHO.
		fazendaService.plantar(usuario.getId(), 1, Cultivo.MILHO);
		Vila apósTroca = vilaService.obterParaAtualizacao(usuario.getId());
		assertThat(apósTroca.getComida()).isEqualTo(comidaInicial + 20_000L);

		// Mais 1h: agora produz com MILHO (30/h).
		relogio.avancar(Duration.ofHours(1));
		Vila depoisDeMilho = vilaService.obterParaAtualizacao(usuario.getId());
		assertThat(depoisDeMilho.getComida()).isEqualTo(comidaInicial + 20_000L + 30_000L);
	}

	@Test
	void plantioEmCanteiroInexistenteAindaNaoCriadoPelaOrdemDeConstrucao() {
		Usuario usuario = criarUsuario("canteiro-novo", "Gustavo");
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

		fazendaService.plantar(usuario.getId(), 2, Cultivo.TRIGO);

		List<Canteiro> canteiros = canteiroRepository.findByVilaId(vila.getId());
		assertThat(canteiros).hasSize(2);
		assertThat(canteiros).extracting(Canteiro::getPosicao).containsExactlyInAnyOrder(1, 2);
	}

	@Test
	void melhoriaDaFazendaDe5Para6NaoCriaCanteiro() {
		Usuario usuario = criarUsuario("faixa-5-6", "Helena");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		prepararFazenda(vila.getId(), 5, 5);

		concluirMelhoriaDaFazenda(usuario.getId(), vila.getId(), 6);

		assertThat(canteiroRepository.findByVilaId(vila.getId())).extracting(Canteiro::getPosicao)
				.containsExactlyInAnyOrder(1, 2, 3, 4, 5);
	}

	@Test
	void melhoriaDaFazendaDe9Para10CriaCanteiroDaPosicao6ComTrigo() {
		Usuario usuario = criarUsuario("faixa-9-10", "Igor");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		prepararFazenda(vila.getId(), 9, 5);

		concluirMelhoriaDaFazenda(usuario.getId(), vila.getId(), 10);

		List<Canteiro> canteiros = canteiroRepository.findByVilaId(vila.getId());
		assertThat(canteiros).extracting(Canteiro::getPosicao).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6);
		assertThat(canteiros).filteredOn(c -> c.getPosicao() == 6).singleElement()
				.satisfies(c -> assertThat(c.getCultivo()).isEqualTo(Cultivo.TRIGO));
	}

	@Test
	void plantioNaPosicao6ComFazendaNivel9FalhaComCanteiroInexistente() {
		Usuario usuario = criarUsuario("plantio-n9", "Julia");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		prepararFazenda(vila.getId(), 9, 5);

		assertThatThrownBy(() -> fazendaService.plantar(usuario.getId(), 6, Cultivo.TRIGO))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.CANTEIRO_INEXISTENTE));
	}

	@Test
	void plantioNaPosicao6ComFazendaNivel10EAceito() {
		Usuario usuario = criarUsuario("plantio-n10", "Kleber");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		prepararFazenda(vila.getId(), 10, 6);

		fazendaService.plantar(usuario.getId(), 6, Cultivo.TRIGO);

		assertThat(canteiroRepository.findByVilaId(vila.getId())).hasSize(6);
	}

}
