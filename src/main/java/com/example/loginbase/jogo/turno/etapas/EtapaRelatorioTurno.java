package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 13 do pipeline: Relatório do turno. Stub: a lógica fica para seu épico/lote. */
@Component
public class EtapaRelatorioTurno implements EtapaTurno {

	@Override
	public int ordem() {
		return 13;
	}

	@Override
	public String nome() {
		return "Relatório do turno";
	}

	@Override
	public void executar(Vila vila, int turno) {
		// stub
	}

}
