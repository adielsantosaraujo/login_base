package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.cidadao.EnvelhecimentoService;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 10 do pipeline: Envelhecimento. Delega ao EnvelhecimentoService. */
@Component
public class EtapaEnvelhecimento implements EtapaTurno {

	private final EnvelhecimentoService envelhecimentoService;

	public EtapaEnvelhecimento(EnvelhecimentoService envelhecimentoService) {
		this.envelhecimentoService = envelhecimentoService;
	}

	@Override
	public int ordem() {
		return 10;
	}

	@Override
	public String nome() {
		return "Envelhecimento";
	}

	@Override
	public void executar(Vila vila, int turno) {
		envelhecimentoService.processar(vila, turno);
	}

}
