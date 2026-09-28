package com.example.loginbase.jogo.suporte;

import java.time.Instant;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.example.loginbase.jogo.config.AleatorioNomes;
import com.example.loginbase.jogo.config.AleatorioPadrao;
import com.example.loginbase.jogo.dominio.ContadorNomeRepository;
import com.example.loginbase.jogo.quartel.GeradorNomes;
import com.example.loginbase.jogo.quartel.NumeradorNomes;

/**
 * Sobrescreve os beans {@code Clock} e {@code Aleatorio} de
 * {@code JogoConfig} por versões de teste ({@link RelogioAjustavel} e
 * {@link AleatorioSequencia}), determinísticas e controláveis pelo teste.
 * Também expõe {@link GeradorNomes} (e o {@link AleatorioNomes} de que
 * depende) para os testes que importam {@code AplicadorOrdens}, já que
 * {@code JogoConfig} — que os define em produção — não é importado nestas
 * fatias {@code @DataJpaTest}. Usa {@link AleatorioPadrao} (aleatoriedade
 * real, não sequência fixa) para não esgotar rapidamente com os múltiplos
 * sorteios de nome/sobrenome de um lote de treino. Expõe ainda
 * {@link NumeradorNomes}, outra dependência de {@code AplicadorOrdens}
 * desde a task 2.5.
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

	@Bean
	public AleatorioNomes aleatorioNomes() {
		return new AleatorioNomes(new AleatorioPadrao());
	}

	@Bean
	public GeradorNomes geradorNomes(AleatorioNomes aleatorioNomes) {
		return new GeradorNomes(aleatorioNomes);
	}

	@Bean
	public NumeradorNomes numeradorNomes(ContadorNomeRepository contadorNomeRepository) {
		return new NumeradorNomes(contadorNomeRepository);
	}

}
