package com.example.loginbase.jogo.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Qualidade {
	SIMPLES(0, 0),
	BOA(1, 1),
	EXCELENTE(3, 2),
	DIVINA(5, 3);

	private final int slotsPedra;
	private final int qtdBonus;
}
