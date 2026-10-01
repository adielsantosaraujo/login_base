package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.OuroService;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 2 do pipeline: Ouro passivo (imposto e Estalagem). Delega ao OuroService. */
@Component
public class EtapaOuroPassivo implements EtapaTurno {

	private final OuroService ouroService;

	public EtapaOuroPassivo(OuroService ouroService) {
		this.ouroService = ouroService;
	}

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
		ouroService.processarOuroPassivo(vila, turno);
	}

}
