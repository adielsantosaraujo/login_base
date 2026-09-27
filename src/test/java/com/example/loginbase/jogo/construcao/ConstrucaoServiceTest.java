package com.example.loginbase.jogo.construcao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.dominio.Canteiro;
import com.example.loginbase.jogo.dominio.CanteiroRepository;
import com.example.loginbase.jogo.dominio.CategoriaOrdem;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.OrdemRepository;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.PredioRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.dominio.VilaRepository;
import com.example.loginbase.jogo.economia.AplicadorOrdens;
import com.example.loginbase.jogo.economia.VilaService;
import com.example.loginbase.jogo.suporte.JogoTestConfig;
import com.example.loginbase.jogo.suporte.RelogioAjustavel;

/**
 * Testa {@link ConstrucaoService} contra o Postgres real do
 * {@code docker-compose} (mesma premissa de {@code VilaServiceTest}): as
 * cinco validações da tabela A.3 (na ordem especificada), o débito imediato
 * de recursos, a conclusão da ordem com o nível subindo e o efeito de
 * conclusão específico da {@code FAZENDA} (novo canteiro com {@code TRIGO}).
 *
 * <p>{@code @Transactional(propagation = NOT_SUPPORTED)} pelo mesmo motivo de
 * {@code VilaServiceTest}: {@link VilaService} usa uma transação
 * {@code REQUIRES_NEW} genuína para criar a vila na primeira chamada.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ AuditoriaConfig.class, UsuarioAuditorAware.class, JogoTestConfig.class, JogoProperties.class,
		VilaService.class, AplicadorOrdens.class, ConstrucaoService.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ConstrucaoServiceTest {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private VilaService vilaService;

	@Autowired
	private ConstrucaoService construcaoService;

	@Autowired
	private VilaRepository vilaRepository;

	@Autowired
	private PredioRepository predioRepository;

	@Autowired
	private CanteiroRepository canteiroRepository;

	@Autowired
	private OrdemRepository ordemRepository;

	@Autowired
	private RelogioAjustavel relogio;

	/**
	 * @param prefixoEmail identificador curto do cenário; um sufixo único é anexado para que
	 *                     reexecuções do teste (dados commitados de verdade, sem rollback —
	 *                     ver Javadoc da classe) nunca colidam com a unique constraint de e-mail
	 */
	private Usuario criarUsuario(String prefixoEmail, String nome) {
		Usuario usuario = new Usuario();
		usuario.setNome(nome);
		usuario.setEmail(prefixoEmail + "+" + UUID.randomUUID() + "@teste.local");
		usuario.setSenha("senha-hash");
		return usuarioRepository.saveAndFlush(usuario);
	}

	private Predio predioDoTipo(long vilaId, TipoPredio tipo) {
		return predioRepository.findByVilaId(vilaId).stream()
				.filter(predio -> predio.getTipo() == tipo)
				.findFirst()
				.orElseThrow();
	}

	private void definirNivel(long vilaId, TipoPredio tipo, int nivel) {
		Predio predio = predioDoTipo(vilaId, tipo);
		predio.setNivel(nivel);
		predioRepository.saveAndFlush(predio);
	}

	private List<Ordem> ordensDeConstrucao(long vilaId) {
		return ordemRepository.findByVilaId(vilaId).stream()
				.filter(ordem -> ordem.getCategoria() == CategoriaOrdem.CONSTRUCAO)
				.toList();
	}

	@Test
	void prediosNoNivelMaximoRejeitamMelhoriaComNivelMaximo() {
		Usuario usuario = criarUsuario("nivel-maximo", "Ana");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		definirNivel(vila.getId(), TipoPredio.CENTRO_VILA, TipoPredio.NIVEL_MAXIMO);

		RegraJogoException erro = catchThrowableOfType(RegraJogoException.class,
				() -> construcaoService.melhorar(usuario.getId(), TipoPredio.CENTRO_VILA));

		assertThat(erro.getCodigo()).isEqualTo(CodigoErro.NIVEL_MAXIMO);
	}

	@Test
	void centroDaVilaLimitaNivelDosDemaisPredios() {
		// Vila nova: CENTRO_VILA e ARMAZEM nível 1; melhorar o armazém para 2 excede o centro.
		Usuario usuario = criarUsuario("limite-centro", "Bruno");

		RegraJogoException erro = catchThrowableOfType(RegraJogoException.class,
				() -> construcaoService.melhorar(usuario.getId(), TipoPredio.ARMAZEM));

		assertThat(erro.getCodigo()).isEqualTo(CodigoErro.REQUISITO_NAO_ATENDIDO);
	}

	@Test
	void forjaExigeMinaDeFerroNoNivelUmOuSuperior() {
		// Vila nova: MINA_FERRO nível 0; construir a forja (0 → 1) é rejeitado.
		Usuario usuario = criarUsuario("forja-sem-mina", "Carla");

		RegraJogoException erro = catchThrowableOfType(RegraJogoException.class,
				() -> construcaoService.melhorar(usuario.getId(), TipoPredio.FORJA));

		assertThat(erro.getCodigo()).isEqualTo(CodigoErro.REQUISITO_NAO_ATENDIDO);
	}

	@Test
	void quartelExigeForjaNoNivelUmOuSuperior() {
		// Vila nova: FORJA nível 0; construir o quartel (0 → 1) é rejeitado.
		Usuario usuario = criarUsuario("quartel-sem-forja", "Diego");

		RegraJogoException erro = catchThrowableOfType(RegraJogoException.class,
				() -> construcaoService.melhorar(usuario.getId(), TipoPredio.QUARTEL));

		assertThat(erro.getCodigo()).isEqualTo(CodigoErro.REQUISITO_NAO_ATENDIDO);
	}

	@Test
	void filaDeConstrucaoOcupadaRejeitaNovaMelhoria() {
		Usuario usuario = criarUsuario("fila-ocupada", "Elisa");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());

		Instant agora = relogio.instant();
		Ordem ordemEmAndamento = new Ordem();
		ordemEmAndamento.setVilaId(vila.getId());
		ordemEmAndamento.setCategoria(CategoriaOrdem.CONSTRUCAO);
		ordemEmAndamento.setAlvo(TipoPredio.ARMAZEM.name());
		ordemEmAndamento.setNivel(2);
		ordemEmAndamento.setIniciadaEm(agora);
		ordemEmAndamento.setConcluiEm(agora.plusSeconds(60));
		ordemRepository.saveAndFlush(ordemEmAndamento);

		// CENTRO_VILA (1 → 2) não esbarra no limite do próprio centro: só a fila bloqueia aqui.
		RegraJogoException erro = catchThrowableOfType(RegraJogoException.class,
				() -> construcaoService.melhorar(usuario.getId(), TipoPredio.CENTRO_VILA));

		assertThat(erro.getCodigo()).isEqualTo(CodigoErro.FILA_OCUPADA);
	}

	@Test
	void recursosInsuficientesRejeitamMelhoria() {
		Usuario usuario = criarUsuario("recursos-insuficientes", "Fabio");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		vila.setMadeira(0L);
		vilaRepository.saveAndFlush(vila);

		// CENTRO_VILA (1 → 2) custa M225 P225; sem madeira, é rejeitado por recursos.
		RegraJogoException erro = catchThrowableOfType(RegraJogoException.class,
				() -> construcaoService.melhorar(usuario.getId(), TipoPredio.CENTRO_VILA));

		assertThat(erro.getCodigo()).isEqualTo(CodigoErro.RECURSOS_INSUFICIENTES);
	}

	@Test
	void validacoesOcorremNaOrdemEspecificadaPrimeiroErroPrevalece() {
		Usuario usuario = criarUsuario("ordem-validacoes", "Gustavo");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());

		// Simultaneamente: CENTRO_VILA no nível máximo, fila ocupada e sem recursos.
		// A validação de nível máximo (1ª) deve prevalecer sobre fila (4ª) e recursos (5ª).
		definirNivel(vila.getId(), TipoPredio.CENTRO_VILA, TipoPredio.NIVEL_MAXIMO);
		Instant agora = relogio.instant();
		Ordem ordemEmAndamento = new Ordem();
		ordemEmAndamento.setVilaId(vila.getId());
		ordemEmAndamento.setCategoria(CategoriaOrdem.CONSTRUCAO);
		ordemEmAndamento.setAlvo(TipoPredio.ARMAZEM.name());
		ordemEmAndamento.setNivel(2);
		ordemEmAndamento.setIniciadaEm(agora);
		ordemEmAndamento.setConcluiEm(agora.plusSeconds(60));
		ordemRepository.saveAndFlush(ordemEmAndamento);
		vila.setMadeira(0L);
		vila.setPedra(0L);
		vilaRepository.saveAndFlush(vila);

		RegraJogoException erro = catchThrowableOfType(RegraJogoException.class,
				() -> construcaoService.melhorar(usuario.getId(), TipoPredio.CENTRO_VILA));

		assertThat(erro.getCodigo()).isEqualTo(CodigoErro.NIVEL_MAXIMO);
	}

	@Test
	void recursosSaoDebitadosImediatamenteEPredioPermaneceNoNivelAnteriorAteConclusao() {
		Usuario usuario = criarUsuario("debito-imediato", "Helena");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		long madeiraAntes = vila.getMadeira();
		long pedraAntes = vila.getPedra();

		// MINA_FERRO (0 → 1) custa M100 P80 e não esbarra no limite do centro (nível 1).
		construcaoService.melhorar(usuario.getId(), TipoPredio.MINA_FERRO);

		Vila vilaAtualizada = vilaRepository.findByUsuarioId(usuario.getId()).orElseThrow();
		assertThat(vilaAtualizada.getMadeira()).isEqualTo(madeiraAntes - 100_000L);
		assertThat(vilaAtualizada.getPedra()).isEqualTo(pedraAntes - 80_000L);

		assertThat(predioDoTipo(vila.getId(), TipoPredio.MINA_FERRO).getNivel()).isZero();

		List<Ordem> ordens = ordensDeConstrucao(vila.getId());
		assertThat(ordens).hasSize(1);
		Ordem ordem = ordens.get(0);
		assertThat(ordem.getAlvo()).isEqualTo(TipoPredio.MINA_FERRO.name());
		assertThat(ordem.getNivel()).isEqualTo(1);
		assertThat(ordem.getIniciadaEm()).isEqualTo(relogio.instant());
		assertThat(ordem.getConcluiEm()).isEqualTo(relogio.instant().plusSeconds(90));
	}

	@Test
	void nivelDoPredioSobeAoAlvoQuandoOrdemConcluiNaSincronizacao() {
		Usuario usuario = criarUsuario("conclusao", "Ines");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());

		construcaoService.melhorar(usuario.getId(), TipoPredio.MINA_FERRO);
		relogio.avancar(Duration.ofSeconds(90));
		vilaService.obterParaAtualizacao(usuario.getId());

		assertThat(predioDoTipo(vila.getId(), TipoPredio.MINA_FERRO).getNivel()).isEqualTo(1);
		assertThat(ordensDeConstrucao(vila.getId())).isEmpty();
	}

	@Test
	void fazendaNoNivelDoisCriaNovoCanteiroComTrigoAoConcluir() {
		Usuario usuario = criarUsuario("canteiro-novo", "Julia");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		// FAZENDA (1 → 2) exige CENTRO_VILA >= 2.
		definirNivel(vila.getId(), TipoPredio.CENTRO_VILA, 2);

		construcaoService.melhorar(usuario.getId(), TipoPredio.FAZENDA);
		relogio.avancar(Duration.ofSeconds(120));
		vilaService.obterParaAtualizacao(usuario.getId());

		assertThat(predioDoTipo(vila.getId(), TipoPredio.FAZENDA).getNivel()).isEqualTo(2);
		List<Canteiro> canteiros = canteiroRepository.findByVilaId(vila.getId());
		assertThat(canteiros).hasSize(2);
		assertThat(canteiros).extracting(Canteiro::getPosicao).containsExactlyInAnyOrder(1, 2);
		assertThat(canteiros).extracting(Canteiro::getCultivo).containsOnly(Cultivo.TRIGO);
	}

}
