package com.example.loginbase.jogo.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.comum.UsuarioAtual;
import com.example.loginbase.jogo.dto.CriarVilaRequest;
import com.example.loginbase.jogo.dto.CriarVilaRespostaDTO;
import com.example.loginbase.jogo.dto.PreviaMapaDTO;
import com.example.loginbase.jogo.dto.VilaResumoDTO;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.servico.VilaPreviaService;
import com.example.loginbase.jogo.servico.VilaService;

@RestController
@RequestMapping("/api/jogo/vila")
public class VilaControlador {

	private final UsuarioAtual usuarioAtual;
	private final VilaService vilaService;
	private final VilaPreviaService previaService;

	public VilaControlador(UsuarioAtual usuarioAtual, VilaService vilaService, VilaPreviaService previaService) {
		this.usuarioAtual = usuarioAtual;
		this.vilaService = vilaService;
		this.previaService = previaService;
	}

	@PostMapping
	public ResponseEntity<CriarVilaRespostaDTO> criar(@RequestBody CriarVilaRequest req) {
		Vila vila = vilaService.criarVila(usuarioAtual.idUsuario(), req.previaId(), req.indices());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new CriarVilaRespostaDTO(vila.getId(), "DISTRIBUIR_POPULACAO"));
	}

	@GetMapping
	public VilaResumoDTO obter() {
		return vilaService.resumo(usuarioAtual.idUsuario());
	}

	@PostMapping("/previa")
	public PreviaMapaDTO gerarPrevia() {
		return previaService.gerar(usuarioAtual.idUsuario());
	}

	@GetMapping("/previa")
	public PreviaMapaDTO obterPrevia() {
		return previaService.obter(usuarioAtual.idUsuario());
	}

}
