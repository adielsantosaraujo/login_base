package com.example.loginbase.jogo.ferraria;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.ferraria.EngasteService.EngasteRequest;
import com.example.loginbase.jogo.ferraria.EngasteService.EngasteResultadoDTO;
import com.example.loginbase.jogo.ferraria.EngasteService.ItemCompativelDTO;
import com.example.loginbase.jogo.ferraria.EngasteService.RemocaoResultadoDTO;
import com.example.loginbase.jogo.ferraria.EngasteService.RemoverPedraRequest;
import com.example.loginbase.jogo.pedra.PedraDTO;
import com.example.loginbase.jogo.servico.VilaAtual;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EngasteController {

	private final VilaAtual vilaAtual;
	private final EngasteService service;

	@GetMapping("/api/jogo/pedras")
	public List<PedraDTO> pedras() {
		return service.pedras(vilaAtual.obter());
	}

	@GetMapping("/api/jogo/ferraria/itens-compativeis")
	public List<ItemCompativelDTO> itensCompativeis() {
		return service.itensCompativeis(vilaAtual.obter());
	}

	@PostMapping("/api/jogo/ferraria/engaste")
	public EngasteResultadoDTO engastar(@RequestBody EngasteRequest req) {
		return service.engastar(vilaAtual.obter(), req);
	}

	@PostMapping("/api/jogo/ferraria/remover-pedra")
	public RemocaoResultadoDTO remover(@RequestBody RemoverPedraRequest req) {
		return service.remover(vilaAtual.obter(), req);
	}
}
