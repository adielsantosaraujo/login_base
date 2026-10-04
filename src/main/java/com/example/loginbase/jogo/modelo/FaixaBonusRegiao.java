package com.example.loginbase.jogo.modelo;

/**
 * Faixa de valor (inclusiva, em %) do bônus de uma região, conforme a posição do bônus (1 a 3).
 */
public enum FaixaBonusRegiao {
	POSICAO_1(35, 50),
	POSICAO_2(16, 34),
	POSICAO_3(5, 15);

	private final int min;
	private final int max;

	FaixaBonusRegiao(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public static FaixaBonusRegiao de(int posicao) {
		if (posicao < 1 || posicao > 3) {
			throw new IllegalArgumentException("Posição de bônus inválida: " + posicao);
		}
		return values()[posicao - 1];
	}
}
