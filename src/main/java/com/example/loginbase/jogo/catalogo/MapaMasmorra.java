package com.example.loginbase.jogo.catalogo;

import java.util.List;
import java.util.Map;

/**
 * Mapa tático 8×8 usado por todas as masmorras: obstáculos, posições
 * iniciais do jogador e pontos de spawn dos inimigos (S1–S5).
 */
public record MapaMasmorra(int largura, int altura, List<Posicao> obstaculos, List<Posicao> posicoesJogador,
		Map<String, Posicao> spawnsInimigos) {

	public MapaMasmorra {
		obstaculos = List.copyOf(obstaculos);
		posicoesJogador = List.copyOf(posicoesJogador);
		spawnsInimigos = Map.copyOf(spawnsInimigos);
	}

	/** Posição no grid tático (x, y), 0-indexado. */
	public record Posicao(int x, int y) {
	}

	public static final MapaMasmorra PADRAO = new MapaMasmorra(
			8, 8,
			List.of(
					new Posicao(3, 2), new Posicao(4, 2),
					new Posicao(1, 3), new Posicao(6, 3),
					new Posicao(1, 4), new Posicao(6, 4),
					new Posicao(3, 5), new Posicao(4, 5)),
			List.of(
					new Posicao(2, 7), new Posicao(3, 7),
					new Posicao(4, 7), new Posicao(5, 7)),
			Map.of(
					"S1", new Posicao(3, 0),
					"S2", new Posicao(2, 1),
					"S3", new Posicao(5, 1),
					"S4", new Posicao(1, 0),
					"S5", new Posicao(6, 0)));

}
