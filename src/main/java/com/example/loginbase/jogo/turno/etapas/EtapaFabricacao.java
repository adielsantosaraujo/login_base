package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.item.FabricacaoTurnoService;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 6 do pipeline: Fabricação. Delega ao FabricacaoTurnoService. */
@Component
public class EtapaFabricacao implements EtapaTurno {

	private final FabricacaoTurnoService servico;

	public EtapaFabricacao(FabricacaoTurnoService servico) {
		this.servico = servico;
	}

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
		servico.processar(vila, turno);
	}

}
