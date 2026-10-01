package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 6 do pipeline: Fabricação. Stub: a lógica fica para seu épico/lote. */
@Component
public class EtapaFabricacao implements EtapaTurno {

	@Override
	public int ordem() {
		return 6;
	}

	@Override
	public String nome() {
		return "Fabricação";
	}

	@Override
	public void executar(Vila vila, int turno) {
		// stub
	}

}
