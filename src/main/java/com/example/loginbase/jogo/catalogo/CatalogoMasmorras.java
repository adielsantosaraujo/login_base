package com.example.loginbase.jogo.catalogo;

import static com.example.loginbase.jogo.catalogo.TipoInimigo.ESQUELETO_ARQUEIRO;
import static com.example.loginbase.jogo.catalogo.TipoInimigo.GOBLIN;
import static com.example.loginbase.jogo.catalogo.TipoInimigo.ORC;
import static com.example.loginbase.jogo.catalogo.TipoInimigo.TROLL;

import java.util.List;
import java.util.Map;

/**
 * Catálogo estático das masmorras (níveis 1 a 5), com suas composições de
 * inimigos. Jogador entra em masmorras até {@code masmorra_nivel_liberado};
 * vitória no nível N libera {@code min(5, N+1)}.
 */
public final class CatalogoMasmorras {

	public static final int NIVEL_MAXIMO = 5;

	private static final Map<Integer, Masmorra> MASMORRAS = Map.of(
			1, new Masmorra(1, List.of(GOBLIN, GOBLIN, GOBLIN)),
			2, new Masmorra(2, List.of(ESQUELETO_ARQUEIRO, GOBLIN, GOBLIN, GOBLIN)),
			3, new Masmorra(3, List.of(ORC, GOBLIN, GOBLIN, ESQUELETO_ARQUEIRO, ESQUELETO_ARQUEIRO)),
			4, new Masmorra(4, List.of(ORC, ORC, ORC, ESQUELETO_ARQUEIRO, ESQUELETO_ARQUEIRO)),
			5, new Masmorra(5, List.of(TROLL, ORC, ORC, ESQUELETO_ARQUEIRO, ESQUELETO_ARQUEIRO)));

	private CatalogoMasmorras() {
	}

	public static Masmorra porNivel(int nivel) {
		Masmorra masmorra = MASMORRAS.get(nivel);
		if (masmorra == null) {
			throw new IllegalArgumentException("Nível de masmorra inválido: " + nivel);
		}
		return masmorra;
	}

}
