package com.example.loginbase.jogo.controlador;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.dto.MapaDTO;
import com.example.loginbase.jogo.dto.RegiaoDetalheDTO;
import com.example.loginbase.jogo.servico.MapaService;
import com.example.loginbase.jogo.servico.VilaAtual;

@RestController
@RequestMapping("/api/jogo")
public class MapaControlador {

	private final VilaAtual vilaAtual;
	private final MapaService mapaService;

	public MapaControlador(VilaAtual vilaAtual, MapaService mapaService) {
		this.vilaAtual = vilaAtual;
		this.mapaService = mapaService;
	}

	@GetMapping("/vila/mapa")
	public MapaDTO mapa() {
		return mapaService.obterMapaVila(vilaAtual.obter());
	}

	@GetMapping("/regioes/{indice}")
	public RegiaoDetalheDTO regiao(@PathVariable int indice) {
		return mapaService.obterRegiaoDetalhada(vilaAtual.obter(), indice);
	}

}
