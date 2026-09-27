package com.example.loginbase.jogo.config;

/**
 * Fonte de números aleatórios usada pelas regras de jogo (loot, IA de
 * combate). Abstraída em interface para permitir substituição por sequência
 * determinística em testes ({@code AleatorioSequencia}).
 */
public interface Aleatorio {

	/**
	 * Retorna um inteiro pseudoaleatório entre {@code 0} (inclusive) e
	 * {@code limite} (exclusive).
	 */
	int proximoInt(int limite);

}
