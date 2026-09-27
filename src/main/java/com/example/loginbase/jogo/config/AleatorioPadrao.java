package com.example.loginbase.jogo.config;

import java.util.random.RandomGenerator;

/**
 * Implementação padrão de {@link Aleatorio}, baseada no
 * {@link RandomGenerator} default da JDK. Não é criptograficamente segura,
 * o que é suficiente para as regras de jogo (loot, IA de combate).
 */
public class AleatorioPadrao implements Aleatorio {

	@Override
	public int proximoInt(int limite) {
		return RandomGenerator.getDefault().nextInt(limite);
	}

}
