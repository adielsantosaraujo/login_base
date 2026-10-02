package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.quartel.TreinamentoQuartelService;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 7 do pipeline: Quartel (treino). Delega ao TreinamentoQuartelService. */
@Component
public class EtapaQuartel implements EtapaTurno {

	private final TreinamentoQuartelService servico;

	public EtapaQuartel(TreinamentoQuartelService servico) {
		this.servico = servico;
	}

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
		servico.processar(vila, turno);
	}

}
