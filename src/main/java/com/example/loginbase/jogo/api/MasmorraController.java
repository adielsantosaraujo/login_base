package com.example.loginbase.jogo.api;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.dominio.Batalha;
import com.example.loginbase.jogo.masmorra.MasmorraService;
import com.example.loginbase.jogo.masmorra.combate.AcaoCombate;

/**
 * API REST de masmorras e batalhas (ver design.md, seção 17, e spec
 * game-dungeon-combat/game-dungeon-loot). Requer autenticação (401 anônimo) e,
 * por serem {@code POST}, token CSRF válido (403 sem ele) — ver
 * {@code SecurityConfig}. Cada usuário só acessa batalhas da própria vila
 * ({@link MasmorraService} devolve 404 caso contrário).
 */
@RestController
@RequestMapping("/api/jogo")
@Transactional
public class MasmorraController {

	private final MasmorraService masmorraService;
	private final JogoMapper jogoMapper;

	public MasmorraController(MasmorraService masmorraService, JogoMapper jogoMapper) {
		this.masmorraService = masmorraService;
		this.jogoMapper = jogoMapper;
	}

	@PostMapping("/masmorras/{nivel}/batalhas")
	public ResponseEntity<BatalhaDto> iniciar(@PathVariable int nivel, @Valid @RequestBody IniciarBatalhaRequest req,
			Authentication auth) {
		long usuarioId = UsuarioAtual.id(auth);
		Batalha batalha = masmorraService.iniciar(usuarioId, nivel, req.unidadeIds());
		return ResponseEntity.status(HttpStatus.CREATED).body(jogoMapper.toBatalhaDto(batalha));
	}

	@GetMapping("/batalhas/{id}")
	public ResponseEntity<BatalhaDto> consultar(@PathVariable long id, Authentication auth) {
		long usuarioId = UsuarioAtual.id(auth);
		Batalha batalha = masmorraService.consultar(usuarioId, id);
		return ResponseEntity.ok(jogoMapper.toBatalhaDto(batalha));
	}

	@PostMapping("/batalhas/{id}/acoes")
	public ResponseEntity<BatalhaDto> agir(@PathVariable long id, @Valid @RequestBody AcaoCombateRequest req,
			Authentication auth) {
		long usuarioId = UsuarioAtual.id(auth);
		AcaoCombate acao = new AcaoCombate(req.tipo(), req.combatenteId(), valorOuZero(req.x()),
				valorOuZero(req.y()), req.alvoId(), req.turno());
		Batalha batalha = masmorraService.agir(usuarioId, id, acao);
		return ResponseEntity.ok(jogoMapper.toBatalhaDto(batalha));
	}

	/** {@code AcaoCombate.x()/y()} são {@code int} primitivos; só têm sentido para {@code MOVER}. */
	private static int valorOuZero(Integer valor) {
		return valor != null ? valor : 0;
	}

}
