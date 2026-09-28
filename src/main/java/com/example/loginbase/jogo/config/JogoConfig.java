package com.example.loginbase.jogo.config;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans de infraestrutura do jogo: relógio (usado por todo cálculo de tempo,
 * para permitir substituição por {@code RelogioAjustavel} em testes) e fontes
 * de números aleatórios. {@code aleatorioNomes} usa uma instância de
 * {@code AleatorioPadrao} própria (não a do bean {@code aleatorio}) para não
 * deslocar as sequências determinísticas de loot/IA nos testes (design.md
 * D2).
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

	@Bean
	public AleatorioNomes aleatorioNomes() {
		return new AleatorioNomes(new AleatorioPadrao());
	}

}
