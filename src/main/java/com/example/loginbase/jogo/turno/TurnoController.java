package com.example.loginbase.jogo.turno;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.servico.VilaAtual;

@RestController
@RequestMapping("/api/jogo/turno")
public class TurnoController {

	private final TurnoService turnoService;
	private final VilaAtual vilaAtual;

	public TurnoController(TurnoService turnoService, VilaAtual vilaAtual) {
		this.turnoService = turnoService;
		this.vilaAtual = vilaAtual;
	}

	@GetMapping
	public TurnoDTO turnoAtual() {
		return turnoService.getTurnoAtual();
	}

	@GetMapping("/eventos")
	public RelatorioTurnoDTO eventos(@RequestParam(required = false) Integer turno) {
		return turnoService.getEventos(vilaAtual.obter(), turno);
	}

}
