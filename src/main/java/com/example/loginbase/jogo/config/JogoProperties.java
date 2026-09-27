package com.example.loginbase.jogo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Propriedades de configuração do jogo ({@code app.jogo.*}).
 */
@Getter
@Setter
@Validated
@ConfigurationProperties("app.jogo")
public class JogoProperties {

	/**
	 * Multiplicador de velocidade do jogo (inteiro &gt;= 1): taxas efetivas de
	 * produção são multiplicadas por ele, e tempos efetivos de ordens são
	 * divididos por ele (arredondando para cima).
	 */
	@Min(1)
	private int velocidade = 1;

}
