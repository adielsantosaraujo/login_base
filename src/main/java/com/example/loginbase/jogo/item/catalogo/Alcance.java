package com.example.loginbase.jogo.item.catalogo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Alcance {
	CORPO_A_CORPO_FRENTE("Corpo a corpo, só da linha de frente"),
	CORPO_A_CORPO_FLEX("Corpo a corpo, da frente ou da retaguarda"),
	DISTANCIA("À distância, qualquer alvo");

	private final String descricao;
}
