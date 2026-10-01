package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 3 do pipeline: Consumo de comida. Stub: a lógica fica para seu épico/lote. */
@Component
public class EtapaConsumoComida implements EtapaTurno {

	@Override
	public int ordem() {
		return 3;
	}

	@Override
	public String nome() {
		return "Consumo de comida";
	}

	@Override
	public void executar(Vila vila, int turno) {
		// stub
	}

}
