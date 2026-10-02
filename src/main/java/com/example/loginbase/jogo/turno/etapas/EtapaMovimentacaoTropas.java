package com.example.loginbase.jogo.turno.etapas;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.quartel.MovimentacaoTropasService;
import com.example.loginbase.jogo.turno.EtapaTurno;

/** Passo 8 do pipeline: Movimentação de tropas. Delega ao MovimentacaoTropasService. */
@Component
public class EtapaMovimentacaoTropas implements EtapaTurno {

	private final MovimentacaoTropasService servico;

	public EtapaMovimentacaoTropas(MovimentacaoTropasService servico) {
		this.servico = servico;
	}

	@Override
	public int ordem() {
		return 8;
	}

	@Override
	public String nome() {
		return "Movimentação de tropas";
	}

	@Override
	public void executar(Vila vila, int turno) {
		servico.processar(vila, turno);
	}

}
