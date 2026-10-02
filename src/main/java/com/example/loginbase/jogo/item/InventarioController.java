package com.example.loginbase.jogo.item;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.item.AprimoramentoService.AprimorarRequest;
import com.example.loginbase.jogo.item.AprimoramentoService.InventarioDTO;
import com.example.loginbase.jogo.item.AprimoramentoService.ItemDetalheDTO;
import com.example.loginbase.jogo.servico.VilaAtual;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jogo/inventario")
@RequiredArgsConstructor
public class InventarioController {

	private final VilaAtual vilaAtual;
	private final AprimoramentoService service;

	@GetMapping
	public InventarioDTO listar(@RequestParam(required = false) ItemCategoria categoria,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.listar(vilaAtual.obter(), categoria, page, size);
	}

	@GetMapping("/{itemId}")
	public ItemDetalheDTO detalhe(@PathVariable Long itemId) {
		return service.detalhe(vilaAtual.obter(), itemId);
	}

	@PostMapping("/{itemId}/aprimorar")
	@ResponseStatus(HttpStatus.CREATED)
	public FabricacaoDTO aprimorar(@PathVariable Long itemId, @RequestBody AprimorarRequest req) {
		return service.aprimorar(vilaAtual.obter(), itemId, req);
	}
}
