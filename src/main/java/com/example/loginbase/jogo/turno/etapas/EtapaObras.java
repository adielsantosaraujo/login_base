package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.construcao.ObraService;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 5 do pipeline: Obras. Delega ao ObraService. */
@Component
public class EtapaObras implements EtapaTurno {

	private final ObraService obraService;

	public EtapaObras(ObraService obraService) {
		this.obraService = obraService;
	}

	@Override
	public int ordem() {
		return 5;
	}

	@Override
	public String nome() {
		return "Obras";
	}

	@Override
	public void executar(Vila vila, int turno) {
		obraService.processarObras(vila, turno);
	}

}
