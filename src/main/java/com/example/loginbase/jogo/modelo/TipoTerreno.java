package com.example.loginbase.jogo.modelo;

/**
 * Tipo de terreno de um ladrilho (13 tipos). Cada tipo de região tem 3 tipos de terreno.
 */
public enum TipoTerreno {
	FLORESTA("Floresta"),
	BARREIRO("Barreiro"),
	PLANTACOES("Plantações"),
	CRIACOES("Criações"),
	ROCHA("Rocha"),
	FERRO("Ferro"),
	CARVAO("Carvão"),
	SALINAS("Salinas"),
	ENXOFRE("Enxofre"),
	MILITAR("Militar"),
	INDUSTRIA("Indústria"),
	COMERCIO("Comércio"),
	DESENVOLVIMENTO("Desenvolvimento");

	private final String nomeExibicao;

	TipoTerreno(String nomeExibicao) {
		this.nomeExibicao = nomeExibicao;
	}

	public String getNomeExibicao() {
		return nomeExibicao;
	}
}
