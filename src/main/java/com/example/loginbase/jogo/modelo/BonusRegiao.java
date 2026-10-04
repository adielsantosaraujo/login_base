package com.example.loginbase.jogo.modelo;

/**
 * Bônus que uma região pode conceder à vila.
 */
public enum BonusRegiao {
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

	BonusRegiao(String nomeExibicao) {
		this.nomeExibicao = nomeExibicao;
	}

	public String getNomeExibicao() {
		return nomeExibicao;
	}
}
