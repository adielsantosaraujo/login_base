package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.ProducaoService;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 1 do pipeline: Produção (coleta, rural e, futuramente, fábricas), delegada a {@link ProducaoService}. */
@Component
public class EtapaProducao implements EtapaTurno {

	private final ProducaoService producaoService;

	public EtapaProducao(ProducaoService producaoService) {
		this.producaoService = producaoService;
	}

	@Override
	public int ordem() {
		return 1;
	}

	@Override
	public String nome() {
		return "Produção";
	}

	@Override
	public void executar(Vila vila, int turno) {
		producaoService.processarProducao(vila, turno);
	}

}
