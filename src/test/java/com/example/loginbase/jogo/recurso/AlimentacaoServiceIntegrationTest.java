package com.example.loginbase.jogo.recurso;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

@SpringBootTest
@Transactional
class AlimentacaoServiceIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired EstoqueService estoqueService;
	@Autowired AlimentacaoService servico;
	@Autowired EventoTurnoRepository eventoRepository;

	private Vila vila;
	private Familia familia;

	private void preparar(String refeicao, String graos, String carne) {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
		estoqueService.inicializar(vila, Map.of(Recurso.REFEICAO, new BigDecimal(refeicao),
				Recurso.GRAOS, new BigDecimal(graos), Recurso.CARNE, new BigDecimal(carne)));
	}

	private Cidadao novo(String nome, int anos, int vit) {
		Cidadao c = new Cidadao(vila.getId(), familia.getId(), nome, Sexo.M, anos * 12);
		c.setVit(vit);
		return cidadaoRepository.saveAndFlush(c);
	}

	private void populacao(int adultos, int menores) {
		for (int i = 0; i < adultos; i++) novo("A" + i, 30, 5);
		for (int i = 0; i < menores; i++) novo("M" + i, 5, 5);
	}

	private long eventos(TipoEventoTurno t) {
		return eventoRepository.findAll().stream()
				.filter(e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == t).count();
	}

	private BigDecimal qtd(Recurso r) {
		return estoqueService.quantidade(vila, r);
	}

	@Test
	void consumoNormalTodosAlimentados() {
		preparar("12", "0", "0");
		populacao(10, 4);
		servico.processarConsumoAlimentacao(vila, 1);
		assertThat(qtd(Recurso.REFEICAO)).isEqualByComparingTo("0");
		assertThat(cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId())).allMatch(c -> c.getFamintoTurnos() == 0);
		assertThat(eventos(TipoEventoTurno.ALIMENTOS_CONSUMIDOS)).isEqualTo(1);
		assertThat(eventos(TipoEventoTurno.FOME)).isZero();
	}

	@Test
	void bemAlimentadaComTotalmenteRefeicao() {
		preparar("20", "0", "0");
		populacao(10, 4);
		servico.processarConsumoAlimentacao(vila, 1);
		assertThat(vilaRepository.findById(vila.getId()).orElseThrow().isBemAlimentada()).isTrue();
		assertThat(qtd(Recurso.REFEICAO)).isEqualByComparingTo("8");
		assertThat(eventos(TipoEventoTurno.BEM_ALIMENTADA)).isEqualTo(1);
	}

	@Test
	void naoBemAlimentadaComQuarentaPorCentoDeRefeicao() {
		preparar("5", "20", "0");
		populacao(10, 4); // demanda 12; refeição 5 (~42%) -> não bem alimentada ... 5/12 < 50%
		servico.processarConsumoAlimentacao(vila, 1);
		assertThat(vilaRepository.findById(vila.getId()).orElseThrow().isBemAlimentada()).isFalse();
		assertThat(qtd(Recurso.GRAOS)).isEqualByComparingTo("13");
		assertThat(eventos(TipoEventoTurno.BEM_ALIMENTADA)).isZero();
	}

	@Test
	void ordemRefeicaoGraosCarne() {
		preparar("2", "3", "10");
		populacao(6, 0);
		servico.processarConsumoAlimentacao(vila, 1);
		assertThat(qtd(Recurso.REFEICAO)).isEqualByComparingTo("0");
		assertThat(qtd(Recurso.GRAOS)).isEqualByComparingTo("0");
		assertThat(qtd(Recurso.CARNE)).isEqualByComparingTo("9");
	}

	@Test
	void fomeParcialAlimentaMenoresPrimeiro() {
		preparar("2", "0", "0");
		Cidadao adulto = novo("Adulto", 30, 5);
		Cidadao m1 = novo("M1", 5, 5);
		Cidadao m2 = novo("M2", 6, 5);
		Cidadao m3 = novo("M3", 7, 5);
		// demanda 2,5; disponível 2 -> 3 menores (1,5) + resta 0,5 < 1 -> adulto faminto
		adulto.setFamintoTurnos(1);
		cidadaoRepository.saveAndFlush(adulto);
		m1.setFamintoTurnos(2);
		cidadaoRepository.saveAndFlush(m1);
		servico.processarConsumoAlimentacao(vila, 1);
		assertThat(cidadaoRepository.findById(adulto.getId()).orElseThrow().getFamintoTurnos()).isEqualTo(2);
		assertThat(cidadaoRepository.findById(m1.getId()).orElseThrow().getFamintoTurnos()).isZero();
		assertThat(cidadaoRepository.findById(m2.getId()).orElseThrow().getFamintoTurnos()).isZero();
		assertThat(cidadaoRepository.findById(m3.getId()).orElseThrow().getFamintoTurnos()).isZero();
		assertThat(qtd(Recurso.REFEICAO)).isEqualByComparingTo("0.5");
		assertThat(eventos(TipoEventoTurno.FOME)).isEqualTo(1);
		// consumo demandado 3,0; refeição consumida 1,5 -> bem alimentada (50%)
		assertThat(vilaRepository.findById(vila.getId()).orElseThrow().isBemAlimentada()).isTrue();
	}

	@Test
	void tresTurnosConsecutivosDeFomeSomamContador() {
		preparar("0", "0", "0");
		Cidadao c = novo("A", 30, 5);
		for (int t = 1; t <= 3; t++) {
			servico.processarConsumoAlimentacao(vila, t);
		}
		Cidadao lido = cidadaoRepository.findById(c.getId()).orElseThrow();
		assertThat(lido.getFamintoTurnos()).isEqualTo(3);
		assertThat(lido.getVit()).isEqualTo(4); // perde VIT no 3º turno
		assertThat(lido.isVivo()).isTrue();
	}

	@Test
	void morteDeFomeComVitUm() {
		preparar("0", "0", "0");
		Cidadao c = novo("A", 30, 1);
		c.setFamintoTurnos(2);
		cidadaoRepository.saveAndFlush(c);
		servico.processarConsumoAlimentacao(vila, 5);
		Cidadao lido = cidadaoRepository.findById(c.getId()).orElseThrow();
		assertThat(lido.isVivo()).isFalse();
		assertThat(eventos(TipoEventoTurno.MORTE)).isEqualTo(1);
	}

	@Test
	void comerZeraContadorEFlagBemAlimentadaPersiste() {
		preparar("10", "0", "0");
		Cidadao c = novo("A", 30, 5);
		c.setFamintoTurnos(2);
		cidadaoRepository.saveAndFlush(c);
		servico.processarConsumoAlimentacao(vila, 1);
		assertThat(cidadaoRepository.findById(c.getId()).orElseThrow().getFamintoTurnos()).isZero();
		assertThat(vilaRepository.findById(vila.getId()).orElseThrow().isBemAlimentada()).isTrue();
	}

}
