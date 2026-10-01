package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 10 do pipeline: Envelhecimento. Stub: a lógica fica para seu épico/lote. */
@Component
public class EtapaEnvelhecimento implements EtapaTurno {

	@Override
	public int ordem() {
		return 10;
	}

	@Override
	public String nome() {
		return "Envelhecimento";
	}

	@Override
	public void executar(Vila vila, int turno) {
		// stub
	}

}
