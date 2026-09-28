package com.example.loginbase.jogo.catalogo;

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
	 * informado do armazém: {@code 500 × 2^(nivel-1)}.
	 *
	 * @param nivelArmazem nível do armazém (1 a 100)
	 */
	public static long capacidadeArmazem(int nivelArmazem) {
		if (nivelArmazem < 1 || nivelArmazem > 100) {
			throw new IllegalArgumentException("Nível de armazém inválido: " + nivelArmazem);
		}
		return 500L << (nivelArmazem - 1);
	}

}
