package com.example.loginbase.jogo.modelo;

import static com.example.loginbase.jogo.modelo.BonusRegiao.*;

import java.util.List;

/**
 * Tipo de uma região possuída pela vila.
 */
public enum TipoRegiao {
	FLORESTA("Floresta", BonusRegiao.FLORESTA, BARREIRO, PLANTACOES),
	PLANICIE("Planície", PLANTACOES, CRIACOES, BonusRegiao.FLORESTA),
	URBANA("Urbana", INDUSTRIA, COMERCIO, DESENVOLVIMENTO),
	LITORAL("Litoral", SALINAS, ENXOFRE, MILITAR),
	MONTANHA("Montanha", ROCHA, FERRO, CARVAO);

	private final String nomeExibicao;
	private final List<BonusRegiao> bonus;

	TipoRegiao(String nomeExibicao, BonusRegiao... bonus) {
		this.nomeExibicao = nomeExibicao;
		this.bonus = List.of(bonus);
	}

	public String getNomeExibicao() {
		return nomeExibicao;
	}

	public List<BonusRegiao> bonus() {
		return bonus;
	}

	/** Os 5 tipos válidos atualmente. */
	public static List<TipoRegiao> atuais() {
		return List.of(FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA);
	}
}
