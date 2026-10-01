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
@RequestMapping("/api/jogo/construcoes/{id}/alocacoes")
public class AlocacaoController {

	private final VilaAtual vilaAtual;
	private final AlocacaoService service;

	public AlocacaoController(VilaAtual vilaAtual, AlocacaoService service) {
		this.vilaAtual = vilaAtual;
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AlocacaoDTO alocar(@PathVariable Long id, @RequestBody AlocacaoRequest req) {
		return service.alocar(vilaAtual.obter(), id, req.cidadaoId(), req.profissao());
	}

	@DeleteMapping("/{cidadaoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void desalocar(@PathVariable Long id, @PathVariable Long cidadaoId) {
		service.desalocar(vilaAtual.obter(), id, cidadaoId);
	}

	@GetMapping
	public List<AlocacaoDTO> listar(@PathVariable Long id) {
		return service.listar(vilaAtual.obter(), id);
	}

}
