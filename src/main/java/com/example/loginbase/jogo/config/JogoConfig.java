package com.example.loginbase.jogo.config;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans de infraestrutura do jogo: relógio (usado por todo cálculo de tempo,
 * para permitir substituição por {@code RelogioAjustavel} em testes) e fonte
 * de números aleatórios.
 */
@Configuration
@EnableConfigurationProperties(JogoProperties.class)
public class JogoConfig {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	public Aleatorio aleatorio() {
		return new AleatorioPadrao();
	}

}
