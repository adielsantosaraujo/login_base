package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.masmorra.MasmorraService;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 12 do pipeline: Masmorras (evolução e surgimento). */
@Component
public class EtapaMasmorras implements EtapaTurno {

	private final MasmorraService masmorraService;

	public EtapaMasmorras(MasmorraService masmorraService) {
		this.masmorraService = masmorraService;
	}

	@Override
	public int ordem() {
		return 12;
	}

	@Override
	public String nome() {
		return "Masmorras";
	}

	@Override
	public void executar(Vila vila, int turno) {
		masmorraService.processar(vila, turno);
	}

}
