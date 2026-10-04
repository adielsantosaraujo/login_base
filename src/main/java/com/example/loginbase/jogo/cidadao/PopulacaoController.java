package com.example.loginbase.jogo.cidadao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jogo/vila/populacao")
public class PopulacaoController {

	private final PopulacaoService service;

	public PopulacaoController(PopulacaoService service) {
		this.service = service;
	}

	@GetMapping
	public PopulacaoDTO obter() {
		return service.obter();
	}

	@PostMapping
	public ConfirmacaoPopulacaoDTO confirmar(@RequestBody DistribuirPopulacaoRequest req) {
		return service.confirmar(req);
	}

}
