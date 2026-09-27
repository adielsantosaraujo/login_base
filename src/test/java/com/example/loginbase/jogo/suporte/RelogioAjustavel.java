package com.example.loginbase.jogo.suporte;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * {@link Clock} mutável para testes: o instante atual só muda quando
 * {@link #avancar(Duration)} é chamado, permitindo simular a passagem de
 * tempo de forma determinística.
 */
public class RelogioAjustavel extends Clock {

	private final ZoneId zona;
	private Instant instante;

	public RelogioAjustavel(Instant instanteInicial) {
		this(instanteInicial, ZoneId.of("UTC"));
	}

	public RelogioAjustavel(Instant instanteInicial, ZoneId zona) {
		this.instante = instanteInicial;
		this.zona = zona;
	}

	/**
	 * Avança o relógio pela duração informada (aceita duração negativa, para
	 * retroceder).
	 */
	public void avancar(Duration duracao) {
		this.instante = this.instante.plus(duracao);
	}

	@Override
	public ZoneId getZone() {
		return zona;
	}

	@Override
	public Clock withZone(ZoneId zonaNova) {
		return new RelogioAjustavel(instante, zonaNova);
	}

	@Override
	public Instant instant() {
		return instante;
	}

}
