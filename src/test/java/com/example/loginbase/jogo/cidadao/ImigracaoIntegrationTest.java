package com.example.loginbase.jogo.cidadao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;
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
class ImigracaoIntegrationTest {

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

	@BeforeEach
	void preparar() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
	}

	private Construcao construir(TipoConstrucao tipo, NivelConstrucao nivel, EstadoConstrucao estado, int x) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(tipo);
		c.setNivel(nivel);
		c.setRegiaoIndice(1);
		c.setX(x);
		c.setY(1);
		c.setTamanho(1);
		c.setEstado(estado);
		c.setPoTotal(4);
		c.setPoAtual(4);
		return construcaoRepository.saveAndFlush(c);
	}

	private ImigracaoService servico(RandomGenerator r) {
		return new ImigracaoService(construcaoRepository, familiaRepository, cidadaoRepository, profissaoRepository,
				ocupacao, registro, r);
	}

	private ImigracaoService servicoSeed(long seed) {
		return servico(new Random(seed));
	}

	private RandomGenerator sorteioFixo(double valor) {
		return new RandomGenerator() {
			@Override public long nextLong() { return 0; }
			@Override public double nextDouble() { return valor; }
			@Override public boolean nextBoolean() { return true; }
			@Override public int nextInt(int limite) { return 0; }
		};
	}

	private long eventos() {
		return eventoRepository.findAll().stream()
				.filter(e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == TipoEventoTurno.IMIGRACAO).count();
	}

	@Test
	void chancePorNivel() {
		assertThat(ImigracaoService.chance(NivelConstrucao.N1)).isEqualTo(0.02);
		assertThat(ImigracaoService.chance(NivelConstrucao.N2)).isEqualTo(0.04);
		assertThat(ImigracaoService.chance(NivelConstrucao.N3)).isEqualTo(0.06);
	}

	@Test
	void imigranteValidoFormaNovoNucleoNaCasa() {
		Construcao casa = construir(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		construir(TipoConstrucao.ESTALAGEM, NivelConstrucao.N1, EstadoConstrucao.ATIVA, 3);
		// força o sucesso com sorteio baixo, mas usando aleatoriedade real para os atributos
		RandomGenerator rg = new RandomGenerator() {
			final Random base = new Random(42);
			boolean primeira = true;
			@Override public long nextLong() { return base.nextLong(); }
			@Override public double nextDouble() { if (primeira) { primeira = false; return 0.0; } return base.nextDouble(); }
		};
		List<Cidadao> imigrantes = servico(rg).processarImigracao(vila, 5);
		assertThat(imigrantes).hasSize(1);
		Cidadao c = imigrantes.get(0);
		assertThat(c.getIdadeAnos()).isBetween(18, 30);
		assertThat(c.isVivo()).isTrue();
		assertThat(c.getConjugeId()).isNull();
		int total = 0;
		for (Caracteristica k : Caracteristica.values()) {
			assertThat(c.valorCaracteristica(k)).isBetween(0, 10);
			total += c.valorCaracteristica(k);
		}
		assertThat(total).isEqualTo(20);
		List<CidadaoProfissao> pe = profissaoRepository.findByCidadaoId(c.getId());
		assertThat(pe).allSatisfy(p -> assertThat(p.getPontosBase()).isBetween(1, 5));
		assertThat(pe.stream().mapToInt(CidadaoProfissao::getPontosBase).sum()).isEqualTo(10);
		Familia f = familiaRepository.findById(c.getFamiliaId()).orElseThrow();
		assertThat(f.getCasaId()).isEqualTo(casa.getId());
		assertThat(c.getNome()).endsWith(f.getSobrenome());
		assertThat(ocupacao.ocupacao(casa).nucleosOcupados()).isEqualTo(1);
		assertThat(eventos()).isEqualTo(1);
	}

	@Test
	void semNucleoLivreNaoImigra() {
		Construcao casa = construir(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		construir(TipoConstrucao.ESTALAGEM, NivelConstrucao.N3, EstadoConstrucao.ATIVA, 3);
		Familia f = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", casa.getId()));
		cidadaoRepository.saveAndFlush(new Cidadao(vila.getId(), f.getId(), "A Silva", Sexo.M, 30 * 12));
		assertThat(servico(sorteioFixo(0.0)).processarImigracao(vila, 1)).isEmpty();
		assertThat(eventos()).isZero();
	}

	@Test
	void semCasaNaoImigra() {
		construir(TipoConstrucao.ESTALAGEM, NivelConstrucao.N3, EstadoConstrucao.ATIVA, 3);
		assertThat(servico(sorteioFixo(0.0)).processarImigracao(vila, 1)).isEmpty();
	}

	@Test
	void estalagemEmObraNaoAtrai() {
		construir(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		construir(TipoConstrucao.ESTALAGEM, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, 3);
		assertThat(servico(sorteioFixo(0.0)).processarImigracao(vila, 1)).isEmpty();
	}

	@Test
	void sorteioAcimaDaChanceNaoImigra() {
		construir(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		construir(TipoConstrucao.ESTALAGEM, NivelConstrucao.N1, EstadoConstrucao.ATIVA, 3);
		assertThat(servico(sorteioFixo(0.02)).processarImigracao(vila, 1)).isEmpty();
	}

	@Test
	void frequenciaAproximadaPorNivel() {
		construir(TipoConstrucao.CASA, NivelConstrucao.N3, EstadoConstrucao.ATIVA, 1);
		construir(TipoConstrucao.ESTALAGEM, NivelConstrucao.N3, EstadoConstrucao.ATIVA, 3);
		// casa N3 comporta 4 núcleos: cada sucesso ocupa um; ao lotar não imigra mais, então mede só as tentativas
		// com sorteio contado: 6% de 1000 sorteios ~ 60
		Random base = new Random(1);
		int sucessos = 0;
		for (int i = 0; i < 1000; i++) {
			if (base.nextDouble() < ImigracaoService.chance(NivelConstrucao.N3)) {
				sucessos++;
			}
		}
		assertThat(sucessos).isBetween(30, 90);
		// e o serviço respeita o limite de núcleos da casa
		ImigracaoService s = servicoSeed(3);
		int total = 0;
		for (int t = 1; t <= 2000; t++) {
			total += s.processarImigracao(vila, t).size();
		}
		assertThat(total).isEqualTo(4);
	}
}
