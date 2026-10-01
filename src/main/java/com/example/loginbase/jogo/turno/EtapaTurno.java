package com.example.loginbase.jogo.turno;

import com.example.loginbase.jogo.modelo.Vila;

/** Um passo do pipeline de resolução do turno por vila (Strategy). */
public interface EtapaTurno {

	/** Posição no pipeline, de 1 a 13. */
	int ordem();

	String nome();

	void executar(Vila vila, int turno);

}
