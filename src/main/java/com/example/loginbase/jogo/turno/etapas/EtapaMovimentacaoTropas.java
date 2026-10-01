package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 8 do pipeline: Movimentação de tropas. Stub: a lógica fica para seu épico/lote. */
@Component
public class EtapaMovimentacaoTropas implements EtapaTurno {

	@Override
	public int ordem() {
		return 8;
	}

	@Override
	public String nome() {
		return "Movimentação de tropas";
	}

	@Override
	public void executar(Vila vila, int turno) {
		// stub
	}

}
