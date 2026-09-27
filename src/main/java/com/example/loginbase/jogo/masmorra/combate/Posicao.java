package com.example.loginbase.jogo.masmorra.combate;

/**
 * Posição no grid tático (x, y), 0-indexado, usada pelo motor de combate.
 */
public record Posicao(int x, int y) {

	/** Distância Manhattan (sem diagonais) até {@code outro}. */
	public int distanciaManhattan(Posicao outro) {
		return Math.abs(x - outro.x) + Math.abs(y - outro.y);
	}

}
