package com.example.loginbase.jogo.construcao;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.servico.VilaAtual;

@RestController
@RequestMapping("/api/jogo/construcoes/{id}/marcacoes")
public class MarcacaoController {

	private final VilaAtual vilaAtual;
	private final MarcacaoService marcacaoService;

	public MarcacaoController(VilaAtual vilaAtual, MarcacaoService marcacaoService) {
		this.vilaAtual = vilaAtual;
		this.marcacaoService = marcacaoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MarcacaoDTO marcar(@PathVariable Long id, @RequestBody MarcarLadrilhoRequest req) {
		return MarcacaoDTO.de(marcacaoService.marcar(vilaAtual.obter(), id, req.x(), req.y()));
	}

	@DeleteMapping("/{x}/{y}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void desmarcar(@PathVariable Long id, @PathVariable int x, @PathVariable int y) {
		marcacaoService.desmarcar(vilaAtual.obter(), id, x, y);
	}

	@GetMapping
	public List<MarcacaoDTO> listar(@PathVariable Long id) {
		return marcacaoService.listar(vilaAtual.obter(), id).stream().map(MarcacaoDTO::de).toList();
	}

}
