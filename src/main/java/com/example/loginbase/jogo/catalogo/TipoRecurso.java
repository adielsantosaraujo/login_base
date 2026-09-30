package com.example.loginbase.jogo.catalogo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tipos de recurso armazenáveis pela vila. Os valores acumulados são
 * guardados em milésimos ({@code long}, 1 unidade = 1000) para acumular
 * produção fracionária sem perda de precisão; a API expõe {@code floor(valor/1000)}.
 */
public enum TipoRecurso {

	COMIDA,
	MADEIRA,
	PEDRA,
	FERRO;

	/**
	 * Capacidade de armazenamento por recurso (igual para todos) para o nível
	 * informado do armazém: até o nível 5, {@code 500 × 2^(nivel-1)}; acima,
	 * {@code round_half_up(8000 × (N/5)^p)}.
	 *
	 * @param nivelArmazem nível do armazém (1 a {@link TipoPredio#NIVEL_MAXIMO})
	 * @param curva        curva de níveis configurada
	 */
	public static long capacidadeArmazem(int nivelArmazem, CurvaNiveis curva) {
		if (nivelArmazem < 1 || nivelArmazem > TipoPredio.NIVEL_MAXIMO) {
			throw new IllegalArgumentException("Nível de armazém inválido: " + nivelArmazem);
		}
		if (nivelArmazem <= CurvaNiveis.NIVEL_BASE) {
			return 500L << (nivelArmazem - 1);
		}
		return BigDecimal.valueOf(8000).multiply(curva.fator(nivelArmazem)).setScale(0, RoundingMode.HALF_UP)
				.longValueExact();
	}

}
