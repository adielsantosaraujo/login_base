package com.example.loginbase.jogo.cidadao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurno;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

@SpringBootTest
@Transactional
class EnvelhecimentoMorteIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired MorteService morteService;
	@Autowired RegistroEventoTurnoService registro;
	@Autowired EventoTurnoRepository eventoRepository;

	private Vila vila;
	private Familia familia;

	private void preparar() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
	}

	private Cidadao novo(String nome, Sexo sexo, int meses, int vit) {
		Cidadao c = new Cidadao(vila.getId(), familia.getId(), nome, sexo, meses);
		c.setVit(vit);
		return cidadaoRepository.saveAndFlush(c);
	}

	private EnvelhecimentoService servico(double sorteio) {
		RandomGenerator r = new RandomGenerator() {
			@Override public long nextLong() { return 0; }
			@Override public double nextDouble() { return sorteio; }
		};
		return new EnvelhecimentoService(cidadaoRepository, morteService, registro, r);
	}

	private List<EventoTurno> eventos(TipoEventoTurno tipo) {
		return eventoRepository.findAll().stream()
				.filter(e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == tipo).toList();
	}

	@Test
	void todosEnvelhecemUmMesSemAniversario() {
		preparar();
		Cidadao c = novo("A", Sexo.M, 100, 5);
		servico(0.0).processar(vila, 2);
		assertThat(cidadaoRepository.findById(c.getId()).orElseThrow().getIdadeMeses()).isEqualTo(101);
		assertThat(eventos(TipoEventoTurno.ANIVERSARIO)).isEmpty();
	}

	@Test
	void crescimentoAteDezoitoAnos() {
		preparar();
		Cidadao c = novo("Crianca", Sexo.F, 0, 5);
		EnvelhecimentoService s = servico(0.99);
		for (int t = 1; t <= 24 * 12; t++) {
			s.processar(vila, t);
		}
		Cidadao lido = cidadaoRepository.findById(c.getId()).orElseThrow();
		assertThat(lido.getIdadeAnos()).isEqualTo(24);
		assertThat(lido.getPontosCarPendentes()).isEqualTo(18);
		assertThat(lido.getPontosProfPendentes()).isEqualTo(9);
		assertThat(eventos(TipoEventoTurno.ANIVERSARIO)).hasSize(24);
	}

	@Test
	void aniversarioNoPrimeiroAnoDaCaracteristicaMasNaoProfissao() {
		preparar();
		Cidadao c = novo("B", Sexo.M, 11, 5);
		servico(0.99).processar(vila, 1);
		Cidadao lido = cidadaoRepository.findById(c.getId()).orElseThrow();
		assertThat(lido.getPontosCarPendentes()).isEqualTo(1);
		assertThat(lido.getPontosProfPendentes()).isZero();
	}

	@Test
	void chanceDeMorteSegueFormula() {
		assertThat(EnvelhecimentoService.calcularChanceMorte(60, 5)).isCloseTo(0.10, org.assertj.core.data.Offset.offset(1e-9));
		assertThat(EnvelhecimentoService.calcularChanceMorte(50, 8)).isZero();
		assertThat(EnvelhecimentoService.calcularChanceMorte(49, 0)).isZero();
		assertThat(EnvelhecimentoService.calcularChanceMorte(90, 10)).isEqualTo(1.0);
	}

	@Test
	void morteSoNoAniversarioComSorteioAbaixoDaChance() {
		preparar();
		Cidadao meioAno = novo("Meio", Sexo.M, 60 * 12 + 3, 5);
		Cidadao aniv = novo("Aniv", Sexo.M, 60 * 12 - 1, 5);
		Cidadao salvo = novo("Salvo", Sexo.M, 60 * 12 - 1, 5);
		// sorteio 0.05 < 10%: morre quem faz aniversario; meio ano nunca testa
		servico(0.05).processar(vila, 2);
		assertThat(cidadaoRepository.findById(meioAno.getId()).orElseThrow().isVivo()).isTrue();
		assertThat(cidadaoRepository.findById(aniv.getId()).orElseThrow().isVivo()).isFalse();
		assertThat(eventos(TipoEventoTurno.MORTE)).hasSize(2);
		assertThat(eventos(TipoEventoTurno.MORTE).get(0).getDados()).containsEntry("causa", "IDADE");
		// sorteio alto: sobrevive
		preparar();
		Cidadao s2 = novo("S2", Sexo.M, 60 * 12 - 1, 5);
		servico(0.5).processar(vila, 2);
		assertThat(cidadaoRepository.findById(s2.getId()).orElseThrow().isVivo()).isTrue();
		assertThat(salvo.getId()).isNotNull();
	}

	@Test
	void noventaAnosMorreComCerteza() {
		preparar();
		Cidadao c = novo("Velho", Sexo.M, 90 * 12 - 1, 10);
		servico(0.9999).processar(vila, 2);
		assertThat(cidadaoRepository.findById(c.getId()).orElseThrow().isVivo()).isFalse();
	}

	@Test
	void morteDesalocaViuvaConjugeERegistraEvento() {
		preparar();
		Cidadao m = novo("Marido", Sexo.M, 400, 5);
		Cidadao f = novo("Esposa", Sexo.F, 400, 5);
		m.setConjugeId(f.getId());
		f.setConjugeId(m.getId());
		m.setConstrucaoId(null);
		cidadaoRepository.saveAllAndFlush(List.of(m, f));
		morteService.morrer(vila, m, "FOME", 3);
		assertThat(m.isVivo()).isFalse();
		assertThat(m.getConstrucaoId()).isNull();
		assertThat(cidadaoRepository.findById(f.getId()).orElseThrow().getConjugeId()).isNull();
		assertThat(eventos(TipoEventoTurno.MORTE).get(0).getDados()).containsEntry("causa", "FOME");
	}

	@Test
	void sucessaoDoLiderSoQuandoFamiliaFicaSemAdultoVivo() {
		preparar();
		vila.setFamiliaLiderId(familia.getId());
		vilaRepository.saveAndFlush(vila);
		Cidadao a1 = novo("Adulto1", Sexo.M, 30 * 12, 5);
		Cidadao a2 = novo("Adulto2", Sexo.F, 25 * 12, 5);
		novo("Crianca", Sexo.F, 24, 5);
		morteService.morrer(vila, a1, "IDADE", 5);
		assertThat(vila.getFamiliaLiderId()).isEqualTo(familia.getId());
		assertThat(eventos(TipoEventoTurno.SUCESSAO_LIDER)).isEmpty();
		morteService.morrer(vila, a2, "IDADE", 6);
		assertThat(vila.getFamiliaLiderId()).isNull();
		assertThat(eventos(TipoEventoTurno.SUCESSAO_LIDER)).hasSize(1);
	}

}
