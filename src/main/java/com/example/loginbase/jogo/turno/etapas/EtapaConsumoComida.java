package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.AlimentacaoService;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 3 do pipeline: Consumo de comida. Delega ao AlimentacaoService. */
@Component
public class EtapaConsumoComida implements EtapaTurno {

	private final AlimentacaoService alimentacaoService;

	public EtapaConsumoComida(AlimentacaoService alimentacaoService) {
		this.alimentacaoService = alimentacaoService;
	}

	@Override
	public int ordem() {
		return 3;
	}

	@Override
	public String nome() {
		return "Consumo de comida";
	}

	@Override
	public void executar(Vila vila, int turno) {
		alimentacaoService.processarConsumoAlimentacao(vila, turno);
	}

}
