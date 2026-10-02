package com.example.loginbase.jogo.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemSubtipo {
	ESPADA(ItemCategoria.ARMA, "Espada"),
	LANCA(ItemCategoria.ARMA, "Lança"),
	ARCO(ItemCategoria.ARMA, "Arco"),
	BESTA(ItemCategoria.ARMA, "Besta"),
	MARTELO(ItemCategoria.FERRAMENTA, "Martelo"),
	CARRINHO_DE_MAO(ItemCategoria.FERRAMENTA, "Carrinho de mão"),
	ENXADA(ItemCategoria.FERRAMENTA, "Enxada"),
	FORCADO(ItemCategoria.FERRAMENTA, "Forcado"),
	PICARETA(ItemCategoria.FERRAMENTA, "Picareta"),
	MACHADO(ItemCategoria.FERRAMENTA, "Machado"),
	MALHO(ItemCategoria.FERRAMENTA, "Malho"),
	CUTELO(ItemCategoria.FERRAMENTA, "Cutelo"),
	KIT_DE_COSTURA(ItemCategoria.FERRAMENTA, "Kit de costura"),
	FACA_DE_CACA(ItemCategoria.FERRAMENTA, "Faca de caça"),
	BALANCA(ItemCategoria.FERRAMENTA, "Balança"),
	PEITORAL(ItemCategoria.ARMADURA, "Peitoral"),
	CAPACETE(ItemCategoria.ARMADURA, "Capacete"),
	OMBREIRAS(ItemCategoria.ARMADURA, "Ombreiras"),
	LUVAS(ItemCategoria.ARMADURA, "Luvas"),
	CALCAS(ItemCategoria.ARMADURA, "Calças"),
	SAPATO(ItemCategoria.ARMADURA, "Sapato"),
	COLAR(ItemCategoria.JOIA, "Colar"),
	ANEL(ItemCategoria.JOIA, "Anel");

	private final ItemCategoria categoria;
	private final String nomeExibicao;
}
