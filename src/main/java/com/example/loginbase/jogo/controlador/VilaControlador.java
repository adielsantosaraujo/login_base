package com.example.loginbase.jogo.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.comum.UsuarioAtual;
import com.example.loginbase.jogo.dto.CriarVilaRequest;
import com.example.loginbase.jogo.dto.PreviaVilaDTO;
import com.example.loginbase.jogo.dto.VilaResumoDTO;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.servico.VilaService;

@RestController
@RequestMapping("/api/jogo/vila")
public class VilaControlador {

	private final UsuarioAtual usuarioAtual;
	private final VilaService vilaService;

	public VilaControlador(UsuarioAtual usuarioAtual, VilaService vilaService) {
		this.usuarioAtual = usuarioAtual;
		this.vilaService = vilaService;
	}

	@PostMapping
	public ResponseEntity<VilaResumoDTO> criar(@RequestBody CriarVilaRequest req) {
		Vila vila = vilaService.criarVila(usuarioAtual.idUsuario(), req.regioesEscolhidas(), req.tipos(),
				req.semente());
		return ResponseEntity.status(HttpStatus.CREATED).body(vilaService.resumo(vila));
	}

	@GetMapping
	public VilaResumoDTO obter() {
		return vilaService.resumo(usuarioAtual.idUsuario());
	}

	@GetMapping("/preview")
	public PreviaVilaDTO preview(@RequestParam(required = false) Long semente) {
		usuarioAtual.idUsuario();
		return vilaService.previa(semente);
	}

}
