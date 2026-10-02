package com.example.loginbase.jogo.batalha;

import java.util.Random;

/** Fórmula de dano (seção 10.2). */
public final class CalculoDano {

	private CalculoDano() {
	}

	/** Defesa x 0,75 quando o atacante é Besta ou Xamã orc; senão a própria defesa. */
	public static double defesaEfetiva(double defesa, boolean atacanteIgnora25) {
		return atacanteIgnora25 ? defesa * 0.75 : defesa;
	}

	/** dano = max(1; round(ataque x 100 / (100 + 3 x defesaEfetiva) x U(0,9; 1,1))). Consome um nextDouble(). */
	public static int dano(double ataque, double defesaEfetiva, Random rng) {
		double u = 0.9 + 0.2 * rng.nextDouble();
		long dano = Math.round(ataque * 100 / (100 + 3 * defesaEfetiva) * u);
		return (int) Math.max(1, dano);
	}
}
