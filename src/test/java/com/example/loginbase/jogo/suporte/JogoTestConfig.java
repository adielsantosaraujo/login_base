package com.example.loginbase.jogo.suporte;

import java.time.Instant;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Sobrescreve os beans {@code Clock} e {@code Aleatorio} de
 * {@code JogoConfig} por versões de teste ({@link RelogioAjustavel} e
 * {@link AleatorioSequencia}), determinísticas e controláveis pelo teste.
 */
@TestConfiguration
public class JogoTestConfig {

	@Bean
	@Primary
	public RelogioAjustavel clock() {
		return new RelogioAjustavel(Instant.parse("2024-01-01T00:00:00Z"));
	}

	@Bean
	@Primary
	public AleatorioSequencia aleatorio() {
		return new AleatorioSequencia();
	}

}
