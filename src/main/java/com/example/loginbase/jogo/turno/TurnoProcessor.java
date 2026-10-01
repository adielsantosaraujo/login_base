package com.example.loginbase.jogo.turno;

/** Processa o turno global. Executado dentro da transação e da trava do agendador. */
public interface TurnoProcessor {

	void processarTurno(int numero);

}
