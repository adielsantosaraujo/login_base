package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 9 do pipeline: Reprodução. Stub: a lógica fica para seu épico/lote. */
@Component
public class EtapaReproducao implements EtapaTurno {

	@Override
	public int ordem() {
		return 9;
	}

	@Override
	public String nome() {
		return "Reprodução";
	}

	@Override
	public void executar(Vila vila, int turno) {
		// stub
	}

}
