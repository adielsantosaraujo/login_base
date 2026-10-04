package com.example.loginbase.jogo.modelo;

import static com.example.loginbase.jogo.modelo.TipoTerreno.*;

import java.util.List;

/**
 * Tipo de uma região possuída pela vila.
 */
public enum TipoRegiao {
	FLORESTA("Floresta", TipoTerreno.FLORESTA, BARREIRO, PLANTACOES),
	PLANICIE("Planície", PLANTACOES, CRIACOES, TipoTerreno.FLORESTA),
	URBANA("Urbana", INDUSTRIA, COMERCIO, DESENVOLVIMENTO),
	LITORAL("Litoral", SALINAS, ENXOFRE, MILITAR),
	MONTANHA("Montanha", ROCHA, FERRO, CARVAO);

	private final String nomeExibicao;
	private final List<TipoTerreno> terrenos;

	TipoRegiao(String nomeExibicao, TipoTerreno... terrenos) {
		this.nomeExibicao = nomeExibicao;
		this.terrenos = List.of(terrenos);
	}

	public String getNomeExibicao() {
		return nomeExibicao;
	}

	public List<TipoTerreno> terrenos() {
		return terrenos;
	}

	/** Os 5 tipos válidos atualmente. */
	public static List<TipoRegiao> atuais() {
		return List.of(FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA);
	}
}
