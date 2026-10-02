package com.example.loginbase.jogo.item;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.servico.VilaAtual;

@RestController
@RequestMapping("/api/jogo/cidadao/{id}/equipamento")
public class EquipamentoController {

	public record EquiparRequest(Long itemId) {
	}

	private final VilaAtual vilaAtual;
	private final EquipamentoService service;

	public EquipamentoController(VilaAtual vilaAtual, EquipamentoService service) {
		this.vilaAtual = vilaAtual;
		this.service = service;
	}

	@PutMapping("/{slot}")
	public Map<String, ItemDTO> equipar(@PathVariable Long id, @PathVariable String slot,
			@RequestBody EquiparRequest req) {
		return service.equipar(vilaAtual.obter(), id, slot, req == null ? null : req.itemId());
	}

	@DeleteMapping("/{slot}")
	public Map<String, ItemDTO> desequipar(@PathVariable Long id, @PathVariable String slot) {
		return service.desequipar(vilaAtual.obter(), id, slot);
	}
}
