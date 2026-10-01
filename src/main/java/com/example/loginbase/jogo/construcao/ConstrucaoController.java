package com.example.loginbase.jogo.construcao;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.servico.VilaAtual;


@RestController
@RequestMapping("/api/jogo/construcoes")
public class ConstrucaoController {

	private final VilaAtual vilaAtual;
	private final ConstrucaoService construcaoService;

	public ConstrucaoController(VilaAtual vilaAtual, ConstrucaoService construcaoService) {
		this.vilaAtual = vilaAtual;
		this.construcaoService = construcaoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ConstrucaoDTO criar(@RequestBody CriarConstrucaoRequest req) {
		return ConstrucaoDTO.de(construcaoService.criar(vilaAtual.obter(), req.tipo(), req.regiaoIndice(), req.x(), req.y()));
	}

	@PostMapping("/{id}/upgrade")
	public ConstrucaoDTO upgrade(@PathVariable Long id, @RequestBody(required = false) UpgradeConstrucaoRequest req) {
		UpgradeConstrucaoRequest r = req == null ? new UpgradeConstrucaoRequest(null, null, null) : req;
		return ConstrucaoDTO.de(construcaoService.upgrade(vilaAtual.obter(), id, r.novoNivel(), r.novaX(), r.novaY()));
	}

	@GetMapping("/catalogo")
	public List<CatalogoConstrucaoDTO> catalogo() {
		return construcaoService.catalogo();
	}

	@GetMapping("/{id}")
	public ConstrucaoDTO obter(@PathVariable Long id) {
		return ConstrucaoDTO.de(construcaoService.obter(vilaAtual.obter(), id));
	}

}
