package com.example.loginbase.jogo.catalogo;

import java.util.List;

/**
 * Masmorra de nível 1 a 5, com a composição de inimigos (em ordem de spawn,
 * S1..Sn conforme o tamanho da lista) sobre o {@link MapaMasmorra#PADRAO}.
 */
public record Masmorra(int nivel, List<TipoInimigo> composicaoInimigos) {

	public Masmorra {
		if (nivel < 1 || nivel > 5) {
			throw new IllegalArgumentException("Nível de masmorra inválido: " + nivel);
		}
		composicaoInimigos = List.copyOf(composicaoInimigos);
	}

	public MapaMasmorra mapa() {
		return MapaMasmorra.PADRAO;
	}

}
