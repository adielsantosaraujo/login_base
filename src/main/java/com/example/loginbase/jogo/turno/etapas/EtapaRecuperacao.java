package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 11 do pipeline: Recuperação. Stub: a lógica fica para seu épico/lote. */
@Component
public class EtapaRecuperacao implements EtapaTurno {

	@Override
	public int ordem() {
		return 11;
	}

	@Override
	public String nome() {
		return "Recuperação";
	}

	@Override
	public void executar(Vila vila, int turno) {
		// stub
	}

}
