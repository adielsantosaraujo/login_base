package com.example.loginbase.jogo.controlador;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.dto.AnexacaoDTO;
import com.example.loginbase.jogo.dto.CustoAnexacaoDTO;
import com.example.loginbase.jogo.servico.AnexacaoService;
import com.example.loginbase.jogo.servico.VilaAtual;

@RestController
@RequestMapping("/api/jogo/regioes")
public class RegiaoControlador {

	private final VilaAtual vilaAtual;
	private final AnexacaoService anexacaoService;

	public RegiaoControlador(VilaAtual vilaAtual, AnexacaoService anexacaoService) {
		this.vilaAtual = vilaAtual;
		this.anexacaoService = anexacaoService;
	}

	@PostMapping("/{indice}/anexar")
	public AnexacaoDTO anexar(@PathVariable int indice) {
		return anexacaoService.anexarRegiao(vilaAtual.obter(), indice);
	}

	/** Custo da próxima anexação (independe da região escolhida). */
	@GetMapping("/{indice}/custo-anexacao")
	public CustoAnexacaoDTO custo(@PathVariable int indice) {
		return anexacaoService.calcularCustoAnexacao(vilaAtual.obter());
	}

}
