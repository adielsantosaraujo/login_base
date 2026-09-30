package com.example.loginbase.jogo.catalogo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

/**
 * Curva de níveis do jogo: fator {@code (N/5)^p} em {@link BigDecimal}, onde {@code N} é o nível
 * e {@code p} o expoente configurável ({@code app.jogo.expoente-curva}).
 *
 * <p>O expoente deve estar em [1,0; 2,0] e ser múltiplo de 0,25, o que permite calcular a potência
 * de forma determinística (sem {@code double}) usando apenas potência inteira e raízes quadradas
 * em {@link MathContext#DECIMAL128}.
 *
 * <p>{@link #verificarLimites()} garante, na inicialização, que nenhuma fórmula que usa a curva
 * sofre overflow em {@code long}.
 */
public record CurvaNiveis(BigDecimal expoente) {

	public static final int NIVEL_BASE = 5;

	public static final BigDecimal EXPOENTE_PADRAO = new BigDecimal("1.5");

	public static final BigDecimal EXPOENTE_MINIMO = new BigDecimal("1.0");

	public static final BigDecimal EXPOENTE_MAXIMO = new BigDecimal("2.0");

	private static final BigDecimal QUATRO = BigDecimal.valueOf(4);

	/** Curva com o expoente padrão (p = 1,5); usada apenas em testes. */
	public static final CurvaNiveis PADRAO = new CurvaNiveis(EXPOENTE_PADRAO);

	public CurvaNiveis {
		Objects.requireNonNull(expoente, "expoente da curva de níveis não pode ser nulo");
		if (expoente.compareTo(EXPOENTE_MINIMO) < 0 || expoente.compareTo(EXPOENTE_MAXIMO) > 0) {
			throw new IllegalArgumentException(
					"expoente da curva de níveis deve estar entre 1,0 e 2,0: " + expoente.toPlainString());
		}
		if (expoente.multiply(QUATRO).stripTrailingZeros().scale() > 0) {
			throw new IllegalArgumentException(
					"expoente da curva de níveis deve ser múltiplo de 0,25: " + expoente.toPlainString());
		}
	}

	/**
	 * Verifica que custos de todos os prédios e a capacidade do armazém (em milésimos) cabem em
	 * {@code long} para os níveis 1 a {@link TipoPredio#NIVEL_MAXIMO}.
	 *
	 * @throws ArithmeticException se alguma fórmula estourar {@code long}
	 */
	public void verificarLimites() {
		for (TipoPredio tipo : TipoPredio.values()) {
			for (int nivel = 1; nivel <= TipoPredio.NIVEL_MAXIMO; nivel++) {
				tipo.custo(nivel, this);
			}
		}
		for (int nivel = 1; nivel <= TipoPredio.NIVEL_MAXIMO; nivel++) {
			Math.multiplyExact(TipoRecurso.capacidadeArmazem(nivel, this), 1000L);
		}
	}

	/**
	 * Calcula {@code (nivel/5)^p}.
	 *
	 * @param nivel nível (&gt;= 1)
	 * @return fator da curva para o nível
	 */
	public BigDecimal fator(int nivel) {
		if (nivel < 1) {
			throw new IllegalArgumentException("nível deve ser >= 1: " + nivel);
		}
		MathContext mc = MathContext.DECIMAL128;
		BigDecimal x = BigDecimal.valueOf(nivel).divide(BigDecimal.valueOf(NIVEL_BASE));
		int q = expoente.multiply(QUATRO).intValueExact();
		int k = q / 4;
		int f = q % 4;
		BigDecimal r = x.pow(k);
		BigDecimal s = x.sqrt(mc);
		BigDecimal t = s.sqrt(mc);
		if (f >= 2) {
			r = r.multiply(s, mc);
		}
		if (f % 2 == 1) {
			r = r.multiply(t, mc);
		}
		return r;
	}

}
