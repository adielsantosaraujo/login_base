package com.example.loginbase.jogo.cidadao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.item.BonusEquipamentoService;
import com.example.loginbase.jogo.modelo.Vila;

class EficienciaServiceTest {

	private CidadaoProfissaoRepository profRepo;
	private CidadaoRepository cidRepo;
	private BonusEquipamentoService bonusEq;
	private EficienciaService service;
	private Vila vila;

	@BeforeEach
	void setUp() {
		profRepo = mock(CidadaoProfissaoRepository.class);
		cidRepo = mock(CidadaoRepository.class);
		bonusEq = mock(BonusEquipamentoService.class);
		service = new EficienciaService(new CalculadoraPeEfetivo(bonusEq), profRepo, cidRepo, bonusEq);
		vila = new Vila(1L, "V", 1L, 0);
	}

	private Cidadao cidadao(long id, int idadeAnos, int pe) {
		Cidadao c = new Cidadao(1L, 1L, "A", Sexo.M, idadeAnos * 12);
		c.setId(id);
		when(profRepo.findByCidadaoIdAndProfissao(id, Profissao.AGRICULTOR))
				.thenReturn(Optional.of(new CidadaoProfissao(id, Profissao.AGRICULTOR, pe)));
		return c;
	}

	private double ef(Cidadao c) {
		return service.eficiencia(c, Profissao.AGRICULTOR, 1.0, vila);
	}

	@Test
	void eficienciaBasica() {
		assertThat(ef(cidadao(1, 30, 5))).isCloseTo(1.0, within(1e-9));
	}

	@Test
	void menorDe14a17Anos() {
		assertThat(ef(cidadao(1, 15, 5))).isCloseTo(0.5, within(1e-9));
	}

	@Test
	void faminto() {
		Cidadao c = cidadao(1, 30, 5);
		c.setFamintoTurnos(2);
		assertThat(ef(c)).isCloseTo(0.5, within(1e-9));
	}

	@Test
	void bemAlimentada() {
		vila.setBemAlimentada(true);
		assertThat(ef(cidadao(1, 30, 5))).isCloseTo(1.1, within(1e-9));
	}

	@Test
	void bonusLiderCar10() {
		vila.setFamiliaLiderId(7L);
		Cidadao lider = new Cidadao(1L, 7L, "L", Sexo.M, 40 * 12);
		lider.setId(99L);
		lider.setCar(10);
		Cidadao crianca = new Cidadao(1L, 7L, "C", Sexo.F, 5 * 12);
		crianca.setId(98L);
		crianca.setCar(20);
		when(cidRepo.findByFamiliaIdAndVivoTrue(7L)).thenReturn(List.of(crianca, lider));
		assertThat(ef(cidadao(1, 30, 5))).isCloseTo(1.05, within(1e-9));
	}

	@Test
	void liderMaisVelhoEBonusMaximo10() {
		vila.setFamiliaLiderId(7L);
		Cidadao novo = new Cidadao(1L, 7L, "N", Sexo.M, 20 * 12);
		novo.setId(2L);
		novo.setCar(30);
		Cidadao velho = new Cidadao(1L, 7L, "V", Sexo.M, 60 * 12);
		velho.setId(3L);
		velho.setCar(30);
		when(cidRepo.findByFamiliaIdAndVivoTrue(7L)).thenReturn(List.of(novo, velho));
		assertThat(service.bonusLider(vila)).isCloseTo(0.10, within(1e-9));
	}

	@Test
	void semFamiliaLiderOuSemAdultoSemBonus() {
		assertThat(service.bonusLider(vila)).isZero();
		vila.setFamiliaLiderId(7L);
		Cidadao crianca = new Cidadao(1L, 7L, "C", Sexo.F, 5 * 12);
		crianca.setId(98L);
		crianca.setCar(20);
		when(cidRepo.findByFamiliaIdAndVivoTrue(7L)).thenReturn(List.of(crianca));
		assertThat(service.bonusLider(vila)).isZero();
	}

	@Test
	void produtoComPropdItensEMultiplicadorNivel() {
		assertThat(EficienciaService.calcular(5, 5, 30, false, false, 0, 0.05, 1.0)).isCloseTo(1.05, within(1e-9));
		assertThat(EficienciaService.calcular(5, 5, 30, false, false, 0, 0, 0.5)).isCloseTo(0.5, within(1e-9));
	}

	@Test
	void capada() {
		// base 3,5 -> 3,0 ; x1,1 x1,1 x1,1 = 3,993 -> 3,0
		assertThat(EficienciaService.calcular(30, 30, 30, false, true, 0.10, 0.10, 1.0)).isEqualTo(3.0);
		vila.setBemAlimentada(true);
		assertThat(ef(cidadao(1, 30, 30))).isEqualTo(3.0);
	}

	@Test
	void zeroPeRende05() {
		Cidadao c = new Cidadao(1L, 1L, "A", Sexo.M, 30 * 12);
		c.setId(5L);
		when(profRepo.findByCidadaoIdAndProfissao(anyLong(), any())).thenReturn(Optional.empty());
		assertThat(ef(c)).isCloseTo(0.5, within(1e-9));
	}

	@Test
	void ferramentaSomaPeNaFormula() {
		Cidadao c = cidadao(1, 30, 2);
		when(bonusEq.bonusFerramenta(1L, Profissao.AGRICULTOR)).thenReturn(3);
		assertThat(ef(c)).isCloseTo(1.0, within(1e-9));
	}

	@Test
	void prodItensMultiplica() {
		Cidadao c = cidadao(1, 30, 5);
		when(bonusEq.prodItens(1L)).thenReturn(8);
		assertThat(ef(c)).isCloseTo(1.08, within(1e-9));
	}

	@Test
	void peBaseZeroComFerramentaAplicaFormula() {
		Cidadao c = new Cidadao(1L, 1L, "A", Sexo.M, 30 * 12);
		c.setId(5L);
		when(profRepo.findByCidadaoIdAndProfissao(anyLong(), any())).thenReturn(Optional.empty());
		when(bonusEq.bonusFerramenta(5L, Profissao.AGRICULTOR)).thenReturn(4);
		assertThat(ef(c)).isCloseTo(0.9, within(1e-9));
	}

	@Test
	void tetoPreservadoComFerramentaGrande() {
		Cidadao c = cidadao(1, 30, 25);
		when(bonusEq.bonusFerramenta(1L, Profissao.AGRICULTOR)).thenReturn(10);
		assertThat(ef(c)).isEqualTo(3.0);
	}

}
