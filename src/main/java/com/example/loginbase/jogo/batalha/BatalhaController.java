package com.example.loginbase.jogo.batalha;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.servico.VilaAtual;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jogo/batalhas")
@RequiredArgsConstructor
public class BatalhaController {

	private final VilaAtual vilaAtual;
	private final BatalhaConsultaService service;

	@GetMapping
	public List<ResumoBatalhaDTO> listar() {
		return service.listar(vilaAtual.obter());
	}

	@GetMapping("/{id}")
	public DetalheBatalhaDTO detalhe(@PathVariable Long id) {
		return service.detalhe(vilaAtual.obter(), id);
	}
}
