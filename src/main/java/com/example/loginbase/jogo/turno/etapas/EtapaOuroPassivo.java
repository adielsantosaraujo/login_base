package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 2 do pipeline: Ouro passivo. Stub: a lógica fica para seu épico/lote. */
@Component
public class EtapaOuroPassivo implements EtapaTurno {

	@Override
	public int ordem() {
		return 2;
	}

	@Override
	public String nome() {
		return "Ouro passivo";
	}

	@Override
	public void executar(Vila vila, int turno) {
		// stub
	}

}
