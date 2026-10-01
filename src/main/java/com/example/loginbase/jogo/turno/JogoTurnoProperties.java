package com.example.loginbase.jogo.turno;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "jogo.turno")
@Getter
@Setter
public class JogoTurnoProperties {

	/** Liga/desliga o agendador do turno global. */
	private boolean habilitado = true;

	/** Intervalo, em minutos reais, entre turnos. */
	private long intervaloMinutos = 60;

}
