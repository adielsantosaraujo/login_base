package com.example.loginbase.jogo.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CurvaNiveisTest {

	@Test
	void expoentePadraoValores() {
		CurvaNiveis curva = CurvaNiveis.PADRAO;
		assertThat(curva.fator(20)).isEqualByComparingTo("8");
		assertThat(curva.fator(5)).isEqualByComparingTo("1");
		assertThat(curva.fator(10)).isEqualByComparingTo("2.828427124746190097603377448419396");
		assertThat(curva.fator(6)).isEqualByComparingTo("1.314534138012398672296727478721925");
		assertThat(curva.fator(100)).isEqualByComparingTo("89.44271909999158785636694674925104");
	}

	@Test
	void outrosExpoentes() {
		assertThat(new CurvaNiveis(new BigDecimal("1.25")).fator(80)).isEqualByComparingTo("32");
		assertThat(new CurvaNiveis(new BigDecimal("1.75")).fator(80)).isEqualByComparingTo("128");
		assertThat(new CurvaNiveis(new BigDecimal("2.0")).fator(10)).isEqualByComparingTo("4");
		assertThat(new CurvaNiveis(new BigDecimal("1.0")).fator(10)).isEqualByComparingTo("2");
	}

	@ParameterizedTest
	@ValueSource(strings = { "1.0", "1.5", "2.0" })
	void verificarLimitesNaoLancaParaExpoentesValidos(String valor) {
		assertThatCode(() -> new CurvaNiveis(new BigDecimal(valor)).verificarLimites()).doesNotThrowAnyException();
	}

	@Test
	void nivelInvalidoRejeitado() {
		assertThatThrownBy(() -> CurvaNiveis.PADRAO.fator(0)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void construtorRejeitaNulo() {
		assertThatThrownBy(() -> new CurvaNiveis(null)).isInstanceOf(NullPointerException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = { "0.99", "2.01", "2.25", "1.3", "1.1" })
	void construtorRejeitaExpoenteInvalido(String valor) {
		assertThatThrownBy(() -> new CurvaNiveis(new BigDecimal(valor)))
				.isInstanceOf(IllegalArgumentException.class);
	}

}
