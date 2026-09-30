package com.example.loginbase.jogo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import com.example.loginbase.jogo.config.JogoProperties;

class JogoPropertiesTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withUserConfiguration(Config.class);

	@Test
	void velocidadePadraoEUm() {
		runner.run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context.getBean(JogoProperties.class).getVelocidade()).isEqualTo(1);
		});
	}

	@Test
	void velocidadeConfiguradaEAceita() {
		runner.withPropertyValues("app.jogo.velocidade=3").run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context.getBean(JogoProperties.class).getVelocidade()).isEqualTo(3);
		});
	}

	@Test
	void velocidadeZeroFalhaNaInicializacao() {
		runner.withPropertyValues("app.jogo.velocidade=0").run(context -> assertThat(context).hasFailed());
	}

	@Test
	void velocidadeNegativaFalhaNaInicializacao() {
		runner.withPropertyValues("app.jogo.velocidade=-1").run(context -> assertThat(context).hasFailed());
	}

	@Test
	void expoenteCurvaPadraoEUmVirgulaCinco() {
		runner.run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context.getBean(JogoProperties.class).getExpoenteCurva())
					.isEqualByComparingTo("1.5");
		});
	}

	@Test
	void expoenteCurvaValidoEAceito() {
		runner.withPropertyValues("app.jogo.expoente-curva=2.0")
				.run(context -> assertThat(context).hasNotFailed());
		runner.withPropertyValues("app.jogo.expoente-curva=1.25").run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context.getBean(JogoProperties.class).curvaNiveis().fator(80))
					.isEqualByComparingTo("32");
		});
	}

	@Test
	void expoenteCurvaInvalidoFalhaNaInicializacao() {
		for (String valor : new String[] { "2.5", "0.5", "1.3" }) {
			runner.withPropertyValues("app.jogo.expoente-curva=" + valor)
					.run(context -> assertThat(context).hasFailed());
		}
	}

	@Configuration
	@EnableConfigurationProperties(JogoProperties.class)
	static class Config {
	}

}
