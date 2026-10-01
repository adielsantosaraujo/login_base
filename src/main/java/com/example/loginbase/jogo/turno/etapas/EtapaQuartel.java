package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 7 do pipeline: Quartel. Stub: a lógica fica para seu épico/lote. */
@Component
public class EtapaQuartel implements EtapaTurno {

	@Override
	public int ordem() {
		return 7;
	}

	@Override
	public String nome() {
		return "Quartel";
	}

	@Override
	public void executar(Vila vila, int turno) {
		// stub
	}

}
