package com.example.loginbase.jogo.pedra;

import lombok.Getter;

@Getter
public enum TipoPedra {
	SIMPLES(1, 10),
	BOA(2, 25),
	EXCELENTE(3, 60),
	DIVINA(4, 150);

	private final int qtdBonus;
	private final int custoEngaste;

	TipoPedra(int qtdBonus, int custoEngaste) {
		this.qtdBonus = qtdBonus;
		this.custoEngaste = custoEngaste;
	}
}
