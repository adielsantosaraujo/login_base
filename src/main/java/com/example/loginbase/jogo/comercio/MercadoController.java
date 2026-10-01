package com.example.loginbase.jogo.comercio;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.servico.VilaAtual;

@RestController
@RequestMapping("/api/jogo/mercado")
public class MercadoController {

	private final VilaAtual vilaAtual;
	private final MercadoService mercadoService;

	public MercadoController(VilaAtual vilaAtual, MercadoService mercadoService) {
		this.vilaAtual = vilaAtual;
		this.mercadoService = mercadoService;
	}

	private static final int LIMITE_HISTORICO = 50;

	@GetMapping("/precos")
	public PrecosMercadoDTO precos() {
		return mercadoService.precos(vilaAtual.obter());
	}

	@GetMapping("/ordens")
	public List<OrdemComercioDTO> historico() {
		return mercadoService.historico(vilaAtual.obter()).stream().limit(LIMITE_HISTORICO)
				.map(OrdemComercioDTO::de).toList();
	}

	@PostMapping("/ordens")
	@ResponseStatus(HttpStatus.OK)
	public OrdemResultadoDTO executarOrdem(@RequestBody OrdemRequest req) {
		if (req == null || req.recurso() == null || req.tipo() == null || req.quantidade() == null) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Recurso, tipo e quantidade são obrigatórios");
		}
		Recurso recurso;
		TipoOrdem tipo;
		try {
			recurso = Recurso.valueOf(req.recurso().trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Recurso inválido");
		}
		try {
			tipo = TipoOrdem.valueOf(req.tipo().trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Tipo de ordem inválido");
		}
		return mercadoService.executarOrdem(vilaAtual.obter(), recurso, tipo, req.quantidade());
	}

}
