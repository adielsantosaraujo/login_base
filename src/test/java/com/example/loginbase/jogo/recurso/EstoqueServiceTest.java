package com.example.loginbase.jogo.recurso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.FonteCapacidadeArmazens.ArmazemAtivo;

class EstoqueServiceTest {

	FonteCapacidadeArmazens fonte = mock(FonteCapacidadeArmazens.class);
	EstoqueService service = new EstoqueService(mock(EstoqueRepository.class), fonte);
	Vila vila = new Vila(1L, "V", 1L, 1);

	private Map<Recurso, BigDecimal> capacidade(ArmazemAtivo... armazens) {
		when(fonte.armazensAtivos(vila)).thenReturn(List.of(armazens));
		return service.calcularCapacidadeTotal(vila);
	}

	private static BigDecimal bd(String v) {
		return new BigDecimal(v);
	}

	@Test
	void capacidadeSemArmazemEBase500ExcetoOuroIlimitado() {
		Map<Recurso, BigDecimal> c = capacidade();
		assertThat(c).hasSize(Recurso.values().length);
		for (Recurso r : Recurso.values()) {
			if (r == Recurso.OURO) {
				assertThat(c.get(r)).isEqualByComparingTo(EstoqueService.CAPACIDADE_ILIMITADA);
			} else {
				assertThat(c.get(r)).isEqualByComparingTo("500");
			}
		}
	}

	@Test
	void capacidadeArmazemN1EficienciaUmSomaQuinhentos() {
		assertThat(capacidade(new ArmazemAtivo(NivelConstrucao.N1, 1, bd("1.0"))).get(Recurso.MADEIRA))
				.isEqualByComparingTo("1000");
	}

	@Test
	void capacidadeArmazemSemMinimoDeCarregadoresNaoSoma() {
		assertThat(capacidade(new ArmazemAtivo(NivelConstrucao.N1, 0, bd("1.0")),
				new ArmazemAtivo(NivelConstrucao.N2, 1, bd("1.0")),
				new ArmazemAtivo(NivelConstrucao.N3, 3, bd("1.0"))).get(Recurso.PEDRA)).isEqualByComparingTo("500");
	}

	@Test
	void capacidadeArmazemN2LimitaEficienciaEmUmVirgulaCinco() {
		assertThat(capacidade(new ArmazemAtivo(NivelConstrucao.N2, 2, bd("2.0"))).get(Recurso.MADEIRA))
				.isEqualByComparingTo("2750");
		assertThat(capacidade(new ArmazemAtivo(NivelConstrucao.N2, 2, bd("0.8"))).get(Recurso.MADEIRA))
				.isEqualByComparingTo("1700");
	}

	@Test
	void capacidadeSomaVariosArmazensAtivos() {
		assertThat(capacidade(new ArmazemAtivo(NivelConstrucao.N1, 1, bd("1")),
				new ArmazemAtivo(NivelConstrucao.N3, 4, bd("1"))).get(Recurso.SAL)).isEqualByComparingTo("5000");
	}

}
