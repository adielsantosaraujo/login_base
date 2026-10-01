package com.example.loginbase.jogo.recurso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.servico.VilaAtual;

@RestController
@RequestMapping("/api/jogo/estoque")
public class EstoqueController {

	private final VilaAtual vilaAtual;
	private final EstoqueService estoqueService;

	public EstoqueController(VilaAtual vilaAtual, EstoqueService estoqueService) {
		this.vilaAtual = vilaAtual;
		this.estoqueService = estoqueService;
	}

	@GetMapping
	public EstoqueDTO estoque() {
		Vila vila = vilaAtual.obter();
		Map<Recurso, BigDecimal> quantidades = estoqueService.listar(vila);
		Map<Recurso, BigDecimal> capacidades = estoqueService.calcularCapacidadeTotal(vila);
		List<EstoqueDTO.LinhaEstoque> linhas = new ArrayList<>();
		for (Recurso r : Recurso.values()) {
			BigDecimal quantidade = quantidades.get(r);
			BigDecimal capacidade = capacidades.get(r);
			boolean ilimitada = capacidade == null || capacidade.compareTo(EstoqueService.CAPACIDADE_ILIMITADA) >= 0;
			BigDecimal percentual = null;
			if (!ilimitada) {
				percentual = capacidade.signum() == 0 ? BigDecimal.ZERO
						: quantidade.multiply(BigDecimal.valueOf(100)).divide(capacidade, 2, RoundingMode.HALF_UP);
			}
			linhas.add(new EstoqueDTO.LinhaEstoque(r.name(), r.getNomeExibicao(), quantidade,
					ilimitada ? null : capacidade, percentual));
		}
		return new EstoqueDTO(linhas);
	}

}
