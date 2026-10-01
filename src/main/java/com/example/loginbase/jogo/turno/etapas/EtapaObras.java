package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 5 do pipeline: Obras. Stub: a lógica fica para seu épico/lote. */
@Component
public class EtapaObras implements EtapaTurno {

	@Override
	public int ordem() {
		return 5;
	}

	@Override
	public String nome() {
		return "Obras";
	}

	@Override
	public void executar(Vila vila, int turno) {
		// stub
	}

}
