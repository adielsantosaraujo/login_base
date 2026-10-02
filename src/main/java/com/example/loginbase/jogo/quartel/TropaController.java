package com.example.loginbase.jogo.quartel;

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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jogo")
@RequiredArgsConstructor
public class TropaController {

	private final VilaAtual vilaAtual;
	private final TropaService service;
	private final ExpedicaoService expedicaoService;

	@GetMapping("/quarteis/{id}")
	public QuartelDTO quartel(@PathVariable Long id) {
		return service.quartel(vilaAtual.obter(), id);
	}

	@GetMapping("/quarteis/{id}/guerreiros-disponiveis")
	public List<GuerreiroDisponivelDTO> guerreirosDisponiveis(@PathVariable Long id) {
		return service.guerreirosDisponiveis(vilaAtual.obter(), id);
	}

	@PostMapping("/quarteis/{id}/tropas")
	@ResponseStatus(HttpStatus.CREATED)
	public TropaDTO criar(@PathVariable Long id, @RequestBody CriarTropaRequest req) {
		return service.criar(vilaAtual.obter(), id, req);
	}

	@GetMapping("/tropas/{id}")
	public TropaDTO tropa(@PathVariable Long id) {
		return service.tropa(vilaAtual.obter(), id);
	}

	@PostMapping("/tropas/{id}/membros")
	@ResponseStatus(HttpStatus.CREATED)
	public TropaDTO adicionarMembro(@PathVariable Long id, @RequestBody MembroTropaRequest req) {
		return service.adicionarMembro(vilaAtual.obter(), id, req);
	}

	@DeleteMapping("/tropas/{id}/membros/{cidadaoId}")
	public TropaDTO removerMembro(@PathVariable Long id, @PathVariable Long cidadaoId) {
		return service.removerMembro(vilaAtual.obter(), id, cidadaoId);
	}

	@DeleteMapping("/tropas/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void desfazer(@PathVariable Long id) {
		service.desfazer(vilaAtual.obter(), id);
	}

	@GetMapping("/tropas/{id}/destinos")
	public List<DestinoExpedicaoDTO> destinos(@PathVariable Long id) {
		return expedicaoService.destinos(vilaAtual.obter(), id);
	}

	@PostMapping("/tropas/{id}/expedicao")
	@ResponseStatus(HttpStatus.CREATED)
	public TropaDTO enviarExpedicao(@PathVariable Long id, @RequestBody ExpedicaoRequest req) {
		return expedicaoService.enviar(vilaAtual.obter(), id, req);
	}
}
