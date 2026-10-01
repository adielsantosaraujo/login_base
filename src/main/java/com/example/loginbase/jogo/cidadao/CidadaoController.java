package com.example.loginbase.jogo.cidadao;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.servico.VilaAtual;

@RestController
@RequestMapping("/api/jogo")
public class CidadaoController {

	private final VilaAtual vilaAtual;
	private final CidadaoService service;

	public CidadaoController(VilaAtual vilaAtual, CidadaoService service) {
		this.vilaAtual = vilaAtual;
		this.service = service;
	}

	@GetMapping("/cidadao/{id}")
	public CidadaoDTO obter(@PathVariable Long id) {
		return service.obter(vilaAtual.obter(), id);
	}

	@PostMapping("/cidadao/{id}/distribuir-pontos")
	public CidadaoDTO distribuirPontos(@PathVariable Long id, @RequestBody DistribuirPontosRequest req) {
		return service.distribuirPontos(vilaAtual.obter(), id, req);
	}

	@GetMapping("/cidadaos")
	public List<CidadaoResumoDTO> listar(@RequestParam(name = "elegiveisTrabalho", defaultValue = "false") boolean elegiveis) {
		return service.listar(vilaAtual.obter(), elegiveis);
	}

}
