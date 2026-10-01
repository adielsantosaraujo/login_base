package com.example.loginbase.jogo.dto;

import java.util.List;
import java.util.Map;

/** Prévia das jazidas das 16 regiões para uma semente. */
public record PreviaVilaDTO(long semente, List<Regiao> regioes) {

	/** Contagem de jazidas (por nome do tipo) de uma região. */
	public record Regiao(int indice, Map<String, Integer> jazidas) {
	}

}
