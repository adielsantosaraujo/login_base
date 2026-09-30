package com.example.loginbase.jogo.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.example.loginbase.jogo.catalogo.CurvaNiveis;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

	/**
	 * Expoente {@code p} da curva de níveis {@code (N/5)^p} (faixa 1,0 a 2,0, múltiplo de 0,25).
	 */
	@NotNull
	@DecimalMin("1.0")
	@DecimalMax("2.0")
	private BigDecimal expoenteCurva = new BigDecimal("1.5");

	/**
	 * Curva de níveis construída a partir do expoente configurado. Sem prefixo {@code get} para o
	 * binder não tratá-la como propriedade.
	 */
	public CurvaNiveis curvaNiveis() {
		return new CurvaNiveis(expoenteCurva);
	}

	/**
	 * Valida que o expoente forma uma curva válida (múltiplo de 0,25) e que as fórmulas não sofrem
	 * overflow ({@link CurvaNiveis#verificarLimites()}).
	 */
	@AssertTrue(message = "app.jogo.expoente-curva deve ser múltiplo de 0,25 entre 1,0 e 2,0")
	public boolean isCurvaNiveisValida() {
		try {
			new CurvaNiveis(expoenteCurva).verificarLimites();
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

}
