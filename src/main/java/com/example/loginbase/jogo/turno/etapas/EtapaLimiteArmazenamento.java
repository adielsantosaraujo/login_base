package com.example.loginbase.jogo.turno.etapas;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.turno.EtapaTurno;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/** Passo 4 do pipeline: Limite de armazenamento; o excedente é perdido e registrado. */
@Component
public class EtapaLimiteArmazenamento implements EtapaTurno {

	private final EstoqueService estoqueService;
	private final RegistroEventoTurnoService registro;

	public EtapaLimiteArmazenamento(EstoqueService estoqueService, RegistroEventoTurnoService registro) {
		this.estoqueService = estoqueService;
		this.registro = registro;
	}

	@Override
	public int ordem() {
		return 4;
	}

	@Override
	public String nome() {
		return "Limite de armazenamento";
	}

	@Override
	public void executar(Vila vila, int turno) {
		estoqueService.aplicarLimiteArmazenamento(vila, turno).forEach((recurso, perda) -> registro.registrar(vila,
				turno, TipoEventoTurno.ESTOQUE_PERDIDO,
				"Perdido por falta de espaço: " + perda.stripTrailingZeros().toPlainString() + " de "
						+ recurso.getNomeExibicao(),
				Map.<String, Object>of("recurso", recurso.name(), "quantidade", perda.toPlainString())));
	}

}
