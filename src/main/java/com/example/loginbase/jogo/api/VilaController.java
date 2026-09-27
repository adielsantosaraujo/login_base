package com.example.loginbase.jogo.api;

import java.time.Clock;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.economia.EstadoVila;
import com.example.loginbase.jogo.economia.VilaService;

/**
 * API REST da vila e do catálogo de regras do jogo (ver design.md, seção 17,
 * e spec game-village). Requer autenticação (401 anônimo, ver
 * {@code SecurityConfig}); cada usuário só acessa a própria vila.
 */
@RestController
@RequestMapping("/api/jogo")
@Transactional
public class VilaController {

	private final VilaService vilaService;
	private final JogoMapper jogoMapper;
	private final Clock clock;
	private final JogoProperties jogoProperties;

	public VilaController(VilaService vilaService, JogoMapper jogoMapper, Clock clock, JogoProperties jogoProperties) {
		this.vilaService = vilaService;
		this.jogoMapper = jogoMapper;
		this.clock = clock;
		this.jogoProperties = jogoProperties;
	}

	@GetMapping("/vila")
	public ResponseEntity<VilaDto> consultarVila(Authentication auth) {
		long usuarioId = UsuarioAtual.id(auth);
		EstadoVila estado = vilaService.consultar(usuarioId);
		VilaDto dto = jogoMapper.toVilaDto(estado, clock);
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/catalogo")
	public ResponseEntity<CatalogoDto> catalogo() {
		CatalogoDto dto = jogoMapper.toCatalogoDto(jogoProperties);
		return ResponseEntity.ok(dto);
	}

}
