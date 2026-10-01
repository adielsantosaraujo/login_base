package com.example.loginbase.jogo.cidadao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

@SpringBootTest
@Transactional
class ReproducaoIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired OcupacaoCasasService ocupacao;
	@Autowired RegistroEventoTurnoService registro;
	@Autowired EventoTurnoRepository eventoRepository;

	Vila vila;
	Familia familia;
	Construcao casa;
	Cidadao pai;
	Cidadao mae;

	@BeforeEach
	void preparar() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
		casa = new Construcao();
		casa.setVilaId(vila.getId());
		casa.setTipo(TipoConstrucao.CASA);
		casa.setNivel(NivelConstrucao.N1);
		casa.setRegiaoIndice(1);
		casa.setX(1);
		casa.setY(1);
		casa.setTamanho(2);
		casa.setEstado(EstadoConstrucao.ATIVA);
		casa.setPoTotal(4);
		casa.setPoAtual(4);
		casa = construcaoRepository.saveAndFlush(casa);
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", casa.getId()));
		pai = novo("Pai", Sexo.M, 30 * 12, 10);
		mae = novo("Mae", Sexo.F, 25 * 12, 6);
		pai.setConjugeId(mae.getId());
		mae.setConjugeId(pai.getId());
		cidadaoRepository.saveAllAndFlush(List.of(pai, mae));
	}

	private Cidadao novo(String nome, Sexo sexo, int meses, int base) {
		Cidadao c = new Cidadao(vila.getId(), familia.getId(), nome, sexo, meses);
		c.setVit(base);
		c.setForca(base + 1);
		c.setVel(base + 2);
		c.setInteligencia(base + 3);
		c.setCar(base + 4);
		return cidadaoRepository.saveAndFlush(c);
	}

	private ReproducaoService servico(double sorteio, boolean sexoMasculino) {
		RandomGenerator r = new RandomGenerator() {
			@Override public long nextLong() { return 0; }
			@Override public double nextDouble() { return sorteio; }
			@Override public boolean nextBoolean() { return sexoMasculino; }
			@Override public int nextInt(int limite) { return 0; }
		};
		return new ReproducaoService(cidadaoRepository, profissaoRepository, familiaRepository, construcaoRepository,
				ocupacao, registro, r);
	}

	private Cidadao mae() {
		return cidadaoRepository.findById(mae.getId()).orElseThrow();
	}

	private long eventos(TipoEventoTurno tipo) {
		return eventoRepository.findAll().stream()
				.filter(e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == tipo).count();
	}

	@Test
	void formulaDeHerancaArredondaParaBaixo() {
		assertThat(ReproducaoService.calcularCaracteristicaHerdada(10, 6)).isEqualTo(2);
		assertThat(ReproducaoService.calcularCaracteristicaHerdada(7, 0)).isEqualTo(0);
		assertThat(ReproducaoService.calcularCaracteristicaHerdada(16, 16)).isEqualTo(4);
	}

	@Test
	void gestacaoDuraExatamenteNoveTurnosENasceHerdando() {
		profissaoRepository.saveAndFlush(new CidadaoProfissao(pai.getId(), Profissao.CACADOR, 8));
		profissaoRepository.saveAndFlush(new CidadaoProfissao(mae.getId(), Profissao.CACADOR, 8));
		profissaoRepository.saveAndFlush(new CidadaoProfissao(mae.getId(), Profissao.COSTUREIRO, 3));
		ReproducaoService conceber = servico(0.0, true);
		assertThat(conceber.processar(vila, 1)).isEmpty(); // concepção no turno 1
		assertThat(mae().getGestacaoTurnos()).isEqualTo(9);
		assertThat(eventos(TipoEventoTurno.CONCEPCAO)).isEqualTo(1);
		ReproducaoService gestar = servico(0.99, true);
		for (int t = 2; t <= 9; t++) {
			assertThat(gestar.processar(vila, t)).isEmpty();
		}
		assertThat(mae().getGestacaoTurnos()).isEqualTo(1);
		List<Cidadao> nascidos = gestar.processar(vila, 10);
		assertThat(nascidos).hasSize(1);
		Cidadao bebe = nascidos.get(0);
		assertThat(bebe.getIdadeMeses()).isZero();
		assertThat(bebe.getSexo()).isEqualTo(Sexo.M);
		assertThat(bebe.getFamiliaId()).isEqualTo(familia.getId());
		assertThat(bebe.getNome()).endsWith("Silva");
		assertThat(bebe.getPaiId()).isEqualTo(pai.getId());
		assertThat(bebe.getMaeId()).isEqualTo(mae.getId());
		assertThat(bebe.getVit()).isEqualTo(2); // (10+6)/8
		assertThat(bebe.getForca()).isEqualTo(2); // (11+7)/8
		assertThat(bebe.getCar()).isEqualTo(3); // (14+10)/8
		assertThat(profissaoRepository.findByCidadaoIdAndProfissao(bebe.getId(), Profissao.CACADOR))
				.get().extracting(CidadaoProfissao::getPontosBase).isEqualTo(2);
		assertThat(profissaoRepository.findByCidadaoIdAndProfissao(bebe.getId(), Profissao.COSTUREIRO)).isEmpty();
		assertThat(mae().getGestacaoTurnos()).isNull();
		assertThat(mae().getTurnoUltimoParto()).isEqualTo(10);
		assertThat(eventos(TipoEventoTurno.NASCIMENTO)).isEqualTo(1);
	}

	@Test
	void intervaloDeDozeTurnosAposParto() {
		mae.setTurnoUltimoParto(10);
		cidadaoRepository.saveAndFlush(mae);
		ReproducaoService s = servico(0.0, true);
		s.processar(vila, 21);
		assertThat(mae().getGestacaoTurnos()).isNull();
		s.processar(vila, 22);
		assertThat(mae().getGestacaoTurnos()).isEqualTo(9);
	}

	@Test
	void faminhoImpedeConcepcao() {
		pai.setFamintoTurnos(1);
		cidadaoRepository.saveAndFlush(pai);
		servico(0.0, true).processar(vila, 1);
		assertThat(mae().getGestacaoTurnos()).isNull();
		pai.setFamintoTurnos(0);
		mae.setFamintoTurnos(1);
		cidadaoRepository.saveAndFlush(List.of(pai, mae).get(0));
		cidadaoRepository.saveAndFlush(mae);
		servico(0.0, true).processar(vila, 1);
		assertThat(mae().getGestacaoTurnos()).isNull();
	}

	@Test
	void maeForaDaFaixaEtariaNaoConcebe() {
		mae.setIdadeMeses(46 * 12);
		cidadaoRepository.saveAndFlush(mae);
		servico(0.0, true).processar(vila, 1);
		assertThat(mae().getGestacaoTurnos()).isNull();
		mae.setIdadeMeses(17 * 12 + 11);
		cidadaoRepository.saveAndFlush(mae);
		servico(0.0, true).processar(vila, 1);
		assertThat(mae().getGestacaoTurnos()).isNull();
	}

	@Test
	void semVagaNaCasaNaoConcebe() {
		// casa N1 tem 4 vagas: 2 do casal + 2 filhos
		novo("F1", Sexo.M, 12, 1);
		novo("F2", Sexo.F, 12, 1);
		assertThat(ocupacao.ocupacao(casa).vagasLivres()).isZero();
		servico(0.0, true).processar(vila, 1);
		assertThat(mae().getGestacaoTurnos()).isNull();
	}

	@Test
	void semSorteioFavoravelNaoConcebe() {
		servico(0.08, true).processar(vila, 1);
		assertThat(mae().getGestacaoTurnos()).isNull();
	}

	@Test
	void semCasaNaoConcebe() {
		familia.setCasaId(null);
		familiaRepository.saveAndFlush(familia);
		servico(0.0, true).processar(vila, 1);
		assertThat(mae().getGestacaoTurnos()).isNull();
	}

	@Test
	void gestanteReservaVagaEDeixaDeConceberDeNovo() {
		servico(0.0, false).processar(vila, 1);
		assertThat(ocupacao.ocupacao(casa).vagasOcupadas()).isEqualTo(3);
		servico(0.0, false).processar(vila, 2);
		assertThat(mae().getGestacaoTurnos()).isEqualTo(8);
		assertThat(eventos(TipoEventoTurno.CONCEPCAO)).isEqualTo(1);
	}

	@Test
	void taxaAproximadaDeConcepcaoEmMuitosTurnos() {
		ReproducaoService s = new ReproducaoService(cidadaoRepository, profissaoRepository, familiaRepository,
				construcaoRepository, ocupacao, registro, new java.util.Random(42));
		int concepcoes = 0;
		for (int t = 1; t <= 1000; t++) {
			Integer antes = mae().getGestacaoTurnos();
			s.processar(vila, t);
			if (antes == null && mae().getGestacaoTurnos() != null) {
				concepcoes++;
			}
			// libera vaga para o teste seguir: remove bebês
			cidadaoRepository.findByFamiliaIdAndVivoTrue(familia.getId()).stream()
					.filter(c -> c.getIdadeMeses() == 0 && c.getMaeId() != null).forEach(c -> {
						c.setVivo(false);
						cidadaoRepository.save(c);
					});
		}
		assertThat(concepcoes).isBetween(15, 50);
	}

}
