package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.cidadao.RecuperacaoService;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 11 do pipeline: Recuperação de feridos. Delega ao RecuperacaoService. */
@Component
public class EtapaRecuperacao implements EtapaTurno {

	private final RecuperacaoService servico;

	public EtapaRecuperacao(RecuperacaoService servico) {
		this.servico = servico;
	}

	@Override
	public int ordem() {
		return 11;
	}

	@Override
	public String nome() {
		return "Recuperação";
	}

	@Override
	public void executar(Vila vila, int turno) {
		servico.processar(vila, turno);
	}

}
