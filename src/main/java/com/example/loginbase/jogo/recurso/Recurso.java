package com.example.loginbase.jogo.recurso;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Recursos do jogo (tabela 3.1 da documentação). */
@Getter
@RequiredArgsConstructor
public enum Recurso {
	MADEIRA("Madeira"),
	PEDRA("Pedra"),
	ARGILA("Argila"),
	MINERIO_DE_FERRO("Minério de ferro"),
	CARVAO("Carvão"),
	SAL("Sal"),
	ENXOFRE("Enxofre"),
	GRAOS("Grãos"),
	FIBRA("Fibra"),
	CARNE("Carne"),
	COURO("Couro"),
	LA("Lã"),
	TABUA("Tábua"),
	TIJOLO("Tijolo"),
	FERRO("Ferro"),
	ACO("Aço"),
	TECIDO("Tecido"),
	COURO_CURTIDO("Couro curtido"),
	REFEICAO("Refeição"),
	OURO("Ouro");

	private final String nomeExibicao;

}
