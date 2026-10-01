package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 1 do pipeline: Produção. Stub: a lógica fica para seu épico/lote. */
@Component
public class EtapaProducao implements EtapaTurno {

	@Override
	public int ordem() {
		return 1;
	}

	@Override
	public String nome() {
		return "Produção";
	}

	@Override
	public void executar(Vila vila, int turno) {
		// stub
	}

}
