package com.example.loginbase.jogo.item;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
public class OficinaController {

	private final VilaAtual vilaAtual;
	private final FabricacaoService service;

	@GetMapping("/oficinas/{id}")
	public OficinaDTO oficina(@PathVariable Long id) {
		return service.oficina(vilaAtual.obter(), id);
	}

	@GetMapping("/oficinas/{id}/fila")
	public List<FabricacaoDTO> fila(@PathVariable Long id) {
		return service.fila(vilaAtual.obter(), id);
	}

	@GetMapping("/oficinas/{id}/receitas")
	public List<ReceitaDTO> receitas(@PathVariable Long id) {
		return service.receitas(vilaAtual.obter(), id);
	}

	@PostMapping("/oficinas/{id}/fabricacoes")
	@ResponseStatus(HttpStatus.CREATED)
	public FabricacaoDTO criar(@PathVariable Long id, @RequestBody CriarFabricacaoRequest req) {
		return service.criar(vilaAtual.obter(), id, req);
	}

	@PatchMapping("/fabricacoes/{id}")
	public FabricacaoDTO reatribuir(@PathVariable Long id, @RequestBody ReatribuirArtesaoRequest req) {
		return service.reatribuir(vilaAtual.obter(), id, req.artesaoId());
	}
}
