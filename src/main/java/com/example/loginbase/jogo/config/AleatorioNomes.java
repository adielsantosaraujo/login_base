package com.example.loginbase.jogo.config;

/**
 * Fonte de números aleatórios dedicada ao sorteio de nomes de unidades
 * ({@code GeradorNomes}). Isolada de {@link Aleatorio} propositalmente: um
 * sorteio de nome no mesmo bean usado por loot/IA deslocaria as sequências
 * determinísticas ({@code AleatorioSequencia}) usadas nos testes daquelas
 * regras (ver design.md D2).
 *
 * <p>
 * Delega a um {@link Aleatorio} próprio, injetado no construtor — em
 * produção uma instância dedicada de {@code AleatorioPadrao} (bean
 * {@code aleatorioNomes} em {@code JogoConfig}); em testes, uma
 * {@code AleatorioSequencia} construída diretamente (sem afetar o bean
 * {@code Aleatorio} principal).
 */
public class AleatorioNomes {

	private final Aleatorio aleatorio;

	public AleatorioNomes(Aleatorio aleatorio) {
		this.aleatorio = aleatorio;
	}

	/**
	 * Retorna um índice pseudoaleatório entre {@code 0} (inclusive) e
	 * {@code tamanho} (exclusive).
	 */
	public int proximoEntre(int tamanho) {
		return aleatorio.proximoInt(tamanho);
	}

}
