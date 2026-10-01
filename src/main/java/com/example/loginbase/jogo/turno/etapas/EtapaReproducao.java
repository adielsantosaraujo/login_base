package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.cidadao.ImigracaoService;
import com.example.loginbase.jogo.cidadao.ReproducaoService;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 9 do pipeline: Reprodução. Reprodução e nascimentos; a imigração entra depois, nesta mesma etapa. */
@Component
public class EtapaReproducao implements EtapaTurno {

	private final ReproducaoService reproducaoService;

	private final ImigracaoService imigracaoService;

	public EtapaReproducao(ReproducaoService reproducaoService, ImigracaoService imigracaoService) {
		this.reproducaoService = reproducaoService;
		this.imigracaoService = imigracaoService;
	}

	@Override
	public int ordem() {
		return 9;
	}

	@Override
	public String nome() {
		return "Reprodução";
	}

	@Override
	public void executar(Vila vila, int turno) {
		reproducaoService.processar(vila, turno);
		imigracaoService.processarImigracao(vila, turno);
	}

}
