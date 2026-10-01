package com.example.loginbase.jogo.cidadao;

import static com.example.loginbase.jogo.cidadao.Caracteristica.*;

import java.util.List;

/** Profissões do cidadão e as características que dão bônus de PE (docs/jogo/v1-002-cidadaos/cidadao.md). */
public enum Profissao {

	CONSTRUTOR(INT),
	CARREGADOR(FOR, VEL),
	AGRICULTOR(INT),
	FAZENDEIRO(INT),
	MINEIRO(FOR),
	MADEIREIRO(FOR, VIT),
	FERREIRO(INT),
	COZINHEIRO(VEL, CAR),
	COSTUREIRO(VEL, CAR),
	CACADOR(VIT, VEL, CAR),
	GUERREIRO(FOR, VIT, VEL),
	COMERCIANTE(CAR);

	private final List<Caracteristica> caracteristicas;

	Profissao(Caracteristica... caracteristicas) {
		this.caracteristicas = List.of(caracteristicas);
	}

	public List<Caracteristica> getCaracteristicas() {
		return caracteristicas;
	}

}
